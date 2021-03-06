// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTimelineImpl.java,v 1.8 2003-06-12 23:49:46 taylor Exp $
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
  private String key;
  private List slotIdList;
  private PwPartialPlanImpl partialPlan;
  /**
   * <code>Timeline</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwTimelineImpl( String name, String key, PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.key = key;
    this.partialPlan = partialPlan;
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
   * @return name - <code>String</code> -
   */
  public String getKey() {
    return key;
  }
	
  /**
   * <code>getSlotList</code>
   *
   * @return name - <code>List</code> - of PwSlot
   */
  public List getSlotList() {
    List retval = new ArrayList( slotIdList.size());
    for (int i = 0; i < slotIdList.size(); i++) {
      retval.add( partialPlan.getSlot( (String) slotIdList.get(i)));
    }
    return retval;
  }

  /**
   * <code>addSlot</code>
   *
   * @param key - <code>String</code> - 
   * @return slot - <code>PwSlotImpl</code> - 
   */
  public PwSlotImpl addSlot( String key) {
    PwSlotImpl slot = new PwSlotImpl( key, partialPlan);
    slotIdList.add( key);
    partialPlan.addSlot( key, slot);
    return slot;
  } // end addSlot


} // end class PwTimelineImpl
