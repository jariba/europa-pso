// 
// $Id: CreateSequenceViewThread.java,v 1.12 2004-07-27 21:58:02 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 30sep03
//

package gov.nasa.arc.planworks;

import java.lang.reflect.InvocationTargetException;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>CreateSequenceViewThread</code> - handles PlanWorks sequence view actions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class CreateSequenceViewThread extends CreateViewThread {

  private static Object staticObject = new Object();

  private PwPlanningSequence planSequence;
  private ViewListener viewListener;

  /**
   * <code>CreateSequenceViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>JMenuItem</code> - 
   */
  public CreateSequenceViewThread( final String viewName,
                                   final SequenceViewMenuItem menuItem) {
    super( viewName);
    this.seqUrl = menuItem.getSeqUrl();
    this.sequenceName = menuItem.getSequenceName();
    this.viewListener = menuItem.getViewListener();
  }

  /**
   * <code>run</code>
   *
   */
  public void run() {
//     try {
//       SwingUtilities.invokeAndWait( new Runnable() {
//           public void run() {
//             createSequenceView();
//           }
//         });
//     } catch (InterruptedException ie) {
//       System.err.println( "CreateSequenceViewThread: InterruptedException");
//       ie.printStackTrace();
//     } catch (InvocationTargetException ite) {
//       System.err.println( "CreateSequenceViewThread: InvocationTargetException");
//       ite.printStackTrace();
//     }

    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          createSequenceView();
          return null;
        }
    };
    worker.start();  
  } // end run

  private void createSequenceView() {
    synchronized( staticObject) {
      progressMonitorThread( "Get Planning Sequence ...", 0, 6);
      if (! progressMonitorWait()) {
        return;
      }
      progressMonitor.setProgress( 3 * ViewConstants.MONITOR_MIN_MAX_SCALING);

      PlanWorks.getPlanWorks().setViewRenderingStartTime( System.currentTimeMillis(), viewName);
      ViewGenerics.setRedrawCursor( PlanWorks.getPlanWorks());
      MDIDynamicMenuBar dynamicMenuBar =
        (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
      JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
      PlanWorks.getPlanWorks().projectMenu.setEnabled( false);

      try {
        planSequence = PlanWorks.getPlanWorks().currentProject.getPlanningSequence( seqUrl);

        try{Thread.sleep(4000);}catch(Exception e){}
        isProgressMonitorCancel = true;
 
        MDIInternalFrame viewFrame = renderView( sequenceName, planSequence, viewListener);

        if (viewName.equals( ViewConstants.SEQUENCE_STEPS_VIEW)) {
          PlanWorks.getPlanWorks().setSequenceStepsViewFrame( seqUrl, viewFrame);
        }

      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
        rnfExcep.printStackTrace();
        ViewGenerics.resetRedrawCursor( PlanWorks.getPlanWorks());
      }

      PlanWorks.getPlanWorks().projectMenu.setEnabled( true);
      dynamicMenuBar.enableMenu( planSeqMenu);
    }
  } // end createSequenceView


} // end class CreateSequenceViewThread
