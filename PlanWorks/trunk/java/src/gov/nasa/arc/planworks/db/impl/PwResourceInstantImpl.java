// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceInstantImpl.java,v 1.9 2004-09-30 22:03:03 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 27Jan04
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResourceInstant;
import gov.nasa.arc.planworks.db.PwResourceTransaction;


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
  private Integer resourceId;
  private int time;
  private double levelMin;
  private double levelMax;
  private List transactionIds;
  private PwPartialPlanImpl partialPlan;

  public PwResourceInstantImpl( final Integer id,  final Integer resourceId, final int time, 
                                final double levelMin, final double levelMax, 
                                final String transactions,
                                final PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.resourceId = resourceId;
    this.time = time;
    this.levelMin = levelMin;
    this.levelMax = levelMax;
    this.partialPlan = partialPlan;
    
    transactionIds = new ArrayList();
    if(transactions != null) {
      StringTokenizer strTok = new StringTokenizer(transactions, ",");
      while(strTok.hasMoreTokens()) {
        transactionIds.add(Integer.valueOf(strTok.nextToken()));
      }
    }
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
   * <code>getResourceId</code>
   *
   * @return resourceId - <code>Integer</code> -
   */
  public Integer getResourceId() {
    return resourceId;
  }

  /**
   * <code>getTime</code>
   *
   * @return - <code>PwIntervalDomain</code> - 
   */
  public int getTime() {
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

  public List getTransactions() {
    return partialPlan.getResourceTransactionList(transactionIds);
  }

  public String toString() {
    return "Instant " + id + " at " + time + ": [" + levelMin + "-" + levelMax + "]";
  }

  public String toOutputString() {
    StringBuffer retval = new StringBuffer(partialPlan.getId().toString());
    retval.append("\t").append(resourceId).append("\t").append(id);
    retval.append("\t").append(time).append("\t").append(levelMin);
    retval.append("\t").append(levelMax).append("\t");
    for(ListIterator it = transactionIds.listIterator(); it.hasNext();) {
      retval.append(it.next()).append(",");
    }
    retval.append("\n");
    return retval.toString();
  }

  public List getNeighbors(){return null;}
  public List getNeighbors(List classes){return null;}
  public List getNeighbors(List classes, Set ids){return null;}
  public List getNeighbors(List classes, List linkTypes){return null;}
} // end class PwResourceInstantImpl
