//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksTest.java,v 1.6 2003-07-17 17:19:11 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Container;
// import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
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
import gov.nasa.arc.planworks.viz.views.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.nodes.SlotNode;
import gov.nasa.arc.planworks.viz.nodes.TokenLink;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
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
 * <code>PlanWorksTest</code> - JFCUnit test case for timeline view, along with
 *                  project management
 *
 * @author <a href="mailto:miatauro@email.arcnasa.gov">Michael Iatauro</a>
 *                  NASA Ames Research Center - Code IC
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PlanWorksTest extends JFCTestCase{

  private JFrame frame;
  private PlanWorks planWorks;
  private String testType;
  private String projectName;
  private String sequenceName;
  private String partialPlanName;

  /**
   * <code>PlanWorksTest</code> - constructor 
   *
   * @param test - <code>String</code> - 
   * @param testType - <code>String</code> - 
   */
  public PlanWorksTest(String test, String testType) {
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

    planWorks.setCurrentProjectName( "");
    sequenceName = "seq0";
    partialPlanName = "step0";
    // set proper test filesi
    if ((testType.equals( "create")) || (testType.equals("contentSpec"))) {
      // System.getProperty( "default.project.name")
      // System.getProperty( "default.sequence.dir")
      projectName = "monkey";
      System.setProperty( "default.project.name", projectName);
      System.setProperty( "default.sequence.dir",
                          System.getProperty( "planworks.test.data.dir") +
                          System.getProperty( "file.separator") + sequenceName);
    } else if (testType.equals( "freeTokens")) {
      projectName = "freeTokens";
      System.setProperty( "default.project.name", projectName);
      System.setProperty( "default.sequence.dir",
                          System.getProperty( "planworks.test.data.dir") +
                          System.getProperty( "file.separator") + projectName +
                          System.getProperty("file.separator") + sequenceName);
      System.err.println("Set default.project.name: " + System.getProperty("default.project.name"));
      System.err.println("Set default.sequence.dir: " + System.getProperty("default.sequence.dir"));
    } else if (testType.equals( "emptySlots")) {
      projectName = "emptySlots";
      System.setProperty( "default.project.name", projectName);
      System.setProperty( "default.sequence.dir",
                          System.getProperty( "planworks.test.data.dir") +
                          System.getProperty("file.separator") + projectName +
                          System.getProperty( "file.separator") + sequenceName);
    } else {
      throw new Exception( "setup: testType " + testType + " not handled"); 
    }
    planWorks.getSequenceDirChooser().setCurrentDirectory
      ( new File( System.getProperty( "default.sequence.dir")));


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
    createProject();
  } // end testCreateProject

  private void createProject() throws Exception {
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
      if ((projectMenu.getItem( i) != null) &&
          (projectMenu.getItem(i).getText().equals("Create ..."))) {
        createItem = projectMenu.getItem(i);
        break;
      }
    }
    assertNotNull("Failed to get \"Create ...\" item.", createItem);
    assertTrue("Failed to get \"Create ...\" item.", createItem.getText().equals("Create ..."));
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, createItem));

    createProjectEnter();

    String [] seqAndPlanNames = selectView( "Timeline");
    // System.err.println( "\n\nGot to here 1\n\n");
    TimelineView timelineView = getTimelineView( seqAndPlanNames);

    if (testType.equals( "create")) {
      validateMonkeyTimelines( timelineView);
    } else if (testType.equals( "freeTokens")) {
      validateFreeTokensTimelines( timelineView);
    } else if (testType.equals("emptySlots")) {
      validateEmptySlotsTimelines(timelineView);
    }
    seqAndPlanNames = selectView( "Token Network");
    TokenNetworkView tokenNetworkView = getTokenNetworkView( seqAndPlanNames);
    
    if(testType.equals("freeTokens")) {
      validateFreeTokensNetwork(tokenNetworkView);
    }
    exitPlanWorks( menuBar);
  } // end createProject

  private String [] selectView( String viewName) throws Exception {
    // clicking enter on CreateProject or OpenProject dialog creates Partial Plan menu
    awtSleep();
    JMenu partialPlanMenu = null;
    partialPlanMenu = getPartialPlanMenu();
    assertNotNull( "Failed to get partialPlanMenu", partialPlanMenu);
    helper.enterClickAndLeave(new MouseEventData(this, partialPlanMenu));
    Thread.sleep( 1000);
    JMenu sequenceMenu = null;
    JMenu partialPlanSubMenu = null;
    PlanWorks.SeqPartPlanViewMenuItem viewItem = null;
    String menuSequenceName = null;
    String menuPartialPlanName = null;
    found: for (int i = 0; i < partialPlanMenu.getItemCount(); i++) {
      if (partialPlanMenu.getItem(i).getText().equals( sequenceName)) {
        sequenceMenu = (JMenu) partialPlanMenu.getItem(i);
        helper.enterClickAndLeave(new MouseEventData(this, sequenceMenu));
        Thread.sleep( 1000);
        menuSequenceName = partialPlanMenu.getItem(i).getText();
        for (int j = 0; j < sequenceMenu.getItemCount(); j++) {
          if (sequenceMenu.getItem(j).getText().equals( partialPlanName)) {
            partialPlanSubMenu = (JMenu) sequenceMenu.getItem(j);
            helper.enterClickAndLeave(new MouseEventData(this, partialPlanSubMenu));
            Thread.sleep( 1000);
            menuPartialPlanName = sequenceMenu.getItem(j).getText();
            for (int k = 0; k < partialPlanSubMenu.getItemCount(); k++) {
              if (partialPlanSubMenu.getItem(k).getText().equals( viewName)) {
                viewItem =
                  (PlanWorks.SeqPartPlanViewMenuItem) partialPlanSubMenu.getItem(k);
                assertNotNull( "Failed to get view \"" + viewName + "\"", viewItem);
                helper.enterClickAndLeave(new MouseEventData(this, viewItem));
                break found;
              }
            }
          }
        }
      }
    }
    assertNotNull( "Failed to get sequence \"" + sequenceName + "\"", menuSequenceName);
    assertNotNull( "Failed to get partialPlan \"" + partialPlanName + "\"",
                   menuPartialPlanName);
    assertNotNull( "Failed to get view \"" + viewName + "\"", viewItem);
    assertTrue("Failed to get \"" + viewName + "\" submenu item.", 
               viewItem.getText().equals( viewName));
    // System.err.println( "\n\nGot to here 2\n\n");
    // helper.mousePressed(viewItem, EventDataConstants.DEFAULT_MOUSE_MODIFIERS, 1, false);
    return new String [] { sequenceName, partialPlanName , viewItem.getSeqUrl()};
  } // end selectTimelineView


  private void createProjectEnter() throws Exception {
    // project name dialog
    List dialogs = new ArrayList();
    while (dialogs.size() == 0) {
      // System.err.println( "wait for create dialog");
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      dialogs = TestHelper.getShowingDialogs("Create Project");
    }
    assertEquals("Create Project Dialog not found:", 1, dialogs.size());
    Container projectNameDialog = (Container) dialogs.get(0);
    // JTextField field = null;
    // field = (JTextField) TestHelper.findComponent(JTextField.class, jfcunitDialog, 0);
    // assertNotNull("Could not find \"Enter\" field", field);
    // helper.sendString(new StringEventData(this, field, "Harry Potter"));
    // helper.sendKeyAction(new KeyEventData(this, field, KeyEvent.VK_ENTER));
    // Cancel button -- cancel 2nd dialog created by PlanWorks
    JButton button = null;
    button = (JButton) TestHelper.findComponent(JButton.class, projectNameDialog, 0);
    System.err.println( "projectNameDialog " + button.getText());
    assertNotNull("Could not find \"Enter\" button", button);
    helper.enterClickAndLeave(new MouseEventData(this, button));

    // project sequence dialog
    JFileChooser fileChooser = null;
    while (fileChooser == null) {
      // System.err.println( "wait for create JFileChooser");
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      fileChooser = helper.getShowingJFileChooser( planWorks);
    }
    assertNotNull( "Select Sequence Directory Dialog not found:", fileChooser);
    Container projectSeqDialog = (Container) fileChooser;
    JButton okButton = null;
    okButton = (JButton) TestHelper.findComponent(JButton.class, projectSeqDialog, 4);
    // this does not work
//     for (int i = 0, n = 10; i < n; i++) {
//       button = (JButton) TestHelper.findComponent(JButton.class, projectSeqDialog, i);
//       System.err.println( "projectSeqDialog button " + button.getText());
//       if (button.getText().equals( "OK")) {
//         okButton = button;
//         break;
//       }
//     }
    System.err.println( "projectSeqDialog " + okButton.getText());
    assertNotNull("Could not find \"OK\" button", okButton);
    helper.enterClickAndLeave(new MouseEventData(this, okButton));
  } // end createProjectEnter


  private void openProjectEnter() throws Exception {
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
    Container projectNameDialog = (Container) dialogs.get(0);

    JButton okButton = null;
    for (int i = 0, n = 10; i < n; i++) {
      JButton button = (JButton) TestHelper.findComponent(JButton.class, projectNameDialog, i);
      if (button.getText().equals( "OK")) {
        okButton = button;
        break;
      }
    }
    assertNotNull("Could not find \"OK\" button", okButton);
    helper.enterClickAndLeave(new MouseEventData(this, okButton));
  } // end openProjectEnter

  private JMenu getPartialPlanMenu() {
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
  } // end getPartialPlanMenu

  private TimelineView getTimelineView( String [] seqAndPlanNames) throws Exception {
    String sequenceName = seqAndPlanNames[0];
    String partialPlanName = seqAndPlanNames[1];
    String sequenceUrl = seqAndPlanNames[2];
    PwPartialPlan partialPlan = getPartialPlan( sequenceUrl, partialPlanName);

    ViewManager viewManager = null;
    viewManager = planWorks.getViewManager();
    assertNotNull( "Failed to get ViewManager.", viewManager);

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

  private PwPartialPlan getPartialPlan( String sequenceUrl, String partialPlanName)
    throws Exception {
    PwPartialPlan partialPlan = null;
    PwPlanningSequence planSequence =
      planWorks.getCurrentProject().getPlanningSequence( sequenceUrl);
    while (partialPlan == null) {
      // System.err.println( "partialPlan still null");
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      partialPlan = planSequence.getPartialPlan( partialPlanName);
    }
    assertNotNull( "Failed to get Partial Plan.", partialPlan);
    return partialPlan;
  } // end getPartialPlan

  private TokenNetworkView getTokenNetworkView( String [] seqAndPlanNames) throws Exception {
    String sequenceName = seqAndPlanNames[0];
    String partialPlanName = seqAndPlanNames[1];
    String sequenceUrl = seqAndPlanNames[2];
    PwPartialPlan partialPlan = getPartialPlan( sequenceUrl, partialPlanName);

    ViewManager viewManager = null;
    viewManager = planWorks.getViewManager();
    assertNotNull( "Failed to get ViewManager.", viewManager);

    //System.err.println( "\n\nGot to here 3\n\n");
    MDIInternalFrame viewFrame = null;
    TokenNetworkView tokenNetworkView = null;
    Thread.sleep( 3000);
    long startTimeMSecs = (new Date()).getTime();
    viewFrame =
      viewManager.openTokenNetworkView( partialPlan, sequenceName +
                                    System.getProperty( "file.separator") +
                                    partialPlanName, startTimeMSecs);
    assertNotNull("Failed to get tokenNetwork view MDI internal frame.", viewFrame);

    Container contentPane = viewFrame.getContentPane();
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof TokenNetworkView) {
        tokenNetworkView = (TokenNetworkView) contentPane.getComponent(i);
        break;
      }
    }
    assertNotNull("Failed to get TokenNetworkView object.", tokenNetworkView);
    return tokenNetworkView;
  } // end getTokenNetworkView

  private void validateMonkeyTimelines( TimelineView timelineView) {
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
  } // end validateMonkeyTimelines

  private void validateFreeTokensTimelines( TimelineView timelineView) {
    List freeTokenNodeList = timelineView.getFreeTokenNodeList();
    ListIterator freeTokenNodeIterator = freeTokenNodeList.listIterator();
    while(freeTokenNodeIterator.hasNext()) {
      TokenNode tokenNode = (TokenNode) freeTokenNodeIterator.next();
      assertTrue("Non-free token asserts freedom.", tokenNode.isFreeToken());
      String predicateName = tokenNode.getPredicateName().trim();
      assertTrue("Invalid free token predicate '" + predicateName + "'", 
                 predicateName.equals("Predicate 0") || predicateName.equals("Predicate 1") ||
                 predicateName.equals("Predicate 6"));
    }
  } // end validateFreeTokensimelines

  private void validateFreeTokensNetwork(TokenNetworkView tokenNetworkView) {
    List tokenNodeList = tokenNetworkView.getNodeList();
    ListIterator tokenNodeIterator = tokenNodeList.listIterator();
    int numFreeTokens = 0;
    while(tokenNodeIterator.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeIterator.next();
      if(tokenNode.isFreeToken()) {
        String predicateName = tokenNode.getPredicateName().trim();
        assertTrue("Invalid free token predicate '" + predicateName + "'",
                   predicateName.equals("Predicate 0") || predicateName.equals("Predicate 1") ||
                   predicateName.equals("Predicate 6"));
        numFreeTokens++;
      }
    }
    assertTrue("Incorrect number of free tokens in Token Network View", numFreeTokens == 4);
    List tokenLinkList = tokenNetworkView.getLinkList();
    ListIterator tokenLinkIterator = tokenLinkList.listIterator();
    while(tokenLinkIterator.hasNext()) {
      TokenLink tokenLink = (TokenLink) tokenLinkIterator.next();
      assertTrue("Free token is super-goal.", 
                 tokenLink.getFromTokenNode().isFreeToken() == false);
    }
  }

  private void validateEmptySlotsTimelines(TimelineView timelineView) {
    ListIterator timelineNodeIterator = timelineView.getTimelineNodeList().listIterator();
    TimelineNode firstTimeline = (TimelineNode) timelineNodeIterator.next();
    List slotNodeList = firstTimeline.getSlotNodeList();
    SlotNode leadingNode = (SlotNode) slotNodeList.get(0);
    SlotNode trailingNode = (SlotNode) slotNodeList.get(slotNodeList.size()-1);
    assertTrue("Incorrectly displayed leading empty slot.  Displayed '" +
               leadingNode.getPredicateName() + "'", 
               leadingNode.getPredicateName().trim().equals("<empty>"));
    assertTrue("Incorrectly displayed trailing empty slot.  Displayed '" + 
               trailingNode.getPredicateName() + "'",
               trailingNode.getPredicateName().trim().equals("<empty>"));
  }

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
      JPanel panel = (JPanel)contentSpecWindow.getComponent(i);
      for(int j = 0; j < panel.getComponentCount(); j++) {
        if(panel.getComponent(j) instanceof JButton) {
          if(((JButton)panel.getComponent(j)).getText().equals("Apply Spec")) {
            activateSpecButton = (JButton) panel.getComponent(j);
          }
          else if(((JButton)panel.getComponent(j)).getText().equals("Reset Spec")) {
            System.err.println("found reset spec button");
            resetSpecButton = (JButton) panel.getComponent(j);
          }
        }
        else if(contentSpecWindow.getComponent(i) instanceof TimelineGroupBox) {
          timelineGroup = (GroupBox) contentSpecWindow.getComponent(i);
        }
      }
    }
    assertNotNull("Failed to get \"Apply Spec\" button.", activateSpecButton);
    assertNotNull("Failed to get \"Reset Spec\" button.", resetSpecButton);
    assertNotNull("Failed to get Timline GroupBox.", timelineGroup);
    JComboBox keyBox = null;
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
                else if(timelineBox.getComponent(k) instanceof JComboBox) {
                  keyBox = (JComboBox) timelineBox.getComponent(k);
                }
              }
            }
          }
        }
      }
    }
    assertNotNull("Failed to get key text field.", keyBox);
    assertNotNull("Failed to get negation check box.", negationBox);
    keyBox.setSelectedIndex(3);
    helper.enterClickAndLeave(new MouseEventData(this, activateSpecButton));
    List timelineNodes;
    while((timelineNodes = timelineView.getTimelineNodeList()) == null) {
      Thread.sleep(50);
    }
    //Thread.sleep(2000);
    int timelineNodeCnt = 0;
    for (int i = 0; i < timelineNodes.size(); i++) {
      if (((TimelineNode) timelineNodes.get( i)).isVisible()) {
        timelineNodeCnt++;
      }
    }
    assertTrue("Content spec not specing correctly: Too many timeline nodes.",
               timelineNodeCnt == 1);
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
    timelineNodeCnt = 0;
    List visibleTimelines = new ArrayList();
    for (int i = 0; i < timelineNodes.size(); i++) {
      if (((TimelineNode) timelineNodes.get( i)).isVisible()) {
        visibleTimelines.add( (TimelineNode) timelineNodes.get( i));
        timelineNodeCnt++;
      }
    }
    assertTrue("Content spec not specing correctly: incorrect number of timelines.",
               timelineNodeCnt == 2);
    Object [] temp = visibleTimelines.toArray();
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
    validateMonkeyTimelines(timelineView);
    assertTrue("Reset spec didn't reset text box", keyBox.getSelectedIndex() == 0);
    assertTrue("Reset spec didn't reset check box", !negationBox.isSelected());
  }

  public void testOpenAndContentSpec() throws Exception {
    System.err.println( "\n\nOpenAndContentSpecTestCase\n\n");
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
      if ((projectMenu.getItem( i) != null) &&
          (projectMenu.getItem(i).getText().equals("Open ..."))) {
        openItem = projectMenu.getItem(i);
        break;
      }
    }
    assertNotNull("Failed to get \"Open ...\" item.", openItem);
    assertTrue("Failed to get \"Open ...\" item.", openItem.getText().equals("Open ..."));
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, openItem));

    openProjectEnter();

   String [] seqAndPlanNames = selectView( "Timeline");
    TimelineView timelineView = getTimelineView( seqAndPlanNames);
    seqAndPlanNames = selectView( "Token Network");
    TokenNetworkView tokenNetworkView = getTokenNetworkView( seqAndPlanNames);

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
    validateMonkeyTimelines( timelineView);
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

  /**
   * <code>testFreeTokens</code>
   *
   * @exception Exception if an error occurs
   */
  public void testFreeTokens() throws Exception {
    System.err.println( "\n\nFreeTokensTestCase\n\n");
    createProject();
  } // end testFreeTokens

  /**
   * <code>testEmptySlots</code>
   *
   * @exception Exception if an error occurs
   */
  public void testEmptySlots() throws Exception {
    System.err.println( "\n\nEmptySlotsTestCase\n\n");
    createProject();
  } // end testEmptySlots

  /**
   * <code>suite</code> - create the test cases for the TestRunner
   *
   * @return - <code>TestSuite</code> - 
   */
  public static TestSuite suite() {
    TestSuite testSuite = new TestSuite();
    testSuite.addTest( new PlanWorksTest( "testCreateProject", "create"));
    testSuite.addTest( new PlanWorksTest( "testOpenAndContentSpec", "contentSpec"));
    testSuite.addTest( new PlanWorksTest( "testFreeTokens", "freeTokens"));
    testSuite.addTest( new PlanWorksTest( "testEmptySlots", "emptySlots"));
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
    PlanWorks.osType = System.getProperty("os.type");
    PlanWorks.planWorksRoot = System.getProperty( "planworks.root");

    TestRunner.run( suite());
  } // end main

} // end class PlanWorksTest
 
    
