// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTimelineImpl.java,v 1.1 2003-05-15 22:16:23 taylor Exp $
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
  private List slotList; // element PwSlotImpl


  /**
   * <code>Timeline</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   */
  public PwTimelineImpl( String name, String key) {
    this.name = name;
    this.key = key;
    slotList = new ArrayList();

  } // end constructor

  /**
   * <code>addSlot</code>
   *
   * @param key - <code>String</code> - 
   * @return slot - <code>PwSlotImpl</code> - 
   */
  public PwSlotImpl addSlot( String key) {
    PwSlotImpl slot = new PwSlotImpl( key);
    slotList.add( slot);
    return slot;
  } // end addSlot



} // end class PwTimelineImpl
