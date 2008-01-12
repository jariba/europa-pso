// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NewSequenceThread.java,v 1.22 2006-08-18 20:42:27 javier Exp $
//
package gov.nasa.arc.planworks;

import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ConfigureNewSequenceDialog;
import gov.nasa.arc.planworks.util.DebugConsole;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.PlannerController;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.TransactionTypesDialog;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>NewSequenceThread</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                            NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class NewSequenceThread extends ThreadWithProgressMonitor {

  private PwProject currentProject;
  private String projectName;
  private String workingDir;
  private String plannerPath;
  private String modelName;
  private String modelPath;
  private String modelInitStatePath;
  private String modelOutputDestDir;
  private String plannerConfigPath;
    private String heuristicsPath;
    private String sourcePaths;
  private boolean areConfigParamsChanged;


  /**
   * <code>NewSequenceThread</code> - constructor 
   *
   * @param threadListener - <code>ThreadListener</code> - 
   */
  public NewSequenceThread( ThreadListener threadListener) {
    if (threadListener != null) {
      addThreadListener( threadListener);
    }
    areConfigParamsChanged = false;
  } // end constructor 

  /**
   * <code>run</code>
   *
   */
  public void run() {
    try {
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
    } catch (Exception excp) {
      /*
       * We can arrive here when an exception occured durring model loading, 
       * or initializing planner 
       * In both cases, tell the planner to cleanup and unload model library if necessary  
       */
      JOptionPane.showMessageDialog
        ( PlanWorks.getPlanWorks(), "An exception occured while initializing the planner.",
          "Planner Exception", JOptionPane.INFORMATION_MESSAGE);
      System.err.println(excp.getMessage());
      excp.printStackTrace();
      PlannerControlJNI.terminatePlannerRun();
    } finally {
      PlanWorks.getPlanWorks().projectMenu.setEnabled( true);
      PlanWorks.getPlanWorks().setProjectMenuEnabled(PlanWorks.DELSEQ_MENU_ITEM, true);
      // dynamicMenuBar.enableMenu( planSeqMenu);
      handleEvent( ThreadListener.EVT_THREAD_ENDED);
    }
  } // end run

  private String getNewSequenceUrl() throws Exception {
      System.err.println("Starting getNewSequenceUrl");
    String plannerControlJNIFile;
    if (PlanWorks.isMacOSX()) {
      plannerControlJNIFile = ConfigureAndPlugins.MACOSX_PLANNER_CONTROL_JNI_LIB;
    } else {
      plannerControlJNIFile = ConfigureAndPlugins.PLANNER_CONTROL_JNI_LIB;
    }
    String plannerControlJNIPath = System.getProperty( "integration.home") +
      System.getProperty( "file.separator") + plannerControlJNIFile;
    if (! currentProject.getJNIAdapterLoaded()) {
      try {
        System.load( plannerControlJNIPath);
        currentProject.setJNIAdapterLoaded( true);
        System.err.println( "Loading: " + plannerControlJNIPath);
      } catch (UnsatisfiedLinkError err) {
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), err.getMessage() +
           "\n                 Have you done  'ant createJNI'  ?",
           "PlannerControlJNI Loading Error",
           JOptionPane.ERROR_MESSAGE);
        return null;
      }
    }

    if (! getConfigureParameters()) {
      return null;
    }
    
    //ensure that the destination path exists
    File outputDir = new File(modelOutputDestDir);
    if(!outputDir.exists())
	outputDir.mkdirs();

    if (PlannerControlJNI.initPlannerRun( plannerPath, modelPath, modelInitStatePath,
                                          modelOutputDestDir, plannerConfigPath, modelOutputDestDir + System.getProperty("file.separator") + "DEBUG_FILE",
					  sourcePaths.split(":")) !=
        PlannerControlJNI.PLANNER_IN_PROGRESS) {
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), "PlannerControlJNI.initPlannerRun failed",
         "Planner Initialization Exception", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    String seqUrl = PlannerControlJNI.getDestinationPath();
    System.err.println("from getNewSequenceUrl, returning '" + seqUrl + "'");
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
      if (areConfigParamsChanged) {
        setConfigureParameters( currentProject);
      }
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

      MDIInternalFrame debugConsoleFrame =
	  PlanWorks.getPlanWorks().createFrame(ViewConstants.DEBUG_CONSOLE_TITLE + " for " +
					       seqViewMenuItem.getSequenceName(),
					       viewSet, true, true, false, false);
      Container debugContentPane = debugConsoleFrame.getContentPane();
      debugContentPane.add(new DebugConsole(modelOutputDestDir + System.getProperty("file.separator") + "DEBUG_FILE"));
      debugConsoleFrame.pack();
      viewSet.getViews().put(((SequenceViewSet)viewSet).getDebugConsoleViewSetKey(),
			     debugConsoleFrame);

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
    areConfigParamsChanged = false;
    String plannerPathCurrent = ConfigureAndPlugins.getProjectConfigValue
      ( ConfigureAndPlugins.PROJECT_PLANNER_PATH, projectName);
    String modelPathCurrent = ConfigureAndPlugins.getProjectConfigValue
      ( ConfigureAndPlugins.PROJECT_MODEL_PATH, projectName);
    String modelInitStatePathCurrent = ConfigureAndPlugins.getProjectConfigValue
      ( ConfigureAndPlugins.PROJECT_MODEL_INIT_STATE_PATH, projectName);
    String modelOutputDestDirCurrent = ConfigureAndPlugins.getProjectConfigValue
      ( ConfigureAndPlugins.PROJECT_MODEL_OUTPUT_DEST_DIR, projectName);
    String plannerConfigPathCurrent = 
      ConfigureAndPlugins.getProjectConfigValue(ConfigureAndPlugins.PROJECT_PLANNER_CONFIG_PATH,
                                                projectName);
    String heuristicsPathCurrent = ConfigureAndPlugins.getProjectConfigValue
	(ConfigureAndPlugins.PROJECT_HEURISTICS_PATH, projectName);

    String sourcePathsCurrent = 
	ConfigureAndPlugins.getProjectConfigValue(ConfigureAndPlugins.PROJECT_SOURCE_PATH, projectName);

    ConfigureNewSequenceDialog configureDialog =
      new ConfigureNewSequenceDialog( PlanWorks.getPlanWorks());
    if (configureDialog.getModelPath() == null) {
      // user chose cancel
      return false;
    }

    workingDir = ConfigureAndPlugins.getProjectConfigValue
      ( ConfigureAndPlugins.PROJECT_WORKING_DIR, projectName);
    plannerPath = configureDialog.getPlannerPath();
//     modelName = configureDialog.getModelName();
    modelPath = configureDialog.getModelPath();
    modelInitStatePath = configureDialog.getModelInitStatePath();
    modelOutputDestDir = configureDialog.getModelOutputDestDir();
    plannerConfigPath = configureDialog.getPlannerConfigPath();
    heuristicsPath = configureDialog.getHeuristicsPath();
    sourcePaths = configureDialog.getSourcePaths();

    if ((! plannerPath.equals(  plannerPathCurrent)) ||
        (! modelPath.equals( modelPathCurrent)) ||
        (! modelInitStatePath.equals( modelInitStatePathCurrent)) ||
        (! modelOutputDestDir.equals( modelOutputDestDirCurrent)) ||
        ! plannerConfigPath.equals(plannerConfigPathCurrent) ||
	! heuristicsPath.equals(heuristicsPathCurrent) ||
	! sourcePaths.equals(sourcePathsCurrent)) {
      areConfigParamsChanged = true;
    }
    return true;
  } // end getConfigureParameters

  private void setConfigureParameters( PwProject currentProject) {
    List nameValueList = new ArrayList();
    nameValueList.add( ConfigureAndPlugins.PROJECT_WORKING_DIR);
    nameValueList.add( workingDir);
    nameValueList.add( ConfigureAndPlugins.PROJECT_PLANNER_PATH);
    nameValueList.add( plannerPath);
//     nameValueList.add( ConfigureAndPlugins.PROJECT_MODEL_NAME);
//     nameValueList.add( modelName);
    nameValueList.add( ConfigureAndPlugins.PROJECT_MODEL_PATH);
    nameValueList.add( modelPath);
    nameValueList.add( ConfigureAndPlugins.PROJECT_MODEL_INIT_STATE_PATH);
    nameValueList.add( modelInitStatePath);
    nameValueList.add( ConfigureAndPlugins.PROJECT_MODEL_OUTPUT_DEST_DIR);
    nameValueList.add( modelOutputDestDir);
    nameValueList.add(ConfigureAndPlugins.PROJECT_HEURISTICS_PATH);
    nameValueList.add(heuristicsPath);
    nameValueList.add(ConfigureAndPlugins.PROJECT_SOURCE_PATH);
    nameValueList.add(sourcePaths);
    ConfigureAndPlugins.updateProjectConfigMap( currentProject.getName(), nameValueList);
  } // end setConfigureParameters

} // end class NewSequenceThread
