// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceTransaction.java,v 1.1 2004-02-03 20:43:45 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 27Jan04
//

package gov.nasa.arc.planworks.db;

import java.util.List;

/**
 * <code>PwResourceTransaction</code> - Transactions on a resource 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwResourceTransaction {


  /**
   * <code>getId</code>
   *
   * @return id - <code>Integer</code> -
   */
  public abstract Integer getId();

  /**
   * <code>getInterval</code>
   *
   * @return - <code>PwIntervalDomain</code> - 
   */
  public abstract PwIntervalDomain getInterval();

  /**
   * <code>getQuantityMin</code>
   *
   * @return - <code>double</code> - 
   */
  public abstract double getQuantityMin();

  /**
   * <code>getQuantityMax</code>
   *
   * @return - <code>double</code> - 
   */
  public abstract double getQuantityMax();

  
} // end interface PwResourceTransaction
