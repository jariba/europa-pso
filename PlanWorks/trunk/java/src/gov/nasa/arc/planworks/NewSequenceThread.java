// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NewSequenceThread.java,v 1.7 2004-09-09 22:45:04 taylor Exp $
//
package gov.nasa.arc.planworks;

import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.PlannerControlJNI;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ConfigureNewSequenceDialog;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.PlannerController;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


public class NewSequenceThread extends ThreadWithProgressMonitor {

  private PwProject currentProject;
  private String projectName;
  private String workingDir;
  private String plannerPath;
  private String modelName;
  private String modelPath;
  private String modelInitStatePath;
  private String modelOutputDestDir;

  public NewSequenceThread( ThreadListener threadListener) {
    if (threadListener != null) {
      addThreadListener( threadListener);
    }
  } // end constructor 

  public void run() {
    handleEvent( ThreadListener.EVT_THREAD_BEGUN);
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
    // JMenu planSeqMenu = dynamicMenuBar.disableMenu(PlanWorks.PLANSEQ_MENU);
    PlanWorks.getPlanWorks().projectMenu.setEnabled( false);

    currentProject = PlanWorks.getPlanWorks().getCurrentProject();
    projectName = currentProject.getName();
    String seqUrl = null;
    if ((seqUrl = getNewSequenceUrl()) != null) {
      newSequence( seqUrl);
    }

    PlanWorks.getPlanWorks().projectMenu.setEnabled( true);
    PlanWorks.getPlanWorks().setProjectMenuEnabled(PlanWorks.DELSEQ_MENU_ITEM, true);
    // dynamicMenuBar.enableMenu( planSeqMenu);
    handleEvent( ThreadListener.EVT_THREAD_ENDED);
  } // end run

  private String getNewSequenceUrl() {
    if (! getConfigureParameters()) {
      return null;
    }

    if (! currentProject.getJNIAdapterLoaded()) {
      if (plannerPath.indexOf( ConfigureAndPlugins.PLANNER_LIB_NAME_MATCH) == -1) {
        JOptionPane.showMessageDialog
          ( PlanWorks.getPlanWorks(),
            "Library name '" +  (new File( plannerPath)).getName() +
            "' does not match 'lib<planner-name>" +
            ConfigureAndPlugins.PLANNER_LIB_NAME_MATCH + "'",
            "Invalid Planner JNI Library", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      System.err.println( "Loading: " + plannerPath);
      System.load( plannerPath);
      currentProject.setJNIAdapterLoaded( true);
    }

    if (PlannerControlJNI.initPlannerRun( modelPath, modelInitStatePath,
                                          modelOutputDestDir) !=
        PlannerControlJNI.PLANNER_IN_PROGRESS) {
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), "PlannerControlJNI.initPlannerRun failed",
         "Planner Initialization Exception", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    String seqUrl = PlannerControlJNI.getDestinationPath();
    // String seqUrl = "/home/wtaylor/PlanWorksProject/sequences/PLASMA/basic-model1091643100999";
    return seqUrl;
  } // end getNewSequenceUrl

  private void newSequence( String seqUrl) {
    // open SeqQuery and SeqSteps views
    try {
      List selectedSequenceUrls = new ArrayList();
      selectedSequenceUrls.add( seqUrl);
      List invalidSequenceUrls = new ArrayList();
      PlanWorks.getPlanWorks().addPlanningSequences( currentProject, selectedSequenceUrls,
                                                     invalidSequenceUrls);
      MDIDynamicMenuBar dynamicMenuBar =
        (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
      int numProjects = PwProject.listProjects().size();
      JMenu planSeqMenu = dynamicMenuBar.clearMenu( PlanWorks.PLANSEQ_MENU, numProjects);
      PlanWorks.getPlanWorks().addPlanSeqViewMenu( currentProject, planSeqMenu);
      SequenceViewMenuItem seqViewMenuItem =
        (SequenceViewMenuItem) dynamicMenuBar.getPlanSeqItem( seqUrl);
      PlanWorks.getPlanWorks().createSequenceViewThread
        ( ViewConstants.SEQUENCE_STEPS_VIEW, seqViewMenuItem);
      setConfigureParameters( currentProject);
      PwPlanningSequence planSequence = currentProject.getPlanningSequence( seqUrl);
      ViewSet viewSet = getViewSetWithWait( planSequence);
      MDIInternalFrame sequenceStepsFrame = getSequenceStepsFrameWithWait( viewSet);

      // open Planner Controller frame
      MDIInternalFrame plannerControllerFrame = 
        PlanWorks.getPlanWorks().createFrame( ViewConstants.PLANNER_CONTROLLER_TITLE +
                                              " for " + seqViewMenuItem.getSequenceName(),
                                              viewSet, true, true, false, false);
      Container contentPane = plannerControllerFrame.getContentPane();
      PlannerController plannerControllerContent =
        new PlannerController( planSequence, plannerControllerFrame, projectName,
                               ViewGenerics.getSequenceStepsView( sequenceStepsFrame));
      contentPane.add( plannerControllerContent);
      plannerControllerFrame.pack();
      viewSet.getViews().put( ((SequenceViewSet) viewSet).getPlanControllerViewSetKey(),
                              plannerControllerFrame);
    } catch (DuplicateNameException dupExcep) {
      int index = dupExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), dupExcep.getMessage().substring( index + 1),
         "Duplicate Name Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( dupExcep);
      return;
    } catch (ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      return;
    }
  } // end newSequence

  private ViewSet getViewSetWithWait( PwPlanningSequence planSequence) {
    ViewManager viewManager = PlanWorks.getPlanWorks().viewManager;
    ViewSet viewSet = null;
    while ((viewSet = viewManager.getViewSet( planSequence)) == null) {
      try {
        Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
      }
      catch (InterruptedException ie) {}
      // System.err.println( "getViewSetWithWait.newSequence: wait for viewSet != null");
    }
    return viewSet;
  } // end getViewSetWithWait

  private MDIInternalFrame getSequenceStepsFrameWithWait( ViewSet viewSet) {
    MDIInternalFrame sequenceStepsFrame = null;
    String sequenceStepsViewClassName =
      (String) PlanWorks.VIEW_CLASS_NAME_MAP.get( ViewConstants.SEQUENCE_STEPS_VIEW);
    while (sequenceStepsFrame == null) {
      try {
        Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
      }
      catch (InterruptedException ie) {}
      try {
        sequenceStepsFrame = viewSet.getView( Class.forName( sequenceStepsViewClassName));
      } catch (ClassNotFoundException excp) {
        excp.printStackTrace();
        System.exit(1);
      }
      // System.err.println( "getSequenceStepsFrameWithWait: wait for sequenceStepsFrame != null");
    }
    int componentCnt = sequenceStepsFrame.getContentPane().getComponentCount();
    while (componentCnt == 0) {
      try {
        Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
      }
      catch (InterruptedException ie) {}
      componentCnt = sequenceStepsFrame.getContentPane().getComponentCount();
      // System.err.println( "getSequenceStepsFrameWithWait: wait for component cnt != 0");
    }
    return sequenceStepsFrame;
  } // end getSequenceStepsFrameWithWait

  private boolean getConfigureParameters() {
    ConfigureNewSequenceDialog configureDialog =
      new ConfigureNewSequenceDialog( PlanWorks.getPlanWorks());
    if (configureDialog.getModelPath() == null) {
      // user chose cancel
      return false;
    }
    workingDir = ConfigureAndPlugins.getProjectConfigValue
      ( ConfigureAndPlugins.PROJECT_WORKING_DIR, projectName);
    plannerPath = ConfigureAndPlugins.getProjectConfigValue
      ( ConfigureAndPlugins.PROJECT_PLANNER_PATH, projectName);
    modelName = configureDialog.getModelName();
    modelPath = configureDialog.getModelPath();
    modelInitStatePath = configureDialog.getModelInitStatePath();
    modelOutputDestDir = configureDialog.getModelOutputDestDir();
    return true;
  } // end getConfigureParameters

  private void setConfigureParameters( PwProject currentProject) {
    List nameValueList = new ArrayList();
    nameValueList.add( ConfigureAndPlugins.PROJECT_WORKING_DIR);
    nameValueList.add( workingDir);
    nameValueList.add( ConfigureAndPlugins.PROJECT_PLANNER_PATH);
    nameValueList.add( plannerPath);
    nameValueList.add( ConfigureAndPlugins.PROJECT_MODEL_NAME);
    nameValueList.add( modelName);
    nameValueList.add( ConfigureAndPlugins.PROJECT_MODEL_PATH);
    nameValueList.add( modelPath);
    nameValueList.add( ConfigureAndPlugins.PROJECT_MODEL_INIT_STATE_PATH);
    nameValueList.add( modelInitStatePath);
    nameValueList.add( ConfigureAndPlugins.PROJECT_MODEL_OUTPUT_DEST_DIR);
    nameValueList.add( modelOutputDestDir);
    ConfigureAndPlugins.updateProjectConfigMap( currentProject.getName(), nameValueList);
  } // end setConfigureParameters



} // end class NewSequenceThread
