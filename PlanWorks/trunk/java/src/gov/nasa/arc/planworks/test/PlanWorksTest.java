//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksTest.java,v 1.17 2003-09-29 23:52:11 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
// import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
// import junit.extensions.jfcunit.eventdata.EventDataConstants;
// import junit.extensions.jfcunit.eventdata.KeyEventData;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.TestSuite; 
import junit.textui.TestRunner;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.SlotNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenLink;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.ContentSpecWindow;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.GroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.LogicComboBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.NegationCheckBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.PredicateBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.PredicateGroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.TimeIntervalBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.TimeIntervalGroupBox;
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

    /*try {
      PwProject.initProjects();
    } catch (ResourceNotFoundException rnfExcep) {
      System.err.println( rnfExcep);
      System.exit( -1);
      }*/

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
                          System.getProperty("file.separator")/* + sequenceName*/);
      System.err.println("Set default.project.name: " + System.getProperty("default.project.name"));
      System.err.println("Set default.sequence.dir: " + System.getProperty("default.sequence.dir"));
    } else if (testType.equals( "emptySlots")) {
      projectName = "emptySlots";
      System.setProperty( "default.project.name", projectName);
      System.setProperty( "default.sequence.dir",
                          System.getProperty( "planworks.test.data.dir") +
                          System.getProperty("file.separator") + projectName +
                          System.getProperty( "file.separator")/* + sequenceName*/);
    } else {
      throw new Exception( "setup: testType " + testType + " not handled"); 
    }
    planWorks.getSequenceDirChooser().setCurrentDirectory
      ( new File( System.getProperty( "default.sequence.dir")));
    File [] temp = new File [1];
    temp[0] = new File(System.getProperty("default.sequence.dir") +
                       System.getProperty("file.separator") + "seq0");
    planWorks.getSequenceDirChooser().setSelectedFiles(temp);
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

      addAndDeleteSequences();

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

    seqAndPlanNames = selectView( "Temporal Extent");
    TemporalExtentView temporalExtentView = getTemporalExtentView( seqAndPlanNames);

    if(testType.equals("freeTokens")) {
      validateFreeTokensTemporalExtent( temporalExtentView);
    }

    seqAndPlanNames = selectView( "Constraint Network");
    ConstraintNetworkView constraintNetworkView = getConstraintNetworkView( seqAndPlanNames);

    if (testType.equals( "create")) {
      validateMonkeyConstraintsOpen( constraintNetworkView);
      validateMonkeyConstraintsClose( constraintNetworkView);
    }
    exitPlanWorks( menuBar);
  } // end createProject

  private String [] selectView( String viewName) throws Exception {
    // clicking enter on CreateProject or OpenProject dialog creates Partial Plan menu
    awtSleep();
    Thread.sleep( 1000);
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
    found:
    for (int i = 0; i < partialPlanMenu.getItemCount(); i++) {
      System.err.println( "partialPlanMenu.getItem(i) " + i + " '" +
                          partialPlanMenu.getItem(i).getText() + "' sequenceName '" +
                          sequenceName + "'");
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
        if (((JMenu)elements[i]).getText().equals("Planning Sequence")) {
          partialPlanMenu = (JMenu) elements[i];
        }
      }
    }
    assertNotNull("Failed to get \"Planning Sequence\" menu.", partialPlanMenu);
    assertTrue("Failed to get \"Planning Sequence\" menu.",
               partialPlanMenu.getText().equals("Planning Sequence"));
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
      viewManager.openView( partialPlan,
                            (String) PlanWorks.viewNameToViewClassMap.
                            get( PlanWorks.TIMELINE_VIEW));
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
    viewFrame =
      viewManager.openView( partialPlan,
                            (String) PlanWorks.viewNameToViewClassMap.
                            get( PlanWorks.TOKEN_NETWORK_VIEW));
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

  private TemporalExtentView getTemporalExtentView( String [] seqAndPlanNames) throws Exception {
    String sequenceName = seqAndPlanNames[0];
    String partialPlanName = seqAndPlanNames[1];
    String sequenceUrl = seqAndPlanNames[2];
    PwPartialPlan partialPlan = getPartialPlan( sequenceUrl, partialPlanName);

    ViewManager viewManager = null;
    viewManager = planWorks.getViewManager();
    assertNotNull( "Failed to get ViewManager.", viewManager);

    //System.err.println( "\n\nGot to here 3\n\n");
    MDIInternalFrame viewFrame = null;
    TemporalExtentView temporalExtentView = null;
    Thread.sleep( 3000);
    viewFrame =
      viewManager.openView( partialPlan, 
                            (String) PlanWorks.viewNameToViewClassMap.
                            get( PlanWorks.TEMPORAL_EXTENT_VIEW));
    assertNotNull("Failed to get temporalExtent view MDI internal frame.", viewFrame);

    Container contentPane = viewFrame.getContentPane();
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof TemporalExtentView) {
        temporalExtentView = (TemporalExtentView) contentPane.getComponent(i);
        break;
      }
    }
    assertNotNull("Failed to get TemporalExtentView object.", temporalExtentView);
    return temporalExtentView;
  } // end getTemporalExtentView

  private ConstraintNetworkView getConstraintNetworkView( String [] seqAndPlanNames)
    throws Exception {
    String sequenceName = seqAndPlanNames[0];
    String partialPlanName = seqAndPlanNames[1];
    String sequenceUrl = seqAndPlanNames[2];
    PwPartialPlan partialPlan = getPartialPlan( sequenceUrl, partialPlanName);

    ViewManager viewManager = null;
    viewManager = planWorks.getViewManager();
    assertNotNull( "Failed to get ViewManager.", viewManager);

    //System.err.println( "\n\nGot to here 3\n\n");
    MDIInternalFrame viewFrame = null;
    ConstraintNetworkView constraintNetworkView = null;
    Thread.sleep( 3000);
    viewFrame =
      viewManager.openView( partialPlan,
                            (String) PlanWorks.viewNameToViewClassMap.
                            get( PlanWorks.CONSTRAINT_NETWORK_VIEW));
    assertNotNull("Failed to get constraintNetwork view MDI internal frame.", viewFrame);

    Container contentPane = viewFrame.getContentPane();
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof ConstraintNetworkView) {
        constraintNetworkView = (ConstraintNetworkView) contentPane.getComponent(i);
        break;
      }
    }
    assertNotNull("Failed to get ConstraintNetworkView object.", constraintNetworkView);
    return constraintNetworkView;
  } // end getConstraintNetworkView

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
    List tokenNodeList = null;
    while (tokenNodeList == null) {
      // System.err.println( "tokenNetworkView tokenNodeList still null");
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      tokenNodeList = tokenNetworkView.getNodeList();
    }
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

  private void validateFreeTokensTemporalExtent( TemporalExtentView temporalExtentView) {
    List temporalNodeList = temporalExtentView.getTemporalNodeList();
    ListIterator temporalNodeIterator = temporalNodeList.listIterator();
    int numTemporalNodes = 0;
    while(temporalNodeIterator.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeIterator.next();
      numTemporalNodes++;
    }
    assertTrue("Incorrect number of temporal nodes in Temporal Extent View",
               numTemporalNodes == 19);
  } // end validateFreeTokensTemporalExtent

  private void validateEmptySlotsTimelines(TimelineView timelineView) {
    ListIterator timelineNodeIterator = timelineView.getTimelineNodeList().listIterator();
    timelineNodeIterator.next();
    timelineNodeIterator.next();
    TimelineNode thirdTimeline = (TimelineNode) timelineNodeIterator.next();
    List slotNodeList = thirdTimeline.getSlotNodeList();
    SlotNode fourthNode = (SlotNode) slotNodeList.get(3);
    SlotNode seventhNode = (SlotNode) slotNodeList.get(6);
    assertTrue("Incorrectly displayed fourth node as empty slot.  Displayed '" +
               fourthNode.getPredicateName() + "'", 
               fourthNode.getPredicateName().trim().equals("<empty>"));
    assertTrue("Incorrectly displayed seventh node as empty slot.  Displayed '" + 
               seventhNode.getPredicateName() + "'",
               seventhNode.getPredicateName().trim().equals("<empty>"));
  }

  private void validateMonkeyConstraintsOpen( ConstraintNetworkView constraintNetworkView)
    throws Exception {
    List tokenNodeList = constraintNetworkView.getTokenNodeList();
    List variableNodeList = constraintNetworkView.getVariableNodeList();
    List constraintNodeList = constraintNetworkView.getConstraintNodeList();
    TokenNode t19 = null;
    Iterator tokenNodeItr = tokenNodeList.iterator();
    while (tokenNodeItr.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeItr.next();
      if (tokenNode.getToken().getId().equals( new Integer( 19))) {
        t19 = tokenNode;
        break;
      }
    }
    assertTrue( "tokenNode id=19 not found", (t19 != null));
    t19.doMouseClick( MouseEvent.BUTTON1_MASK,
                       new Point( (int) t19.getLocation().getX(),
                                  (int) t19.getLocation().getY()),
                       new Point( 0, 0), constraintNetworkView.getJGoView());
    Thread.sleep( 3000);
    VariableNode v56 = null;
    Iterator variableNodeItr = variableNodeList.iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (variableNode.getVariable().getId().equals( new Integer( 56)) &&
          variableNode.isVisible()) {
        v56 = variableNode;
        break;
      }
    }
    assertTrue( "variableNode id=56 not found", (v56 != null));
    v56.doMouseClick( MouseEvent.BUTTON1_MASK,
                      new Point( (int) v56.getLocation().getX(),
                                 (int) v56.getLocation().getY()),
                      new Point( 0, 0), constraintNetworkView.getJGoView());
    Thread.sleep( 3000);
    ConstraintNode c108 = null;
    Iterator constraintNodeItr = constraintNodeList.iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      if (constraintNode.getConstraint().getId().equals( new Integer( 108)) &&
          constraintNode.isVisible()) {
        c108 = constraintNode;
        break;
      }
    }
    assertTrue( "constraintNode id=108 not found", (c108 != null));
    c108.doMouseClick( MouseEvent.BUTTON1_MASK,
                      new Point( (int) c108.getLocation().getX(),
                                 (int) c108.getLocation().getY()),
                      new Point( 0, 0), constraintNetworkView.getJGoView());
    Thread.sleep( 3000);
    VariableNode v57 = null;
    variableNodeItr = variableNodeList.iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (variableNode.getVariable().getId().equals( new Integer( 57)) &&
          variableNode.isVisible()) {
        v57 = variableNode;
        break;
      }
    }
   assertTrue( "variableNode id=57 not found", (v57 != null));
    v57.doMouseClick( MouseEvent.BUTTON1_MASK,
                      new Point( (int) v57.getLocation().getX(),
                                 (int) v57.getLocation().getY()),
                      new Point( 0, 0), constraintNetworkView.getJGoView());
    Thread.sleep( 3000);
    TokenNode t20 = null;
    tokenNodeItr = tokenNodeList.iterator();
    while (tokenNodeItr.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeItr.next();
      if (tokenNode.getToken().getId().equals( new Integer( 20))) {
        t20 = tokenNode;
        break;
      }
    }
    assertTrue( "tokenNode id=20 not found", (t20 != null));
    t20.doMouseClick( MouseEvent.BUTTON1_MASK,
                       new Point( (int) t20.getLocation().getX(),
                                  (int) t20.getLocation().getY()),
                       new Point( 0, 0), constraintNetworkView.getJGoView());
  } // end validateMonkeyConstraintsOpen

  private void validateMonkeyConstraintsClose( ConstraintNetworkView constraintNetworkView)
    throws Exception {
    List tokenNodeList = constraintNetworkView.getTokenNodeList();
    List variableNodeList = constraintNetworkView.getVariableNodeList();
    List constraintNodeList = constraintNetworkView.getConstraintNodeList();
    Thread.sleep( 3000);
    TokenNode t20 = null;
    Iterator tokenNodeItr = tokenNodeList.iterator();
    while (tokenNodeItr.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeItr.next();
      if (tokenNode.getToken().getId().equals( new Integer( 20))) {
        t20 = tokenNode;
        break;
      }
    }
    assertTrue( "tokenNode id=20 not found", (t20 != null));
    t20.doMouseClick( MouseEvent.BUTTON1_MASK,
                       new Point( (int) t20.getLocation().getX(),
                                  (int) t20.getLocation().getY()),
                       new Point( 0, 0), constraintNetworkView.getJGoView());
    Thread.sleep( 3000);
    VariableNode v57 = null;
    Iterator variableNodeItr = variableNodeList.iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (variableNode.getVariable().getId().equals( new Integer( 57)) &&
          variableNode.isVisible()) {
        v57 = variableNode;
        break;
      }
    }
    assertTrue( "variableNode id=57 not found", (v57 != null));
    v57.doMouseClick( MouseEvent.BUTTON1_MASK,
                      new Point( (int) v57.getLocation().getX(),
                                 (int) v57.getLocation().getY()),
                      new Point( 0, 0), constraintNetworkView.getJGoView());
    Thread.sleep( 3000);
    ConstraintNode c108 = null;
    Iterator constraintNodeItr = constraintNodeList.iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      if (constraintNode.getConstraint().getId().equals( new Integer( 108)) &&
          constraintNode.isVisible()) {
        c108 = constraintNode;
        break;
      }
    }
    assertTrue( "constraintNode id=108 not found", (c108 != null));
    c108.doMouseClick( MouseEvent.BUTTON1_MASK,
                      new Point( (int) c108.getLocation().getX(),
                                 (int) c108.getLocation().getY()),
                      new Point( 0, 0), constraintNetworkView.getJGoView());
    Thread.sleep( 3000);
    VariableNode v56 = null;
    variableNodeItr = variableNodeList.iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (variableNode.getVariable().getId().equals( new Integer( 56)) &&
          variableNode.isVisible()) {
        v56 = variableNode;
        break;
      }
    }
    assertTrue( "variableNode id=56 not found", (v56 != null));
    v56.doMouseClick( MouseEvent.BUTTON1_MASK,
                      new Point( (int) v56.getLocation().getX(),
                                 (int) v56.getLocation().getY()),
                      new Point( 0, 0), constraintNetworkView.getJGoView());
    Thread.sleep( 3000);
    TokenNode t19 = null;
    tokenNodeItr = tokenNodeList.iterator();
    while (tokenNodeItr.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeItr.next();
      if (tokenNode.getToken().getId().equals( new Integer( 19))) {
        t19 = tokenNode;
        break;
      }
    }
    assertTrue( "tokenNode id=19 not found", (t19 != null));
    t19.doMouseClick( MouseEvent.BUTTON1_MASK,
                       new Point( (int) t19.getLocation().getX(),
                                  (int) t19.getLocation().getY()),
                       new Point( 0, 0), constraintNetworkView.getJGoView());

    Thread.sleep( 3000);
    int visibleNodeCnt = 0;
    variableNodeItr = variableNodeList.iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (variableNode.isVisible()) {
        visibleNodeCnt++;
      }
    }
    assertTrue( "Count of visible variable nodes was " + visibleNodeCnt + ", not 2",
                (visibleNodeCnt == 2));
    visibleNodeCnt = 0;
    constraintNodeItr = constraintNodeList.iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      if (constraintNode.isVisible()) {
        visibleNodeCnt++;
      }
    }
    assertTrue( "Count of visible constraint nodes was " + visibleNodeCnt + ", not 0",
                (visibleNodeCnt == 0));
  } // end validateMonkeyConstraintsClose

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
    GroupBox predicateGroup = null;
    GroupBox timeIntervalGroup = null;
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
      else if(contentSpecWindow.getComponent(i) instanceof JPanel) {
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
        }
      }
    }
    assertNotNull("Failed to get \"Apply Spec\" button.", activateSpecButton);
    assertNotNull("Failed to get \"Reset Spec\" button.", resetSpecButton);
    assertNotNull("Failed to get Timeline GroupBox.", timelineGroup);
    assertNotNull("Failed to get Predicate GroupBox.", predicateGroup);
    assertNotNull("Failed to get Time Interval GroupBox.", timeIntervalGroup);
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
    assertNotNull("Failed to get timeline field.", keyBox);
    assertNotNull("Failed to get negation check box.", negationBox);
    // apply timeline = Location ==> timelines: Location
    keyBox.setSelectedIndex(3);
    helper.enterClickAndLeave(new MouseEventData(this, activateSpecButton));
    Thread.sleep(2000);

    List timelineNodes = timelineView.getTimelineNodeList();
    int timelineNodeCnt = timelineNodes.size();

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
        assertTrue("Invalid slot name for LOCATION_SV timeline: ".concat(slotName), false);
      }
    }
    // not timeline = Location ==> timelines: altitude & banana
    negationBox.setSelected(true);
    helper.enterClickAndLeave(new MouseEventData(this, activateSpecButton));
    Thread.sleep(2000);

    timelineNodes = timelineView.getTimelineNodeList();
    timelineNodeCnt = timelineNodes.size();
    assertTrue("Content spec not specing correctly: incorrect number of timelines: " +
               timelineNodeCnt, timelineNodeCnt == 2);
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
            assertTrue("Invalid slot name for BANANA_SV: ".concat(slotName), false);
          }
        }
      }
    }
    // reset
    helper.enterClickAndLeave(new MouseEventData(this, resetSpecButton));
    Thread.sleep(2000); 

    validateMonkeyTimelines(timelineView);
    assertTrue("Reset spec didn't reset text box", keyBox.getSelectedIndex() == 0);
    assertTrue("Reset spec didn't reset check box", !negationBox.isSelected());

    // appply predicate At  ==> timelines: Location
    keyBox = null;
    negationBox = null;
    for(int i = 0; i < predicateGroup.getComponentCount(); i++) {
      if(predicateGroup.getComponent(i) instanceof PredicateBox) {
        PredicateBox predicateBox = (PredicateBox) predicateGroup.getComponent(i);
        for(int j = 0; j < predicateBox.getComponentCount(); j++) {
          if(predicateBox.getComponent(j) instanceof LogicComboBox) {
            if(!((LogicComboBox)predicateBox.getComponent(j)).isEnabled()) {
              System.err.println("Found first predicate box!");
              for(int k = 0; k < predicateBox.getComponentCount(); k++) {
                System.err.println(predicateBox.getComponentCount() + ":" + k);
                if(predicateBox.getComponent(k) instanceof NegationCheckBox) {
                  negationBox = (NegationCheckBox) predicateBox.getComponent(k);
                }
                else if(predicateBox.getComponent(k) instanceof JComboBox) {
                  keyBox = (JComboBox) predicateBox.getComponent(k);
                }
              }
            }
          }
        }
      }
    }
    assertNotNull("Failed to get predicate field.", keyBox);
    assertNotNull("Failed to get negation check box.", negationBox);
    keyBox.setSelectedIndex(1);
    helper.enterClickAndLeave(new MouseEventData(this, activateSpecButton));
    Thread.sleep(2000);

    timelineNodes = timelineView.getTimelineNodeList();
    timelineNodeCnt = timelineNodes.size();
    for(int i = 0; i < timelineNodes.size(); i++) {
      System.err.println("TIMELINE " + ((TimelineNode)timelineNodes.get(i)).getTimelineName() +
                         " is visible!");
    }
    assertTrue("Content spec not specing correctly.  Incorrect number of timeline nodes: " +
               timelineNodeCnt, timelineNodeCnt == 1);
    timeline = (TimelineNode) timelineNodes.toArray()[0];
    assertTrue("Content spec specified incorrect timeline.", 
               timeline.getTimelineName().indexOf("Monkey1 : LOCATION_SV") != -1);
    slotNodes = timeline.getSlotNodeList();
    slotNodeIterator = slotNodes.listIterator();
    while(slotNodeIterator.hasNext()) {
      SlotNode slot = (SlotNode) slotNodeIterator.next();
      assertTrue("Improperly specified slot.", (slot.isVisible() && slot.getPredicateName().indexOf("At") != -1) || (!slot.isVisible() && slot.getPredicateName().indexOf("At") == -1));
    }
    // apply predicate At, negation = true  ==> all timelines
    negationBox.setSelected(true);
    helper.enterClickAndLeave(new MouseEventData(this, activateSpecButton));
    Thread.sleep(2000);

    timelineNodes = timelineView.getTimelineNodeList();
    timelineNodeCnt = timelineNodes.size();
    assertTrue("Content spec not specing correctly.  Incorrect number of timeline nodes: " +
               timelineNodeCnt, timelineNodeCnt == 3);
    for(int i = 0; i < timelineNodes.size(); i++) {
      slotNodeIterator = ((TimelineNode)timelineNodes.get(i)).getSlotNodeList().listIterator();
      while(slotNodeIterator.hasNext()) {
        SlotNode slot = (SlotNode) slotNodeIterator.next();
        assertTrue("Improperly specified slot.", (slot.isVisible() && slot.getPredicateName().indexOf("At") == -1) || (!slot.isVisible() && slot.getPredicateName().indexOf("At") != -1));
      }
    }
    // reset
    helper.enterClickAndLeave(new MouseEventData(this, resetSpecButton));
    Thread.sleep(2000);
    // apply start interval = 1, end interval = 4 ==> all timelines
    validateMonkeyTimelines(timelineView);
    assertTrue("Reset spec didn't reset text box", keyBox.getSelectedIndex() == 0);
    assertTrue("Reset spec didn't reset check box", !negationBox.isSelected());
    JTextField start = null;
    JTextField end = null;
    negationBox = null;
    for(int i = 0; i < timeIntervalGroup.getComponentCount(); i++) {
      if(timeIntervalGroup.getComponent(i) instanceof TimeIntervalBox) {
        TimeIntervalBox tiBox = (TimeIntervalBox) timeIntervalGroup.getComponent(i);
        for(int j = 0; j < tiBox.getComponentCount(); j++) {
          if(tiBox.getComponent(j) instanceof LogicComboBox) {
            if(!((LogicComboBox)tiBox.getComponent(j)).isEnabled()) {
              System.err.println("Found first time interval box!");
              for(int k = 0; k < tiBox.getComponentCount(); k++) {
                System.err.println(tiBox.getComponentCount() + ":" + k);
                if(tiBox.getComponent(k) instanceof NegationCheckBox) {
                  negationBox = (NegationCheckBox) tiBox.getComponent(k);
                }
                else if(tiBox.getComponent(k) instanceof JLabel) {
                  JLabel tempLabel = (JLabel) tiBox.getComponent(k);
                  if(tempLabel.getText().trim().equals("Time Interval Start")) {
                    k++;
                    start = (JTextField) tiBox.getComponent(k);
                  }
                  else if(tempLabel.getText().trim().equals("End")) {
                    k++;
                    end = (JTextField) tiBox.getComponent(k);
                  }
                }
              }
            }
          }
        }
      }
    }
    assertNotNull("Failed to get time interval negation box.", negationBox);
    assertNotNull("Failed to get time interval start entry box.", start);
    assertNotNull("Failed to get time interval end entry box.", end);
    start.setText("1");
    end.setText("4");
    helper.enterClickAndLeave(new MouseEventData(this, activateSpecButton));
    Thread.sleep(2000);
    timelineNodes = timelineView.getTimelineNodeList();
    timelineNodeCnt = timelineNodes.size();
    assertTrue("Content spec not specing correctly: Too many timeline nodes.",
               timelineNodeCnt == 3);
    // reset
    helper.enterClickAndLeave(new MouseEventData(this, resetSpecButton));
    Thread.sleep(2000);
    validateMonkeyTimelines(timelineView);
    assertTrue("Reset spec didn't reset start box", start.getText().equals(""));
    assertTrue("Reset spec didn't reset end box", end.getText().equals(""));
    assertTrue("Reset spec didn't reset check box", !negationBox.isSelected()); 
  } // end confirmTimelineSpec

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
    assertTrue("\"Open ...\" item not enabled", openItem.isEnabled());
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, openItem));

    openProjectEnter();

    String [] seqAndPlanNames = selectView( "Timeline");
    TimelineView timelineView = getTimelineView( seqAndPlanNames);
    seqAndPlanNames = selectView( "Token Network");
    TokenNetworkView tokenNetworkView = getTokenNetworkView( seqAndPlanNames);
    seqAndPlanNames = selectView( "Temporal Extent");
    TemporalExtentView temporalExtentView = getTemporalExtentView( seqAndPlanNames);
    seqAndPlanNames = selectView( "Constraint Network");
    ConstraintNetworkView constraintNetworkView = getConstraintNetworkView( seqAndPlanNames);
    Thread.sleep( 1000);
    tileTheViews();
    Thread.sleep( 1000);
    validateMonkeyConstraintsOpen( constraintNetworkView);

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
  } // end testOpenAndContentSpec

  private void tileTheViews() throws Exception {
    JMenuBar menuBar = frame.getJMenuBar();
    assertNotNull("Failed to get menu bar from frame", menuBar);
    JMenu windowMenu = null;
    MenuElement [] elements = menuBar.getSubElements();
    for(int i = 0; i < elements.length; i++) {
      if(((JMenu)elements[i]).getText().equals("Window")) {
        windowMenu = (JMenu) elements[i];
      }
    }
    assertNotNull("Failed to get \"Window\" menu", windowMenu);
    assertTrue("Failed to get \"Window\" menu.", windowMenu.getText().equals("Window"));
    JMenuItem tileWindowsItem = null;
    for(int i = 0; i < windowMenu.getItemCount(); i++) {
      if ((windowMenu.getItem( i) != null) &&
          (windowMenu.getItem(i).getText().equals("Tile Windows"))) {
        tileWindowsItem = windowMenu.getItem(i);
        break;
      }
    }
    assertNotNull("Failed to get \"Tile Windows\" item.", tileWindowsItem);
    assertTrue("Failed to get \"Tile Windows\" item.",
               tileWindowsItem.getText().equals("Tile Windows"));
    assertTrue("\"Tile Windows\" item not enabled", tileWindowsItem.isEnabled());
    helper.enterClickAndLeave(new MouseEventData(this, windowMenu));
    helper.enterClickAndLeave(new MouseEventData(this, tileWindowsItem));
  } // end tileTheViews


  private void addAndDeleteSequences()  throws Exception {
    addSequence( "emptySlots", "seq0");
    Thread.sleep( 1000);
    sequenceName = "seq0 (1)";
    String [] seqAndPlanNames = selectView( "Timeline");
    TimelineView timelineView = getTimelineView( seqAndPlanNames);
    Thread.sleep( 1000);

    addSequence( "freeTokens", "seq0");
    sequenceName = "seq0 (2)";
    seqAndPlanNames = selectView( "Timeline");
    timelineView = getTimelineView( seqAndPlanNames);
    Thread.sleep( 1000);
 
    tileTheViews();
    Thread.sleep( 2000);

    deleteSequence( "emptySlots", "seq0");
    Thread.sleep( 1000);
    deleteSequence( "freeTokens", "seq0");

    // return to monkey sequence name
    sequenceName = "seq0";
  } // end addAndDeleteSequences


  private void addSequence( String seqParentName, String seqName) throws Exception {
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
    JMenuItem addSequenceItem = null;
    for(int i = 0; i < projectMenu.getItemCount(); i++) {
      if ((projectMenu.getItem( i) != null) &&
          (projectMenu.getItem(i).getText().equals("Add Sequence ..."))) {
        addSequenceItem = projectMenu.getItem(i);
        break;
      }
    }
    assertNotNull("Failed to get \"Add Sequence ...\" item.", addSequenceItem);
    assertTrue("Failed to get \"Add Sequence ...\" item.",
               addSequenceItem.getText().equals("Add Sequence ..."));
    assertTrue("\"Add Sequence ...\" item not enabled", addSequenceItem.isEnabled());
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, addSequenceItem));

    // seq dir chooser 
    JFileChooser fileChooser = null;
    fileChooser = helper.getShowingJFileChooser( planWorks);
    assertNotNull( "Select Sequence Directory Dialog not found:", fileChooser);
    Container projectSeqDialog = (Container) fileChooser;
    StringBuffer seqDir = new StringBuffer( PlanWorks.planWorksRoot);
    seqDir.append( System.getProperty( "file.separator")).append( "java");
    seqDir.append( System.getProperty( "file.separator")).append( "src");
    seqDir.append( System.getProperty( "file.separator")).append( "gov");
    seqDir.append( System.getProperty( "file.separator")).append( "nasa");
    seqDir.append( System.getProperty( "file.separator")).append( "arc");
    seqDir.append( System.getProperty( "file.separator")).append( "planworks");
    seqDir.append( System.getProperty( "file.separator")).append( "test");
    seqDir.append( System.getProperty( "file.separator")).append( "data");
    seqDir.append( System.getProperty( "file.separator")).append( seqParentName);
    fileChooser.setCurrentDirectory( new File( seqDir.toString()));
    fileChooser.setSelectedFile( new File( seqName));
    JButton okButton = null;
    okButton = (JButton) TestHelper.findComponent(JButton.class, projectSeqDialog, 4);
    System.err.println( "projectSeqDialog " + okButton.getText());
    assertNotNull("Could not find \"OK\" button", okButton);
    helper.enterClickAndLeave(new MouseEventData(this, okButton));
  } // end addSequence


  private void deleteSequence( String seqParentName, String seqName) throws Exception {
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
    JMenuItem deleteSequenceItem = null;
    for(int i = 0; i < projectMenu.getItemCount(); i++) {
      if ((projectMenu.getItem( i) != null) &&
          (projectMenu.getItem(i).getText().equals("Delete Sequence ..."))) {
        deleteSequenceItem = projectMenu.getItem(i);
        break;
      }
    }
    assertNotNull("Failed to get \"Delete Sequence ...\" item.", deleteSequenceItem);
    assertTrue("Failed to get \"Delete Sequence ...\" item.",
               deleteSequenceItem.getText().equals("Delete Sequence ..."));
    assertTrue("\"Delete Sequence ...\" item not enabled", deleteSequenceItem.isEnabled());
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, deleteSequenceItem));
    Thread.sleep( 1000);

    // delete sequence dialog
    List dialogs = null;
    dialogs = helper.getShowingDialogs( planWorks);
    assertNotNull( "Delete Sequence Directory Dialog not found:", dialogs);
    Container sequenceDeleteDialog = (Container) dialogs.get(0);
    String seqDir = seqParentName + System.getProperty( "file.separator") + seqName;
    JButton okButton = null;
    JComboBox comboBox = null;
    String deleteSequence = null;
    for (int i = 0; i < sequenceDeleteDialog.getComponentCount(); i++) {
      JRootPane rootPane = (JRootPane) sequenceDeleteDialog.getComponent(i);
      // System.err.println( "component i " + i + " " + sequenceDeleteDialog.getComponent( i));
      for (int j = 0; j < rootPane.getComponentCount(); j++) {
        // System.err.println( "component j " + j + " " + rootPane.getComponent( j));
        if (rootPane.getComponent( j) instanceof JLayeredPane) {
          JLayeredPane layeredPane = (JLayeredPane) rootPane.getComponent( j);
          for (int jl = 0; jl < layeredPane.getComponentCount(); jl++) {
            // System.err.println( "layeredPane jl " + jl + " " + layeredPane.getComponent( jl));
            if (layeredPane.getComponent( jl) instanceof JPanel) {
              JPanel panel = (JPanel) layeredPane.getComponent( jl);
              for (int jlp = 0; jlp < panel.getComponentCount(); jlp++) {
                // System.err.println( "panel jlp " + jlp + " " +
                //                     panel.getComponent( jlp));
                if (panel.getComponent( jlp) instanceof JOptionPane) {
                  JOptionPane optionPane = (JOptionPane) panel.getComponent( jlp);
                  for (int jlpo = 0; jlpo < optionPane.getComponentCount(); jlpo++) {
                    // System.err.println( "optionPane jlpo " + jlpo + " " +
                    //                     optionPane.getComponent( jlpo));
                    if (jlpo == 0) {
                      JPanel panel0 = (JPanel) optionPane.getComponent( 0);
                      itemFound:
                      for (int jlpo0 = 0; jlpo0 < panel0.getComponentCount(); jlpo0++) {
                        // System.err.println( "optionPanePanel0 jlpo0 " + jlpo0 + " " +
                        //                 panel0.getComponent( jlpo0));
                        JComponent options = (JComponent) panel0.getComponent( 0);
                        for (int jlpo0o = 0; jlpo0o < options.getComponentCount(); jlpo0o++) {
                          // System.err.println( "options jlpo0o " + jlpo0o + " " +
                          //                     options.getComponent( jlpo0o));
                          JComponent options0 = (JComponent) options.getComponent( jlpo0o);
                          for (int jlpo0o0 = 0; jlpo0o0 < options0.getComponentCount();
                               jlpo0o0++) {
                            // System.err.println( "options0 jlpo0o0 " + jlpo0o0 + " " +
                            //                     options0.getComponent( jlpo0o0));
                            comboBox = (JComboBox) options0.getComponent( jlpo0o0);
                            for (int itemCnt = 0; itemCnt < comboBox.getItemCount(); itemCnt++) {
                              // System.err.println( "item " + itemCnt + " " +
                              //                     comboBox.getItemAt( itemCnt));
                              String itemString = (String) comboBox.getItemAt( itemCnt);
                              if (itemString.indexOf( seqDir) >= 0) {
                                deleteSequence = itemString;
                                comboBox.setSelectedItem( itemString);
                                break itemFound;
                              }
                            }
                          }
                        }
                      }
                    } else if (jlpo == 1) {
                      JPanel panel1 = (JPanel) optionPane.getComponent( 1);
                      for (int jlpo1 = 0; jlpo1 < panel1.getComponentCount(); jlpo1++) {
                        // System.err.println( "optionPanePanel1 jlpo1 " + jlpo1 + " " +
                        //                 panel1.getComponent( jlpo1));
                        okButton = (JButton) panel1.getComponent( 0);
                        break;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    assertNotNull( "Could not find comboBox ", comboBox);
    assertNotNull( "Could not find \"seqDir\" item", deleteSequence);
    System.err.println( "sequenceDeleteDialog " + okButton.getText());
    assertNotNull("Could not find \"OK\" button", okButton);
    helper.enterClickAndLeave( new MouseEventData( this, okButton));
  } // end deleteSequence


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
    String maxScreenValue = "false";
    for (int argc = 0; argc < args.length; argc++) {
      // System.err.println( "argc " + argc + " " + args[argc]);
      if (argc == 0) {
        PlanWorks.name = args[argc];
      } else if (argc == 1) {
        maxScreenValue = args[argc];
      } else {
        System.err.println( "argument '" + args[argc] + "' not handled");
        System.exit(-1);
      }
    }
    PlanWorks.osType = System.getProperty("os.type");
    PlanWorks.planWorksRoot = System.getProperty( "planworks.root");
    PlanWorks.isMaxScreen = false;
    if (maxScreenValue.equals( "true")) {
      PlanWorks.isMaxScreen = true;
    }

    TestRunner.run( suite());
  } // end main

} // end class PlanWorksTest
 
    













