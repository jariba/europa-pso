//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PWTestHelper.java,v 1.2 2004-04-06 21:27:49 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.impl.PwPlanningSequenceImpl;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


public abstract class PWTestHelper {

  public static final String GUI_TEST_DIR = "GUITest";

  public static List buildTestData( int numSequences, int numSteps, PlanWorks planWorks) {
    String guiSequencesUrl = System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + GUI_TEST_DIR;
    File guiSequencesUrlFile = new File( guiSequencesUrl);
    boolean success = false;
    if (guiSequencesUrlFile.isDirectory()) {
      success = FileUtils.deleteDir( guiSequencesUrlFile);
      if (! success) {
        System.err.println( "PWTestHelper.buildTestData: deleting '" + guiSequencesUrl +
                            "' failed"); System.exit( -1);
      }
    }
    success = guiSequencesUrlFile.mkdir();
    if (! success) {
      System.err.println( "PWTestHelper.buildTestData: creating '" + guiSequencesUrl +
                          "' failed"); System.exit( -1);
    }
    List sequenceUrls = new ArrayList();
    for (int i = 0; i < numSequences; i++) {
      sequenceUrls.add( createSequence( guiSequencesUrl, numSteps));
    }
    return sequenceUrls;
  } // end buildTestData

  private static String createSequence( String guiSequencesUrl, int numSteps) {
    boolean forTesting = true;
    int entityId = 0;
    Long sequenceId = new Long( System.currentTimeMillis());
    String sequenceIdString = sequenceId.toString();
    String sequenceName = "sequence" + sequenceIdString;
    String sequenceUrl = guiSequencesUrl + System.getProperty( "file.separator") + sequenceName;
    System.err.println( "sequenceUrl " + sequenceUrl);
    writeDirectory( sequenceUrl);
    PwPlanningSequenceImpl planSequence = null;
    try {
      planSequence = new PwPlanningSequenceImpl( sequenceUrl, sequenceId, forTesting);
    } catch (ResourceNotFoundException rnfe) {
      System.err.println( rnfe.getMessage());
      System.exit( -1);
    }

    for (int stepNum = 0; stepNum < numSteps; stepNum++) {
      planSequence.addPartialPlan( createPartialPlan( planSequence, sequenceUrl,
                                                      stepNum, entityId), forTesting);
    }

    String [] planSequenceContent = planSequence.toOutputString();
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_PP_STATS, planSequenceContent[0]);
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_FILE, planSequenceContent[1]);
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_TRANSACTIONS, planSequenceContent[2]);
    return sequenceUrl;
  } // end createSequence

  private static PwPartialPlanImpl createPartialPlan( PwPlanningSequenceImpl planSequence,
                                                      String sequenceUrl, int stepNum,
                                                      int entityId) {
    String model = "basic-model";
    Long partialPlanId = new Long( System.currentTimeMillis());
    String partialPlanName = "step" + String.valueOf( stepNum);
    String partialPlanUrl = sequenceUrl + System.getProperty( "file.separator") +
      partialPlanName;
    System.err.println( "partialPlanUrl " + partialPlanUrl);
    writeDirectory( partialPlanUrl);
    PwPartialPlanImpl partialPlan = null;
    try {
      partialPlan = new PwPartialPlanImpl( sequenceUrl, partialPlanName, planSequence,
                                           partialPlanId, model);
      partialPlan.setName( partialPlanUrl);
    } catch (ResourceNotFoundException rnfe) {
      System.err.println( rnfe.getMessage());
      System.exit( -1);
    }

    int numTokens = 3, numVariables = 15, numConstraints = 15, numTransactions = 0;
//     for (int nToken = 0; nToken < numTokens; nToken++) {
//       PwTokenImpl token = new PwTokenImpl();
//       partialPlan.addToken( new Integer( ++entityId), token);
//     }

    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_CONSTRAINTS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_CONSTRAINT_VAR_MAP_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_RESOURCE_INSTANTS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_OBJECTS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_PARTIAL_PLAN_EXT,
                          partialPlan.toOutputString());
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_TOKEN_RELATIONS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_TOKENS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_VARIABLES_EXT,
                          "");
    return partialPlan;
  } // end createPartialPlan

  private static void writeDirectory( String dir) {
    File dirFile = new File( dir);
    boolean success = dirFile.mkdir();
    if (! success) {
      System.err.println( "PWTestHelper.buildTestData: creating '" + dir +
                          "' failed");
      System.exit( -1);
    }
  } // end writeDirectory

  private static void writePlanSequenceFile( String sequenceUrl, String fileExtension,
                                             String content) {
    String fileName = sequenceUrl + System.getProperty( "file.separator") + fileExtension;
    try {
      BufferedWriter out = new BufferedWriter( new FileWriter( fileName));
      out.write( content);
      out.close();
    } catch (IOException e) {
    }
  } // end writePlanSequenceFile

  private static void writePartialPlanFile( String partialPlanUrl, String partialPlanName,
                                            String fileExtension, String content) {
    String fileName = partialPlanUrl + System.getProperty( "file.separator") +
      partialPlanName + "." + fileExtension;
    BufferedWriter out = null;
    try {
      out = new BufferedWriter( new FileWriter( fileName));
      out.write( content);
      out.close();
    } catch (IOException e) {
    }
  } // end writePartialPlanFile


} // end abstract class PWTestHelper

    

