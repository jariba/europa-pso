// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResource.java,v 1.1 2004-02-03 20:43:45 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26Jan04
//

package gov.nasa.arc.planworks.db;

import java.util.List;

import gov.nasa.arc.planworks.util.UniqueSet;

/**
 * <code>PwResource</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwResource {


  /**
   * <code>getId</code>
   *
   * @return id - <code>Integer</code> -
   */
  public abstract Integer getId();

  /**
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public abstract String getName();

  /**
   * <code>getInitialCapacity</code>
   *
   * @return - <code>double</code> - 
   */
  public abstract double getInitialCapacity();

  /**
   * <code>getLevelLimitMin</code>
   *
   * @return - <code>double</code> - 
   */
  public abstract double getLevelLimitMin();
  
  /**
   * <code>getLevelLimitMax</code>
   *
   * @return - <code>double</code> - 
   */
  public abstract double getLevelLimitMax();

  /**
   * <code>getHorizonStart</code>
   *
   * @return - <code>int</code> - 
   */
  public abstract int getHorizonStart();

  /**
   * <code>getHorizonEnd</code>
   *
   * @return - <code>int</code> - 
   */
  public abstract int getHorizonEnd();

  /**
   * <code>getTransactionSet</code> -
   *
   * @return <code>UniqueSet</code> - of PwResourceTransaction
   */
  public abstract UniqueSet getTransactionSet();

  /**
   * <code>getInstantList</code>
   *
   * @return - <code>List</code> - of PwResourceInstant
   */
  public abstract List getInstantList();

} // end interface PwResource
