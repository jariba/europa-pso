// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: InstantiateProjectThread.java,v 1.9 2004-02-03 20:43:43 taylor Exp $
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
import gov.nasa.arc.planworks.util.ProjectNameDialog;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;


/**
 * <code>InstantiateProjectThread</code> - handles PlanWorks create & open project actions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class InstantiateProjectThread extends Thread {

  private String type;

  /**
   * <code>InstantiateProjectThread</code> - constructor 
   *
   * @param type - <code>String</code> - 
   */
  public InstantiateProjectThread( final String type) {
    this.type = type;
  }  // end constructor

  /**
   * <code>run</code>
   *
   */
  public void run() {      
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.projectMenu.setEnabled( false);

    PwProject instantiatedProject = null;
    if (type.equals( PlanWorks.CREATE)) {
      instantiatedProject = createProject();
    } else if (type.equals( PlanWorks.OPEN)) {
      instantiatedProject = openProject();
    } else {
      System.err.println( "InstantiateProjectThread.run: " + type + " not handled");
      System.exit( -1);
    }
    if (instantiatedProject != null) {
      PlanWorks.getPlanWorks().currentProject = instantiatedProject;
      int numProjects = PwProject.listProjects().size();
      planSeqMenu = dynamicMenuBar.clearMenu( PlanWorks.PLANSEQ_MENU, numProjects);
      PlanWorks.getPlanWorks().addPlanSeqViewMenu( instantiatedProject, planSeqMenu);
      if (PlanWorks.getPlanWorks().currentProject.listPlanningSequences().size() == 0) {
        dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
      }
      // clear the old project's views
      if (PlanWorks.getPlanWorks().viewManager != null) {
        PlanWorks.getPlanWorks().viewManager.clearViewSets();
      }
      PlanWorks.getPlanWorks().viewManager = new ViewManager( PlanWorks.getPlanWorks());
    }

    PlanWorks.getPlanWorks().projectMenu.setEnabled( true);
    dynamicMenuBar.enableMenu( planSeqMenu);
  } //end run


  // projects can have 0 sequences: create project, even if selected sequences
  // are invalid
  private PwProject createProject() {
    boolean isProjectCreated = false;
    PwProject project = null;
    while (! isProjectCreated) {
      ProjectNameDialog projectNameDialog = new ProjectNameDialog( PlanWorks.getPlanWorks());
      String inputName = projectNameDialog.getTypedText();
      if ((inputName == null) || (inputName.equals( ""))) {
        return null;
      }
      try {
        if (PwProject.listProjects().indexOf( inputName) >= 0) {
          throw new DuplicateNameException( "A project named '" + inputName +
                                            "' already exists.");
        }
        List invalidSequenceDirs = null;
        while (true) {
          invalidSequenceDirs = new ArrayList();
          // ask user for a single sequence directory of partialPlan directories
          int returnVal =
            PlanWorks.getPlanWorks().sequenceDirChooser.showDialog( PlanWorks.getPlanWorks(), "");
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            for (int i = 0, n = PlanWorks.getPlanWorks().sequenceDirectories.length; i < n; i++) {
              String sequenceDirectory = PlanWorks.getPlanWorks().sequenceParentDirectory +
                System.getProperty( "file.separator") +
                PlanWorks.getPlanWorks().sequenceDirectories[i].getName();
              String validateMsg = FileUtils.validateSequenceDirectory( sequenceDirectory);
              if (validateMsg != null) {
                JOptionPane.showMessageDialog
                  (PlanWorks.getPlanWorks(), validateMsg, "Invalid Sequence Directory",
                   JOptionPane.ERROR_MESSAGE);
                invalidSequenceDirs.add( sequenceDirectory);
              }
            }
            if (invalidSequenceDirs.size() ==
                PlanWorks.getPlanWorks().sequenceDirectories.length) {
              continue; // user must reselect
            } else {
              break; // some sequences are valid
            }
          } else {
            // return null; // exit dialog
            break; // exit dialog with no sequences added - use New Sequence
          }
        } // end while
        project = PwProject.createProject( inputName);
        PlanWorks.getPlanWorks().currentProjectName = inputName;
        isProjectCreated = true;
        //System.err.println( "Create Project: " + currentProjectName);
        PlanWorks.getPlanWorks().setTitle( PlanWorks.getPlanWorksTitle() + " of Project =>  " +
                                      PlanWorks.getPlanWorks().currentProjectName);
        PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.DELETE_MENU_ITEM, true);
        PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.ADDSEQ_MENU_ITEM, true);
        PlanWorks.getPlanWorks().setProjectMenuEnabled(PlanWorks.NEWSEQ_MENU_ITEM, true);
        if (PwProject.listProjects().size() > 1) {
          PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, true);
          PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, true);
        }
        if (PlanWorks.getPlanWorks().sequenceDirectories != null) {
          for (int i = 0, n = PlanWorks.getPlanWorks().sequenceDirectories.length; i < n; i++) {
            String sequenceDirectory = PlanWorks.getPlanWorks().sequenceParentDirectory +
              System.getProperty( "file.separator") +
              PlanWorks.getPlanWorks().sequenceDirectories[i].getName();
            if (invalidSequenceDirs.indexOf( sequenceDirectory) == -1) {
              System.err.println( "project.addPlanningSequence " + sequenceDirectory);
              project.addPlanningSequence( sequenceDirectory);
            }
          }
        }

      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
        // rnfExcep.printStackTrace();
        isProjectCreated = false;
      } catch (DuplicateNameException dupExcep) {
        // duplicate project name or duplicate sequence
        int index = dupExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), dupExcep.getMessage().substring( index + 1),
           "Duplicate Name Exception", JOptionPane.ERROR_MESSAGE);
          System.err.println( dupExcep);
          // dupExcep.printStackTrace();
          isProjectCreated = false; 
      } catch (Exception e) {
        JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), e.getMessage(),
                                      "Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println(e);
        e.printStackTrace();
        isProjectCreated = false;
      }
    }
    return project;
  } // end createProject

  private PwProject openProject() {
    PwProject project = null;
    List projectUrls = PwProject.listProjects();
    // System.err.println( "projectUrls " + projectUrls);
    List namesLessCurrent = PlanWorks.getPlanWorks().getProjectsLessCurrent();
    // System.err.println( "namesLessCurrent " + namesLessCurrent);
    Object[] options = new Object[namesLessCurrent.size()];
    for (int i = 0, n = namesLessCurrent.size(); i < n; i++) {
        options[i] = (String) namesLessCurrent.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( PlanWorks.getPlanWorks(), "", "Open Project", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    // System.err.println( "response " + response);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          String projectName = (String) namesLessCurrent.get( i);
          try {
            project = PwProject.getProject( projectName);
            PlanWorks.getPlanWorks().currentProjectName = projectName;
            //System.err.println( "Open Project: " + currentProjectName);
            PlanWorks.getPlanWorks().setTitle( PlanWorks.getPlanWorksTitle() +
                                               " of Project =>  " +
                                               PlanWorks.getPlanWorks().currentProjectName);
            if (PlanWorks.getPlanWorks().getProjectsLessCurrent().size() == 0) {
              PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, false);
            }
            PlanWorks.getPlanWorks().setProjectMenuEnabled(PlanWorks.NEWSEQ_MENU_ITEM, true);
            PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.ADDSEQ_MENU_ITEM, true);
            PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, true);
          } catch (ResourceNotFoundException rnfExcep) {
            // System.err.println( "Project " + projectName + " not found: " + rnfExcep1);
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
            rnfExcep.printStackTrace();
          }
          break;
        }
      }
    } 
    // JOptionPane.showInputDialog returns null if user selected "cancel"
    return project;
  } // end openProject

} // end class InstantiateProjectThread
