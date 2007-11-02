// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceImpl.java,v 1.7 2004-09-30 22:03:02 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26Jan04
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceInstant;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.ViewConstants;


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

  public List getNeighbors() {
    List classes = new LinkedList();
    classes.add(PwResourceTransaction.class);
    classes.add(PwVariable.class);
    classes.add(PwResource.class);
    return getNeighbors(classes);
  }

  public List getNeighbors(List classes) {
    List retval = new LinkedList();
    for(Iterator classIt = classes.iterator(); classIt.hasNext();) {
      Class cclass = (Class) classIt.next();
      if(PwResourceTransaction.class.isAssignableFrom( cclass))
        retval.addAll(getTransactionSet());
      else if(PwVariable.class.isAssignableFrom( cclass))
        retval.addAll(((PwVariableContainer) this).getVariables());
      else if(PwResource.class.isAssignableFrom( cclass)) {
	if (getParent() != null) {
	  retval.add(getParent());
	}
        retval.addAll(getComponentList());
      }
    }
    return retval;
  }

  public List getNeighbors(List classes, List linkTypes) {
    List retval = new LinkedList();
    for(Iterator it = linkTypes.iterator(); it.hasNext();) {
      String linkType = (String) it.next();
      if(linkType.equals(ViewConstants.RESOURCE_TO_TOKEN_LINK_TYPE) &&
         CollectionUtils.findFirst(new AssignableFunctor(PwResourceTransaction.class), classes) != null)
        retval.addAll(getTransactionSet());
      else if(linkType.equals(ViewConstants.RESOURCE_TO_VARIABLE_LINK_TYPE) &&
              CollectionUtils.findFirst(new AssignableFunctor(PwVariable.class), classes) != null)
        retval.addAll(getVariables());
      else if((linkType.equals(ViewConstants.OBJECT_TO_OBJECT_LINK_TYPE) ||
               linkType.equals(ViewConstants.OBJECT_TO_RESOURCE_LINK_TYPE) ||
               linkType.equals(ViewConstants.TIMELINE_TO_RESOURCE_LINK_TYPE)) &&
              CollectionUtils.findFirst(new AssignableFunctor(PwObject.class), classes) != null) {
        if(getParent() != null)
          retval.add(getParent());
        retval.addAll(getComponentList());
      }
    }
    return retval;
  }

  public List getNeighbors(List classes, Set ids) {
    return PwEntityImpl.getNeighbors(this, classes, ids);
  }

} // end class PwResourceImpl
