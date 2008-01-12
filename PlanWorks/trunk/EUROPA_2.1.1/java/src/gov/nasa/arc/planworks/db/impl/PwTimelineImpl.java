// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTimelineImpl.java,v 1.26 2004-09-30 22:03:03 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>PwTimelineImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTimelineImpl extends PwObjectImpl implements PwTimeline {

  private List slotIdList;
  private String emptySlotInfo;
  private boolean hasCreatedEmptySlots;
  private boolean hasCalculatedSlotTimes;
  /**
   * <code>Timeline</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwTimelineImpl(final Integer id, final int type, final Integer parentId, 
                        final String name, final String childObjectIds, final String emptySlotInfo,
                        final String variableIds, final String tokenIds,
                        final PwPartialPlanImpl partialPlan) {
    super(id, type, parentId, name, childObjectIds, variableIds, tokenIds, partialPlan);
    this.emptySlotInfo = emptySlotInfo;
    slotIdList = new ArrayList();
    hasCreatedEmptySlots = hasCalculatedSlotTimes = false;
  } // end constructor

  /**
   * <code>getSlotList</code>
   *
   * @return name - <code>List</code> - of PwSlot
   */
  public List getSlotList() {
    return partialPlan.getSlotList(slotIdList);
  }

  /**
   * <code>addSlot</code>
   *
   * @param id - <code>int</code> - 
   * @return slot - <code>PwSlotImpl</code> - 
   */
  public PwSlotImpl addSlot( final Integer id) {
    if(slotIdList.contains(id)) {
      return (PwSlotImpl) partialPlan.getSlot(id);
    }
    PwSlotImpl slot = new PwSlotImpl( id, this.id, partialPlan);
    slotIdList.add( id);
    partialPlan.addSlot( id, slot);
    return slot;
  } // end addSlot

  private void createEmptySlot(final Integer sId, final int slotIndex) {
    PwSlotImpl slot = new PwSlotImpl(sId, this.id, partialPlan);
    slotIdList.add(slotIndex, sId);
    partialPlan.addSlot(sId, slot);
  }

  public void finishSlots() {
    createEmptySlots();
    calculateSlotTimes();
  }

  private void createEmptySlots() {
    if(!hasCreatedEmptySlots && emptySlotInfo != null) {
      StringTokenizer strTok = new StringTokenizer(emptySlotInfo, ":");
      while(strTok.hasMoreTokens()) {
        String emptySlot = strTok.nextToken();
        StringTokenizer subTok = new StringTokenizer(emptySlot, ",");
        if(subTok.hasMoreTokens()) {
          createEmptySlot(Integer.valueOf(subTok.nextToken()), 
                          Integer.parseInt(subTok.nextToken()));
        }
      }
      hasCreatedEmptySlots = true;
    }
  }

  private void calculateSlotTimes() {
    createEmptySlots();
    if(!hasCalculatedSlotTimes) {
      ListIterator slotIterator = getSlotList().listIterator();
      PwSlotImpl prev = null;
      while(slotIterator.hasNext()) {
        PwSlotImpl next = null;
        PwSlotImpl slot = (PwSlotImpl) slotIterator.next();
        if(slotIterator.hasNext()) {
          next = (PwSlotImpl) slotIterator.next();
        }
        slot.calcTimes(prev, next);
        if(next != null) {
          slotIterator.previous();
        }
        prev = slot;
      }
      hasCalculatedSlotTimes = true;
    }
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
    if(emptySlotInfo == null) {
      retval.append("\\N");
    }
    else {
      retval.append(emptySlotInfo);
      }
    retval.append("\n");
    return retval.toString();
  }

  public List getNeighbors() {
    List classes = new LinkedList();
    classes.add(PwTimeline.class);
    classes.add(PwVariable.class);
    classes.add(PwSlot.class);
    return getNeighbors(classes);
  }

  public List getNeighbors(List classes) {
    List retval = new LinkedList();
    for(Iterator classIt = classes.iterator(); classIt.hasNext();) {
      Class cclass = (Class) classIt.next();
      if(PwSlot.class.isAssignableFrom( cclass)) {
        retval.addAll(getSlotList());
      } else if(PwVariable.class.isAssignableFrom( cclass)) {
        retval.addAll(((PwVariableContainer) this).getVariables());
      } else if(PwTimeline.class.isAssignableFrom( cclass)) {
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
      if(linkType.equals(ViewConstants.TIMELINE_TO_SLOT_LINK_TYPE) &&
         CollectionUtils.findFirst(new AssignableFunctor(PwSlot.class), classes) != null) {
        retval.addAll(getSlotList());
      }
      else if(linkType.equals(ViewConstants.TIMELINE_TO_VARIABLE_LINK_TYPE) &&
              CollectionUtils.findFirst(new AssignableFunctor(PwVariable.class), classes) != null) {
        retval.addAll(getVariables());
      }
      else if((linkType.equals(ViewConstants.TIMELINE_TO_OBJECT_LINK_TYPE) ||
               linkType.equals(ViewConstants.TIMELINE_TO_RESOURCE_LINK_TYPE) ||
               linkType.equals(ViewConstants.TIMELINE_TO_TIMELINE_LINK_TYPE)) &&
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


} // end class PwTimelineImpl
