// 
// $Id: CreateViewThread.java,v 1.1 2003-10-01 23:53:54 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 30sep03
//

package gov.nasa.arc.planworks;

import java.beans.PropertyVetoException;
import java.util.Iterator;
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
      int contentSpecFrameHeight =
        (int) viewSet.getContentSpecWindow().getSize().getHeight();
      Iterator viewItr = PlanWorks.planWorks.supportedViewNames.iterator();
      int viewIndex = 0;
      while (viewItr.hasNext()) {
        if (viewItr.next().equals( viewName)) {
          break;
        }
        viewIndex++;
      }
      viewFrame.setLocation( ViewConstants.INTERNAL_FRAME_X_DELTA * viewIndex,
                             contentSpecFrameHeight +
                             ViewConstants.INTERNAL_FRAME_Y_DELTA * viewIndex);
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
