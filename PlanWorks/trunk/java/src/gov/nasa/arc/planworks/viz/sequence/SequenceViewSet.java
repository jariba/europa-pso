// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceViewSet.java,v 1.9 2003-11-03 19:02:41 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.sequence;

import java.awt.Container;
import java.util.HashMap;

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


  /**
   * <code>getViews</code> - make views accessible to SequenceQueryWindow, so that
   *                         MDIInternalFrames which it creates for QueryResults,
   *                         can be added to the view set -- and hence be deleted
   *                         when the view set is deleted.
   *
   * @return - <code>HashMap</code> - 
   */
  public HashMap getViews() {
    return views;
  }

} // end class SequenceViewSet

