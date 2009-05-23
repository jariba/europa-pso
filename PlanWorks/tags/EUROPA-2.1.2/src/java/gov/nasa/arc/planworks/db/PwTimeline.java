// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTimeline.java,v 1.8 2004-02-05 23:23:33 miatauro Exp $
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
public interface PwTimeline extends PwObject {

  /**
   * <code>getSlotList</code>
   *
   * @return name - <code>List</code> - of PwSlot
   */
  public abstract List getSlotList();

} // end interface PwTimeline
