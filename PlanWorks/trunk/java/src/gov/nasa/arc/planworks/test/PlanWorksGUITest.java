//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest.java,v 1.6 2004-05-04 01:27:15 taylor Exp $
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
import java.util.List;
import java.util.ListIterator;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.JFCEventManager;
import junit.extensions.jfcunit.eventdata.JMenuMouseEventData;
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
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.mdi.MDIDesktopPane;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkObjectNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkResourceNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.StepQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.DBTransactionQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.StepElement;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
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
public class PlanWorksGUITest extends JFCTestCase {
  
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

  public PlanWorksGUITest(String test) {
    super(test);
  }

  public int incEntityIdInt() {
    return ++entityIdInt;
  }
  
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
      sequenceUrls = PWSetupHelper.buildTestData( numSequences, numSteps, this);

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
//       planViz01(); 
//       planViz02(); 
//       planViz03(); planViz04(); // 04 depends on 03
//       planViz05(); 
//       planViz06(); planViz07(); planViz08(); planViz09(); // dependent sequence of tests
      planViz10(); 

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
    assertTrue("PlanWorks title does not contain " + PWTestHelper.PROJECT1,
               PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT1));

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
    (new File( sequenceFileArray[1] + System.getProperty("file.separator") +
               DbConstants.SEQ_FILE)).delete();
    // modify sequence #3
    String stepName = "step1"; int stepNumber = 1;
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
    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK",
                               "0 sequence files in directory -- 3 are required",
                               helper, this);
    AbstractButton cancelButton = PWTestHelper.findButton( "Cancel");
    assertNotNull( "'Project->Create' cancel button not found:", cancelButton);
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
    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK",
                               "2 sequence files in directory -- 3 are required",
                               helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK",
                               "Has 7 files -- 8 are required", helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

    ViewListener viewListener01 = new ViewListenerWait01( this);
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME, viewListener01,
                                       helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME, helper, this);

    ViewListener viewListener = null;
    String viewMenuItemName = "Open " + ViewConstants.TIMELINE_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListener, helper, this);

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
    assertNotNull( "'Project->Create' cancel button not found:", cancelButton);
    helper.enterClickAndLeave( new MouseEventData( this, cancelButton));

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

    System.err.println( "\nPLANVIZ_03 COMPLETED\n");
  } // end planViz03

  public void planViz04() throws Exception {
    // post planViz03 deleteProject condition 1
    assertFalse( "PlanWorks title contains '" + PWTestHelper.PROJECT1 +
                 "'after Project->Delete",
                 PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT1));

    // post planViz03 deleteProject condition 2
    JMenuItem deleteItem =
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.DELETE_MENU_ITEM,
                                 helper, this);
    assertTrue( "'Project->Delete' should be disabled", (deleteItem.isEnabled() == false));
    assertNotNull( "'Project->Delete' not found:", deleteItem);
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
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 5));
    PWTestHelper.createProject( PWTestHelper.PROJECT2, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);

    PWTestHelper.addSequencesToProject( helper, this, planWorks);
    assertTrue( "PlanWorks title does not contain '" + PWTestHelper.PROJECT2 +
                 "' after 2nd Project->Create",
                 PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT2));

    PWTestHelper.openProject( PWTestHelper.PROJECT1, helper, this, planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    System.err.println( "title " + PlanWorks.getPlanWorks().getTitle());
    assertTrue( "PlanWorks title does not contain '" + PWTestHelper.PROJECT1,
                 PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT1));

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
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, sequenceFileArray,
                                helper, this, planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 5));
    PWTestHelper.addPlanSequence( sequenceDirectory, sequenceFileArray, helper,
                                  this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 6));
    PWTestHelper.addPlanSequence( sequenceDirectory, sequenceFileArray, helper,
                                  this, planWorks);
    PWTestHelper.addSequencesToProject( helper, this, planWorks);

    assertTrue( "PlanWorks title does not contain '" + PWTestHelper.PROJECT1,
                 PlanWorks.getPlanWorks().getTitle().endsWith( PWTestHelper.PROJECT1));

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
    assertTrue( "There are not 3 'sequence' plans under 'Planning Sequence'",
                (sequenceCount == 3));

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
    viewListener01.reset();
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListener01,
                                                helper, this);
    viewListener01.viewWait();

    // then sequence (1) timeline view
    viewListener01.reset();
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (1)",
                                        viewListener01, helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView1 =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (1)", helper, this);
    viewListener01.reset();
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView1, stepNumber,
                                                viewMenuItemName, viewListener01,
                                                helper, this);
    viewListener01.viewWait();
    // then sequence (2) timeline view
    viewListener01.reset();
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (2)",
                                        viewListener01, helper, this);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView2 =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (2)", helper, this);
    viewListener01.reset();
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView2, stepNumber,
                                                viewMenuItemName, viewListener01,
                                                helper, this);
    viewListener01.viewWait();
    // now select the timeline view from the first sequence
    // view exists, so no wait needed
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, null, helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

   // post condition: seqStepsView timeline view has focus
    String timelineViewName =
      seqStepsView.getName().replaceFirst( ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", ""),
                                           ViewConstants.TIMELINE_VIEW.replaceAll( " ", ""));
    timelineViewName = timelineViewName.concat( System.getProperty( "file.separator") +
                                                "step" + String.valueOf( stepNumber));
    TimelineView timelineView = (TimelineView) PWTestHelper.findComponentByName
      ( TimelineView.class, timelineViewName, Finder.OP_EQUALS);
    assertNotNull( ViewConstants.TIMELINE_VIEW + " for " + PWTestHelper.SEQUENCE_NAME +
                   " not found", timelineView);
    int stackIndex = PWTestHelper.getStackIndex( timelineView.getViewFrame());
    // System.err.println( "  stackIndex " + stackIndex);
    assertTrue( ViewConstants.TIMELINE_VIEW + " for " + PWTestHelper.SEQUENCE_NAME +
                " is not at top of window stack order", (stackIndex == 0));

    // post condition: 3 content filter and 3 timeline view windows exist
    JMenu  windowMenu = PWTestHelper.findMenu( PlanWorks.WINDOW_MENU);
    assertNotNull( "Window menu not found", windowMenu);
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
    assertTrue( "Only " + contentFilterCount + " '" + ViewConstants.CONTENT_SPEC_TITLE +
                "' windows found, should be 3", (contentFilterCount == 3));
    assertTrue( "Only " + timelineViewCount + " '" + ViewConstants.TIMELINE_VIEW +
                "' windows found, should be 3", (timelineViewCount == 3));
    // this is needed to free up Window menu and allow Project->Delete to be available
    helper.enterClickAndLeave( new MouseEventData( this, stackTopMenuItem));
    
    // try{Thread.sleep(2000);}catch(Exception e){}
    System.err.println( "\nPLANVIZ_07 COMPLETED\n");
  } // end planViz07

  public void planViz08() throws Exception {
    JMenuItem tileItem =
      PWTestHelper.findMenuItem( PlanWorks.WINDOW_MENU, PlanWorks.TILE_WINDOWS_MENU_ITEM,
                                 helper, this);
    System.err.println("Found " + PlanWorks.TILE_WINDOWS_MENU_ITEM + " menu item");
    assertNotNull( "'Window->Tile Windows' not found:", tileItem);
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
    assertTrue( "Only " + contentFilterFrames.size() + " '" + ViewConstants.CONTENT_SPEC_TITLE +
                "' windows found, should be 3", (contentFilterFrames.size() == 3));
    assertTrue( "Only " + sequenceQueryFrames.size() + " '" + ViewConstants.SEQUENCE_QUERY_TITLE +
                "' windows found, should be 3", (sequenceQueryFrames.size() == 3));
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
      assertTrue( frame.getTitle() + " frame y location not in first row",
                  (frame.getLocation().getY() == firstRowYLocation));
    }
//     System.err.println("secondRowYLocation " + secondRowYLocation);
    String timelineViewPrefix =
      Utilities.trimView( ViewConstants.TIMELINE_VIEW).replaceAll( " ", "");
    List timelineViewFrames = PWTestHelper.getInternalFramesByPrefixName( timelineViewPrefix);
    String seqStepsViewPrefix =
      Utilities.trimView( ViewConstants.SEQUENCE_STEPS_VIEW).replaceAll( " ", "");
    List sequenceStepsFrames = PWTestHelper.getInternalFramesByPrefixName( seqStepsViewPrefix);
    assertTrue( "Only " + timelineViewFrames.size() + " '" + ViewConstants.TIMELINE_VIEW +
                "' windows found, should be 3", (timelineViewFrames.size() == 3));
    assertTrue( "Only " + sequenceStepsFrames.size() + " '" + ViewConstants.SEQUENCE_STEPS_VIEW +
                "' windows found, should be 3", (sequenceStepsFrames.size() == 3));
    timelineViewFrames.addAll( sequenceStepsFrames);
    Iterator timelineViewItr = timelineViewFrames.iterator();
    while (timelineViewItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) timelineViewItr.next();
//       System.err.println( frame.getTitle() + " y " + frame.getLocation().getY() +
//                           " height " + frame.getSize().getHeight());
      assertTrue( frame.getTitle() + " frame y location not >= second row",
                  (frame.getLocation().getY() >= secondRowYLocation));
    }
    // "Window->Cascade".
    JMenuItem cascadeItem =
      PWTestHelper.findMenuItem( PlanWorks.WINDOW_MENU, PlanWorks.CASCADE_WINDOWS_MENU_ITEM,
                                 helper, this);
    System.err.println("Found " + PlanWorks.CASCADE_WINDOWS_MENU_ITEM + " menu item");
    assertNotNull( "'Window->Cascade Windows' not found:", cascadeItem);
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
            assertTrue( frame.getTitle() + " not in first row",
                        (frame.getLocation().getY() == firstRowYLocation));
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
      assertTrue( frame.getTitle() + " not cascaded in X",
                  ((frame.getLocation().getX() > frameX) ||
                   (frame.getLocation().getX() == 0.0)));
      frameX = frame.getLocation().getX();
      assertTrue( frame.getTitle() + " not cascaded in Y",
                  (frame.getLocation().getY() > frameY));
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
    assertTrue( "There should be " +  totalFrameCnt + " frames, there are "
                + internalFrameCnt, (internalFrameCnt == totalFrameCnt));
    int contentFilterCnt =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.CONTENT_SPEC_TITLE).size();
    int sequenceQueryCnt =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.SEQUENCE_QUERY_TITLE).size();
    int timelineViewCnt = PWTestHelper.getInternalFramesByPrefixName
      ( ViewConstants.TIMELINE_VIEW.replaceAll( " ", "")).size();
    int sequenceStepsViewCnt = PWTestHelper.getInternalFramesByPrefixName
        ( ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", "")).size();
    assertTrue( "There should be 1 " + ViewConstants.CONTENT_SPEC_TITLE + " window, " +
                " 1 " + ViewConstants.SEQUENCE_QUERY_TITLE + " window, " +
                " 1 " + ViewConstants.TIMELINE_VIEW + " window, and " +
                " 1 " + ViewConstants.SEQUENCE_STEPS_VIEW + " window.",
                ((contentFilterCnt + sequenceQueryCnt + timelineViewCnt +
                  sequenceStepsViewCnt) == totalFrameCnt));

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
    assertTrue( "There should be " +  totalFrameCnt + " frames, there are "
                + internalFrameCnt, (internalFrameCnt == totalFrameCnt));
    sequenceQueryCnt =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.SEQUENCE_QUERY_TITLE).size();
    sequenceStepsViewCnt = PWTestHelper.getInternalFramesByPrefixName
        ( ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", "")).size();
    assertTrue( "There should be 2 " + ViewConstants.SEQUENCE_QUERY_TITLE + " windows",
                (sequenceQueryCnt == totalSeqQueryCnt));
    assertTrue( "There should be 2 " + ViewConstants.SEQUENCE_STEPS_VIEW + " windows",
                (sequenceStepsViewCnt == totalSeqStepsCnt));
    assertTrue( "The re-loaded sequence ("  + sequenceName + ") is not the original" +
                " third sequence ", seqStepsView.getPlanningSequence().getUrl().
                equals( (String) sequenceUrls.get( 6)));
    //try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

    System.err.println( "\nPLANVIZ_09 COMPLETED\n");
  } // end planViz09


  public void planViz10() throws Exception {
    int stepNumber = 1, seqUrlIndex = 4;
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

    viewListener01.reset();
    String viewMenuItemName = "Open " + ViewConstants.CONSTRAINT_NETWORK_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListener01,
                                                helper, this);
   String viewNameSuffix = PWTestHelper.SEQUENCE_NAME +
     System.getProperty( "file.separator") + "step" + String.valueOf( stepNumber);
   viewListener01.viewWait();
   String constraintNetworkViewName =
     ViewConstants.CONSTRAINT_NETWORK_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) PWTestHelper.findComponentByName
      ( ConstraintNetworkView.class, constraintNetworkViewName, Finder.OP_EQUALS);
    assertNotNull( constraintNetworkViewName + " not found", constraintNetworkView);

    ViewListener viewListener02 = new ViewListenerWait02( this);
    viewMenuItemName = "Open " + ViewConstants.TEMPORAL_EXTENT_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListener02,
                                                helper, this);
   viewListener02.viewWait();
   String temporalExtentViewName =
     ViewConstants.TEMPORAL_EXTENT_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
    TemporalExtentView temporalExtentView =
      (TemporalExtentView) PWTestHelper.findComponentByName
      ( TemporalExtentView.class, temporalExtentViewName, Finder.OP_EQUALS);
    assertNotNull( temporalExtentViewName + " not found", temporalExtentView);

    ViewListener viewListener03 = new ViewListenerWait03( this);
    viewMenuItemName = "Open " + ViewConstants.TIMELINE_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListener03,
                                                helper, this);
   viewListener03.viewWait();
   String timelineViewName =
     ViewConstants.TIMELINE_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
    TimelineView timelineView =
      (TimelineView) PWTestHelper.findComponentByName
      ( TimelineView.class, timelineViewName, Finder.OP_EQUALS);
    assertNotNull( timelineViewName + " not found", timelineView);

    ViewListener viewListener04 = new ViewListenerWait04( this);
    viewMenuItemName = "Open " + ViewConstants.TOKEN_NETWORK_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListener04,
                                                helper, this);
   viewListener04.viewWait();
   String tokenNetworkViewName =
     ViewConstants.TOKEN_NETWORK_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
    TokenNetworkView tokenNetworkView =
      (TokenNetworkView) PWTestHelper.findComponentByName
      ( TokenNetworkView.class, tokenNetworkViewName, Finder.OP_EQUALS);
    assertNotNull( tokenNetworkViewName + " not found", tokenNetworkView);

    ViewListener viewListener05 = new ViewListenerWait05( this);
    viewMenuItemName = "Open " + ViewConstants.DB_TRANSACTION_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListener05,
                                                helper, this);
   viewListener05.viewWait();
   String dbTransactionViewName =
     ViewConstants.DB_TRANSACTION_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
    DBTransactionView dbTransactionView =
      (DBTransactionView) PWTestHelper.findComponentByName
      ( DBTransactionView.class, dbTransactionViewName, Finder.OP_EQUALS);
    assertNotNull( dbTransactionViewName + " not found", dbTransactionView);

    planViz10CNet( constraintNetworkView, stepNumber, seqUrlIndex);



    // try{Thread.sleep(6000);}catch(Exception e){}

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

    System.err.println( "\nPLANVIZ_10 COMPLETED\n");
  } // end planViz10

  public void planViz10CNet( ConstraintNetworkView constraintNetworkView, int stepNumber,
                             int seqUrlIndex) throws Exception {
    ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
    // open interval token, resource, variable, and constraint nodes in ConstraintNetworkView
    int numTokensOpened = 3, numResourcesOpened = 1, numResTransactionsOpened = 1;
    int numTimelinesOpened = 1, numObjectsOpened = 1;
    ConstraintNetworkTokenNode tokenNode1 = null;
    ConstraintNetworkTokenNode tokenNode2 = null;
    ConstraintNetworkTokenNode freeTokenNode = null;
    ConstraintNetworkTokenNode resTransactionNode = null;
    ConstraintNetworkResourceNode resourceNode = null;
    ConstraintNetworkTimelineNode timelineNode = null;
    ConstraintNetworkObjectNode objectNode = null;
    
    int numTokenNodes = 0, numResourceNodes = 0, numResTransactionNodes = 0;
    int numTimelineNodes = 0, numObjectNodes = 0;
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
          boolean isFree = tokenNode.getToken().isFree();
          if ((!isFree ) && (tokenNode1 == null)) {
            tokenNode1 = (ConstraintNetworkTokenNode) contNode;
          } else if ((! isFree) && (tokenNode2 == null)) {
            tokenNode2 = (ConstraintNetworkTokenNode) contNode;
          } else if (isFree && (freeTokenNode == null)) {
            freeTokenNode = (ConstraintNetworkTokenNode) contNode;
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
      }
    }
    // try{Thread.sleep(6000);}catch(Exception e){}

    assertNotNull( "Did not find ConstraintNetworkTokenNode tokenNode1", tokenNode1);
    assertNotNull( "Did not find ConstraintNetworkTokenNode tokenNode2", tokenNode2);
    assertNotNull( "Did not find ConstraintNetworkTokenNode freeTokenNode", freeTokenNode);
    assertNotNull( "Did not find ConstraintNetworkResourceNode resourceNode", resourceNode);
    assertNotNull( "Did not find ConstraintNetworkTokenNode resTransactionNode",
                   resTransactionNode);
    assertNotNull( "Did not find ConstraintNetworkTimelineNode timelineNode", timelineNode);
    assertNotNull( "Did not find ConstraintNetworkObjectNode objectNode", objectNode);
    PwPlanningSequence planSeq =
      planWorks.getCurrentProject().getPlanningSequence( (String) sequenceUrls.get( seqUrlIndex));
    PwPartialPlan partialPlan = planSeq.getPartialPlan( stepNumber);
    assertTrue( "Number of partial plan interval tokens (" +
                partialPlan.getTokenList().size() + ") != number of ContraintNetwork " +
                "token nodes (" + numTokenNodes + ")",
                (partialPlan.getTokenList().size() == numTokenNodes));
    assertTrue( "Number of partial plan resources (" +
                partialPlan.getResourceList().size() + ") != number of ContraintNetwork " +
                "resource nodes (" + numResourceNodes + ")",
                (partialPlan.getResourceList().size() == numResourceNodes));
    assertTrue( "Number of partial plan resourceTransactions (" +
                partialPlan.getResourceList().size() + ") != number of ContraintNetwork " +
                "resourceTransaction nodes (" + numResTransactionNodes + ")",
                (partialPlan.getResTransactionList().size() == numResTransactionNodes));
    assertTrue( "Number of partial plan timelines (" +
                partialPlan.getTimelineList().size() + ") != number of ContraintNetwork " +
                "timeline nodes (" + numTimelineNodes + ")",
                (partialPlan.getTimelineList().size() == numTimelineNodes));
    assertTrue( "Number of partial plan objects (" +
                partialPlan.getObjectList().size() + ") != number of ContraintNetwork " +
                "object nodes (" + numObjectNodes + ")",
                (partialPlan.getObjectList().size() == (numObjectNodes + numResourceNodes +
                                                        numTimelineNodes)));

    tokenNode1.doMouseClick( MouseEvent.BUTTON1_MASK, tokenNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    tokenNode2.doMouseClick( MouseEvent.BUTTON1_MASK, tokenNode2.getLocation(),
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
    int numVarNodesInView = constraintNetworkView.getVariableNodeList().size();
    int numVarNodesOpened =
      (numTokensOpened * PWSetupHelper.NUM_VARS_PER_TOKEN) +
      (numResourcesOpened * PWSetupHelper.NUM_VARS_PER_RESOURCE) +
      (numResTransactionsOpened * PWSetupHelper.NUM_VARS_PER_RESOURCE_TRANS) +
      (numTimelinesOpened * PWSetupHelper.NUM_VARS_PER_TIMELINE) +
      (numObjectsOpened * PWSetupHelper.NUM_VARS_PER_OBJECT);
    assertTrue( "Number of ContraintNetwork variable nodes (" + numVarNodesInView +
                ") != number of token (interval & resTrans) and resource variables opened (" +
                numVarNodesOpened + ")", (numVarNodesOpened == numVarNodesInView));
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
            if (tokenNode.getToken().getId().equals( tokenNode1.getToken().getId())) {
              variableNode1 = varNode;
            } else if (tokenNode.getToken().getId().equals( tokenNode2.getToken().getId())) {
              variableNode2 = varNode;
            } else if (tokenNode.getToken().getId().equals( freeTokenNode.getToken().getId())) {
              variableNode3 = varNode;
            }
          }
        }
      }
      numVariableNodes++;
    }
    assertNotNull( "Did not find ConstraintNetwork VariableNode variableNode1",
                   variableNode1);
    assertNotNull( "Did not find ConstraintNetwork VariableNode variableNode2",
                   variableNode2);
    assertNotNull( "Did not find ConstraintNetwork VariableNode variableNode3",
                   variableNode3);
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
    assertTrue( "Number of ContraintNetwork constraint nodes (" + numContNodesInView +
                ") != number of token (interval & resTrans) and resource constraints opened (" +
                numContNodesOpened + ")", (numContNodesOpened == numContNodesInView));
    System.err.println( "ContraintNetworkView: " + numVariablesOpened +
                        " variables opened ==>> " +
                        numContNodesInView + " constraint nodes visible");

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
    assertNotNull( "Did not find ConstraintNetwork ConstraintNode constraintNode1",
                   constraintNode1);
    assertNotNull( "Did not find ConstraintNetwork ConstraintNode constraintNode2",
                   constraintNode2);
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
    tokenNode1.doMouseClick( MouseEvent.BUTTON1_MASK, tokenNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    flushAWT(); awtSleep();
    tokenNode2.doMouseClick( MouseEvent.BUTTON1_MASK, tokenNode2.getLocation(),
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

    assertFalse( "constraintNode1 is still open", constraintNode1.areNeighborsShown());
    assertFalse( "constraintNode2 is still open", constraintNode2.areNeighborsShown());
    assertFalse( "variableNode1 is still open", variableNode1.areNeighborsShown());
    assertFalse( "variableNode2 is still open", variableNode2.areNeighborsShown());
    assertFalse( "variableNode3 is still open", variableNode3.areNeighborsShown());
    assertFalse( "tokenNode1 is still open", tokenNode1.areNeighborsShown());
    assertFalse( "tokenNode2 is still open", tokenNode2.areNeighborsShown());
    assertFalse( "freeTokenNode is still open", freeTokenNode.areNeighborsShown());
    assertFalse( "resourceNode is still open", resourceNode.areNeighborsShown());
    assertFalse( "resTransactionNode is still open", resTransactionNode.areNeighborsShown());
    assertFalse( "timelineNode is still open", timelineNode.areNeighborsShown());
    assertFalse( "objectNode is still open", objectNode.areNeighborsShown());

    System.err.println( "tokenNode1.getToolTipText() " + tokenNode1.getToolTipText());
    System.err.println( "tokenNode1.getText() " + tokenNode1.getText());
    String toolTipText = tokenNode1.getToolTipText();
    String labelText = tokenNode1.getText();
    String predSubString = tokenNode1.getPredicateName();
    String predParamSubString = predSubString + " (";
    String slotSubString = "slot key=" + tokenNode1.getToken().getSlotId().toString();
    String tokenSubString = "key=" + tokenNode1.getToken().getId().toString();
    String mouseSubString = "Mouse-L: open";
    assertTrue( "ConstraintNetworkView token node (" +
                tokenNode1.getToken().getId().toString() + ") tool tip does not contain " +
                predParamSubString, (toolTipText.indexOf( predParamSubString) >= 0));
    assertTrue( "ConstraintNetworkView token node (" +
                tokenNode1.getToken().getId().toString() + ") tool tip does not contain " +
                slotSubString, (toolTipText.indexOf( slotSubString) >= 0));
    assertTrue( "ConstraintNetworkView token node (" +
                tokenNode1.getToken().getId().toString() + ") tool tip does not contain " +
                mouseSubString, (toolTipText.indexOf( mouseSubString) >= 0));
    assertTrue( "ConstraintNetworkView token node (" +
                tokenNode1.getToken().getId().toString() + ") label does not contain " +
                tokenSubString, (labelText.indexOf( tokenSubString) >= 0));
    assertTrue( "ConstraintNetworkView token node (" +
                tokenNode1.getToken().getId().toString() + ") label does not contain " +
                predSubString, (labelText.indexOf( predSubString) >= 0));

    System.err.println( "resTransactionNode.getToolTipText() " +
                        resTransactionNode.getToolTipText());
    System.err.println( "resTransactionNode.getText() " + resTransactionNode.getText());
    toolTipText = resTransactionNode.getToolTipText();
    labelText = resTransactionNode.getText();
    String nameSubString = resTransactionNode.getToken().getName();
    String resourceSubString = "key=" + resTransactionNode.getToken().getId().toString();
    assertTrue( "ConstraintNetworkView resTransaction node (" +
                resTransactionNode.getToken().getId().toString() +
                ") tool tip does not contain " +
                nameSubString, (toolTipText.indexOf( nameSubString) >= 0));
    assertTrue( "ConstraintNetworkView resTransaction node (" +
                resTransactionNode.getToken().getId().toString() +
                ") tool tip does not contain " +
                mouseSubString, (toolTipText.indexOf( mouseSubString) >= 0));
    assertTrue( "ConstraintNetworkView resTransaction node (" +
                resTransactionNode.getToken().getId().toString() +
                ") label does not contain " +
                resourceSubString, (labelText.indexOf( resourceSubString) >= 0));
    assertTrue( "ConstraintNetworkView resTransaction node (" +
                resTransactionNode.getToken().getId().toString() +
                ") label does not contain " +
                nameSubString, (labelText.indexOf( nameSubString) >= 0));

    System.err.println( "resourceNode.getToolTipText() " + resourceNode.getToolTipText());
    System.err.println( "resourceNode.getText() " + resourceNode.getText());
    toolTipText = resourceNode.getToolTipText();
    labelText = resourceNode.getText();
    nameSubString = resourceNode.getResource().getName();
    resourceSubString = "key=" + resourceNode.getResource().getId().toString();
    assertTrue( "ConstraintNetworkView resource node (" +
                resourceNode.getResource().getId().toString() + ") tool tip does not contain " +
                nameSubString, (toolTipText.indexOf( nameSubString) >= 0));
    assertTrue( "ConstraintNetworkView resource node (" +
                resourceNode.getResource().getId().toString() + ") tool tip does not contain " +
                mouseSubString, (toolTipText.indexOf( mouseSubString) >= 0));
    assertTrue( "ConstraintNetworkView resource node (" +
                resourceNode.getResource().getId().toString() + ") label does not contain " +
                resourceSubString, (labelText.indexOf( resourceSubString) >= 0));
    assertTrue( "ConstraintNetworkView resource node (" +
                resourceNode.getResource().getId().toString() + ") label does not contain " +
                nameSubString, (labelText.indexOf( nameSubString) >= 0));

    System.err.println( "variableNode1.getToolTipText() " + variableNode1.getToolTipText());
    System.err.println( "variableNode1.getText() " + variableNode1.getText());
    toolTipText = variableNode1.getToolTipText();
    labelText = variableNode1.getText();
    String typeSubString = variableNode1.getVariable().getType();
    String variableSubString = "key=" + variableNode1.getVariable().getId().toString();
    String domainSubString = variableNode1.getVariable().getDomain().toString();
    assertTrue( "ConstraintNetworkView variable node (" +
                variableNode1.getVariable().getId().toString() + ") tool tip does not contain " +
                typeSubString, (toolTipText.indexOf( typeSubString) >= 0));
    assertTrue( "ConstraintNetworkView variable node (" +
                variableNode1.getVariable().getId().toString() + ") tool tip does not contain " +
                mouseSubString, (toolTipText.indexOf( mouseSubString) >= 0));
    assertTrue( "ConstraintNetworkView variable node (" +
                variableNode1.getVariable().getId().toString() + ") label does not contain " +
                variableSubString, (labelText.indexOf( variableSubString) >= 0));
    assertTrue( "ConstraintNetworkView variable node (" +
                variableNode1.getVariable().getId().toString() + ") label does not contain " +
                domainSubString, (labelText.indexOf( domainSubString) >= 0));

    System.err.println( "constraintNode1.getToolTipText() " + constraintNode1.getToolTipText());
    System.err.println( "constraintNode1.getText() " + constraintNode1.getText());
    toolTipText = constraintNode1.getToolTipText();
    labelText = constraintNode1.getText();
    typeSubString = constraintNode1.getConstraint().getType();
    String constraintSubString = "key=" + constraintNode1.getConstraint().getId().toString();
    nameSubString = constraintNode1.getConstraint().getName();
    assertTrue( "ConstraintNetworkView constraint node (" +
                constraintNode1.getConstraint().getId().toString() +
                ") tool tip does not contain " +
                typeSubString, (toolTipText.indexOf( typeSubString) >= 0));
    assertTrue( "ConstraintNetworkView constraint node (" +
                constraintNode1.getConstraint().getId().toString() +
                ") tool tip does not contain " +
                mouseSubString, (toolTipText.indexOf( mouseSubString) >= 0));
    assertTrue( "ConstraintNetworkView constraint node (" +
                constraintNode1.getConstraint().getId().toString() +
                ") label does not contain " +
                constraintSubString, (labelText.indexOf( constraintSubString) >= 0));
    assertTrue( "ConstraintNetworkView constraint node (" +
                constraintNode1.getConstraint().getId().toString() +
                ") label does not contain " +
                nameSubString, (labelText.indexOf( nameSubString) >= 0));
    
  } // end planViz10CNet

 





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
