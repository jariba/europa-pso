// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwObjectImpl.java,v 1.17 2004-02-05 23:23:54 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 14May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwObject;


/**
 * <code>PwObjectImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwObjectImpl implements PwObject {
  private int type;
  protected Integer id;
  private Integer parentId;
  private String name;
  //private String emptySlotInfo;
  private List componentIdList; // element Integer
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
                       final String name, final String componentIds, 
                       final PwPartialPlanImpl partialPlan) {
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
  } // end constructor


  /**
   * <code>addTimeline</code>
   *
   * @param name - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @return timeline - <code>PwTimelineImpl</code> - 
   */
//   public PwTimelineImpl addTimeline( final String name, final Integer id) {
//     timelineIdList.add(id);
//     PwTimelineImpl timeline = new PwTimelineImpl( name, id, this.id, partialPlan);
//     partialPlan.addTimeline( id, timeline);
//     return timeline;
//   } // end addTimeline

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
    List retval = new ArrayList(componentIdList.size());
    ListIterator componentIdIterator = componentIdList.listIterator();
    while(componentIdIterator.hasNext()) {
      Integer compId = (Integer) componentIdIterator.next();
      PwObject comp = partialPlan.getObject(compId);
      if(comp != null) {
        retval.add(comp);
      }
      else {
        System.err.println("PwObjectImpl.getComponentList: ObjectId " + compId + " is null.");
      }
    }
    return retval;
  }

  public int getObjectType(){return type;}
  /**
   * <code>getTimelineList</code> -
   *
   * @return timelineList - <code>List</code> - of PwTimelineImpl
   */
//   public List getTimelineList() {
//     List retval = new ArrayList(timelineIdList.size());
//     for (int i = 0; i < timelineIdList.size(); i++) {
//       retval.add ( partialPlan.getTimeline((Integer) timelineIdList.get(i)));
//     }
//     return retval;
//   }
  
//   public void createEmptySlots() {
//     if(emptySlotInfo == null || haveCreatedSlots) {
//       return;
//     }
//     StringTokenizer slotTok = new StringTokenizer(emptySlotInfo, ":");
//     while(slotTok.hasMoreTokens()) {
//       StringTokenizer infoTok = new StringTokenizer(slotTok.nextToken(), ",");
//       Integer tId = new Integer(infoTok.nextToken());
//       Integer sId = new Integer(infoTok.nextToken());
//       int index = Integer.parseInt(infoTok.nextToken());
//       ((PwTimelineImpl)partialPlan.getTimeline(tId)).createEmptySlot(sId, index);
//     }
//     haveCreatedSlots = true;
//   }

//   public void calculateSlotTimes() {
//     if(haveCalculatedSlotTimes) {
//       return;
//     }
//     ListIterator timelineIterator = getTimelineList().listIterator();
//     while(timelineIterator.hasNext()) {
//       ((PwTimelineImpl)timelineIterator.next()).calculateSlotTimes();
//     }
//   }
} // end class PwObjectImpl
