//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest.java,v 1.5 2004-04-22 19:26:20 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import junit.extensions.jfcunit.finder.Finder;
import junit.framework.AssertionFailedError;
import junit.framework.TestSuite; 
import junit.textui.TestRunner;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.StepElement;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


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
      planViz06(); planViz07(); planViz08(); // 08 depends on 07, depends on 06

      try{Thread.sleep(2000);}catch(Exception e){}

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
    String viewMenuItemName = "Open " + PlanWorks.TIMELINE_VIEW;
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
    String viewMenuItemName = "Open " + PlanWorks.TIMELINE_VIEW;
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
      seqStepsView.getName().replaceFirst( PlanWorks.SEQUENCE_STEPS_VIEW.replaceAll( " ", ""),
                                           PlanWorks.TIMELINE_VIEW.replaceAll( " ", ""));
    timelineViewName = timelineViewName.concat( System.getProperty( "file.separator") +
                                                "step" + String.valueOf( stepNumber));
    TimelineView timelineView = (TimelineView) PWTestHelper.findComponentByName
      ( TimelineView.class, timelineViewName, Finder.OP_EQUALS);
    assertNotNull( PlanWorks.TIMELINE_VIEW + " for " + PWTestHelper.SEQUENCE_NAME +
                   " not found", timelineView);
    int stackIndex = PWTestHelper.getStackIndex( timelineView.getViewFrame());
    // System.err.println( "  stackIndex " + stackIndex);
    assertTrue( PlanWorks.TIMELINE_VIEW + " for " + PWTestHelper.SEQUENCE_NAME +
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
        if (menuItem.getText().startsWith( ContentSpec.CONTENT_SPEC_TITLE)) {
          contentFilterCount++;
        } else if (menuItem.getText().startsWith( PlanWorks.TIMELINE_VIEW.
                                                  replaceAll( " ", ""))) {
          if (stackTopMenuItem == null) {
            stackTopMenuItem = menuItem;
          }
          timelineViewCount++;
        }
      }
    }
    assertTrue( "Only " + contentFilterCount + " '" + ContentSpec.CONTENT_SPEC_TITLE +
                "' windows found, should be 3", (contentFilterCount == 3));
    assertTrue( "Only " + timelineViewCount + " '" + PlanWorks.TIMELINE_VIEW +
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
    // System.err.println("Found tile menu item: " + tileItem);
    assertNotNull( "'Window->Tile Windows' not found:", tileItem);
    helper.enterClickAndLeave( new MouseEventData( this, tileItem));
    this.flushAWT(); this.awtSleep();
    try{Thread.sleep(2000);}catch(Exception e){}
    // post condition:  The ContentFilter and SequenceQuery windows are tiled
    // across the top row of the frame.  The TimelineView and SequenceStepsView
    // windows are tiled in the second thru fourth rows.
    List contentFilterFrames =
      PWTestHelper.getInternalFrameByPrefixName( ContentSpec.CONTENT_SPEC_TITLE);
    List sequenceQueryFrames =
      PWTestHelper.getInternalFrameByPrefixName( ContentSpec.SEQUENCE_QUERY_TITLE);
    assertTrue( "Only " + contentFilterFrames.size() + " '" + ContentSpec.CONTENT_SPEC_TITLE +
                "' windows found, should be 3", (contentFilterFrames.size() == 3));
    assertTrue( "Only " + sequenceQueryFrames.size() + " '" + ContentSpec.SEQUENCE_QUERY_TITLE +
                "' windows found, should be 3", (sequenceQueryFrames.size() == 3));
    double firstRowYLocation = 0.0, secondRowYLocation = 0.0;
    double thirdRowYLocation = 0.0, fourthRowYLocation = 0.0;
    Iterator contentFilterItr = contentFilterFrames.iterator();
    while (contentFilterItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) contentFilterItr.next();
      System.err.println( "contentFilterFrame y " + frame.getLocation().getY() +
                          " height " + frame.getSize().getHeight());
      if (frame.getSize().getHeight() > secondRowYLocation) {
        secondRowYLocation = frame.getSize().getHeight();
      }
      assertTrue( "ContentFilter frame y location not in first row",
                  (frame.getLocation().getY() == firstRowYLocation));
    }
    Iterator sequenceQueryItr = sequenceQueryFrames.iterator();
    while (sequenceQueryItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) sequenceQueryItr.next();
      System.err.println( "sequenceQueryFrame y " + frame.getLocation().getY() +
                          " height " + frame.getSize().getHeight());
      if (frame.getSize().getHeight() > secondRowYLocation) {
        secondRowYLocation = frame.getSize().getHeight();
      }
      assertTrue( "SequenceQuery frame y location not in first row",
                  (frame.getLocation().getY() == firstRowYLocation));
    }
    System.err.println("secondRowYLocation " + secondRowYLocation);
    String timelineViewPrefix =
      Utilities.trimView( PlanWorks.TIMELINE_VIEW).replaceAll( " ", "");
    List timelineViewFrames = PWTestHelper.getInternalFrameByPrefixName( timelineViewPrefix);
    String seqStepsViewPrefix =
      Utilities.trimView( PlanWorks.SEQUENCE_STEPS_VIEW).replaceAll( " ", "");
    List sequenceStepsFrames = PWTestHelper.getInternalFrameByPrefixName( seqStepsViewPrefix);


    // "Window->Cascade".


    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, this);

    System.err.println( "\nPLANVIZ_08 COMPLETED\n");
  } // end planViz08


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
