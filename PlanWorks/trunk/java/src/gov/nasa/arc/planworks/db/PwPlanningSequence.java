// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequence.java,v 1.16 2003-10-07 02:13:34 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;

import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>PwPlanningSequence</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwPlanningSequence extends ViewableObject {


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
   * <code>getName</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getName();

  public abstract Long getId();

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
   * <code>listPartialPlans</code>
   *
   * @return - <code>List</code> of PwPartialPlan
   */

  public abstract List listPartialPlans();

  /**
   * <code>listPartialPlans</code>
   *
   * @return - <code>List</code> of String
   */
  public abstract List listPartialPlanNames();

  /**
   * <code>getPartialPlan</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>PartialPlan</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlan( int step) throws IndexOutOfBoundsException,
  ResourceNotFoundException;


  /**
   * <code>getPartialPlan</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlan( String planName)
    throws ResourceNotFoundException;

  /**
   * <code>delete</code>
   *
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract void delete() throws ResourceNotFoundException;

  // extend ViewableObject

  /**
   * <code>setContentSpec</code>
   *
   * @param spec - <code>List</code> - 
   */
  public abstract void setContentSpec( List spec);

  /**
   * <code>getContentSpec</code>
   *
   * @return - <code>List</code> - 
   */
  public abstract List getContentSpec();

  // getName already defined
  
  // end extend ViewableObject

  /**
   * <code>setSeqName</code>
   *
   * @param seqName - <code>String</code> - 
   */
  public void setSeqName( String seqName);

} // end interface PwPlanningSequence
