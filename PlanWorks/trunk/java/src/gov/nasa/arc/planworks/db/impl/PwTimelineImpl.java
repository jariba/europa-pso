// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTimelineImpl.java,v 1.23 2004-03-23 18:20:50 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwTimeline;


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
} // end class PwTimelineImpl
