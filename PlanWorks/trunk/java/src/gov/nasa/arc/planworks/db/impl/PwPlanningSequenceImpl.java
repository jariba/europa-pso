// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequenceImpl.java,v 1.28 2003-08-28 20:45:23 miatauro Exp $
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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

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

/**
 * <code>PwPlanningSequenceImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
class PwPlanningSequenceImpl implements PwPlanningSequence {

  private Integer id;
  private String projectName;
  private String url; //directory containing the partialplan directories
  private PwModel model;

  private int stepCount;
  private List partialPlans; // List of of PwPartialPlan
  private List transactions; // List of List of PwTransaction
  private String name;
  private List partialPlanNames; // List of String

  /**
   * <code>PwPlanningSequenceImpl</code> - constructor - for CreateProject
   *
   * @param url - <code>String</code> - pathname of planning sequence
   * @param id - <code>Integer</code> - id of sequence
   * @param project - <code>PwProjectImpl</code> - the project to which the sequence will be added
   * @param model - <code>PwModelImpl</code> - not currently used
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPlanningSequenceImpl( String url, Integer id, PwProjectImpl project, PwModelImpl model)
    throws ResourceNotFoundException, SQLException {
    System.err.println("In PwPlanningSequenceImpl(String, Integer, PwProjectImpl, PwModelImpl");
    this.url = url;
    this.id = id;
    this.projectName = project.getName();
    this.model = model;
    stepCount = 0;
    partialPlanNames = new ArrayList();
    transactions = new ArrayList();

    int index = url.lastIndexOf( System.getProperty( "file.separator"));
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '" +
                                           System.getProperty( "file.separator") + "'");
    } 
    name = url.substring( index + 1);
    
    List planNames = MySQLDB.getPlanNamesInSequence(id);
    partialPlanNames.addAll(MySQLDB.getPlanNamesInSequence(id));
    stepCount = partialPlanNames.size();
    partialPlans = new ArrayList(partialPlanNames.size());
    ListIterator planNameIterator = partialPlanNames.listIterator();
    while(planNameIterator.hasNext()) {
      partialPlans.add(null);
      addPartialPlan((String) planNameIterator.next());
    }
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
    throws ResourceNotFoundException, SQLException {
    System.err.println("In PwPlanningSequenceImpl(String, PwProjectImpl, PwModelImpl)");
    this.url = url;
    this.projectName = project.getName();
    this.model = model;
    partialPlanNames = new ArrayList();
    transactions = new ArrayList();

    int index = url.lastIndexOf( System.getProperty( "file.separator"));
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '" +
                                           System.getProperty( "file.separator") + "'");
    } 
    name = url.substring( index + 1);
    
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
          temp.put(planDirs[i].getName(), new Integer(0));
          stepCount++;
        }
      }
    }
    partialPlanNames.addAll(temp.keySet());
    this.partialPlans = new ArrayList(partialPlanNames.size());
    ListIterator partialPlanIterator = partialPlanNames.listIterator();
    while(partialPlanIterator.hasNext()) {
      partialPlans.add(null);
      addPartialPlan((String)partialPlanIterator.next());
    }
    // put these in PwProjectImpl so that its XMLDecode/Encode can access them
    //project.addPartialPlanNames( partialPlanNames);
  } // end constructor for OpenProject call
  
  
  
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
    if ((step >= 0) && (step < transactions.size())) {
      return (List) transactions.get( step);
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
    return new ArrayList(partialPlans);
  }

  /**
   * <code>listPartialPlanNames</code>
   *    get a list of the names of every partial plan in the sequence
   *
   * @return List of Strings
   */

  public List listPartialPlanNames() {
    return new ArrayList(partialPlanNames);
  }
  /**
   * <code>getPartialPlan</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public PwPartialPlan getPartialPlan( int step) throws IndexOutOfBoundsException {
    if ((step >= 0) && (step < partialPlans.size())) {
      return (PwPartialPlan) partialPlans.get( step);
    } else {
      throw new IndexOutOfBoundsException( "step " + step + ", not >= 0 and < " +
                                           partialPlans.size());
    }
  } // end getPartialPlan( int)

  /**
   * <code>getPartialPlan</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   */
  public PwPartialPlan getPartialPlan( String planName) throws ResourceNotFoundException {
    for (int index = 0, n = partialPlanNames.size(); index < n; index++) {
      if (((String) partialPlanNames.get( index)).equals( planName)) {
        return (PwPartialPlan) partialPlans.get( index);
      }
    }
    throw new ResourceNotFoundException( "plan name '" + planName +
                                         "'not found in url " + url);
  } // end getPartialPlan( String)

  /**
   * <code>addPartialPlan</code> -
   *          maintain PwPartialPlanImpl instance ordering of partialPlanNames
   *
   * @param partialPlanName - <code>String</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPartialPlan addPartialPlan(String partialPlanName) 
    throws ResourceNotFoundException, SQLException {
    System.err.println("In addPartialPlan");
    int index = -1;
    if((index = partialPlanNames.indexOf(partialPlanName)) != -1) {
      PwPartialPlanImpl partialPlan = new PwPartialPlanImpl(url, partialPlanName, id);
      if(System.getProperty("plan.check") != null) {
        partialPlan.checkPlan();
      }
      partialPlans.set(index, partialPlan);
      return partialPlan;
    }
    throw new ResourceNotFoundException("Failed to find plan " + partialPlanName + " in sequence " + name);
  }

  public void delete() throws ResourceNotFoundException {
    MySQLDB.deletePlanningSequence(id);
  }
}
