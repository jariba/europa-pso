// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AddSequenceThread.java,v 1.16 2004-09-10 20:02:30 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 29sep03
//

package gov.nasa.arc.planworks;

import java.io.File;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;


/**
 * <code>AddSequenceThread</code> - handles PlanWorks add sequence action
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class AddSequenceThread extends ThreadWithProgressMonitor {

  private boolean doProgMonitor;

  /**
   * <code>AddSequenceThread</code> - constructor 
   *
   */
  public AddSequenceThread( ThreadListener threadListener) {
    boolean doProgMonitor = true;
    if (System.getProperty("ant.target.test").equals( "true")) {
      doProgMonitor = false;
    }
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
    PlanWorks.getPlanWorks().projectMenu.setEnabled(false);
    addSequence();
    PlanWorks.getPlanWorks().projectMenu.setEnabled( true);
    PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, true);
    dynamicMenuBar.enableMenu( planSeqMenu);
    handleEvent( ThreadListener.EVT_THREAD_ENDED);
  } // end run


  private void addSequence() {
    boolean isSequenceAdded = false;
    while (! isSequenceAdded) {
      try {
        File workingDir = new File( ConfigureAndPlugins.getProjectConfigValue
                                    ( ConfigureAndPlugins.PROJECT_WORKING_DIR,
                                      PlanWorks.getPlanWorks().getCurrentProjectName()));
        List selectedAndInvalidUrls =
          PlanWorks.getPlanWorks().askSequenceDirectory( workingDir);
        List selectedSequenceUrls = (List) selectedAndInvalidUrls.get( 0);
        List invalidSequenceUrls = (List) selectedAndInvalidUrls.get( 1);

        ViewGenerics.setRedrawCursor( PlanWorks.getPlanWorks());

        if (doProgMonitor) {
          progressMonitorThread( "Adding sequence(s) ...", 0, 6);
          if (! progressMonitorWait()) {
            return;
          }
          progressMonitor.setProgress( 3 * ViewConstants.MONITOR_MIN_MAX_SCALING);
        }
        PwProject project = PlanWorks.getPlanWorks().currentProject;
        isSequenceAdded = true;
        PlanWorks.getPlanWorks().addPlanningSequences( project, selectedSequenceUrls,
                                                       invalidSequenceUrls);
        //System.err.println( "Adding sequence " + sequenceDirectory);
        PwProject currentProject = PlanWorks.getPlanWorks().currentProject;
        MDIDynamicMenuBar dynamicMenuBar =
          (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
        int numProjects = PwProject.listProjects().size();
        JMenu planSeqMenu = dynamicMenuBar.clearMenu( PlanWorks.PLANSEQ_MENU, numProjects);
        PlanWorks.getPlanWorks().addPlanSeqViewMenu( currentProject, planSeqMenu);

        if (doProgMonitor) {
          isProgressMonitorCancel = true;
        }
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
      } finally {
        ViewGenerics.resetRedrawCursor( PlanWorks.getPlanWorks());
        if (doProgMonitor) {
          isProgressMonitorCancel = true;
        }
      }
    }
  } // end addSequence


} // end class AddSequenceThread

