//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest.java,v 1.4 2004-04-09 23:11:24 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.ListIterator;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.JMenuMouseEventData;
import junit.extensions.jfcunit.eventdata.KeyEventData;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.eventdata.StringEventData;
import junit.framework.TestSuite; 

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.impl.PwProjectImpl;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.StepElement;


public class PlanWorksGUITest extends JFCTestCase {
  
  private PlanWorks planWorks;
  private JFCTestHelper helper;

  private static final String PROJECT1 = "testProject1";
  private static final String PROJECT2 = "testProject2";

  private List sequenceUrls; // element String
  private Point popUpLocation;

  public PlanWorksGUITest(String test) {
    super(test);
  }
  
  public void setUp() throws Exception {
    super.setUp();
    helper = new JFCTestHelper();

    planWorks = new PlanWorks( PlanWorks.buildConstantMenus(),
                               System.getProperty( "name.application"),
                               System.getProperty( "boolean.isMaxTestScreen"),
                               System.getProperty( "os.type"),
                               System.getProperty( "planworks.root"));
    PlanWorks.setPlanWorks( planWorks);
    popUpLocation = new Point( (int) (PlanWorks.getPlanWorks().getWidth() / 2),
                               (int) (PlanWorks.getPlanWorks().getHeight() / 2));

    int numSequences = 4, numSteps = 2;
    sequenceUrls = PWTestHelper.buildTestData( numSequences, numSteps, planWorks);

    // System.exit( 0);

    flushAWT(); awtSleep();
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
    PWTestHelper.createProject( PROJECT1, sequenceDirectory, sequenceFileArray, helper,
                                this, planWorks);
    // post condition 1
    assertTrue("PlanWorks title does not contain " + PROJECT1,
               PlanWorks.getPlanWorks().getTitle().endsWith( PROJECT1));

    // post condition 2
    PWTestHelper.getPlanSequenceMenu();

    // try{Thread.sleep(5000);}catch(Exception e){}

    // post condition 3
    PWTestHelper.deleteProject( helper, this);

    System.err.println( "\nPLANVIZ_01 COMPLETED\n");

    planViz02();
  } // end planViz01

  public void planViz02() throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    planWorks.getSequenceDirChooser().setCurrentDirectory( new File( sequenceDirectory));
    int numSequences = 4;
    File [] sequenceFileArray = new File [numSequences];
    for (int i = 0; i < numSequences; i++) {
      sequenceFileArray[i] = new File( (String) sequenceUrls.get( i));
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
    String stepName = "step1"; int stepNumber = 1;
    (new File( sequenceFileArray[2] + System.getProperty("file.separator") + stepName +
               System.getProperty("file.separator") + stepName + "." +
               DbConstants.PP_PARTIAL_PLAN_EXT)).delete();
    // modify sequence #4
    String stepDir = sequenceFileArray[3] + System.getProperty("file.separator") + stepName;
    success = FileUtils.deleteDir( new File( stepDir));
    if (! success) {
      System.err.println( "PlanWorksGUITest.planViz02: deleting '" + stepDir +
                          "' failed"); System.exit( -1);
    }
    File [] seqFileArray = new File [1];
    // try sequence #1
    seqFileArray[0] = sequenceFileArray[0];
    PWTestHelper.createProject( PROJECT1, sequenceDirectory, seqFileArray, helper,
                                this, planWorks);
    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK",
                               "0 sequence files in directory -- 3 are required",
                               helper, this);
    AbstractButton cancelButton = PWTestHelper.findButton( "Cancel");
    assertNotNull( "'Project->Create' cancel button not found:", cancelButton);
    helper.enterClickAndLeave(new MouseEventData(this, cancelButton));
    PWTestHelper.deleteProject( helper, this);
    // try{Thread.sleep(5000);}catch(Exception e){}

    // try sequences #2, #3, #4
    File [] seq3FileArray = new File [3];
    seq3FileArray[0] = sequenceFileArray[1];
    seq3FileArray[1] = sequenceFileArray[2];
    seq3FileArray[2] = sequenceFileArray[3];
    PWTestHelper.createProject( PROJECT1, sequenceDirectory, seq3FileArray, helper,
                                this, planWorks);
    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK",
                               "2 sequence files in directory -- 3 are required",
                               helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK",
                               "Has 7 files -- 8 are required", helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( "sequence", helper, this);
    StepElement stepElement =
      (StepElement) ((List) seqStepsView.getStepElementList().get( stepNumber)).get( 0);
    // 2nd arg to enterClickAndLeave must be of class Component
//     helper.enterClickAndLeave( new MouseEventData( this, stepElement, 1,
//                                                    MouseEvent.BUTTON3_MASK));
    stepElement.doMouseClick( MouseEvent.BUTTON3_MASK, stepElement.getLocation(),
                              new Point( 0, 0), seqStepsView.getJGoView());
    flushAWT(); awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}

    PartialPlanViewMenu popupMenu =
      (PartialPlanViewMenu) PWTestHelper.findComponentByClass( PartialPlanViewMenu.class);
    assertNotNull( "Failed to get \"" + popupMenu + "\" popupMenu.", popupMenu); 

    String viewMenuItemName = "Open " + PlanWorks.TIMELINE_VIEW;
    PartialPlanViewMenuItem viewMenuItem =
      PWTestHelper.getPopupViewMenuItem( viewMenuItemName, popupMenu);
    // try{Thread.sleep(2000);}catch(Exception e){}

    System.err.println( "viewMenuItem " + viewMenuItem.getText());
    assertNotNull( viewMenuItemName + "' not found:", viewMenuItem); 
    helper.enterClickAndLeave( new MouseEventData( this, viewMenuItem));
    flushAWT(); awtSleep();
    PWTestHelper.handleDialog( "Resource Not Found Exception", "OK",
                               "Failed to get file listing for " + stepName, helper, this);
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.deleteProject( helper, this);

    System.err.println( "\nPLANVIZ_02 COMPLETED\n");
  } // end planViz02







  public static TestSuite suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new PlanWorksGUITest("planViz01"));
    // this does not work -- so just chain the tests onto planViz01
    // suite.addTest(new PlanWorksGUITest("planViz02"));
    return suite;
  }


}
