//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest.java,v 1.17 2004-07-15 21:24:46 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoStroke;
// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.Overview;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.JFCEventManager;
import junit.extensions.jfcunit.eventdata.JMenuMouseEventData;
import junit.extensions.jfcunit.eventdata.JTableHeaderMouseEventData;
import junit.extensions.jfcunit.eventdata.KeyEventData;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.eventdata.StringEventData;
import junit.extensions.jfcunit.finder.ComponentFinder;
import junit.extensions.jfcunit.finder.Finder;
import junit.framework.AssertionFailedError;
import junit.framework.TestSuite; 
import junit.textui.TestRunner;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.db.PwDecision;
import gov.nasa.arc.planworks.db.PwPartialPlan;    
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.mdi.MDIDesktopPane;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkObjectNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkResourceNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkRuleInstanceNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;    
import gov.nasa.arc.planworks.viz.partialPlan.decision.DecisionView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.rule.RuleInstanceView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNode;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNodeDurationBridge;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNodeTimeMark;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.SlotNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineViewTimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.StepQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.DBTransactionQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.StepElement;
import gov.nasa.arc.planworks.viz.util.DBTransactionTable;
import gov.nasa.arc.planworks.viz.util.DBTransactionTableModel;
import gov.nasa.arc.planworks.viz.util.TableSorter;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecWindow;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.GroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.LogicComboBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.MergeBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.NegationCheckBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.PredicateBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.PredicateGroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TimeIntervalBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TimeIntervalGroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TimelineBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TimelineGroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TokenTypeBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.UniqueKeyBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.UniqueKeyGroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;

/**
 * <code>PlanWorksGUITest</code> - PlanWorks/testCases/planViz.txt contains the script
 *                                 for this test suite 
 * calling JFCTest methods out of listeners invoked from the PlanWorks thread
 * results in IllegalMonitorStateException
 *   "This happens if the test case methods which synchronize the AWTEvent queue 
 *    are called from outside the TestCase thread."  Thus we set wait flags only.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                   NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PlanWorksGUITest extends JFCTestCase implements IdSource {
  
  // there must be a listener for every partial plan view - with this view ordering
  // PlanWorks.PARTIAL_PLAN_VIEW_LIST.size()

  private static final int CONSTRAINT_NETWORK_VIEW_INDEX =
    PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.CONSTRAINT_NETWORK_VIEW);
  private static final int DB_TRANSACTION_VIEW_INDEX =
    PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.DB_TRANSACTION_VIEW);
  private static final int DECISION_VIEW_INDEX =
    PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.DECISION_VIEW);
  private static final int RESOURCE_PROFILE_VIEW_INDEX =
    PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.RESOURCE_PROFILE_VIEW);
  private static final int RESOURCE_TRANSACTION_VIEW_INDEX =
    PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.RESOURCE_TRANSACTION_VIEW);
  private static final int TEMPORAL_EXTENT_VIEW_INDEX =
    PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.TEMPORAL_EXTENT_VIEW);
  private static final int TIMELINE_VIEW_INDEX =
    PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.TIMELINE_VIEW);
  private static final int TOKEN_NETWORK_VIEW_INDEX =
    PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.TOKEN_NETWORK_VIEW);

  private PlanWorks planWorks;
  private JFCTestHelper helper;

  private List sequenceUrls; // element String
  private Point popUpLocation;
  private int entityIdInt;

  private boolean viewListenerWait01;
  private boolean viewListenerWait02;
  private boolean viewListenerWait03;
  private boolean viewListenerWait04;
  private boolean viewListenerWait05;
  private boolean viewListenerWait06;
  private boolean viewListenerWait07;
  private boolean viewListenerWait08;

  public PlanWorksGUITest(String test) {
    super(test);
  }

  // implements IdSource
  public int incEntityIdInt() {
    return ++entityIdInt;
  }
  
  // implements IdSource
  public void resetEntityIdInt() {
    entityIdInt = 0;
  }

  public boolean getViewListenerWait01() {
    return viewListenerWait01;
  }

  public void setViewListenerWait01( boolean value) {
    viewListenerWait01 = value;
  }

  public boolean getViewListenerWait02() {
    return viewListenerWait02;
  }

  public void setViewListenerWait02( boolean value) {
    viewListenerWait02 = value;
  }

  public boolean getViewListenerWait03() {
    return viewListenerWait03;
  }

  public void setViewListenerWait03( boolean value) {
    viewListenerWait03 = value;
  }

  public boolean getViewListenerWait04() {
    return viewListenerWait04;
  }

  public void setViewListenerWait04( boolean value) {
    viewListenerWait04 = value;
  }

  public boolean getViewListenerWait05() {
    return viewListenerWait05;
  }

  public void setViewListenerWait05( boolean value) {
    viewListenerWait05 = value;
  }

  public boolean getViewListenerWait06() {
    return viewListenerWait06;
  }

  public void setViewListenerWait06( boolean value) {
    viewListenerWait06 = value;
  }

  public boolean getViewListenerWait07() {
    return viewListenerWait07;
  }

  public void setViewListenerWait07( boolean value) {
    viewListenerWait07 = value;
  }

  public boolean getViewListenerWait08() {
    return viewListenerWait08;
  }

  public void setViewListenerWait08( boolean value) {
    viewListenerWait08 = value;
  }


  public void setUp() throws Exception {
    try {
      helper = new JFCTestHelper();
      this.setHelper( helper);

      // assertNotNull( "before planworks ", null);

      planWorks = new PlanWorks( PlanWorks.buildConstantMenus(),
                                 System.getProperty( "name.application"),
                                 System.getProperty( "boolean.isMaxTestScreen"),
                                 System.getProperty( "os.type"),
                                 System.getProperty( "planworks.root"));
      PlanWorks.setPlanWorks( planWorks);
      popUpLocation = new Point( (int) (PlanWorks.getPlanWorks().getWidth() / 2),
                                 (int) (PlanWorks.getPlanWorks().getHeight() / 2));

      // assertNotNull( "after planworks ", null);

      int numSequences = 7, numSteps = 2;
      // 1-4 used by planViz01 & planViz02 (destructively modified)
      // 5-6 used by planViz03, planViz04 & planViz05
      // 5-7 used by planViz06, planViz07, & planViz08
      sequenceUrls = PWSetupHelper.buildTestData( numSequences, numSteps, this,
                                                  PWTestHelper.GUI_TEST_DIR);

      // System.exit( 0);

      // turn on debugging
      //     JFCEventManager.setDebug(true);
      //     JFCEventManager.setDebugType(JFCEventManager.DEBUG_OUTPUT);
      //     JFCEventManager.setRecording(true);

      flushAWT(); awtSleep();
    } catch (Exception excp) {
      excp.printStackTrace();
      System.exit( -1);
    }
  }

  public void tearDown() throws Exception {
    super.tearDown();
    helper.cleanUp(this);
    //System.exit(0);
  }

  // catch assert errors and Exceptions here, since JUnit seems to not do it 
  public void planVizTests() throws Exception {
    try {
      planViz01(); 
      planViz02(); 
      planViz03(); planViz04(); // 04 depends on 03
      planViz05(); 
      planViz06(); planViz07(); planViz08(); planViz09(); // dependent sequence of tests
      planViz10(); 
      planViz11(); 
      planViz12(); // methods 1-15 of 18 complete for TimelineView; no other views yet

      PWTestHelper.exitPlanWorks( helper, this);

    } catch (AssertionFailedError err) {
      err.printStackTrace();
      System.exit( -1);
    } catch (Exception excp) {
      excp.printStackTrace();
      System.exit( -1);
    }
  } // end planVizTests

  public void planViz01() throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( 0));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);
    // post condition 1
    assertTrueVerbose("PlanWorks title does not contain " + PWTestHelper.PROJECT1,
                      PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT1),
                      "not ");

    // post condition 2
    PWTestHelper.getPlanSequenceMenu();

    // try{Thread.sleep(5000);}catch(Exception e){}

    // post condition 3
    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

    System.err.println( "\nPLANVIZ_01 COMPLETED\n");
  } // end planViz01

  public void planViz02() throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    int numSequences = 4;
    File [] sequenceFileArray = new File [numSequences];
    for (int i = 0; i < numSequences; i++) {
      sequenceFileArray[i] = new File( (String) sequenceUrls.get( i));
    }
    // modify sequence #1
    int seq1NumFilesDeleted = 3;
    (new File( sequenceFileArray[0] + System.getProperty("file.separator") +
               DbConstants.SEQ_PP_STATS)).delete();
    (new File( sequenceFileArray[0] + System.getProperty("file.separator") +
               DbConstants.SEQ_FILE)).delete();
    (new File( sequenceFileArray[0] + System.getProperty("file.separator") +
               DbConstants.SEQ_TRANSACTIONS)).delete();
    String [] fileNames = sequenceFileArray[0].list();
    boolean success = true;
    for (int i = 0, n = fileNames.length; i < n; i++) {
      File fileNameFile = new File( fileNames[i]);
      if (fileNameFile.isDirectory()) {
        success = FileUtils.deleteDir( fileNameFile);
        if (! success) {
          System.err.println( "PlanWorksGUITest.planViz02: deleting '" + fileNames[i] +
                              "' failed"); System.exit( -1);
        }
      }
    }
    // modify sequence #2
    int seq2NumFilesDeleted = 1;
    (new File( sequenceFileArray[1] + System.getProperty("file.separator") +
               DbConstants.SEQ_FILE)).delete();
    // modify sequence #3
    String stepName = "step1"; int stepNumber = 1;
    int seq3Step1FilesDeleted = 1;
    (new File( sequenceFileArray[2] + System.getProperty("file.separator") + stepName +
               System.getProperty("file.separator") + stepName + "." +
               DbConstants.PP_PARTIAL_PLAN_EXT)).delete();
    // modify sequence #4
    String stepDir = sequenceFileArray[3] + System.getProperty("file.separator") + stepName;
    success = FileUtils.deleteDir( new File( stepDir));
    if (! success) {
      System.err.println( "PlanWorksGUITest.planViz02: deleting '" + stepDir +
                          "' failed"); System.exit( -1);
    }
    File [] seqFileArray = new File [1];
    // try sequence #1
    seqFileArray[0] = sequenceFileArray[0];
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, seqFileArray,
                                helper, this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);
    int seq1FilesRemaining = DbConstants.SEQUENCE_FILES.length - seq1NumFilesDeleted;
    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK", seq1FilesRemaining +
                               " sequence files in directory -- " +
                               DbConstants.SEQUENCE_FILES.length + " are required", helper, this);
    AbstractButton cancelButton = PWTestHelper.findButton( "Cancel");
    assertNotNullVerbose( "'Project->Create' cancel button not found:", cancelButton, "not ");
    helper.enterClickAndLeave(new MouseEventData(this, cancelButton));
    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);
    // try{Thread.sleep(5000);}catch(Exception e){}

    // try sequences #2, #3, #4
    File [] seq3FileArray = new File [3];
    seq3FileArray[0] = sequenceFileArray[1];
    seq3FileArray[1] = sequenceFileArray[2];
    seq3FileArray[2] = sequenceFileArray[3];
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, seq3FileArray,
                                helper, this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);
    int seq2FilesRemaining = DbConstants.SEQUENCE_FILES.length - seq2NumFilesDeleted;
    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK", seq2FilesRemaining +
                               " sequence files in directory -- " +
                               DbConstants.SEQUENCE_FILES.length + " are required",
                               helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}
    int seq3Step1FilesRemaining = DbConstants.PARTIAL_PLAN_FILE_EXTS.length -
      seq3Step1FilesDeleted;
    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK",
                               "Has " + seq3Step1FilesRemaining + " files -- " +
                               DbConstants.PARTIAL_PLAN_FILE_EXTS.length + " are required",
                               helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

    ViewListener viewListener01 = new ViewListenerWait01( this);
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME, viewListener01,
                                       helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME, helper, this);

    List viewListenerList = createViewListenerList();
    String viewMenuItemName = "Open " + ViewConstants.TIMELINE_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);

    PWTestHelper.handleDialog( "Resource Not Found Exception", "OK",
                               "Failed to get file listing for " + stepName, helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

    System.err.println( "\nPLANVIZ_02 COMPLETED\n");
  } // end planViz02

  public void planViz03() throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( 4));
//     System.err.println( "sequenceDirectory " + sequenceDirectory);
//     System.err.println( "sequenceFileArray[0] " + sequenceFileArray[0].getName());

    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);
   // try{Thread.sleep(2000);}catch(Exception e){}

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 5));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.handleDialog( "Duplicate Name Exception", "OK",
                               "A project named '" + PWTestHelper.PROJECT1 +
                               "' already exists",
                               helper, this);
    AbstractButton cancelButton = PWTestHelper.findButton( "Cancel");
    assertNotNullVerbose( "'Project->Create' cancel button not found:", cancelButton, "not ");
    helper.enterClickAndLeave( new MouseEventData( this, cancelButton));

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

    System.err.println( "\nPLANVIZ_03 COMPLETED\n");
  } // end planViz03

  public void planViz04() throws Exception {
    // post planViz03 deleteProject condition 1
    assertFalseVerbose( "PlanWorks title does not contain '" + PWTestHelper.PROJECT1 +
                        "'after Project->Delete",
                        PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT1),
                        "not ");

    // post planViz03 deleteProject condition 2
    JMenuItem deleteItem =
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.DELETE_MENU_ITEM,
                                 helper, this);
    assertTrueVerbose( "'Project->Delete' is not disabled", (deleteItem.isEnabled() == false),
                       "not ");
    assertNotNullVerbose( "'Project->Delete' not found:", deleteItem, "not ");
    helper.enterClickAndLeave( new MouseEventData( this, deleteItem));

    System.err.println( "\nPLANVIZ_04 COMPLETED\n");
  } // end planViz04

  public void planViz05() throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( 4));
    // System.err.println( "planViz05 sequenceUrls.get( 4) " + sequenceUrls.get( 4));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);
    // try{Thread.sleep(4000);}catch(Exception e){}

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 5));
    // System.err.println( "planViz05 sequenceUrls.get( 5) " + sequenceUrls.get( 5));
    PWTestHelper.createProject( PWTestHelper.PROJECT2, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);

    PWTestHelper.addSequencesToProject( helper, this, planWorks);
    // try{Thread.sleep(4000);}catch(Exception e){}
    assertTrueVerbose( "PlanWorks title does not contain '" + PWTestHelper.PROJECT2 +
                       "' after 2nd Project->Create",
                       PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT2),
                       "not ");

    PWTestHelper.openProject( PWTestHelper.PROJECT1, helper, this, planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    assertTrueVerbose( "PlanWorks title does not contain '" + PWTestHelper.PROJECT1,
                       PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT1),
                       "not ");

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);
    PWTestHelper.deleteProject( PWTestHelper.PROJECT2, helper, this);

    System.err.println( "\nPLANVIZ_05 COMPLETED\n");
  } // end planViz05

  public void planViz06() throws Exception {
    // try{Thread.sleep(2000);}catch(Exception e){}
     String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( 4));
    // System.err.println( "planViz06 sequenceUrls.get( 4) " + sequenceUrls.get( 4));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 5));
    // System.err.println( "planViz06 sequenceUrls.get( 5) " + sequenceUrls.get( 5));
    PWTestHelper.addPlanSequence( sequenceDirectory, sequenceFileArray, helper,
                                  this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 6));
    // System.err.println( "planViz06 sequenceUrls.get( 6) " + sequenceUrls.get( 6));
    PWTestHelper.addPlanSequence( sequenceDirectory, sequenceFileArray, helper,
                                  this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    assertTrueVerbose( "PlanWorks title does not contain '" + PWTestHelper.PROJECT1,
                       PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT1),
                       "not ");

    // 3 sequences under Planning Sequence
    int sequenceCount = 0;
    JMenu  planSequenceMenu = PWTestHelper.getPlanSequenceMenu();
    for (int i = 0, n = planSequenceMenu.getItemCount(); i < n; i++) {
      JMenuItem menuItem = planSequenceMenu.getItem( i);
      System.err.println( "Planning Sequence->" + menuItem.getText());
      if (menuItem.getText().startsWith( PWTestHelper.SEQUENCE_NAME)) {
        sequenceCount++;
      }
    }
    assertTrueVerbose( "There are not 3 'sequence' plans under 'Planning Sequence'",
                       (sequenceCount == 3), "not ");

    System.err.println( "\nPLANVIZ_06 COMPLETED\n");
  } // end planViz06

  public void planViz07() throws Exception {
    ViewListener viewListener01 = new ViewListenerWait01( this);
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME, viewListener01,
                                       helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME, helper, this);
    int stepNumber = 1;
    String viewMenuItemName = "Open " + ViewConstants.TIMELINE_VIEW;
    List viewListenerList = createViewListenerList();
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
    viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);

    // then sequence (1) timeline view
    viewListener01.reset();
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (1)",
                                        viewListener01, helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView1 =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (1)", helper, this);
    viewListenerListReset( TIMELINE_VIEW_INDEX, viewListenerList);
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView1, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
    viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
    // then sequence (2) timeline view
    viewListener01.reset();
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (2)",
                                        viewListener01, helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView2 =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (2)", helper, this);
    viewListenerListReset( TIMELINE_VIEW_INDEX, viewListenerList);
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView2, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
    viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
    // then sequence (2) timeline view
    // now select the timeline view from the first sequence
    // view exists, so no wait needed
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

   // post condition: seqStepsView timeline view has focus
    String timelineViewName =
      seqStepsView.getName().replaceFirst( ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", ""),
                                           ViewConstants.TIMELINE_VIEW.replaceAll( " ", ""));
    timelineViewName = timelineViewName.concat( System.getProperty( "file.separator") +
                                                "step" + String.valueOf( stepNumber));
    TimelineView timelineView = (TimelineView) PWTestHelper.findComponentByName
      ( TimelineView.class, timelineViewName, Finder.OP_EQUALS);
    assertNotNullVerbose( ViewConstants.TIMELINE_VIEW + " for " + PWTestHelper.SEQUENCE_NAME +
                          " not found", timelineView, "not ");
    int stackIndex = PWTestHelper.getStackIndex( timelineView.getViewFrame());
    // System.err.println( "  stackIndex " + stackIndex);
    assertTrueVerbose( ViewConstants.TIMELINE_VIEW + " for " + PWTestHelper.SEQUENCE_NAME +
                       " is not at top of window stack order", (stackIndex == 0), "not ");

    // post condition: 3 content filter and 3 timeline view windows exist
    JMenu  windowMenu = PWTestHelper.findMenu( PlanWorks.WINDOW_MENU);
    assertNotNullVerbose( "Window menu not found", windowMenu, "not ");
    helper.enterClickAndLeave( new MouseEventData( this, windowMenu));
    JMenuItem stackTopMenuItem = null; // the first timeline one in the list
    int contentFilterCount = 0, timelineViewCount = 0;
    for (int i = 0, n = windowMenu.getMenuComponentCount(); i < n; i++) {
      if (windowMenu.getMenuComponent( i) instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) windowMenu.getMenuComponent( i);
        // System.err.println( "  menu component i " + i + " " + menuItem.getText());
        if (menuItem.getText().startsWith( ViewConstants.CONTENT_SPEC_TITLE)) {
          contentFilterCount++;
        } else if (menuItem.getText().startsWith( ViewConstants.TIMELINE_VIEW.
                                                  replaceAll( " ", ""))) {
          if (stackTopMenuItem == null) {
            stackTopMenuItem = menuItem;
          }
          timelineViewCount++;
        }
      }
    }
    assertTrueVerbose( "There are not 3 '" +  ViewConstants.CONTENT_SPEC_TITLE +
                       "' windows (found " + String.valueOf( contentFilterCount) + ")",
                       (contentFilterCount == 3), "not ");
    assertTrueVerbose( "There are not 3 '" +  ViewConstants.TIMELINE_VIEW +
                       "' windows (found " + String.valueOf( timelineViewCount) + ")",
                       (timelineViewCount == 3), "not ");


    // this is needed to free up Window menu and allow Project->Delete to be available
    helper.enterClickAndLeave( new MouseEventData( this, stackTopMenuItem));
    
    // try{Thread.sleep(2000);}catch(Exception e){}
    System.err.println( "\nPLANVIZ_07 COMPLETED\n");
  } // end planViz07

  public void planViz08() throws Exception {
    JMenuItem tileItem =
      PWTestHelper.findMenuItem( PlanWorks.WINDOW_MENU, PlanWorks.TILE_WINDOWS_MENU_ITEM,
                                 helper, this);
    assertNotNullVerbose( "'Window->Tile Windows' not found:", tileItem, "not ");
    helper.enterClickAndLeave( new MouseEventData( this, tileItem));
    this.flushAWT(); this.awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}
    // post condition:  The ContentFilter and SequenceQuery windows are tiled
    // across the top row of the frame.  The TimelineView and SequenceStepsView
    // windows are tiled in the second thru fourth rows.
    List contentFilterFrames =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.CONTENT_SPEC_TITLE);
    List sequenceQueryFrames =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.SEQUENCE_QUERY_TITLE);
    assertTrueVerbose( "There are not 3 '" +  ViewConstants.CONTENT_SPEC_TITLE +
                       "' windows (found " + contentFilterFrames.size() + ")",
                       (contentFilterFrames.size() == 3), "not ");
    assertTrueVerbose( "There are not 3 '" +  ViewConstants.SEQUENCE_QUERY_TITLE +
                       "' windows (found " + sequenceQueryFrames.size() + ")",
                       (sequenceQueryFrames.size() == 3), "not ");
    double firstRowYLocation = 0.0, secondRowYLocation = 0.0;
    contentFilterFrames.addAll( sequenceQueryFrames);
    Iterator contentFilterItr = contentFilterFrames.iterator();
    while (contentFilterItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) contentFilterItr.next();
//       System.err.println( frame.getTitle() + " y " + frame.getLocation().getY() +
//                           " height " + frame.getSize().getHeight());
      if (frame.getSize().getHeight() > secondRowYLocation) {
        secondRowYLocation = frame.getSize().getHeight();
      }
      assertTrueVerbose( frame.getTitle() + " frame y location not in first row",
                         (frame.getLocation().getY() == firstRowYLocation), "not ");
    }
//     System.err.println("secondRowYLocation " + secondRowYLocation);
    String timelineViewPrefix =
      Utilities.trimView( ViewConstants.TIMELINE_VIEW).replaceAll( " ", "");
    List timelineViewFrames = PWTestHelper.getInternalFramesByPrefixName( timelineViewPrefix);
    String seqStepsViewPrefix =
      Utilities.trimView( ViewConstants.SEQUENCE_STEPS_VIEW).replaceAll( " ", "");
    List sequenceStepsFrames = PWTestHelper.getInternalFramesByPrefixName( seqStepsViewPrefix);
    assertTrueVerbose( "There are not 3 '" +  ViewConstants.TIMELINE_VIEW +
                       "' windows (found " + timelineViewFrames.size() + ")",
                       (timelineViewFrames.size() == 3), "not ");
    assertTrueVerbose( "There are not 3 '" +  ViewConstants.SEQUENCE_STEPS_VIEW +
                       "' windows (found " + sequenceStepsFrames.size() + ")",
                       (sequenceStepsFrames.size() == 3), "not ");
    timelineViewFrames.addAll( sequenceStepsFrames);
    Iterator timelineViewItr = timelineViewFrames.iterator();
    while (timelineViewItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) timelineViewItr.next();
//       System.err.println( frame.getTitle() + " y " + frame.getLocation().getY() +
//                           " height " + frame.getSize().getHeight());
      assertTrueVerbose( frame.getTitle() + " frame y location not >= second row",
                         (frame.getLocation().getY() >= secondRowYLocation), "not ");
    }
    // "Window->Cascade".
    JMenuItem cascadeItem =
      PWTestHelper.findMenuItem( PlanWorks.WINDOW_MENU, PlanWorks.CASCADE_WINDOWS_MENU_ITEM,
                                 helper, this);
    assertNotNullVerbose( "'Window->Cascade Windows' not found:", cascadeItem, "not ");
    helper.enterClickAndLeave( new MouseEventData( this, cascadeItem));
    this.flushAWT(); this.awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}

    List cascadedFrames = new ArrayList();
    Container contentPane = PlanWorks.getPlanWorks().getContentPane();
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      Component component = (Component) contentPane.getComponent( i);
      if (component instanceof MDIDesktopPane) {
        JInternalFrame[] frames = ((MDIDesktopPane) component).getAllFrames();
        for (int j = 0, m = frames.length; j < m; j++) {
          // System.err.println( "j " + j + " " + ((MDIInternalFrame) frames[j]).getTitle());
          MDIInternalFrame frame = (MDIInternalFrame) frames[j];
//           System.err.println( "frame " + frame.getTitle() + " y " +
//                               frame.getLocation().getY() +
//                               " height " + frame.getSize().getHeight());
          if ((frame.getTitle().startsWith( ViewConstants.CONTENT_SPEC_TITLE)) ||
              (frame.getTitle().startsWith( ViewConstants.SEQUENCE_QUERY_TITLE))) {
            assertTrueVerbose( frame.getTitle() + " not in first row",
                               (frame.getLocation().getY() == firstRowYLocation), "not ");
          } else if ((frame.getTitle().startsWith( timelineViewPrefix)) ||
                     (frame.getTitle().startsWith( seqStepsViewPrefix))) {
            cascadedFrames.add( frame);
          }
        }
        break;
      }
    }
    // sort by x, then check that x & y vlaues are incrementally increasing
    double frameX = 0.0, frameY = 0.0;
    Collections.sort( cascadedFrames, new FrameXAscending());
    Iterator frameItr = cascadedFrames.iterator();
    while (frameItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) frameItr.next();
//       System.err.println( "frame " + frame.getTitle() + " x " +
//                           frame.getLocation().getX() + " y " + frame.getLocation().getY());
      assertTrueVerbose( frame.getTitle() + " not cascaded in X",
                         ((frame.getLocation().getX() > frameX) ||
                          (frame.getLocation().getX() == 0.0)), "not ");
      frameX = frame.getLocation().getX();
      assertTrueVerbose( frame.getTitle() + " not cascaded in Y",
                         (frame.getLocation().getY() > frameY), "not ");
      frameY = frame.getLocation().getY();
    }

    System.err.println( "\nPLANVIZ_08 COMPLETED\n");
  } // end planViz08

  class FrameXAscending implements Comparator {
    public FrameXAscending() {
    }
    public int compare( Object o1, Object o2) {
      Double s1 = new Double( ((MDIInternalFrame) o1).getLocation().getX());
      Double s2 = new Double( ((MDIInternalFrame) o2).getLocation().getX());
      return s1.compareTo( s2);
    }

    public boolean equals( Object o1, Object o2) {
      Double s1 = new Double( ((MDIInternalFrame) o1).getLocation().getX());
      Double s2 = new Double( ((MDIInternalFrame) o2).getLocation().getX());
      return s1.equals( s2);
    }
  } // end class FrameXAscending

  public void planViz09() throws Exception {
    //  Use "SequenceQuery for test-seq-2" to create a "Steps" and a
    //      "Transactions" QueryResult for ..." window.
    String sequenceName = PWTestHelper.SEQUENCE_NAME + " (2)";
    String viewName = ViewConstants.SEQUENCE_QUERY_TITLE + " for " + sequenceName;
    int stepNumber = 1;
    SequenceQueryWindow  seqQueryWindow =
      PWTestHelper.getSequenceQueryWindow( sequenceName, helper, this);

    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_STEPS,
                                     helper, this);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.STEPS_WHERE_CONSTRAINT_TRANSACTED,
                                     helper, this);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.ConstraintTransComboBox.class,
                                     SequenceQueryWindow.CONSTRAINT_TRANSACTION_TYPE_ALL,
                                     helper, this);
    ViewListener viewListener01 = new ViewListenerWait01( this);
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, this);
    viewListener01.viewWait();
    int resultsWindowCount = 1;
    StepQueryView stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          sequenceName, resultsWindowCount,
                                                          helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_TRANSACTIONS,
                                     helper, this);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.TRANSACTIONS_FOR_TOKEN,
                                     helper, this);
    PwPlanningSequence planSeq =
      planWorks.getCurrentProject().getPlanningSequence( (String) sequenceUrls.get( 5));
    PwPartialPlan partialPlan = planSeq.getPartialPlan( stepNumber);
    PwToken token = (PwToken) partialPlan.getTokenList().get( 0);
    String fieldName = "Key";
      PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName,
                                          token.getId().toString(), helper, this);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, this);
    viewListener01.viewWait();
    resultsWindowCount = 2;
    DBTransactionQueryView transQueryResults =
      (DBTransactionQueryView) PWTestHelper.getQueryResultsWindow
      ( DBTransactionQueryView.class, sequenceName, resultsWindowCount, helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

    // delete seq2 & seq3
    PWTestHelper.deleteSequenceFromProject( (String) sequenceUrls.get( 5), helper, this);
    PWTestHelper.deleteSequenceFromProject( (String) sequenceUrls.get( 6), helper, this);
    // Method:2 - Four windows remain: the ContentFilter, the SequenceQuery,
    //  the TimelineView, and the SequenceStepsView of "test-seq-1".
    int totalFrameCnt = 4;
    int internalFrameCnt = PWTestHelper.getAllInternalFrames().size();
    assertTrueVerbose( "There are not " +  totalFrameCnt + " (found " +
                       internalFrameCnt + ")", (internalFrameCnt == totalFrameCnt), "not ");
    int contentFilterCnt =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.CONTENT_SPEC_TITLE).size();
    int sequenceQueryCnt =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.SEQUENCE_QUERY_TITLE).size();
    int timelineViewCnt = PWTestHelper.getInternalFramesByPrefixName
      ( ViewConstants.TIMELINE_VIEW.replaceAll( " ", "")).size();
    int sequenceStepsViewCnt = PWTestHelper.getInternalFramesByPrefixName
        ( ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", "")).size();
    assertTrueVerbose( "There is not 1 " + ViewConstants.CONTENT_SPEC_TITLE + " window, " +
                       " 1 " + ViewConstants.SEQUENCE_QUERY_TITLE + " window, " +
                       " 1 " + ViewConstants.TIMELINE_VIEW + " window, and " +
                       " 1 " + ViewConstants.SEQUENCE_STEPS_VIEW + " window.",
                       ((contentFilterCnt + sequenceQueryCnt + timelineViewCnt +
                         sequenceStepsViewCnt) == totalFrameCnt), "not ");

    // Re-add "test-seq-3" sequence by using menu-bar selection of
    //  "Project->Add Sequence ...".  Then open the sequence using
    //  "Planning Sequence->test-seq-3"
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 6));
    PWTestHelper.addPlanSequence( sequenceDirectory, sequenceFileArray, helper,
                                  this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    sequenceName = PWTestHelper.SEQUENCE_NAME + " (1)";
    viewListener01.reset();
    PWTestHelper.openSequenceStepsView( sequenceName, viewListener01, helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( sequenceName, helper, this);
    totalFrameCnt = 6;
    int totalSeqQueryCnt = 2, totalSeqStepsCnt = 2;
    internalFrameCnt = PWTestHelper.getAllInternalFrames().size();
    assertTrueVerbose( "There are not " +  String.valueOf( totalFrameCnt) + " frames (found " +
                       String.valueOf( internalFrameCnt) + ")",
                       (internalFrameCnt == totalFrameCnt), "not ");
    sequenceQueryCnt =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.SEQUENCE_QUERY_TITLE).size();
    sequenceStepsViewCnt = PWTestHelper.getInternalFramesByPrefixName
        ( ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", "")).size();
    assertTrueVerbose( "There are not 2 " + ViewConstants.SEQUENCE_QUERY_TITLE + " windows",
                       (sequenceQueryCnt == totalSeqQueryCnt), "not ");
    assertTrueVerbose( "There are not 2 " + ViewConstants.SEQUENCE_STEPS_VIEW + " windows",
                       (sequenceStepsViewCnt == totalSeqStepsCnt), "not ");
    assertTrueVerbose( "The re-loaded sequence ("  + sequenceName + ") is not the original" +
                       " third sequence ", seqStepsView.getPlanningSequence().getUrl().
                       equals( (String) sequenceUrls.get( 6)), "not ");
    //try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

    System.err.println( "\nPLANVIZ_09 COMPLETED\n");
  } // end planViz09


  public void planViz10() throws Exception {
    int stepNumber = 1, seqUrlIndex = 4;
    List viewList = new ArrayList();
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( seqUrlIndex));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    ViewListener viewListener01 = new ViewListenerWait01( this);
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME, viewListener01,
                                       helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME, helper, this);

    List viewListenerList = createViewListenerList();
    String viewMenuItemName = "Open " + ViewConstants.CONSTRAINT_NETWORK_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
   String viewNameSuffix = PWTestHelper.SEQUENCE_NAME +
     System.getProperty( "file.separator") + "step" + String.valueOf( stepNumber);
   viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
   ConstraintNetworkView constraintNetworkView =
     (ConstraintNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.CONSTRAINT_NETWORK_VIEW, viewNameSuffix, this);
    viewList.add( constraintNetworkView);

    viewMenuItemName = "Open " + ViewConstants.TEMPORAL_EXTENT_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
    viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
    TemporalExtentView temporalExtentView =
      (TemporalExtentView) PWTestHelper.getPartialPlanView
      ( ViewConstants.TEMPORAL_EXTENT_VIEW, viewNameSuffix, this);
    viewList.add( temporalExtentView);

    viewMenuItemName = "Open " + ViewConstants.TIMELINE_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
    viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
    TimelineView timelineView = (TimelineView) PWTestHelper.getPartialPlanView
      ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, this);
    viewList.add( timelineView);

    viewMenuItemName = "Open " + ViewConstants.TOKEN_NETWORK_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
    viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    TokenNetworkView tokenNetworkView = (TokenNetworkView) PWTestHelper.getPartialPlanView
      ( ViewConstants.TOKEN_NETWORK_VIEW, viewNameSuffix, this);
    viewList.add( tokenNetworkView);

    viewMenuItemName = "Open " + ViewConstants.DB_TRANSACTION_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
    viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
     DBTransactionView dbTransactionView =
       (DBTransactionView) PWTestHelper.getPartialPlanView
       ( ViewConstants.DB_TRANSACTION_VIEW, viewNameSuffix, this);
    viewList.add( dbTransactionView);

    PwPlanningSequence planSeq =
      planWorks.getCurrentProject().getPlanningSequence( (String) sequenceUrls.get( seqUrlIndex));
    PwPartialPlan partialPlan = planSeq.getPartialPlan( stepNumber);

    planViz10CNet( constraintNetworkView, stepNumber, planSeq, partialPlan);

    planViz10TempExt( temporalExtentView, stepNumber, planSeq, partialPlan);

    planViz10Timeline( timelineView, stepNumber, planSeq, partialPlan);

    planViz10TokNet( tokenNetworkView, stepNumber, planSeq, partialPlan);

    planViz10DBTrans( dbTransactionView, stepNumber, planSeq, partialPlan);

    // no need to wait on these calls, since all the views currently exist
    viewMenuItemName = "Hide All Views";
    PWTestHelper.seqStepsViewStepItemSelection
      ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}
    Iterator viewItr = viewList.iterator();
    while (viewItr.hasNext()) {
      MDIInternalFrame frame = ((PartialPlanView) viewItr.next()).getViewFrame();
      assertTrueVerbose( frame.getTitle() + " is not iconified", (frame.isIcon() == true),
                         " not");
    }

    viewMenuItemName = "Show All Views";
    PWTestHelper.seqStepsViewStepItemSelection
      ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}
    viewItr = viewList.iterator();
    while (viewItr.hasNext()) {
      MDIInternalFrame frame = ((PartialPlanView) viewItr.next()).getViewFrame();
      assertTrueVerbose( frame.getTitle() + " is not shown", (frame.isIcon() == false),
                         " not");
    }

    viewMenuItemName = "Close All Views";
    PWTestHelper.seqStepsViewStepItemSelection
      ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, this);
    // viewSet is set to null, if there are no views
    ViewSet viewSet = PlanWorks.getPlanWorks().getViewManager().getViewSet( partialPlan);
    assertNullVerbose( "Partial Plan view set is not null", viewSet, " not");

    PWTestHelper.viewBackgroundItemSelection( seqStepsView, "Overview Window", helper, this);
    String overviewViewName =
      Utilities.trimView( ViewConstants.SEQUENCE_STEPS_VIEW).replaceAll( " ", "") +
      ViewConstants.OVERVIEW_TITLE + planSeq.getName();
    VizViewOverview seqStepsOverview =
      (VizViewOverview) PWTestHelper.findComponentByName
      ( VizViewOverview.class, overviewViewName, Finder.OP_EQUALS);
    assertNotNullVerbose( overviewViewName + " not found", seqStepsOverview, "not ");

    Overview.OverviewRectangle rectangle = seqStepsOverview.getOverviewRect();
    System.err.println( "OverviewRectangle location " + rectangle.getLocation());
    // try{Thread.sleep(4000);}catch(Exception e){}

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

    System.err.println( "\nPLANVIZ_10 COMPLETED\n");
  } // end planViz10

  public void planViz10CNet( ConstraintNetworkView constraintNetworkView, int stepNumber,
                             PwPlanningSequence planSeq,  PwPartialPlan partialPlan)
    throws Exception {
    ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
    // open interval token, resource, variable, and constraint nodes in ConstraintNetworkView
    int numTokensOpened = 3, numResourcesOpened = 1, numResTransactionsOpened = 1;
    int numTimelinesOpened = 1, numObjectsOpened = 1, numRuleInstancesOpened = 1;
    ConstraintNetworkTokenNode baseTokenNode = null;
    ConstraintNetworkTokenNode mergedTokenNode = null;
    ConstraintNetworkTokenNode freeTokenNode = null;
    ConstraintNetworkTokenNode resTransactionNode = null;
    ConstraintNetworkResourceNode resourceNode = null;
    ConstraintNetworkTimelineNode timelineNode = null;
    ConstraintNetworkObjectNode objectNode = null;
    ConstraintNetworkRuleInstanceNode ruleInstanceNode = null;
    
    int numTokenNodes = 0, numResourceNodes = 0, numResTransactionNodes = 0;
    int numTimelineNodes = 0, numObjectNodes = 0, numRuleInstanceNodes = 0;
    Iterator containerNodeItr = constraintNetworkView.getContainerNodeList().iterator();
    while (containerNodeItr.hasNext()) {
      VariableContainerNode contNode = (VariableContainerNode) containerNodeItr.next();
      if (contNode instanceof ConstraintNetworkTokenNode) {
        ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) contNode;
//         System.err.println( "tokenNode " + tokenNode.getClass().getName() +
//                             " token " + tokenNode.getToken().getClass().getName());
        if (tokenNode.getToken() instanceof PwResourceTransaction) {
          if (resTransactionNode == null) {
            resTransactionNode = (ConstraintNetworkTokenNode) contNode;
          }
          numResTransactionNodes++;
        } else if (tokenNode.getToken() instanceof PwToken) {
          PwToken token = (PwToken) tokenNode.getToken();
          boolean isFree = token.isFree(), isBaseToken = token.isBaseToken();
          int slotTokenCnt = 0;
          if (token.getSlotId() != DbConstants.NO_ID) {
            slotTokenCnt =
              partialPlan.getSlot( token.getSlotId()).getTokenList().size();
          }
          if ((! isFree ) && isBaseToken && (slotTokenCnt > 1) && (baseTokenNode == null)) {
            baseTokenNode = tokenNode;
          } else if (isFree && (freeTokenNode == null)) {
            freeTokenNode = tokenNode;
          }
        }
        numTokenNodes++;
      } else if (contNode instanceof ConstraintNetworkResourceNode) {
        if (resourceNode == null) {
          resourceNode = (ConstraintNetworkResourceNode) contNode;
        }
        numResourceNodes++;
      } else if (contNode instanceof ConstraintNetworkTimelineNode) {
        if (timelineNode == null) {
          timelineNode = (ConstraintNetworkTimelineNode) contNode;
        }
        numTimelineNodes++;
      } else if (contNode instanceof ConstraintNetworkObjectNode) {
        if (objectNode == null) {
          objectNode = (ConstraintNetworkObjectNode) contNode;
        }
        numObjectNodes++;
      } else if (contNode instanceof ConstraintNetworkRuleInstanceNode) {
        if (ruleInstanceNode == null) {
          ruleInstanceNode = (ConstraintNetworkRuleInstanceNode) contNode;
        }
        numRuleInstanceNodes++;
      }
    }
    // find mergedTokenNode
    containerNodeItr = constraintNetworkView.getContainerNodeList().iterator();
    while (containerNodeItr.hasNext()) {
      VariableContainerNode contNode = (VariableContainerNode) containerNodeItr.next();
      if (contNode instanceof ConstraintNetworkTokenNode) {
        ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) contNode;
        if (tokenNode.getToken() instanceof PwToken) {
          PwToken token = (PwToken) tokenNode.getToken();
          boolean isFree = token.isFree(), isBaseToken = token.isBaseToken();
          if ((! isFree) && (! isBaseToken) && (mergedTokenNode == null) &&
                     (baseTokenNode != null) &&
                     (baseTokenNode.getToken().getSlotId().equals( token.getSlotId()))) {
            mergedTokenNode = tokenNode;
            break;
          }
        }
      }
    }
    // try{Thread.sleep(6000);}catch(Exception e){}

    assertNotNullVerbose( "Did not find ConstraintNetworkTokenNode baseTokenNode",
                          baseTokenNode, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetworkTokenNode mergedTokenNode",
                          mergedTokenNode, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetworkTokenNode freeTokenNode",
                          freeTokenNode, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetworkResourceNode resourceNode",
                          resourceNode, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetworkTokenNode resTransactionNode",
                          resTransactionNode, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetworkTimelineNode timelineNode",
                          timelineNode, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetworkObjectNode objectNode",
                          objectNode, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetworkRuleInstanceNode ruleInstanceNode",
                          ruleInstanceNode, "not ");
    assertTrueVerbose
      ( "Number of partial plan interval tokens and resource transactions (" +
        partialPlan.getTokenList().size() +
        ") not equal number of ContraintNetwork interval token and resource transaction " +
        "nodes (" + numTokenNodes + ")", (partialPlan.getTokenList().size() == numTokenNodes),
        "not ");
    assertTrueVerbose
      ( "Number of partial plan resources (" + partialPlan.getResourceList().size() +
        ") not equal number of ContraintNetwork resource nodes (" + numResourceNodes +
        ")", (partialPlan.getResourceList().size() == numResourceNodes), "not ");
    assertTrueVerbose
      ( "Number of partial plan resourceTransactions (" +
        partialPlan.getResTransactionList().size() + ") not equal number of ContraintNetwork " +
        "resourceTransaction nodes (" + numResTransactionNodes + ")",
        (partialPlan.getResTransactionList().size() == numResTransactionNodes), "not ");
    assertTrueVerbose
      ( "Number of partial plan timelines (" + partialPlan.getTimelineList().size() +
        ") not equal number of ContraintNetwork timeline nodes (" + numTimelineNodes +
        ")", (partialPlan.getTimelineList().size() == numTimelineNodes), "not ");
    int numNodes = numObjectNodes + numResourceNodes + numTimelineNodes;
    assertTrueVerbose
      ( "Number of partial plan objects, timelines, & resources (" +
        partialPlan.getObjectList().size() +
        ") not equal number of ContraintNetwork object, timeline, & resources nodes (" +
        numNodes + ")", (partialPlan.getObjectList().size() == numNodes), "not ");
    assertTrueVerbose
      ( "Number of partial plan rule instances (" + partialPlan.getRuleInstanceList().size() +
        ") not equal number of ContraintNetwork rule instance nodes (" + numRuleInstanceNodes +
        ")", (partialPlan.getRuleInstanceList().size() == numRuleInstanceNodes), "not ");
    assertTrueVerbose
      ( "Token id=" + mergedTokenNode.getToken().getId() + " is not a merged " +
        " token of base token id=" + baseTokenNode.getToken().getId(),
        (mergedTokenNode.getToken().getSlotId().equals
         ( baseTokenNode.getToken().getSlotId())), "not ");

    baseTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, baseTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    mergedTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, mergedTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    freeTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, freeTokenNode.getLocation(),
                                new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    resourceNode.doMouseClick( MouseEvent.BUTTON1_MASK, resourceNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    resTransactionNode.doMouseClick( MouseEvent.BUTTON1_MASK, resTransactionNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    timelineNode.doMouseClick( MouseEvent.BUTTON1_MASK, timelineNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    objectNode.doMouseClick( MouseEvent.BUTTON1_MASK, objectNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    ruleInstanceNode.doMouseClick( MouseEvent.BUTTON1_MASK, ruleInstanceNode.getLocation(),
                                   new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    int numVarNodesInView = constraintNetworkView.getVariableNodeList().size();
    int numVarNodesOpened =
      (numTokensOpened * PWSetupHelper.NUM_VARS_PER_TOKEN) +
      (numResourcesOpened * PWSetupHelper.NUM_VARS_PER_RESOURCE) +
      (numResTransactionsOpened * PWSetupHelper.NUM_VARS_PER_RESOURCE_TRANS) +
      (numTimelinesOpened * PWSetupHelper.NUM_VARS_PER_TIMELINE) +
      (numObjectsOpened * PWSetupHelper.NUM_VARS_PER_OBJECT) +
      (numRuleInstancesOpened * PWSetupHelper.NUM_VARS_PER_RULE_INSTANCE);
    assertTrueVerbose( "Number of ContraintNetwork variable nodes (" + numVarNodesInView +
                       ") not equal number of token (interval & resTrans) and resource " +
                       "variables opened (" + numVarNodesOpened + ")",
                       (numVarNodesOpened == numVarNodesInView), "not ");
    // open token type = START_VAR variables
    int numVariablesOpened = 3;
    VariableNode variableNode1 = null;
    VariableNode variableNode2 = null;
    VariableNode variableNode3 = null;
    int numVariableNodes = 0;
    Iterator variableNodeItr = constraintNetworkView.getVariableNodeList().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode varNode = (VariableNode) variableNodeItr.next();
      if (varNode.getVariable().getType().equals( DbConstants.START_VAR)) {
        Iterator containNodeItr = varNode.getContainerNodeList().iterator();
        while (containNodeItr.hasNext()) {
          VariableContainerNode containNode = (VariableContainerNode) containNodeItr.next();
          if (containNode instanceof ConstraintNetworkTokenNode) {
            ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) containNode;
            if (tokenNode.getToken().getId().equals( baseTokenNode.getToken().getId())) {
              variableNode1 = varNode;
            } else if (tokenNode.getToken().getId().equals
                       ( mergedTokenNode.getToken().getId())) {
              variableNode2 = varNode;
            } else if (tokenNode.getToken().getId().equals( freeTokenNode.getToken().getId())) {
              variableNode3 = varNode;
            }
          }
        }
      }
      numVariableNodes++;
    }
    assertNotNullVerbose( "Did not find ConstraintNetwork VariableNode variableNode1",
                   variableNode1, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetwork VariableNode variableNode2",
                   variableNode2, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetwork VariableNode variableNode3",
                   variableNode3, "not ");
    variableNode1.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    variableNode2.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode2.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    variableNode3.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode3.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    int numContNodesInView = constraintNetworkView.getConstraintNodeList().size();
    int numContNodesOpened =
      (numTokensOpened * PWSetupHelper.NUM_CONSTRAINTS_PER_TOKEN) +
      (numResourcesOpened * PWSetupHelper.NUM_CONSTRAINTS_PER_RESOURCE);
    assertTrueVerbose( "Number of ContraintNetwork constraint nodes (" + numContNodesInView +
                       ") not equal number of token (interval & resTrans) and resource " +
                       "constraints opened (" + numContNodesOpened + ")",
                       (numContNodesOpened == numContNodesInView), "not ");
//     System.err.println( "ContraintNetworkView: " + numVariablesOpened +
//                         " variables opened ==>> " +
//                         numContNodesInView + " constraint nodes visible");

    ConstraintNode constraintNode1 = null;
    ConstraintNode constraintNode2 = null;
    Iterator constraintNodeItr = constraintNetworkView.getConstraintNodeList().iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode contNode = (ConstraintNode) constraintNodeItr.next();
      if (! contNode.areNeighborsShown()) {
        if (constraintNode1 == null) {
          constraintNode1 = contNode;
        } else if (constraintNode2 == null) {
          constraintNode2 = contNode;
          break;
        }
      }
    }
    assertNotNullVerbose( "Did not find ConstraintNetwork ConstraintNode constraintNode1",
                   constraintNode1, "not ");
    assertNotNullVerbose( "Did not find ConstraintNetwork ConstraintNode constraintNode2",
                   constraintNode2, "not ");
    constraintNode1.doMouseClick( MouseEvent.BUTTON1_MASK, constraintNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    constraintNode2.doMouseClick( MouseEvent.BUTTON1_MASK, constraintNode2.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();

    // try{Thread.sleep(6000);}catch(Exception e){}

    // now close them in reverse order
    constraintNode1.doMouseClick( MouseEvent.BUTTON1_MASK, constraintNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    constraintNode2.doMouseClick( MouseEvent.BUTTON1_MASK, constraintNode2.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    variableNode1.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    variableNode2.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode2.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    variableNode3.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode3.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    baseTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, baseTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    mergedTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, mergedTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    freeTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, freeTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    resourceNode.doMouseClick( MouseEvent.BUTTON1_MASK, resourceNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    resTransactionNode.doMouseClick( MouseEvent.BUTTON1_MASK, resTransactionNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    timelineNode.doMouseClick( MouseEvent.BUTTON1_MASK, timelineNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    objectNode.doMouseClick( MouseEvent.BUTTON1_MASK, objectNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    ruleInstanceNode.doMouseClick( MouseEvent.BUTTON1_MASK, ruleInstanceNode.getLocation(),
                                   new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();

    assertTrueVerbose( "constraintNode1 is not closed",
                        (! constraintNode1.areNeighborsShown()), "not ");
    assertTrueVerbose( "constraintNode2 is not closed",
                        (! constraintNode2.areNeighborsShown()), "not ");
    assertTrueVerbose( "variableNode1 is not closed",
                        (! variableNode1.areNeighborsShown()), "not ");
    assertTrueVerbose( "variableNode2 is not closed",
                        (! variableNode2.areNeighborsShown()), "not ");
    assertTrueVerbose( "variableNode3 is not closed",
                       (! variableNode3.areNeighborsShown()), "not ");
    assertTrueVerbose( "baseTokenNode is not closed",
                       (! baseTokenNode.areNeighborsShown()), "not ");
    assertTrueVerbose( "mergedTokenNode is not closed",
                       (! mergedTokenNode.areNeighborsShown()), "not ");
    assertTrueVerbose( "freeTokenNode is not closed",
                       (! freeTokenNode.areNeighborsShown()), "not ");
    assertTrueVerbose( "resourceNode is not closed",
                       (! resourceNode.areNeighborsShown()), "not ");
    assertTrueVerbose( "resTransactionNode is not closed",
                        (! resTransactionNode.areNeighborsShown()), "not ");
    assertTrueVerbose( "timelineNode is not closed",
                       (! timelineNode.areNeighborsShown()), "not ");
    assertTrueVerbose( "objectNode is not closed",
                       (! objectNode.areNeighborsShown()), "not ");
    assertTrueVerbose( "ruleInstanceNode is not closed",
                       (! ruleInstanceNode.areNeighborsShown()), "not ");

//     System.err.println( "baseTokenNode.getToolTipText() " + baseTokenNode.getToolTipText());
//     System.err.println( "baseTokenNode.getText() " + baseTokenNode.getText());
    String toolTipText = baseTokenNode.getToolTipText();
    String labelText = baseTokenNode.getText();
    String predSubString = baseTokenNode.getPredicateName();
    String predParamSubString = predSubString + " (";
    String slotSubString = "slot key=" + baseTokenNode.getToken().getSlotId().toString();
    String tokenSubString = "key=" + baseTokenNode.getToken().getId().toString();
    String mouseSubString = "Mouse-L: open";
    assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + predParamSubString + "'",
                       (toolTipText.indexOf( predParamSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + slotSubString + "'",
                       (toolTipText.indexOf( slotSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") label does not contain '" + tokenSubString + "'",
                       (labelText.indexOf( tokenSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") label does not contain '" + predSubString + "'",
                       (labelText.indexOf( predSubString) >= 0), "not ");

//     System.err.println( "resTransactionNode.getToolTipText() " +
//                         resTransactionNode.getToolTipText());
//     System.err.println( "resTransactionNode.getText() " + resTransactionNode.getText());
    toolTipText = resTransactionNode.getToolTipText();
    labelText = resTransactionNode.getText();
    String nameSubString = resTransactionNode.getToken().getName();
    String resourceSubString = "key=" + resTransactionNode.getToken().getId().toString();
    assertTrueVerbose( "ConstraintNetworkView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + nameSubString + "'",
                       (toolTipText.indexOf( nameSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" + resourceSubString + "'",
                       (labelText.indexOf( resourceSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" + nameSubString + "'",
                       (labelText.indexOf( nameSubString) >= 0), "not ");

//     System.err.println( "resourceNode.getToolTipText() " + resourceNode.getToolTipText());
//     System.err.println( "resourceNode.getText() " + resourceNode.getText());
    toolTipText = resourceNode.getToolTipText();
    labelText = resourceNode.getText();
    nameSubString = resourceNode.getResource().getName();
    resourceSubString = "key=" + resourceNode.getResource().getId().toString();
    assertTrueVerbose( "ConstraintNetworkView resource node (" +
                       resourceNode.getResource().getId().toString() +
                       ") tool tip does not contain '" + nameSubString + "'",
                       (toolTipText.indexOf( nameSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView resource node (" +
                       resourceNode.getResource().getId().toString() +
                       ") tool tip does not contain '" + mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView resource node (" +
                       resourceNode.getResource().getId().toString() +
                       ") label does not contain '" + resourceSubString + "'",
                       (labelText.indexOf( resourceSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView resource node (" +
                       resourceNode.getResource().getId().toString() +
                       ") label does not contain '" + nameSubString + "'",
                       (labelText.indexOf( nameSubString) >= 0), "not ");

//     System.err.println( "variableNode1.getToolTipText() " + variableNode1.getToolTipText());
//     System.err.println( "variableNode1.getText() " + variableNode1.getText());
    toolTipText = variableNode1.getToolTipText();
    labelText = variableNode1.getText();
    String typeSubString = variableNode1.getVariable().getType();
    String variableSubString = "key=" + variableNode1.getVariable().getId().toString();
    String domainSubString = variableNode1.getVariable().getDomain().toString();
    assertTrueVerbose( "ConstraintNetworkView variable node (" +
                       variableNode1.getVariable().getId().toString() +
                       ") tool tip does not contain '" + typeSubString + "'",
                       (toolTipText.indexOf( typeSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView variable node (" +
                       variableNode1.getVariable().getId().toString() +
                       ") tool tip does not contain '" +mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView variable node (" +
                       variableNode1.getVariable().getId().toString() +
                       ") label does not contain '" + variableSubString + "'",
                       (labelText.indexOf( variableSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView variable node (" +
                       variableNode1.getVariable().getId().toString() +
                       ") label does not contain '" + domainSubString + "'",
                       (labelText.indexOf( domainSubString) >= 0), "not ");

//     System.err.println( "constraintNode1.getToolTipText() " + constraintNode1.getToolTipText());
//     System.err.println( "constraintNode1.getText() " + constraintNode1.getText());
    toolTipText = constraintNode1.getToolTipText();
    labelText = constraintNode1.getText();
    typeSubString = constraintNode1.getConstraint().getType();
    String constraintSubString = "key=" + constraintNode1.getConstraint().getId().toString();
    nameSubString = constraintNode1.getConstraint().getName();
    assertTrueVerbose( "ConstraintNetworkView constraint node (" +
                       constraintNode1.getConstraint().getId().toString() +
                       ") tool tip does not contain '" + typeSubString + "'",
                       (toolTipText.indexOf( typeSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView constraint node (" +
                       constraintNode1.getConstraint().getId().toString() +
                       ") tool tip does not contain '" + mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView constraint node (" +
                       constraintNode1.getConstraint().getId().toString() +
                       ") label does not contain '" + constraintSubString + "'",
                       (labelText.indexOf( constraintSubString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView constraint node (" +
                       constraintNode1.getConstraint().getId().toString() +
                       ") label does not contain '" + nameSubString + "'",
                       (labelText.indexOf( nameSubString) >= 0), "not ");

    labelText = ruleInstanceNode.getText();
    toolTipText = ruleInstanceNode.getToolTipText();
    String keyString = "key=" + ruleInstanceNode.getRuleInstance().getId().toString();
    String ruleString = "rule ";
    assertTrueVerbose( "ConstraintNetworkView rule instance node (" +
                       ruleInstanceNode.getRuleInstance().getId().toString() +
                       ") label does not contain '" + ruleString + "'",
                       (labelText.indexOf( ruleString) >= 0), "not ");
    assertTrueVerbose( "ConstraintNetworkView rule instance node (" +
                       ruleInstanceNode.getRuleInstance().getId().toString() +
                       ") label does not contain '" + keyString + "'",
                       (labelText.indexOf( keyString) >= 0), "not ");
  } // end planViz10CNet

 
  public void planViz10TempExt( TemporalExtentView temporalExtentView, int stepNumber,
                                PwPlanningSequence planSeq,  PwPartialPlan partialPlan)
    throws Exception {
    ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
    // try{Thread.sleep(6000);}catch(Exception e){}
    TemporalNode baseTokenNode = null;
    TemporalNode mergedTokenNode = null;
    TemporalNode freeTokenNode = null;
    TemporalNode resTransactionNode = null;
    int numIntTokenNodes = 0, numResTransNodes = 0;
    Iterator temporalNodeItr = temporalExtentView.getTemporalNodeList().iterator();
    while (temporalNodeItr.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeItr.next();
      PwToken token = temporalNode.getToken();
      if (token instanceof PwResourceTransaction) {
        if (resTransactionNode == null) {
          resTransactionNode = temporalNode;
        }
        numResTransNodes++;
      } else {
        int slotTokenCnt = 0;
        if (token.getSlotId() != DbConstants.NO_ID) {
          slotTokenCnt =
            partialPlan.getSlot( token.getSlotId()).getTokenList().size();
        }
        if ((! token.isFree()) && token.isBaseToken() && (slotTokenCnt > 1) &&
            (baseTokenNode == null)) {
          baseTokenNode = temporalNode;
        } else if (token.isFree() && (freeTokenNode == null)) {
          freeTokenNode = temporalNode;
        }
        numIntTokenNodes++;
      }
    }
    // find mergedTokenNode
    temporalNodeItr = temporalExtentView.getTemporalNodeList().iterator();
    while (temporalNodeItr.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeItr.next();
      PwToken token = temporalNode.getToken();
      boolean isFree = token.isFree(), isBaseToken = token.isBaseToken();
      if ((! token.isFree()) && (! token.isBaseToken()) &&
          (mergedTokenNode == null) && (baseTokenNode != null) &&
          (baseTokenNode.getToken().getSlotId().equals
           (temporalNode.getToken().getSlotId()))) {
          mergedTokenNode = temporalNode;
          break;
      }
    }

    assertNotNullVerbose( "Did not find TemporalExtentView baseToken TemporalNode",
                   baseTokenNode, "not ");
    assertNotNullVerbose( "Did not find TemporalExtentView mergedToken TemporalNode",
                   mergedTokenNode, "not ");
    assertNotNullVerbose( "Did not find TemporalExtentView freeToken TemporalNode",
                   freeTokenNode, "not ");
    assertNotNullVerbose( "Did not find TemporalExtentView resourceTransaction TemporalNode",
                   resTransactionNode, "not ");
    assertTrueVerbose( "Number of partial plan interval tokens (" +
                       partialPlan.getTokenList().size() +
                       ") not equal number of TemporalExtent " +
                       "temporal nodes (" + (numIntTokenNodes + numResTransNodes) + ")",
                       (partialPlan.getTokenList().size() ==
                        (numIntTokenNodes + numResTransNodes)), "not ");
    assertTrueVerbose( "mergedToken id=" + mergedTokenNode.getToken().getId() +
                       " is not a merged token of baseToken id=" +
                       baseTokenNode.getToken().getId(),
                       (mergedTokenNode.getToken().getSlotId().equals
                        ( baseTokenNode.getToken().getSlotId())), "not ");

//     System.err.println( "baseTokenNode.getToolTipText() " + baseTokenNode.getToolTipText());
//     System.err.println( "baseTokenNode.getText() " + baseTokenNode.getText());
    String toolTipText = baseTokenNode.getToolTipText();
    String labelText = baseTokenNode.getText();
    String predSubString = baseTokenNode.getPredicateName();
    String predParamSubString = predSubString + " (";
    String slotSubString = "slot key=" + baseTokenNode.getToken().getSlotId().toString();
    String tokenSubString = "key=" + baseTokenNode.getToken().getId().toString();
    assertTrueVerbose( "TemporalExtentView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + predParamSubString + "'",
                       (toolTipText.indexOf( predParamSubString) >= 0), "not ");
    assertTrueVerbose( "TemporalExtentView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + slotSubString + "'",
                       (toolTipText.indexOf( slotSubString) >= 0), "not ");
    assertTrueVerbose( "TemporalExtentView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") label does not contain '" + tokenSubString + "'",
                       (labelText.indexOf( tokenSubString) >= 0), "not ");
    assertTrueVerbose( "TemporalExtentView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") label does not contain '" + predSubString + "'",
                       (labelText.indexOf( predSubString) >= 0), "not ");

//     System.err.println( "resTransactionNode.getToolTipText() " +
//                         resTransactionNode.getToolTipText());
//     System.err.println( "resTransactionNode.getText() " + resTransactionNode.getText());
    toolTipText = resTransactionNode.getToolTipText();
    labelText = resTransactionNode.getText();
    String nameSubString = resTransactionNode.getToken().getName();
    String resourceSubString = "key=" + resTransactionNode.getToken().getId().toString();
    assertTrueVerbose( "TemporalExtentView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       nameSubString + "'", (toolTipText.indexOf( nameSubString) >= 0),
                       "not ");
    assertTrueVerbose( "TemporalExtentView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       resourceSubString + "'", (labelText.indexOf( resourceSubString) >= 0),
                       "not ");
    assertTrueVerbose( "TemporalExtentView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       nameSubString + "'", (labelText.indexOf( nameSubString) >= 0), "not ");
    
    Iterator markBridgeItr = baseTokenNode.getMarkAndBridgeList().iterator();
    int markBridgeCnt = 0;
    int earliestStartTime = 0, latestStartTime = 0, earliestEndTime = 0;
    int latestEndTime = 0, minDurationTime = 0, maxDurationTime = 0;
    while (markBridgeItr.hasNext()) {
      JGoStroke markOrBridge = (JGoStroke) markBridgeItr.next();
      if (markOrBridge instanceof TemporalNodeTimeMark) {
        TemporalNodeTimeMark timeMark = (TemporalNodeTimeMark) markOrBridge;
        if (timeMark.getType() == TemporalNode.EARLIEST_START_TIME_MARK) {
          earliestStartTime = Integer.parseInt( timeMark.getToolTipText());
        } else if (timeMark.getType() == TemporalNode.LATEST_START_TIME_MARK) {
          latestStartTime = Integer.parseInt( timeMark.getToolTipText());
        } else if (timeMark.getType() == TemporalNode.EARLIEST_END_TIME_MARK) {
          earliestEndTime = Integer.parseInt( timeMark.getToolTipText());
        } else if (timeMark.getType() == TemporalNode.LATEST_END_TIME_MARK) {
          latestEndTime = Integer.parseInt( timeMark.getToolTipText());
        }
        markBridgeCnt++;
      } else if (markOrBridge instanceof TemporalNodeDurationBridge) {
        TemporalNodeDurationBridge bridge = (TemporalNodeDurationBridge) markOrBridge;
        String durationString = bridge.getToolTipText();
        durationString = durationString.substring( 1, durationString.length() - 1);
        StringTokenizer durationTokens = new StringTokenizer( durationString, ",");
        minDurationTime = Integer.parseInt( durationTokens.nextToken().replaceAll( " ", ""));
        maxDurationTime = Integer.parseInt( durationTokens.nextToken().replaceAll( " ", ""));
        markBridgeCnt++;
      }
    }
    assertTrueVerbose
      ( "TemporalExtentView baseTokenNode does not show four time marks " +
        " and duration bridge", (markBridgeCnt == 5), "not ");
    assertTrueVerbose
      ( "TemporalExtentView baseTokenNode latestStartTime (" + latestStartTime +
        ") not >= earliestStartTime (" + earliestStartTime + ")",
        (latestStartTime >= earliestStartTime), "not ");
    assertTrueVerbose
      ( "TemporalExtentView baseTokenNode latestEndTime (" + latestEndTime +
        ") not >= earliestEndTime (" + earliestEndTime + ")",
        (latestEndTime >= earliestEndTime), "not ");
    assertTrueVerbose
      ( "TemporalExtentView baseTokenNode maxDurationTime (" + maxDurationTime +
        ") not consistent with " +
        "start/end times", (maxDurationTime == (latestEndTime - earliestStartTime)),
        "not ");
    assertTrueVerbose
      ( "TemporalExtentView baseTokenNode minDurationTime (" + minDurationTime +
        ")  not consistent with " + "start/end times",
        (minDurationTime == (earliestEndTime - latestStartTime)), "not ");
  } // end planViz10TempExt

  public void planViz10Timeline( TimelineView timelineView, int stepNumber,
                                 PwPlanningSequence planSeq,  PwPartialPlan partialPlan)
    throws Exception {
    ViewGenerics.raiseFrame( timelineView.getViewFrame());
    // try{Thread.sleep(6000);}catch(Exception e){}
    TimelineViewTimelineNode theTimelineNode = null;
    TimelineTokenNode theFreeTokenNode = null;
    SlotNode theSlotNode = null;
    SlotNode theEmptySlotNode = null;
    int numTimelineNodes = 0, numSlotNodes = 0, numFreeTokenNodes = 0;
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      if (theTimelineNode == null) {
        theTimelineNode = timelineNode;
      }
      numTimelineNodes++;
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        if ((theSlotNode == null) && (slotNode.getSlot().getTokenList().size() > 0)) {
          theSlotNode = slotNode;
        }
        if ((theEmptySlotNode == null) && (slotNode.getSlot().getTokenList().size() == 0)) {
          theEmptySlotNode = slotNode;
        }
        numSlotNodes++;
      }
    }
    Iterator freeTokenNodeItr = timelineView.getFreeTokenNodeList().iterator();
    while (freeTokenNodeItr.hasNext()) {
      TimelineTokenNode freeTokenNode = (TimelineTokenNode) freeTokenNodeItr.next();
      if (theFreeTokenNode == null) {
        theFreeTokenNode = freeTokenNode;
      }
      numFreeTokenNodes++;
    }
    assertNotNullVerbose( "Did not find TimelineView timelineNode (TimelineViewTimelineNode)",
                          theTimelineNode, "not ");
    assertNotNullVerbose( "Did not find TimelineView slotNode (SlotNode)",
                          theSlotNode, "not ");
    assertNotNullVerbose( "Did not find TimelineView emptySlotNode (SlotNode)",
                          theEmptySlotNode, "not ");
    assertNotNullVerbose( "Did not find TimelineView freeTokenNode (TimelineViewTokenNode)",
                          theFreeTokenNode, "not ");
    assertTrueVerbose( "Number of partial plan timelines (" +
                       partialPlan.getTimelineList().size() +
                       ") not equal to number of TimelineView timeline nodes ("
                       + numTimelineNodes + ")",
                       (partialPlan.getTimelineList().size() == numTimelineNodes), "not ");
    assertTrueVerbose( "Number of partial plan slots (" + partialPlan.getSlotList().size() +
                       ") not equal to number of TimelineView slot nodes (" + numSlotNodes + ")",
                       (partialPlan.getSlotList().size() == numSlotNodes), "not ");
    assertTrueVerbose( "Number of partial plan free tokens (" +
                       partialPlan.getFreeTokenList().size() +
                       ") not equal to number of TimelineView free token nodes (" +
                       + numFreeTokenNodes + ")",
                       (partialPlan.getFreeTokenList().size() == numFreeTokenNodes), "not ");

//     System.err.println( "theTimelineNode labelText " + theTimelineNode.getText());
//     System.err.println( "theTimelineNode toolTipText " + theTimelineNode.getToolTipText());
    String labelText = theTimelineNode.getText();
    String toolTipText = theTimelineNode.getToolTipText();
    String nameString = theTimelineNode.getTimeline().getParent().getName() + " : " +
      theTimelineNode.getTimeline().getName();
    String nameSubString = theTimelineNode.getTimeline().getName();
    String keySubString = "key=" + theTimelineNode.getTimeline().getId().toString();
    assertTrueVerbose( "TimelineView timelineNode (" +
                       theTimelineNode.getTimeline().getId().toString() +
                       ") label does not contain '" +
                       nameString + "'", (labelText.indexOf( nameString) >= 0), "not ");
    assertTrueVerbose( "TimelineView timelineNode (" +
                       theTimelineNode.getTimeline().getId().toString() +
                       ") label does not contain '" +
                       keySubString + "'", (labelText.indexOf( keySubString) >= 0), "not ");
    assertTrueVerbose( "TimelineView timelineNode (" +
                       theTimelineNode.getTimeline().getId().toString() +
                       ") tool tip does not contain '" +
                       nameSubString + "'", (toolTipText.indexOf( nameSubString) >= 0), "not ");

//     System.err.println( "theSlotNode labelText " + theSlotNode.getText());
//     System.err.println( "theSlotNode toolTipText " + theSlotNode.getToolTipText());
    labelText = theSlotNode.getText();
    toolTipText = theSlotNode.getToolTipText();
    nameSubString = theSlotNode.getPredicateName() + " (" +
      theSlotNode.getSlot().getTokenList().size() + ")";
    keySubString = "slot key=" + theSlotNode.getSlot().getId().toString();
    StringBuffer toolTipStrBuf = new StringBuffer( "");
    NodeGenerics.getSlotNodeToolTipText( theSlotNode.getSlot(), toolTipStrBuf);
    String toolTipStr = toolTipStrBuf.toString();
    toolTipStr = toolTipStr.replaceAll( "<html> ", "");
    toolTipStr = toolTipStr.replaceAll( " </html>", "");
    assertTrueVerbose( "TimelineView slotNode (" +
                       theSlotNode.getSlot().getId().toString() +
                       ") label does not contain '" +
                       nameSubString + "'", (labelText.indexOf( nameSubString) >= 0),
                       "not ");
    assertTrueVerbose( "TimelineView slotNode (" +
                       theSlotNode.getSlot().getId().toString() +
                       ") label does not contain '" +  keySubString + "'",
                       (labelText.indexOf( keySubString) >= 0), "not ");
     assertTrueVerbose( "TimelineView slotNode (" +
                       theSlotNode.getSlot().getId().toString() +
                       ") tooltip does not contain '" +  toolTipStr + "'",
                       (toolTipText.indexOf( toolTipStr) >= 0), "not ");

//     System.err.println( "theEmptySlotNode labelText " + theEmptySlotNode.getText());
//     System.err.println( "theEmptySlotNode toolTipText " + theEmptySlotNode.getToolTipText());
    labelText = theEmptySlotNode.getText();
    toolTipText = theEmptySlotNode.getToolTipText();
    nameString = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
    keySubString = "key=" + theEmptySlotNode.getSlot().getId().toString();
    assertTrueVerbose( "TimelineView empty slotNode (" +
                       theEmptySlotNode.getSlot().getId().toString() +
                       ") label does not contain '" +
                       nameString + "'", (labelText.indexOf( nameString) >= 0), "not ");
    assertTrueVerbose( "TimelineView empty slotNode (" +
                       theEmptySlotNode.getSlot().getId().toString() +
                       ") label does not contain '" +  keySubString + "'",
                       (labelText.indexOf( keySubString) >= 0), "not ");
    // no tool tip

//     System.err.println( "theFreeTokenNode labelText " + theFreeTokenNode.getText());
//     System.err.println( "theFreeTokenNode toolTipText " + theFreeTokenNode.getToolTipText());
    labelText = theFreeTokenNode.getText();
    toolTipText = theFreeTokenNode.getToolTipText();
    nameString = theFreeTokenNode.getToken().getPredicateName();
    keySubString = "key=" + theFreeTokenNode.getToken().getId().toString();
    toolTipStr = theFreeTokenNode.getToken().toString();
    assertTrueVerbose( "TimelineView freeTokenNode (" +
                       theFreeTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       nameString + "'", (labelText.indexOf( nameString) >= 0), "not ");
    assertTrueVerbose( "TimelineView freeTokenNode (" +
                       theFreeTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +  keySubString + "'",
                       (labelText.indexOf( keySubString) >= 0), "not ");
     assertTrueVerbose( "TimelineView freeTokenNode (" +
                       theFreeTokenNode.getToken().getId().toString() +
                       ") tooltip does not contain '" +  toolTipStr + "'",
                       (toolTipText.indexOf( toolTipStr) >= 0), "not ");
  } // end planViz10Timeline


  private void planViz10TokNet( TokenNetworkView tokenNetworkView, int stepNumber,
                                PwPlanningSequence planSeq,  PwPartialPlan partialPlan)
    throws Exception {
    ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
    // try{Thread.sleep(6000);}catch(Exception e){}
    TokenNode slottedTokenNode = null, freeTokenNode = null, resTransactionNode = null;
    RuleInstanceNode ruleInstanceNode = null;
    int numSlottedTokenNodes = 0, numFreeTokenNodes = 0, numResTransactionNodes = 0;
    int numRuleInstanceNodes = 0;
    Iterator tokenNodeKeyItr = tokenNetworkView.getTokenNodeKeyList().iterator();
    while (tokenNodeKeyItr.hasNext()) {
      TokenNode tokenNode = tokenNetworkView.getTokenNode( (Integer) tokenNodeKeyItr.next());
      if (tokenNode.getToken() instanceof PwResourceTransaction) {
        if (resTransactionNode == null) {
          resTransactionNode = tokenNode;
        }
        numResTransactionNodes++;
      } else if (tokenNode.getToken() instanceof PwToken) {
        PwToken token = (PwToken) tokenNode.getToken();
        if (token.isSlotted()) {
          if (slottedTokenNode == null) {
            slottedTokenNode = tokenNode;
          }
          numSlottedTokenNodes++;
        }
        if (token.isFree()) {
          if (freeTokenNode == null) {
            freeTokenNode = tokenNode;
          }
          numFreeTokenNodes++;
        }
      }
    }
    Iterator ruleInstKeyItr = tokenNetworkView.getRuleInstanceNodeKeyList().iterator();
    while (ruleInstKeyItr.hasNext()) {
      RuleInstanceNode ruleInstNode =
        tokenNetworkView.getRuleInstanceNode( (Integer) ruleInstKeyItr.next());
        if (ruleInstanceNode == null) {
          ruleInstanceNode = ruleInstNode;
        }
        numRuleInstanceNodes++;
    }
       
//     System.err.println( "numSlottedTokenNodes " + numSlottedTokenNodes +
//                         "numFreeTokenNodes " + numFreeTokenNodes +
//                         " numResTransactionNodes " + numResTransactionNodes);
//     System.err.println( "pp tokenCnt " + partialPlan.getTokenList().size() +
//                         " pp resTrans " + partialPlan.getResTransactionList().size());
    assertNotNullVerbose( "Did not find TokenNetworkView slotted TokenNode (TokenNode)",
                          slottedTokenNode, "not ");
    assertNotNullVerbose( "Did not find TokenNetworkView free TokenNode (TokenNode)",
                          freeTokenNode, "not ");
    assertNotNullVerbose( "Did not find TokenNetworkView resTransactionNode (TokenNode)",
                          resTransactionNode, "not ");
    assertNotNullVerbose( "Did not find TokenNetworkView ruleInstanceNode (RuleInstanceNode)",
                          ruleInstanceNode, "not ");
    assertTrueVerbose( "Number of partial plan interval tokens and resource transactions (" +
                       partialPlan.getTokenList().size() +
                       ") not equal to number of TokenNetworkView slotted token, " +
                       "free token, and resource transaction nodes (" +
                       (numSlottedTokenNodes + numFreeTokenNodes + numResTransactionNodes) +
                       ")", (partialPlan.getTokenList().size() ==
                             (numSlottedTokenNodes + numFreeTokenNodes +
                              numResTransactionNodes)), "not ");
    assertTrueVerbose
      ( "Number of partial plan slotted interval tokens (" +
        (partialPlan.getTokenList().size() - partialPlan.getFreeTokenList().size() -
         partialPlan.getResTransactionList().size()) +
        ") not equal to number of TokenNetworkView slotted interval token nodes (" +
        numSlottedTokenNodes + ")", ((partialPlan.getTokenList().size() -
                                      partialPlan.getFreeTokenList().size() -
                                      partialPlan.getResTransactionList().size()) ==
                                     numSlottedTokenNodes), "not ");
    assertTrueVerbose
      ( "Number of partial plan free interval tokens (" +
        partialPlan.getFreeTokenList().size() +
        ") not equal to number of TokenNetworkView slotted interval token nodes (" +
        numFreeTokenNodes + ")", (partialPlan.getFreeTokenList().size() ==
                                     numFreeTokenNodes), "not ");
    assertTrueVerbose( "Number of partial plan resource transactions (" +
                       partialPlan.getResTransactionList().size() +
                       ") not equal to number of TokenNetworkView resource transaction nodes (" +
                       numResTransactionNodes + ")",
                       partialPlan.getResTransactionList().size() ==
                       numResTransactionNodes, "not ");
    assertTrueVerbose( "Number of partial plan rule instances (" +
                       partialPlan.getRuleInstanceList().size() +
                       ") not equal to number of TokenNetworkView rule instance nodes (" +
                       numRuleInstanceNodes + ")",
                       partialPlan.getRuleInstanceList().size() ==
                       numRuleInstanceNodes, "not ");
 
    System.err.println( "slottedTokenNode labelText " + slottedTokenNode.getText());
    System.err.println( "slottedTokenNode toolTipText " + slottedTokenNode.getToolTipText());
    String labelText = slottedTokenNode.getText();
    String toolTipText = slottedTokenNode.getToolTipText();
    String predArgsString = slottedTokenNode.getToken().toString();
    String predString = slottedTokenNode.getToken().getPredicateName();
    String keyString = "key=" + slottedTokenNode.getToken().getId().toString();
    String slotKeyString = "slot key=" + slottedTokenNode.getToken().getSlotId().toString();
    assertTrueVerbose( "TokenNetworkView slotted token node (" +
                       slottedTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       predString + "'", (labelText.indexOf( predString) >= 0), "not ");
    assertTrueVerbose( "TokenNetworkView slotted token node (" +
                       slottedTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       keyString + "'", (labelText.indexOf( keyString) >= 0), "not ");
    assertTrueVerbose( "TokenNetworkView slotted token node (" +
                       slottedTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       predArgsString + "'", (toolTipText.indexOf( predArgsString) >= 0),
                       "not ");
    assertTrueVerbose( "TokenNetworkView slotted token node (" +
                       slottedTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       slotKeyString + "'", (toolTipText.indexOf( slotKeyString) >= 0),
                       "not ");

    System.err.println( "freeTokenNode labelText " + freeTokenNode.getText());
    System.err.println( "freeTokenNode toolTipText " + freeTokenNode.getToolTipText());
    labelText = freeTokenNode.getText();
    toolTipText = freeTokenNode.getToolTipText();
    predArgsString = freeTokenNode.getToken().toString();
    predString = freeTokenNode.getToken().getPredicateName();
    keyString = "key=" + freeTokenNode.getToken().getId().toString();
    slotKeyString = "slot key=";
    assertTrueVerbose( "TokenNetworkView free token node (" +
                       freeTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       predString + "'", (labelText.indexOf( predString) >= 0), "not ");
    assertTrueVerbose( "TokenNetworkView free token node (" +
                       freeTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       keyString + "'", (labelText.indexOf( keyString) >= 0), "not ");
    assertTrueVerbose( "TokenNetworkView free token node (" +
                       freeTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       predArgsString + "'", (toolTipText.indexOf( predArgsString) >= 0),
                       "not ");
    assertFalseVerbose( "TokenNetworkView free token node (" +
                       freeTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       slotKeyString + "'", (toolTipText.indexOf( slotKeyString) >= 0),
                       "not ");

    System.err.println( "resTransactionNode labelText " + resTransactionNode.getText());
    System.err.println( "resTransactionNode toolTipText " + resTransactionNode.getToolTipText());
    labelText = resTransactionNode.getText();
    toolTipText = resTransactionNode.getToolTipText();
    predArgsString = resTransactionNode.getToken().toString();
    predString = resTransactionNode.getToken().getPredicateName();
    keyString = "key=" + resTransactionNode.getToken().getId().toString();
    slotKeyString = "slot key=";
    assertTrueVerbose( "TokenNetworkView resource transaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       predString + "'", (labelText.indexOf( predString) >= 0), "not ");
    assertTrueVerbose( "TokenNetworkView resource transaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       keyString + "'", (labelText.indexOf( keyString) >= 0), "not ");
    assertTrueVerbose( "TokenNetworkView resource transaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       predArgsString + "'", (toolTipText.indexOf( predArgsString) >= 0),
                       "not ");
    assertFalseVerbose( "TokenNetworkView resource transaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       slotKeyString + "'", (toolTipText.indexOf( slotKeyString) >= 0),
                       "not ");

    System.err.println( "ruleInstanceNode labelText " + ruleInstanceNode.getText());
    System.err.println( "ruleInstanceNode toolTipText " + ruleInstanceNode.getToolTipText());
    labelText = ruleInstanceNode.getText();
    toolTipText = ruleInstanceNode.getToolTipText();
    keyString = "key=" + ruleInstanceNode.getRuleInstance().getId().toString();
    String ruleString = "rule ";
    assertTrueVerbose( "TokenNetworkView rule instances node (" +
                       ruleInstanceNode.getRuleInstance().getId().toString() +
                       ") label does not contain '" + ruleString + "'",
                       (labelText.indexOf( ruleString) >= 0), "not ");
    assertTrueVerbose( "TokenNetworkView rule instances node (" +
                       ruleInstanceNode.getRuleInstance().getId().toString() +
                       ") label does not contain '" + keyString + "'",
                       (labelText.indexOf( keyString) >= 0), "not ");
  } // end planViz10TokNet

  private void planViz10DBTrans( DBTransactionView dbTransactionView, int stepNumber,
                                 PwPlanningSequence planSeq,  PwPartialPlan partialPlan)
    throws Exception {
    ViewGenerics.raiseFrame( dbTransactionView.getViewFrame());

    try{Thread.sleep(6000);}catch(Exception e){}

    List transactionList = planSeq.getTransactionsList( partialPlan.getId());
    int numTransactions = transactionList.size();
    int numTransactionEntries = dbTransactionView.getDBTransactionList().size();
    assertTrueVerbose
      ( "Number of partial plan step " + stepNumber + " db transactions (" + numTransactions +
        ") not equal to number of DBTransactionView entries (" +
        numTransactionEntries + ")", (numTransactions == numTransactionEntries), "not ");
    List transactionNameList = MySQLDB.queryTransactionNameList();
    TableModel tableModel = ((DBTransactionTable) dbTransactionView.getDBTransactionTable()).
      getTableSorter().getTableModel();
    String transName = null, fieldObjName = null;
    String variableNamePrefix = DbConstants.VARIABLE_ALL_TYPES.substring
      ( 0, DbConstants.VARIABLE_ALL_TYPES.length() - 1);
    List decisionList = null;
    try {
      decisionList = planSeq.getOpenDecisionsForStep( stepNumber);
    } catch ( ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      rnfExcep.printStackTrace();
    }
    int columnCnt = tableModel.getColumnCount() - 1; // last column is empty
    for (int rowIndx = 0; rowIndx < tableModel.getRowCount(); rowIndx++) {
      for (int colIndx = 0; colIndx < columnCnt; colIndx++) {
        String columnName = tableModel.getColumnName( colIndx);
//         System.err.println( "rowIndx " + rowIndx + " colIndx " + colIndx + " columnName " +
//                            columnName + " tableModel " + tableModel);
        String transField =
          ((String) tableModel.getValueAt( rowIndx, colIndx)).replaceAll( " ", "");
        if (columnName.equals( ViewConstants.DB_TRANSACTION_KEY_HEADER)) {
          Integer fieldTransId = new Integer( Integer.parseInt( transField));
          PwDBTransaction ppTrans =
            (PwDBTransaction) CollectionUtils.findFirst
            ( new PwDBTransactionFunctor( fieldTransId), transactionList);
//           assertNotNullVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                                 ViewConstants.DB_TRANSACTION_KEY_HEADER + "' " + fieldTransId +
//                                 " not found", ppTrans, " not");
          assertNotNull( "Transaction entry " + (rowIndx + 1) + " '" +
                         ViewConstants.DB_TRANSACTION_KEY_HEADER + "' " + fieldTransId +
                         " not found", ppTrans);
        } else if (columnName.replaceAll( " ", "").equals
                   ( ViewConstants.DB_TRANSACTION_NAME_HEADER)) {
          transName = transField;
          boolean isValidName = transactionNameList.contains( transName);
//           assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                              ViewConstants.DB_TRANSACTION_NAME_HEADER + "' '" +
//                              transName + "' not found", isValidName, " not");
          assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
                      ViewConstants.DB_TRANSACTION_NAME_HEADER + "' '" +
                      transName + "' not found", isValidName);
        } else if (columnName.equals( ViewConstants.DB_TRANSACTION_SOURCE_HEADER)) {
          boolean isValidSource =
            DbConstants.SOURCE_USER.equals( transField) ||
            DbConstants.SOURCE_SYSTEM.equals( transField) ||
            DbConstants.SOURCE_UNKNOWN.equals( transField);
//           assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                              ViewConstants.DB_TRANSACTION_SOURCE_HEADER + "' '" +
//                              transField + "' not found", isValidSource, " not");
          assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
                      ViewConstants.DB_TRANSACTION_SOURCE_HEADER + "' '" +
                      transField + "' not found", isValidSource);
        } else if (columnName.equals( ViewConstants.DB_TRANSACTION_ENTITY_KEY_HEADER)) {
          Integer objectId = new Integer( Integer.parseInt( transField));
          PwToken tokenObject = partialPlan.getToken( objectId);
          PwConstraint constraintObject = partialPlan.getConstraint( objectId);
          PwVariable variableObject = partialPlan.getVariable( objectId);
          PwDecision decisionObject = null;
          Iterator decisionItr = decisionList.iterator();
          while (decisionItr.hasNext()) {
            PwDecision decision = (PwDecision) decisionItr.next();
            if (decision.getId().equals( objectId)) {
              decisionObject = decision;
            }
          }
//           assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                              ViewConstants.DB_TRANSACTION_ENTITY_KEY_HEADER +
//                              "' '" + objectId + "' not found",
//                              ((tokenObject != null) || (constraintObject != null) ||
//                               (variableObject != null) || (decisionObject != null)),
//                              " not");
          assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
                      ViewConstants.DB_TRANSACTION_ENTITY_KEY_HEADER +
                      "' '" + objectId + "' not found",
                      ((tokenObject != null) || (constraintObject != null) ||
                       (variableObject != null) || (decisionObject != null)));

        } else if (columnName.equals( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER)) {
          int fieldStepNumber = Integer.parseInt( transField);
//           assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                              ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER
//                              + "' '" + fieldStepNumber +
//                              "' not found", (fieldStepNumber == stepNumber), " not");
          assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
                      ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER + "' '" + fieldStepNumber +
                      "' not found", (fieldStepNumber == stepNumber));
        } else if (columnName.equals( ViewConstants.DB_TRANSACTION_PARENT_HEADER)) {
          if (transName.indexOf( variableNamePrefix) >= 0) {
//             assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                                ViewConstants.DB_TRANSACTION_PARENT_HEADER +
//                                "' '" + transField +
//                                "' not non-empty", (! transField.equals( "")), " not");
            assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
                        ViewConstants.DB_TRANSACTION_PARENT_HEADER + "' '" + transField +
                        "' not non-empty", (! transField.equals( "")));
          } else {
//             assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                                ViewConstants.DB_TRANSACTION_PARENT_HEADER +
//                                "' '" + transField +
//                                "' not empty", transField.equals( ""), " not");
            assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
                        ViewConstants.DB_TRANSACTION_PARENT_HEADER + "' '" + transField +
                        "' not empty", transField.equals( ""));
          }
        } else if (columnName.equals( ViewConstants.DB_TRANSACTION_ENTITY_NAME_HEADER)) {
          fieldObjName = transField;
        } else if (columnName.equals( ViewConstants.DB_TRANSACTION_PARAMETER_HEADER)) {
          String fieldParamName = transField;
          if ((transName.indexOf( variableNamePrefix) >= 0) &&
              fieldObjName.equals( DbConstants.PARAMETER_VAR)) {
//             assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                                ViewConstants.DB_TRANSACTION_PARAMETER_HEADER + 
//                                "' '" + fieldParamName +
//                                "' not non-empty", (! fieldParamName.equals( "")), " not");
            assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
                        ViewConstants.DB_TRANSACTION_PARAMETER_HEADER + "' '" +
                        fieldParamName + "' not non-empty", (! fieldParamName.equals( "")));
          } else {
//             assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                                ViewConstants.DB_TRANSACTION_PARAMETER_HEADER + 
//                                "' '" + fieldParamName +
//                                "' not empty", fieldParamName.equals( ""), " not");
            assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
                        ViewConstants.DB_TRANSACTION_PARAMETER_HEADER + "' '" +
                        fieldParamName + "' not empty", fieldParamName.equals( ""));
          }
        } else {
          assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
                             columnName + "' not handled", false, " not");
        }
      }
    }
  } // end planViz10DBTrans

  class PwDBTransactionFunctor implements BooleanFunctor {
    private Integer transId;
    public PwDBTransactionFunctor(Integer transId){ this.transId = transId; }
    public boolean func(Object o){return ((PwDBTransaction)o).getId().equals(transId) ;}
  }


  public void planViz11() throws Exception {
    int stepNumber = 1, seqUrlIndex = 4;
    List viewList = new ArrayList();
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( seqUrlIndex));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    ViewListener viewListener01 = new ViewListenerWait01( this);
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME, viewListener01,
                                       helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME, helper, this);

    List viewListenerList = createViewListenerList();
    String viewMenuItemName = "Open All Views";
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
   viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
   viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
   viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
   viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
   viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
   viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
   viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
   viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
   String viewNameSuffix = PWTestHelper.SEQUENCE_NAME +
     System.getProperty( "file.separator") + "step" + String.valueOf( stepNumber);
   PwPlanningSequence planSeq =
     planWorks.getCurrentProject().getPlanningSequence( (String) sequenceUrls.get( seqUrlIndex));
   PwPartialPlan partialPlan = planSeq.getPartialPlan( stepNumber);

   Integer timelineId = ((PwTimeline) partialPlan.getTimelineList().get( 0)).getId();
   Integer slottedTokenId = ((PwToken) partialPlan.getSlottedTokenList().get( 0)).getId();
   Integer freeTokenId = ((PwToken) partialPlan.getFreeTokenList().get( 0)).getId();
   assertNotNullVerbose( "Did not find timeline id in partial plan",
                         timelineId, "not ");
   assertNotNullVerbose( "Did not find slotted token id in partial plan",
                         slottedTokenId, "not ");
   assertNotNullVerbose( "Did not find free token id in partial plan",
                         freeTokenId, "not ");
   Integer slotId = ((PwSlot) partialPlan.getSlotList().get( 0)).getId();
   assertNotNullVerbose( "Did not find slot id in partial plan", slotId, "not ");
   Integer variableId = null;
   Iterator variableItr = partialPlan.getVariableList().iterator();
   while (variableItr.hasNext()) {
     PwVariable variable = (PwVariable) variableItr.next();
     if (variable.getParent() instanceof PwToken) {
       variableId = variable.getId();
       break;
     }
   }
   assertNotNullVerbose( "Did not find variable id in partial plan", variableId, "not ");
   Integer constraintId = ((PwConstraint) partialPlan.getConstraintList().get( 0)).getId();
   assertNotNullVerbose( "Did not find constraint id in partial plan", constraintId, "not ");
   Integer ruleInstanceId = ((PwRuleInstance) partialPlan.getRuleInstanceList().get( 0)).getId();
   assertNotNullVerbose( "Did not find rule instance id in partial plan",
                         ruleInstanceId, "not ");
   ConstraintNetworkView constraintNetworkView =
     (ConstraintNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.CONSTRAINT_NETWORK_VIEW, viewNameSuffix, this);
   TemporalExtentView temporalExtentView =
     (TemporalExtentView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TEMPORAL_EXTENT_VIEW, viewNameSuffix, this);
   TimelineView timelineView = (TimelineView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, this);
   TokenNetworkView tokenNetworkView = (TokenNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TOKEN_NETWORK_VIEW, viewNameSuffix, this);

   System.err.println( "Method 2 -------------");
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slottedTokenId.toString(),
                                         helper, this);
   assertTrueVerbose( "Constraint Network focus node id is not " + slottedTokenId.toString() +
                      " (slotted token)",
                      constraintNetworkView.getFocusNodeId().equals( slottedTokenId), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slottedTokenId.toString(),
                                         helper, this);
   assertTrueVerbose( "Temporal Extent focus node id is not " + slottedTokenId.toString() +
                      " (slotted token)",
                      temporalExtentView.getFocusNodeId().equals( slottedTokenId), "not ");

   ViewGenerics.raiseFrame( timelineView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( timelineView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slottedTokenId.toString(),
                                         helper, this);
   assertTrueVerbose( "Timeline focus node id is not " + slottedTokenId.toString() +
                      " (slotted token)",
                      timelineView.getFocusNodeId().equals( slottedTokenId), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slottedTokenId.toString(),
                                         helper, this);
   assertTrueVerbose( "Token Network focus node id is not " + slottedTokenId.toString() +
                      " (slotted token)",
                      tokenNetworkView.getFocusNodeId().equals( slottedTokenId), "not ");

   System.err.println( "Method 3 -------------");
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", freeTokenId.toString(),
                                         helper, this);
   assertTrueVerbose( "Constraint Network focus node id is not " + freeTokenId.toString() +
                      " (free token)",
                      constraintNetworkView.getFocusNodeId().equals( freeTokenId), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", freeTokenId.toString(),
                                         helper, this);
   assertTrueVerbose( "Temporal Extent focus node id is not " + freeTokenId.toString() +
                      " (free token)",
                      temporalExtentView.getFocusNodeId().equals( freeTokenId), "not ");

   ViewGenerics.raiseFrame( timelineView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( timelineView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", freeTokenId.toString(),
                                         helper, this);
   assertTrueVerbose( "Timeline focus node id is not " + freeTokenId.toString() +
                      " (free token)",
                      timelineView.getFocusNodeId().equals( freeTokenId), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", freeTokenId.toString(),
                                         helper, this);
   assertTrueVerbose( "Token Network focus node id is not " + freeTokenId.toString() +
                      " (free token)",
                      tokenNetworkView.getFocusNodeId().equals( freeTokenId), "not ");

   ViewGenerics.raiseFrame(timelineView .getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( timelineView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slotId.toString(),
                                         helper, this);
   assertTrueVerbose( "Timeline focus node id is not " + slotId.toString() + " (slot)",
                      timelineView.getFocusNodeId().equals( slotId), "not ");

   PWTestHelper.viewBackgroundItemSelection( timelineView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", timelineId.toString(),
                                         helper, this);
   assertTrueVerbose( "Timeline focus node id is not " + timelineId.toString() + " (timeline)",
                      timelineView.getFocusNodeId().equals( timelineId), "not ");

   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", variableId.toString(),
                                         helper, this);
   assertTrueVerbose( "Constraint Network focus node id is not " + variableId.toString() +
                      " (variable)",
                      constraintNetworkView.getFocusNodeId().equals( variableId), "not ");

   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", constraintId.toString(),
                                         helper, this);
   assertTrueVerbose( "Constraint Network focus node id is not " + constraintId.toString() +
                      " (constraint) - variableNode ! inLayout",
                      constraintNetworkView.getFocusNodeId().equals( constraintId), "not ");
   ConstraintNode constraintNode = (ConstraintNode) constraintNetworkView.getFocusNode();
   VariableNode variableNode = null;
   Iterator varItr = constraintNode.getVariableNodes().iterator();
   while (varItr.hasNext()) {
     VariableNode varNode = (VariableNode)  varItr.next();
     if (varNode.inLayout()) {
       variableNode = varNode;
       break;
     }
   }
   assertNotNullVerbose( "Constraint Network in-layout variable node of constraint node " +
                         "is not found", variableNode, "not ");
   variableNode.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
   flushAWT(); awtSleep();
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", constraintId.toString(),
                                         helper, this);
   assertTrueVerbose( "Constraint Network focus node id is not " + constraintId.toString() +
                      " (constraint) - variableNode inLayout",
                      constraintNetworkView.getFocusNodeId().equals( constraintId), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, "Find by Key", helper,
                                             this);
   PWTestHelper.handleDialogValueEntry( "Find by Key", ruleInstanceId.toString(),
                                         helper, this);
   assertTrueVerbose( "Token Network focus node id is not " + ruleInstanceId.toString() +
                      " (rule instance)",
                      tokenNetworkView.getFocusNodeId().equals( ruleInstanceId), "not ");

   System.err.println( "Method 4 -------------");
   TokenNode slottedTokenNode = null;
   Iterator nodeKeyItr = tokenNetworkView.getTokenNodeKeyList().iterator();
   while (nodeKeyItr.hasNext()) {
     TokenNode tokenNode = tokenNetworkView.getTokenNode( (Integer) nodeKeyItr.next());
     if (tokenNode.getToken().isSlotted()) {
       slottedTokenNode = tokenNode;
       break;
     }
   }
   slottedTokenNode.doMouseClick( MouseEvent.BUTTON3_MASK, slottedTokenNode.getLocation(),
                                  new Point( 0, 0), tokenNetworkView.getJGoView());
   PWTestHelper.selectViewMenuItem( tokenNetworkView, "Set Active Token", helper, this);
   PwToken activeToken = ((PartialPlanViewSet) tokenNetworkView.getViewSet()).getActiveToken();
   assertTrueVerbose( "Slotted token node (id=" + slottedTokenNode.getToken().getId() +
                      ") is not set to active token",
                      activeToken.getId().equals( slottedTokenNode.getToken().getId()), "not ");

   System.err.println( "Method 5 -------------");
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   Integer activeTokenId = activeToken.getId();
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Snap to Active Token",
                                             helper, this);
   assertTrueVerbose( "Constraint Network focus node id is not " +
                      activeTokenId.toString() + " (slotted token)",
                      constraintNetworkView.getFocusNodeId().equals( activeTokenId), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Snap to Active Token",
                                             helper, this);
   assertTrueVerbose( "Temporal Extent focus node id is not " +
                      activeTokenId.toString() + " (slotted token)",
                      temporalExtentView.getFocusNodeId().equals( activeTokenId), "not ");

   ViewGenerics.raiseFrame( timelineView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( timelineView, "Snap to Active Token",
                                             helper, this);
   assertTrueVerbose( "Timeline focus node id is not " +
                      activeTokenId.toString() + " (slotted token)",
                      timelineView.getFocusNodeId().equals( activeTokenId), "not ");

   System.err.println( "Method 6 -------------");
   TokenNode freeTokenNode = null;
   Iterator nodeItr = timelineView.getFreeTokenNodeList().iterator();
   while (nodeItr.hasNext()) {
     TokenNode tokenNode = (TokenNode) nodeItr.next();
     if (tokenNode.getToken().isFree()) {
       freeTokenNode = tokenNode;
       break;
     }
   }
   freeTokenNode.doMouseClick( MouseEvent.BUTTON3_MASK, freeTokenNode.getLocation(),
                                  new Point( 0, 0), timelineView.getJGoView());
   PWTestHelper.selectViewMenuItem( timelineView, "Set Active Token", helper, this);
   activeToken = ((PartialPlanViewSet) timelineView.getViewSet()).getActiveToken();
   assertTrueVerbose( "Free token node (id=" + freeTokenNode.getToken().getId() +
                      ") is not set to active token",
                      activeToken.getId().equals( freeTokenNode.getToken().getId()), "not ");

   System.err.println( "Method 7 -------------");
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   activeTokenId = activeToken.getId();
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Snap to Active Token",
                                             helper, this);
   assertTrueVerbose( "Constraint Network focus node id is not " +
                      activeTokenId.toString() + " (free token)",
                      constraintNetworkView.getFocusNodeId().equals( activeTokenId), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Snap to Active Token",
                                             helper, this);
   assertTrueVerbose( "Temporal Extent focus node id is not " +
                      activeTokenId.toString() + " (free token)",
                      temporalExtentView.getFocusNodeId().equals( activeTokenId), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, "Snap to Active Token",
                                             helper, this);
   assertTrueVerbose( "TokenNetwork focus node id is not " +
                      activeTokenId.toString() + " (free token)",
                      tokenNetworkView.getFocusNodeId().equals( activeTokenId), "not ");

   System.err.println( "Method 8 -------------");
   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   int xLoc = 50, yLoc = 10;
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Set Time Scale Line",
                                             new Point( xLoc, yLoc), helper, this);
   int scaleMarkXLoc =
     ((TemporalExtentView.TimeScaleMark) temporalExtentView.getTimeScaleMark()).getXLoc();
   assertTrueVerbose( "Temporal Extent View time scale mark is not located at x = " +
                      scaleMarkXLoc, (scaleMarkXLoc == xLoc), "not ");
                      
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Hide Node Labels",
                                             new Point( xLoc, yLoc), helper, this); 
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Show Node Labels",
                                             new Point( xLoc, yLoc), helper, this); 
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Show Earliest",
                                             new Point( xLoc, yLoc), helper, this); 
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Show Latest",
                                             new Point( xLoc, yLoc), helper, this); 
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Show Intervals",
                                             new Point( xLoc, yLoc), helper, this); 

   System.err.println( "Method 9 -------------");
   DBTransactionView dbTransactionView =
     ( DBTransactionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.DB_TRANSACTION_VIEW, viewNameSuffix, this);
   ViewGenerics.raiseFrame( dbTransactionView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( dbTransactionView,
                                             "Find Transaction by Entity_Key", helper, this);
   int transIndx = dbTransactionView.getDBTransactionList().size() - 1;
   PwDBTransaction dbTransaction =
     (PwDBTransaction) dbTransactionView.getDBTransactionList().get( transIndx);
   PWTestHelper.handleDialogValueEntry( "Find Transaction by Entity_Key",
                                        dbTransaction.getEntityId().toString(),
                                         helper, this);
   System.err.println( "Method 10 -------------");
   DBTransactionTable dbTable = (DBTransactionTable) dbTransactionView.getDBTransactionTable();
   TableSorter tableSorter = dbTable.getTableSorter();
   for (int colIndx = 0, n = dbTable.getModel().getColumnCount() - 1; colIndx < n; colIndx++) {
//      System.err.println( "sort colIndx " + colIndx + " status " +
//                          tableSorter.getSortingStatus( colIndx));
     String columnName = dbTable.getModel().getColumnName( colIndx);
     assertTrueVerbose( "column " + columnName + " sort status is not NOT_SORTED",
                        (tableSorter.getSortingStatus( colIndx) ==
                         TableSorter.NOT_SORTED), "not ");
     helper.enterClickAndLeave( new JTableHeaderMouseEventData
                                ( this, dbTable.getTableHeader(), colIndx, 1));
     flushAWT(); awtSleep();
     assertTrueVerbose( "column " + columnName + " sort status is not ASCENDING",
                        (tableSorter.getSortingStatus( colIndx) ==
                         TableSorter.ASCENDING), "not ");
     helper.enterClickAndLeave( new JTableHeaderMouseEventData
                                ( this, dbTable.getTableHeader(), colIndx, 1));
     flushAWT(); awtSleep();
     assertTrueVerbose( "column " + columnName + " sort status is not DESCENDING",
                        (tableSorter.getSortingStatus( colIndx) ==
                         TableSorter.DESCENDING), "not ");
   }

   System.err.println( "Method 11 -------------");
   DecisionView decisionView =
     (DecisionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.DECISION_VIEW, viewNameSuffix, this);
   ResourceProfileView resourceProfileView =
     (ResourceProfileView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_PROFILE_VIEW, viewNameSuffix, this);
   ResourceTransactionView resourceTransactionView =
     (ResourceTransactionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_TRANSACTION_VIEW, viewNameSuffix, this);

   String currentViewName = ViewConstants.CONSTRAINT_NETWORK_VIEW;
   PartialPlanView currentView = constraintNetworkView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, this);
 
   currentViewName = ViewConstants.DB_TRANSACTION_VIEW;
   currentView = dbTransactionView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, this);
 
   currentViewName = ViewConstants.DECISION_VIEW;
   currentView = decisionView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, this);
 
   currentViewName = ViewConstants.RESOURCE_PROFILE_VIEW;
   currentView = resourceProfileView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, this);
 
   currentViewName = ViewConstants.RESOURCE_TRANSACTION_VIEW;
   currentView = resourceTransactionView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, this);
 
   currentViewName = ViewConstants.TEMPORAL_EXTENT_VIEW;
   currentView = temporalExtentView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, this);
 
   currentViewName = ViewConstants.TIMELINE_VIEW;
   currentView = timelineView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, this);
 
   currentViewName = ViewConstants.TOKEN_NETWORK_VIEW;
   currentView = tokenNetworkView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, this);
 
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   assertTrueVerbose( ViewConstants.CONSTRAINT_NETWORK_VIEW + " is not selected",
                      (constraintNetworkView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView,
                                             "Raise Content Filter", helper, this);
   String viewName =  ViewConstants.CONTENT_SPEC_TITLE + " for " + viewNameSuffix;
   assertTrueVerbose( viewName + " is not selected",
                      (constraintNetworkView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( resourceProfileView.getViewFrame());
   assertTrueVerbose( ViewConstants.RESOURCE_PROFILE_VIEW + " is not selected",
                      (resourceProfileView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( resourceProfileView,
                                             "Raise Content Filter", helper, this);
   assertTrueVerbose( viewName + " is not selected",
                      (resourceProfileView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( resourceTransactionView.getViewFrame());
   assertTrueVerbose( ViewConstants.RESOURCE_TRANSACTION_VIEW + " is not selected",
                      (resourceTransactionView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( resourceTransactionView,
                                             "Raise Content Filter", helper, this);
   assertTrueVerbose( viewName + " is not selected",
                      (resourceTransactionView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   assertTrueVerbose( ViewConstants.TEMPORAL_EXTENT_VIEW + " is not selected",
                      (temporalExtentView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView,
                                             "Raise Content Filter", helper, this);
   assertTrueVerbose( viewName + " is not selected",
                      (temporalExtentView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( timelineView.getViewFrame());
   assertTrueVerbose( ViewConstants.TIMELINE_VIEW + " is not selected",
                      (timelineView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( timelineView,
                                             "Raise Content Filter", helper, this);
   assertTrueVerbose( viewName + " is not selected",
                      (timelineView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   assertTrueVerbose( ViewConstants.TOKEN_NETWORK_VIEW + " is not selected",
                      (tokenNetworkView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView,
                                             "Raise Content Filter", helper, this);
   assertTrueVerbose( viewName + " is not selected",
                      (tokenNetworkView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   System.err.println( "Method 12 -------------");
   ViewManager viewMgr = PlanWorks.getPlanWorks().getViewManager();
   viewMenuItemName = "Close All Views";
   PWTestHelper.seqStepsViewStepItemSelection
     ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, this);
   ViewSet viewSet = viewMgr.getViewSet( partialPlan);
   assertNullVerbose( "planSeq step " + stepNumber + " does not have 0 views", viewSet, "not ");

   invokeAllViewsSelections( ViewConstants.CONSTRAINT_NETWORK_VIEW,
                             CONSTRAINT_NETWORK_VIEW_INDEX,
                             ViewConstants.DB_TRANSACTION_VIEW, DB_TRANSACTION_VIEW_INDEX,
                             viewNameSuffix, seqStepsView, partialPlan, viewMgr, stepNumber);

   invokeAllViewsSelections( ViewConstants.DB_TRANSACTION_VIEW, DB_TRANSACTION_VIEW_INDEX,
                             ViewConstants.DECISION_VIEW, DECISION_VIEW_INDEX,
                             viewNameSuffix, seqStepsView, partialPlan, viewMgr, stepNumber);

   invokeAllViewsSelections( ViewConstants.DECISION_VIEW, DECISION_VIEW_INDEX,
                             ViewConstants.RESOURCE_PROFILE_VIEW, RESOURCE_PROFILE_VIEW_INDEX,
                             viewNameSuffix, seqStepsView, partialPlan, viewMgr, stepNumber);

   invokeAllViewsSelections( ViewConstants.RESOURCE_PROFILE_VIEW, RESOURCE_PROFILE_VIEW_INDEX,
                             ViewConstants.RESOURCE_TRANSACTION_VIEW,
                             RESOURCE_TRANSACTION_VIEW_INDEX,
                             viewNameSuffix, seqStepsView, partialPlan, viewMgr, stepNumber);

   invokeAllViewsSelections( ViewConstants.RESOURCE_TRANSACTION_VIEW,
                             RESOURCE_TRANSACTION_VIEW_INDEX,
                             ViewConstants.TEMPORAL_EXTENT_VIEW, TEMPORAL_EXTENT_VIEW_INDEX,
                             viewNameSuffix, seqStepsView, partialPlan, viewMgr, stepNumber);

   invokeAllViewsSelections( ViewConstants.TEMPORAL_EXTENT_VIEW, TEMPORAL_EXTENT_VIEW_INDEX,
                             ViewConstants.TIMELINE_VIEW, TIMELINE_VIEW_INDEX,
                             viewNameSuffix, seqStepsView, partialPlan, viewMgr, stepNumber);

   invokeAllViewsSelections( ViewConstants.TIMELINE_VIEW, TIMELINE_VIEW_INDEX,
                             ViewConstants.TOKEN_NETWORK_VIEW, TOKEN_NETWORK_VIEW_INDEX,
                             viewNameSuffix, seqStepsView, partialPlan, viewMgr, stepNumber);

   invokeAllViewsSelections( ViewConstants.TOKEN_NETWORK_VIEW, TOKEN_NETWORK_VIEW_INDEX,
                             ViewConstants.CONSTRAINT_NETWORK_VIEW,
                             CONSTRAINT_NETWORK_VIEW_INDEX,
                             viewNameSuffix, seqStepsView, partialPlan, viewMgr, stepNumber);

   System.err.println( "Method 13 -------------");
   viewListenerList = createViewListenerList();
   viewMenuItemName = "Open All Views";
   PWTestHelper.seqStepsViewStepItemSelection
     ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, this);
   viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
   viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
   viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
   viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
   viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
   viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
   viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
   viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);

   constraintNetworkView = (ConstraintNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.CONSTRAINT_NETWORK_VIEW, viewNameSuffix, this);
   createOverviewWindow( ViewConstants.CONSTRAINT_NETWORK_VIEW, constraintNetworkView,
                         viewNameSuffix);

   resourceProfileView = (ResourceProfileView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_PROFILE_VIEW, viewNameSuffix, this);
   createOverviewWindow( ViewConstants.RESOURCE_PROFILE_VIEW, resourceProfileView,
                         viewNameSuffix);

   resourceTransactionView = (ResourceTransactionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_TRANSACTION_VIEW, viewNameSuffix, this);
   createOverviewWindow( ViewConstants.RESOURCE_TRANSACTION_VIEW, resourceTransactionView,
                         viewNameSuffix);

   temporalExtentView = (TemporalExtentView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TEMPORAL_EXTENT_VIEW, viewNameSuffix, this);
   createOverviewWindow( ViewConstants.TEMPORAL_EXTENT_VIEW, temporalExtentView,
                         viewNameSuffix);

   timelineView = (TimelineView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, this);
   createOverviewWindow( ViewConstants.TIMELINE_VIEW, timelineView,
                         viewNameSuffix);

   tokenNetworkView = (TokenNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TOKEN_NETWORK_VIEW, viewNameSuffix, this);
   createOverviewWindow( ViewConstants.TOKEN_NETWORK_VIEW, tokenNetworkView,
                         viewNameSuffix);


   System.err.println( "Method 14 -------------");
   viewMenuItemName = "Enable Auto Snap";
   PWTestHelper.viewBackgroundItemSelection( timelineView, viewMenuItemName, helper, this);
    TimelineTokenNode theFreeTokenNode = null;
    SlotNode theSlotNode = null;
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        if ((theSlotNode == null) && (slotNode.getSlot().getTokenList().size() > 0)) {
          theSlotNode = slotNode;
        }
      }
    }
    Iterator freeTokenNodeItr = timelineView.getFreeTokenNodeList().iterator();
    while (freeTokenNodeItr.hasNext()) {
      TimelineTokenNode freeTimelineTokenNode = (TimelineTokenNode) freeTokenNodeItr.next();
      if (theFreeTokenNode == null) {
        theFreeTokenNode = freeTimelineTokenNode;
      }
    }
    assertNotNullVerbose( "Did not find TimelineView slotNode (SlotNode)",
                          theSlotNode, "not ");
    assertNotNullVerbose( "Did not find TimelineView freeTokenNode (TimelineViewTokenNode)",
                          theFreeTokenNode, "not ");

    temporalExtentView.getViewFrame().
      setLocation( 0, (int) temporalExtentView.getViewFrame().getLocation().getY());
    ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
    ViewGenerics.raiseFrame( timelineView.getViewFrame());
    theSlotNode.doUncapturedMouseMove( 0, theSlotNode.getLocation(), new Point( 0, 0),
                                       timelineView.getJGoView());
    flushAWT(); awtSleep();
    PwToken baseToken = theSlotNode.getSlot().getBaseToken();
    List mergedTokens = theSlotNode.getSlot().getTokenList();
    mergedTokens.remove( baseToken);
    PwToken mergedToken = (PwToken) mergedTokens.get( 0);
    activeToken = ((PartialPlanViewSet) temporalExtentView.getViewSet()).getActiveToken();
    assertTrueVerbose( "Base Token id=" + baseToken.getId() + " is not the active token",
                       activeToken.getId().equals( baseToken.getId()), "not ");
    assertTrueVerbose( "Base Token id=" + baseToken.getId() +
                       " is not the focus token node in the Temporal Extent View",
                       temporalExtentView.getFocusNodeId().equals( baseToken.getId()), "not ");
    List secondaryTokens = ((PartialPlanViewSet) temporalExtentView.getViewSet()).
      getSecondaryTokens();
    assertTrueVerbose( "Merged Token id=" + mergedToken.getId() +
                       " is not the secondary token node in the Temporal Extent View",
                       secondaryTokens.contains( mergedToken), "not ");

    theFreeTokenNode.doUncapturedMouseMove( 0, theFreeTokenNode.getLocation(), new Point( 0, 0),
                                       timelineView.getJGoView());
    flushAWT(); awtSleep();
    PwToken freeToken = theFreeTokenNode.getToken();
    activeToken = ((PartialPlanViewSet) temporalExtentView.getViewSet()).getActiveToken();
    assertTrueVerbose( "Free Token id=" + freeToken.getId() + " is not the active token",
                       activeToken.getId().equals( freeToken.getId()), "not ");
    assertTrueVerbose( "Free Token id=" + freeToken.getId() +
                       " is not the focus token node in the Temporal Extent View",
                       temporalExtentView.getFocusNodeId().equals( freeToken.getId()), "not ");

   System.err.println( "Method 15 -------------");
   int maxRuleNodes = 4, viewNum = 1;
   List keyList = tokenNetworkView.getRuleInstanceNodeKeyList();
   viewListener01 = new ViewListenerWait01( this);
   for (int i = 0; i < maxRuleNodes; i++) {
     RuleInstanceNode ruleNode =
       tokenNetworkView.getRuleInstanceNode( (Integer) keyList.get( i));
     ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
     viewListener01.reset();
     ruleNode.doMouseClickWithListener( MouseEvent.BUTTON3_MASK, ruleNode.getLocation(),
                                        new Point( 0, 0), tokenNetworkView.getJGoView(),
                                        viewListener01);
     viewMenuItemName = "Open Rule Instance View";
     PWTestHelper.selectViewMenuItem( tokenNetworkView, viewMenuItemName, helper, this);
     viewListener01.viewWait();
     RuleInstanceView ruleInstanceView = (RuleInstanceView) PWTestHelper.getPartialPlanView
       ( ViewConstants.RULE_INSTANCE_VIEW, viewNameSuffix + " - " + viewNum, this);
     flushAWT(); awtSleep();
     viewNum++;
   }

   System.err.println( "Method 16 -------------");
   viewMenuItemName = "Close Rule Instance Views";
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, viewMenuItemName, helper, this);
   viewNum = 1;
   viewSet = viewMgr.getViewSet( partialPlan);
   for (int i = 0; i < maxRuleNodes; i++) {
     String ruleInstanceViewName =
       ViewConstants.RULE_INSTANCE_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix +
       " - " + viewNum;
     assertFalseVerbose( "'" + ruleInstanceViewName + "' is not closed",
                         viewSet.doesViewFrameExist( ruleInstanceViewName), "not ");
     viewNum++;
   }

   // try{Thread.sleep(4000);}catch(Exception e){}

   PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

   System.err.println( "\nPLANVIZ_11 COMPLETED\n");
   try{Thread.sleep(1000);}catch(Exception e){}
  } // end planViz11

  private void invokeAllViewsSelections( String viewName1, int viewIndx1, String viewName2,
                                         int viewIndx2, String viewNameSuffix,
                                         SequenceStepsView seqStepsView,
                                         PwPartialPlan partialPlan,
                                         ViewManager viewMgr, int stepNumber)
    throws Exception {
   List viewListenerList = createViewListenerList();

   String viewMenuItemName = "Open " + viewName1;
   PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                               viewMenuItemName, viewListenerList, helper, this);
   viewListenerListWait( viewIndx1, viewListenerList);
   viewMenuItemName = "Open " + viewName2;
   PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                               viewMenuItemName, viewListenerList, helper, this);
   viewListenerListWait( viewIndx2, viewListenerList);
   int numViews = viewMgr.getViewSet( partialPlan).getViews().keySet().size();
   assertTrueVerbose( "planSeq step " + stepNumber + " does not have 2 views",
                      (numViews == 2), "not ");
   PartialPlanView view1 =
     (PartialPlanView) PWTestHelper.getPartialPlanView( viewName1, viewNameSuffix, this);
   PartialPlanView view2 =
     (PartialPlanView) PWTestHelper.getPartialPlanView( viewName2, viewNameSuffix, this);

   viewMenuItemName = "Hide All Views";
   PWTestHelper.viewBackgroundItemSelection( view1, viewMenuItemName, viewListenerList,
                                             helper, this);
   boolean isIconified = view1.getViewFrame().isIcon();
   assertTrueVerbose( viewName1 + " is not iconified", isIconified, "not ");

   PWTestHelper.selectWindow( viewName1.replaceAll( " ", "") + " of " + viewNameSuffix,
                              helper, this);

   isIconified = view1.getViewFrame().isIcon();
   assertTrueVerbose( viewName1 + " is not shown", (isIconified == false), "not ");

   viewMenuItemName = "Show All Views";
   PWTestHelper.viewBackgroundItemSelection( view1, viewMenuItemName, viewListenerList,
                                             helper, this);
   isIconified = view2.getViewFrame().isIcon();
   assertTrueVerbose( viewName2 + " is not shown", (isIconified == false), "not ");

   viewMenuItemName = "Open All Views";
   PWTestHelper.viewBackgroundItemSelection( view1, viewMenuItemName, viewListenerList,
                                             helper, this);
   waitForAllViews( viewName1, viewListenerList);
   numViews = viewMgr.getViewSet( partialPlan).getViews().keySet().size();
   int expectedNumViews = PlanWorks.PARTIAL_PLAN_VIEW_LIST.size();
   assertTrueVerbose( "planSeq step " + stepNumber + " does not have " +
                      expectedNumViews + " views", (numViews == expectedNumViews), "not ");

   viewMenuItemName = "Close All Views";
   PWTestHelper.viewBackgroundItemSelection( view1, viewMenuItemName, viewListenerList,
                                             helper, this);
   ViewSet viewSet = viewMgr.getViewSet( partialPlan);
   assertNullVerbose( "planSeq step " + stepNumber + " does not have 0 views", viewSet, "not ");
  } // end invokeAllViewsSelections

  private void waitForAllViews( String viewName, List viewListenerList) throws Exception {
    if (viewName.equals( ViewConstants.CONSTRAINT_NETWORK_VIEW)) {
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.DB_TRANSACTION_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.DECISION_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.RESOURCE_PROFILE_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.RESOURCE_TRANSACTION_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.TEMPORAL_EXTENT_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.TIMELINE_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.TOKEN_NETWORK_VIEW)) {
      viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
    }
  } // end waitForAllViews

  private void createOverviewWindow( String viewName, PartialPlanView view,
                                     String viewNameSuffix) throws Exception {
    String viewMenuItemName = "Overview Window";
    PWTestHelper.viewBackgroundItemSelection( view, viewMenuItemName, helper, this);
    String overviewViewName = Utilities.trimView( viewName).replaceAll( " ", "") +
      ViewConstants.OVERVIEW_TITLE + viewNameSuffix;
      VizViewOverview viewOverview =
      (VizViewOverview) PWTestHelper.findComponentByName
      ( VizViewOverview.class, overviewViewName, Finder.OP_EQUALS);
    assertNotNullVerbose( overviewViewName + " not found", viewOverview, "not ");
  } // end createOverviewWindow


  public void planViz12() throws Exception {
    int stepNumber = 1, seqUrlIndex = 4;
    List viewList = new ArrayList();
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( seqUrlIndex));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    ViewListener viewListener01 = new ViewListenerWait01( this);
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME, viewListener01,
                                       helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME, helper, this);

    List viewListenerList = createViewListenerList();
    String viewMenuItemName = "Open All Views";
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, this);
   viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
   viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
   viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
   viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
   viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
   viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
   viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
   viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
   String viewNameSuffix = PWTestHelper.SEQUENCE_NAME +
     System.getProperty( "file.separator") + "step" + String.valueOf( stepNumber);
   PwPlanningSequence planSeq =
     planWorks.getCurrentProject().getPlanningSequence( (String) sequenceUrls.get( seqUrlIndex));
   PwPartialPlan partialPlan = planSeq.getPartialPlan( stepNumber);

   ViewSet viewSet = PlanWorks.getPlanWorks().getViewManager().getViewSet( partialPlan);
   DBTransactionView dbTransactionView =
     (DBTransactionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.DB_TRANSACTION_VIEW, viewNameSuffix, this);
   dbTransactionView.getViewFrame().setClosed( true);
   DecisionView decisionView =
     (DecisionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.DECISION_VIEW, viewNameSuffix, this);
   decisionView.getViewFrame().setClosed( true);
   ResourceProfileView resourceProfileView =
     (ResourceProfileView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_PROFILE_VIEW, viewNameSuffix, this);
   resourceProfileView.getViewFrame().setClosed( true);
   ResourceTransactionView resourceTransactionView =
     (ResourceTransactionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_TRANSACTION_VIEW, viewNameSuffix, this);
   resourceTransactionView.getViewFrame().setClosed( true);
   JMenuItem tileItem =
     PWTestHelper.findMenuItem( PlanWorks.WINDOW_MENU, PlanWorks.TILE_WINDOWS_MENU_ITEM,
                                helper, this);
   assertNotNullVerbose( "'Window->Tile Windows' not found:", tileItem, "not ");
   helper.enterClickAndLeave( new MouseEventData( this, tileItem));
   this.flushAWT(); this.awtSleep();

   ContentSpecWindow contentSpecWindow =
     PWTestHelper.getContentSpecWindow( viewNameSuffix, helper, this);
   ConstraintNetworkView constraintNetworkView =
     (ConstraintNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.CONSTRAINT_NETWORK_VIEW, viewNameSuffix, this);
   TemporalExtentView temporalExtentView = (TemporalExtentView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TEMPORAL_EXTENT_VIEW, viewNameSuffix, this);
   TimelineView timelineView = (TimelineView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, this);
   TokenNetworkView tokenNetworkView = (TokenNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TOKEN_NETWORK_VIEW, viewNameSuffix, this);

   // validateConstraintsOpen( constraintNetworkView);

   confirmSpecForViews( contentSpecWindow, timelineView);

   // try{Thread.sleep(4000);}catch(Exception e){}

   PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

   System.err.println( "\nPLANVIZ_12 COMPLETED\n");
  } // end planViz12


//   private void validateConstraintsOpen( ConstraintNetworkView constraintNetworkView)
//     throws Exception {
//     List containerNodeList = constraintNetworkView.getContainerNodeList();
//     ConstraintNetworkTokenNode t1303 = null;
//     Iterator containerNodeItr = containerNodeList.iterator();
//     while (containerNodeItr.hasNext()) {
//       VariableContainerNode containNode = (VariableContainerNode) containerNodeItr.next();
//       // System.err.println( "containNode " + containNode.getClass().getName());
//       if (containNode instanceof ConstraintNetworkTokenNode) {
//         ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) containNode;
//         if (((PwToken) tokenNode.getContainer()).getId().equals( new Integer( 1303))) {
//           t1303 = tokenNode;
//           break;
//         }
//       }
//     }
//     assertTrueVerbose( "tokenNode id=1303 not found", (t1303 != null), "not ");
//     t1303.doMouseClick( MouseEvent.BUTTON1_MASK,
//                         new Point( (int) t1303.getLocation().getX(),
//                                    (int) t1303.getLocation().getY()),
//                         new Point( 0, 0), constraintNetworkView.getJGoView());
//     flushAWT(); awtSleep();

//     List variableNodeList = constraintNetworkView.getVariableNodeList();
//     VariableNode v1305 = null;
//     Iterator variableNodeItr = variableNodeList.iterator();
//     while (variableNodeItr.hasNext()) {
//       VariableNode variableNode = (VariableNode) variableNodeItr.next();
//       if (variableNode.getVariable().getId().equals( new Integer( 1305)) &&
//           variableNode.isVisible()) {
//         v1305 = variableNode;
//         break;
//       }
//     }
//     assertTrueVerbose( "variableNode id=1305 not found", (v1305 != null), "not ");
//     v1305.doMouseClick( MouseEvent.BUTTON1_MASK,
//                         new Point( (int) v1305.getLocation().getX(),
//                                    (int) v1305.getLocation().getY()),
//                         new Point( 0, 0), constraintNetworkView.getJGoView());
//     flushAWT(); awtSleep();

//     List constraintNodeList = constraintNetworkView.getConstraintNodeList();
//     ConstraintNode c1323 = null;
//     Iterator constraintNodeItr = constraintNodeList.iterator();
//     while (constraintNodeItr.hasNext()) {
//       ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
//       if (constraintNode.getConstraint().getId().equals( new Integer( 1323)) &&
//           constraintNode.isVisible()) {
//         c1323 = constraintNode;
//         break;
//       }
//     }
//     assertTrueVerbose( "constraintNode id=1323 not found", (c1323 != null), "not ");
//     c1323.doMouseClick( MouseEvent.BUTTON1_MASK,
//                        new Point( (int) c1323.getLocation().getX(),
//                                   (int) c1323.getLocation().getY()),
//                        new Point( 0, 0), constraintNetworkView.getJGoView());
//     flushAWT(); awtSleep();
//   } // end validateConstraintsOpen

  private void confirmSpecForViews( ContentSpecWindow contentSpecWindow,
                                    TimelineView timelineView) throws Exception {
    JButton activateFilterButton = null;
    JButton resetFilterButton = null;
    GroupBox timelineGroup = null;
    GroupBox predicateGroup = null;
    GroupBox timeIntervalGroup = null;
    MergeBox mergeBox = null;
    TokenTypeBox tokenTypeBox = null;
    UniqueKeyGroupBox uniqueKeyGroupBox = null;
    for(int i = 0; i < contentSpecWindow.getComponentCount(); i++) {
      if(contentSpecWindow.getComponent(i) instanceof TimelineGroupBox) {
        timelineGroup = (GroupBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof PredicateGroupBox) {
        predicateGroup = (GroupBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof TimeIntervalGroupBox) {
        timeIntervalGroup = (GroupBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof MergeBox) {
        mergeBox = (MergeBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof TokenTypeBox) {
        tokenTypeBox = (TokenTypeBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof UniqueKeyGroupBox) {
        uniqueKeyGroupBox = (UniqueKeyGroupBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof JPanel) {
        JPanel panel = (JPanel)contentSpecWindow.getComponent(i);
        for(int j = 0; j < panel.getComponentCount(); j++) {
          if(panel.getComponent(j) instanceof JButton) {
            if(((JButton)panel.getComponent(j)).getText().equals("Apply Filter")) {
              activateFilterButton = (JButton) panel.getComponent(j);
            }
            else if(((JButton)panel.getComponent(j)).getText().equals("Reset Filter")) {
              resetFilterButton = (JButton) panel.getComponent(j);
            }
          }
        }
      }
    }
    assertNotNullVerbose("Did not find \"Apply Filter\" button.", activateFilterButton, "not ");
    assertNotNullVerbose("Did not find \"Reset Filter\" button.", resetFilterButton, "not ");
    assertNotNullVerbose("Did not find Timeline GroupBox.", timelineGroup, "not ");
    assertNotNullVerbose("Did not find Predicate GroupBox.", predicateGroup, "not ");
    assertNotNullVerbose("Did not find Time Interval GroupBox.", timeIntervalGroup, "not ");
    assertNotNullVerbose("Did not find Merge Box.", mergeBox, "not ");
    assertNotNullVerbose("Did not find Token Type Box.", tokenTypeBox, "not ");
    assertNotNullVerbose("Did not find Unique Key GroupBox.", uniqueKeyGroupBox, "not ");

    // try{Thread.sleep(4000);}catch(Exception e){}

    System.err.println( "Method 1 -------------");
    // appply predicate 
    int predicateGroupIndx = 0;
    Object[] predicateGroupBoxes = getPredicateGroupBoxes( predicateGroup, predicateGroupIndx);
    JComboBox predKeyBox0 = (JComboBox) predicateGroupBoxes[0];
    NegationCheckBox predNegationBox0 = (NegationCheckBox) predicateGroupBoxes[1];
    LogicComboBox predLogicBox0 = (LogicComboBox) predicateGroupBoxes[2];

    assertNotNullVerbose("Did not find predicate field.", predKeyBox0, "not ");
    assertNotNullVerbose("Did not find negation check box.", predNegationBox0, "not ");
    int predicateNodeIndex = 1;
    predKeyBox0.setSelectedIndex( predicateNodeIndex);
    System.err.println( "selected predicate name " + predKeyBox0.getSelectedItem());
    String selectedPredicateName = (String) predKeyBox0.getSelectedItem();
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();

    List selectedPredicateNameList = new ArrayList();
    selectedPredicateNameList.add( selectedPredicateName);
    boolean isToBeFound = true;
    validateTimelineViewPredicate( selectedPredicateNameList, timelineView, isToBeFound);

   System.err.println( "Method 2 -------------");
    // not predicate
    predNegationBox0.setSelected(true);
    System.err.println( "selected NOT 'selected predicate name'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();

    isToBeFound = false;
    validateTimelineViewPredicate( selectedPredicateNameList, timelineView, isToBeFound);

    System.err.println( "Method 3 -------------");
    predicateGroupIndx = 1;
    predicateGroupBoxes = getPredicateGroupBoxes( predicateGroup, predicateGroupIndx);
    JComboBox predKeyBox1 = (JComboBox) predicateGroupBoxes[0];
    NegationCheckBox predNegationBox1 = (NegationCheckBox) predicateGroupBoxes[1];
    LogicComboBox predLogicBox1 = (LogicComboBox) predicateGroupBoxes[2];
    predLogicBox1.setSelectedIndex( 1);
    System.err.println( "selected predLogicBox1 " + predLogicBox1.getSelectedItem());
    predNegationBox1.setSelected( true);
    System.err.println( "selected NOT 'selected predicate name 1'");
    predicateNodeIndex = 10;
    predKeyBox1.setSelectedIndex( predicateNodeIndex);
    System.err.println( "selected predicate name 1 " + predKeyBox1.getSelectedItem());
    String selectedPredicateName1 = (String) predKeyBox1.getSelectedItem();
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();

    isToBeFound = false;
    selectedPredicateNameList.add( selectedPredicateName1);
    validateTimelineViewPredicate( selectedPredicateNameList, timelineView, isToBeFound);

    System.err.println( "Method 4 -------------");
    predLogicBox1.setSelectedIndex( 2);
    System.err.println( "selected predLogicBox1 " + predLogicBox1.getSelectedItem());
    predNegationBox0.setSelected( false);
    System.err.println( "inverted NOT 'selected predicate name 0'");
    predNegationBox1.setSelected( false);
    System.err.println( "inverted NOT 'selected predicate name 1'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();
    isToBeFound = true;
    validateTimelineViewPredicate( selectedPredicateNameList, timelineView, isToBeFound);

    System.err.println( "Method 5 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(this, resetFilterButton));
    System.err.println( "Reset Filter");
    flushAWT(); awtSleep();

    predicateGroupIndx = 0;
    predicateGroupBoxes = getPredicateGroupBoxes( predicateGroup, predicateGroupIndx);
    predKeyBox0 = (JComboBox) predicateGroupBoxes[0];
    predNegationBox0 = (NegationCheckBox) predicateGroupBoxes[1];
    predLogicBox0 = (LogicComboBox) predicateGroupBoxes[2];
    // System.err.println( "predKeyBox0.getSelectedItem() " + predKeyBox0.getSelectedItem());
    assertTrueVerbose("Reset Filter did not reset predicate text box",
                      (predKeyBox0.getSelectedItem().equals( "")), "not ");
    assertTrueVerbose("Reset Filter did not reset predicate negation check box",
                      (! predNegationBox0.isSelected()), "not ");
    assertTrueVerbose("Reset Filter did not reset predicate logic check box",
                      (predLogicBox0.getSelectedItem().equals( "")), "not ");

    System.err.println( "Method 6 -------------");
    // apply timeline
    int timelineGroupIndx = 0;
    Object[] timelineGroupBoxes = getTimelineGroupBoxes( timelineGroup, timelineGroupIndx);
    JComboBox keyBox0 = (JComboBox) timelineGroupBoxes[0];
    NegationCheckBox negationBox0 = (NegationCheckBox) timelineGroupBoxes[1];
    LogicComboBox logicBox0 = (LogicComboBox) timelineGroupBoxes[2];
    assertNotNullVerbose("Did not find timeline field.", keyBox0, "not ");
    assertNotNullVerbose("Did not find negation check box.", negationBox0, "not ");

    int timelineNodeIndex = 3;
    keyBox0.setSelectedIndex( timelineNodeIndex);
    System.err.println( "selected timeline name " + keyBox0.getSelectedItem());
    String selectedTimelineName = (String) keyBox0.getSelectedItem();
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();

    boolean hasSlotsSelected = true;
    List selectedTimelineNameList = new ArrayList();
    selectedTimelineNameList.add( selectedTimelineName);
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected);

    System.err.println( "Method 7 -------------");
    // not timeline 
    negationBox0.setSelected(true);
    System.err.println( "selected NOT 'selected timeline name'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();

    hasSlotsSelected = false;
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected);

    timelineGroupIndx = 1;
    timelineGroupBoxes = getTimelineGroupBoxes( timelineGroup, timelineGroupIndx);
    JComboBox keyBox1 = (JComboBox) timelineGroupBoxes[0];
    NegationCheckBox negationBox1 = (NegationCheckBox) timelineGroupBoxes[1];
    LogicComboBox logicBox1 = (LogicComboBox) timelineGroupBoxes[2];
    logicBox1.setSelectedIndex( 1);
    System.err.println( "selected logicBox1 " + logicBox1.getSelectedItem());
    negationBox1.setSelected( true);
    System.err.println( "selected NOT 'selected timeline name 1'");
    timelineNodeIndex = 1;
    keyBox1.setSelectedIndex( timelineNodeIndex);
    System.err.println( "selected timeline name 1 " + keyBox1.getSelectedItem());
    String selectedTimelineName1 = (String) keyBox1.getSelectedItem();
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();

    selectedTimelineNameList.add( selectedTimelineName1);
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected);

    // AND => OR & invert NOTs
    logicBox1.setSelectedIndex( 2);
    System.err.println( "selected logicBox1 " + logicBox1.getSelectedItem());
    negationBox0.setSelected( false);
    System.err.println( "inverted NOT 'selected timeline name 0'");
    negationBox1.setSelected( false);
    System.err.println( "inverted NOT 'selected timeline name 1'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();
    hasSlotsSelected = true;
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected);

    System.err.println( "Method 8 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(this, resetFilterButton));
    System.err.println( "Reset Filter");
    flushAWT(); awtSleep();

    timelineGroupIndx = 0;
    timelineGroupBoxes = getTimelineGroupBoxes( timelineGroup, timelineGroupIndx);
    keyBox0 = (JComboBox) timelineGroupBoxes[0];
    negationBox0 = (NegationCheckBox) timelineGroupBoxes[1];
    logicBox0 = (LogicComboBox) timelineGroupBoxes[2];
    // System.err.println( "keyBox.getSelectedItem() " + keyBox.getSelectedItem());
    assertTrueVerbose("Reset Filter did not reset timeline text box",
                      (keyBox0.getSelectedItem().equals( "")), "not ");
    assertTrueVerbose("Reset Filter did not reset timeline negation check box",
                      (! negationBox0.isSelected()), "not ");
    assertTrueVerbose("Reset Filter did not reset timeline logic check box",
                      (logicBox0.getSelectedItem().equals( "")), "not ");
    selectedTimelineNameList = new ArrayList();
    selectedTimelineNameList.add( "noTimelineSelected");
    hasSlotsSelected = false;
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected);

    System.err.println( "Method 9 -------------");
    // apply time interval
    int timeIntervalGroupIndx = 0;
    Object[] timeIntervalGroupBoxes = getTimeIntervalGroupBoxes( timeIntervalGroup,
                                                                 timeIntervalGroupIndx);
    JTextField startBox0 = (JTextField) timeIntervalGroupBoxes[0];
    JTextField endBox0 = (JTextField) timeIntervalGroupBoxes[1];
    NegationCheckBox intNegationBox0 = (NegationCheckBox) timeIntervalGroupBoxes[2];
    LogicComboBox intLogicBox0 = (LogicComboBox) timeIntervalGroupBoxes[3];
    assertNotNullVerbose("Did not find timeInterval start field.", startBox0, "not ");
    assertNotNullVerbose("Did not find timeInterval end field.", endBox0, "not ");
    assertNotNullVerbose("Did not find negation check box.", intNegationBox0, "not ");
    assertNotNullVerbose("Did not find logic box.", intLogicBox0, "not ");

    System.err.println( "Method 10 -------------");
    String startStr0 = "40", endStr0 = "60";
    System.err.println( "startStr0 " + startStr0 + " endStr0 " + endStr0);
    startBox0.setText( startStr0);
    endBox0.setText( endStr0);
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();
    int numSlotsToFind = 2;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView);

    intNegationBox0.setSelected( true);
    System.err.println( "selected NOT 'selected time interval 0'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();
    numSlotsToFind = 4;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView);

    timeIntervalGroupIndx = 1;
    timeIntervalGroupBoxes = getTimeIntervalGroupBoxes( timeIntervalGroup, timeIntervalGroupIndx);
    JTextField startBox1 = (JTextField) timeIntervalGroupBoxes[0];
    JTextField endBox1 = (JTextField) timeIntervalGroupBoxes[1];
    NegationCheckBox intNegationBox1 = (NegationCheckBox) timeIntervalGroupBoxes[2];
    LogicComboBox intLogicBox1 = (LogicComboBox) timeIntervalGroupBoxes[3];
    assertNotNullVerbose("Did not find timeInterval start field.", startBox1, "not ");
    assertNotNullVerbose("Did not find timeInterval end field.", endBox1, "not ");
    assertNotNullVerbose("Did not find negation check box.", intNegationBox1, "not ");
    assertNotNullVerbose("Did not find logic box.", intLogicBox1, "not ");

    intLogicBox1.setSelectedIndex( 1);
    System.err.println( "selected intLogicBox1 " + intLogicBox1.getSelectedItem());
    intNegationBox1.setSelected( true);
    System.err.println( "selected NOT 'selected time interval 1'");
    String startStr1 = "80", endStr1 = "100";
    System.err.println( "startStr1 " + startStr1 + " endStr1 " + endStr1);
    startBox1.setText( startStr1);
    endBox1.setText( endStr1);
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();
    numSlotsToFind = 2;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView);

    intLogicBox1.setSelectedIndex( 2);
    System.err.println( "selected intLogicbox1 " + intLogicBox1.getSelectedItem());
    intNegationBox0.setSelected( false);
    System.err.println( "inverted NOT 'selected time interval 0'");
    intNegationBox1.setSelected( false);
    System.err.println( "inverted NOT 'selected time interval 1'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();
    numSlotsToFind = 4;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView);

    System.err.println( "Method 11 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(this, resetFilterButton));
    System.err.println( "Reset Filter");
    flushAWT(); awtSleep();

    timeIntervalGroupIndx = 0;
    timeIntervalGroupBoxes = getTimeIntervalGroupBoxes( timeIntervalGroup,
                                                        timeIntervalGroupIndx);
    startBox0 = (JTextField) timeIntervalGroupBoxes[0];
    endBox0 = (JTextField) timeIntervalGroupBoxes[1];
    intNegationBox0 = (NegationCheckBox) timeIntervalGroupBoxes[2];
    intLogicBox0 = (LogicComboBox) timeIntervalGroupBoxes[3];
    assertTrueVerbose("Reset Filter did not reset time interval start box",
                      (startBox0.getText().equals( "")), "not ");
    assertTrueVerbose("Reset Filter did not reset time interval end box",
                      (endBox0.getText().equals( "")), "not ");
    assertTrueVerbose("Reset Filter did not reset time interval negation check box",
                      (! negationBox0.isSelected()), "not ");
    assertTrueVerbose("Reset Filter did not reset time interval logic check box",
                      (logicBox0.getSelectedItem().equals( "")), "not ");
    numSlotsToFind = 6;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView);

    System.err.println( "Method 12 -------------");
    // "Merge tokens" does not effect the TimelineView
    // mergeBox

    System.err.println( "Method 13 -------------");
    // "View slotted"
    helper.enterClickAndLeave(new MouseEventData(this, tokenTypeBox.getSlottedButton()));
    System.err.println( "Content Filter: 'view slotted' selected");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();
    boolean isBaseTokenOnly = false;
    assertTrueVerbose("TimelineView slotted token count is not non-zero",
                      (getTimelineViewSlottedTokenList( timelineView, "",
                                                        isBaseTokenOnly).size() != 0), "not ");
    assertTrueVerbose("TimelineView free token count is not zero",
                      (getTimelineViewFreeTokenList( timelineView).size() == 0), "not ");
    // "View free tokens"
    helper.enterClickAndLeave(new MouseEventData(this, tokenTypeBox.getFreeTokensButton()));
    System.err.println( "Content Filter: 'view slotted' selected");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();
    assertTrueVerbose("TimelineView slotted token count is not zero",
                      (getTimelineViewSlottedTokenList( timelineView, "",
                                                        isBaseTokenOnly).size() == 0), "not ");
    assertTrueVerbose("TimelineView free token count is not non-zero",
                      (getTimelineViewFreeTokenList( timelineView).size() != 0), "not ");

    System.err.println( "Method 14 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(this, resetFilterButton));
    System.err.println( "Reset Filter");
    flushAWT(); awtSleep();
    assertTrueVerbose("TimelineView slotted token count is not non-zero",
                      (getTimelineViewSlottedTokenList( timelineView, "",
                                                        isBaseTokenOnly).size() != 0), "not ");
    assertTrueVerbose("TimelineView free token count is not non-zero",
                      (getTimelineViewFreeTokenList( timelineView).size() != 0), "not ");

    System.err.println( "Method 15 -------------");
    // require Add
    timelineGroupIndx = 0;
    timelineGroupBoxes = getTimelineGroupBoxes( timelineGroup, timelineGroupIndx);
    keyBox0 = (JComboBox) timelineGroupBoxes[0];
    negationBox0 = (NegationCheckBox) timelineGroupBoxes[1];
    keyBox0.setSelectedIndex( timelineNodeIndex);
    System.err.println( "selected timeline name " + keyBox0.getSelectedItem());
    selectedTimelineName = (String) keyBox0.getSelectedItem();
    negationBox0.setSelected(true);
    System.err.println( "selected NOT 'selected timeline name'");
    int uniqueKeyBoxIndexToFind = 0;
    Object[] uniqueKeyButtons = getUniqueKeyGroupBoxes( uniqueKeyGroupBox,
                                                        uniqueKeyBoxIndexToFind);
    JRadioButton requireButton0 = (JRadioButton) uniqueKeyButtons[0];
    JRadioButton excludeButton0 = (JRadioButton) uniqueKeyButtons[1];
    JTextField keyField0 = (JTextField) uniqueKeyButtons[2];
    JButton addButton0 = (JButton) uniqueKeyButtons[3];
    JButton removeButton0 = (JButton) uniqueKeyButtons[4];
    requireButton0.doClick();
    isBaseTokenOnly = true;
    List slottedTokens = getTimelineViewSlottedTokenList( timelineView, selectedTimelineName,
                                                          isBaseTokenOnly);
    PwToken requiredToken1 = (PwToken) slottedTokens.get( 0);
    PwToken requiredToken2 = (PwToken) slottedTokens.get( 3);
    PWTestHelper.setQueryField( keyField0, requiredToken1.getId().toString(), helper, this);
    addButton0.doClick();

    uniqueKeyBoxIndexToFind = 1;
    uniqueKeyButtons = getUniqueKeyGroupBoxes( uniqueKeyGroupBox, uniqueKeyBoxIndexToFind);
    JRadioButton requireButton1 = (JRadioButton) uniqueKeyButtons[0];
    JRadioButton excludeButton1 = (JRadioButton) uniqueKeyButtons[1];
    JTextField keyField1 = (JTextField) uniqueKeyButtons[2];
    JButton addButton1 = (JButton) uniqueKeyButtons[3];
    JButton removeButton1 = (JButton) uniqueKeyButtons[4];
    requireButton1.doClick();
    PWTestHelper.setQueryField( keyField1, requiredToken2.getId().toString(), helper, this);
    addButton1.doClick();
   
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(this, activateFilterButton));
    flushAWT(); awtSleep();

    isBaseTokenOnly = false;
    slottedTokens = getTimelineViewSlottedTokenList( timelineView, selectedTimelineName,
                                                     isBaseTokenOnly);
    System.err.println( "slottedTokens.size() " + slottedTokens.size());

    // try{Thread.sleep(4000);}catch(Exception e){}

    assertTrueVerbose( "Timeline '" + selectedTimelineName + "' does not show only two tokens",
                       (slottedTokens.size() == 2), "not ");
    assertTrueVerbose( "Timeline '" + selectedTimelineName + "' does not show token '" +
                       requiredToken1 + "'", slottedTokens.contains( requiredToken1), "not ");
    assertTrueVerbose( "Timeline '" + selectedTimelineName + "' does not show token '" +
                       requiredToken2 + "'", slottedTokens.contains( requiredToken2), "not ");

    System.err.println( "Method 16 -------------");
    // exclude Remove
//     JRadioButton excludeButton = uniqueKeyGroupBox.getUniqueKeyBox().getExcludeButton();
//     JButton removeButton = uniqueKeyGroupBox.getUniqueKeyBox().getRemoveButton();
//     keyField = uniqueKeyGroupBox.getUniqueKeyBox().getKeyField();

    System.err.println( "Method 17 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(this, resetFilterButton));
    System.err.println( "Reset Filter");
    flushAWT(); awtSleep();
    
    System.err.println( "Method 18 -------------");
    // Click Mouse-Right on a background area of the Content Filter dialog,
    //  and choose either opening an individual partial plan view or all
    //  the partial plan views.


   } // end confirmSpecForViews

  private Object[] getPredicateGroupBoxes( GroupBox predicateGroup, int predicateBoxIndexToFind) {
    JComboBox keyBox = null;
    NegationCheckBox negationBox = null;
    LogicComboBox logicBox = null;
    int predicateBoxIndex = 0;
    for(int i = 0; i < predicateGroup.getComponentCount(); i++) {
      if(predicateGroup.getComponent(i) instanceof PredicateBox) {
        PredicateBox predicateBox = (PredicateBox) predicateGroup.getComponent(i);
        for(int j = 0; j < predicateBox.getComponentCount(); j++) {
          if(predicateBox.getComponent(j) instanceof LogicComboBox) {
            if (predicateBoxIndex == predicateBoxIndexToFind) {
              // System.err.println("predicateBoxIndex " + predicateBoxIndex);
              for(int k = 0; k < predicateBox.getComponentCount(); k++) {
                // System.err.println(predicateBox.getComponentCount() + ":" + k);
                if(predicateBox.getComponent(k) instanceof NegationCheckBox) {
                  negationBox = (NegationCheckBox) predicateBox.getComponent(k);
                }
                else if (predicateBox.getComponent(k) instanceof LogicComboBox) {
                  logicBox = (LogicComboBox) predicateBox.getComponent(k);
                }
                else if(predicateBox.getComponent(k) instanceof JComboBox) {
                  keyBox = (JComboBox) predicateBox.getComponent(k);
                }
              }
            }
          }
        }
      }
      predicateBoxIndex++;
    }
    return new Object[] { keyBox, negationBox, logicBox};
  } // end getPredicateGroupBoxes

  private Object[] getTimelineGroupBoxes( GroupBox timelineGroup, int timelineBoxIndexToFind) {
    JComboBox keyBox = null;
    NegationCheckBox negationBox = null;
    LogicComboBox logicBox = null;
    int timelineBoxIndex = 0;
    for(int i = 0; i < timelineGroup.getComponentCount(); i++) {
      if(timelineGroup.getComponent(i) instanceof TimelineBox) {
        TimelineBox timelineBox = (TimelineBox) timelineGroup.getComponent(i);
        for(int j = 0; j < timelineBox.getComponentCount(); j++) {
          if(timelineBox.getComponent(j) instanceof LogicComboBox) {
            if (timelineBoxIndex == timelineBoxIndexToFind) {
              // System.err.println("timelineBoxIndex " + timelineBoxIndex);
              for(int k = 0; k < timelineBox.getComponentCount(); k++) {
                // System.err.println(timelineBox.getComponentCount() + ":" + k +
                //                   " class " + timelineBox.getComponent(k).getClass().getName());
                if(timelineBox.getComponent(k) instanceof NegationCheckBox) {
                  negationBox = (NegationCheckBox) timelineBox.getComponent(k);
                }
                else if (timelineBox.getComponent(k) instanceof LogicComboBox) {
                  logicBox = (LogicComboBox) timelineBox.getComponent(k);
                }
                else if(timelineBox.getComponent(k) instanceof JComboBox) {
                  keyBox = (JComboBox) timelineBox.getComponent(k);
                }
              }
            }
          }
        }
      }
      timelineBoxIndex++;
    }
    return new Object[] { keyBox, negationBox, logicBox};
  } // end getTimelineGroupBoxes

  private Object[] getTimeIntervalGroupBoxes( GroupBox timeIntervalGroup,
                                              int timeIntervalBoxIndexToFind) {
    JTextField startBox = null, endBox = null;
    NegationCheckBox negationBox = null;
    LogicComboBox logicBox = null;
    int timeIntervalBoxIndex = 0;
    for(int i = 0; i < timeIntervalGroup.getComponentCount(); i++) {
      if(timeIntervalGroup.getComponent(i) instanceof TimeIntervalBox) {
        TimeIntervalBox tiBox = (TimeIntervalBox) timeIntervalGroup.getComponent(i);
        for(int j = 0; j < tiBox.getComponentCount(); j++) {
          if(tiBox.getComponent(j) instanceof LogicComboBox) {
            if (timeIntervalBoxIndex == timeIntervalBoxIndexToFind) {
              // System.err.println("timeIntervalBoxIndex " + timeIntervalBoxIndex);
              for(int k = 0; k < tiBox.getComponentCount(); k++) {
                // System.err.println(tiBox.getComponentCount() + ":" + k);
                if(tiBox.getComponent(k) instanceof NegationCheckBox) {
                  negationBox = (NegationCheckBox) tiBox.getComponent(k);
                }
                else if (tiBox.getComponent(k) instanceof LogicComboBox) {
                  logicBox = (LogicComboBox) tiBox.getComponent(k);
                }
                else if(tiBox.getComponent(k) instanceof JLabel) {
                  JLabel tempLabel = (JLabel) tiBox.getComponent(k);
                  if(tempLabel.getText().trim().equals("Time Interval Start")) {
                    k++;
                    startBox = (JTextField) tiBox.getComponent(k);
                  }
                  else if(tempLabel.getText().trim().equals("End")) {
                    k++;
                    endBox = (JTextField) tiBox.getComponent(k);
                  }
                }
              }
            }
          }
        }
      }
      timeIntervalBoxIndex++;
    }
    return new Object[] { startBox, endBox, negationBox, logicBox};
  } // end getTimeIntervalGroupBoxes

  private Object[] getUniqueKeyGroupBoxes( GroupBox uniqueKeyGroup, int uniqueKeyBoxIndexToFind) {
    JRadioButton requireButton = null;
    JRadioButton excludeButton = null;
    JTextField keyField = null;
    JButton addButton = null;
    JButton removeButton = null;
    int uniqueKeyBoxIndex = 0;
    for(int i = 0; i < uniqueKeyGroup.getComponentCount(); i++) {
      if(uniqueKeyGroup.getComponent(i) instanceof UniqueKeyBox) {
        UniqueKeyBox ukBox = (UniqueKeyBox) uniqueKeyGroup.getComponent(i);
        if (uniqueKeyBoxIndex == uniqueKeyBoxIndexToFind) {
          System.err.println("uniqueKeyBoxIndex " + uniqueKeyBoxIndex);
          requireButton = ukBox.getRequireButton();
          addButton = ukBox.getAddButton();
          keyField = ukBox.getKeyField();
          excludeButton = ukBox.getExcludeButton();
          removeButton = ukBox.getRemoveButton();
        }
      }
      uniqueKeyBoxIndex++;
    }
    return new Object[] { requireButton, excludeButton, keyField, addButton, removeButton};
  } // end getUniqueKeyGroupBoxes

  private void validateTimelineViewPredicate( List selectedPredicateNameList,
                                              TimelineView timelineView, boolean isToBeFound)
    throws Exception {
    List predFoundInTimeline = new ArrayList();
    for (int i = 0; i < selectedPredicateNameList.size(); i++) {
      predFoundInTimeline.add( null);
    }
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      // System.err.println( "timelineNode " + timelineNode.getLabel().getText());
      int numSlots = 0;
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        numSlots++;
        List tokenList = slotNode.getSlot().getTokenList();
        Iterator tokenItr = tokenList.iterator();
        while (tokenItr.hasNext()) {
          PwToken token = (PwToken) tokenItr.next();
          String predicateName = token.getPredicateName();
          if (selectedPredicateNameList.contains( predicateName)) {
            predFoundInTimeline.set( selectedPredicateNameList.indexOf( predicateName),
                                     timelineNode.getTimeline().getName());
            break;
          }
        }
      }
//       System.err.println( "Timeline " + timelineNode.getTimeline().getName() +
//                           " slots " + numSlots);
    }
    Iterator freeTokenNodeItr = timelineView.getFreeTokenNodeList().iterator();
    while (freeTokenNodeItr.hasNext()) {
      TimelineTokenNode freeTokenNode = (TimelineTokenNode) freeTokenNodeItr.next();
      String predicateName = freeTokenNode.getPredicateName();
      if (selectedPredicateNameList.contains( predicateName)) {
        predFoundInTimeline.set( selectedPredicateNameList.indexOf( predicateName), "free token");
        break;
      }
    }
    Iterator selectedPredItr = selectedPredicateNameList.iterator();
    Iterator foundTimelineItr = predFoundInTimeline.iterator();
    while (selectedPredItr.hasNext()) {
      String foundTimeline = (String) foundTimelineItr.next();
      if (isToBeFound) {
        assertNotNullVerbose( "Predicate " + (String) selectedPredItr.next() +
                              " is not found in timeline " + foundTimeline, foundTimeline, "not ");
      } else {
        assertNullVerbose( "Predicate " + (String) selectedPredItr.next() +
                           " is not absent in all timelines", foundTimeline, "not ");
      }
    }
  } // end validateTimelineViewPredicate

  private void validateTimelineViewTimeline( List selectedTimelineNameList,
                                             TimelineView timelineView,
                                             boolean hasSlotsSelected)
    throws Exception {
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      // System.err.println( "timelineNode " + timelineNode.getLabel().getText());
      boolean hasSlots = ! hasSlotsSelected;
      if (selectedTimelineNameList.contains( timelineNode.getTimeline().getName())) {
        hasSlots = hasSlotsSelected;
      }
      int numSlots = 0;
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        // System.err.println( "  slotNode isVisible " + slotNode.isVisible());
        numSlots++;
      }
      if (hasSlots) {
        assertTrueVerbose( "Timeline " + timelineNode.getTimeline().getName() +
                           " does not have slots", (numSlots != 0), "not ");
      } else {
        assertTrueVerbose( "Timeline " + timelineNode.getTimeline().getName() +
                           " does not have zero slots", (numSlots == 0), "not ");
      }
    }
  } // end validateTimelineViewTimeline

  private void validateTimelineViewTimeInterval( int numSlotsToFind,
                                                 TimelineView timelineView) throws Exception {
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      // System.err.println( "timelineNode " + timelineNode.getLabel().getText());
      int numSlots = 0;
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        // System.err.println( "  slotNode isVisible " + slotNode.isVisible());
        numSlots++;
      }
//       System.err.println( "Timeline " + timelineNode.getTimeline().getName() +
//                           " slots " + numSlots);
      assertTrueVerbose( "Timeline " + timelineNode.getTimeline().getName() +
                         " does not have " + numSlotsToFind + " slots",
                         (numSlots == numSlotsToFind), "not ");
    }
  } // end validateTimelineViewTimeInterval

  private List getTimelineViewSlottedTokenList( TimelineView timelineView, String timelineName,
                                                boolean isBaseTokenOnly) {
    List tokenList = new ArrayList();
    List slotIdList = new ArrayList();
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      if (timelineName.equals( "") ||
          ((! timelineName.equals( "")) &&
           (timelineNode.getTimelineName().indexOf( timelineName) >= 0))) {
        Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
        while (slotNodeItr.hasNext()) {
          PwSlot slot = ((SlotNode) slotNodeItr.next()).getSlot();
          if (isBaseTokenOnly) {
            if ((! slotIdList.contains( slot.getId())) &&
                (slot.getTokenList().size() == 1)) {
              tokenList.add( slot.getBaseToken());
              slotIdList.add( slot.getId());
            }
          } else {
            Iterator tokenItr = slot.getTokenList().iterator();
            while (tokenItr.hasNext()) {
              tokenList.add( (PwToken) tokenItr.next());
            }
          }
        }
      }
    }
    return tokenList;
  } // end getTimelineViewSlottedTokenList

  private List getTimelineViewFreeTokenList( TimelineView timelineView) {
    List tokenList = new ArrayList();
    Iterator freeTokenNodeItr = timelineView.getFreeTokenNodeList().iterator();
    while (freeTokenNodeItr.hasNext()) {
      TimelineTokenNode freeTimelineTokenNode = (TimelineTokenNode) freeTokenNodeItr.next();
      tokenList.add( freeTimelineTokenNode.getToken());
    }
    return tokenList;
  } // end getTimelineViewFreeTokenList




  public void assertTrueVerbose( String failureMsg, boolean condition, String replacement)
    throws AssertionFailedError {
    if (condition) {
      System.err.println( "AssertTrue: " + failureMsg.replaceAll( replacement, ""));
    } else {
      throw new AssertionFailedError( failureMsg);
    }
  } // end assertTrueVerbose

  public void assertFalseVerbose( String failureMsg, boolean condition, String replacement)
    throws AssertionFailedError {
    if (! condition) {
      System.err.println( "AssertFalse: " + failureMsg.replaceAll( replacement, ""));
    } else {
      throw new AssertionFailedError( failureMsg);
    }
  } // end assertFalseVerbose

  public void assertNotNullVerbose( String failureMsg, Object object, String replacement)
    throws AssertionFailedError{
    if (object != null) {
      System.err.println( "AssertNotNull: " + failureMsg.replaceAll( replacement, ""));
    } else {
      throw new AssertionFailedError( failureMsg);
    }
  } // end assertTrueVerbose


  public void assertNullVerbose( String failureMsg, Object object, String replacement)
    throws AssertionFailedError{
    if (object == null) {
      System.err.println( "AssertNull: " + failureMsg.replaceAll( replacement, ""));
    } else {
      throw new AssertionFailedError( failureMsg);
    }
  } // end assertTrueVerbose


  public class ViewListenerWait01 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait01( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait01( false); 
    }
    public void reset() {
      guiTest.setViewListenerWait01( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait01( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait01()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait01 still false");
      }
      flushAWT(); awtSleep();
    } 
  } // end class ViewListenerWait01

  public class ViewListenerWait02 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait02( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait02( false);
    }
    public void reset() {
      guiTest.setViewListenerWait02( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait02( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait02()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait02 still false");
      }
      flushAWT(); awtSleep();
    } 
  } // end class ViewListenerWait02

  public class ViewListenerWait03 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait03( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait03( false);
    }
    public void reset() {
      guiTest.setViewListenerWait03( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait03( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait03()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait03 still false");
      }
      flushAWT(); awtSleep();
    } 
  } // end class ViewListenerWait03

  public class ViewListenerWait04 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait04( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait04( false);
    }
    public void reset() {
      guiTest.setViewListenerWait04( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait04( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait04()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait04 still false");
      }
      flushAWT(); awtSleep();
    } 
  } // end class ViewListenerWait04

  public class ViewListenerWait05 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait05( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait05( false);
    }
    public void reset() {
      guiTest.setViewListenerWait05( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait05( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait05()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait05 still false");
      }
      flushAWT(); awtSleep();
    } 
  } // end class ViewListenerWait05

  public class ViewListenerWait06 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait06( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait06( false);
    }
    public void reset() {
      guiTest.setViewListenerWait06( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait06( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait06()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait06 still false");
      }
      flushAWT(); awtSleep();
    } 
  } // end class ViewListenerWait06

  public class ViewListenerWait07 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait07( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait07( false);
    }
    public void reset() {
      guiTest.setViewListenerWait07( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait07( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait07()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait07 still false");
      }
      flushAWT(); awtSleep();
    } 
  } // end class ViewListenerWait07

  public class ViewListenerWait08 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait08( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait08( false);
    }
    public void reset() {
      guiTest.setViewListenerWait08( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait08( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait08()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait08 still false");
      }
      flushAWT(); awtSleep();
    } 
  } // end class ViewListenerWait08

  // needed for calls to PWTestHelper.seqStepsViewStepItemSelection
  private List createViewListenerList() {
    List viewListenerList = new ArrayList();
    viewListenerList.add( new ViewListenerWait01( this));
    viewListenerList.add( new ViewListenerWait02( this));
    viewListenerList.add( new ViewListenerWait03( this));
    viewListenerList.add( new ViewListenerWait04( this));
    viewListenerList.add( new ViewListenerWait05( this));
    viewListenerList.add( new ViewListenerWait06( this));
    viewListenerList.add( new ViewListenerWait07( this));
    viewListenerList.add( new ViewListenerWait08( this));
    if (viewListenerList.size() != PlanWorks.PARTIAL_PLAN_VIEW_LIST.size()) {
      System.err.println( "createViewListenerList: num listeners not = " +
                           PlanWorks.PARTIAL_PLAN_VIEW_LIST.size());
      System.exit( -1);
    }
    return viewListenerList;
  } // end createViewListenerList

  private void viewListenerListWait( int viewIndex, List viewListenerList) {
    if (viewIndex == CONSTRAINT_NETWORK_VIEW_INDEX) {
      ((ViewListenerWait01)  viewListenerList.get( CONSTRAINT_NETWORK_VIEW_INDEX)).viewWait();
    } else if (viewIndex == DB_TRANSACTION_VIEW_INDEX) {
      ((ViewListenerWait02)  viewListenerList.get( DB_TRANSACTION_VIEW_INDEX)).viewWait();
    } else if (viewIndex == DECISION_VIEW_INDEX) {
      ((ViewListenerWait03)  viewListenerList.get( DECISION_VIEW_INDEX)).viewWait();
    } else if (viewIndex == RESOURCE_PROFILE_VIEW_INDEX) {
      ((ViewListenerWait04)  viewListenerList.get( RESOURCE_PROFILE_VIEW_INDEX)).viewWait();
    } else if (viewIndex == RESOURCE_TRANSACTION_VIEW_INDEX) {
      ((ViewListenerWait05)  viewListenerList.get( RESOURCE_TRANSACTION_VIEW_INDEX)).viewWait();
    } else if (viewIndex == TEMPORAL_EXTENT_VIEW_INDEX) {
      ((ViewListenerWait06)  viewListenerList.get( TEMPORAL_EXTENT_VIEW_INDEX)).viewWait();
    } else if (viewIndex == TIMELINE_VIEW_INDEX) {
      ((ViewListenerWait07)  viewListenerList.get( TIMELINE_VIEW_INDEX)).viewWait();
    } else if (viewIndex == TOKEN_NETWORK_VIEW_INDEX) {
      ((ViewListenerWait08)  viewListenerList.get(  TOKEN_NETWORK_VIEW_INDEX)).viewWait();
    } else {
      System.err.println( "viewListenerListWait: listener for index " + viewIndex +
                          " not found");
      System.exit( -1);
    }
  } // end viewListenerListWait


  private void viewListenerListReset( int viewIndex, List viewListenerList) {
    if (viewIndex == CONSTRAINT_NETWORK_VIEW_INDEX) {
      ((ViewListenerWait01)  viewListenerList.get( CONSTRAINT_NETWORK_VIEW_INDEX)).reset();
    } else if (viewIndex == DB_TRANSACTION_VIEW_INDEX) {
      ((ViewListenerWait02)  viewListenerList.get( DB_TRANSACTION_VIEW_INDEX)).reset();
    } else if (viewIndex == DECISION_VIEW_INDEX) {
      ((ViewListenerWait03)  viewListenerList.get( DECISION_VIEW_INDEX)).reset();
    } else if (viewIndex == RESOURCE_PROFILE_VIEW_INDEX) {
      ((ViewListenerWait04)  viewListenerList.get( RESOURCE_PROFILE_VIEW_INDEX)).reset();
    } else if (viewIndex == RESOURCE_TRANSACTION_VIEW_INDEX) {
      ((ViewListenerWait05)  viewListenerList.get( RESOURCE_TRANSACTION_VIEW_INDEX)).reset();
    } else if (viewIndex == TEMPORAL_EXTENT_VIEW_INDEX) {
      ((ViewListenerWait06)  viewListenerList.get( TEMPORAL_EXTENT_VIEW_INDEX)).reset();
    } else if (viewIndex == TIMELINE_VIEW_INDEX) {
      ((ViewListenerWait07)  viewListenerList.get( TIMELINE_VIEW_INDEX)).reset();
    } else if (viewIndex == TOKEN_NETWORK_VIEW_INDEX) {
      ((ViewListenerWait08)  viewListenerList.get(  TOKEN_NETWORK_VIEW_INDEX)).reset();
    } else {
      System.err.println( "viewListenerListReset: listener for index " + viewIndex +
                          " not found");
      System.exit( -1);
    }
  } // end viewListenerListReset

  public static TestSuite suite() {
    TestSuite suite = new TestSuite();
    // this does not work -- 
    // suite.addTest(new PlanWorksGUITest("planViz01"));
    // suite.addTest(new PlanWorksGUITest("planViz02"));

    suite.addTest( new PlanWorksGUITest( "planVizTests"));
    return suite;
  }

//   public static void main(String [] args) {
//     TestRunner.run(suite());
//   }

}
