// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequenceImpl.java,v 1.7 2003-06-11 01:02:12 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.PwModel;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.db.util.XmlFileFilter;
import gov.nasa.arc.planworks.db.util.XmlFilenameFilter;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>PwPlanningSequenceImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
class PwPlanningSequenceImpl implements PwPlanningSequence {

  private String projectName;
  private String url; // e.g. xmlFilesDirectory
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
  public PwPlanningSequenceImpl( String url, PwProjectImpl project, PwModelImpl model)
    throws ResourceNotFoundException {
    this.url = url;
    this.projectName = project.getName();
    this.model = model;
    stepCount = 0;
    partialPlans = new ArrayList();
    transactions = new ArrayList();

    int index = url.lastIndexOf( "/");
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '/'");
    } 
    name = url.substring( index + 1);

    // determine sequences's partial plans (steps)
    String[] xmlFileNames = new File( url).list( new XmlFilenameFilter());
    partialPlanNames = new ArrayList();
    for (int i = 0; i < xmlFileNames.length; i++) {
      String xmlFileName = xmlFileNames[i];
      stepCount++;
      index = xmlFileName.lastIndexOf( ".");
      partialPlanNames.add( xmlFileName.substring( 0, index));
    }
    if (xmlFileNames.length == 0) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' does not have any xml files");
    }
    for (int i = 0; i < stepCount; i++) {
      partialPlans.add( null);
    }
    // put these in PwProjectImpl so that its XMLDecode/Encode can access them
    project.addPartialPlanNames( partialPlanNames);
  } // end constructor for CreateProject call


  /**
   * <code>PwPlanningSequenceImpl</code> - constructor - for OpenProject
   *
   * @param url - <code>String</code> - pathname of planning sequence
   * @param project - <code>PwProjectImpl</code> - 
   * @param model - <code>PwModelImpl</code> - 
   * @param partialPlanNames - <code>List</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPlanningSequenceImpl( String url, PwProjectImpl project,
                                 PwModelImpl model, List partialPlanNames)
    throws ResourceNotFoundException {
    this.url = url;
    this.projectName = project.getName();
    this.model = model;
    this.partialPlanNames = partialPlanNames;
    partialPlans = new ArrayList();
    transactions = new ArrayList();

    int index = url.lastIndexOf( "/");
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '/'");
    } 
    name = url.substring( index + 1);
    stepCount = this.partialPlanNames.size();
    for (int i = 0; i < stepCount; i++) {
      partialPlans.add( null);
    }
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
   * @param url - <code>String</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPartialPlan addPartialPlan( String url, String partialPlanName)
    throws ResourceNotFoundException {
    for (int index = 0, n = partialPlanNames.size(); index < n; index++) {
      if (((String) partialPlanNames.get( index)).equals( partialPlanName)) {
        PwPartialPlan partialPlan =
          new PwPartialPlanImpl( url + "/" + partialPlanName +
                                 XmlFileFilter.XML_EXTENSION_W_DOT,
                                 projectName, name);
        partialPlans.set( index, partialPlan);
        return partialPlan;
      }
    }
    return null;
  } // end addPartialPlan


  // END INTERFACE IMPLEMENTATION 




} // end class PwPlanningSequenceImpl
