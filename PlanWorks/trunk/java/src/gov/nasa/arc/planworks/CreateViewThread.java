// 
// $Id: CreateViewThread.java,v 1.3 2003-10-08 19:10:27 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 30sep03
//

package gov.nasa.arc.planworks;

import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>CreateViewThread</code> - handles PlanWorks render view actions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class CreateViewThread extends Thread {

  protected String viewName;
  protected String seqUrl;
  protected String sequenceName;

  /**
   * <code>CreateViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   */
  public CreateViewThread( String viewName) {
    this.viewName = viewName;
  }

  protected void renderView( String fullSequenceName, ViewableObject viewable) {
    ViewSet viewSet = PlanWorks.planWorks.viewManager.getViewSet( viewable);
    MDIInternalFrame viewFrame = null;
    boolean viewExists = false;
    String viewClassName = (String) PlanWorks.planWorks.viewClassNameMap.get( viewName);
    if ((viewSet != null) && viewSet.viewExists( viewClassName)) {
      viewExists = true;
    }
    if (PlanWorks.planWorks.supportedViewNames.contains( viewName)) {
      if (! viewExists) {
        if (viewable instanceof PwPartialPlan) {
          ((PwPartialPlan) viewable).setSeqName( fullSequenceName);
        } else if (viewable instanceof PwPlanningSequence) {
          ((PwPlanningSequence) viewable).setSeqName( fullSequenceName);
        }
        System.err.println( "Rendering " + viewName + " ...");
      }

      viewFrame = PlanWorks.planWorks.viewManager.openView( viewable, viewClassName);

      finishViewRendering( viewFrame, PlanWorks.planWorks.viewManager, viewExists, viewable);

    } else {
      JOptionPane.showMessageDialog
        (PlanWorks.planWorks, viewName, "View Not Supported", 
         JOptionPane.INFORMATION_MESSAGE);
    }
  } // end renderPartialPlanView

  protected void finishViewRendering( MDIInternalFrame viewFrame, ViewManager viewManager,
                                      boolean viewExists, ViewableObject viewable) {
    ViewSet viewSet = null;
    if (! viewExists) {
      while (viewSet == null) {
        // System.err.println( "wait for ViewSet");
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        viewSet = PlanWorks.planWorks.viewManager.getViewSet( viewable);
      }
      int planWorksFrameHeight = (int) PlanWorks.planWorks.getSize().getHeight();
      int contentSpecFrameHeight =
        (int) viewSet.getContentSpecWindow().getSize().getHeight();
      // locate view's upper left corner in top half of space below content spec
      int yFrameAvailable = (int) ((planWorksFrameHeight - contentSpecFrameHeight -
                                    ViewConstants.MDI_FRAME_DECORATION_HEIGHT) * 0.50);
      int yFrameDelta = 0;
      List viewList = null;
      if (viewable instanceof PwPartialPlan) {
        yFrameDelta = (int) (yFrameAvailable / PlanWorks.PARTIAL_PLAN_VIEW_LIST.size());
        viewList = PlanWorks.PARTIAL_PLAN_VIEW_LIST;
      } else if (viewable instanceof PwPlanningSequence) {
        yFrameDelta = (int) (yFrameAvailable / PlanWorks.SEQUENCE_VIEW_LIST.size());
        viewList = PlanWorks.SEQUENCE_VIEW_LIST;
      }
      Iterator viewItr = viewList.iterator();
      int viewIndex = 0;
      while (viewItr.hasNext()) {
        if (viewItr.next().equals( viewName)) {
          break;
        }
        viewIndex++;
      }
      int delta = Math.min( ((viewManager.getContentSpecWindowCnt() - 1) *
                             ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4),
                            (yFrameAvailable - ViewConstants.MDI_FRAME_DECORATION_HEIGHT));
      viewFrame.setLocation( (ViewConstants.INTERNAL_FRAME_X_DELTA * viewIndex) + delta,
                             contentSpecFrameHeight + yFrameDelta * viewIndex + delta);
      viewFrame.setVisible( true);
    }
    // make associated menus appear & bring window to the front
    try {
      viewFrame.setSelected( false);
      viewFrame.setSelected( true);
    } catch (PropertyVetoException excp) {
    }
  } // end finishViewRendering

} // end class CreateViewThread
