// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwObjectImpl.java,v 1.28 2004-09-30 22:03:02 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 14May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.viz.ViewConstants;

/**
 * <code>PwObjectImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwObjectImpl implements PwObject {
  protected int type;
  protected Integer id;
  protected Integer parentId;
  protected String name;
  //private String emptySlotInfo;
  protected List componentIdList; // element Integer
  protected List variableIdList;
  protected List tokenIdList;
  protected PwPartialPlanImpl partialPlan;
  //  private boolean haveCreatedSlots;
  //   private boolean haveCalculatedSlotTimes;

  /**
   * <code>PwObjectImpl</code> - constructor 
   *
   * @param id - <code>Integer</code> - 
   * @param name - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwObjectImpl( final Integer id, final int objectType, final Integer parentId,
                       final String name, final String componentIds, final String variableIds,
                       final String tokenIds, final PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.name = name;
    this.partialPlan = partialPlan;
    this.parentId = parentId;
    //this.emptySlotInfo = info;
    type = objectType;
//     haveCreatedSlots = false;
//     haveCalculatedSlotTimes = false;
    componentIdList = new ArrayList();
    if(componentIds != null) {
      StringTokenizer strTok = new StringTokenizer(componentIds, ",");
      while(strTok.hasMoreTokens()) {
        componentIdList.add(Integer.valueOf(strTok.nextToken()));
      }
    }
    variableIdList = new ArrayList();
    if(variableIds != null) {
      StringTokenizer strTok = new StringTokenizer(variableIds, ",");
      while(strTok.hasMoreTokens()) {
        variableIdList.add(Integer.valueOf(strTok.nextToken()));
      }
    }
    tokenIdList = new ArrayList();
    if(tokenIds != null) {
      StringTokenizer strTok = new StringTokenizer(tokenIds, ",");
      while(strTok.hasMoreTokens()) {
        tokenIdList.add(Integer.valueOf(strTok.nextToken()));
      }
    }
  } // end constructor

  /**
   * <code>getId</code>
   *
   * @return id - <code>Integer</code> -
   */
  public Integer getId() {
    return this.id;
  }

  /**
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public String getName() {
    return this.name;
  }

  public PwObject getParent() {
    return partialPlan.getObject(parentId);
  }

  public Integer getParentId() {
    return parentId;
  }

  public List getComponentList() {
    return partialPlan.getObjectList(componentIdList);
  }

  public int getObjectType(){return type;}

  public List getVariables() {
    return partialPlan.getVariableList(variableIdList);
  }

  public List getTokens() {
    return partialPlan.getTokenList(tokenIdList);
  }

  public void addToken(Integer tokenId) {
    if(!tokenIdList.contains(tokenId)) {
      tokenIdList.add(tokenId);
    }
  }

  public String toString() {
    return id.toString();
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
    retval.append("\t").append("\\N").append("\n");
    return retval.toString();
  }

  public List getNeighbors() {
    List classes = new LinkedList();
    classes.add(PwToken.class);
    classes.add(PwVariable.class);
    classes.add(PwObject.class);
    return getNeighbors(classes);
  }

  public List getNeighbors(List classes) {
    List retval = new LinkedList();
    for(Iterator classIt = classes.iterator(); classIt.hasNext();) {
      Class cclass = (Class) classIt.next();
      // if(cclass.equals(PwToken.class))
      if (PwToken.class.isAssignableFrom( cclass))
        retval.addAll(getTokens());
      else if(PwVariable.class.isAssignableFrom( cclass))
        retval.addAll(getVariables());
      //else if(cclass.equals(PwObject.class)) {
      else if(PwObject.class.isAssignableFrom( cclass)) {
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
      if(linkType.equals(ViewConstants.OBJECT_TO_OBJECT_LINK_TYPE) &&
         CollectionUtils.findFirst(new AssignableFunctor(PwObject.class), classes) != null) {
        if(getParent() != null)
          retval.add(getParent());
        retval.addAll(getComponentList());
      }
      else if(linkType.equals(ViewConstants.OBJECT_TO_VARIABLE_LINK_TYPE) &&
              CollectionUtils.findFirst(new AssignableFunctor(PwVariable.class), classes) != null)
        retval.addAll(getVariables());
      else if(linkType.equals(ViewConstants.OBJECT_TO_TOKEN_LINK_TYPE) &&
              CollectionUtils.findFirst(new AssignableFunctor(PwToken.class), classes) != null)
        retval.addAll(getTokens());
    }
    return retval;
  }

  public List getNeighbors(List classes, Set ids) {
    return PwEntityImpl.getNeighbors(this, classes, ids);
  }

} // end class PwObjectImpl
