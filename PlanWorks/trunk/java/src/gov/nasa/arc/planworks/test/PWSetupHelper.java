//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PWSetupHelper.java,v 1.1 2004-04-22 19:26:20 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.impl.PwConstraintImpl;
import gov.nasa.arc.planworks.db.impl.PwDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwEnumeratedDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwObjectImpl;
import gov.nasa.arc.planworks.db.impl.PwPlanningSequenceImpl;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceTransactionImpl;
import gov.nasa.arc.planworks.db.impl.PwSlotImpl;
import gov.nasa.arc.planworks.db.impl.PwTimelineImpl;
import gov.nasa.arc.planworks.db.impl.PwTokenImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableImpl;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>PWSetupHelper</code> - create test data
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public abstract class PWSetupHelper {

  private static final int NUM_OBJECTS = 2;
  private static final int NUM_TIMELINES = 3;
  private static final int NUM_RESOURCES = 4;
  private static final int NUM_TOKENS = 5;


  public static List buildTestData( final int numSequences, final int numSteps,
                                    final PlanWorksGUITest guiTest) {
    String guiSequencesUrl = System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File guiSequencesUrlFile = new File( guiSequencesUrl);
    boolean success = false;
    if (guiSequencesUrlFile.isDirectory()) {
      success = FileUtils.deleteDir( guiSequencesUrlFile);
      if (! success) {
        System.err.println( "PWSetupHelper.buildTestData: deleting '" + guiSequencesUrl +
                            "' failed"); System.exit( -1);
      }
    }
    success = guiSequencesUrlFile.mkdir();
    if (! success) {
      System.err.println( "PWSetupHelper.buildTestData: creating '" + guiSequencesUrl +
                          "' failed"); System.exit( -1);
    }
    List sequenceUrls = new ArrayList();
    for (int i = 0; i < numSequences; i++) {
      sequenceUrls.add( createSequence( guiSequencesUrl, numSteps, guiTest));
    }
    return sequenceUrls;
  } // end buildTestData

  private static String createSequence( final String guiSequencesUrl, final int numSteps,
                                        final PlanWorksGUITest guiTest) {
    boolean forTesting = true;
    guiTest.resetEntityIdInt();
    Long sequenceId = new Long( System.currentTimeMillis());
    String sequenceIdString = sequenceId.toString();
    String sequenceName = PWTestHelper.SEQUENCE_NAME + sequenceIdString;
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
                                                      stepNum, guiTest), forTesting);
    }

    String [] planSequenceContent = planSequence.toOutputString();
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_PP_STATS, planSequenceContent[0]);
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_FILE, planSequenceContent[1]);
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_TRANSACTIONS, planSequenceContent[2]);
    return sequenceUrl;
  } // end createSequence

  private static PwPartialPlanImpl createPartialPlan( final PwPlanningSequenceImpl planSequence,
                                                      final String sequenceUrl,
                                                      final int stepNum,
                                                      final PlanWorksGUITest guiTest) {
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

      createObjectTableEntries( partialPlan, guiTest);
                                                         

    } catch (ResourceNotFoundException rnfe) {
      System.err.println( rnfe.getMessage());
      System.exit( -1);
    }

    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_CONSTRAINTS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_CONSTRAINT_VAR_MAP_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_RESOURCE_INSTANTS_EXT,
                          "");
    StringBuffer objectsBuffer = new StringBuffer();
    Iterator objectsItr = partialPlan.getObjectList().iterator();
    while (objectsItr.hasNext()) {
      objectsBuffer.append( ((PwObjectImpl) objectsItr.next()).toOutputString());
    }
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_OBJECTS_EXT,
                          objectsBuffer.toString());
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_PARTIAL_PLAN_EXT,
                          partialPlan.toOutputString());
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_TOKEN_RELATIONS_EXT,
                          "");
    StringBuffer tokensBuffer = new StringBuffer();
    Iterator tokensItr = partialPlan.getTokenList().iterator();
    while (tokensItr.hasNext()) {
      tokensBuffer.append( ((PwTokenImpl) tokensItr.next()).toOutputString());
    }
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_TOKENS_EXT,
                          tokensBuffer.toString());
    StringBuffer variablesBuffer = new StringBuffer();
    Iterator variablesItr = partialPlan.getVariableList().iterator();
    while (variablesItr.hasNext()) {
      variablesBuffer.append( ((PwVariableImpl) variablesItr.next()).toOutputString());
    }
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_VARIABLES_EXT,
                          variablesBuffer.toString());
    return partialPlan;
  } // end createPartialPlan

  private static void writeDirectory( final String dir) {
    File dirFile = new File( dir);
    boolean success = dirFile.mkdir();
    if (! success) {
      System.err.println( "PWSetupHelper.buildTestData: creating '" + dir +
                          "' failed");
      System.exit( -1);
    }
  } // end writeDirectory

  private static void writePlanSequenceFile( final String sequenceUrl,
                                             final String fileExtension,
                                             final String content) {
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

  private static void createObjectTableEntries( final PwPartialPlanImpl partialPlan,
                                                final PlanWorksGUITest guiTest) {
    for (int i = 0; i < NUM_OBJECTS; i++) {
      Integer objectId = new Integer( guiTest.incEntityIdInt());
      StringBuffer componentIds = new StringBuffer();
      boolean isFirst = true; String emptySlotInfo = null;
      for (int j = 0; j < NUM_TIMELINES; j++) {
        int timelineInt = guiTest.incEntityIdInt();
        if (isFirst) { isFirst = false; } else { componentIds.append( ","); }
        componentIds.append( timelineInt);
        Integer timelineId = new Integer( timelineInt);
        String tokenIds = null;
        PwTimelineImpl timeline =
          addTimeline( timelineId, DbConstants.O_TIMELINE, objectId,
                       "timeline" + String.valueOf( timelineInt), "", "", emptySlotInfo,
                       tokenIds, partialPlan);
        // timeline object must exist when tokens are created
        String [] stringArray = addTokensToTimeline( timelineId, partialPlan, guiTest);
        tokenIds = stringArray[0];
        String slotIds = stringArray[1];
        StringTokenizer strTok = new StringTokenizer( tokenIds, ",");
        while (strTok.hasMoreTokens()) {
          Integer tokenId = Integer.valueOf( strTok.nextToken());
          timeline.addToken( tokenId);
        }
        strTok = new StringTokenizer( slotIds, ",");
        while (strTok.hasMoreTokens()) {
          Integer slotId = Integer.valueOf( strTok.nextToken());
          timeline.addSlot( slotId);
        }
      }
      StringBuffer resInfo = new StringBuffer();
      int horizonStart = 0; resInfo.append( String.valueOf( horizonStart));
      resInfo.append( ",");
      int horizonEnd = 100; resInfo.append( String.valueOf( horizonEnd));
      resInfo.append( ",");
      double initialCapacity = 10.0; resInfo.append( String.valueOf( initialCapacity));
      resInfo.append( ",");
      double levelLimitMin = 0.0; resInfo.append( String.valueOf( levelLimitMin));
      resInfo.append( ",");
      double levelLimitMax = 80.0; resInfo.append( String.valueOf( levelLimitMax));
      // resource instant ids optionally follow
      for (int j = 0; j < NUM_RESOURCES; j++) {
        int resourceInt = guiTest.incEntityIdInt();
        if (isFirst) { isFirst = false; } else { componentIds.append( ","); }
        componentIds.append( resourceInt);
        addResource( new Integer( resourceInt), DbConstants.O_RESOURCE, objectId,
                     "resource" + String.valueOf( resourceInt), "", resInfo.toString(),
                     "", "", partialPlan);
      }
      addObject( objectId, DbConstants.O_OBJECT, DbConstants.NO_ID, "object" +
                 objectId.toString(), componentIds.toString(), "", "", partialPlan);
    }
  } // end createObjectTableEntries

  private static String [] addTokensToTimeline( final Integer timelineId, 
                                                final PwPartialPlanImpl partialPlan,
                                                final PlanWorksGUITest guiTest) {
    StringBuffer tokenIds = new StringBuffer();
    StringBuffer slotIds = new StringBuffer();
    boolean isFirst = true, isFirstSlot = true; boolean isValueToken = true;
    int time = 0, timeIncrement = 20;
    for (int i = 0; i < NUM_TOKENS; i++) {
      int tokenInt = guiTest.incEntityIdInt();
      if (isFirst) { isFirst = false; } else { tokenIds.append( ","); }
      tokenIds.append( tokenInt);
      Integer tokenId = new Integer( tokenInt);
      int slotInt = guiTest.incEntityIdInt();
      if (isFirstSlot) { isFirstSlot = false; } else { slotIds.append( ","); }
      slotIds.append( slotInt);
      Integer slotId =  new Integer( slotInt);
      List constraintIds = new ArrayList();
      List parameterNames = new ArrayList(); parameterNames.add( "start");
      Integer startVarId = new Integer( guiTest.incEntityIdInt());
      int startLower = time, startUpper = time + 4;
      addVariable( startVarId, DbConstants.START_VAR, constraintIds,
                   parameterNames, tokenId,
                   new PwIntervalDomainImpl( "INTEGER_SORT", String.valueOf( startLower),
                                             String.valueOf( startUpper)),
                   partialPlan);
      Integer endVarId = new Integer( guiTest.incEntityIdInt());
      int endLower = time + 20, endUpper = time + 24;
      parameterNames = new ArrayList(); parameterNames.add( "end");
      addVariable( endVarId, DbConstants.END_VAR, constraintIds,
                   parameterNames, tokenId,
                   new PwIntervalDomainImpl( "INTEGER_SORT", String.valueOf( endLower),
                                             String.valueOf( endUpper)),
                   partialPlan);
      Integer durationVarId = new Integer( guiTest.incEntityIdInt());
      int durationLower = endLower - startUpper;
      int durationUpper = endUpper - startLower;
      parameterNames = new ArrayList(); parameterNames.add( "duration");
      addVariable( endVarId, DbConstants.DURATION_VAR, constraintIds,
                   parameterNames, tokenId,
                   new PwIntervalDomainImpl( "INTEGER_SORT", String.valueOf( durationLower),
                                             String.valueOf( durationUpper)),
                   partialPlan);
      Integer stateVarId = new Integer( guiTest.incEntityIdInt());
      parameterNames = new ArrayList(); parameterNames.add( "state");
      addVariable( endVarId, DbConstants.STATE_VAR, constraintIds,
                   parameterNames, tokenId, new PwEnumeratedDomainImpl( "2"), partialPlan);
      Integer objectVarId = new Integer( guiTest.incEntityIdInt());
      parameterNames = new ArrayList(); parameterNames.add( "object");
      addVariable( endVarId, DbConstants.OBJECT_VAR, constraintIds,
                   parameterNames, tokenId,
                   new PwEnumeratedDomainImpl( "predicate"), partialPlan);
      String tokenRelationIds = null;
      String paramVarIds = null;
      String tokenInfo = "0"; // becomes slotIndex -- 0 means base token
      addToken( tokenId, isValueToken, slotId, "predicate" +
                String.valueOf( tokenInt), startVarId, endVarId, durationVarId,
                stateVarId, objectVarId, timelineId, tokenRelationIds, paramVarIds,
                tokenInfo, partialPlan);
      time += timeIncrement;
    }
    String [] stringArray = new String [2];
    stringArray[0] = tokenIds.toString();
    stringArray[1] = slotIds.toString();
    return stringArray;
  } // end addTokensToTimeline
 

  private static void addObject( final Integer id, final int objectType,
                                 final Integer parentId, final String name,
                                 final String componentIds, final String variableIds,
                                 final String tokenIds,
                                 final PwPartialPlanImpl partialPlan) {
    partialPlan.addObject( id,
                           new PwObjectImpl( id, objectType, parentId, name, componentIds,
                                             variableIds, tokenIds, partialPlan));
  } // end addObject

  private static PwTimelineImpl addTimeline( final Integer id, final int type,
                                             final Integer parentId, 
                                             final String name, final String childObjectIds,
                                             final String emptySlotInfo,
                                             final String variableIds, final String tokenIds,
                                             final PwPartialPlanImpl partialPlan) {
    PwTimelineImpl timeline = new PwTimelineImpl( id, type, parentId, name, childObjectIds,
                                                 emptySlotInfo, variableIds, tokenIds,
                                                  partialPlan);
    partialPlan.addTimeline( id, timeline);
    return timeline;
  } // end addTimeline

  private static void addResource( final Integer id, final int type, final Integer parentId,
                                   final String name, final String childObjectIds,
                                   final String resInfo, final String variableIds,
                                   final String tokenIds,
                                   final PwPartialPlanImpl partialPlan) {
     partialPlan.addResource( id,
                              new PwResourceImpl( id, type, parentId, name, childObjectIds,
                                                  resInfo, variableIds, tokenIds,
                                                  partialPlan));
  } // end addResource

  private static void addToken( final Integer id, final boolean isValueToken,
                                final Integer slotId, final String predicateName,
                                final Integer startVarId, final Integer endVarId, 
                                final Integer durationVarId, final Integer stateVarId, 
                                final Integer objectVarId, final Integer parentId, 
                                final String tokenRelationIds, final String paramVarIds, 
                                final String tokenInfo, final PwPartialPlanImpl partialPlan) {
    partialPlan.addToken( id,
                          new PwTokenImpl( id, isValueToken, slotId, predicateName,
                                           startVarId, endVarId, durationVarId,
                                           stateVarId, objectVarId, parentId,
                                           tokenRelationIds, paramVarIds, tokenInfo,
                                           partialPlan));
  } // end addToken

  private static void addVariable( final Integer id, final String type,
                                   final List constraintIds, final List parameterNames,
                                   final Integer parentId, final PwDomainImpl domain,
                                   final PwPartialPlanImpl partialPlan) {
    partialPlan.addVariable( id,
                             new PwVariableImpl( id, type, constraintIds, parameterNames,
                                                 parentId, domain, partialPlan));
  } // end addVariable
        
} // end class PWSetupHelper
