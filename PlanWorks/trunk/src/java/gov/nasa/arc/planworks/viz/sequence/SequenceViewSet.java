// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceViewSet.java,v 1.22 2005-11-10 01:22:13 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.sequence;

import java.awt.Container;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.db.util.SequenceContentSpec;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSetRemover;
//import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;


/**
 * <code>SequenceViewSet</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SequenceViewSet extends ViewSet {

  private int planControllerFrameCnt;
    private int debugConsoleFrameCnt;

  /**
   * <code>SequenceViewSet</code> - constructor 
   *
   * @param desktopFrame - <code>MDIDesktopFrame</code> - 
   * @param viewable - <code>ViewableObject</code> - 
   * @param remover - <code>ViewSetRemover</code> - 
   */
  public SequenceViewSet( MDIDesktopFrame desktopFrame, ViewableObject viewable,
                             ViewSetRemover remover) {
    super( desktopFrame, viewable, remover);

    // this.contentSpecWindow = desktopFrame.createFrame( ViewConstants.SEQUENCE_QUERY_TITLE +
//                                                        " for " + viewable.getName(),
//                                                        this, true, false, false, true);
//     Container contentPane = this.contentSpecWindow.getContentPane();

// //     this.contentSpec = new SequenceContentSpec( viewable, this);
// //     ((PwPlanningSequence) viewable).setContentSpec( this.contentSpec.getCurrentSpec());
//     contentPane.add( new SequenceQueryWindow( this.contentSpecWindow, desktopFrame,
//                                               viewable, this));
//     this.contentSpecWindow.pack();

//     int delta = 0;
//     // do not use deltas -- causes windows to slip off screen
// //     int delta = Math.min( (int) (((ViewManager) remover).getContentSpecWindowCnt() *
// //                                  ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4),
// //                           (int) ((PlanWorks.getPlanWorks().getSize().getHeight() -
// //                                   ViewConstants.MDI_FRAME_DECORATION_HEIGHT) * 0.5));

//     this.contentSpecWindow.setLocation( delta, delta);
//     this.contentSpecWindow.setVisible(true);
    planControllerFrameCnt = 0;
    debugConsoleFrameCnt = 0;
  } // end constructor

  /**
   * <code>getPlanControllerFrameCnt</code>
   *
   * @return - <code>int</code> - 
   */
//   public int getPlanControllerFrameCnt() {
//     return planControllerFrameCnt;
//   }

  /**
   * <code>incrPlanControllerFrameCnt</code>
   *
   */
//   public void incrPlanControllerFrameCnt() {
//     planControllerFrameCnt++;
//   }

  /**
   * <code>setPlanControllerFrameCnt</code> - would be needed for a menu item: delete
   *                                    plan controller windows
   *
   * @param cnt - <code>int</code> - 
   */
//   public void setPlanControllerFrameCnt( int cnt) {
//     planControllerFrameCnt = cnt;
//   }

  /**
   * <code>getPlanControllerViewSetKey</code>
   *
   * @return - <code>String</code> - 
   */
  public String getPlanControllerViewSetKey() {
    planControllerFrameCnt++;
    return new String( ViewConstants.PLANNER_CONTROLLER_TITLE.replaceAll( " ", "") + "-" +
                       planControllerFrameCnt);
  }

    public String getDebugConsoleViewSetKey() {
	debugConsoleFrameCnt++;
	return new String(ViewConstants.DEBUG_CONSOLE_TITLE.replaceAll(" ", "") + "-" +
			  debugConsoleFrameCnt);
    }

  /**
   * <code>close</code>
   *
   */
  public void close() {
    super.close();
    List partialPlans = ((PwPlanningSequence) viewable).getPartialPlansList();
    ListIterator planIterator = partialPlans.listIterator();
    while(planIterator.hasNext()) {
      ViewableObject partialPlan = (ViewableObject) planIterator.next();
      ViewSet set = getViewManager().getViewSet(partialPlan);
       if(set != null) {
        set.close();
        getViewManager().removeViewSet(partialPlan);
      }
    }
    try {
      PlanWorks.getPlanWorks().getCurrentProject().
        closePlanningSequence(((PwPlanningSequence)viewable).getId());
    }
    catch(Exception e) {
      System.err.println(e);
      e.printStackTrace();
    }
  }

} // end class SequenceViewSet

