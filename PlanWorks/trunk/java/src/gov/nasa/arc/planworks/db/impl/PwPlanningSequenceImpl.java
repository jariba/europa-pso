// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequenceImpl.java,v 1.3 2003-05-15 18:38:45 taylor Exp $
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
  private String sequenceName;
  private String[] xmlFileNames;

  /**
   * <code>PwPlanningSequenceImpl</code> - constructor -
   *
   * @param url - <code>String</code> - pathname of planning sequence
   * @param projectName - <code>String</code> - 
   * @param model - <code>PwModel</code> - 
   */
  public PwPlanningSequenceImpl( String url, String projectName, PwModel model)
    throws ResourceNotFoundException {
    this.url = url;
    this.projectName = projectName;
    this.model = model;
    stepCount = 0;
    partialPlans = new ArrayList();
    transactions = new ArrayList();

    int index = url.lastIndexOf( "/");
    if (index == -1) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' cannot be parsed for '/'");
    } 
    sequenceName = url.substring( index + 1);

    // determine sequences's partial plans (steps)
    xmlFileNames = new File( url).list( new XmlFilenameFilter());
    for (int i = 0; i < xmlFileNames.length; i++) {
      String xmlFileName = xmlFileNames[i];
      System.err.println( "PwPlanningSequenceImpl xmlFileName: " + xmlFileName);
      partialPlans.add( new PwPartialPlanImpl( url + "/" + xmlFileName, projectName,
                                               sequenceName));
      stepCount++;
    }
    if (xmlFileNames.length == 0) {
      throw new ResourceNotFoundException( "sequence url '" + url +
                                           "' does not have any xml files");
    }
  } // end constructor


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
   * <code>getModel</code>
   *
   * @return - <code>PwModel</code> - 
   */
  public PwModel getModel() {
    return model;
  }

  /**
   * <code>getSequenceName</code>
   *
   * @return - <code>String/code> - 
   */
  public String getSequenceName() {
    return sequenceName;
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
  } // end getPartialPlan

  /**
   * <code>addPartialPlan</code>
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param transactionList - <code>List</code> - of PwTransaction
   * @return - <code>int</code> - length of partial plan set
   */
  public int addPartialPlan( PwPartialPlan partialPlan, List transactionList) {
    partialPlans.add( partialPlan);
    transactions.add( transactionList);
    stepCount++;
    return partialPlans.size();
  } //end addPartialPlan


} // end class PwPlanningSequenceImpl
