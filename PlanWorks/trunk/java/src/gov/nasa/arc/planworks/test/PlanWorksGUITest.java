package gov.nasa.arc.planworks.test;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.JMenuMouseEventData;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.framework.TestSuite; 

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.impl.PwProjectImpl;
import gov.nasa.arc.planworks.db.util.MySQLDB;

public class PlanWorksGUITest extends JFCTestCase {
  
  private PlanWorks planWorks;
  private JFCTestHelper helper;

  private static final String PROJECT1 = "testProject1";
  private static final String PROJECT2 = "testProject2";

  public PlanWorksGUITest(String test) {
    super(test);
  }
  
  public void setUp() throws Exception {
    super.setUp();
    helper = new JFCTestHelper();
    buildTestData();
    planWorks = new PlanWorks(PlanWorks.buildConstantMenus());
    PlanWorks.setPlanWorks(planWorks);
    flushAWT();
    awtSleep();
    
  }
  public void tearDown() throws Exception {
    super.tearDown();
    helper.cleanUp(this);
    //System.exit(0);
  }

  public void testPlanWorks() throws Exception {
    JMenuItem createItem = findMenuItem(PlanWorks.PROJECT_MENU, PlanWorks.CREATE_MENU_ITEM);
    System.err.println("Found create menu item: " + createItem);
    helper.enterClickAndLeave(new MouseEventData(this, createItem));
    AbstractButton cancelButton = findCancelButton();
    System.err.println("Found cancel button: " + cancelButton);
    helper.enterClickAndLeave(new MouseEventData(this, cancelButton));
    JMenuItem deleteItem = findMenuItem(PlanWorks.PROJECT_MENU, PlanWorks.DELETE_MENU_ITEM);
    System.err.println("Found delete menu item: " + deleteItem);
    try{Thread.sleep(10000);}catch(Exception e){}
  }

  public static TestSuite suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new PlanWorksGUITest("testPlanWorks"));
    return suite;
  }

  private JMenu findMenu(String name) {
    AbstractButtonFinder finder = new AbstractButtonFinder(name);
    return (JMenu) finder.find();
  }

  private JMenuItem findMenuItem(String menuName, String itemName) {
    JMenu parent = findMenu(menuName);
    assertTrue(parent != null);
    if(parent == null) {
      return null;
    }
    helper.enterClickAndLeave(new MouseEventData(this, parent));
    AbstractButtonFinder finder = new AbstractButtonFinder(itemName);
    return (JMenuItem) finder.find();
  }

  private AbstractButton findCancelButton() {
    AbstractButtonFinder finder = new AbstractButtonFinder("Cancel");
    return (AbstractButton) finder.find();
  }

  private void buildTestData() {
//     MySQLDB.startDatabase();
//     MySQLDB.registerDatabase();
    
//     PwProjectImpl project1 = new PwProjectImpl(PROJECT1);
//     MySQLDB.updateDatabase("INSERT INTO Project (ProjectName) VALUES ('".concat(project1.getName()).concat("')"));
//     Integer proj1Id = MySQLDB.latestProjectId();

//     MySQLDB.updateDatabase("INSERT INTO Sequence (SequenceURL
  }
}
