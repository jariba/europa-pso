// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: CreatePartialPlanViewThread.java,v 1.12 2005-11-10 01:22:12 miatauro Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 30sep03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.lang.reflect.InvocationTargetException;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.CreateViewThread;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.util.CreatePartialPlanException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;


/**
 * <code>CreatePartialPlanViewThread</code> - handles PlanWorks partial plan view actions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class CreatePartialPlanViewThread extends CreateViewThread {

  private static Object staticObject = new Object();

  private String partialPlanName;
  private PwPartialPlan partialPlan;
  private ViewListener viewListener;

  /**
   * <code>CreatePartialPlanViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>PlanWorks.SeqPartPlanViewMenuItem</code> - 
   */
  public CreatePartialPlanViewThread( final String viewName,
                                      final PartialPlanViewMenuItem menuItem) {
    super( viewName);
    this.seqUrl = menuItem.getSeqUrl();
    this.sequenceName = menuItem.getSequenceName();
    this.partialPlanName = menuItem.getPartialPlanName();
    this.viewListener = menuItem.getViewListener();
  }  // end constructor

  /**
   * <code>run</code>
   *
   */
  public synchronized final void run() {
//     try {
//       SwingUtilities.invokeAndWait( new Runnable() {
//           public final void run() {
//             createPartialPlanView();
//           }
//         });
//     } catch (InterruptedException ie) {
//       System.err.println( "CreatePartialPlanViewThread: InterruptedException");
//       ie.printStackTrace();
//     } catch (InvocationTargetException ite) {
//       System.err.println( "CreatePartialPlanViewThread: InvocationTargetException");
//       ite.printStackTrace();
//     }
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          createPartialPlanView();
          return null;
        }
    };
    worker.start();  
  } // end run

  private void createPartialPlanView() {
    synchronized( staticObject) {
      PlanWorks.getPlanWorks().setViewRenderingStartTime( System.currentTimeMillis(), viewName);
      MDIDynamicMenuBar dynamicMenuBar =
        (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
      JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
      PlanWorks.getProjectMenu().setEnabled( false);

      try {
        PwPlanningSequence planSequence =
          PlanWorks.getPlanWorks().getCurrentProject().getPlanningSequence( seqUrl);
	PwPartialPlan partialPlan = null;
// 	if (viewName.equals( ViewConstants.DB_TRANSACTION_VIEW) &&
// 	    (! planSequence.doesPartialPlanExist( partialPlanName))) {
// 	  // create dummy partial plan for DBTransactionView
// 	  partialPlan = new PwPartialPlanImpl( partialPlanName, planSequence);
// 	} else {
	  partialPlan = planSequence.getPartialPlan( partialPlanName);
	  //}

        renderView( sequenceName + System.getProperty( "file.separator") + partialPlanName,
                    partialPlan, viewListener);

      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          ( PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
            "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
        // rnfExcep.printStackTrace();
      } catch (CreatePartialPlanException cppExcep) {
        // user clicked Cancel on ProgressMonitor - renderView will not be called
      }

      PlanWorks.getPlanWorks().getProjectMenu().setEnabled( true);
      dynamicMenuBar.enableMenu( planSeqMenu);
    }
  } // end createPartialPlanView

} // end class CreatePartialPlanViewThread
