// 
// $Id: CreateSequenceViewThread.java,v 1.4 2003-11-13 23:21:16 taylor Exp $
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

import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>CreateSequenceViewThread</code> - handles PlanWorks sequence view actions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class CreateSequenceViewThread extends CreateViewThread {

  private PwPlanningSequence planSequence;
  private boolean isInvokeAndWait;

  /**
   * <code>CreateSequenceViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>JMenuItem</code> - 
   */
  public CreateSequenceViewThread( String viewName,
                                   SequenceViewMenuItem menuItem) {
    super( viewName);
    this.seqUrl = menuItem.getSeqUrl();
    this.sequenceName = menuItem.getSequenceName();
    this.isInvokeAndWait = false;
  }

  /**
   * <code>CreateSequenceViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>JMenuItem</code> - 
   */
  public CreateSequenceViewThread( String viewName,
                                   SequenceViewMenuItem menuItem,
                                   boolean isInvokeAndWait) {
    super( viewName);
    this.seqUrl = menuItem.getSeqUrl();
    this.sequenceName = menuItem.getSequenceName();
    this.isInvokeAndWait = isInvokeAndWait;
  }

  /**
   * <code>run</code>
   *
   */
  public void run() {
    if (isInvokeAndWait) {
      // needed by SequenceQueryWindow.ensureSequenceStepsViewExists
      try {
        SwingUtilities.invokeAndWait( new Runnable() {
            public void run() {
              createSequenceView();
            }
          });
      } catch (InterruptedException ie) {
        System.err.println( "CreateSequenceViewThread: InterruptedException");
        ie.printStackTrace();
      } catch (InvocationTargetException ite) {
        System.err.println( "CreateSequenceViewThread: InvocationTargetException");
        ite.printStackTrace();
      }
    } else {
      createSequenceView();
    }
  } // run

  private void createSequenceView() {
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.planWorks.projectMenu.setEnabled( false);

    try {
      planSequence = PlanWorks.planWorks.currentProject.getPlanningSequence( seqUrl);

      MDIInternalFrame viewFrame = renderView( sequenceName, planSequence);
      if (viewName.equals( PlanWorks.SEQUENCE_STEPS_VIEW)) {
        PlanWorks.planWorks.sequenceStepsViewMap.put( seqUrl, viewFrame);
      }

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
  } // end createSequenceView


} // end class CreateSequenceViewThread
