// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AddSequenceThread.java,v 1.8 2004-04-09 23:11:23 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 29sep03
//

package gov.nasa.arc.planworks;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>AddSequenceThread</code> - handles PlanWorks add sequence action
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class AddSequenceThread extends Thread {

  /**
   * <code>AddSequenceThread</code> - constructor 
   *
   */
  public AddSequenceThread() {
  }  // end constructor
    
  /**
   * <code>run</code>
   *
   */
  public void run() {
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.projectMenu.setEnabled(false);
    addSequence();
    PlanWorks.getPlanWorks().projectMenu.setEnabled( true);
    PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, true);
    dynamicMenuBar.enableMenu( planSeqMenu);
  } // end run


  private void addSequence() {
    boolean isSequenceAdded = false;
    while (! isSequenceAdded) {
      try {
        List selectedAndInvalidUrls = PlanWorks.getPlanWorks().askSequenceDirectory();
        List selectedSequenceUrls = (List) selectedAndInvalidUrls.get( 0);
        List invalidSequenceUrls = (List) selectedAndInvalidUrls.get( 1);

        PwProject project = PlanWorks.getPlanWorks().currentProject;
        isSequenceAdded = true;
        PlanWorks.getPlanWorks().addPlanningSequences( project, selectedSequenceUrls,
                                                       invalidSequenceUrls);
        //System.err.println( "Adding sequence " + sequenceDirectory);
        MDIDynamicMenuBar dynamicMenuBar =
          (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
        int numProjects = PwProject.listProjects().size();
        JMenu planSeqMenu = dynamicMenuBar.clearMenu( PlanWorks.PLANSEQ_MENU, numProjects);
        PlanWorks.getPlanWorks().addPlanSeqViewMenu
          ( PlanWorks.getPlanWorks().currentProject, planSeqMenu);

      }
      catch (DuplicateNameException dupExcep) {
        int index = dupExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), dupExcep.getMessage().substring( index + 1),
           "Duplicate Name Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( dupExcep);
        // dupExcep.printStackTrace();
        isSequenceAdded = false;
      } 
      catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
        rnfExcep.printStackTrace();
        isSequenceAdded = false;
      }
      catch (Exception e) {
        JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), e.getMessage(),
                                      "Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println(e);
        e.printStackTrace();
        isSequenceAdded = false;
      }
    }
  } // end addSequence


} // end class AddSequenceThread

