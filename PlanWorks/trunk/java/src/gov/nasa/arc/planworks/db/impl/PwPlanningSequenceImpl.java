// $Id: PwPlanningSequenceImpl.java,v 1.2 2003-05-10 01:19:37 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.PwModel;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwTransaction;


/**
 * <code>PwPlanningSequenceImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
class PwPlanningSequenceImpl extends PwPlanningSequence {

  private String url;
  private int stepCount;
  private PwModel model;
  private List partialPlanList; // List of of PwPartialPlan
  private List transactionList; // List of List of PwTransaction


  /**
   * <code>PwPlanningSequenceImpl</code> - constructor -
   *            first (init) step partial plan passed in
   *
   * @param url - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param model - <code>PwModel</code> - 
   * @param transactionList - <code>List</code> - 
   */
  public PwPlanningSequenceImpl( String url, PwPartialPlan partialPlan, PwModel model,
                                 List transactionList) {
    this.url = url;
    stepCount = 1;
    partialPlanList = new ArrayList();
    partialPlanList.add( partialPlan);
    this.model = model;
    this.transactionList = new ArrayList();
    this.transactionList.add( transactionList);
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
   * <code>listTransactions</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>List</code> - of PwTransaction
   */
  public List listTransactions( int step) throws IndexOutOfBoundsException {
    if ((step >= 0) && (step < transactionList.size())) {
      return (List) transactionList.get( step);
    } else {
      throw new IndexOutOfBoundsException( "step " + step + " transactionList size " +
                                           transactionList.size());
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
    if ((step >= 0) && (step < partialPlanList.size())) {
      return (PwPartialPlan) partialPlanList.get( step);
    } else {
      throw new IndexOutOfBoundsException( "step " + step + " partialPlanList size " +
                                           partialPlanList.size());
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
    partialPlanList.add( partialPlan);
    transactionList.add( transactionList);
    return partialPlanList.size();
  } //end addPartialPlan


} // end class PwPlanningSequenceImpl
