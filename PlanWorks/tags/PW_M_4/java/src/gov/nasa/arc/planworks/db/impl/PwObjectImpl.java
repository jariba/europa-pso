// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwObjectImpl.java,v 1.11 2003-07-09 16:51:34 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 14May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.PwObject;


/**
 * <code>PwObjectImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwObjectImpl implements PwObject {

  private Integer key;
  private String name;
  private List timelineIdList; // element Integer
  private PwPartialPlanImpl partialPlan;

  /**
   * <code>PwObjectImpl</code> - constructor 
   *
   * @param key - <code>int</code> - 
   * @param name - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwObjectImpl( Integer key, String name, PwPartialPlanImpl partialPlan) {
    this.key = key;
    this.name = name;
    this.partialPlan = partialPlan;
    timelineIdList = new ArrayList();
  } // end constructor


  /**
   * <code>addTimeline</code>
   *
   * @param name - <code>String</code> - 
   * @param key - <code>int</code> - 
   * @return timeline - <code>PwTimelineImpl</code> - 
   */
  public PwTimelineImpl addTimeline( String name, Integer key) {
    timelineIdList.add(key);
    PwTimelineImpl timeline = new PwTimelineImpl( name, key, this.key, partialPlan);
    partialPlan.addTimeline( key, timeline);
    return timeline;
  } // end addTimeline

  /**
   * <code>getKey</code>
   *
   * @return key - <code>int</code> -
   */
  public Integer getKey() {
    return this.key;
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
	 

} // end class PwObjectImpl
