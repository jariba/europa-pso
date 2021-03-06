// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AddSequenceThread.java,v 1.5 2003-12-03 01:48:38 miatauro Exp $
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
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.projectMenu.setEnabled(false);

    addSequence();

    PlanWorks.planWorks.projectMenu.setEnabled( true);
    PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, true);
    dynamicMenuBar.enableMenu( planSeqMenu);
  } // end run


  private void addSequence() {
    boolean isSequenceAdded = false;
    while (! isSequenceAdded) {
      List invalidSequenceDirs = null;
      while (true) {
        invalidSequenceDirs = new ArrayList();
        // ask user for one or more sequence directories of partialPlan directories
        int returnVal =
          PlanWorks.planWorks.sequenceDirChooser.showDialog( PlanWorks.planWorks, "");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          for (int i = 0, n = PlanWorks.planWorks.sequenceDirectories.length; i < n; i++) {
            String sequenceDirectory = PlanWorks.planWorks.sequenceParentDirectory +
              System.getProperty( "file.separator") +
              PlanWorks.planWorks.sequenceDirectories[i].getName();
            String validateMsg = FileUtils.validateSequenceDirectory( sequenceDirectory);
            if (validateMsg != null) {
              JOptionPane.showMessageDialog
                (PlanWorks.planWorks, validateMsg, "Invalid Sequence Directory",
                 JOptionPane.ERROR_MESSAGE);
              invalidSequenceDirs.add( sequenceDirectory);
            }
          }
          if (invalidSequenceDirs.size() == PlanWorks.planWorks.sequenceDirectories.length) {
            continue; // user must reselect
          } else {
            break; // some sequences are valid
          }
        } else {
          return; // exit dialog
        }
      } // end while

      for (int i = 0, n = PlanWorks.planWorks.sequenceDirectories.length; i < n; i++) {
        String sequenceDirectory = PlanWorks.planWorks.sequenceParentDirectory +
          System.getProperty( "file.separator") +
          PlanWorks.planWorks.sequenceDirectories[i].getName();
        if (invalidSequenceDirs.indexOf( sequenceDirectory) == -1) {
          try {
            PlanWorks.planWorks.currentProject.addPlanningSequence( sequenceDirectory);
            System.err.println( "project.addPlanningSequence " + sequenceDirectory);
            isSequenceAdded = true;
          }
          catch (DuplicateNameException dupExcep) {
            int index = dupExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.planWorks, dupExcep.getMessage().substring( index + 1),
               "Duplicate Name Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( dupExcep);
            // dupExcep.printStackTrace();
          } 
          catch (ResourceNotFoundException rnfExcep) {
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
            rnfExcep.printStackTrace();
          }
        }
      }
      //System.err.println( "Adding sequence " + sequenceDirectory);
      MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
      int numProjects = PwProject.listProjects().size();
      JMenu planSeqMenu = dynamicMenuBar.clearMenu( PlanWorks.PLANSEQ_MENU, numProjects);
      PlanWorks.planWorks.addPlanSeqViewMenu
        ( PlanWorks.planWorks.currentProject, planSeqMenu);
    }
  } // end addSequence


} // end class AddSequenceThread

