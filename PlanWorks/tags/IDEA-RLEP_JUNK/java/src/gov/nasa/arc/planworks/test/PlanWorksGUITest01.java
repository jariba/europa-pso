//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest01.java,v 1.1 2004-10-01 20:04:31 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.io.File;
import java.util.List;

import junit.extensions.jfcunit.JFCTestHelper;

import gov.nasa.arc.planworks.PlanWorks;


public class PlanWorksGUITest01 {

  private PlanWorksGUITest01() {
  }

  public static void planViz01( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
                                PlanWorksGUITest guiTest)
    throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( 0));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, helper, guiTest,
                                planWorks);
    PWTestHelper.addSequencesToProject( sequenceFileArray, helper, guiTest, planWorks);
    // post condition 1
    guiTest.assertTrueVerbose("PlanWorks title does not contain " + PWTestHelper.PROJECT1,
                              planWorks.getTitle().endsWith( PWTestHelper.PROJECT1), "not ");

    // post condition 2
    PWTestHelper.getPlanSequenceMenu();

    // try{Thread.sleep(5000);}catch(Exception e){}

    // post condition 3
    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);

    System.err.println( "\nPLANVIZ_01 COMPLETED\n");
  } // end planViz01


} // end class PlanWorksGUITest01
