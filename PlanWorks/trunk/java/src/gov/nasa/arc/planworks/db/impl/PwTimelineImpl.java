// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTimelineImpl.java,v 1.15 2004-01-05 17:17:44 miatauro Exp $
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
  private Integer id;
  private Integer objectId;
  private List slotIdList;
  private PwPartialPlanImpl partialPlan;
  /**
   * <code>Timeline</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwTimelineImpl( String name, Integer id, Integer objectId, 
                         PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.id = id;
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
   * <code>getId</code>
   *
   * @return name - <code>Integer</code> -
   */
  public Integer getId() {
    return id;
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
   * @param id - <code>int</code> - 
   * @return slot - <code>PwSlotImpl</code> - 
   */
  public PwSlotImpl addSlot( Integer id) {
    PwSlotImpl slot = new PwSlotImpl( id, partialPlan);
    slotIdList.add( id);
    partialPlan.addSlot( id, slot);
    return slot;
  } // end addSlot

  public void createEmptySlot(Integer sId, int slotIndex) {
    PwSlotImpl slot = new PwSlotImpl(sId, partialPlan);
    slotIdList.add(slotIndex, sId);
    partialPlan.addSlot(sId, slot);
  }
} // end class PwTimelineImpl
