// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceViewSet.java,v 1.11 2003-12-20 00:46:20 miatauro Exp $
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
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSetRemover;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;


/**
 * <code>SequenceViewSet</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SequenceViewSet extends ViewSet {

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

    this.contentSpecWindow = desktopFrame.createFrame( ContentSpec.SEQUENCE_QUERY_TITLE +
                                                       " for " + viewable.getName(),
                                                       this, true, false, false, true);
    Container contentPane = this.contentSpecWindow.getContentPane();

//     this.contentSpec = new SequenceContentSpec( viewable, this);
//     ((PwPlanningSequence) viewable).setContentSpec( this.contentSpec.getCurrentSpec());
    contentPane.add( new SequenceQueryWindow( this.contentSpecWindow, desktopFrame,
                                              viewable, this));
    this.contentSpecWindow.pack();

    int delta = Math.min( (int) (((ViewManager) remover).getContentSpecWindowCnt() *
                                 ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4),
                          (int) ((PlanWorks.planWorks.getSize().getHeight() -
                                  ViewConstants.MDI_FRAME_DECORATION_HEIGHT) * 0.5));

    this.contentSpecWindow.setLocation( delta, delta);
    this.contentSpecWindow.setVisible(true);
  }
  public void close() {
    super.close();
    List partialPlans = ((PwPlanningSequence) viewable).getPartialPlansList();
    ListIterator planIterator = partialPlans.listIterator();
    while(planIterator.hasNext()) {
      ViewableObject partialPlan = (ViewableObject) planIterator.next();
      ViewSet set = getViewManager().getViewSet(partialPlan);
      System.err.println(partialPlan);
      if(set != null) {
        set.close();
        getViewManager().removeViewSet(partialPlan);
      }
    }
  }

} // end class SequenceViewSet

