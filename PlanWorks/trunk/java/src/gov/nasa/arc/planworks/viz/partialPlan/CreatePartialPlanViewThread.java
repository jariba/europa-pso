// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: CreatePartialPlanViewThread.java,v 1.3 2003-11-13 23:21:17 taylor Exp $
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
  private boolean isInvokeAndWait;

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
    this.isInvokeAndWait = false;
  }  // end constructor

  /**
   * <code>CreatePartialPlanViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>PartialPlanViewMenuItem</code> - 
   * @param isInvokeAndWait - <code>boolean</code> - 
   */
  public CreatePartialPlanViewThread( String viewName,
                                      PartialPlanViewMenuItem menuItem,
                                      boolean isInvokeAndWait) {
    super( viewName);
    this.seqUrl = menuItem.getSeqUrl();
    this.sequenceName = menuItem.getSequenceName();
    this.partialPlanName = menuItem.getPartialPlanName();
    this.isInvokeAndWait = isInvokeAndWait;
  }  // end constructor

  /**
   * <code>run</code>
   *
   */
  public void run() {
    if (isInvokeAndWait) {
      // needed by PartialPlanView.createOpenAllItem
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
    } else {
      createPartialPlanView();
    }
  } // end run

  private void createPartialPlanView() { 
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.projectMenu.setEnabled( false);

    try {
      PwPlanningSequence planSequence =
        PlanWorks.planWorks.currentProject.getPlanningSequence( seqUrl);
        
      PwPartialPlan partialPlan = planSequence.getPartialPlan(partialPlanName);
        
      renderView( sequenceName + System.getProperty("file.separator") + partialPlanName,
                  partialPlan);

    } catch (ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      rnfExcep.printStackTrace();
    }

    PlanWorks.planWorks.projectMenu.setEnabled( true);
    dynamicMenuBar.enableMenu( planSeqMenu);
  } // end createPartialPlanView





} // end class CreatePartialPlanViewThread
