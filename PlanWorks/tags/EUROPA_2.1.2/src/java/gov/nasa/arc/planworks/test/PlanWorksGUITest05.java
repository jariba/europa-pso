//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest05.java,v 1.1 2004-10-01 20:04:32 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.io.File;
import java.util.List;

import junit.extensions.jfcunit.JFCTestHelper;

import gov.nasa.arc.planworks.PlanWorks;


public class PlanWorksGUITest05 {

  private PlanWorksGUITest05() {
  }

  public static void planViz05( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
                                  PlanWorksGUITest guiTest) throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( 4));
    // System.err.println( "planViz05 sequenceUrls.get( 4) " + sequenceUrls.get( 4));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, helper, guiTest,
                                planWorks);
    PWTestHelper.addSequencesToProject( sequenceFileArray, helper, guiTest, planWorks);
    // try{Thread.sleep(4000);}catch(Exception e){}

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 5));
    // System.err.println( "planViz05 sequenceUrls.get( 5) " + sequenceUrls.get( 5));
    PWTestHelper.createProject( PWTestHelper.PROJECT2, sequenceDirectory, helper, guiTest,
                                planWorks);

    PWTestHelper.addSequencesToProject( sequenceFileArray, helper, guiTest, planWorks);
    // try{Thread.sleep(4000);}catch(Exception e){}
    guiTest.assertTrueVerbose( "PlanWorks title does not contain '" + PWTestHelper.PROJECT2 +
                               "' after 2nd Project->Create",
                               planWorks.getTitle().endsWith( PWTestHelper.PROJECT2), "not ");

    PWTestHelper.openProject( PWTestHelper.PROJECT1, helper, guiTest, planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    guiTest.assertTrueVerbose( "PlanWorks title does not contain '" + PWTestHelper.PROJECT1,
                               planWorks.getTitle().endsWith( PWTestHelper.PROJECT1), "not ");

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);
    PWTestHelper.deleteProject( PWTestHelper.PROJECT2, helper, guiTest);

    System.err.println( "\nPLANVIZ_05 COMPLETED\n");
  } // end planViz05

} // end class PlanWorksGUITest05
