// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTimelineImpl.java,v 1.10 2003-07-09 16:51:35 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwTimeline;


/**
 * <code>PwTimelineImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTimelineImpl implements PwTimeline {

  private String name;
  private Integer key;
  private Integer objectId;
  private List slotIdList;
  private PwPartialPlanImpl partialPlan;
  /**
   * <code>Timeline</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>int</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwTimelineImpl( String name, Integer key, Integer objectId, 
                         PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.key = key;
    this.partialPlan = partialPlan;
    this.objectId = objectId;
    slotIdList = new ArrayList();
  } // end constructor

  /**
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public String getName() {
    return name;
  }

  /**
   * <code>getKey</code>
   *
   * @return name - <code>int</code> -
   */
  public Integer getKey() {
    return key;
  }
  
  public Integer getObjectId() {
    return objectId;
  }
  /**
   * <code>getSlotList</code>
   *
   * @return name - <code>List</code> - of PwSlot
   */
  public List getSlotList() {
    List retval = new ArrayList( slotIdList.size());
    for (int i = 0; i < slotIdList.size(); i++) {
      retval.add( partialPlan.getSlot( (Integer) slotIdList.get(i)));
    }
    return retval;
  }

  /**
   * <code>addSlot</code>
   *
   * @param key - <code>int</code> - 
   * @return slot - <code>PwSlotImpl</code> - 
   */
  public PwSlotImpl addSlot( Integer key) {
    PwSlotImpl slot = new PwSlotImpl( key, partialPlan);
    slotIdList.add( key);
    partialPlan.addSlot( key, slot);
    return slot;
  } // end addSlot


} // end class PwTimelineImpl
