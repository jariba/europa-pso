// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequenceImpl.java,v 1.12 2003-06-26 19:46:55 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwModel;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.util.FileCopy;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>PwPlanningSequenceImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
class PwPlanningSequenceImpl implements PwPlanningSequence {

  private Integer key;
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
   * @param project - <code>PwProjectImpl</code> - 
   * @param model - <code>PwModelImpl</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  //from new PwProject(blah, true);
  public PwPlanningSequenceImpl( String url, Integer key, PwProjectImpl project, PwModelImpl model)
    throws ResourceNotFoundException, SQLException {
    this.url = url;
    this.key = key;
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
    
    ResultSet partialPlans = 
      MySQLDB.queryDatabase("SELECT (PlanName) FROM PartialPlan WHERE SequenceId=".concat(key.toString()));
    while(partialPlans.next()) {
      partialPlanNames.add(partialPlans.getString("PlanName"));
      //partialPlans.add(new PwPartialPlan(new Long(partialPlans.getLong("PartialPlanId"))));
      stepCount++;
    }
    this.partialPlans = new ArrayList(partialPlanNames.size());
  }


  /**
   * <code>PwPlanningSequenceImpl</code> - constructor - for OpenProject
   *
   * @param url - <code>String</code> - pathname of planning sequence
   * @param project - <code>PwProjectImpl</code> - 
   * @param model - <code>PwModelImpl</code> - 
   * @param partialPlanNames - <code>List</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */ 
  //from addPlanningSequence(url)
  public PwPlanningSequenceImpl( String url, PwProjectImpl project, PwModelImpl model)
    throws ResourceNotFoundException {
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
    
    File sequenceDir = new File(url);
    if(!sequenceDir.isDirectory()) {
      throw new ResourceNotFoundException("sequence url '" + url + "' is not a directory.");
    }
    HashMap temp = new HashMap();
    File [] planDirs = sequenceDir.listFiles();
    for(int i = 0; i < planDirs.length; i++) {
      if(planDirs[i].isDirectory()) {
        temp.put(planDirs[i].getName(), new Integer(0));
        stepCount++;
      }
    }
    partialPlanNames.addAll(temp.keySet());
    this.partialPlans = new ArrayList(partialPlanNames.size());
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

  public List listPartialPlans() {
    return partialPlans;
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
    int index = -1;
    if((index = partialPlanNames.indexOf(partialPlanName)) != -1) {
      PwPartialPlan partialPlan =
        new PwPartialPlanImpl(url, partialPlanName, key);
      partialPlans.set(index, partialPlan);
      return partialPlan;
    }
    throw new ResourceNotFoundException("Failed to find plan " + partialPlanName + " in sequence " + name);
  }
}
