//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksBigTest.java,v 1.5 2003-09-18 20:48:42 taylor Exp $
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
import javax.swing.JDialog;
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
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.views.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.views.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.views.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.views.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.views.temporalExtent.TemporalNode;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineNode;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.views.timeline.SlotNode;
import gov.nasa.arc.planworks.viz.views.tokenNetwork.TokenLink;
import gov.nasa.arc.planworks.viz.views.tokenNetwork.TokenNetworkView;
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
      if(projectMenu.getItem(i) != null && projectMenu.getItem(i).getText().equals("Add Sequence ...")) {
        addSeqItem = (JMenuItem) projectMenu.getItem(i);
      }
    }
    assertNotNull("Failed to get 'Add Sequence' menu item.", addSeqItem);
    JMenuItem delSeqItem = null;
    for(int i = 0; i < projectMenu.getItemCount(); i++) {
      if(projectMenu.getItem(i) != null && projectMenu.getItem(i).getText().equals("Delete Sequence ...")) {
        delSeqItem = projectMenu.getItem(i);
      }
    }
    assertNotNull("Failed to get 'Delete Sequence' menu item.", delSeqItem);
    while(seqIterator.hasNext()) {
      String seqUrl = (String) seqIterator.next();
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
      //fileChooser.setCurrentDirectory(new File(seqUrl));
      fileChooser.setCurrentDirectory(new File(seqUrl.substring(0, seqUrl.lastIndexOf(System.getProperty("file.separator")))));
      //fileChooser.setSelectedFile(new File(seqUrl));
      File [] temp = new File[1];
      temp[0] = new File(seqUrl);
      fileChooser.setSelectedFiles(temp);
      System.err.println(fileChooser.getSelectedFile().getName());
      helper.enterClickAndLeave(new MouseEventData(this, okButton));
      first = false;
      viewTests(seqUrl);
      do {
        Thread.sleep(50);
      }
      while(!projectMenu.isEnabled());
      helper.enterClickAndLeave(new MouseEventData(this, projectMenu));
      while(!delSeqItem.isEnabled()) {
        try {
          Thread.sleep(50);
        }
        catch(Exception e){}
      }
      helper.enterClickAndLeave(new MouseEventData(this, delSeqItem));
      do {
        Thread.sleep(50);
      }
      while(helper.getShowingDialogs().size() == 0);
      JDialog delDialog = (JDialog) helper.getShowingDialogs().get(0);
      okButton = (JButton) TestHelper.findComponent(JButton.class, delDialog, 1);
      helper.enterClickAndLeave(new MouseEventData(this, okButton));
    }
  }

  private void viewTests(String url) throws Exception {
    PwProject proj = null;
    do {
      Thread.sleep(50);
    }
    while((proj = planWorks.getCurrentProject()) == null || proj.listPlanningSequences().size() != 1);
    assertNotNull("Odd... project is null", proj);
    boolean seqIsDone = false;
    PwPlanningSequence seq = null;
    while(!seqIsDone && seq == null) {
      try {
        seq = proj.getPlanningSequence(url);
      }
      catch(Exception e) {
        Thread.sleep(50);
      }
      seqIsDone = true;
    }
    assertNotNull("Failed to get planning sequence " + url, seq);
    ListIterator ppNameIterator = seq.listPartialPlanNames().listIterator();
    while(ppNameIterator.hasNext()) {
      PwPartialPlan plan = seq.getPartialPlan((String)ppNameIterator.next());
      ViewManager viewManager = null;
      do {
        Thread.sleep(50);
      }
      while((viewManager = planWorks.getViewManager()) == null);
      assertNotNull("Failed to get view manager.", viewManager);
      MDIInternalFrame timelineViewFrame = viewManager.openTimelineView(plan, plan.getUrl(), 0);
      assertNotNull("Failed to open timeline view", timelineViewFrame);
      MDIInternalFrame temporalExtentViewFrame = viewManager.openTemporalExtentView(plan, plan.getUrl(), 0);
      assertNotNull("Failed to open temporal extent view", temporalExtentViewFrame);
      MDIInternalFrame tokenNetworkViewFrame = viewManager.openTokenNetworkView(plan, plan.getUrl(), 0);
      assertNotNull("Failed to open token network view", tokenNetworkViewFrame);
      MDIInternalFrame constraintNetworkViewFrame = viewManager.openConstraintNetworkView(plan, 
                                                                                          plan.getUrl(),
                                                                                          0);
      assertNotNull("Failed to open constraint network view", constraintNetworkViewFrame);
      timelineViewTest(timelineViewFrame);
      temporalExtentViewTest(temporalExtentViewFrame);
      tokenNetworkViewTest(tokenNetworkViewFrame);
      constraintNetworkViewTest(constraintNetworkViewFrame);
      /*openTemporalNetworkView();
        temporalNetworkViewTest();*/
    }
  }

  private void timelineViewTest(MDIInternalFrame frame) throws Exception {
    Container contentPane = frame.getContentPane();
    TimelineView view = null;
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof TimelineView) {
        view = (TimelineView) contentPane.getComponent(i);
      }
    }
    assertNotNull("Failed to get TimelineView object.", view);
    do {
      Thread.sleep(50);
    }
    while(view.getTimelineNodeList() == null);
    assertTrue("Timeline view with no timelines...", view.getTimelineNodeList().size() > 0);
    ListIterator timelineNodeIterator = view.getTimelineNodeList().listIterator();
    while(timelineNodeIterator.hasNext()) {
      TimelineNode timelineNode = (TimelineNode) timelineNodeIterator.next();
      assertTrue("Timeline with no slots!", timelineNode.getSlotNodeList().size() > 0);
      boolean lastWasEmpty = false;
      ListIterator slotNodeIterator = timelineNode.getSlotNodeList().listIterator();
      while(slotNodeIterator.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeIterator.next();
        assertTrue("Two consecutive empty slots!", 
                   !lastWasEmpty || (slotNode.getSlot().getTokenList().size() != 0));
        lastWasEmpty = slotNode.getSlot().getTokenList().size() == 0;
      }
    }
  }

  private void temporalExtentViewTest(MDIInternalFrame frame) throws Exception {
    Container contentPane = frame.getContentPane();
    TemporalExtentView view = null;
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof TemporalExtentView) {
        view = (TemporalExtentView) contentPane.getComponent(i);
      }
    }
    assertNotNull("Failed to get TemporalExtentView object.", view);
    do {
      Thread.sleep(50);
    }
    while(view.getTemporalNodeList() == null);
    int min = view.scaleTime(view.getTimeScaleStart());
    int max = view.scaleTime(view.getTimeScaleEnd());
    List temporalNodes = view.getTemporalNodeList();
    ListIterator temporalNodeIterator = temporalNodes.listIterator();
    while(temporalNodeIterator.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeIterator.next();
      assertTrue("Temporal node off of timescale.", temporalNode.getStart() >= min);
      assertTrue("Temporal node off of timescale.", temporalNode.getEnd() <= max);
    }
    for(int i = 0; i < temporalNodes.size(); i++) {
      TemporalNode a = (TemporalNode) temporalNodes.get(i);
      for(int j = i+1; j < temporalNodes.size(); j++) {
        TemporalNode b = (TemporalNode) temporalNodes.get(j);
        if(b.getRow() == a.getRow()) {
          assertTrue("Overlapping temporal nodes.",
                     (a.getStart() < b.getStart() && a.getEnd() < b.getEnd()) ||
                     (a.getStart() > b.getStart() && a.getEnd() > b.getEnd()));
        }
      }
    }
  }

  private void tokenNetworkViewTest(MDIInternalFrame frame) throws Exception {
    Container contentPane = frame.getContentPane();
    TokenNetworkView view = null;
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof TokenNetworkView) {
        view = (TokenNetworkView) contentPane.getComponent(i);
      }
    }
    assertNotNull("Failed to get TokenNetworkView object.", view);
    do {
      Thread.sleep(50);
    }
    while(view.getNodeList() == null);
    assertTrue("Token network view with no tokens...", view.getNodeList().size() > 0);
    ListIterator linkIterator = view.getLinkList().listIterator();
    while(linkIterator.hasNext()) {
      TokenLink link = (TokenLink) linkIterator.next();
      assertTrue("Free token with slaves!", !link.getFromTokenNode().isFreeToken());
    }
  }

  private void constraintNetworkViewTest(MDIInternalFrame frame) throws Exception {
    Container contentPane = frame.getContentPane();
    ConstraintNetworkView view = null;
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof ConstraintNetworkView) {
        view = (ConstraintNetworkView) contentPane.getComponent(i);
      }
    }
    assertNotNull("Failed to get ConstraintNetworkView object.", view);
    do {
      Thread.sleep(50);
    }
    while(view.getTokenNodeList() == null || view.getVariableNodeList() == null || 
          view.getConstraintNodeList() == null);
    ListIterator constraintIterator = view.getConstraintNodeList().listIterator();
    while(constraintIterator.hasNext()) {
      ConstraintNode node = (ConstraintNode) constraintIterator.next();
      if(node.getConstraint().getName().equals(PwConstraint.unaryTempConst) || 
         node.getConstraint().getName().equals(PwConstraint.unaryConst)) {
        assertTrue("Unary constraint on incorrect number of variables.", 
                   node.getConstraintVariableLinkList().size() == 1);
      }
      else if(node.getConstraint().getName().equals(PwConstraint.varTempConst)) {
        assertTrue("Variable temporal constraint on incorrect number of variables.",
                   node.getConstraintVariableLinkList().size() == 3);
      }
      else if(node.getConstraint().getName().equals(PwConstraint.fixedTempConst) ||
              node.getConstraint().getName().equals(PwConstraint.eqConst)) {
        assertTrue("Equality constraint on incorrect number of variables.",
                   node.getConstraintVariableLinkList().size() == 2);
      }
    }
    ListIterator variableIterator = view.getVariableNodeList().listIterator();
    while(variableIterator.hasNext()) {
      assertTrue("Variable on multiple tokens.", 
                 ((VariableNode)variableIterator.next()).getTokenNodeList().size() == 1);
    }
    ListIterator tokenIterator = view.getTokenNodeList().listIterator();
    while(tokenIterator.hasNext()) {
      TokenNode node = (TokenNode) tokenIterator.next();
      List variables = node.getVariableNodeList();
      assertTrue("Token with incorrect number of variables.", variables.size() >= 5);
      int requiredVars = 0;
      variableIterator = variables.listIterator();
      while(variableIterator.hasNext()) {
        PwVariable variable = ((VariableNode)variableIterator.next()).getVariable();
        String type = variable.getType();
        if(type.equals("OBJECT_VAR") || type.equals("REJECT_VAR") || type.equals("DURATION_VAR") ||
           type.equals("START_VAR") || type.equals("END_VAR")) {
          requiredVars++;
        }
      }
      assertTrue("Token without one of the required variables.", requiredVars == 5);
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
