//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimelineViewTest.java,v 1.16 2003-07-02 21:46:10 taylor Exp $
//
package gov.nasa.arc.planworks.viz.views.test;

import java.awt.Container;
// import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.MenuElement;

import junit.extensions.jfcunit.*;
// import junit.extensions.jfcunit.eventdata.EventDataConstants;
// import junit.extensions.jfcunit.eventdata.KeyEventData;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.*; 
import junit.textui.TestRunner;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.nodes.SlotNode;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.ContentSpecWindow;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.GroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.KeyEntryBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.LogicComboBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.NegationCheckBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.TimelineBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.TimelineGroupBox;


/**
 * <code>TimelineViewTest</code> - JFCUnit test case for timeline view, along with
 *                  project management
 *
 * @author <a href="mailto:miatauro@email.arcnasa.gov">Michael Iatauro</a>
 *                  NASA Ames Research Center - Code IC
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineViewTest extends JFCTestCase{

  private JFrame frame;
  private PlanWorks planWorks;
  private String testType;

  /**
   * <code>TimelineViewTest</code> - constructor 
   *
   * @param test - <code>String</code> - 
   * @param testType - <code>String</code> - 
   */
  public TimelineViewTest(String test, String testType) {
    super(test);
    this.testType = testType;
  }

  /**
   * <code>setUp</code> - start PlanWorks
   *
   * @exception Exception if an error occurs
   */
  public void setUp() throws Exception {
    System.err.println( "\nsetUp entered testType " + testType + "\n");
    helper = new JFCTestHelper();

    try {
      PwProject.initProjects();
    } catch (ResourceNotFoundException rnfExcep) {
      System.err.println( rnfExcep);
      System.exit( -1);
    }
    planWorks = new PlanWorks( PlanWorks.buildConstantMenus());
    PlanWorks.setPlanWorks( planWorks);
    /*
      if (testType.equals( "create") || testType.equals( "open") || testType.equals("contentSpec")) {
      CreateProjectTestCase
      OpenProjectTestCase
      } else {
      throw new Exception( "setup: testType " + testType + " not handled");
      }
    */
    // Give a little extra time for the painting/construction
    Thread.currentThread().sleep(500);
    flushAWT();
  }
  

  /**
   * <code>tearDown</code>
   *
   * @exception Exception if an error occurs
   */
  public void tearDown() throws Exception {
    // System.err.println( "\ntearDown entered\n");
    helper.cleanUp(this);
    super.tearDown();
    System.exit(0);
  }
  
  /**
   * <code>testCreateProject</code>
   *
   * @exception Exception if an error occurs
   */
  public void testCreateProject() throws Exception {
    System.err.println( "\n\nCreateProjectTestCase\n\n");
    awtSleep();
    Set windows = helper.getWindows();
    assertEquals("PlanWorks window failed to open", 1, windows.size());
    frame = (JFrame)(windows.toArray())[0];
    assertNotNull("Failed to get frame from set", frame);
    JMenuBar menuBar = frame.getJMenuBar();
    assertNotNull("Failed to get menu bar from frame", menuBar);
    JMenu projectMenu = null;
    MenuElement [] elements = menuBar.getSubElements();
    for(int i = 0; i < elements.length; i++) {
      if(((JMenu)elements[i]).getText().equals("Project")) {
        projectMenu = (JMenu) elements[i];
      }
    }
    assertNotNull("Failed to get \"Project\" menu", projectMenu);
    assertTrue("Failed to get \"Project\" menu.", projectMenu.getText().equals("Project"));
    JMenuItem createItem = null;
    for(int i = 0; i < projectMenu.getItemCount(); i++) {
      if(projectMenu.getItem(i).getText().equals("Create ...")) {
        createItem = projectMenu.getItem(i);
      }
    }
    assertNotNull("Failed to get \"Create ...\" item.", createItem);
    assertTrue("Failed to get \"Create ...\" item.", createItem.getText().equals("Create ..."));
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, createItem));

    String [] seqAndPlanNames = selectTimelineView();

    TimelineView timelineView = getTimelineView( seqAndPlanNames);

    validateTimelines( timelineView);

//     while ( true) {
//       try {
//         Thread.currentThread().sleep(50);
//       } catch (InterruptedException excp) {
//       }
//     }
    exitPlanWorks( menuBar);
  } // end testCreateProject

  private String [] selectTimelineView() throws Exception {
    // click enter on CreateProject dialog, which creates Partial Plan menu
    awtSleep();
    JMenu partialPlanMenu = null;
    partialPlanMenu = urlEnter();
    assertNotNull( "Failed to get partialPlanMenu", partialPlanMenu);
    helper.enterClickAndLeave(new MouseEventData(this, partialPlanMenu));
    Thread.sleep( 1000);
    // System.err.println( "\n\nGot to here 1\n\n");
    JMenu sequenceMenu = null;
    JMenu partialPlanSubMenu = null;
    PlanWorks.SeqPartPlanViewMenuItem timelineViewItem = null;
    String sequenceName = null;
    String partialPlanName = null;
    String seqUrl = null;
    found: for (int i = 0; i < partialPlanMenu.getItemCount(); i++) {
      if (partialPlanMenu.getItem(i).getText().equals("monkey")) {
        sequenceMenu = (JMenu) partialPlanMenu.getItem(i);
        assertNotNull( "Failed to get sequence \"monkey\"", sequenceMenu);
        helper.enterClickAndLeave(new MouseEventData(this, sequenceMenu));
        Thread.sleep( 1000);
        sequenceName = partialPlanMenu.getItem(i).getText();
        for (int j = 0; j < sequenceMenu.getItemCount(); j++) {
          if (sequenceMenu.getItem(j).getText().equals("step0000")) {
            partialPlanSubMenu = (JMenu) sequenceMenu.getItem(j);
            assertNotNull( "Failed to get partialPlan \"step0000\"", sequenceMenu);
            helper.enterClickAndLeave(new MouseEventData(this, partialPlanSubMenu));
            Thread.sleep( 1000);
            partialPlanName = sequenceMenu.getItem(j).getText();
            for (int k = 0; k < partialPlanSubMenu.getItemCount(); k++) {
              if (partialPlanSubMenu.getItem(k).getText().equals("Timeline")) {
                timelineViewItem =
                  (PlanWorks.SeqPartPlanViewMenuItem) partialPlanSubMenu.getItem(k);
                assertNotNull( "Failed to get view \"Timeline\"", sequenceMenu);
                helper.enterClickAndLeave(new MouseEventData(this, timelineViewItem));
                break found;
              }
            }
          }
        }
      }
    }
    assertNotNull("Failed to get any menu item.", timelineViewItem);
    assertTrue("Failed to get \"Timeline\" submenu item.", 
               timelineViewItem.getText().equals("Timeline"));
    // System.err.println( "\n\nGot to here 2\n\n");
    // helper.mousePressed(timelineViewItem, EventDataConstants.DEFAULT_MOUSE_MODIFIERS, 1, false);
    return new String [] { sequenceName, partialPlanName , timelineViewItem.getSeqUrl()};
  } // end selectTimelineView


  private JMenu urlEnter() throws Exception {
    if (testType.equals( "create")) {
      List dialogs = new ArrayList();
      while (dialogs.size() == 0) {
        // System.err.println( "wait for create dialog");
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        dialogs = TestHelper.getShowingDialogs("Create Project");
      }
      assertEquals("Dialog not found:", 1, dialogs.size());
      Container planWorksDialog = (Container) dialogs.get(0);
      // JTextField field = null;
      // field = (JTextField) TestHelper.findComponent(JTextField.class, jfcunitDialog, 0);
      // assertNotNull("Could not find \"Enter\" field", field);
      // helper.sendString(new StringEventData(this, field, "Harry Potter"));
      // helper.sendKeyAction(new KeyEventData(this, field, KeyEvent.VK_ENTER));
      // Cancel button -- cancel 2nd dialog created by PlanWorks
      JButton button = null;
      button = (JButton) TestHelper.findComponent(JButton.class, planWorksDialog, 0);
      assertNotNull("Could not find \"Enter\" button", button);
      helper.enterClickAndLeave(new MouseEventData(this, button));
    } else if (testType.equals( "open") || testType.equals("contentSpec")) {
      List dialogs = new ArrayList();
      while (dialogs.size() == 0) {
        // System.err.println( "wait for open dialog");
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        dialogs = TestHelper.getShowingDialogs("Open Project");
      }
      assertEquals("Dialog not found:", 1, dialogs.size());
      Container planWorksDialog = (Container) dialogs.get(0);

      JButton okButton = null;
      for (int i = 0, n = 10; i < n; i++) {
        JButton button = (JButton) TestHelper.findComponent(JButton.class, planWorksDialog, i);
        if (button.getText().equals( "OK")) {
          okButton = button;
          break;
        }
      }
      assertNotNull("Could not find \"OK\" button", okButton);
      helper.enterClickAndLeave(new MouseEventData(this, okButton));
    } else {
      throw new Exception( "urlEnter: testType " + testType + " not handled");
    }

    JMenu partialPlanMenu = null;
    JMenuBar menuBar = frame.getJMenuBar();
    // wait for TimelineView instance to become displayable
    while (partialPlanMenu == null) {
      // System.err.println( "partialPlanMenu still null");
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      MenuElement [] elements = menuBar.getSubElements();
      for (int i = 0; i < elements.length; i++) {
        if (((JMenu)elements[i]).getText().equals("Partial Plan")) {
          partialPlanMenu = (JMenu) elements[i];
        }
      }
    }
    assertNotNull("Failed to get \"Partial Plan\" menu.", partialPlanMenu);
    assertTrue("Failed to get \"Partial Plan\" menu.",
               partialPlanMenu.getText().equals("Partial Plan"));
    return partialPlanMenu;
  } // end urlEnter

  private TimelineView getTimelineView( String [] seqAndPlanNames) throws Exception {
    String sequenceName = seqAndPlanNames[0];
    String partialPlanName = seqAndPlanNames[1];
    String sequenceUrl = seqAndPlanNames[2];
    PwPlanningSequence planSequence =
      planWorks.getCurrentProject().getPlanningSequence( sequenceUrl);
    PwPartialPlan partialPlan = null;
    while (partialPlan == null) {
      // System.err.println( "partialPlan still null");
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      partialPlan = planSequence.getPartialPlan( partialPlanName);
    }

    assertNotNull( "Failed to get Partial Plan.", partialPlan);
    ViewManager viewManager = null;
    viewManager = planWorks.getViewManager();
    assertNotNull( "Failed to get ViewManager.", viewManager);

    //System.err.println( "\n\nGot to here 3\n\n");
    MDIInternalFrame viewFrame = null;
    TimelineView timelineView = null;
    Thread.sleep( 3000);
    viewFrame =
      viewManager.openTimelineView( partialPlan, sequenceName +
                                    System.getProperty( "file.separator") +
                                    partialPlanName);
    assertNotNull("Failed to get timeline view MDI internal frame.", viewFrame);

    Container contentPane = viewFrame.getContentPane();
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof TimelineView) {
        timelineView = (TimelineView) contentPane.getComponent(i);
        break;
      }
    }
    assertNotNull("Failed to get TimelineView object.", timelineView);
    return timelineView;
  } // end getTimelineView

  private void validateTimelines( TimelineView timelineView) {
    List timelineNodes = timelineView.getTimelineNodeList();
    ListIterator timelineNodeIterator = timelineNodes.listIterator();
    while(timelineNodeIterator.hasNext()){
      TimelineNode timelineNode = (TimelineNode) timelineNodeIterator.next();
      String timelineName = timelineNode.getTimelineName();
      List slotNodes = timelineNode.getSlotNodeList();
      ListIterator slotNodeIterator = slotNodes.listIterator();
      if(timelineName.indexOf("Monkey1 : LOCATION_SV") != -1) {
        while(slotNodeIterator.hasNext()) {
          SlotNode slotNode = (SlotNode) slotNodeIterator.next();
          String slotName = slotNode.getPredicateName();
          if(slotName.indexOf("At") == -1 && slotName.indexOf("Going") == -1) {
            assertTrue("Invalid slot name for LOCATION_SV timeline.", false);
          }
        }
      }
      else if(timelineName.indexOf("Monkey1 : ALTITUDE_SV") != -1){
        while(slotNodeIterator.hasNext()) {
          SlotNode slotNode = (SlotNode) slotNodeIterator.next();
          String slotName = slotNode.getPredicateName();
          if(slotName.indexOf("LOW") == -1 && slotName.indexOf("CLIMBING") == -1 && 
             slotName.indexOf("HIGH") == -1 && slotName.indexOf("CLIMBING_DOWN") == -1) {
            assertTrue("Invalid slot name for ALTITUDE_SV", false);
          }
        }
      }
      else if(timelineName.indexOf("Monkey1 : BANANA_SV") != -1){
        while(slotNodeIterator.hasNext()) {
          SlotNode slotNode = (SlotNode) slotNodeIterator.next();
          String slotName = slotNode.getPredicateName();
          if(slotName.indexOf("NOT_HAVE_BANANA") == -1 && 
             slotName.indexOf("GRABBING_BANANA") == -1 && 
             slotName.indexOf("HAVE_BANANA") == -1) {
            assertTrue("Invalid slot name for BANANA_SV", false);
          }
        }
      }
      else {
        assertTrue("Invalid timeline name.", false);
      }
    }
  } // end validateTimelines

  private void exitPlanWorks( JMenuBar menuBar) throws Exception {
    // exit from CreateProject Test
    JMenu fileMenu = null;
    MenuElement [] elements = menuBar.getSubElements();
    for(int i = 0; i < elements.length; i++) {
      if(((JMenu)elements[i]).getText().equals("File")) {
        fileMenu = (JMenu) elements[i];
      }
    }
    assertNotNull("Failed to get \"File\" menu", fileMenu);
    assertTrue("Failed to get \"File\" menu.", fileMenu.getText().equals("File"));
    JMenuItem exitItem = null;
    for(int i = 0; i < fileMenu.getItemCount(); i++) {
      if(fileMenu.getItem(i).getText().equals("Exit")) {
        exitItem = fileMenu.getItem(i);
      }
    }
    assertNotNull("Failed to get \"Exit\" item.", exitItem);
    assertTrue("Failed to get \"Exit\" item.", exitItem.getText().equals("Exit"));
    helper.enterClickAndLeave(new MouseEventData(this, fileMenu));
    helper.enterClickAndLeave(new MouseEventData(this, exitItem));
  } // end exitPlanWorks


  private void confirmTimelineSpec(ContentSpecWindow contentSpecWindow,
                                   TimelineView timelineView) throws Exception {
    JButton activateSpecButton = null;
    JButton resetSpecButton = null;
    GroupBox timelineGroup = null;
    for(int i = 0; i < contentSpecWindow.getComponentCount(); i++) {
      if(contentSpecWindow.getComponent(i) instanceof JButton) {
        if(((JButton)contentSpecWindow.getComponent(i)).getText().equals("Apply Spec")) {
          activateSpecButton = (JButton) contentSpecWindow.getComponent(i);
        }
        else if(((JButton)contentSpecWindow.getComponent(i)).getText().equals("Reset Spec")) {
          System.err.println("found reset spec button");
          resetSpecButton = (JButton) contentSpecWindow.getComponent(i);
        }
      }
      else if(contentSpecWindow.getComponent(i) instanceof TimelineGroupBox) {
        timelineGroup = (GroupBox) contentSpecWindow.getComponent(i);
      }
    }
    assertNotNull("Failed to get \"Apply Spec\" button.", activateSpecButton);
    assertNotNull("Failed to get \"Reset Spec\" button.", resetSpecButton);
    assertNotNull("Failed to get Timline GroupBox.", timelineGroup);
    JTextField keyBox = null;
    NegationCheckBox negationBox = null;
    for(int i = 0; i < timelineGroup.getComponentCount(); i++) {
      if(timelineGroup.getComponent(i) instanceof TimelineBox) {
        TimelineBox timelineBox = (TimelineBox) timelineGroup.getComponent(i);
        for(int j = 0; j < timelineBox.getComponentCount(); j++) {
          if(timelineBox.getComponent(j) instanceof LogicComboBox) {
            if(!((LogicComboBox)timelineBox.getComponent(j)).isEnabled()) {
              System.err.println("Found first timeline box!");
              for(int k = 0; k < timelineBox.getComponentCount(); k++) {
                System.err.println(timelineBox.getComponentCount() + ":" + k);
                if(timelineBox.getComponent(k) instanceof NegationCheckBox) {
                  negationBox = (NegationCheckBox) timelineBox.getComponent(k);
                }
                else if(timelineBox.getComponent(k) instanceof JTextField) {
                  keyBox = (JTextField) timelineBox.getComponent(k);
                }
              }
            }
          }
        }
      }
    }
    assertNotNull("Failed to get key text field.", keyBox);
    assertNotNull("Failed to get negation check box.", negationBox);
    keyBox.setText("K3");
    helper.enterClickAndLeave(new MouseEventData(this, activateSpecButton));
    List timelineNodes;
    while((timelineNodes = timelineView.getTimelineNodeList()) == null) {
      Thread.sleep(50);
    }
    //Thread.sleep(2000);
    assertTrue("Content spec not specing correctly: Too many timeline nodes.",
               timelineNodes.size() == 1);
    TimelineNode timeline = (TimelineNode) timelineNodes.toArray()[0];
    assertTrue("Content spec specified incorrect timeline.", 
               timeline.getTimelineName().indexOf("Monkey1 : LOCATION_SV") != -1);
    List slotNodes = timeline.getSlotNodeList();
    ListIterator slotNodeIterator = slotNodes.listIterator();
    while(slotNodeIterator.hasNext()) {
      SlotNode slotNode = (SlotNode) slotNodeIterator.next();
      String slotName = slotNode.getPredicateName();
      if(slotName.indexOf("At") == -1 && slotName.indexOf("Going") == -1) {
        assertTrue("Invalid slot name for LOCATION_SV timeline.", false);
      }
    }
    negationBox.setSelected(true);
    helper.enterClickAndLeave(new MouseEventData(this, activateSpecButton));
    while((timelineNodes = timelineView.getTimelineNodeList()) == null) {
      Thread.sleep(50);
    }
    assertTrue("Content spec not specing correctly: incorrect number of timelines.",
               timelineNodes.size() == 2);
    Object [] temp = timelineNodes.toArray();
    TimelineNode [] timelines = new TimelineNode[temp.length];
    System.arraycopy(temp, 0, timelines, 0, temp.length);
    for(int i = 0; i < timelines.length; i++) {
      slotNodeIterator = timelines[i].getSlotNodeList().listIterator();
      assertTrue("Invalid timeline name.", timelines[i].getTimelineName().indexOf("Monkey1 : ALTITUDE_SV") != -1 || timelines[i].getTimelineName().indexOf("Monkey1 : BANANA_SV") != -1);
      if(timelines[i].getTimelineName().indexOf("Monkey1 : ALTITUDE_SV") != -1) {
        while(slotNodeIterator.hasNext()) {
          SlotNode slotNode =  (SlotNode) slotNodeIterator.next();
          String slotName = slotNode.getPredicateName();
          if(slotName.indexOf("LOW") == -1 && slotName.indexOf("HIGH") == -1 &&
             slotName.indexOf("CLIMBING") == -1 && slotName.indexOf("CLIMBING_DOWN") == -1) {
            assertTrue("Invalid slot name for ALTITUDE_SV timeline: ".concat(slotName), false);
          }
        }
      }
      else if(timelines[i].getTimelineName().indexOf("Monkey1 : BANANA_SV") != -1) {
        while(slotNodeIterator.hasNext()) {
          SlotNode slotNode = (SlotNode) slotNodeIterator.next();
          String slotName = slotNode.getPredicateName();
          if(slotName.indexOf("NOT_HAVE_BANANA") == -1 && 
             slotName.indexOf("GRABBING_BANANA") == -1 && 
             slotName.indexOf("HAVE_BANANA") == -1) {
            assertTrue("Invalid slot name for BANANA_SV", false);
          }
        }
      }
    }
    helper.enterClickAndLeave(new MouseEventData(this, resetSpecButton));
    while(timelineView.getTimelineNodeList() == null) {
      Thread.sleep(50);
    }
    validateTimelines(timelineView);
    assertTrue("Reset spec didn't reset text box", keyBox.getText().trim().equals(""));
    assertTrue("Reset spec didn't reset check box", !negationBox.isSelected());
  }

  /**
   * <code>testOpenProject</code>
   *
   * @exception Exception if an error occurs
   */
  /*  public void testOpenProject() throws Exception {
    System.err.println( "\n\nOpenProjectTestCase\n\n");
    awtSleep();
    Set windows = helper.getWindows();
    assertEquals("PlanWorks window failed to open", 1, windows.size());
    frame = (JFrame)(windows.toArray())[0];
    assertNotNull("Failed to get frame from set", frame);
    JMenuBar menuBar = frame.getJMenuBar();
    assertNotNull("Failed to get menu bar from frame", menuBar);
    JMenu projectMenu = null;
    MenuElement [] elements = menuBar.getSubElements();
    for(int i = 0; i < elements.length; i++) {
      if(((JMenu)elements[i]).getText().equals("Project")) {
        projectMenu = (JMenu) elements[i];
      }
    }
    assertNotNull("Failed to get \"Project\" menu", projectMenu);
    assertTrue("Failed to get \"Project\" menu.", projectMenu.getText().equals("Project"));
    JMenuItem openItem = null;
    for(int i = 0; i < projectMenu.getItemCount(); i++) {
      if(projectMenu.getItem(i).getText().equals("Open ...")) {
        openItem = projectMenu.getItem(i);
      }
    }
    assertNotNull("Failed to get \"Open ...\" item.", openItem);
    assertTrue("Failed to get \"Open ...\" item.", openItem.getText().equals("Open ..."));
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, openItem));

    String [] seqAndPlanNames = selectTimelineView();

    TimelineView timelineView = getTimelineView( seqAndPlanNames);

    validateTimelines( timelineView);

//     while ( true) {
//       try {
//         Thread.currentThread().sleep(50);
//       } catch (InterruptedException excp) {
//       }
//     }
    exitPlanWorks( menuBar);
  } // end testOpenProject
  */

  public void testOpenAndContentSpec() throws Exception {
    System.err.println( "\n\nContentSpecTestCase\n\n");
    awtSleep();
    Set windows = helper.getWindows();
    assertEquals("PlanWorks window failed to open", 1, windows.size());
    frame = (JFrame)(windows.toArray())[0];
    assertNotNull("Failed to get frame from set", frame);
    JMenuBar menuBar = frame.getJMenuBar();
    assertNotNull("Failed to get menu bar from frame", menuBar);
    JMenu projectMenu = null;
    MenuElement [] elements = menuBar.getSubElements();
    for(int i = 0; i < elements.length; i++) {
      if(((JMenu)elements[i]).getText().equals("Project")) {
        projectMenu = (JMenu) elements[i];
      }
    }
    assertNotNull("Failed to get \"Project\" menu", projectMenu);
    assertTrue("Failed to get \"Project\" menu.", projectMenu.getText().equals("Project"));
    JMenuItem openItem = null;
    for(int i = 0; i < projectMenu.getItemCount(); i++) {
      if(projectMenu.getItem(i).getText().equals("Open ...")) {
        openItem = projectMenu.getItem(i);
      }
    }
    assertNotNull("Failed to get \"Open ...\" item.", openItem);
    assertTrue("Failed to get \"Open ...\" item.", openItem.getText().equals("Open ..."));
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, openItem));

    String [] seqAndPlanNames = selectTimelineView();

    //<<<<<<< TimelineViewTest.java
    TimelineView timelineView = getTimelineView( seqAndPlanNames);
    Container contentPane = frame.getContentPane();
    JDesktopPane desktopPane = null;
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof JDesktopPane) {
        desktopPane = (JDesktopPane) contentPane.getComponent(i);
      }
    }
    
    assertNotNull("Failed to get Desktop Pane.", desktopPane);
    JInternalFrame [] internalFrames = desktopPane.getAllFrames();
    JInternalFrame contentSpecFrame = null;
    for(int i = 0; i < internalFrames.length; i++) {
      if(internalFrames[i].getTitle().indexOf("Content") != -1) {
        contentSpecFrame = internalFrames[i];
        break;
      }
    }
    assertNotNull("Failed to get Content Specification frame.", contentSpecFrame);
    validateTimelines( timelineView);
    contentSpecFrame.setSelected(true);
    contentPane = contentSpecFrame.getContentPane();
    ContentSpecWindow contentSpecWindow = null;
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof ContentSpecWindow) {
        contentSpecWindow = (ContentSpecWindow) contentPane.getComponent(i);
        break;
      }
    }
    assertNotNull("Failed to get Content Specification window.");
    confirmTimelineSpec(contentSpecWindow, timelineView);
    exitPlanWorks( menuBar);
  }
  
  //=======
  /**
   * <code>suite</code> - create the test cases for the TestRunner
   *
   * @return - <code>TestSuite</code> - 
   */
  //>>>>>>> 1.13
  public static TestSuite suite() {
    TestSuite testSuite = new TestSuite();
    testSuite.addTest( new TimelineViewTest( "testCreateProject", "create"));
    //testSuite.addTest( new TimelineViewTest( "testOpenProject", "open"));
    testSuite.addTest( new TimelineViewTest("testOpenAndContentSpec", "contentSpec"));
    return testSuite;
  }

  /**
   * <code>main</code> - run the test suite
   *
   * @param args - <code>String[]</code> - 
   */
  public static void main( String[] args) {
    PlanWorks.name = "";
    for (int argc = 0; argc < args.length; argc++) {
      // System.err.println( "argc " + argc + " " + args[argc]);
      if (argc == 0) {
        PlanWorks.name = args[argc];
      } else {
        System.err.println( "argument '" + args[argc] + "' not handled");
        System.exit(-1);
      }
    }
    PlanWorks.osName = System.getProperty("os.name");
    PlanWorks.planWorksRoot = System.getProperty( "planworks.root");

    TestRunner.run( suite());
  }
}
 
    
