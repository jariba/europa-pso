// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequenceImpl.java,v 1.36 2003-10-07 02:13:34 taylor Exp $
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwModel;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwTransaction;
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
class PwPlanningSequenceImpl implements PwPlanningSequence, ViewableObject {

  private Long id;
  private String projectName;
  private String url; //directory containing the partialplan directories
  private PwModel model;

  private int stepCount;
  private Map transactions; // stepNumber Map of List of PwTransaction
  private String name;
  //private List partialPlanNames; // List of String
  private List contentSpec;
  private Map partialPlans; // partialPlanName Map of PwPartialPlan

  /**
   * <code>PwPlanningSequenceImpl</code> - constructor - for CreateProject
   *
   * @param url - <code>String</code> - pathname of planning sequence
   * @param id - <code>Integer</code> - id of sequence
   * @param project - <code>PwProjectImpl</code> - the project to which the sequence will be added
   * @param model - <code>PwModelImpl</code> - not currently used
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPlanningSequenceImpl( String url, Long id, PwProjectImpl project, PwModelImpl model)
    throws ResourceNotFoundException {
    //System.err.println("In PwPlanningSequenceImpl(String, Integer, PwProjectImpl, PwModelImpl");
    this.url = url;
    this.id = id;
    this.projectName = project.getName();
    this.model = model;
    stepCount = 0;
    //partialPlanNames = new ArrayList();
    //transactions = new ArrayList();
    transactions = new OneToManyMap();

    int index = url.lastIndexOf( System.getProperty( "file.separator"));
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '" +
                                           System.getProperty( "file.separator") + "'");
    } 
    name = url.substring( index + 1);
    contentSpec = new ArrayList();
   
    partialPlans = new HashMap();
    ListIterator planNameIterator = MySQLDB.getPlanNamesInSequence(id).listIterator();
    while(planNameIterator.hasNext()) {
      partialPlans.put((String) planNameIterator.next(), null);
      stepCount++;
    }
    loadTransactions();
    //List planNames = MySQLDB.getPlanNamesInSequence(id);
    //partialPlanNames.addAll(MySQLDB.getPlanNamesInSequence(id));
    //stepCount = partialPlanNames.size();
    //partialPlans = new ArrayList(partialPlanNames.size());
    //ListIterator planNameIterator = partialPlanNames.listIterator();
    //while(planNameIterator.hasNext()) {
    //  partialPlans.add(null);
    //  addPartialPlan((String) planNameIterator.next());
    //}
    
  }


  /**
   * <code>PwPlanningSequenceImpl</code> - constructor - for OpenProject
   *
   * @param url - <code>String</code> - pathname of planning sequence
   * @param project - <code>PwProjectImpl</code> - project to which the sequence will be added
   * @param model - <code>PwModelImpl</code> - not currently used
   * @exception ResourceNotFoundException if an error occurs
   */ 
  public PwPlanningSequenceImpl( String url, PwProjectImpl project, PwModelImpl model)
    throws ResourceNotFoundException {
    //System.err.println("In PwPlanningSequenceImpl(String, PwProjectImpl, PwModelImpl)");
    this.url = url;
    this.projectName = project.getName();
    this.model = model;
    //partialPlanNames = new ArrayList();
    //transactions = new ArrayList();
    transactions = new OneToManyMap();

    partialPlans = new HashMap();

    int index = url.lastIndexOf( System.getProperty( "file.separator"));
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '" +
                                           System.getProperty( "file.separator") + "'");
    } 
    name = url.substring( index + 1);
    contentSpec = new ArrayList();
    
    MySQLDB.addSequence(url, project.getId());
    this.id = MySQLDB.latestSequenceId();
    File sequenceDir = new File(url);
    if(!sequenceDir.isDirectory()) {
      throw new ResourceNotFoundException("sequence url '" + url + "' is not a directory.");
    }
    HashMap temp = new HashMap();
    File [] planDirs = sequenceDir.listFiles();
    for(int i = 0; i < planDirs.length; i++) {
      if(planDirs[i].isDirectory()) {
        String [] names = planDirs[i].list(new PwSQLFilenameFilter());
        if(names.length == DbConstants.NUMBER_OF_PP_FILES) {
          //temp.put(planDirs[i].getName(), new Integer(0));
          partialPlans.put(planDirs[i].getName(), null);
          stepCount++;
          loadFiles(planDirs[i]);
        }
      }
    }
    loadTransactions();
    //partialPlanNames.addAll(temp.keySet());
    //this.partialPlans = new ArrayList(partialPlanNames.size());
    //ListIterator partialPlanIterator = partialPlanNames.listIterator();
    //while(partialPlanIterator.hasNext()) {
    //  partialPlans.add(null);
    //  addPartialPlan((String)partialPlanIterator.next());
    // }
    // put these in PwProjectImpl so that its XMLDecode/Encode can access them
    //project.addPartialPlanNames( partialPlanNames);
  } // end constructor for OpenProject call
  

  private void loadFiles(File planDir) {
    String [] fileNames = planDir.list(new PwSQLFilenameFilter());
    for(int i = 0; i < fileNames.length; i++) {
      String tableName = fileNames[i].substring(fileNames[i].lastIndexOf(".") + 1);
      tableName = tableName.substring(0,1).toUpperCase().concat(tableName.substring(1));
      if(tableName.lastIndexOf("s") == tableName.length() - 1) {
        tableName = tableName.substring(0, tableName.length() - 1);
      }
      if(tableName.equals("Constraint")) {
        tableName = "VConstraint";
      }
      MySQLDB.loadFile(planDir.getAbsolutePath().concat(System.getProperty("file.separator")).concat(fileNames[i]), tableName);
      //MySQLDB.loadFile(url.toString().concat(System.getProperty("file.separator")).concat(fileNames[i]), tableName);
    }
    MySQLDB.analyzeDatabase();
  }

  private void loadTransactions() {
    transactions = MySQLDB.queryTransactions(id);
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

  /**
   * <code>getName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getName() {
    return name;
  }

  public Long getId() {
    //return id;
    return new Long(id.longValue());
  }

  /**
   * <code>getModel</code>
   *
   * @return - <code>PwModel</code> - 
   */
  public PwModel getModel() {
    return model;
  }

  /**
   * <code>listTransactions</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>List</code> - of PwTransaction
   */
  public List listTransactions( int step) throws IndexOutOfBoundsException {
    if ((step >= 0) && (step < stepCount)) {
	//return (List) transactions.get( step);
	return (List) transactions.get("step" + step);
    } else {
      throw new IndexOutOfBoundsException( "step " + step + ", not >= 0 and < " +
                                           transactions.size());
    }
  } // end listTransactions


  /**
   * <code>listPartialPlans</code>
   *    get a list of all partial plans in the planning sequence
   *
   * @return List of PwPartialPlan objects
   */
  public List listPartialPlans() {
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
  public List listPartialPlanNames() {
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
  public PwPartialPlan getPartialPlan( int step) throws IndexOutOfBoundsException, 
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
   */
  public PwPartialPlan getPartialPlan( String planName) throws ResourceNotFoundException {
    //for (int index = 0, n = partialPlanNames.size(); index < n; index++) {
    //  if (((String) partialPlanNames.get( index)).equals( planName)) {
    //    return (PwPartialPlan) partialPlans.get( index);
    //  }
    //}
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
   * <code>addPartialPlan</code> -
   *          maintain PwPartialPlanImpl instance ordering of partialPlanNames
   *
   * @param partialPlanName - <code>String</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  private PwPartialPlan addPartialPlan(String partialPlanName) 
    throws ResourceNotFoundException {
    //System.err.println("In addPartialPlan");
    /*int index = -1;
      if((index = partialPlanNames.indexOf(partialPlanName)) != -1) {
      PwPartialPlanImpl partialPlan = new PwPartialPlanImpl(url, partialPlanName, id);
      if(System.getProperty("plan.check") != null) {
      partialPlan.checkPlan();
      }
      partialPlans.set(index, partialPlan);
      return partialPlan;
      }
      throw new ResourceNotFoundException("Failed to find plan " + partialPlanName + " in sequence " + name);*/
    if(partialPlans.containsKey(partialPlanName)) {
      PwPartialPlanImpl partialPlan = new PwPartialPlanImpl(url, partialPlanName, id);
      partialPlans.put(partialPlanName, partialPlan);
      return partialPlan;
    }
    throw new ResourceNotFoundException("Failed to find plan " + partialPlanName + " in sequence " +
                                        name);
  }

  public void delete() throws ResourceNotFoundException {
    MySQLDB.deletePlanningSequence(id);
  }

  // implement ViewableObject

  public void setContentSpec(List spec) {
    contentSpec.clear();
    contentSpec.addAll(spec);
  }
  
  public List getContentSpec() {
    return new ArrayList(contentSpec);
  }

  // getName already defined

  // end implement ViewableObject

  /**
   * <code>setSeqName</code> - sequenceDir
   *      PlanWorks.renderSequenceView invokes this method 
   *
   * @param seqName - <code>String</code> - 
   */
  public void setSeqName( String seqName) {
    this.name = seqName;
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

}
