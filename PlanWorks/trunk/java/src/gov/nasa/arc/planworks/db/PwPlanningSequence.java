// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequence.java,v 1.3 2003-05-15 18:38:44 taylor Exp $
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
public interface PwPlanningSequence {


  /**
   * <code>getStepCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getStepCount();

  /**
   * <code>getUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public String getUrl();

  /**
   * <code>getModel</code>
   *
   * @return - <code>PwModel</code> - 
   */
  public PwModel getModel();

  /**
   * <code>listTransactions</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>List</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public List listTransactions( int step) throws IndexOutOfBoundsException;

  /**
   * <code>getPartialPlan</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>PartialPlan</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public PwPartialPlan getPartialPlan( int step) throws IndexOutOfBoundsException;

  /**
   * <code>addPartialPlan</code>
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param transactionList - <code>List</code> - of PwTransaction
   * @return - <code>int</code> - length of partial plan set
   */
  public int addPartialPlan( PwPartialPlan partialPlan, List transactionList);

} // end interface PwPlanningSequence
