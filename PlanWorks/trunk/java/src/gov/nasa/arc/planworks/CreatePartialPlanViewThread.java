// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: CreatePartialPlanViewThread.java,v 1.1 2003-09-30 19:18:54 taylor Exp $
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
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>CreatePartialPlanViewThread</code> - handles PlanWorks partial plan view actions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class CreatePartialPlanViewThread implements Runnable {

  private String seqUrl;
  private String sequenceName;
  private String partialPlanName;
  private PwPartialPlan partialPlan;
  private String viewName;

  /**
   * <code>CreatePartialPlanViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>PlanWorks.SeqPartPlanViewMenuItem</code> - 
   */
  public CreatePartialPlanViewThread( String viewName,
                                      PlanWorks.SeqPartPlanViewMenuItem menuItem) {
    this.seqUrl = menuItem.getSeqUrl();
    this.sequenceName = menuItem.getSequenceName();
    this.partialPlanName = menuItem.getPartialPlanName();
    this.viewName = viewName;
  }  // end constructor

  /**
   * <code>run</code>
   *
   */
  public void run() { 
    try {
      PwPlanningSequence planSequence =
        PlanWorks.planWorks.currentProject.getPlanningSequence( seqUrl);
        
      PwPartialPlan partialPlan = planSequence.getPartialPlan(partialPlanName);
        
      renderPartialPlanView( viewName, sequenceName, partialPlanName, partialPlan);

    } catch (ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      rnfExcep.printStackTrace();
    }
  } //end run

  private void renderPartialPlanView( String viewName, String sequenceName,
                                      String partialPlanName,
                                      PwPartialPlan partialPlan) {
    ViewSet viewSet = PlanWorks.planWorks.viewManager.getViewSet( partialPlan);
    MDIInternalFrame viewFrame = null;
    boolean viewExists = false;
    String viewClassName = (String) PlanWorks.planWorks.viewClassNameMap.get( viewName);
    if ((viewSet != null) && viewSet.viewExists( viewClassName)) {
      viewExists = true;
    }
    if (PlanWorks.planWorks.supportedPartialPlanViewNames.contains( viewName)) {
      if (! viewExists) {
        partialPlan.setSeqName( sequenceName +
                                System.getProperty("file.separator") + partialPlanName);
        System.err.println( "Rendering " + viewName + " ...");
      }
      viewFrame = PlanWorks.planWorks.viewManager.openView( partialPlan, viewClassName);
      finishPartPlanViewRendering( viewFrame, viewName, PlanWorks.planWorks.viewManager,
                                   viewExists, partialPlan);
    } else {
      JOptionPane.showMessageDialog
        (PlanWorks.planWorks, viewName, "View Not Supported", 
         JOptionPane.INFORMATION_MESSAGE);
    }
  } // end renderPartialPlanView

  private void finishPartPlanViewRendering( MDIInternalFrame viewFrame, String viewName,
                                            ViewManager viewManager, boolean viewExists,
                                            PwPartialPlan partialPlan) {
    ViewSet viewSet = null;
    if (! viewExists) {
      while (viewSet == null) {
        // System.err.println( "wait for ViewSet");
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        viewSet = PlanWorks.planWorks.viewManager.getViewSet( partialPlan);
      }
      viewFrame.setSize( PlanWorks.INTERNAL_FRAME_WIDTH,
                         PlanWorks.planWorks.INTERNAL_FRAME_HEIGHT);
      int contentSpecFrameHeight =
        (int) viewSet.getContentSpecWindow().getSize().getHeight();
      Iterator viewItr = PlanWorks.planWorks.supportedPartialPlanViewNames.iterator();
      int viewIndex = 0;
      while (viewItr.hasNext()) {
        if (viewItr.next().equals( viewName)) {
          break;
        }
        viewIndex++;
      }
      viewFrame.setLocation( PlanWorks.INTERNAL_FRAME_X_DELTA * viewIndex,
                             contentSpecFrameHeight +
                             PlanWorks.INTERNAL_FRAME_Y_DELTA * viewIndex);
      viewFrame.setVisible( true);
    }
    // make associated menus appear & bring window to the front
    try {
      viewFrame.setSelected( false);
      viewFrame.setSelected( true);
    } catch (PropertyVetoException excp) { };
  } // end finishPartPlanViewRendering



} // end class CreatePartialPlanViewThread
