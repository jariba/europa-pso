// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceImpl.java,v 1.4 2004-03-23 18:20:47 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26Jan04
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceInstant;
import gov.nasa.arc.planworks.util.UniqueSet;


/**
 * <code>PwResourceImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwResourceImpl extends PwObjectImpl implements PwResource {

  private double initialCapacity;
  private double levelLimitMin;
  private double levelLimitMax;
  private int horizonStart;
  private int horizonEnd;
  private List instantIdList; 

  public PwResourceImpl(final Integer id, final int type, final Integer parentId,
                        final String name, final String childObjectIds, final String resInfo,
                        final String variableIds, final String tokenIds,
                        final PwPartialPlanImpl partialPlan) {
    super(id, type, parentId, name, childObjectIds, variableIds, tokenIds, partialPlan);

    StringTokenizer strTok = new StringTokenizer(resInfo, ",");
    this.horizonStart = Integer.parseInt(strTok.nextToken());
    this.horizonEnd = Integer.parseInt(strTok.nextToken());
    this.initialCapacity = Double.parseDouble(strTok.nextToken());
    this.levelLimitMin = Double.parseDouble(strTok.nextToken());
    this.levelLimitMax = Double.parseDouble(strTok.nextToken());
    this.instantIdList = new ArrayList();
    while(strTok.hasMoreTokens()) {
      instantIdList.add(Integer.valueOf(strTok.nextToken()));
    }
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
  public List getTransactionSet() {
    return getTokens();
  }

  /**
   * <code>getInstantList</code>
   *
   * @return - <code>List</code> - of PwResourceInstant
   */
  public List getInstantList() {
    return partialPlan.getInstantList(instantIdList);
  }

  public String toOutputString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(type).append("\t").append(parentId).append("\t");
    retval.append(partialPlan.getId()).append("\t").append(name).append("\t");
    if(!componentIdList.isEmpty()) {
      for(ListIterator it = componentIdList.listIterator(); it.hasNext();) {
        retval.append(it.next()).append(",");
      }
    }
    else {
      retval.append("\\N");
    }
    retval.append("\t");
    if(!variableIdList.isEmpty()) {
      for(ListIterator it = variableIdList.listIterator(); it.hasNext();) {
        retval.append(it.next()).append(",");
      }
    }
    else {
      retval.append("\\N");
    }
    retval.append("\t");
    if(!tokenIdList.isEmpty()) {
      for(ListIterator it = tokenIdList.listIterator(); it.hasNext();) {
        retval.append(it.next()).append(",");
      }
    }
    else {
      retval.append("\\N");
    }
    retval.append("\t");
    retval.append(horizonStart).append(",").append(horizonEnd).append(",").append(initialCapacity);
    retval.append(",").append(levelLimitMin).append(",").append(levelLimitMax).append("\n");
    return retval.toString();
  }

} // end class PwResourceImpl
