//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest02.java,v 1.2 2005-11-10 01:22:10 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.io.File;
import java.util.List;
import javax.swing.AbstractButton;

import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;
//import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;    
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;


public class PlanWorksGUITest02 {

  private PlanWorksGUITest02() {
  }

  public static void planViz02( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
                                PlanWorksGUITest guiTest)
    throws Exception {
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    int numSequences = 3;
    File [] sequenceFileArray = new File [numSequences];
    for (int i = 0; i < numSequences; i++) {
      sequenceFileArray[i] = new File( (String) sequenceUrls.get( i));
    }
    // modify sequence #1
    int seq1NumFilesDeleted = 3;
    (new File( sequenceFileArray[0] + System.getProperty("file.separator") +
               DbConstants.SEQ_PP_STATS)).delete();
    (new File( sequenceFileArray[0] + System.getProperty("file.separator") +
               DbConstants.SEQ_FILE)).delete();
//     (new File( sequenceFileArray[0] + System.getProperty("file.separator") +
//                DbConstants.SEQ_TRANSACTIONS)).delete();
    String [] fileNames = sequenceFileArray[0].list();
    boolean success = true;
    for (int i = 0, n = fileNames.length; i < n; i++) {
      File fileNameFile = new File( fileNames[i]);
      if (fileNameFile.isDirectory()) {
        success = FileUtils.deleteDir( fileNameFile);
        if (! success) {
          System.err.println( "PlanWorksGUITest02.planViz02: deleting '" + fileNames[i] +
                              "' failed"); System.exit( -1);
        }
      }
    }
    // modify sequence #2
    int seq2NumFilesDeleted = 1;
    (new File( sequenceFileArray[1] + System.getProperty("file.separator") +
               DbConstants.SEQ_FILE)).delete();

    String stepName = "step1"; int stepNumber = 1;

    // modify sequence #3
    String stepDir = sequenceFileArray[2] + System.getProperty("file.separator") + stepName;
    success = FileUtils.deleteDir( new File( stepDir));
    if (! success) {
      System.err.println( "PlanWorksGUITest02.planViz02: deleting '" + stepDir +
                          "' failed"); System.exit( -1);
    }
    File [] seqFileArray = new File [1];
    // try sequence #1
    seqFileArray[0] = sequenceFileArray[0];
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, helper, guiTest,
                                planWorks);
    PWTestHelper.addSequencesToProject( seqFileArray, helper, guiTest, planWorks);
    int seq1FilesRemaining = DbConstants.SEQUENCE_FILES.length - seq1NumFilesDeleted;
    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK", seq1FilesRemaining +
                               " sequence files in directory -- " +
                               DbConstants.SEQUENCE_FILES.length + " are required", helper,
                               guiTest);
    AbstractButton cancelButton = PWTestHelper.findButton( "Cancel");
    guiTest.assertNotNullVerbose( "'Project->Create' cancel button not found:",
                                  cancelButton, "not ");
    helper.enterClickAndLeave(new MouseEventData(guiTest, cancelButton));
    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);
    // try{Thread.sleep(5000);}catch(Exception e){}

    // try sequences #2, #3
    File [] seq2FileArray = new File [2];
    seq2FileArray[0] = sequenceFileArray[1];
    seq2FileArray[1] = sequenceFileArray[2];
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, helper, guiTest,
                                planWorks);
    PWTestHelper.addSequencesToProject( seq2FileArray, helper, guiTest, planWorks);
    int seq2FilesRemaining = DbConstants.SEQUENCE_FILES.length - seq2NumFilesDeleted;
    PWTestHelper.handleDialog( "Invalid Sequence Directory", "OK", seq2FilesRemaining +
                               " sequence files in directory -- " +
                               DbConstants.SEQUENCE_FILES.length + " are required",
                               helper, guiTest);

    ViewListener viewListener01 = new PlanWorksGUITest.ViewListenerWait01( guiTest);
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME, viewListener01,
                                       helper, guiTest);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME, helper, guiTest);

    List viewListenerList = guiTest.createViewListenerList();
    String viewMenuItemName = "Open " + ViewConstants.TIMELINE_VIEW;
    boolean isTimelineViewAvailable =
      PWTestHelper.seqStepsViewStepItemExists( seqStepsView, stepNumber, viewMenuItemName,
                                               viewListenerList, helper, guiTest);
    guiTest.assertFalseVerbose( "DBTransaction View is not is only view available",
                                isTimelineViewAvailable, "not ");

//     viewMenuItemName = "Open " + ViewConstants.DB_TRANSACTION_VIEW;
//     PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
//                                                 viewMenuItemName, viewListenerList,
//                                                 helper, guiTest);
//    String viewNameSuffix = PWTestHelper.SEQUENCE_NAME +
//      System.getProperty( "file.separator") + "step" + String.valueOf( stepNumber);
//     DBTransactionView dbTransactionView =
//       ( DBTransactionView) PWTestHelper.getPartialPlanView
//       ( ViewConstants.DB_TRANSACTION_VIEW, viewNameSuffix, guiTest);

    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);

    System.err.println( "\nPLANVIZ_02 COMPLETED\n");
  } // end planViz02

} // end class PlanWorksGUITest02
