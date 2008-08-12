// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: UnaryResourceProfileView.java,v 1.1 2004-09-14 22:59:41 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 13sep04
//

package gov.nasa.arc.planworks.viz.partialPlan.resourceProfile;

import java.util.List;

import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.StringViewSetKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>UnaryResourceProfileView</code> - render the rescaled profile of one
 *                partial plan's resources
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class UnaryResourceProfileView extends ResourceProfileView implements StringViewSetKey {

  private String viewSetKey;

  /**
   * <code>UnaryResourceProfileView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param resource - <code>PwResource</code> - 
   * @param viewSetKey - <code>String</code> - 
   * @param unaryResourceProfileFrame - <code>MDIInternalFrame</code> - 
   */
  public UnaryResourceProfileView( final ViewableObject partialPlan, final ViewSet vSet,
                                   final PwResource resource, final String viewSetKey,
                                   final MDIInternalFrame unaryResourceProfileFrame,
                                   final List scalingList) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet, unaryResourceProfileFrame,
           resource, scalingList);
    this.viewSetKey = viewSetKey;
  } // end constructor


  /**
   * <code>getViewSetKey</code> - implements StringViewSetKey
   *
   * @return - <code>String</code> - 
   */
  public final String getViewSetKey() {
    return viewSetKey;
  }

} // end class UnaryResourceProfileView
