// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceInstant.java,v 1.5 2004-07-27 21:58:04 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 27Jan04
//

package gov.nasa.arc.planworks.db;

import java.util.List;

/**
 * <code>PwResourceInstant</code> - Represents the state of the Resource at an
 *                          instant in time within the horizon of the resource
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwResourceInstant extends PwEntity {


  /**
   * <code>getId</code>
   *
   * @return id - <code>Integer</code> -
   */
  public abstract Integer getId();

  /**
   * <code>getResourceId</code>
   *
   * @return resourceId - <code>Integer</code> -
   */
  public abstract Integer getResourceId();

  /**
   * <code>getTime</code>
   *
   * @return - <code>PwIntervalDomain</code> - 
   */
  public abstract int getTime();

  /**
   * <code>getLevelMin</code>
   *
   * @return - <code>double</code> - 
   */
  public abstract double getLevelMin();

  /**
   * <code>getLevelMax</code>
   *
   * @return - <code>double</code> - 
   */
  public abstract double getLevelMax();

  public abstract List getTransactions();
} // end interface PwResourceInstant
