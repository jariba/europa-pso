// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwObject.java,v 1.3 2003-05-20 18:25:34 taylor Exp $
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
  public String getKey();

  /**
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public String getName();


  /**
   * <code>getTimelineList</code> -
   *
   * @return timelineList - <code>List</code> - of PwTimeline
   */
  public List getTimelineList();


} // end interface PwObject
