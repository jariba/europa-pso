// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceInstantImpl.java,v 1.1 2004-02-03 20:43:47 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 27Jan04
//

package gov.nasa.arc.planworks.db.impl;

import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.PwResourceInstant;


/**
 * <code>PwResourceInstant</code> - Represents the state of the Resource at an
 *                          instant in time within the horizon of the resource
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwResourceInstantImpl implements PwResourceInstant {

  private Integer id;
  private PwIntervalDomain time;
  private double levelMin;
  private double levelMax;

  public PwResourceInstantImpl( Integer id, PwIntervalDomain time, double levelMin,
                                double levelMax) {
    this.id = id;
    this.time = time;
    this.levelMin = levelMin;
    this.levelMax = levelMax;
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
   * <code>getTime</code>
   *
   * @return - <code>PwIntervalDomain</code> - 
   */
  public PwIntervalDomain getTime() {
    return time;
  }

  /**
   * <code>getLevelMin</code>
   *
   * @return - <code>double</code> - 
   */
  public double getLevelMin() {
    return levelMin;
  }

  /**
   * <code>getLevelMax</code>
   *
   * @return - <code>double</code> - 
   */
  public double getLevelMax() {
    return levelMax;
  }

  
} // end class PwResourceInstantImpl
