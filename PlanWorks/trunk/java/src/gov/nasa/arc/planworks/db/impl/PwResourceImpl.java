// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceImpl.java,v 1.1 2004-02-03 20:43:47 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26Jan04
//

package gov.nasa.arc.planworks.db.impl;

import java.util.List;

import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.util.UniqueSet;


/**
 * <code>PwResourceImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwResourceImpl implements PwResource {

  private Integer id;
  private String name;
  private double initialCapacity;
  private double levelLimitMin;
  private double levelLimitMax;
  private int horizonStart;
  private int horizonEnd;
  private UniqueSet transactionSet; // element PwResourceTransaction
  private List instantList; // element PwResourceInstant

  public PwResourceImpl( Integer id, String name, double initialCapacity, double levelLimitMin,
                         double levelLimitMax, int horizonStart, int horizonEnd,
                         UniqueSet transactionSet, List instantList) {
    this.id = id;
    this.name = name;
    this.initialCapacity = initialCapacity;
    this.levelLimitMin = levelLimitMin;
    this.levelLimitMax = levelLimitMax;
    this.horizonStart = horizonStart;
    this.horizonEnd = horizonEnd;
    this.transactionSet = transactionSet;
    this.instantList = instantList;
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
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public String getName() {
    return name;
  }

  /**
   * <code>getInitialCapacity</code>
   *
   * @return - <code>double</code> - 
   */
  public double getInitialCapacity() {
    return initialCapacity;
  }

  /**
   * <code>getLevelLimitMin</code>
   *
   * @return - <code>double</code> - 
   */
  public double getLevelLimitMin() {
    return levelLimitMin;
  }
  
  /**
   * <code>getLevelLimitMax</code>
   *
   * @return - <code>double</code> - 
   */
  public double getLevelLimitMax() {
    return levelLimitMax;
  }

  /**
   * <code>getHorizonStart</code>
   *
   * @return - <code>int</code> - 
   */
  public int getHorizonStart() {
    return horizonStart;
  }

  /**
   * <code>getHorizonEnd</code>
   *
   * @return - <code>int</code> - 
   */
  public int getHorizonEnd() {
    return horizonEnd;
  }

  /**
   * <code>getTransactionSet</code> -
   *
   * @return <code>UniqueSet</code> - of PwResourceTransaction
   */
  public UniqueSet getTransactionSet() {
    return transactionSet;
  }

  /**
   * <code>getInstantList</code>
   *
   * @return - <code>List</code> - of PwResourceInstant
   */
  public List getInstantList() {
    return instantList;
  }



} // end class PwResourceImpl
