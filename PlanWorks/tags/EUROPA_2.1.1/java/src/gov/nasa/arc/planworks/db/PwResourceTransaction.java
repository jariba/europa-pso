// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceTransaction.java,v 1.2 2004-02-27 18:04:14 miatauro Exp $
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
public interface PwResourceTransaction extends PwToken {

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
