//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest.java,v 1.2 2004-04-06 01:31:43 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.JMenuMouseEventData;
import junit.extensions.jfcunit.eventdata.KeyEventData;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.eventdata.StringEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.ComponentFinder;
import junit.extensions.jfcunit.finder.LabeledComponentFinder;
import junit.framework.TestSuite; 

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.impl.PwProjectImpl;
import gov.nasa.arc.planworks.db.util.MySQLDB;

public class PlanWorksGUITest extends JFCTestCase {
  
  private PlanWorks planWorks;
  private JFCTestHelper helper;

  private static final String PROJECT1 = "testProject1";
  private static final String PROJECT2 = "testProject2";

  private List sequenceUrls;

  public PlanWorksGUITest(String test) {
    super(test);
  }
  
  public void setUp() throws Exception {
    super.setUp();
    helper = new JFCTestHelper();

    planWorks = new PlanWorks( PlanWorks.buildConstantMenus(),
                               System.getProperty( "name.application"));
    PlanWorks.setPlanWorks( planWorks);

    // planWorks.makeMaxScreen();

    sequenceUrls = PWTestHelper.buildTestData( planWorks);

    // System.exit( 0);

    flushAWT();
    awtSleep();
    
  }
  public void tearDown() throws Exception {
    super.tearDown();
    helper.cleanUp(this);
    //System.exit(0);
  }

  public void planViz01() throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    planWorks.getSequenceDirChooser().setCurrentDirectory( new File( sequenceDirectory));
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory + System.getProperty("file.separator") +
                                     sequenceUrls.get( 0));
    createProject( PROJECT1, sequenceDirectory, sequenceFileArray);
    // post condition 1
    String planWorksTitle = null;
    planWorksTitle = PlanWorks.getPlanWorks().getTitle();
    if (planWorksTitle.endsWith( PROJECT1)) {
      planWorksTitle = PlanWorks.getPlanWorks().getPlanWorksTitle();
    }
    assertNotNull( "PlanWorks title does not contain " + PROJECT1, planWorksTitle);
    // post condition 2
    getPlanSequenceMenu();

    // try{Thread.sleep(5000);}catch(Exception e){}

    // post condition 3
    deleteProject();

    System.err.println( "\nPLANVIZ_01 COMPLETE\n");

    // try{Thread.sleep(5000);}catch(Exception e){}
  }

  private void createProject( String projectName, String sequenceDirectory,
                              File [] sequenceFileArray) {
    JMenuItem createItem = findMenuItem(PlanWorks.PROJECT_MENU, PlanWorks.CREATE_MENU_ITEM);
    // System.err.println("Found create menu item: " + createItem);
    assertNotNull( "'Project->Create' not found:", createItem);
    helper.enterClickAndLeave( new MouseEventData( this, createItem));
    awtSleep();

    planWorks.getSequenceDirChooser().setCurrentDirectory( new File( sequenceDirectory));
    planWorks.getSequenceDirChooser().setSelectedFiles( sequenceFileArray);

    JTextField field = (JTextField) findComponentByClass( JTextField.class, "Enter");
    assertNotNull( "Could not find \"Enter\" field", field);
    helper.sendString( new StringEventData( this, field, projectName));
    helper.sendKeyAction( new KeyEventData( this, field, KeyEvent.VK_ENTER));
    
    JFileChooser fileChooser = null;
    fileChooser = helper.getShowingJFileChooser( planWorks);
    assertNotNull( "Select Sequence Directory Dialog not found:", fileChooser);
    Container projectSeqDialog = (Container) fileChooser;

    JButton okButton = null;
    okButton = (JButton) helper.findComponent( JButton.class, projectSeqDialog, 4);
    // System.err.println( "projectSeqDialog " + okButton.getText());
    assertNotNull("Could not find projectSeqDialog \"OK\" button", okButton);
    helper.enterClickAndLeave( new MouseEventData( this, okButton));
    awtSleep();

//     AbstractButton cancelButton = findCancelButton();
//     System.err.println("Found cancel button: " + cancelButton);
//     helper.enterClickAndLeave(new MouseEventData(this, cancelButton));
  } // end createProject

  private void deleteProject() {
    JMenuItem deleteItem = findMenuItem(PlanWorks.PROJECT_MENU, PlanWorks.DELETE_MENU_ITEM);
    // System.err.println("Found delete menu item: " + deleteItem);
    assertNotNull( "'Project->Delete' not found:", deleteItem);
    helper.enterClickAndLeave( new MouseEventData( this, deleteItem));
    awtSleep();

    List dialogs = null;
    dialogs = helper.getShowingDialogs( "Delete Project");
    assertEquals( "Delete Project Dialog not found:", 1, dialogs.size());
    Container deleteDialog = (Container) dialogs.get( 0);

    JButton okButton = null;
    okButton = (JButton) helper.findComponent( JButton.class, deleteDialog, 1);
    // System.err.println( "deleteDialog " + okButton.getText());
    assertNotNull( "Could not find deleteDialog \"OK\" button", okButton);
    helper.enterClickAndLeave( new MouseEventData( this, okButton));
    awtSleep();
  } // end deleteProject

  private JMenu getPlanSequenceMenu() {
    JMenu planSequenceMenu = null;
    planSequenceMenu = findMenu( PlanWorks.PLANSEQ_MENU);
    assertNotNull("Failed to get \"Planning Sequence\" menu.", planSequenceMenu);
    assertTrue("Failed to get \"Planning Sequence\" menu.",
               planSequenceMenu.getText().equals("Planning Sequence"));
    // System.err.println( "planSequenceMenu " + planSequenceMenu);
    return planSequenceMenu;
  } // end getPlanSequenceMenu




  public static TestSuite suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new PlanWorksGUITest("planViz01"));
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

  private Component findComponentByClass( Class componentClass, String compLabel) {
    // how to use compLabel, if there are more than one components in finder??
    ComponentFinder finder = new ComponentFinder( componentClass);
    return (Component) finder.find();
  }


}
