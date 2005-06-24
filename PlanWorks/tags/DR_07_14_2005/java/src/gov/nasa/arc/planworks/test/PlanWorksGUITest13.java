//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest13.java,v 1.1 2004-10-13 23:49:20 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPopupMenu;

import junit.extensions.jfcunit.JFCTestHelper;
import junit.framework.Assert;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwTokenQuery;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableQuery;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.DBTransactionQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.StepQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.TokenQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.VariableQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;


public class PlanWorksGUITest13 {

  private PlanWorksGUITest13() {
  }

  public static void planViz13( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
                                PlanWorksGUITest guiTest) throws Exception {
    int stepNumber = 1, seqUrlIndex = 4;
    Integer constraintId = new Integer( 1289);
    Integer variableId = new Integer( 1248);
    Integer tokenId =  new Integer( 1253);
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( seqUrlIndex));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, helper, guiTest,
                                planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    PWTestHelper.addSequencesToProject( sequenceFileArray, helper, guiTest, planWorks);

    ViewListener viewListener01 = new PlanWorksGUITest.ViewListenerWait01( guiTest);
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME, viewListener01,
                                        helper, guiTest);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME, helper, guiTest);

    SequenceQueryWindow seqQueryWindow =
      PWTestHelper.getSequenceQueryWindow( PWTestHelper.SEQUENCE_NAME, helper, guiTest);

    PwPlanningSequence planSeq =
      planWorks.getCurrentProject().getPlanningSequence( (String) sequenceUrls.get( seqUrlIndex));
    PwPartialPlan partialPlanStep0 = planSeq.getPartialPlan( 0);
    PwPartialPlan partialPlanStep1 = planSeq.getPartialPlan( stepNumber);

//     System.err.println( "Method 3 -------------");

    // Steps -> Constraints
    int queryResultsCnt = 1;
    int constraintAllCnt = 0, variableAllCnt = 0, tokenAllCnt = 0;
    String fieldName = "Key";
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_STEPS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.STEPS_WHERE_CONSTRAINT_TRANSACTED,
                                     helper, guiTest);
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName,
                                        constraintId.toString(), helper, guiTest);
     
    Iterator typesItr = SequenceQueryWindow.CONSTRAINT_TRANSACTION_TYPES.iterator();
    while (typesItr.hasNext()) {
      String transType = (String) typesItr.next();
      System.err.println( "   queryResultsCnt " + queryResultsCnt + " transType" + transType);
      PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                       SequenceQueryWindow.ConstraintTransComboBox.class,
                                       transType, helper, guiTest);
      viewListener01.reset();
      PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                       SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
      viewListener01.viewWait();
      checkStepConstraintResults( constraintId, partialPlanStep0, partialPlanStep1,
                              queryResultsCnt, helper, guiTest);
      queryResultsCnt++;
    }
    constraintAllCnt = queryResultsCnt;
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.ConstraintTransComboBox.class,
                                     SequenceQueryWindow.CONSTRAINT_TRANSACTION_TYPE_ALL,
                                     helper, guiTest);
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName, "", helper, guiTest);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    checkStepConstraintResults( new Integer( 0), partialPlanStep0, partialPlanStep1,
                            queryResultsCnt, helper, guiTest);
    queryResultsCnt++;

    // Steps -> Variables
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_STEPS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.STEPS_WHERE_VARIABLE_TRANSACTED,
                                     helper, guiTest);
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName,
                                        variableId.toString(), helper, guiTest);
     
    typesItr = SequenceQueryWindow.VARIABLE_TRANSACTION_TYPES.iterator();
    while (typesItr.hasNext()) {
      String transType = (String) typesItr.next();
      System.err.println( "   queryResultsCnt " + queryResultsCnt + " transType" + transType);
      PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                       SequenceQueryWindow.VariableTransComboBox.class,
                                       transType, helper, guiTest);
      viewListener01.reset();
      PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                       SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
      viewListener01.viewWait();
      checkStepVariableResults( variableId, partialPlanStep0, partialPlanStep1,
                              queryResultsCnt, helper, guiTest);
      queryResultsCnt++;
    }
    variableAllCnt = queryResultsCnt;
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.VariableTransComboBox.class,
                                     SequenceQueryWindow.VARIABLE_TRANSACTION_TYPE_ALL,
                                     helper, guiTest);
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName, "", helper, guiTest);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    checkStepVariableResults( new Integer( 0), partialPlanStep0, partialPlanStep1,
                            queryResultsCnt, helper, guiTest);
    queryResultsCnt++;

    // Steps -> Tokens
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_STEPS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.STEPS_WHERE_TOKEN_TRANSACTED,
                                     helper, guiTest);
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName,
                                        tokenId.toString(), helper, guiTest);
     
    typesItr = SequenceQueryWindow.TOKEN_TRANSACTION_TYPES.iterator();
    while (typesItr.hasNext()) {
      String transType = (String) typesItr.next();
      System.err.println( "   queryResultsCnt " + queryResultsCnt + " transType" + transType);
      PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                       SequenceQueryWindow.TokenTransComboBox.class,
                                       transType, helper, guiTest);
      viewListener01.reset();
      PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                       SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
      viewListener01.viewWait();
      checkStepTokenResults( tokenId, partialPlanStep0, partialPlanStep1,
                              queryResultsCnt, helper, guiTest);
      queryResultsCnt++;
    }
    tokenAllCnt = queryResultsCnt;
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.TokenTransComboBox.class,
                                     SequenceQueryWindow.TOKEN_TRANSACTION_TYPE_ALL,
                                     helper, guiTest);
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName, "", helper, guiTest);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    checkStepTokenResults( new Integer( 0), partialPlanStep0, partialPlanStep1,
                            queryResultsCnt, helper, guiTest);
    queryResultsCnt++;

    System.err.println( "Method 4 -------------");

    StepQueryView stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          constraintAllCnt , helper, guiTest);
    ViewGenerics.raiseFrame( stepQueryResults.getViewFrame());
    stepQueryResults.getHeaderJGoView().doBackgroundClick( MouseEvent.BUTTON3_MASK,
                                                           new Point( 0, 0), new Point( 0, 0));
    guiTest.flushAWT(); guiTest.awtSleep();
    String viewMenuItemName = "Find Transaction by " + ViewConstants.QUERY_CONSTRAINT_KEY_HEADER;
    PWTestHelper.selectViewMenuItem( stepQueryResults, viewMenuItemName, helper, guiTest);
    PWTestHelper.handleDialogValueEntry( viewMenuItemName, constraintId.toString(),
                                         helper, guiTest);

    stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          variableAllCnt , helper, guiTest);
    ViewGenerics.raiseFrame( stepQueryResults.getViewFrame());
    stepQueryResults.getHeaderJGoView().doBackgroundClick( MouseEvent.BUTTON3_MASK,
                                                           new Point( 0, 0), new Point( 0, 0));
    guiTest.flushAWT(); guiTest.awtSleep();
    viewMenuItemName = "Find Transaction by " + ViewConstants.QUERY_VARIABLE_KEY_HEADER;
    PWTestHelper.selectViewMenuItem( stepQueryResults, viewMenuItemName, helper, guiTest);
    PWTestHelper.handleDialogValueEntry( viewMenuItemName, variableId.toString(),
                                         helper, guiTest);

    stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          tokenAllCnt , helper, guiTest);
    ViewGenerics.raiseFrame( stepQueryResults.getViewFrame());
    stepQueryResults.getHeaderJGoView().doBackgroundClick( MouseEvent.BUTTON3_MASK,
                                                           new Point( 0, 0), new Point( 0, 0));
    guiTest.flushAWT(); guiTest.awtSleep();
    viewMenuItemName = "Find Transaction by " + ViewConstants.QUERY_TOKEN_KEY_HEADER;
    PWTestHelper.selectViewMenuItem( stepQueryResults, viewMenuItemName, helper, guiTest);
    PWTestHelper.handleDialogValueEntry( viewMenuItemName, tokenId.toString(),
                                         helper, guiTest);

    System.err.println( "Method 5 -------------");

    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_STEPS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.STEPS_WITH_NON_UNIT_VARIABLE_DECISIONS,
                                     helper, guiTest);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    guiTest.assertNotNullVerbose( SequenceQueryWindow.STEPS_WITH_NON_UNIT_VARIABLE_DECISIONS +
                                  " not found", stepQueryResults, "not ");
   
    queryResultsCnt++;

    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_STEPS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.STEPS_WITH_RESTRICTIONS,
                                     helper, guiTest);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    guiTest.assertNotNullVerbose( SequenceQueryWindow.STEPS_WITH_RESTRICTIONS +
                                  " not found", stepQueryResults, "not ");
    queryResultsCnt++;

    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_STEPS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.STEPS_WITH_UNIT_VARIABLE_DECISIONS,
                                     helper, guiTest);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    guiTest.assertNotNullVerbose( SequenceQueryWindow.STEPS_WITH_UNIT_VARIABLE_DECISIONS+
                                  " not found", stepQueryResults, "not ");
    queryResultsCnt++;

    System.err.println( "Method 6 -------------");

    // Transactions->Constraints
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_TRANSACTIONS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.TRANSACTIONS_FOR_CONSTRAINT,
                                     helper, guiTest);
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName,
                                        constraintId.toString(), helper, guiTest);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    checkTransConstraintResults( partialPlanStep0, partialPlanStep1, queryResultsCnt,
                                 helper, guiTest);
    queryResultsCnt++;


    // Transactions -> Variables
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_TRANSACTIONS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.TRANSACTIONS_FOR_VARIABLE,
                                     helper, guiTest);
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName,
                                        variableId.toString(), helper, guiTest);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    checkTransVariableResults( partialPlanStep0, partialPlanStep1, queryResultsCnt,
                               helper, guiTest);
    queryResultsCnt++;


    // Transactions -> Tokens
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_TRANSACTIONS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.TRANSACTIONS_FOR_TOKEN,
                                     helper, guiTest);
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName,
                                        tokenId.toString(), helper, guiTest);
     
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    checkTransTokenResults( partialPlanStep0, partialPlanStep1, queryResultsCnt,
                            helper, guiTest);
    queryResultsCnt++;

    // TRANSACTIONS_IN_RANGE
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MajorTypeComboBox.class,
                                     SequenceQueryWindow.QUERY_FOR_TRANSACTIONS,
                                     helper, guiTest);
    PWTestHelper.selectComboBoxItem( seqQueryWindow,
                                     SequenceQueryWindow.MinorTypeComboBox.class,
                                     SequenceQueryWindow.TRANSACTIONS_IN_RANGE,
                                     helper, guiTest);
    fieldName = "StartStep";
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName, "0", helper, guiTest);
    fieldName = "EndStep";
    PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName, "1", helper, guiTest);
    viewListener01.reset();
    PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
                                     SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
    viewListener01.viewWait();
    checkTransResults( partialPlanStep0, partialPlanStep1, queryResultsCnt, helper, guiTest);
 
    System.err.println( "Method 7 -------------");

    DBTransactionQueryView transQueryResults =
      (DBTransactionQueryView) PWTestHelper.getQueryResultsWindow( DBTransactionQueryView.class,
                                                                   PWTestHelper.SEQUENCE_NAME,
                                                                   queryResultsCnt, helper,
                                                                   guiTest);
    transQueryResults.getHeaderJGoView().doBackgroundClick( MouseEvent.BUTTON3_MASK,
                                                           new Point( 0, 0), new Point( 0, 0));
    guiTest.flushAWT(); guiTest.awtSleep();
    viewMenuItemName = "Find Transaction by " + ViewConstants.DB_TRANSACTION_ENTITY_KEY_HEADER;
    PWTestHelper.selectViewMenuItem( transQueryResults, viewMenuItemName, helper, guiTest);
    PWTestHelper.handleDialogValueEntry( viewMenuItemName, tokenId.toString(),
                                         helper, guiTest);
    queryResultsCnt++;

    // somehow the free tokend selection does not take and the step number
    // does not either, resulting in the Apply throwing an error

//     System.err.println( "Method 8 -------------");

//     PWTestHelper.selectComboBoxItem( seqQueryWindow,
//                                      SequenceQueryWindow.MajorTypeComboBox.class,
//                                      SequenceQueryWindow.QUERY_FOR_FREE_TOKENS,
//                                      helper, guiTest);
//     fieldName = "Step";
//     PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName, String.valueOf( stepNumber),
//                                         helper, guiTest);
//     viewListener01.reset();
//     PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
//                                      SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
//     viewListener01.viewWait();
//     checkFreeTokenResults( queryResultsCnt, helper, guiTest);

//     System.err.println( "Method 9 -------------");

//     TokenQueryView freeTokenQueryResults =
//       (TokenQueryView) PWTestHelper.getQueryResultsWindow( TokenQueryView.class,
//                                                            PWTestHelper.SEQUENCE_NAME,
//                                                            queryResultsCnt, helper, guiTest);
//     freeTokenQueryResults.getHeaderJGoView().doBackgroundClick
//       ( MouseEvent.BUTTON3_MASK, new Point( 0, 0), new Point( 0, 0));
//     guiTest.flushAWT(); guiTest.awtSleep();
//     viewMenuItemName = "Find Token by " + ViewConstants.QUERY_TOKEN_KEY_HEADER;
//     PWTestHelper.selectViewMenuItem( freeTokenQueryResults, viewMenuItemName, helper, guiTest);
//     PWTestHelper.handleDialogValueEntry
//       ( viewMenuItemName, ((PwToken) partialPlanStep1.getFreeTokenList().get(6)).
//         getId().toString(), helper, guiTest);
//     queryResultsCnt++;

    // try{Thread.sleep(1000);}catch(Exception e){}

    // somehow the unbound variable selection does not take and the step number
    // does not either, resulting in the Apply throwing an error

//     System.err.println( "Method 10 -------------");

//     PWTestHelper.selectComboBoxItem( seqQueryWindow,
//                                      SequenceQueryWindow.MajorTypeComboBox.class,
//                                      SequenceQueryWindow.QUERY_FOR_UNBOUND_VARIABLES,
//                                      helper, guiTest);
//     fieldName = "Step";
//     PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName, String.valueOf( stepNumber),
//                                         helper, guiTest);
//     viewListener01.reset();
//     PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
//                                      SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
//     viewListener01.viewWait();
//     checkVariableResults( queryResultsCnt, helper, guiTest);

//     System.err.println( "Method 11 -------------");

//     VariableQueryView unboundVariableQueryResults =
//       (VariableQueryView) PWTestHelper.getQueryResultsWindow( VariableQueryView.class,
//                                                               PWTestHelper.SEQUENCE_NAME,
//                                                               queryResultsCnt, helper, guiTest);
//     unboundVariableQueryResults.getHeaderJGoView().doBackgroundClick
//       ( MouseEvent.BUTTON3_MASK, new Point( 0, 0), new Point( 0, 0));
//     guiTest.flushAWT(); guiTest.awtSleep();
//     viewMenuItemName = "Find Variable by " + ViewConstants.QUERY_VARIABLE_KEY_HEADER;
//     PWTestHelper.selectViewMenuItem( unboundVariableQueryResults, viewMenuItemName,
//                                      helper, guiTest);
//     PWTestHelper.handleDialogValueEntry
//       ( viewMenuItemName, ((PwVariable) partialPlanStep1.getVariableList().get(6)).
//         getId().toString(), helper, guiTest);
//     queryResultsCnt++;





//     System.err.println( "Method 12 -------------");
//     System.err.println( "Method 13 -------------");
//     System.err.println( "Method 14 -------------");
//     System.err.println( "Method 15 -------------");

    // try{Thread.sleep(40000);}catch(Exception e){}

    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);

    System.err.println( "\nPLANVIZ_13 COMPLETED\n");
  } // end planViz13

  private static void checkStepConstraintResults( Integer constraintId,
                                              PwPartialPlan partialPlanStep0,
                                              PwPartialPlan partialPlanStep1,
                                              int queryResultsCnt, JFCTestHelper helper,
                                              PlanWorksGUITest guiTest)
    throws Exception {
    StepQueryView stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    List stepList = stepQueryResults.getStepList();
    for (int row = 0, nRows = stepList.size(); row < nRows; row++) {
      PwDBTransaction transaction = (PwDBTransaction) stepList.get( row);
      if (! constraintId.equals( new Integer( 0))) {
        guiTest.assertTrueVerbose( "QueryResults entry not found for constraintId=" +
                                   constraintId.toString(),
                                   transaction.getEntityId().toString().equals
                                   ( constraintId.toString()), "not ");
      } else {
        guiTest.assertTrue( "QueryResults entry " + row + " not a constraint",
                            ((partialPlanStep0.getConstraint( transaction.getEntityId())
                              != null) ||
                             (partialPlanStep1.getConstraint( transaction.getEntityId())
                              != null)));
      }
    }
  } // end checkStepConstraintResults

  private static void checkStepVariableResults( Integer variableId,
                                            PwPartialPlan partialPlanStep0,
                                            PwPartialPlan partialPlanStep1,
                                            int queryResultsCnt, JFCTestHelper helper,
                                            PlanWorksGUITest guiTest)
    throws Exception {
    StepQueryView stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    List stepList = stepQueryResults.getStepList();
    for (int row = 0, nRows = stepList.size(); row < nRows; row++) {
      PwDBTransaction transaction = (PwDBTransaction) stepList.get( row);
      if (! variableId.equals( new Integer( 0))) {
        guiTest.assertTrueVerbose( "QueryResults entry not found for variableId=" +
                                   variableId.toString(),
                                   transaction.getEntityId().toString().equals
                                   ( variableId.toString()), "not ");
      } else {
        guiTest.assertTrue( "QueryResults entry " + row + " not a variable",
                            ((partialPlanStep0.getVariable( transaction.getEntityId())
                              != null) ||
                             (partialPlanStep1.getVariable( transaction.getEntityId())
                              != null)));
      }
    }
  } // end checkStepVariableResults

  private static void checkStepTokenResults( Integer tokenId,
                                         PwPartialPlan partialPlanStep0,
                                         PwPartialPlan partialPlanStep1,
                                         int queryResultsCnt, JFCTestHelper helper,
                                         PlanWorksGUITest guiTest)
    throws Exception {
    StepQueryView stepQueryResults =
      (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    List stepList = stepQueryResults.getStepList();
    for (int row = 0, nRows = stepList.size(); row < nRows; row++) {
      PwDBTransaction transaction = (PwDBTransaction) stepList.get( row);
      if (! tokenId.equals( new Integer( 0))) {
        guiTest.assertTrueVerbose( "QueryResults entry not found for tokenId=" +
                                   tokenId.toString(),
                                   transaction.getEntityId().toString().equals
                                   ( tokenId.toString()), "not ");
      } else {
        guiTest.assertTrue( "QueryResults entry " + row + " not a token",
                            ((partialPlanStep0.getToken( transaction.getEntityId())
                              != null) ||
                             (partialPlanStep1.getToken( transaction.getEntityId())
                              != null)));
      }
    }
  } // end checkStepTokenResults

  private static void checkTransConstraintResults( PwPartialPlan partialPlanStep0,
                                                   PwPartialPlan partialPlanStep1,
                                                   int queryResultsCnt, JFCTestHelper helper,
                                                   PlanWorksGUITest guiTest)
    throws Exception {
    DBTransactionQueryView transQueryResults =
      (DBTransactionQueryView) PWTestHelper.getQueryResultsWindow( DBTransactionQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    List transList = transQueryResults.getDbTransactionList();
    for (int row = 0, nRows = transList.size(); row < nRows; row++) {
      PwDBTransaction transaction = (PwDBTransaction) transList.get( row);
      guiTest.assertTrueVerbose( "QueryResults entry not a constraint transaction",
                                 (transaction.getName().indexOf( "CONSTRAINT") >= 0), "not ");
    }
  } // end checkTransConstraintResults

  private static void checkTransVariableResults( PwPartialPlan partialPlanStep0,
                                                 PwPartialPlan partialPlanStep1,
                                                 int queryResultsCnt, JFCTestHelper helper,
                                                 PlanWorksGUITest guiTest)
    throws Exception {
    DBTransactionQueryView transQueryResults =
      (DBTransactionQueryView) PWTestHelper.getQueryResultsWindow( DBTransactionQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    List transList = transQueryResults.getDbTransactionList();
    for (int row = 0, nRows = transList.size(); row < nRows; row++) {
      PwDBTransaction transaction = (PwDBTransaction) transList.get( row);
      guiTest.assertTrueVerbose( "QueryResults entry not a variable transaction",
                                 (transaction.getName().indexOf( "VARIABLE") >= 0), "not ");
    }
  } // end checkTransVariableResults

  private static void checkTransTokenResults( PwPartialPlan partialPlanStep0,
                                              PwPartialPlan partialPlanStep1,
                                              int queryResultsCnt, JFCTestHelper helper,
                                              PlanWorksGUITest guiTest)
    throws Exception {
    DBTransactionQueryView transQueryResults =
      (DBTransactionQueryView) PWTestHelper.getQueryResultsWindow( DBTransactionQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    List transList = transQueryResults.getDbTransactionList();
    for (int row = 0, nRows = transList.size(); row < nRows; row++) {
      PwDBTransaction transaction = (PwDBTransaction) transList.get( row);
      guiTest.assertTrueVerbose( "QueryResults entry not a token transaction",
                                 (transaction.getName().indexOf( "TOKEN") >= 0), "not ");
    }
  } // end checkTransTokenResults

  private static void checkTransResults( PwPartialPlan partialPlanStep0,
                                         PwPartialPlan partialPlanStep1,
                                         int queryResultsCnt, JFCTestHelper helper,
                                         PlanWorksGUITest guiTest)
    throws Exception {
    DBTransactionQueryView transQueryResults =
      (DBTransactionQueryView) PWTestHelper.getQueryResultsWindow( DBTransactionQueryView.class,
                                                          PWTestHelper.SEQUENCE_NAME,
                                                          queryResultsCnt, helper, guiTest);
    List transList = transQueryResults.getDbTransactionList();
    for (int row = 0, nRows = transList.size(); row < nRows; row++) {
      PwDBTransaction transaction = (PwDBTransaction) transList.get( row);
      // System.err.println( " transaction.getName() " + transaction.getName());
      guiTest.assertTrue( "QueryResults entry not a token transaction: row " + row,
                          ((transaction.getName().indexOf( "TOKEN") >= 0) ||
                           (transaction.getName().indexOf( "VARIABLE") >= 0) ||
                           (transaction.getName().indexOf( "CONSTRAINT") >= 0) ||
                           (transaction.getName().indexOf( "ASSIGN") >= 0)));
    }
  } // end checkTransResults

  private static void checkFreeTokenResults( int queryResultsCnt, JFCTestHelper helper,
                                             PlanWorksGUITest guiTest)
    throws Exception {
    TokenQueryView freeTokenQueryResults =
      (TokenQueryView) PWTestHelper.getQueryResultsWindow( TokenQueryView.class,
                                                           PWTestHelper.SEQUENCE_NAME,
                                                           queryResultsCnt, helper, guiTest);
    List freeTokenList = freeTokenQueryResults.getFreeTokenList();
    for (int row = 0, nRows = freeTokenList.size(); row < nRows; row++) {
      PwTokenQuery freeTokenQuery = (PwTokenQuery) freeTokenList.get( row);
      guiTest.assertTrue( "QueryResults entry not a freeToken: row " + row,
                          freeTokenQuery.isFreeToken());

    }
  } // end checkFreeTokenResults

  private static void checkVariableResults( int queryResultsCnt, JFCTestHelper helper,
                                            PlanWorksGUITest guiTest)
    throws Exception {
    VariableQueryView unboundVariableQueryResults =
      (VariableQueryView) PWTestHelper.getQueryResultsWindow( VariableQueryView.class,
                                                           PWTestHelper.SEQUENCE_NAME,
                                                           queryResultsCnt, helper, guiTest);
    List unboundVariableList = unboundVariableQueryResults.getVariableList();
    for (int row = 0, nRows = unboundVariableList.size(); row < nRows; row++) {
      PwVariableQuery unboundVariableQuery = (PwVariableQuery) unboundVariableList.get( row);
      guiTest.assertTrue( "QueryResults entry not a unbound variable: row " + row,
                          unboundVariableQuery.isUnbound());

    }
  } // end checkVariableResults




} // end class PlanWorksGUITest13
