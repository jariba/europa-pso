//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksBigTest.java,v 1.1 2003-08-28 20:46:55 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MenuElement;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.TestSuite; 
import junit.textui.TestRunner;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.views.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.views.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.views.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.nodes.SlotNode;
import gov.nasa.arc.planworks.viz.nodes.TemporalNode;
import gov.nasa.arc.planworks.viz.nodes.TokenLink;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
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

public class PlanWorksBigTest extends JFCTestCase {
  private JFrame frame;
  private JMenu projectMenu;
  private JMenuBar menuBar;
  private PlanWorks planWorks;
  

  /**
   * <code>PlanWorksTest</code> - constructor 
   *
   * @param test - <code>String</code> - 
   * @param testType - <code>String</code> - 
   */
  public PlanWorksBigTest(String test) {
    super(test);
  }
  
  /**
   * <code>setUp</code> - start PlanWorks
   *
   * @exception Exception if an error occurs
   */
  public void setUp() throws Exception {
    helper = new JFCTestHelper();

    planWorks = new PlanWorks( PlanWorks.buildConstantMenus());
    PlanWorks.setPlanWorks( planWorks);
    flushAWT();
    awtSleep();
    Set windows = helper.getWindows();
    frame = (JFrame)(windows.toArray()[0]);
    menuBar = frame.getJMenuBar();
    projectMenu = null;
    MenuElement [] elements = menuBar.getSubElements();
    for(int i = 0; i < elements.length; i++) {
      if(((JMenu)elements[i]).getText().equals("Project")) {
        projectMenu = (JMenu) elements[i];
      }
    }
    JMenuItem createItem = null;
    for(int i = 0; i < projectMenu.getItemCount(); i++) {
      if((projectMenu.getItem(i) != null && 
          projectMenu.getItem(i).getText().equals("Create ..."))) {
        createItem = (JMenuItem) projectMenu.getItem(i);
        break;
      }
    }
    helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
    helper.enterClickAndLeave(new MouseEventData(this, createItem));
    List dialogs = null;
    do {
      try {
        Thread.sleep(50);
      }
      catch(InterruptedException ie) {
      }
      dialogs = TestHelper.getShowingDialogs("Create Project");
    }
    while(dialogs.size() == 0);
    Container projectNameDialog = (Container) dialogs.get(0);
    JButton button = null;
    button = (JButton) TestHelper.findComponent(JButton.class, projectNameDialog, 0);
    helper.enterClickAndLeave(new MouseEventData(this, button));
  }

  /**
   * <code>tearDown</code>
   *
   * @exception Exception if an error occurs
   */
  public void tearDown() throws Exception {
    helper.cleanUp(this);
    super.tearDown();
    System.exit(0);
  }

  public void testPlanWorks() throws Exception {
    String europaHome = System.getProperty("europa.home");
    assertNotNull("EUROPA_HOME not set", europaHome);
    File europaBaseDir = new File(europaHome);
    assertTrue("EUROPA_HOME not a directory", europaBaseDir.isDirectory());
    assertTrue("No read access to EUROPA_HOME", europaBaseDir.canRead());
    List sequenceDirs = findSequenceDirectories(europaBaseDir);
    ListIterator seqIterator = sequenceDirs.listIterator();
    boolean first = true;
    JMenuItem addSeqItem = null;
    for(int i = 0; i < projectMenu.getItemCount(); i++) {
      if(projectMenu.getItem(i) != null && 
         ((JMenuItem)projectMenu.getItem(i)).getText().equals("Add Sequence ...")) {
        addSeqItem = (JMenuItem) projectMenu.getItem(i);
      }
    }
    assertNotNull("Failed to get 'Add Sequence' menu item.", addSeqItem);
    while(seqIterator.hasNext()) {
      if(!first) {
        while(!projectMenu.isEnabled()) {
          try {
            Thread.sleep(50);
          }
          catch(Exception e){}
        }
        helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
        while(!addSeqItem.isEnabled()) {
          try {
            Thread.sleep(50);
          }
          catch(Exception e){}
        }
        helper.enterClickAndLeave(new MouseEventData(this, addSeqItem));
      }
      JFileChooser fileChooser = null;
      do {
        try {
          Thread.sleep(50);
        }
        catch(Exception e){}
        fileChooser = helper.getShowingJFileChooser(planWorks);
      }
      while(fileChooser == null);
      JButton okButton = null;
      okButton = (JButton) TestHelper.findComponent(JButton.class, fileChooser, 4);
      fileChooser.setCurrentDirectory(new File((String) seqIterator.next()));
      helper.enterClickAndLeave(new MouseEventData(this, okButton));
      first = false;
    }
  }

  private List findSequenceDirectories(File dir) {
    List retval = new ArrayList();
    File [] files = dir.listFiles();
    for(int i = 0; i < files.length; i++) {
      if(files[i].isDirectory()) {
        try {
          if(files[i].getName().indexOf("step") != -1) {
            retval.add(dir.getCanonicalPath());
            return retval;
          }
          else {
            retval.addAll(findSequenceDirectories(files[i]));
          }
        }
        catch(IOException ioe) {
          ioe.printStackTrace();
          System.err.println(ioe);
          System.exit(-1);
        }
      }
    }
    return retval;
  }

  public static TestSuite suite() {
    TestSuite testSuite = new TestSuite();
    testSuite.addTest(new PlanWorksBigTest("testPlanWorks"));
    return testSuite;
  }

  public static void main(String [] args) {
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
  }
}
