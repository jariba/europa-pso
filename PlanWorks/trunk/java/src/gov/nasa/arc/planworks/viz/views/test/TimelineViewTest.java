//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimelineViewTest.java,v 1.9 2003-06-02 19:18:22 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.views.test;

import java.awt.Color;
import java.awt.Container;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import junit.extensions.jfcunit.*;
import junit.extensions.jfcunit.eventdata.EventDataConstants;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.*;
import junit.textui.TestRunner;
import gov.nasa.arc.planworks.db.test.PwProjectTest;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.nodes.SlotNode;

public class TimelineViewTest extends JFCTestCase {

  private JFCTestHelper helper;

  public TimelineViewTest(String test) {
    super(test);
  }
  
  public void setUp() {
    helper = new JFCTestHelper();
  }
  
  public void tearDown() throws Exception {
    helper.cleanUp(this);
    super.tearDown();
    System.exit(0);
  }
  
  public void testMain() throws Exception {
    awtSleep();
    Set windows = helper.getWindows();
    assertEquals("Timeline window failed to open", 1, windows.size());
    JFrame frame = (JFrame)(windows.toArray())[0];
    assertNotNull("Failed to get frame from set", frame);
    JMenuBar menuBar = frame.getJMenuBar();
    assertNotNull("Failed to get menu bar from frame", menuBar);
    JMenu projectMenu = null;
    JMenu renderMenu = null;
    MenuElement [] elements = menuBar.getSubElements();
    for(int i = 0; i < elements.length; i++) {
      if(((JMenu)elements[i]).getText().equals("Render")) {
        renderMenu = (JMenu) elements[i];
      }
      else if(((JMenu)elements[i]).getText().equals("Project")) {
        projectMenu = (JMenu) elements[i];
      }
    }
    assertNotNull("Failed to get menu", projectMenu);
    assertTrue("Failed to get \"Project\" menu.", projectMenu.getText().equals("Project"));
    JMenuItem createItem = null;
    for(int i = 0; i < projectMenu.getItemCount(); i++) {
      if(projectMenu.getItem(i).getText().equals("Create")) {
        createItem = projectMenu.getItem(i);
      }
    }
    assertNotNull("Failed to get \"Create\" item.", createItem);
    assertTrue("Failed to get \"Create\" item.", createItem.getText().equals("Create"));
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, createItem));
    assertNotNull("Failed to get menu.", renderMenu);
    assertTrue("Failed to get \"Render\" menu.", renderMenu.getText().equals("Render"));
    JMenuItem renderItem = null;
    for(int i = 0; i < renderMenu.getItemCount(); i++) {
      if(renderMenu.getItem(i).getText().equals("Timeline View")) {
        renderItem = renderMenu.getItem(i);
      }
    }
    assertNotNull("Failed to get any menu item.", renderItem);
    assertTrue("Failed to get \"Timeline View\" item.", 
               renderItem.getText().equals("Timeline View"));
    helper.enterClickAndLeave(new MouseEventData(this, renderMenu));
    helper.enterClickAndLeave(new MouseEventData(this, renderItem));
    //helper.mousePressed(renderItem, EventDataConstants.DEFAULT_MOUSE_MODIFIERS, 1, false);
    Container contentPane = frame.getContentPane();
    //System.out.println(contentPane.getComponent(0));
    TimelineView timelineView = (TimelineView) contentPane.getComponent(0);
    assertNotNull("Failed to get timeline view.", timelineView);
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
  }
  
  public static void main( String[] args) {
    PwProjectTest projectTest = new PwProjectTest(System.getProperty("os.name"),
                                                  System.getProperty("xml.files.dir"));
    projectTest.setSize(PwProjectTest.FRAME_WIDTH,
                        PwProjectTest.FRAME_HEIGHT);
    projectTest.setLocation(PwProjectTest.FRAME_X_LOCATION,
                            PwProjectTest.FRAME_Y_LOCATION);
    projectTest.setBackground(Color.gray);
    projectTest.setVisible(true);
    TestRunner.run(TimelineViewTest.class);
                                // testMain();
  }
}
 
