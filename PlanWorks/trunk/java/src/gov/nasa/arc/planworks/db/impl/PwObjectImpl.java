// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwObjectImpl.java,v 1.16 2004-02-03 22:43:42 miatauro Exp $
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

  private Integer id;
  private String name;
  private String emptySlotInfo;
  private List timelineIdList; // element Integer
  private PwPartialPlanImpl partialPlan;
  private boolean haveCreatedSlots;
  private boolean haveCalculatedSlotTimes;

  /**
   * <code>PwObjectImpl</code> - constructor 
   *
   * @param id - <code>Integer</code> - 
   * @param name - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwObjectImpl( final Integer id, final String name, final PwPartialPlanImpl partialPlan, 
                       final String info) {
    this.id = id;
    this.name = name;
    this.partialPlan = partialPlan;
    this.emptySlotInfo = info;
    timelineIdList = new ArrayList();
    haveCreatedSlots = false;
    haveCalculatedSlotTimes = false;
  } // end constructor


  /**
   * <code>addTimeline</code>
   *
   * @param name - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @return timeline - <code>PwTimelineImpl</code> - 
   */
  public PwTimelineImpl addTimeline( final String name, final Integer id) {
    timelineIdList.add(id);
    PwTimelineImpl timeline = new PwTimelineImpl( name, id, this.id, partialPlan);
    partialPlan.addTimeline( id, timeline);
    return timeline;
  } // end addTimeline

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


  /**
   * <code>getTimelineList</code> -
   *
   * @return timelineList - <code>List</code> - of PwTimelineImpl
   */
  public List getTimelineList() {
    List retval = new ArrayList(timelineIdList.size());
    for (int i = 0; i < timelineIdList.size(); i++) {
      retval.add ( partialPlan.getTimeline((Integer) timelineIdList.get(i)));
    }
    return retval;
  }
  
  public void createEmptySlots() {
    if(emptySlotInfo == null || haveCreatedSlots) {
      return;
    }
    StringTokenizer slotTok = new StringTokenizer(emptySlotInfo, ":");
    while(slotTok.hasMoreTokens()) {
      StringTokenizer infoTok = new StringTokenizer(slotTok.nextToken(), ",");
      Integer tId = new Integer(infoTok.nextToken());
      Integer sId = new Integer(infoTok.nextToken());
      int index = Integer.parseInt(infoTok.nextToken());
      ((PwTimelineImpl)partialPlan.getTimeline(tId)).createEmptySlot(sId, index);
    }
    haveCreatedSlots = true;
  }

  public void calculateSlotTimes() {
    if(haveCalculatedSlotTimes) {
      return;
    }
    ListIterator timelineIterator = getTimelineList().listIterator();
    while(timelineIterator.hasNext()) {
      ((PwTimelineImpl)timelineIterator.next()).calculateSlotTimes();
    }
  }
} // end class PwObjectImpl
