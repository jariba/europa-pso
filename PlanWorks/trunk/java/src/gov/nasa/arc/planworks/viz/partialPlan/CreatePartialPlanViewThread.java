// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: CreatePartialPlanViewThread.java,v 1.7 2004-05-08 01:44:14 taylor Exp $
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
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.ViewListener;


/**
 * <code>CreatePartialPlanViewThread</code> - handles PlanWorks partial plan view actions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class CreatePartialPlanViewThread extends CreateViewThread {

  private String partialPlanName;
  private PwPartialPlan partialPlan;
  private ViewListener viewListener;

  /**
   * <code>CreatePartialPlanViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>PlanWorks.SeqPartPlanViewMenuItem</code> - 
   */
  public CreatePartialPlanViewThread( String viewName,
                                      PartialPlanViewMenuItem menuItem) {
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
  public void run() {
    try {
      SwingUtilities.invokeAndWait( new Runnable() {
          public void run() {
            createPartialPlanView();
          }
        });
    } catch (InterruptedException ie) {
      System.err.println( "CreatePartialPlanViewThread: InterruptedException");
      ie.printStackTrace();
    } catch (InvocationTargetException ite) {
      System.err.println( "CreatePartialPlanViewThread: InvocationTargetException");
      ite.printStackTrace();
    }
  } // end run

  private void createPartialPlanView() { 
    PlanWorks.getPlanWorks().setViewRenderingStartTime( System.currentTimeMillis());
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.getProjectMenu().setEnabled( false);

    try {
      PwPlanningSequence planSequence =
        PlanWorks.getPlanWorks().getCurrentProject().getPlanningSequence( seqUrl);
        
      PwPartialPlan partialPlan = planSequence.getPartialPlan(partialPlanName);
        
      renderView( sequenceName + System.getProperty("file.separator") + partialPlanName,
                  partialPlan, viewListener);

    } catch (ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      // rnfExcep.printStackTrace();
    }

    PlanWorks.getPlanWorks().getProjectMenu().setEnabled( true);
    dynamicMenuBar.enableMenu( planSeqMenu);
  } // end createPartialPlanView





} // end class CreatePartialPlanViewThread
