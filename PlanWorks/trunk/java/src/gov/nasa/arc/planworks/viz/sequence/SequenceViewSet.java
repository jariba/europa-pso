// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceViewSet.java,v 1.4 2003-10-04 01:16:12 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.sequence;

import java.awt.Container;

import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.util.SequenceContentSpec;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSetRemover;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.ContentSpecWindow;


/**
 * <code>SequenceViewSet</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SequenceViewSet extends ViewSet {

  public SequenceViewSet( MDIDesktopFrame desktopFrame, ViewableObject viewable,
                             ViewSetRemover remover) {
    super( desktopFrame, viewable, remover);
    this.contentSpecWindow = desktopFrame.createFrame( "Content specification for " +
                                                       viewable.getName(),
                                                       this, true, false, false, true);
    Container contentPane = this.contentSpecWindow.getContentPane();

    this.contentSpec = new SequenceContentSpec( viewable, this);
    ((PwPlanningSequence) viewable).setContentSpec( this.contentSpec.getCurrentSpec());
    contentPane.add( new ContentSpecWindow( this.contentSpecWindow, this.contentSpec));
    this.contentSpecWindow.pack();

    this.contentSpecWindow.setSize( 300, 100); // until contentSpec created

    int delta = ((ViewManager) remover).getContentSpecWindowCnt() *
      ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4;
    this.contentSpecWindow.setLocation( delta, delta);
    this.contentSpecWindow.setVisible(true);
  }




} // end class SequenceViewSet

