// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTimeline.java,v 1.7 2003-08-19 00:24:00 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwTimeline</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwTimeline {

  /**
   * <code>getName</code>
   *
   * @return <code>String</code> -
   */
  public abstract String getName();

  /**
   * <code>getId</code>
   *
   * @return <code>Integer</code> -
   */
  public abstract Integer getId();
	
  /**
   * <code>getSlotList</code>
   *
   * @return name - <code>List</code> - of PwSlot
   */
  public abstract List getSlotList();

  /**
   * <code>getObjectId</code>
   *
   * @return <code>Integer</code>
   */

  public abstract Integer getObjectId();

} // end interface PwTimeline
