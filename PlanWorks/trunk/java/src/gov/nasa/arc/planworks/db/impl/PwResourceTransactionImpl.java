// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceTransactionImpl.java,v 1.1 2004-02-03 20:43:48 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 27Jan04
//

package gov.nasa.arc.planworks.db.impl;

import java.util.List;

import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.PwResourceTransaction;


/**
 * <code>PwResourceTransactionImpl</code> - Transaction on a resource -
 *                                          a member of a unique set
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwResourceTransactionImpl implements PwResourceTransaction {

  private Integer id;
  private PwIntervalDomain interval;
  private double quantityMin;
  private double quantityMax;

  public PwResourceTransactionImpl( Integer id, PwIntervalDomain interval,
                                    double quantityMin, double quantityMax) {
    this.id = id;
    this.interval = interval;
    this.quantityMin = quantityMin;
    this.quantityMax = quantityMax;
  }

  /**
   * <code>getId</code>
   *
   * @return id - <code>Integer</code> -
   */
  public Integer getId() {
    return id;
  }

  /**
   * <code>getInterval</code>
   *
   * @return - <code>PwIntervalDomain</code> - 
   */
  public PwIntervalDomain getInterval() {
    return interval;
  }

  /**
   * <code>getQuantityMin</code>
   *
   * @return - <code>double</code> - 
   */
  public double getQuantityMin() {
    return quantityMin;
  }

  /**
   * <code>getQuantityMax</code>
   *
   * @return - <code>double</code> - 
   */
  public double getQuantityMax() {
    return quantityMax;
  }

  
} // end class PwResourceTransactionImpl
