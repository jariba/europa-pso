// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DeleteProjectThread.java,v 1.1 2003-09-30 19:18:54 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 29sep03
//

package gov.nasa.arc.planworks;

import java.util.List;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>DeleteProjectThread</code> - handles PlanWorks delete project action
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class DeleteProjectThread extends Thread {

  /**
   * <code>DeleteProjectThread</code> - constructor 
   *
   */
  public DeleteProjectThread() {
  }  // end constructor

  /**
   * <code>run</code>
   *
   */
  public void run() {
    deleteProject();
  } //end run


  private void deleteProject() {
    List projectNames = PwProject.listProjects();
    Object[] options = new Object[projectNames.size()];
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
    for (int i = 0, n = projectNames.size(); i < n; i++) {
      options[i] = (String) projectNames.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( PlanWorks.planWorks, "", "Delete Project", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          String projectName = (String) projectNames.get( i);
          System.out.println( "Delete Project: " + projectName);
          try {

            PwProject.getProject( projectName).delete();

            if ((! PlanWorks.planWorks.currentProjectName.equals( "")) &&
                PlanWorks.planWorks.currentProjectName.equals( projectName)) {
              PlanWorks.planWorks.viewManager.clearViewSets();
              PlanWorks.planWorks.setTitle( PlanWorks.planWorks.name);
              int numProjects = PwProject.listProjects().size();
              dynamicMenuBar.clearMenu( PlanWorks.PLANSEQ_MENU, numProjects);
              dynamicMenuBar.clearMenu( PlanWorks.SEQSTEPS_MENU, numProjects);
            }
            if (PwProject.listProjects().size() == 0) {
              PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.DELETE_MENU_ITEM, false);
              PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, false);
              PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.ADDSEQ_MENU_ITEM, false);
              PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, false);
            } else if (PlanWorks.planWorks.getProjectsLessCurrent().size() == 0) {
              PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, false);
            } else {
              PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, true);
            }
          } catch (ResourceNotFoundException rnfExcep) {
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
            rnfExcep.printStackTrace();
          } catch (Exception excep) {
            excep.printStackTrace();
            System.err.println( " delete: excep " + excep);
            int index = excep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.planWorks, excep.getMessage().substring( index + 1),
               "Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( excep);
            excep.printStackTrace();
          }
          break;
        }
      }
    }
    // JOptionPane.showInputDialog returns null if user selected "cancel"
  } // end deleteProject

} // end class DeleteProjectThread
