//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimelineViewTest.java,v 1.11 2003-06-17 22:19:02 taylor Exp $
//
package gov.nasa.arc.planworks.viz.views.test;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.MenuElement;

import junit.extensions.jfcunit.*;
import junit.extensions.jfcunit.eventdata.EventDataConstants;
import junit.extensions.jfcunit.eventdata.KeyEventData;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.*;
import junit.textui.TestRunner;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ParseProjectUrl;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.nodes.SlotNode;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


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
public class TimelineViewTest extends JFCTestCase {

  private JFrame frame;
  private static PlanWorks planWorks;
  private boolean firstEntry = false;
  // private JFCTestHelper helper;
  private TestHelper helper;
  /** The thread the modal dialog will execute on */
  private Thread modalThread = null;
  /** Any exception thrown by the modal dialog */
  private Exception lastException = null;
  /** True if the application exited normally. */
  private boolean normalExit = false;
  /** Flag. True if the application has been started */
  private volatile boolean started = false;
  /** modal dialog */
  private ParseProjectUrl createProjectDialog;

  public TimelineViewTest(String test) {
    super(test);
  }
  
  public void setUp() throws Exception {
    // System.err.println( "setUp entered");
    if (started == true) {
      return;
    }
    helper = new JFCTestHelper();
    lastException = null;
    modalThread = new Thread(new Runnable() {
        public void run() {
          try {
            lastException = null;
            // System.err.println( "setUp: started " + started);
            started = true;
            // create dialog
            ParseProjectUrl createProjectDialog =
              new ParseProjectUrl( PlanWorks.planWorks);
            // System.err.println( "setUp: normalExit");
            normalExit = true;
          } catch (Exception e) {
            lastException = e;
          }
        }
      }, "ModalThread");
    modalThread.start();
    Thread.currentThread().yield();

    // Wait for the thread to start
    while (!started) {
      Thread.currentThread().sleep(50);
    }

    // Give a little extra time for the painting/construction
    Thread.currentThread().sleep(500);
    flushAWT();
    // checkException();
  }
  

  /**
   * Interrupt the modalThread if it is still running.
   * Then shutdown the fixtures used.
   *
   * @throws Exception may be thrown.
   */
  public void tearDown() throws Exception {
    if (lastException != null) {
      throw lastException;
    }
    if (modalThread.isAlive()) {
      modalThread.interrupt();
    }
    modalThread = null;
    helper.cleanUp(this);
    super.tearDown();
    System.exit(0);
  }
  
    /**
     * Check for the occurance of a Exception on the
     * modalThread. Rethrow the exception if one was generated.
     *
     * @throws Exception may be thrown.
     */
  private void checkException() throws Exception {
    System.err.println( "checkException");
    if (lastException != null) {
      throw lastException;
    }
  }

  public void testMain() throws Exception {
    // System.err.println( "testMain entered");
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

    // click enter on CreateProject dialog, which creates Partial Plan menu
    JMenu partialPlanMenu = testUrlEnter();

    // System.err.println( "\n\nGot to here 1\n\n");
    JMenu sequenceMenu = null;
    JMenu partialPlanSubMenu = null;
    JMenuItem timelineViewItem = null;
    String sequenceName = null;
    String partialPlanName = null;
    helper.enterClickAndLeave(new MouseEventData(this, partialPlanMenu));
    Thread.sleep( 1000);
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
                timelineViewItem = partialPlanSubMenu.getItem(k);
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

    String projectUrl = planWorks.getCurrentProjectUrl();
    String sequenceUrl = projectUrl + System.getProperty( "file.separator") +
      sequenceName;
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
//     while ( true) {
//       try {
//         Thread.currentThread().sleep(50);
//       } catch (InterruptedException excp) {
//       }
//     }
  }

  /**
   * Since the modal was fired by another thread. The
   * test thread is left free to drive the modal dialog.
   *
   * @throws Exception may be thrown.
   */
  public JMenu testUrlEnter() throws Exception {
    // System.err.println( "testUrlEnter entered");
    // prevents "Test runs: 2, ...
    if (firstEntry == false) {
      firstEntry = true;
    } else {
      return null;
    }
    List dialogs = TestHelper.getShowingDialogs("Create Project");
    assertEquals("Dialog not found:", 2, dialogs.size());

    Container planWorksDialog = (Container) dialogs.get(0);
    Container jfcunitDialog = (Container) dialogs.get(1);
    // System.err.println( "zeroPane " + zeroPane.getClass().getName());

    // Enter button - use dialog created by TimelineTestView
    JTextField field = null;
    field = (JTextField) TestHelper.findComponent(JTextField.class, jfcunitDialog, 0);
    assertNotNull("Could not find \"Enter\" field", field);
    // helper.sendString(new StringEventData(this, field, "Harry Potter"));
    helper.sendKeyAction(new KeyEventData(this, field, KeyEvent.VK_ENTER));
    // Cancel button -- cancel 2nd dialog created by PlanWorks
    JButton button = null;
    button = (JButton) TestHelper.findComponent(JButton.class, planWorksDialog, 1);
    assertNotNull("Could not find \"Cancel\" button", field);

    helper.enterClickAndLeave(new MouseEventData(this, button));

    // assertTrue("Unsuccessful exit:", normalExit);
    // checkException();

    // System.err.println( "\n\nGot to here 0\n\n");
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
  }



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
    PlanWorks.userCollectionName = System.getProperty( "file.separator") +
      System.getProperty( "user");

    try {
      PwProject.initProjects();
    } catch (ResourceNotFoundException rnfExcep) {
      System.err.println( rnfExcep);
      System.exit( -1);
    }

    planWorks = new PlanWorks( PlanWorks.buildConstantMenus());
    PlanWorks.planWorks = planWorks;

    TestRunner.run( TimelineViewTest.class);
    // testMain();
  }
}
 
    
