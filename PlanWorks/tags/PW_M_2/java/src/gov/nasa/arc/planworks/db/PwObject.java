// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwObject.java,v 1.4 2003-06-11 01:02:11 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 14May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwObject</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwObject {


  /**
   * <code>getKey</code>
   *
   * @return key - <code>String</code> -
   */
  public abstract String getKey();

  /**
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public abstract String getName();


  /**
   * <code>getTimelineList</code> -
   *
   * @return timelineList - <code>List</code> - of PwTimeline
   */
  public abstract List getTimelineList();


} // end interface PwObject
