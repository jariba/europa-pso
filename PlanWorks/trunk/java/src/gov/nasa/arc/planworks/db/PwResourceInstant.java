// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceInstant.java,v 1.1 2004-02-03 20:43:45 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 27Jan04
//

package gov.nasa.arc.planworks.db;

import gov.nasa.arc.planworks.db.PwIntervalDomain;


/**
 * <code>PwResourceInstant</code> - Represents the state of the Resource at an
 *                          instant in time within the horizon of the resource
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwResourceInstant {


  /**
   * <code>getId</code>
   *
   * @return id - <code>Integer</code> -
   */
  public abstract Integer getId();

  /**
   * <code>getTime</code>
   *
   * @return - <code>PwIntervalDomain</code> - 
   */
  public abstract PwIntervalDomain getTime();

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

  
} // end interface PwResourceInstant
