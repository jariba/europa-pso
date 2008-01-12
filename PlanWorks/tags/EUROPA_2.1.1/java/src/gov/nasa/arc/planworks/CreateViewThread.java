// 
// $Id: CreateViewThread.java,v 1.21 2004-09-14 22:59:38 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 30sep03
//

package gov.nasa.arc.planworks;

import java.awt.Container;
import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecWindow;

/**
 * <code>CreateViewThread</code> - handles PlanWorks render view actions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class CreateViewThread extends ThreadWithProgressMonitor {

  protected String viewName;
  protected String seqUrl;
  protected String sequenceName;

  /**
   * <code>CreateViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   */
  public CreateViewThread( final String viewName) {
    this.viewName = viewName;
  }

  /**
   * <code>renderView</code>
   *
   * @param fullSequenceName - <code>String</code> - 
   * @param viewable - <code>ViewableObject</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   * @return - <code>MDIInternalFrame</code> - 
   */
  public MDIInternalFrame renderView( final String fullSequenceName, 
                                      final ViewableObject viewable,
                                      final ViewListener viewListener) {
    ViewSet viewSet = PlanWorks.getPlanWorks().viewManager.getViewSet( viewable);
    MDIInternalFrame viewFrame = null;
    boolean viewExists = false;
    String viewClassName = (String) PlanWorks.getPlanWorks().getViewClassName( viewName);
    viewExists = (viewSet != null) && viewSet.viewExists(viewClassName);
    if (PlanWorks.supportedViewNames.contains( viewName)) {
      if (! viewExists) {
        if (viewable instanceof PwPartialPlan) {
          ((PwPartialPlan) viewable).setName( fullSequenceName);
        } else if (viewable instanceof PwPlanningSequence) {
          ((PwPlanningSequence) viewable).setName( fullSequenceName);
        }
        System.err.println( "Rendering " + viewName + " ...");
      }

      viewFrame =
        PlanWorks.getPlanWorks().viewManager.openView( viewable, viewClassName, viewListener);
      
      finishViewRendering( viewFrame, PlanWorks.getPlanWorks().viewManager, viewExists,
                           viewName, viewable);

    } else {
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), viewName, "View Not Supported", 
         JOptionPane.INFORMATION_MESSAGE);
    }
    return viewFrame;
  } // end renderView

  private void finishViewRendering( final MDIInternalFrame viewFrame, 
                                    final ViewManager viewManager, final boolean viewExists, 
                                    final String viewName, final ViewableObject viewable) {
    ViewSet viewSet = null;
    while (viewSet == null) {
      // System.err.println( "wait for ViewSet");
      try {
        Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
      } catch (InterruptedException excp) {
      }
      viewSet = PlanWorks.getPlanWorks().viewManager.getViewSet( viewable);
    }
    if (! viewExists) {
      int planWorksFrameHeight = (int) PlanWorks.getPlanWorks().getSize().getHeight();
      int contentSpecFrameOffset = 0;
      MDIInternalFrame contentSpecWindow = viewSet.getContentSpecWindow();
      if (contentSpecWindow != null) {
        // SequenceQuery window -- use full height
        // ContentSpec window (partial plans) -- use 1/4 height
        contentSpecFrameOffset = (int) viewSet.getContentSpecWindow().getSize().getHeight();
        if (isContentSpecView( contentSpecWindow)) {
          contentSpecFrameOffset = (int) (contentSpecFrameOffset / 4);
        }
      }
      // locate view's upper left corner in top half of space below content spec
      int yFrameAvailable = (int) (planWorksFrameHeight - contentSpecFrameOffset -
                                    ViewConstants.MDI_FRAME_DECORATION_HEIGHT);
      int yFrameDelta = 0;
      List viewList = null;
      int sequenceStepsViewHeight = 0;
      // int deltaCnt = viewManager.getContentSpecWindowCnt();
      if (viewable instanceof PwPartialPlan) {
        // put content spec windows below the sequence steps window
        sequenceStepsViewHeight =
          (int) (PlanWorks.getPlanWorks().
                  getSequenceStepsViewFrame( seqUrl).getSize().getHeight() * 0.5);
        yFrameAvailable -= sequenceStepsViewHeight;
        yFrameDelta = (int) ((yFrameAvailable * 0.50) /
                             PlanWorks.PARTIAL_PLAN_VIEW_LIST.size());
        viewList = PlanWorks.PARTIAL_PLAN_VIEW_LIST;
        // deltaCnt--;
      } else if (viewable instanceof PwPlanningSequence) {
        yFrameDelta = (int) ((yFrameAvailable * 0.50)/ ViewConstants.SEQUENCE_VIEW_LIST.size());
        viewList = ViewConstants.SEQUENCE_VIEW_LIST;
        // deltaCnt--;
      }
      // System.err.println( "finishViewRendering: viewName " + viewName);
      Iterator viewItr = viewList.iterator();
      int viewIndex = 0;
      while (viewItr.hasNext()) {
        if (viewItr.next().equals( viewName)) {
          break;
        }
        viewIndex++;
      }
      // keep views from sliding off desktop
//       int delta = Math.min( (deltaCnt * ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4),
//                             (yFrameAvailable - (2 * ViewConstants.MDI_FRAME_DECORATION_HEIGHT)));
      int delta = 0;
      // do not use deltas -- causes windows to slip off screen
      viewFrame.setLocation( (ViewConstants.INTERNAL_FRAME_X_DELTA * viewIndex) + delta,
                             contentSpecFrameOffset + sequenceStepsViewHeight +
                             yFrameDelta * viewIndex + delta);
      viewFrame.setVisible( true);
    }
    // make associated menus appear & bring window to the front
    try {
      // in case content spec existed and was iconified
      if ((viewSet.getContentSpecWindow() != null) && viewSet.getContentSpecWindow().isIcon()) {
        viewSet.getContentSpecWindow().setIcon( false);
      }
      // in case view existed and was iconified
      if (viewFrame.isIcon()) {
        viewFrame.setIcon( false);
      }
      viewFrame.setSelected( false);
      viewFrame.setSelected( true);

    } catch (PropertyVetoException excp) {
    }
  } // end finishViewRendering

  private boolean isContentSpecView( final MDIInternalFrame window) {
    Container contentPane = window.getContentPane();
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      // System.err.println( "i " + i + " " +
      //                    contentPane.getComponent( i).getClass().getName());
      if (contentPane.getComponent(i) instanceof ContentSpecWindow) {
        return true;
      }
    }
    return false;
  } // end isContentSpecWindow

} // end class CreateViewThread
