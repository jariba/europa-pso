// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequenceImpl.java,v 1.70 2004-02-03 20:43:46 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

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

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwModel;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.db.util.PwSequenceFilenameFilter;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.db.util.PwSQLFilenameFilter;
import gov.nasa.arc.planworks.util.FileCopy;
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
public class PwPlanningSequenceImpl implements PwPlanningSequence, ViewableObject {

  private Long id;
  private String projectName;
  private String url; //directory containing the partialplan directories
  private PwModel model;

  private int stepCount;
  private Map transactions;
  private String name;
  private List contentSpec;
  private Map partialPlans; // partialPlanName Map of PwPartialPlan

  private long timeSpentLoadingFiles;
  private long timeSpentAnalyzingDatabase;
  /**
   * <code>PwPlanningSequenceImpl</code> - constructor - for CreateProject
   *
   * @param url - <code>String</code> - pathname of planning sequence
   * @param id - <code>Integer</code> - id of sequence
   * @param project - <code>PwProjectImpl</code> - the project to which the sequence will be added
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPlanningSequenceImpl( final String url, final Long id)
    throws ResourceNotFoundException {
    this.url = url;
    this.id = id;
    this.model = null;
    stepCount = 0;
    
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
    //ListIterator planNameIterator = MySQLDB.getPlanNamesInSequence(id).listIterator();
    ListIterator planNameIterator = MySQLDB.queryPartialPlanNames(id).listIterator();
    while(planNameIterator.hasNext()) {
      partialPlans.put((String) planNameIterator.next(), null);
      stepCount++;
    }
    //loadTransactions();
    transactions = null;
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
    File sequenceDir = new File(url);
    if(!sequenceDir.isDirectory()) {
      throw new ResourceNotFoundException("sequence url '" + url + "' is not a directory.");
    }
    String error = null;
    if((error = validateSequenceDirectory(url)) != null) {
      throw new ResourceNotFoundException("url '" + url + "' is not a valid sequence directory: "
                                          + error);
    }
    this.id = MySQLDB.addSequence(url, project.getId());
    loadTransactionFile();
    loadStatsFile();
    MySQLDB.analyzeDatabase();
    //loadTransactions();
    transactions = null;
    ListIterator ppNameIterator = MySQLDB.queryPartialPlanNames(id).listIterator();
    while(ppNameIterator.hasNext()) {
      partialPlans.put(ppNameIterator.next(), null);
      stepCount++;
    }
  } // end constructor for OpenProject call
  

  private void loadTransactionFile() {
    MySQLDB.loadFile(url + System.getProperty("file.separator") + "transactions", "Transaction");
  }

  private void loadStatsFile() {
    MySQLDB.loadFile(url + System.getProperty("file.separator") + "partialPlanStats",
                     "PartialPlanStats");
  }

  private void loadTransactions() {
    long t1 = System.currentTimeMillis();
    transactions = MySQLDB.queryTransactions(id);
    System.err.println(transactions.keySet().size() + " transactions.");
    System.err.println("Loading transactions took " + (System.currentTimeMillis() - t1) + "ms");
  }
  
  public void cleanTransactions(final PwPartialPlanImpl pp) {
    ListIterator transactionNameIterator = (new ArrayList(transactions.keySet())).listIterator();
    String ppIdStr = pp.getId().toString();
    while(transactionNameIterator.hasNext()) {
      String transactionName = (String) transactionNameIterator.next();
      if(transactionName.indexOf(ppIdStr) == 0) {
        PwDBTransactionImpl transaction = (PwDBTransactionImpl) transactions.get(transactionName);
        if(transaction.getType().indexOf("DELETED") == -1) {
          if(transaction.getType().indexOf("TOKEN") != -1) {
            if(!pp.tokenExists(transaction.getObjectId())) {
              transactions.remove(transactionName);
            }
          }
          else if(transaction.getType().indexOf("VARIABLE") != -1) {
            if(pp.getVariable(transaction.getObjectId()) == null) {
              transactions.remove(transactionName);
            }
          }
          else if(transaction.getType().indexOf("CONSTRAINT") != -1) {
            if(pp.getConstraint(transaction.getObjectId()) == null) {
              transactions.remove(transactionName);
            }
          }
        }
      }
    }
  }

  // IMPLEMENT INTERFACE 


  /**
   * <code>getStepCount</code> - number of PartialPlans, each a step
   *
   * @return - <code>int</code> - 
   */
  public int getStepCount() {
    return stepCount;
  }

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
   * <code>getTransactionsList</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>List</code> - of PwDBTransaction
   */
  public List getTransactionsList( final Long partialPlanId) {
    if(transactions == null) {
      loadTransactions();
    }
    List retval = new ArrayList();
    String ppId = partialPlanId.toString();
    ListIterator transactionKeyIterator = (new ArrayList(transactions.keySet())).listIterator();
    while(transactionKeyIterator.hasNext()) {
      String key = (String) transactionKeyIterator.next();
      if(key.indexOf(ppId) == 0) {
        retval.add(transactions.get(key));
      }
    }
    return retval;
  } // end listTransactions

  public List getTransactionsList(final int stepNum) throws IndexOutOfBoundsException {
    return getTransactionsList(getPartialPlanId(stepNum));
  }

  /**
   * <code>listPartialPlans</code>
   *    get a list of all partial plans in the planning sequence
   *
   * @return List of PwPartialPlan objects
   */
  public List getPartialPlansList() {
    List retval = new ArrayList();
    Iterator nameIterator = partialPlans.keySet().iterator();
    while(nameIterator.hasNext()) {
      String name = (String) nameIterator.next();
      if(partialPlans.get(name) != null) {
        retval.add(partialPlans.get(name));
      }
    }
    return retval;
  }

  /**
   * <code>listPartialPlanNames</code>
   *    get a list of the names of every partial plan in the sequence
   *
   * @return List of Strings
   */
  public List getPartialPlanNamesList() {
    // return new ArrayList(partialPlans.keySet());
    List names = new ArrayList();
    Iterator keyItr = partialPlans.keySet().iterator();
    while (keyItr.hasNext()) {
      names.add( (String) keyItr.next());
    }
    Collections.sort( names, new PartialPlanNameComparator());
    return names;
  }
  /**
   * <code>getPartialPlan</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public PwPartialPlan getPartialPlan( final int step) throws IndexOutOfBoundsException, 
  ResourceNotFoundException {
    if(step >= 0 && step < stepCount) {
      String name = "step" + step;
      if(!partialPlans.containsKey(name)) {
        throw new IndexOutOfBoundsException("step " + step + ", not >= 0 and < " + stepCount);
      }
      PwPartialPlan retval = (PwPartialPlan) partialPlans.get(name);
      if(retval == null) {
        retval = addPartialPlan(name);
      }
      return retval;
    }
    throw new IndexOutOfBoundsException("step " + step + ", not >= 0 and < " + stepCount);
  } // end getPartialPlan( int)

  /**
   * <code>getPartialPlan</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPartialPlan getPartialPlan( final String planName) throws ResourceNotFoundException {
    if(!partialPlans.containsKey(planName)) {
      throw new ResourceNotFoundException("plan name '" + planName + "' not found in url " + url);
    }
    PwPartialPlan retval = (PwPartialPlan) partialPlans.get(planName);
    if(retval == null) {
      retval = addPartialPlan(planName);
    }
    return retval;
  } // end getPartialPlan( String)

  public PwPartialPlan getNextPartialPlan(final int step) throws ResourceNotFoundException, 
  IndexOutOfBoundsException {
    return getPartialPlan(step+1);
  }
  public PwPartialPlan getNextPartialPlan(final String planName) throws ResourceNotFoundException,
  IndexOutOfBoundsException {
    return getPartialPlan(Integer.parseInt(planName.substring(4)) + 1);
  }
  
  public PwPartialPlan getPrevPartialPlan(final int step) throws ResourceNotFoundException,
  IndexOutOfBoundsException {
    return getPartialPlan(step-1);
  }
  public PwPartialPlan getPrevPartialPlan(final String planName) throws ResourceNotFoundException, 
  IndexOutOfBoundsException {
    return getPartialPlan(Integer.parseInt(planName.substring(4)) - 1);
  }
  
  /**
   * <code>addPartialPlan</code> -
   *          maintain PwPartialPlanImpl instance ordering of partialPlanNames
   *
   * @param partialPlanName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  private PwPartialPlan addPartialPlan(final String partialPlanName) 
    throws ResourceNotFoundException {
    if(partialPlans.containsKey(partialPlanName)) {
      PwPartialPlanImpl partialPlan = new PwPartialPlanImpl(url, partialPlanName, this);
      partialPlans.put(partialPlanName, partialPlan);
      return partialPlan;
    }
    throw new ResourceNotFoundException("Failed to find plan " + partialPlanName +
                                        " in sequence " + name);
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
      throw new ResourceNotFoundException("plan name '" + planName +
                                          "' not found in url " + url);
    }
    return (PwPartialPlan) partialPlans.get(planName);
  }

  public void delete() throws ResourceNotFoundException {
    long t1 = System.currentTimeMillis();
    MySQLDB.deletePlanningSequence(id);
    System.err.println("Deleting sequence took " + (System.currentTimeMillis() - t1));
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


  private List getTransactionsById(final List ids) {
    if(transactions == null) {
      loadTransactions();
    }
    List retval = new ArrayList();
    ListIterator idIterator = ids.listIterator();
    while(idIterator.hasNext()) {
      String id = (String) idIterator.next();
      if(transactions.containsKey(id)) {
        retval.add(transactions.get(id));
      }
    }
    return retval;
  }

  public List getTransactionsForConstraint(final Integer id) {
    return getTransactionsById(MySQLDB.queryTransactionsForConstraint(this.id, id));
  }

  public List getTransactionsForToken(final Integer id) {
    return getTransactionsById(MySQLDB.queryTransactionsForToken(this.id, id));
  }
 
  public List getTransactionsForVariable(final Integer id) {
    return getTransactionsById(MySQLDB.queryTransactionsForVariable(this.id, id));
  }
  
  public List getTransactionsInRange(final int istart, final int iend) {
    if(istart == iend) {
      return getTransactionsList(istart);
    }
    int start, end;
    if(istart >= iend) {
      start = iend;
      end = istart;
    }
    else {
      start = istart;
      end = iend;
    }
    List retval = new ArrayList();
    for(int i = start; i <= end; i++) {
      retval.addAll(getTransactionsList(i));
    }
    return retval;
  }

  public List getTransactionsInRange(final Integer start, final Integer end) {
    return getTransactionsInRange(start.intValue(), end.intValue());
  }
  
  public List getStepsWhereTokenTransacted(final Integer id, final String type) 
    throws IllegalArgumentException {
    if(!type.equals(DbConstants.TOKEN_CREATED) && !type.equals(DbConstants.TOKEN_DELETED) &&
       !type.equals(DbConstants.TOKEN_FREED) && !type.equals(DbConstants.TOKEN_INSERTED)&&
       !type.equals(DbConstants.TOKEN_ALL_TYPES)) {
      throw new IllegalArgumentException("transaction type");
    }
    return getTransactionsById(MySQLDB.queryStepsWithTokenTransaction(this.id, id, type));
  }

  public List getStepsWhereVariableTransacted(final Integer id, final String type) 
    throws IllegalArgumentException  {
    if(!type.equals(DbConstants.VARIABLE_CREATED) && !type.equals(DbConstants.VARIABLE_DELETED) &&
       !type.equals(DbConstants.VARIABLE_DOMAIN_EMPTIED) && 
       !type.equals(DbConstants.VARIABLE_DOMAIN_RELAXED) && 
       !type.equals(DbConstants.VARIABLE_DOMAIN_RESET) &&  
       !type.equals(DbConstants.VARIABLE_DOMAIN_RESTRICTED) &&
       !type.equals(DbConstants.VARIABLE_DOMAIN_SPECIFIED) &&
       !type.equals(DbConstants.VARIABLE_ALL_TYPES)) {
      throw new IllegalArgumentException("transaction type");
    }
    return getTransactionsById(MySQLDB.queryStepsWithVariableTransaction(this.id, id, type));
  }

  public List getStepsWhereConstraintTransacted(final Integer id, final String type) 
    throws IllegalArgumentException  {
    if(!type.equals(DbConstants.CONSTRAINT_CREATED) && 
       !type.equals(DbConstants.CONSTRAINT_DELETED) &&
       !type.equals(DbConstants.CONSTRAINT_ALL_TYPES)) { 
      throw new IllegalArgumentException("transaction type");
    }
    return getTransactionsById(MySQLDB.queryStepsWithConstraintTransaction(this.id, id, type));
  }

  public List getStepsWhereTokenTransacted(final String type) throws IllegalArgumentException {
    if(!type.equals(DbConstants.TOKEN_CREATED) && !type.equals(DbConstants.TOKEN_DELETED) &&
       !type.equals(DbConstants.TOKEN_FREED) && !type.equals(DbConstants.TOKEN_INSERTED)&&
       !type.equals(DbConstants.TOKEN_ALL_TYPES)) {
      throw new IllegalArgumentException("transaction type");
    }
    return getTransactionsById(MySQLDB.queryStepsWithTokenTransaction(this.id, type));
  }

  public List getStepsWhereVariableTransacted(final String type) 
    throws IllegalArgumentException  {
    if(!type.equals(DbConstants.VARIABLE_CREATED) && !type.equals(DbConstants.VARIABLE_DELETED) &&
       !type.equals(DbConstants.VARIABLE_DOMAIN_EMPTIED) && 
       !type.equals(DbConstants.VARIABLE_DOMAIN_RELAXED) && 
       !type.equals(DbConstants.VARIABLE_DOMAIN_RESET) &&  
       !type.equals(DbConstants.VARIABLE_DOMAIN_RESTRICTED) &&
       !type.equals(DbConstants.VARIABLE_DOMAIN_SPECIFIED) &&
       !type.equals(DbConstants.VARIABLE_ALL_TYPES)) {
      throw new IllegalArgumentException("transaction type");
    }
    return getTransactionsById(MySQLDB.queryStepsWithVariableTransaction(this.id, type));
  }

  public List getStepsWhereConstraintTransacted(final String type) 
    throws IllegalArgumentException  {
    if(!type.equals(DbConstants.CONSTRAINT_CREATED) && 
       !type.equals(DbConstants.CONSTRAINT_DELETED) &&
       !type.equals(DbConstants.CONSTRAINT_ALL_TYPES)) { 
      throw new IllegalArgumentException("transaction type");
    }
    return getTransactionsById(MySQLDB.queryStepsWithConstraintTransaction(this.id, type));
  }

  public List getStepsWithRestrictions() {
    return getTransactionsById(MySQLDB.queryStepsWithRestrictions(id));
  }

  public List getStepsWithRelaxations() {
    return getTransactionsById(MySQLDB.queryStepsWithRelaxations(id));
  }

  public List getStepsWithUnitVariableBindingDecisions() {
    return getTransactionsById(MySQLDB.queryStepsWithUnitVariableDecisions(this));
  }

  public List getStepsWithNonUnitVariableBindingDecisions() {
    return getTransactionsById(MySQLDB.queryStepsWithNonUnitVariableDecisions(this));
  }

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
    return getTokensById( MySQLDB.queryFreeTokensAtStep( stepNum, this), isFreeToken);
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
    return MySQLDB.queryUnboundVariablesAtStep(stepNum, this);
    //return getVariablesById( MySQLDB.queryUnboundVariablesAtStep( stepNum, this), isUnbound);
  }

  private void loadPartialPlanFiles(final String name) throws ResourceNotFoundException {
    if(MySQLDB.getPartialPlanIdByName(id, name) == null) {
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

  public int [] getPlanDBSize(final int stepNum) throws IndexOutOfBoundsException {
    if(stepNum < 0 || stepNum > stepCount) {
      System.err.println(stepNum + " is OOB");
      throw new IndexOutOfBoundsException();
    }
    return MySQLDB.queryPartialPlanSize(getPartialPlanId(stepNum));
  }
  
  public List getPlanDBSizeList() {
    return MySQLDB.queryPartialPlanSizes(id);
  }

  private Long getPartialPlanId(final int stepNum) {
    //return getPartialPlanId("step".concat(Integer.toString(stepNum)));
    return MySQLDB.queryPartialPlanId(id, stepNum);
  }

  private Long getPartialPlanId(final String stepName) {
    return MySQLDB.queryPartialPlanId(id, stepName);
  }

  public void refresh() {
    System.err.println("Loading transaction file...");
    loadTransactionFile();
    System.err.println("Loading stats file...");
    loadStatsFile();
    System.err.println("Loading transactions...");
    loadTransactions();
    System.err.println("Loading new partial plan info...");
    ListIterator planNameIterator = MySQLDB.queryPartialPlanNames(id).listIterator();
    while(planNameIterator.hasNext()) {
      String planName = (String) planNameIterator.next();
      if(!partialPlans.containsKey(planName)) {
        partialPlans.put(planName, null);
        stepCount++;
      }
    }
    System.err.println("Planning sequence refresh done.");
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
    if(files.length == DbConstants.NUMBER_OF_SEQ_FILES) {
      return "No step directories.";
    }
//     Arrays.sort(files, new StepDirectoryComparator());
//     for(int i = 0; i < files.length; i++) {
//       if(files[i].isDirectory()) {
//         if(!files[i].getName().equals("step" + i)) {
//           return "Skipped step " + i;
//         }
//       }
//     }
    return null;
  }
  private static final Pattern stepPattern = Pattern.compile("step(\\d+)");
  
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
