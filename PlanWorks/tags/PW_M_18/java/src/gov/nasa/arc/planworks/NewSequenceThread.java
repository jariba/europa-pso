// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NewSequenceThread.java,v 1.15 2004-10-13 17:32:33 taylor Exp $
//
package gov.nasa.arc.planworks;

import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ConfigureNewSequenceDialog;
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
    } finally {
      PlanWorks.getPlanWorks().projectMenu.setEnabled( true);
      PlanWorks.getPlanWorks().setProjectMenuEnabled(PlanWorks.DELSEQ_MENU_ITEM, true);
      // dynamicMenuBar.enableMenu( planSeqMenu);
      handleEvent( ThreadListener.EVT_THREAD_ENDED);
    }
  } // end run

  private String getNewSequenceUrl() throws Exception {
    String plannerControlJNIPath = System.getProperty( "integration.home") +
      System.getProperty( "file.separator") + ConfigureAndPlugins.PLANNER_CONTROL_JNI_LIB;
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

    if (PlannerControlJNI.initPlannerRun( plannerPath, modelPath, modelInitStatePath,
                                          modelOutputDestDir) !=
        PlannerControlJNI.PLANNER_IN_PROGRESS) {
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), "PlannerControlJNI.initPlannerRun failed",
         "Planner Initialization Exception", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    getTransactionTypeStates();

    String seqUrl = PlannerControlJNI.getDestinationPath();
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
    if ((! plannerPath.equals(  plannerPathCurrent)) ||
        (! modelPath.equals( modelPathCurrent)) ||
        (! modelInitStatePath.equals( modelInitStatePathCurrent)) ||
        (! modelOutputDestDir.equals( modelOutputDestDirCurrent))) {
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
    ConfigureAndPlugins.updateProjectConfigMap( currentProject.getName(), nameValueList);
  } // end setConfigureParameters

  private void getTransactionTypeStates() {
    String [] transactionTypes;
    int[] transactionTypeStates; // i = enabled; 0 = disabled
    try {
      transactionTypes = PlannerControlJNI.getTransactionTypes();
//     for (int i = 0, n = transactionTypes.length; i < n; i++) {
//       String transType = (String) transactionTypes[i];
//       System.err.println( "transType i=" + i + " " + transType);
//     }
      transactionTypeStates = PlannerControlJNI.getTransactionTypeStates();
//     for (int i = 0, n = transactionTypeStates.length; i < n; i++) {
//       int transTypeState = (int) transactionTypeStates[i];
//       System.err.println( "transTypeState i=" + i + " " + transTypeState);
//     }
    } catch (UnsatisfiedLinkError ule) {
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), "JNI method '" + ule.getMessage() + "' has not been loaded",
         "Planner Loading Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (transactionTypes.length != transactionTypeStates.length) {
      System.err.println( "ConfigureNewSequenceDialog: num transactionTypes (" +
                          transactionTypes.length + ") is != num transactionTypeStates (" +
                          transactionTypeStates.length + ") from PlannerControlJNI");
      System.exit( -1);
    }
    TransactionTypesDialog transactionTypesDialog =
      new TransactionTypesDialog( PlanWorks.getPlanWorks(), transactionTypes,
                                  transactionTypeStates);
    int[] transStates = transactionTypesDialog.getTransactionTypeStates();
    boolean areTransStatesChanged = false;
    for (int i = 0, n = transactionTypeStates.length; i < n; i++) {
      if (transactionTypeStates[i] != transStates[i]) {
        areTransStatesChanged = true;
        break;
      }
    }
    if (areTransStatesChanged == false) {
      return;
    } else {
      PlannerControlJNI.setTransactionTypeStates( transStates);

    }
  } // end getTransactionTypeStates

} // end class NewSequenceThread
