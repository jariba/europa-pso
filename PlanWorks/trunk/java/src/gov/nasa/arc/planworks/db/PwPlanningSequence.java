// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequence.java,v 1.5 2003-06-08 00:14:07 taylor Exp $
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
   * <code>getName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getName();

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
   * <code>addPartialPlan</code> -
   *          maintain PwPartialPlanImpl instance ordering of partialPlanNames
   *
   * @param url - <code>String</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @return - <code>PartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwPartialPlan addPartialPlan( String url, String partialPlanName)
    throws ResourceNotFoundException ;

} // end interface PwPlanningSequence
