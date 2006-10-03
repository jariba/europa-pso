// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequenceImpl.java,v 1.102 2006-10-03 16:14:16 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
//import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.db.PwListenable;
import gov.nasa.arc.planworks.db.PwModel;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwRule;
import gov.nasa.arc.planworks.db.util.PwSequenceFilenameFilter;
import gov.nasa.arc.planworks.db.util.SQLDB;
import gov.nasa.arc.planworks.db.util.PwSQLFilenameFilter;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.FileCopy;
import gov.nasa.arc.planworks.util.FunctorFactory;
import gov.nasa.arc.planworks.util.CreatePartialPlanException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.util.OneToManyMap;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>PwPlanningSequenceImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwPlanningSequenceImpl extends PwListenable implements PwPlanningSequence, 
                                                                    ViewableObject {
  
  private Long id;
  private String projectName;
  private String url; //directory containing the partialplan directories
  private PwModel model;
    //private boolean hasLoadedTransactionFile;
    //private Map transactions;
  private String name;
  private List contentSpec;
  private Map partialPlans; // partialPlanName Map of PwPartialPlan
  private Map ruleMap;
  private List planNamesInDb;
  private List planNamesInFilesystem;

  private long timeSpentLoadingFiles;
  private long timeSpentAnalyzingDatabase;
  /**
   * <code>PwPlanningSequenceImpl</code> - constructor - for CreateProject
   *
   * @param url - <code>String</code> - pathname of planning sequence
   * @param id - <code>Integer</code> - id of sequence
   * @param projectName - <code>String</code> - the project to which the sequence will be added
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPlanningSequenceImpl( final String url, final Long id, final String projectName)
    throws ResourceNotFoundException {
      //hasLoadedTransactionFile = false;
    this.url = url;
    this.id = id;
    this.model = null;
    
    String error = null;
    if((error = validateSequenceDirectory(url)) != null) {
      throw new ResourceNotFoundException("url '" + url + "' is not a valid sequence directory: "
                                          + error);
    }
    int index = url.lastIndexOf( System.getProperty( "file.separator"));
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '" +
                                           System.getProperty( "file.separator") + "'");
    } 
    name = url.substring( index + 1);
    contentSpec = new ArrayList();
   
    partialPlans = new HashMap();
    planNamesInFilesystem = new ArrayList();
    String [] planNames = (new File(url)).list(new StepDirectoryFilter());
    for(int i = 0; i < planNames.length; i++) {
      planNamesInFilesystem.add(planNames[i]);
      partialPlans.put(planNames[i], null);
    }
    planNamesInDb = SQLDB.queryPlanNamesInDatabase(id);
    //loadTransactions();
    //transactions = null;
    instantiateRules( projectName);
  }

  //for testing only
  public PwPlanningSequenceImpl( final String url, final Long id, final boolean forTesting) 
  throws ResourceNotFoundException {
      //hasLoadedTransactionFile = false;
    this.url = url;
    this.id = id;
    this.model = null;
    
    int index = url.lastIndexOf( System.getProperty( "file.separator"));
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '" +
                                           System.getProperty( "file.separator") + "'");
    } 
    name = url.substring( index + 1);
    contentSpec = new ArrayList();
   
    partialPlans = new HashMap();
    planNamesInFilesystem = new ArrayList();
    //transactions = new HashMap();
    fakeRules();
  }

  /**
   * <code>PwPlanningSequenceImpl</code> - constructor - for OpenProject
   *
   * @param url - <code>String</code> - pathname of planning sequence
   * @param project - <code>PwProjectImpl</code> - project to which the sequence will be added
   * @exception ResourceNotFoundException if an error occurs
   */ 
  public PwPlanningSequenceImpl( final String url, final PwProjectImpl project)
    throws ResourceNotFoundException {
      //hasLoadedTransactionFile = false;
    this.url = url;
    this.model = null;
    partialPlans = new HashMap();
    
    int index = url.lastIndexOf( System.getProperty( "file.separator"));
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '" +
                                           System.getProperty( "file.separator") + "'");
    } 
    name = url.substring( index + 1);
    contentSpec = new ArrayList();

    if (! SQLDB.sequenceExists( url)) { 
      File sequenceDir = new File(url);
      if(!sequenceDir.isDirectory()) {
	throw new ResourceNotFoundException("sequence url '" + url + "' is not a directory.");
      }
      String error = null;
      if((error = validateSequenceDirectory(url)) != null) {
	throw new ResourceNotFoundException("url '" + url +
					    "' is not a valid sequence directory: " + error);
      }
      this.id = SQLDB.addSequence(url, project.getId());
    } else {
      this.id = SQLDB.getSequenceId( url);
    }
    String dbUrl = SQLDB.getSequenceUrl(id);
    if(!url.equals(dbUrl)) {
      urlsDontMatchDialog(url, dbUrl);
    }
    //loadTransactionFile();
    if(!SQLDB.statsInDb(this.id))
      loadStatsFile();
    if(!SQLDB.rulesInDb(this.id))
      loadRulesFile();
    SQLDB.analyzeDatabase();
    //loadTransactions();
    //transactions = null;
    planNamesInFilesystem = new ArrayList();
    ListIterator ppNameIterator = SQLDB.queryPartialPlanNames(id).listIterator();
    while(ppNameIterator.hasNext()) {
      String planName = (String) ppNameIterator.next();
      if((new File(url + System.getProperty("file.separator") + planName)).exists()) {
        planNamesInFilesystem.add(planName);
      }
      partialPlans.put(planName, null);
    }
    planNamesInDb = SQLDB.queryPlanNamesInDatabase(id);
    instantiateRules( project.getName());
  } // end constructor for OpenProject call
  

  private void urlsDontMatchDialog(String fsUrl, String dbUrl) {
    System.err.println( "fsUrl '" + fsUrl + "'");
    System.err.println( "dbUrl '" + dbUrl + "'");
    Frame f = new Frame();
    String [] urls = {fsUrl, dbUrl};
    int choice = JOptionPane.showOptionDialog(f, "The URL in the sequence files and the URL from the file selecter don't match.  Please choose one.", "URL Mismatch", JOptionPane.YES_NO_OPTION,
                                              JOptionPane.QUESTION_MESSAGE, null, urls, fsUrl);
    SQLDB.setSequenceUrl(id, urls[choice]);
    url = urls[choice];
  }

//   private void loadTransactionFile() {
//     if(hasLoadedTransactionFile()) {
//       return;
//     }
//     loadTransactionFileDoit();
//   }

//   private void loadTransactionFile( final Long partialPlanId) {
//     if(hasLoadedTransactionFile( partialPlanId)) {
//       return;
//     }
//     loadTransactionFileDoit();
//   }

//   private void loadTransactionFileDoit() {
//     if (isTransactionFileOnDisk()) {
//       long t1 = System.currentTimeMillis();
//       SQLDB.loadFile(url + System.getProperty("file.separator") + DbConstants.SEQ_TRANSACTIONS,
// 		       DbConstants.TBL_TRANSACTION);
//       System.err.println("Loading transaction file took " +
// 			 (System.currentTimeMillis() - t1) + " msecs.");
//       hasLoadedTransactionFile = true;
//     } else {
//       JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), "Sequence " + name,
// 				     "No Transactions Available", JOptionPane.ERROR_MESSAGE);
//     }
//   } // end loadTransactionFileDoit

  private void loadStatsFile() {
    long t1 = System.currentTimeMillis();
    SQLDB.loadFile(url + System.getProperty("file.separator") + DbConstants.SEQ_PP_STATS,
                     DbConstants.TBL_PP_STATS);
    System.err.println("Loading stats file took " + (System.currentTimeMillis() - t1) + " msecs");
  }

  private void loadRulesFile() {
    long t1 = System.currentTimeMillis();
    SQLDB.loadFile(url + System.getProperty("file.separator") + DbConstants.SEQ_RULES, 
                     DbConstants.TBL_RULES);
    System.err.println("Loading rules file took " + (System.currentTimeMillis() - t1) + " msecs");
  }

//   private void loadTransactions() {
//     long t1 = System.currentTimeMillis();
// 		if(!hasLoadedTransactionFile) {
// 			loadTransactionFile();
// 		}
//     int currTransactions = 0;
//     if(transactions != null) {
//       currTransactions = transactions.keySet().size();
//     }
//     if(currTransactions < SQLDB.countTransactions(id)) {
//       transactions = SQLDB.queryTransactions(id);
//     }
//     System.err.println(transactions.keySet().size() + " transactions.");
//     System.err.println("Loading transactions took " + (System.currentTimeMillis() - t1) + "ms");
//   }
  
  // IMPLEMENT INTERFACE 


  /**
   * <code>getUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public String getUrl() {
    return url;
  }

  public Long getId() {
    //return id;
    return new Long(id.longValue());
  }

  /**
   * <code>getModel</code> - lazy construction of model done here
   *
   * @return - <code>PwModel</code> - 
   */
  public PwModel getModel() {
    if (model == null) {
      model = new PwModelImpl();
    }
    return model;
  }

  /**
   * <code>doesPartialPlanExist</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doesPartialPlanExist( final String planName) {
    int step = Integer.parseInt( planName.substring(4));
    return isPartialPlanInDb( step) || isPartialPlanInFilesystem( step);
  }

  /**
   * <code>hasLoadedTransactionFile</code> - for all plan steps
   *
   * @return - <code>boolean</code> - 
   */
//   public boolean hasLoadedTransactionFile() {
//     if (isTransactionFileOnDisk()) {
//       return hasLoadedTransactionFile;
//     } else {
//       int maxStepNumber = getPlanDBSizeList().size() - 1;
//       int maxTransStepNumber = SQLDB.maxStepForTransactionsInDb( id);
//       if (maxTransStepNumber == -1) {
// 	// no transactions in db
// 	return false;
//       } else if (maxTransStepNumber < maxStepNumber) {
// 	JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), "Sequence " + name +
// 				       ": step0 - step" + maxTransStepNumber + " (of " +
// 				       maxStepNumber + ")",
// 				       "Limited Transactions Available", 
// 				       JOptionPane.ERROR_MESSAGE);
//       }
//       return true;
//     }
//   } // end hasLoadedTransactionFile()

  /**
   * <code>hasLoadedTransactionFile</code>
   *
   * @param partialPlanId - <code>Long</code> - 
   * @return - <code>boolean</code> - 
   */
    //  public boolean hasLoadedTransactionFile( final Long partialPlanId) {
//     System.err.println( "hasLoadedTransactionFile(partialPlanId): hasLoadedTransactionFile " +
// 			hasLoadedTransactionFile + " transactionsInDatabaseForStep " +
// 			SQLDB.transactionsInDatabaseForStep( partialPlanId));
//    return hasLoadedTransactionFile || SQLDB.transactionsInDatabaseForStep( partialPlanId);
//  }

  /**
   * <code>hasLoadedTransactionFile</code>
   *
   * @param partialPlanName - <code>String</code> - 
   * @return - <code>boolean</code> - 
   */
  //public boolean hasLoadedTransactionFile( final String partialPlanName) {
//     System.err.println( "hasLoadedTransactionFile(partialPlanName): " + partialPlanName +
// 			" partialPlanId " +
// 			SQLDB.getPartialPlanIdByStepNum
// 			( id, Integer.parseInt( partialPlanName.substring( 4))));
    //return hasLoadedTransactionFile( SQLDB.getPartialPlanIdByStepNum
    //				     ( id, Integer.parseInt( partialPlanName.substring( 4))));
//}

  /**
   * <code>isTransactionFileOnDisk</code>
   *
   * @return - <code>boolean</code> - 
   */
  // public boolean isTransactionFileOnDisk() {
//     return new File( url + System.getProperty("file.separator") +
// 		     DbConstants.SEQ_TRANSACTIONS).exists();
//   }

  /**
   * <code>getTransactionsList</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>List</code> - of PwDBTransaction
   */
  // public List getTransactionsList( final Long partialPlanId) {
//     loadTransactionFile( partialPlanId);
//     return SQLDB.queryTransactionsForStep(id, partialPlanId);
//   } // end listTransactions

//   public List getTransactionsList(final int stepNum) throws IndexOutOfBoundsException {
//     return getTransactionsList(getPartialPlanId(stepNum));
//   }

  /**
   * <code>listPartialPlans</code>
   *    get a list of all partial plans in the planning sequence
   *
   * @return List of PwPartialPlan objects
   */
  public List getPartialPlansList() {
    return CollectionUtils.validValues(partialPlans);
  }

  /**
   * <code>listPartialPlanNames</code>
   *    get a list of the names of every partial plan in the sequence
   *
   * @return List of Strings
   */
  public List getPartialPlanNamesList() {
    List names = new ArrayList(partialPlans.keySet());
    Collections.sort( names, new PartialPlanNameComparator());
    return names;
  }
  /**
   * <code>getPartialPlan</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   * @exception CreatePartialPlanException if the user interrupts the plan creation
   */
  public PwPartialPlan getPartialPlan( final int step)
    throws ResourceNotFoundException, CreatePartialPlanException, IndexOutOfBoundsException {
    if ((step < 0) || (step > (getPlanDBSizeList().size() - 1))) {
      throw new IndexOutOfBoundsException();
    }
    return getPartialPlan( "step" + step);
  } // end getPartialPlan( int)

  /**
   * <code>getPartialPlan</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   * @exception CreatePartialPlanException if the user interrupts the plan creation
   */
  public synchronized PwPartialPlan getPartialPlan( final String planName)
    throws ResourceNotFoundException, CreatePartialPlanException {
    if(!partialPlans.containsKey(planName)) {
      throw new ResourceNotFoundException("plan name '" + planName + "' not found in url " + url);
    }
    PwPartialPlan retval = (PwPartialPlan) partialPlans.get(planName);
    if(retval == null) {
      retval = addPartialPlan(planName);
    }
    return retval;
  } // end getPartialPlan( String)

  /**
   * <code>getPartialPlan</code>
   *
   * @param ppId - <code>Long</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   * @exception CreatePartialPlanException if the user interrupts the plan creation
   */
  public synchronized PwPartialPlan getPartialPlan(final Long ppId)
    throws ResourceNotFoundException, CreatePartialPlanException {
    for(Iterator it = partialPlans.values().iterator(); it.hasNext();) {
      PwPartialPlan pp = (PwPartialPlan) it.next();
      if(pp != null && pp.getId().equals(ppId))
        return pp;
    }
    return addPartialPlan(ppId);
  }

  /**
   * <code>getNextPartialPlan</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   * @exception IndexOutOfBoundsException if an error occurs
   * @exception CreatePartialPlanException if an error occurs
   */
  public PwPartialPlan getNextPartialPlan(final int step)
    throws ResourceNotFoundException, IndexOutOfBoundsException, CreatePartialPlanException {
    return getPartialPlan(step+1);
  }

  /**
   * <code>getNextPartialPlan</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   * @exception IndexOutOfBoundsException if an error occurs
   * @exception CreatePartialPlanException if an error occurs
   */
  public PwPartialPlan getNextPartialPlan(final String planName)
    throws ResourceNotFoundException, IndexOutOfBoundsException, CreatePartialPlanException {
    return getPartialPlan(Integer.parseInt(planName.substring(4)) + 1);
  }
  
  /**
   * <code>getPrevPartialPlan</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   * @exception IndexOutOfBoundsException if an error occurs
   * @exception CreatePartialPlanException if an error occurs
   */
  public PwPartialPlan getPrevPartialPlan(final int step)
    throws ResourceNotFoundException, IndexOutOfBoundsException, CreatePartialPlanException {
    return getPartialPlan(step-1);
  }

  /**
   * <code>getPrevPartialPlan</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   * @exception IndexOutOfBoundsException if an error occurs
   * @exception CreatePartialPlanException if an error occurs
   */
  public PwPartialPlan getPrevPartialPlan(final String planName)
    throws ResourceNotFoundException, IndexOutOfBoundsException, CreatePartialPlanException {
    return getPartialPlan(Integer.parseInt(planName.substring(4)) - 1);
  }

  public void addPartialPlan(final PwPartialPlanImpl partialPlan) {
    partialPlans.put(partialPlan.getName(), partialPlan);
    planNamesInDb.add(partialPlan.getName());
  }

  // for testing only
  public void addPartialPlan(final PwPartialPlanImpl partialPlan, final boolean forTesting) {
    partialPlans.put(partialPlan.getName(), partialPlan);
    planNamesInFilesystem.add(partialPlan.getName());
  }
  
  private PwPartialPlan addPartialPlan(final String partialPlanName) 
    throws ResourceNotFoundException, CreatePartialPlanException {
    if(partialPlans.containsKey(partialPlanName)) {
      try {
        PwPartialPlanImpl partialPlan = null;
	if (doesPartialPlanExist( partialPlanName)) {
	  partialPlan = new PwPartialPlanImpl(url, partialPlanName, this);
	  partialPlans.put(partialPlanName, partialPlan);
	  planNamesInDb.add(partialPlanName);
	  handleEvent(EVT_PP_ADDED);
	} else {
	  // create dummy partial plan for DBTransactionView
	  partialPlan = new PwPartialPlanImpl( partialPlanName, this);
	}
        return partialPlan;
      }
      catch(ResourceNotFoundException rnfe) {
        planNamesInFilesystem.remove(partialPlanName);
        handleEvent(EVT_PP_REMOVED);
        throw rnfe;
      }
    }
    throw new ResourceNotFoundException("Failed to find plan " + partialPlanName +
                                        " in sequence " + name);
  }

  private PwPartialPlan addPartialPlan(final Long ppId)
    throws ResourceNotFoundException, CreatePartialPlanException {
      String planName = SQLDB.getPartialPlanNameById(id, ppId);
      if(planName == null) {
        planNamesInFilesystem.remove(planName);
        handleEvent(EVT_PP_REMOVED);
        throw new ResourceNotFoundException("Failed to find plan " + planName +
                                            "in sequence " + name);
      }
      PwPartialPlanImpl pp = new PwPartialPlanImpl(url, planName, this);
      partialPlans.put(planName, pp);
      planNamesInDb.add(planName);
      handleEvent(EVT_PP_ADDED);
      return pp;
  }

  /**
   * <code>getPartialPlanIfLoaded</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPartialPlan getPartialPlanIfLoaded(final  String planName)
    throws ResourceNotFoundException {
    if (! partialPlans.containsKey( planName)) {
      throw new ResourceNotFoundException("Failed to find plan " + planName +
                                            "in sequence " + name);
    }
    return (PwPartialPlan) partialPlans.get(planName);
  }

  public boolean isPartialPlanLoaded(final int step) {
    return partialPlans.get("step" + step) != null;
  }
  public boolean isPartialPlanInDb(final int step) {
    return planNamesInDb.contains("step" + step);
  }
  public boolean isPartialPlanInFilesystem(final int step) {
    //return (new File(url + System.getProperty("file.separator") + "step" + step)).exists();
    return planNamesInFilesystem.contains("step" + step);
  }

  public void delete() throws ResourceNotFoundException {
    long t1 = System.currentTimeMillis();
    SQLDB.deletePlanningSequence(id);
    System.err.println("Deleting sequence took " + (System.currentTimeMillis() - t1) +
                       " msecs.");
  }

  // implement ViewableObject

  public void setContentSpec(final List spec) {
    contentSpec.clear();
    contentSpec.addAll(spec);
  }
  
  public List getContentSpec() {
    return new ArrayList(contentSpec);
  }

  /**
   * <code>setName</code> - sequenceDir
   *      PlanWorks.renderSequenceView invokes this method 
   *
   * @param name - <code>String</code> - 
   */
  public void setName( final String name) {
    this.name = name;
  }

  /**
   * <code>getName</code> - sequenceDir
   *
   * @return - <code>String</code> - 
   */
  public String getName() {
    return name;
  }

  // end implement ViewableObject

  public List getOpenDecisionsForStep(final int stepNum)
    throws ResourceNotFoundException, CreatePartialPlanException {
    loadPartialPlanFiles("step" + stepNum);
    return SQLDB.queryOpenDecisionsForStep( SQLDB.getPartialPlanIdByStepNum(id, stepNum),
                                              getPartialPlan( stepNum));
  }

//   public Integer getCurrentDecisionIdForStep(final int stepNum) throws ResourceNotFoundException {
//     loadPartialPlanFiles("step" + stepNum);
//     Long partialPlanId = SQLDB.getPartialPlanIdByStepNum(id, stepNum);
//     //loadTransactionFile( partialPlanId);
//     return SQLDB.queryCurrentDecisionIdForStep( partialPlanId);
//   }

//   public List getTransactionsForConstraint(final Integer id) {
//       //loadTransactionFile();
//     return SQLDB.queryTransactionsForConstraint(this.id, id);
//   }

//   public List getTransactionsForToken(final Integer id) {
//       //loadTransactionFile();
//     return SQLDB.queryTransactionsForToken(this.id, id);
//   }
 
//   public List getTransactionsForVariable(final Integer id) {
//       //loadTransactionFile();
//     return SQLDB.queryTransactionsForVariable(this.id, id);
//   }
  
//   public List getTransactionsInRange(final int istart, final int iend) {
//     if(istart == iend) {
//       return getTransactionsList(istart);
//     }
//     int start, end;
//     if(istart >= iend) {
//       start = iend;
//       end = istart;
//     }
//     else {
//       start = istart;
//       end = iend;
//     }
//     List retval = new ArrayList();
//     for(int i = start; i <= end; i++) {
//       retval.addAll(getTransactionsList(i));
//     }
//     return retval;
//   }

//   public List getTransactionsInRange(final Integer start, final Integer end) {
//     return getTransactionsInRange(start.intValue(), end.intValue());
//   }
  
//   public List getStepsWhereTokenTransacted(final Integer id, final String type) 
//     throws IllegalArgumentException {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithTokenTransaction(this.id, id, type);
//   }

//   public List getStepsWhereVariableTransacted(final Integer id, final String type) 
//     throws IllegalArgumentException  {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithVariableTransaction(this.id, id, type);
//   }

//   public List getStepsWhereConstraintTransacted(final Integer id, final String type) 
//     throws IllegalArgumentException  {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithConstraintTransaction(this.id, id, type);
//   }

//   public List getStepsWhereTokenTransacted(final String type) throws IllegalArgumentException {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithTokenTransaction(this.id, type);
//   }

//   public List getStepsWhereVariableTransacted(final String type) 
//     throws IllegalArgumentException  {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithVariableTransaction(this.id, type);
//   }

//   public List getStepsWhereConstraintTransacted(final String type) 
//     throws IllegalArgumentException  {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithConstraintTransaction(this.id, type);
//   }

//   public List getStepsWithRestrictions() {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithRestrictions(id);
//   }

//   public List getStepsWithRelaxations() {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithRelaxations(id);
//   }

//   public List getStepsWithUnitVariableBindingDecisions() {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithUnitDecisions(this);
//   }

//   public List getStepsWithNonUnitVariableBindingDecisions() {
//     loadTransactionFile();
//     return SQLDB.queryStepsWithNonUnitDecisions(this);
//   }

  /**
   * <code>getFreeTokensAtStep</code>
   *
   * @param stepNum - <code>int</code> - 
   * @return - <code>List</code> - 
   */
  public List getFreeTokensAtStep( final int stepNum) throws ResourceNotFoundException {
    boolean isFreeToken = true;
    //getPartialPlan(stepNum);
    loadPartialPlanFiles("step" + stepNum);
    return getTokensById( SQLDB.queryFreeTokensAtStep( stepNum, this), isFreeToken);
  }

  private List getTokensById( final List listOfListOfIds, final boolean isFreeToken) {
    List returnList = new ArrayList();
    Iterator listOfListItr = listOfListOfIds.iterator();
    while (listOfListItr.hasNext()) {
      List listOfIds = (List) listOfListItr.next();
      returnList.add( new PwTokenQueryImpl( (Integer) listOfIds.get( 0), // tokenId
                                            (String) listOfIds.get( 3), // predicateName
                                            (Integer) listOfIds.get( 2), // stepNumber
                                            id, // sequenceId
                                            (Long) listOfIds.get( 1), // partialPlanId
                                            isFreeToken));
    }
    return returnList;
  } // end getTokensById

  /**
   * <code>getUnboundVariablesAtStep</code>
   *
   * @param stepNum - <code>int</code> - 
   * @return - <code>List</code> - 
   */
  public List getUnboundVariablesAtStep( final int stepNum) throws ResourceNotFoundException {
    boolean isUnbound = true;
    loadPartialPlanFiles("step" + stepNum);
    return SQLDB.queryUnboundVariablesAtStep(stepNum, this);
    //return getVariablesById( SQLDB.queryUnboundVariablesAtStep( stepNum, this), isUnbound);
  }

  private void loadPartialPlanFiles(final String name) throws ResourceNotFoundException {
    if(SQLDB.getPartialPlanIdByName(id, name) == null) {
      PwPartialPlanImpl.loadFiles(new File(url + System.getProperty("file.separator") + name));
    }
  }

  private List getVariablesById( final List listOfListOfIds, final boolean isUnbound) {
    List returnList = new ArrayList();
    Iterator listOfListItr = listOfListOfIds.iterator();
    while (listOfListItr.hasNext()) {
      List listOfIds = (List) listOfListItr.next();
      returnList.add( new PwTokenQueryImpl( (Integer) listOfIds.get( 0), // variableId
                                            (String) listOfIds.get( 3), // variableName
                                            (Integer) listOfIds.get( 2), // stepNumber
                                            id, // sequenceId
                                            (Long) listOfIds.get( 1), // partialPlanId
                                            isUnbound));
    }
    return returnList;
  } // end getVariablesById

  public List getPartialPlanNameList() {
    return SQLDB.queryPartialPlanNames( id);
  }

  public int [] getPlanDBSize(final int stepNum) {
    return SQLDB.queryPartialPlanSize(getPartialPlanId(stepNum));
  }
  
  public List getPlanDBSizeList() {
    return SQLDB.queryPartialPlanSizes(id);
  }

  private Long getPartialPlanId(final int stepNum) {
    //return getPartialPlanId("step".concat(Integer.toString(stepNum)));
    return SQLDB.queryPartialPlanId(id, stepNum);
  }

  private Long getPartialPlanId(final String stepName) {
    return SQLDB.queryPartialPlanId(id, stepName);
  }

  public void refresh() {
    System.err.println( "Refreshing planning sequence ...");
    long t1 = System.currentTimeMillis();
    //System.err.println("Loading transaction file...");
    //loadTransactionFile();
    loadStatsFile();
    //System.err.println("Loading transactions...");
    //loadTransactions();
    System.err.println("Loading new partial plan info...");
    planNamesInFilesystem.clear();
    ListIterator planNameIterator = SQLDB.queryPartialPlanNames(id).listIterator();
    while(planNameIterator.hasNext()) {
      String planName = (String) planNameIterator.next();
      System.err.println("Iterating over " + planName);
      if((new File(url + System.getProperty("file.separator") + planName)).exists()) {
        planNamesInFilesystem.add(planName);
      }
      if(!partialPlans.containsKey(planName)) {
        System.err.println("Adding plan " + planName + " to partial plans.");
        partialPlans.put(planName, null);
      }
    }
    planNamesInDb = SQLDB.queryPlanNamesInDatabase(id);
    //hasLoadedTransactionFile = false;
    System.err.println("   ... Refreshing planning sequence elapsed time: " +
                       (System.currentTimeMillis() - t1) + " msecs.");
  }

  private void fakeRules() {
    ruleMap = new HashMap();
    for(int i = 0; i < 20; i++) {
      Integer n = new Integer(i);
      String text = "fake rule " + i;
      ruleMap.put(n, new PwRuleImpl(id, n, text));
    }
  }

  private void instantiateRules( String projectName) {
    String modelRuleDelimiters =
      ConfigureAndPlugins.getProjectConfigValue
      ( ConfigureAndPlugins.PROJECT_MODEL_RULE_DELIMITERS, projectName);
    // System.err.println( "instantiateRules: modelRuleDelimiters " + modelRuleDelimiters);
    ruleMap = SQLDB.queryRules(id, modelRuleDelimiters);
  }

  public PwRule getRule(Integer rId) {
    return (PwRule) ruleMap.get(rId);
  }

  public List getRuleList() {
    return new ArrayList( ruleMap.values());
  }

//   public void addTransaction(PwDBTransactionImpl trans) {
//     transactions.put(id.toString() + trans.getId(), trans);
//   }

  //.partialPlanStats,.sequence,.transactions
  public String [] toOutputString() {
    StringBuffer pps = new StringBuffer();
    for(Iterator it = partialPlans.values().iterator(); it.hasNext();) {
      PwPartialPlanImpl partialPlan = (PwPartialPlanImpl) it.next();
      pps.append(id.toString()).append("\t").append(partialPlan.getId().toString()).append("\t");
      pps.append(partialPlan.getStepNumber()).append("\t");
      pps.append(partialPlan.getTokenList().size()).append("\t");
      pps.append(partialPlan.getVariableList().size()).append("\t");
      pps.append(partialPlan.getConstraintList().size()).append("\t0\n");
    }
    StringBuffer seq = new StringBuffer(url);
    seq.append((char) Integer.parseInt(DbConstants.SEQ_COL_SEP, 16));
    seq.append(id.toString()).append((char) Integer.parseInt(DbConstants.SEQ_COL_SEP, 16));
    // fake rule text -- dummy path must match paths in rules file
    seq.append("--begin /dummy/rulesource/model.nddl\n");
    seq.append("class Rover{\npredicate At{\n Locations m_location;\n eq(duration, 1);\n}}");
    seq.append((char) Integer.parseInt(DbConstants.SEQ_LINE_SEP, 16));

    // StringBuffer trans = new StringBuffer();
//     for(Iterator it = transactions.values().iterator(); it.hasNext();) {
//       PwDBTransaction t = (PwDBTransaction) it.next();
//       trans.append(t.toOutputString());
//     }
    return new String [] {pps.toString(), seq.toString()};//, trans.toString()};
  }

  private class PartialPlanNameComparator implements Comparator {
    public PartialPlanNameComparator() {
    }
    // discard "step" prefix
    public int compare(Object o1, Object o2) {
      Integer s1 = new Integer( ((String) o1).substring( 4));
      Integer s2 = new Integer( ((String) o2).substring( 4));
      return s1.compareTo(s2);
    }
    public boolean equals(Object o1, Object o2) {
      Integer s1 = new Integer( ((String) o1).substring( 4));
      Integer s2 = new Integer( ((String) o2).substring( 4));
      return s1.equals(s2);
    }
  }
  private String validateSequenceDirectory(final String url) {
    File sequenceDir = new File(url);
    if(!sequenceDir.isDirectory()) {
      return "Not a directory.";
    }
    File [] files = sequenceDir.listFiles(new PwSequenceFilenameFilter());
    if(files.length < DbConstants.NUMBER_OF_SEQ_FILES) {
      return "Sequence file(s) missing.";
    }
    // allow sequences with no step directories
//     if(files.length == DbConstants.NUMBER_OF_SEQ_FILES) {
//       return "No step directories.";
//     }
    return null;
  }
  private static final Pattern stepPattern = Pattern.compile("step(\\d+)");

  class StepDirectoryFilter implements FilenameFilter {
    public StepDirectoryFilter(){}
    public boolean accept(File dir, String name) {
      Matcher m = stepPattern.matcher(name);
      return m.matches();
    }
  }
  
  class StepDirectoryComparator implements Comparator {
    public StepDirectoryComparator() {
    }
    public int compare(Object o1, Object o2) {
      String n1 = ((File) o1).getName();
      String n2 = ((File) o2).getName();
      Matcher m1 = stepPattern.matcher(n1);
      Matcher m2 = stepPattern.matcher(n2);
      if(!m1.matches()) {
        if(!m2.matches()) {
          return n1.compareTo(n2);
        }
        return 1;
      }
      else if(!m2.matches()) {
        return -1;
      }
      return Integer.parseInt(n1.substring(m1.start(1))) - 
        Integer.parseInt(n2.substring(m2.start(1)));
    }
    public boolean equals(Object other) {
      return false;
    }
  }
}
