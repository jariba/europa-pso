package gov.nasa.arc.planworks.viz.views.test;

import java.awt.Color;
import java.util.Set;
import junit.extensions.jfcunit.*;
import junit.framework.*;
import junit.textui.TestRunner;
import gov.nasa.arc.planworks.proj.test.PwProjectTest;

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
  }
  
  public void testMain() {
    Set windows;
    
    awtSleep();
    windows = helper.getWindows();
    assertEquals("Timeline window failed to open", 1, windows.size());
  }
  
  public static void main( String[] args) {
    boolean jvm_ge_14 = false;
    if(System.getProperty("java.version").compareTo("1.4.1") >= 0)
      jvm_ge_14 = true;
    PwProjectTest projectTest =
      new PwProjectTest( System.getProperty("os.name"),
                         jvm_ge_14,
                         System.getProperty("xml.files.dir"));
    projectTest.setSize( PwProjectTest.FRAME_WIDTH,
                         PwProjectTest.FRAME_HEIGHT);
    projectTest.setLocation( PwProjectTest.FRAME_X_LOCATION,
                             PwProjectTest.FRAME_Y_LOCATION);
    projectTest.setBackground( Color.gray);
    projectTest.setVisible( true);
    TestRunner.run(TimelineViewTest.class);
                                // testMain();
  }
}
 
