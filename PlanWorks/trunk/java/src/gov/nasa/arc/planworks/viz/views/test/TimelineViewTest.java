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
import gov.nasa.arc.planworks.proj.test.PwProjectTest;
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
    JMenu renderMenu = null;
    MenuElement [] elements = menuBar.getSubElements();
    for(int i = 0; i < elements.length; i++) {
      if(((JMenu)elements[i]).getText().equals("Render")) {
        renderMenu = (JMenu) elements[i];
      }
    }
     assertNotNull("Failed to get menu.", renderMenu);
    assertTrue("Failed to get \"Render\" menu.", renderMenu.getText().equals("Render"));
    JMenuItem renderItem = renderMenu.getItem(0);
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
        if(timelineName.indexOf("Monkey1 : LOCATION_SV") == -1 &&
           timelineName.indexOf("Monkey1 : ALTITUDE_SV") == -1 &&
           timelineName.indexOf("Monkey1 : BANANA_SV") == -1){
          assertTrue("Invalid timeline name", false);
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
 
