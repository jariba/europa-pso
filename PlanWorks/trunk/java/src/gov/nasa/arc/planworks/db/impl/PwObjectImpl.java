// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwObjectImpl.java,v 1.3 2003-05-16 20:05:40 miatauro Exp $
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

  private String key;
  private String name;
  private List timelineList; // element PwTimelineImpl
		private PwPartialPlanImpl partialPlan;
		private String collectionName;

  public PwObjectImpl( String key, String name, PwPartialPlanImpl partialPlan, String collectionName) {
    this.key = key;
    this.name = name;
		this.partialPlan = partialPlan;
		this.collectionName = collectionName;
    timelineList = new ArrayList();
  } // end constructor


  /**
   * <code>addTimeline</code>
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   * @return timeline - <code>PwTimelineImpl</code> - 
   */
	//  public PwTimelineImpl addTimeline( String name, String key) {
	public PwTimelineImple addTimeline(String key)
	{
	  timelineList.add(key);
	  PwTimelineImpl timeline = partialPlan.getTimeline(key, collectionName);
	  //PwTimelineImpl timeline = new PwTimelineImpl( name, key);
	  //timelineList.add( timeline);
    return timeline;
  } // end addTimeline

	/**
	 * <code>getName</code>
	 *
	 * @return name - <code>String</code> -
	 */
	public String getName()
	{
		return this.name;
	}


	/**
	 * <code>getTimelineList</code> -
	 *
	 * @return timelineList - <code>List</code> -
	 */
	public List getTimelineList()
	{
		ArrayList retval = new ArrayList(timelineList.size());
		for(int i = 0; i < timelineList.size(); i++)
			retval.add(partialPlan.getTimeline((String)timelineList.get(i), collectionName));
		return retval;
	}
	 

} // end class PwObjectImpl
