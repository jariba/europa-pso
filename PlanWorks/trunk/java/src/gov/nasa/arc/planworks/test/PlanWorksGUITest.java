//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest.java,v 1.3 2004-04-06 21:27:50 taylor Exp $
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
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.framework.TestSuite; 

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.impl.PwProjectImpl;
import gov.nasa.arc.planworks.db.util.FileUtils;
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

    int numSequences = 4, numSteps = 2;
    sequenceUrls = PWTestHelper.buildTestData( numSequences, numSteps, planWorks);

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
  } // end planViz01

  public void planViz02() throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    planWorks.getSequenceDirChooser().setCurrentDirectory( new File( sequenceDirectory));
    int numSequences = 4;
    File [] sequenceFileArray = new File [numSequences];
    for (int i = 0; i < numSequences; i++) {
      sequenceFileArray[i] =
        new File( sequenceDirectory + System.getProperty("file.separator") +
                  sequenceUrls.get( i));
    }
    // modify sequence #1
    (new File( sequenceFileArray[0] + System.getProperty("file.separator") +
               DbConstants.SEQ_PP_STATS)).delete();
    (new File( sequenceFileArray[0] + System.getProperty("file.separator") +
               DbConstants.SEQ_FILE)).delete();
    (new File( sequenceFileArray[0] + System.getProperty("file.separator") +
               DbConstants.SEQ_TRANSACTIONS)).delete();
    String [] fileNames = sequenceFileArray[0].list();
    boolean success = true;
    for (int i = 0, n = fileNames.length; i < n; i++) {
      File fileNameFile = new File( fileNames[i]);
      if (fileNameFile.isDirectory()) {
        success = FileUtils.deleteDir( fileNameFile);
        if (! success) {
          System.err.println( "PlanWorksGUITest.planViz02: deleting '" + fileNames[i] +
                              "' failed"); System.exit( -1);
        }
      }
    }
    // modify sequence #2
    (new File( sequenceFileArray[1] + System.getProperty("file.separator") +
               DbConstants.SEQ_FILE)).delete();
    // modify sequence #3
    String stepName = "step1";
    (new File( sequenceFileArray[2] + System.getProperty("file.separator") + stepName +
               System.getProperty("file.separator") + stepName + "." +
               DbConstants.PP_PARTIAL_PLAN_EXT)).delete();
    // modify sequence #4
    String stepDir = sequenceFileArray[2] + System.getProperty("file.separator") + stepName;
    success = FileUtils.deleteDir( new File( stepDir));
    if (! success) {
      System.err.println( "PlanWorksGUITest.planViz02: deleting '" + stepDir +
                          "' failed"); System.exit( -1);
    }
    createProject( PROJECT1, sequenceDirectory, sequenceFileArray);

    try{Thread.sleep(10000);}catch(Exception e){}





    deleteProject();

    System.err.println( "\nPLANVIZ_02 COMPLETE\n");

    // try{Thread.sleep(5000);}catch(Exception e){}
  } // end planViz02

  private void createProject( String projectName, String sequenceDirectory,
                              File [] sequenceFileArray) throws Exception {
    JMenuItem createItem = findMenuItem(PlanWorks.PROJECT_MENU, PlanWorks.CREATE_MENU_ITEM);
    // System.err.println("Found create menu item: " + createItem);
    assertNotNull( "'Project->Create' not found:", createItem);
    helper.enterClickAndLeave( new MouseEventData( this, createItem));
    awtSleep();

    planWorks.getSequenceDirChooser().setCurrentDirectory( new File( sequenceDirectory));
    planWorks.getSequenceDirChooser().setSelectedFiles( sequenceFileArray);

    // JTextField field = (JTextField) findComponentByClass( JTextField.class);
    JTextField field = (JTextField) findComponentByName( JTextField.class, "OK");
    System.err.println( "createProject field " + field);
    assertNotNull( "Could not find \"name (string)\" field", field);
    helper.sendString( new StringEventData( this, field, projectName));
    helper.sendKeyAction( new KeyEventData( this, field, KeyEvent.VK_ENTER));
    
    JFileChooser fileChooser = null;
    fileChooser = helper.getShowingJFileChooser( planWorks);
    assertNotNull( "Select Sequence Directory Dialog not found:", fileChooser);
    Container projectSeqDialog = (Container) fileChooser;

    JButton okButton = (JButton) findComponentByName( JButton.class, "OK");
    assertNotNull("Could not find projectSeqDialog \"OK\" button", okButton);
    helper.enterClickAndLeave( new MouseEventData( this, okButton));
    awtSleep();

//     AbstractButton cancelButton = findCancelButton();
//     System.err.println("Found cancel button: " + cancelButton);
//     helper.enterClickAndLeave(new MouseEventData(this, cancelButton));
  } // end createProject

  private void deleteProject() throws Exception {
    JMenuItem deleteItem = findMenuItem(PlanWorks.PROJECT_MENU, PlanWorks.DELETE_MENU_ITEM);
    // System.err.println("Found delete menu item: " + deleteItem);
    assertNotNull( "'Project->Delete' not found:", deleteItem);
    helper.enterClickAndLeave( new MouseEventData( this, deleteItem));
    awtSleep();

    List dialogs = null;
    dialogs = helper.getShowingDialogs( "Delete Project");
    assertEquals( "Delete Project Dialog not found:", 1, dialogs.size());
    Container deleteDialog = (Container) dialogs.get( 0);

    JButton okButton = (JButton) findComponentByName( JButton.class, "OK");
    // System.err.println( "deleteDialog " + okButton.getText());
    assertNotNull( "Could not find deleteDialog \"OK\" button", okButton);
    helper.enterClickAndLeave( new MouseEventData( this, okButton));
    awtSleep();
  } // end deleteProject

  private JMenu getPlanSequenceMenu() throws Exception {
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
    suite.addTest(new PlanWorksGUITest("planViz02"));
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

  private Component findComponentByClass( Class componentClass) {
    ComponentFinder finder = new ComponentFinder( componentClass);
    return (Component) finder.find();
  }

  private Component findComponentByName( Class componentClass, String compLabel) {
    NamedComponentFinder finder =
      new NamedComponentFinder( componentClass, compLabel);
    return (Component) finder.find();
  }


}
