// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: InstantiateProjectThread.java,v 1.6 2003-12-03 01:48:39 miatauro Exp $
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
  public InstantiateProjectThread( String type) {
    this.type = type;
  }  // end constructor

  /**
   * <code>run</code>
   *
   */
  public void run() {      
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
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
      PlanWorks.planWorks.currentProject = instantiatedProject;
      int numProjects = PwProject.listProjects().size();
      planSeqMenu = dynamicMenuBar.clearMenu( PlanWorks.PLANSEQ_MENU, numProjects);
      PlanWorks.planWorks.addPlanSeqViewMenu( instantiatedProject, planSeqMenu);
      if (PlanWorks.planWorks.currentProject.listPlanningSequences().size() == 0) {
        dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
      }
      // clear the old project's views
      if (PlanWorks.planWorks.viewManager != null) {
        PlanWorks.planWorks.viewManager.clearViewSets();
      }
      PlanWorks.planWorks.viewManager = new ViewManager( PlanWorks.planWorks);
    }

    PlanWorks.planWorks.projectMenu.setEnabled( true);
    dynamicMenuBar.enableMenu( planSeqMenu);
  } //end run


  // projects can have 0 sequences: create project, even if selected sequences
  // are invalid
  private PwProject createProject() {
    boolean isProjectCreated = false;
    PwProject project = null;
    while (! isProjectCreated) {
      ProjectNameDialog projectNameDialog = new ProjectNameDialog( PlanWorks.planWorks);
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
            return null; // exit dialog
          }
        } // end while
        project = PwProject.createProject( inputName);
        PlanWorks.planWorks.currentProjectName = inputName;
        isProjectCreated = true;
        //System.err.println( "Create Project: " + currentProjectName);
        PlanWorks.planWorks.setTitle( PlanWorks.planWorks.name + " of Project =>  " +
                                      PlanWorks.planWorks.currentProjectName);
        PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.DELETE_MENU_ITEM, true);
        PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.ADDSEQ_MENU_ITEM, true);
        PlanWorks.planWorks.setProjectMenuEnabled(PlanWorks.NEWSEQ_MENU_ITEM, true);
        if (PwProject.listProjects().size() > 1) {
          PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, true);
          PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, true);
        }
        for (int i = 0, n = PlanWorks.planWorks.sequenceDirectories.length; i < n; i++) {
          String sequenceDirectory = PlanWorks.planWorks.sequenceParentDirectory +
            System.getProperty( "file.separator") +
            PlanWorks.planWorks.sequenceDirectories[i].getName();
          if (invalidSequenceDirs.indexOf( sequenceDirectory) == -1) {
            System.err.println( "project.addPlanningSequence " + sequenceDirectory);
            project.addPlanningSequence( sequenceDirectory);
          }
        }

      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
        // rnfExcep.printStackTrace();
        isProjectCreated = false;
      } catch (DuplicateNameException dupExcep) {
        // duplicate project name or duplicate sequence
        int index = dupExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.planWorks, dupExcep.getMessage().substring( index + 1),
           "Duplicate Name Exception", JOptionPane.ERROR_MESSAGE);
          System.err.println( dupExcep);
          // dupExcep.printStackTrace();
          isProjectCreated = false; 
      } catch (Exception e) {
        //         int index = e.getMessage().indexOf(":");
        //         JOptionPane.showMessageDialog(PlanWorks.this, e.getMessage().substring(index+1),
        //                                       "Exception", JOptionPane.ERROR_MESSAGE);
        JOptionPane.showMessageDialog(PlanWorks.planWorks, e.getMessage(),
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
    List namesLessCurrent = PlanWorks.planWorks.getProjectsLessCurrent();
    // System.err.println( "namesLessCurrent " + namesLessCurrent);
    Object[] options = new Object[namesLessCurrent.size()];
    for (int i = 0, n = namesLessCurrent.size(); i < n; i++) {
        options[i] = (String) namesLessCurrent.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( PlanWorks.planWorks, "", "Open Project", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    // System.err.println( "response " + response);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          String projectName = (String) namesLessCurrent.get( i);
          try {
            project = PwProject.getProject( projectName);
            PlanWorks.planWorks.currentProjectName = projectName;
            //System.err.println( "Open Project: " + currentProjectName);
            PlanWorks.planWorks.setTitle( PlanWorks.planWorks.name + " of Project =>  " +
                                          PlanWorks.planWorks.currentProjectName);
            if (PlanWorks.planWorks.getProjectsLessCurrent().size() == 0) {
              PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, false);
            }
            PlanWorks.planWorks.setProjectMenuEnabled(PlanWorks.NEWSEQ_MENU_ITEM, true);
            PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.ADDSEQ_MENU_ITEM, true);
            PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, true);
          } catch (ResourceNotFoundException rnfExcep) {
            // System.err.println( "Project " + projectName + " not found: " + rnfExcep1);
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
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
