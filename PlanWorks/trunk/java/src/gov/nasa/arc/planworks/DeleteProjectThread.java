// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DeleteProjectThread.java,v 1.9 2005-01-21 22:45:09 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 29sep03
//

package gov.nasa.arc.planworks;

import java.util.List;
import javax.swing.JMenu;
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
public class DeleteProjectThread extends ThreadWithProgressMonitor {

  /**
   * <code>DeleteProjectThread</code> - constructor 
   *
   */
  public DeleteProjectThread( ThreadListener threadListener) {
    if (threadListener != null) {
      addThreadListener( threadListener);
    }
  }  // end constructor

  /**
   * <code>run</code>
   *
   */
  public void run() {
    handleEvent( ThreadListener.EVT_THREAD_BEGUN);
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.projectMenu.setEnabled( false);

    deleteProject();

    PlanWorks.getPlanWorks().projectMenu.setEnabled( true);
    dynamicMenuBar.enableMenu( planSeqMenu);
    handleEvent( ThreadListener.EVT_THREAD_ENDED);
  } //end run


  private void deleteProject() {
    List projectNames = PwProject.listProjects();
    Object[] options = new Object[projectNames.size()];
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
    for (int i = 0, n = projectNames.size(); i < n; i++) {
      options[i] = (String) projectNames.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( PlanWorks.getPlanWorks(), "", "Delete Project", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          String projectName = (String) projectNames.get( i);
          System.err.println( "Delete Project: " + projectName);
          try {

            PwProject.getProject( projectName).delete();
            PlanWorks.PROJECT_CONFIG_MAP.remove( projectName);
            ConfigureAndPlugins.writeProjectConfigMap();

            if ((! PlanWorks.getPlanWorks().currentProjectName.equals( "")) &&
                PlanWorks.getPlanWorks().currentProjectName.equals( projectName)) {
              PlanWorks.getPlanWorks().viewManager.clearViewSets();
              PlanWorks.getPlanWorks().setTitle( PlanWorks.getPlanWorksTitle());
              int numProjects = PwProject.listProjects().size();
              dynamicMenuBar.clearMenu( PlanWorks.PLANSEQ_MENU, numProjects);
            }
            PlanWorks planWorks = PlanWorks.getPlanWorks();
            if (PwProject.listProjects().size() == 0) {
              planWorks.setProjectMenuEnabled( PlanWorks.DELETE_MENU_ITEM, false);
              planWorks.setProjectMenuEnabled( PlanWorks.CONFIGURE_MENU_ITEM, false);
              planWorks.setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, false);
              planWorks.setProjectMenuEnabled( PlanWorks.ADDSEQ_MENU_ITEM, false);
              planWorks.setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, false);
              planWorks.setProjectMenuEnabled(PlanWorks.NEWSEQ_MENU_ITEM, false);
            } else if (planWorks.getProjectsLessCurrent().size() == 0) {
              planWorks.setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, false);
            } else {
              planWorks.setProjectMenuEnabled( PlanWorks.OPEN_MENU_ITEM, true);
            }
          } catch (ResourceNotFoundException rnfExcep) {
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
            rnfExcep.printStackTrace();
          } catch (Exception excep) {
            excep.printStackTrace();
            System.err.println( " delete: excep " + excep);
            int index = excep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.getPlanWorks(), excep.getMessage().substring( index + 1),
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
