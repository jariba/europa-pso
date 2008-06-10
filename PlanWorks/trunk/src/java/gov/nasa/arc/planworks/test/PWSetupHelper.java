//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PWSetupHelper.java,v 1.18 2005-11-10 01:22:10 miatauro Exp $
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

import junit.framework.Assert;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.impl.PwConstraintImpl;
//import gov.nasa.arc.planworks.db.impl.PwDBTransactionImpl;
import gov.nasa.arc.planworks.db.impl.PwDecisionImpl;
import gov.nasa.arc.planworks.db.impl.PwDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwEnumeratedDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwObjectImpl;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.impl.PwPlanningSequenceImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceInstantImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceTransactionImpl;
import gov.nasa.arc.planworks.db.impl.PwRuleImpl;
import gov.nasa.arc.planworks.db.impl.PwRuleInstanceImpl;
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
  private static final int NUM_TIMELINES = 2;
  private static final int NUM_RESOURCES = 4;
  private static final int NUM_RESOURCE_TRANSACTIONS = 3;
  private static final int NUM_SLOTTED_TOKENS = 6;
  private static final int MERGED_TOKEN_INDEX = 1; // in slotted tokens
  private static final int EMPTY_SLOT_INDEX = 1; // in timeline
  private static final int NUM_FREE_TOKENS = 4;

  private static final int SLOTTED_TOKEN = 0;
  private static final int FREE_TOKEN = 1;
  private static final int RESOURCE_TRANSACTION = 2;

  public static final int NUM_VARS_PER_OBJECT = 3;
  public static final int NUM_VARS_PER_TIMELINE = 2;
  public static final int NUM_VARS_PER_TOKEN = 6;
  public static final int NUM_VARS_PER_RESOURCE = 4;
  public static final int NUM_VARS_PER_RESOURCE_TRANS = 5;
  public static final int NUM_VARS_PER_RULE_INSTANCE = 2;
  public static final int NUM_CONSTRAINTS_PER_TOKEN = 1;
  public static final int NUM_CONSTRAINTS_PER_RESOURCE = 0;
  public static final int NUM_INSTANTS_PER_RESOURCE = 1;

  private static List decisionsForStep;
  private static List rulesForSequence;

  public static List buildTestData( final int numSequences, final int numSteps,
                                    final IdSource idSource, final String dest) {
    String sequencesUrl = System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + dest;
    File sequencesUrlFile = new File( sequencesUrl);
    boolean success = false;
    if (sequencesUrlFile.isDirectory()) {
      success = FileUtils.deleteDir( sequencesUrlFile);
      if (! success) {
        System.err.println( "PWSetupHelper.buildTestData: deleting '" + sequencesUrl +
                            "' failed"); System.exit( -1);
      }
    }
    success = sequencesUrlFile.mkdir();
    if (! success) {
      System.err.println( "PWSetupHelper.buildTestData: creating '" + sequencesUrl +
                          "' failed"); System.exit( -1);
    }
    List sequenceUrls = new ArrayList();
    for (int i = 0; i < numSequences; i++) {
      sequenceUrls.add( createSequence( sequencesUrl, numSteps, idSource));
    }
    return sequenceUrls;
  } // end buildTestData

  private static String createSequence( final String sequencesUrl, final int numSteps,
                                        final IdSource idSource) {
    boolean forTesting = true;
    idSource.resetEntityIdInt();
    Long sequenceId = new Long( System.currentTimeMillis());
    String sequenceIdString = sequenceId.toString();
    String sequenceName = PWTestHelper.SEQUENCE_NAME + sequenceIdString;
    String sequenceUrl = sequencesUrl + System.getProperty( "file.separator") + sequenceName;
    System.err.println( "sequenceUrl " + sequenceUrl);
    writeDirectory( sequenceUrl);
    PwPlanningSequenceImpl planSequence = null;
    try {
      planSequence = new PwPlanningSequenceImpl( sequenceUrl, sequenceId, forTesting);
    } catch (ResourceNotFoundException rnfe) {
      System.err.println( rnfe.getMessage());
      System.exit( -1);
    }

    rulesForSequence = new ArrayList();
    for (int stepNum = 0; stepNum < numSteps; stepNum++) {
      planSequence.addPartialPlan( createPartialPlan( planSequence, sequenceUrl,
                                                      stepNum, idSource), forTesting);
    }

    String [] planSequenceContent = planSequence.toOutputString();
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_PP_STATS, planSequenceContent[0]);
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_FILE, planSequenceContent[1]);
    //writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_TRANSACTIONS, planSequenceContent[2]);

    StringBuffer rulesBuffer = new StringBuffer();
    Iterator rulesItr = rulesForSequence.iterator();
    while (rulesItr.hasNext()) {
      rulesBuffer.append( ((PwRuleImpl) rulesItr.next()).toOutputString());
    }
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_RULES, rulesBuffer.toString());
    return sequenceUrl;
  } // end createSequence

  private static PwPartialPlanImpl createPartialPlan( final PwPlanningSequenceImpl planSequence,
                                                      final String sequenceUrl,
                                                      final int stepNum,
                                                      final IdSource idSource) {
    String model = "basic-model";
    Long partialPlanId = new Long( System.currentTimeMillis());
    String partialPlanName = "step" + String.valueOf( stepNum);
    String partialPlanUrl = sequenceUrl + System.getProperty( "file.separator") +
      partialPlanName;
    System.err.println( "partialPlanUrl " + partialPlanUrl);
    writeDirectory( partialPlanUrl);
    PwPartialPlanImpl partialPlan = null; 
    decisionsForStep = new ArrayList();
    try {
      partialPlan = new PwPartialPlanImpl( sequenceUrl, partialPlanName, planSequence,
                                           partialPlanId, model);
      partialPlan.setName( partialPlanUrl);

      createObjectTableEntries( partialPlan, planSequence, stepNum, idSource);
                                                         

    } catch (ResourceNotFoundException rnfe) {
      System.err.println( rnfe.getMessage());
      System.exit( -1);
    }

    StringBuffer constraintsBuffer = new StringBuffer();
    Iterator constraintsItr = partialPlan.getConstraintList().iterator();
    while (constraintsItr.hasNext()) {
      constraintsBuffer.append( ((PwConstraintImpl) constraintsItr.next()).toOutputString());
    }
    StringBuffer constraintVarMapBuffer = new StringBuffer();
    constraintsItr = partialPlan.getConstraintList().iterator();
    while (constraintsItr.hasNext()) {
      constraintVarMapBuffer.append( ((PwConstraintImpl) constraintsItr.next()).
                                     toOutputStringVarMap());
    }
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_CONSTRAINTS_EXT,
                          constraintsBuffer.toString());
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_CONSTRAINT_VAR_MAP_EXT,
                          constraintVarMapBuffer.toString());
    StringBuffer objectsBuffer = new StringBuffer();
    Iterator objectsItr = partialPlan.getObjectList().iterator();
    while (objectsItr.hasNext()) {
      objectsBuffer.append( ((PwObjectImpl) objectsItr.next()).toOutputString());
    }
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_OBJECTS_EXT,
                          objectsBuffer.toString());
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_PARTIAL_PLAN_EXT,
                          partialPlan.toOutputString());
    StringBuffer tokensBuffer = new StringBuffer();
    Iterator tokensItr = partialPlan.getTokenList().iterator();
    while (tokensItr.hasNext()) {
      PwTokenImpl token = (PwTokenImpl) tokensItr.next();
      if (token instanceof PwResourceTransactionImpl) {
        tokensBuffer.append( ((PwResourceTransactionImpl) token).toOutputString());
      } else {
        tokensBuffer.append( ((PwTokenImpl) token).toOutputString());
      }
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

    StringBuffer resourceInstantsBuffer = new StringBuffer();
    Iterator resourceInstantsItr = partialPlan.getInstantList().iterator();
    while (resourceInstantsItr.hasNext()) {
      resourceInstantsBuffer.append( ((PwResourceInstantImpl) resourceInstantsItr.next()).
                                   toOutputString());
    }
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_RESOURCE_INSTANTS_EXT,
                          resourceInstantsBuffer.toString());

    StringBuffer decisionsBuffer = new StringBuffer();
    Iterator decisionsItr = decisionsForStep.iterator();
    while (decisionsItr.hasNext()) {
      decisionsBuffer.append( ((PwDecisionImpl) decisionsItr.next()).toOutputString());
    }
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_DECISIONS_EXT,
                          decisionsBuffer.toString());  
  
    StringBuffer ruleInstancesBuffer = new StringBuffer();
    Iterator ruleInstancesItr = partialPlan.getRuleInstanceList().iterator();
    while (ruleInstancesItr.hasNext()) {
      ruleInstancesBuffer.append( ((PwRuleInstanceImpl) ruleInstancesItr.next()).
                                   toOutputString());
    }
    StringBuffer ruleInstancesSlaveMapBuffer = new StringBuffer();
    ruleInstancesItr = partialPlan.getRuleInstanceList().iterator();
    while (ruleInstancesItr.hasNext()) {
      ruleInstancesSlaveMapBuffer.append( ((PwRuleInstanceImpl) ruleInstancesItr.next()).
                                          toOutputStringSlaveMap());
    }
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_RULE_INSTANCE_EXT,
                           ruleInstancesBuffer.toString());
    writePartialPlanFile( partialPlanUrl, partialPlanName,
                          DbConstants.PP_RULE_INSTANCE_SLAVE_MAP_EXT,
                           ruleInstancesSlaveMapBuffer.toString());
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
                                                final PwPlanningSequenceImpl planSequence,
                                                final int stepNum,
                                                final IdSource idSource) {
    Integer decisionTokenId = null;
    for (int i = 0; i < NUM_OBJECTS; i++) {
      Integer objectId = new Integer( idSource.incEntityIdInt());
      StringBuffer componentIds = new StringBuffer();
      boolean isFirst = true;
      for (int j = 0; j < NUM_TIMELINES; j++) {
        int timelineIdInt = idSource.incEntityIdInt();
        if (isFirst) { isFirst = false; } else { componentIds.append( ","); }
        componentIds.append( timelineIdInt);
        Integer timelineId = new Integer( timelineIdInt);
        String tokenIds = null;
        String emptySlotInfo = (new Integer( idSource.incEntityIdInt())).toString() +
          "," + String.valueOf( EMPTY_SLOT_INDEX);
        PwTimelineImpl timeline =
          addTimeline( timelineId, DbConstants.O_TIMELINE, objectId,
                       "timeline" + String.valueOf( timelineIdInt), "", emptySlotInfo, "", 
                       tokenIds, partialPlan, planSequence, stepNum, idSource);
        // timeline object must exist when tokens are created
        // slotted tokens
        String [] stringArray = addTokensToTimeline( SLOTTED_TOKEN, NUM_SLOTTED_TOKENS,
                                                     timelineId, partialPlan,
                                                     planSequence, stepNum, idSource);
        tokenIds = stringArray[0];
        String slotIds = stringArray[1];
        StringTokenizer strTok = new StringTokenizer( tokenIds, ",");
        while (strTok.hasMoreTokens()) {
          Integer tokenId = Integer.valueOf( strTok.nextToken());
          decisionTokenId = tokenId;
          timeline.addToken( tokenId);
        }
        strTok = new StringTokenizer( slotIds, ",");
        while (strTok.hasMoreTokens()) {
          Integer slotId = Integer.valueOf( strTok.nextToken());
          timeline.addSlot( slotId);
        }
        // free tokens
        stringArray = addTokensToTimeline( FREE_TOKEN, NUM_FREE_TOKENS,
                                           DbConstants.NO_ID, partialPlan,
                                           planSequence, stepNum, idSource);
        tokenIds = stringArray[0];
        strTok = new StringTokenizer( tokenIds, ",");
        while (strTok.hasMoreTokens()) {
          Integer tokenId = Integer.valueOf( strTok.nextToken());
          timeline.addToken( tokenId);
        }
      }
      componentIds.append( addResourcesAndInstants( objectId, partialPlan, planSequence,
                                                    stepNum, idSource));
      addObject( objectId, DbConstants.O_OBJECT, DbConstants.NO_ID, "object" +
                 objectId.toString(), componentIds.toString(), "", "", partialPlan,
                 planSequence, stepNum, idSource);
    }
    Integer decisionId = new Integer( idSource.incEntityIdInt());
//     addTransaction( DbConstants.ASSIGN_NEXT_DECISION_SUCCEEDED,
//                     new Integer( idSource.incEntityIdInt()),
//                     DbConstants.SOURCE_USER, decisionId, new Integer( stepNum),
//                     partialPlan.getId(), null, planSequence);
    boolean unit = false;
    addDecision( decisionId, DbConstants.D_TOKEN, decisionTokenId, unit, partialPlan);
  } // end createObjectTableEntries

  private static StringBuffer addResourcesAndInstants( final Integer objectId,
                                                       final PwPartialPlanImpl partialPlan,
                                                       final PwPlanningSequenceImpl planSequence,
                                                       final int stepNum,
                                                       final IdSource idSource) {
    StringBuffer componentIds = new StringBuffer();
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
      Integer resourceId = new Integer( idSource.incEntityIdInt());
      componentIds.append( ",");
      componentIds.append( resourceId.toString());
      int memberValue = 0;
      boolean isFirst = true;
      StringBuffer resourceVarIds = new StringBuffer();
      for (int i = 0; i < NUM_VARS_PER_RESOURCE; i++) {
        List constraintIds = new ArrayList();
        List parameterNames = new ArrayList(); parameterNames.add( "member" + i);
        Integer resourceVarId = new Integer( idSource.incEntityIdInt());
        if (isFirst) { isFirst = false; } else { resourceVarIds.append( ","); }
        resourceVarIds.append( resourceVarId.toString());
        addVariable( resourceVarId, DbConstants.MEMBER_VAR, constraintIds,
                     parameterNames, resourceId,
                     new PwEnumeratedDomainImpl( String.valueOf( memberValue)),
                     partialPlan, planSequence, stepNum, idSource);
        memberValue += 10;
      }
      // resource instants
      for (int i = 0; i < NUM_INSTANTS_PER_RESOURCE; i++) {
        resInfo.append( ",");
        Integer instantId = new Integer( idSource.incEntityIdInt());
        resInfo.append( String.valueOf( instantId));
        int time = 60;
        double levelMin = 0.0;
        double levelMax = 200.0;
        addResourceInstant(instantId, resourceId, time, levelMin, levelMax, "", partialPlan);
      }

      // resourceTransactions
      String [] stringArray =
        addTokensToTimeline( RESOURCE_TRANSACTION, NUM_RESOURCE_TRANSACTIONS,
                             resourceId, partialPlan, planSequence, stepNum, idSource);
      String transactionIds = stringArray[0];
      addResource( resourceId, DbConstants.O_RESOURCE, objectId,
                   "resource" + resourceId.toString(), transactionIds, resInfo.toString(),
                   resourceVarIds.toString(), "", partialPlan);
      // no constraints
    }
    return componentIds;
  } // end addResourcesAndInstants

  private static String [] addTokensToTimeline( final int tokenType, final int numTokens,
                                                final Integer parentId, 
                                                final PwPartialPlanImpl partialPlan,
                                                final PwPlanningSequenceImpl planSequence,
                                                final int stepNum,
                                                final IdSource idSource) {
    StringBuffer tokenIds = new StringBuffer();
    StringBuffer slotIds = new StringBuffer();
    boolean isFirst = true, isFirstSlot = true; boolean isValueToken = true;
    boolean isFirstMerge = true;
    int time = 0, timeIncrement = 20;
    Integer previousTokenId = null, previousSlotId = null;
    Integer ruleId = new Integer( idSource.incEntityIdInt());
    addRule(ruleId, "/dummy/rulesource/model.nddl,1", planSequence);
    for (int i = 0; i < numTokens; i++) {
      int tokenInt = idSource.incEntityIdInt();
      if (isFirst) { isFirst = false; } else { tokenIds.append( ","); }
      tokenIds.append( tokenInt);
      Integer tokenId = new Integer( tokenInt);
      Integer slotId =  null;
      if (tokenType == SLOTTED_TOKEN) {
        if (i == MERGED_TOKEN_INDEX) {
          slotId = previousSlotId;
          isFirstMerge = false;
        } else {
          int slotInt = idSource.incEntityIdInt();
          if (isFirstSlot) { isFirstSlot = false; } else { slotIds.append( ","); }
          slotIds.append( slotInt);
          slotId =  new Integer( slotInt);
        }
      }
      List constraintIds = new ArrayList();
      List parameterNames = new ArrayList(); parameterNames.add( "start");
      Integer startVarId = new Integer( idSource.incEntityIdInt());
      int startLower = time, startUpper = time + 4;
      PwVariableImpl startVar =
        addVariable( startVarId, DbConstants.START_VAR, constraintIds,
                     parameterNames, tokenId,
                     new PwIntervalDomainImpl( DbConstants.INTEGER_INTERVAL_DOMAIN_TYPE,
                                               String.valueOf( startLower),
                                               String.valueOf( startUpper)),
                     partialPlan, planSequence, stepNum, idSource);
      Integer durationVarId = new Integer( idSource.incEntityIdInt());
      int endLower = time + 20, endUpper = time + 24;
      int durationLower = endLower - startUpper;
      int durationUpper = endUpper - startLower;
      parameterNames = new ArrayList(); parameterNames.add( "duration");
      PwVariableImpl durationVar = null, endVar = null; Integer endVarId = null;
      if (tokenType != RESOURCE_TRANSACTION) {
        endVarId = new Integer( idSource.incEntityIdInt());
        parameterNames = new ArrayList(); parameterNames.add( "end");
        endVar =
          addVariable( endVarId, DbConstants.END_VAR, constraintIds,
                       parameterNames, tokenId,
                       new PwIntervalDomainImpl( DbConstants.INTEGER_INTERVAL_DOMAIN_TYPE,
                                                 String.valueOf( endLower),
                                                 String.valueOf( endUpper)),
                       partialPlan, planSequence, stepNum, idSource);
        durationVar =
          addVariable( durationVarId, DbConstants.DURATION_VAR, constraintIds,
                       parameterNames, tokenId,
                       new PwIntervalDomainImpl( DbConstants.INTEGER_INTERVAL_DOMAIN_TYPE,
                                                 String.valueOf( durationLower),
                                                 String.valueOf( durationUpper)),
                       partialPlan, planSequence, stepNum, idSource);
      } else {
        durationVar =
          addVariable( durationVarId, DbConstants.DURATION_VAR, constraintIds,
                       parameterNames, tokenId, new PwEnumeratedDomainImpl( "0"),
                       partialPlan, planSequence, stepNum, idSource);
      }
      Integer stateVarId = new Integer( idSource.incEntityIdInt());
      parameterNames = new ArrayList(); parameterNames.add( "state");
      PwVariableImpl stateVar =
        addVariable( stateVarId, DbConstants.STATE_VAR, constraintIds,
                     parameterNames, tokenId, new PwEnumeratedDomainImpl( "2"),
                     partialPlan, planSequence, stepNum, idSource);
      Integer objectVarId = new Integer( idSource.incEntityIdInt());
      parameterNames = new ArrayList(); parameterNames.add( "object");
      addVariable( objectVarId, DbConstants.OBJECT_VAR, constraintIds,
                   parameterNames, tokenId,
                   new PwEnumeratedDomainImpl( "predicate"), partialPlan, planSequence,
                   stepNum, idSource);
      Integer paramVarId = new Integer( idSource.incEntityIdInt());
      String paramVarIds = String.valueOf( paramVarId) + ":";
      parameterNames = new ArrayList(); parameterNames.add( "param1");
      addVariable( paramVarId, DbConstants.PARAMETER_VAR, constraintIds,
                   parameterNames, tokenId,
                   new PwEnumeratedDomainImpl( "parameter"), partialPlan, planSequence,
                   stepNum, idSource);
      if ((tokenType == SLOTTED_TOKEN) || (tokenType == FREE_TOKEN)) {
        String tokenInfo = "0"; // becomes slotIndex -- 0 means base token
        addToken( tokenId, isValueToken, slotId, "predicate" +
                  String.valueOf( tokenInt), startVarId, endVarId, durationVarId,
                  stateVarId, objectVarId, parentId, paramVarIds, tokenInfo,
                  partialPlan, planSequence, stepNum, previousTokenId, idSource, ruleId);
        List variablesList = new ArrayList();
        variablesList.add( startVarId); variablesList.add( endVarId);
        variablesList.add( durationVarId);
        Integer constraintId = new Integer( idSource.incEntityIdInt());
        addConstraint( "startEndDurationRelation", constraintId,
                       DbConstants.ATEMPORAL_CONSTRAINT_TYPE, variablesList, partialPlan,
                       planSequence, stepNum, idSource);
        startVar.addConstraint( constraintId);
        endVar.addConstraint( constraintId);
        durationVar.addConstraint( constraintId);
      } else if (tokenType == RESOURCE_TRANSACTION) {
        String transInfo = "5,10"; // quantityMin, quantityMax
        String ruleInstanceIds = null;
        addResourceTransaction( tokenId, isValueToken, "resource.change" +
                                String.valueOf( tokenInt), startVarId, endVarId,
                                durationVarId, stateVarId, objectVarId, parentId,
                                ruleInstanceIds, paramVarIds, transInfo, partialPlan,
                                planSequence, stepNum, idSource);
        // no constraints
      }
      time += timeIncrement;
      previousTokenId = tokenId;
      previousSlotId = slotId;
    }
    String [] stringArray = new String [2];
    stringArray[0] = tokenIds.toString();
    stringArray[1] = slotIds.toString();
    return stringArray;
  } // end addTokensToTimeline
 

  private static void addObject( final Integer id, final int objectType,
                                 final Integer parentId, final String name,
                                 final String componentIds, final String varIds,
                                 final String tokenIds,
                                 final PwPartialPlanImpl partialPlan,
                                 final PwPlanningSequenceImpl planSequence,
                                 final int stepNum, final IdSource idSource) {
    int memberValue = 0; boolean isFirst = true;
    StringBuffer variableIds = new StringBuffer();
    for (int i = 0; i < NUM_VARS_PER_OBJECT; i++) {
      List constraintIds = new ArrayList();
      List parameterNames = new ArrayList(); parameterNames.add( "member" + i);
      Integer variableId = new Integer( idSource.incEntityIdInt());
      if (isFirst) { isFirst = false; } else { variableIds.append( ","); }
      variableIds.append( variableId.toString());
      addVariable( variableId, DbConstants.MEMBER_VAR, constraintIds,
                   parameterNames, id,
                   new PwEnumeratedDomainImpl( String.valueOf( memberValue)),
                   partialPlan, planSequence, stepNum, idSource);
      memberValue += 10;
    }
    partialPlan.addObject( id,
                           new PwObjectImpl( id, objectType, parentId, name, componentIds,
                                             variableIds.toString(), tokenIds, partialPlan));
  } // end addObject

  private static PwTimelineImpl addTimeline( final Integer id, final int type,
                                             final Integer parentId, 
                                             final String name, final String childObjectIds,
                                             final String emptySlotInfo,
                                             final String varIds, final String tokenIds,
                                             final PwPartialPlanImpl partialPlan,
                                             final PwPlanningSequenceImpl planSequence,
                                             final int stepNum,
                                             final IdSource idSource) {
    int memberValue = 0; boolean isFirst = true;
    StringBuffer variableIds = new StringBuffer();
    for (int i = 0; i < NUM_VARS_PER_TIMELINE; i++) {
      List constraintIds = new ArrayList();
      List parameterNames = new ArrayList(); parameterNames.add( "member" + i);
      Integer variableId = new Integer( idSource.incEntityIdInt());
      if (isFirst) { isFirst = false; } else { variableIds.append( ","); }
      variableIds.append( variableId.toString());
      addVariable( variableId, DbConstants.MEMBER_VAR, constraintIds,
                   parameterNames, id,
                   new PwEnumeratedDomainImpl( String.valueOf( memberValue)),
                   partialPlan, planSequence, stepNum, idSource);
      memberValue += 10;
    }
    PwTimelineImpl timeline = new PwTimelineImpl( id, type, parentId, name, childObjectIds,
                                                  emptySlotInfo, variableIds.toString(),
                                                  tokenIds, partialPlan);
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
                                final String paramVarIds, final String tokenInfo,
                                final PwPartialPlanImpl partialPlan,
                                final PwPlanningSequenceImpl planSequence,
                                final int stepNum, final Integer previousTokenId,
                                final IdSource idSource, Integer ruleId) {
    Integer ruleInstanceId = null;
    if (previousTokenId != null) {
      ruleInstanceId = new Integer( idSource.incEntityIdInt());
    }
    new PwTokenImpl( id, isValueToken, slotId, predicateName,
                     startVarId, endVarId, durationVarId,
                     stateVarId, objectVarId, parentId,
                     ruleInstanceId, paramVarIds, tokenInfo, partialPlan);
    if (previousTokenId != null) {
      addRuleInstance( ruleInstanceId, ruleId, previousTokenId, String.valueOf( id),
                       partialPlan, planSequence, stepNum, idSource);
    }
//     addTransaction( DbConstants.TOKEN_CREATED, new Integer( idSource.incEntityIdInt()),
//                     DbConstants.SOURCE_UNKNOWN, id, new Integer( stepNum),
//                     partialPlan.getId(), null, planSequence);
  } // end addToken

  private static PwVariableImpl addVariable( final Integer id, final String type,
                                             final List constraintIds, final List parameterNames,
                                             final Integer parentId, final PwDomainImpl domain,
                                             final PwPartialPlanImpl partialPlan,
                                             final PwPlanningSequenceImpl planSequence,
                                             final int stepNum, final IdSource idSource) {
    //System.err.println( "PWSetupHelper:addVariable " + id + " type " + type + " parent " + parentId);
    PwVariableImpl variable = new PwVariableImpl( id, type, parentId, domain, partialPlan);
    variable.addParameter( (String) parameterNames.get( 0));

    partialPlan.addVariable( id, variable);
    String [] info = new String [] { type, "variable" + id.toString(), "" };
    if (type.equals( DbConstants.PARAMETER_VAR)) {
      info[2] = (String) parameterNames.get( 0);
    }
//     addTransaction( DbConstants.VARIABLE_CREATED, new Integer( idSource.incEntityIdInt()),
//                     DbConstants.SOURCE_UNKNOWN, id, new Integer( stepNum),
//                     partialPlan.getId(), info, planSequence);
    return variable;
  } // end addVariable

 private static void addRuleInstance( final Integer id, final Integer ruleId,
                                      final Integer masterId, final String slaveIds,
                                      final PwPartialPlanImpl partialPlan,
                                      final PwPlanningSequenceImpl planSequence,
                                      final int stepNum, final IdSource idSource) {
   StringBuffer ruleVarIds = new StringBuffer();
   int enumValue = 1;
   boolean isFirst = true;
   for (int i = 0; i < NUM_VARS_PER_RULE_INSTANCE; i++) {
     List constraintIds = new ArrayList();
     List parameterNames = new ArrayList(); parameterNames.add( "member");
     Integer ruleVarId = new Integer( idSource.incEntityIdInt());
     if (isFirst) { isFirst = false; } else { ruleVarIds.append( ","); }
     ruleVarIds.append( ruleVarId.toString());
     addVariable( ruleVarId, DbConstants.RULE_VAR, constraintIds,
                  parameterNames, id,
                  new PwEnumeratedDomainImpl( String.valueOf( enumValue)),
                  partialPlan, planSequence, stepNum, idSource);
     enumValue += 1;
   }

   partialPlan.addRuleInstance( id,
                                 new PwRuleInstanceImpl( id, ruleId, masterId, slaveIds,
                                                         ruleVarIds.toString(), partialPlan));
 } // end addRuleInstance

  private static PwConstraintImpl addConstraint( final String name, final Integer id,
                                                 final String type, final List variableIds,
                                                 final PwPartialPlanImpl partialPlan,
                                                 final PwPlanningSequenceImpl planSequence,
                                                 final int stepNum,
                                                 final IdSource idSource) {
    PwConstraintImpl constraint = new PwConstraintImpl( name, id, type, variableIds,
                                                        partialPlan);
    partialPlan.addConstraint( id, constraint);
    String [] info = new String [] { name };
//     addTransaction( DbConstants.CONSTRAINT_CREATED, new Integer( idSource.incEntityIdInt()),
//                     DbConstants.SOURCE_UNKNOWN, id, new Integer( stepNum),
//                     partialPlan.getId(), info, planSequence);
    return constraint;
  } // end addConstraint

//   private static void addTransaction( final String type, final Integer transactionId,
//                                       final String source, final Integer objectId,
//                                       final Integer stepNumber, final Long partialPlanId,
//                                       final String [] info,
//                                       final PwPlanningSequenceImpl planSequence) {
//     PwDBTransactionImpl transaction =
//       new PwDBTransactionImpl( type, transactionId, source, objectId, stepNumber,
//                                planSequence.getId(), partialPlanId);
//     planSequence.addTransaction( transaction);
//     if (info != null) {
//       transaction.setInfo( info);
//     }
//   } // end addTransaction

  private static void addResourceTransaction( final Integer id,
                                              final boolean isValueToken, 
                                              final String predName,
                                              final Integer startVarId, 
                                              final Integer endVarId,
                                              final Integer durationVarId,
                                              final Integer stateVarId,
                                              final Integer objectVarId,
                                              final Integer parentId,
                                              final String ruleInstanceIds,
                                              final String paramVarIds,
                                              final String transInfo, 
                                              final PwPartialPlanImpl partialPlan,
                                              final PwPlanningSequenceImpl planSequence,
                                              final int stepNum,
                                              final IdSource idSource) {
    Integer ruleInstanceId = null; //dummy value until this code is fixed
    PwResourceTransactionImpl resourceTransaction =
      new PwResourceTransactionImpl( id, isValueToken, predName, startVarId,
                                     endVarId, durationVarId, stateVarId,
                                     objectVarId, parentId, ruleInstanceId,
                                     paramVarIds, transInfo, partialPlan);
    partialPlan.addResourceTransaction( id, resourceTransaction);
//     addTransaction( DbConstants.TOKEN_CREATED, new Integer( idSource.incEntityIdInt()),
//                     DbConstants.SOURCE_UNKNOWN, id, new Integer( stepNum),
//                     partialPlan.getId(), null, planSequence);
  } // end addResourceTransaction

  private static void addResourceInstant( final Integer id,
                                          final Integer resourceId, 
                                          final int time, 
                                          final double levelMin,
                                          final double levelMax,
                                          final String transactions,
                                          final PwPartialPlanImpl partialPlan) {
    PwResourceInstantImpl resourceInstant =
      new PwResourceInstantImpl( id, resourceId, time, levelMin, levelMax, transactions,
                                 partialPlan);
    partialPlan.addResourceInstant( id, resourceInstant);
  } // end addResourceInstant

  private static void addDecision( final Integer id, final int type, Integer entityId,
                                   boolean unit, PwPartialPlanImpl partialPlan) {
    PwDecisionImpl decision =
      new PwDecisionImpl( id, type, entityId, unit, partialPlan);
    decisionsForStep.add( decision);
  } // end addDecision

  private static void addRule( final Integer id, final String blob, 
                               PwPlanningSequenceImpl planSequence) {
    PwRuleImpl rule = 
      new PwRuleImpl( planSequence.getId(), id, blob);
    rulesForSequence.add(rule);
    //System.err.println( "PWSetupHelper.buildTestData: addRule " + planSequence.getId() +
    // " " + id + " " + blob);
  } // end addRule

} // end class PWSetupHelper
