// $Id: PwPlanningSequence.java,v 1.2 2003-05-10 01:19:37 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;

import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>PwPlanningSequence</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public abstract class PwPlanningSequence {


  /**
   * <code>getStepCount</code>
   *
   * @return - <code>int</code> - 
   */
  public abstract int getStepCount();

  /**
   * <code>getUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getUrl();

  /**
   * <code>getModel</code>
   *
   * @return - <code>PwModel</code> - 
   */
  public abstract PwModel getModel();

  /**
   * <code>listTransactions</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>List</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public abstract List listTransactions( int step) throws IndexOutOfBoundsException;

  /**
   * <code>getPartialPlan</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>PartialPlan</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlan( int step) throws IndexOutOfBoundsException;

  /**
   * <code>addPartialPlan</code>
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param transactionList - <code>List</code> - of PwTransaction
   * @return - <code>int</code> - length of partial plan set
   */
  public abstract int addPartialPlan( PwPartialPlan partialPlan, List transactionList);

} // end class PwPlanningSequence
