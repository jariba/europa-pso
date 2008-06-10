//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest0304.java,v 1.1 2004-10-01 20:04:31 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.io.File;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JMenuItem;

import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;

import gov.nasa.arc.planworks.PlanWorks;


public class PlanWorksGUITest0304 {

  private PlanWorksGUITest0304() {
  }

  public static void planViz0304( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
                                  PlanWorksGUITest guiTest) throws Exception {

    planViz03( sequenceUrls, planWorks, helper, guiTest);

    // 04 depends on 03
    planViz04( planWorks, helper, guiTest);

  } // end planViz0304

  public static void planViz03( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
                                PlanWorksGUITest guiTest) throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( 4));
//     System.err.println( "sequenceDirectory " + sequenceDirectory);
//     System.err.println( "sequenceFileArray[0] " + sequenceFileArray[0].getName());

    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, helper, guiTest,
                                planWorks);
    PWTestHelper.addSequencesToProject( sequenceFileArray, helper, guiTest, planWorks);
   // try{Thread.sleep(2000);}catch(Exception e){}

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 5));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, helper, guiTest,
                                planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.handleDialog( "Duplicate Name Exception", "OK",
                               "A project named '" + PWTestHelper.PROJECT1 +
                               "' already exists",
                               helper, guiTest);
    AbstractButton cancelButton = PWTestHelper.findButton( "Cancel");
    guiTest.assertNotNullVerbose( "'Project->Create' cancel button not found:",
                                  cancelButton, "not ");
    helper.enterClickAndLeave( new MouseEventData( guiTest, cancelButton));

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);

    System.err.println( "\nPLANVIZ_03 COMPLETED\n");
  } // end planViz03

  public static void planViz04( PlanWorks planWorks, JFCTestHelper helper,
                                PlanWorksGUITest guiTest) throws Exception {
    // post planViz03 deleteProject condition 1
    guiTest.assertFalseVerbose( "PlanWorks title does not contain '" + PWTestHelper.PROJECT1 +
                                "'after Project->Delete",
                                planWorks.getTitle().endsWith( PWTestHelper.PROJECT1), "not ");

    // post planViz03 deleteProject condition 2
    JMenuItem deleteItem =
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.DELETE_MENU_ITEM,
                                 helper, guiTest);
    guiTest.assertTrueVerbose( "'Project->Delete' is not disabled",
                               (deleteItem.isEnabled() == false), "not ");
    guiTest.assertNotNullVerbose( "'Project->Delete' not found:", deleteItem, "not ");
    helper.enterClickAndLeave( new MouseEventData( guiTest, deleteItem));

    System.err.println( "\nPLANVIZ_04 COMPLETED\n");
  } // end planViz04

} // end class PlanWorksGUITest0304
