// 
// $Id: CreateSequenceViewThread.java,v 1.1 2003-10-01 23:53:54 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 30sep03
//

package gov.nasa.arc.planworks;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
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

  /**
   * <code>CreateSequenceViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>JMenuItem</code> - 
   */
  public CreateSequenceViewThread( String viewName,
                                   PlanWorks.SequenceViewMenuItem menuItem) {
    super( viewName);
    this.seqUrl = menuItem.getSeqUrl();
    this.sequenceName = menuItem.getSequenceName();
  }

  /**
   * <code>run</code>
   *
   */
  public void run() {
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.planWorks.projectMenu.setEnabled( false);

    try {
      planSequence = PlanWorks.planWorks.currentProject.getPlanningSequence( seqUrl);

      renderView( sequenceName, planSequence);

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
  } // end run


} // end class CreateSequenceViewThread
