//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest11.java,v 1.1 2004-10-01 20:04:33 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.eventdata.JTableHeaderMouseEventData;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.ThreadListener;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;    
import gov.nasa.arc.planworks.viz.partialPlan.decision.DecisionView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.rule.RuleInstanceView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.SlotNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineViewTimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.util.DBTransactionTable;
import gov.nasa.arc.planworks.viz.util.TableSorter;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

// planViz11 methods 1-16 of 21 complete

public class PlanWorksGUITest11 {

  private PlanWorksGUITest11() {
  }

  public static void planViz11( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
                                PlanWorksGUITest guiTest) throws Exception {
    int stepNumber = 1, seqUrlIndex = 4;
    List viewList = new ArrayList();
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

    List viewListenerList = guiTest.createViewListenerList();
    String viewMenuItemName = "Open All Views";
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, guiTest);
   guiTest.viewListenerListWait( PlanWorksGUITest.CONSTRAINT_NETWORK_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.DB_TRANSACTION_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.DECISION_VIEW_INDEX, viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.RESOURCE_PROFILE_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.RESOURCE_TRANSACTION_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.TIMELINE_VIEW_INDEX, viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
   String viewNameSuffix = PWTestHelper.SEQUENCE_NAME +
     System.getProperty( "file.separator") + "step" + String.valueOf( stepNumber);
   PwPlanningSequence planSeq =
     planWorks.getCurrentProject().getPlanningSequence( (String) sequenceUrls.get( seqUrlIndex));
   PwPartialPlan partialPlan = planSeq.getPartialPlan( stepNumber);

   Integer timelineId = ((PwTimeline) partialPlan.getTimelineList().get( 0)).getId();
   Integer slottedTokenId = ((PwToken) partialPlan.getSlottedTokenList().get( 0)).getId();
   Integer freeTokenId = ((PwToken) partialPlan.getFreeTokenList().get( 0)).getId();
   guiTest.assertNotNullVerbose( "Did not find timeline id in partial plan",
                         timelineId, "not ");
   guiTest.assertNotNullVerbose( "Did not find slotted token id in partial plan",
                         slottedTokenId, "not ");
   guiTest.assertNotNullVerbose( "Did not find free token id in partial plan",
                         freeTokenId, "not ");
   Integer slotId = ((PwSlot) partialPlan.getSlotList().get( 0)).getId();
   guiTest.assertNotNullVerbose( "Did not find slot id in partial plan", slotId, "not ");
   Integer variableId = null;
   Iterator variableItr = partialPlan.getVariableList().iterator();
   while (variableItr.hasNext()) {
     PwVariable variable = (PwVariable) variableItr.next();
     if (variable.getParent() instanceof PwToken) {
       variableId = variable.getId();
       break;
     }
   }
   guiTest.assertNotNullVerbose( "Did not find variable id in partial plan", variableId, "not ");
   Integer constraintId = ((PwConstraint) partialPlan.getConstraintList().get( 0)).getId();
   guiTest.assertNotNullVerbose( "Did not find constraint id in partial plan", constraintId, "not ");
   Integer ruleInstanceId = ((PwRuleInstance) partialPlan.getRuleInstanceList().get( 0)).getId();
   guiTest.assertNotNullVerbose( "Did not find rule instance id in partial plan",
                         ruleInstanceId, "not ");
   ConstraintNetworkView constraintNetworkView =
     (ConstraintNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.CONSTRAINT_NETWORK_VIEW, viewNameSuffix, guiTest);
   TemporalExtentView temporalExtentView =
     (TemporalExtentView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TEMPORAL_EXTENT_VIEW, viewNameSuffix, guiTest);
   TimelineView timelineView = (TimelineView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, guiTest);
   TokenNetworkView tokenNetworkView = (TokenNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TOKEN_NETWORK_VIEW, viewNameSuffix, guiTest);

   System.err.println( "Method 2 -------------");
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slottedTokenId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Constraint Network focus node id is not " + slottedTokenId.toString() +
                      " (slotted token)",
                      constraintNetworkView.getFocusNodeId().equals( slottedTokenId), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slottedTokenId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Temporal Extent focus node id is not " + slottedTokenId.toString() +
                      " (slotted token)",
                      temporalExtentView.getFocusNodeId().equals( slottedTokenId), "not ");

   ViewGenerics.raiseFrame( timelineView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( timelineView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slottedTokenId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Timeline focus node id is not " + slottedTokenId.toString() +
                      " (slotted token)",
                      timelineView.getFocusNodeId().equals( slottedTokenId), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slottedTokenId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Token Network focus node id is not " + slottedTokenId.toString() +
                      " (slotted token)",
                      tokenNetworkView.getFocusNodeId().equals( slottedTokenId), "not ");

   System.err.println( "Method 3 -------------");
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", freeTokenId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Constraint Network focus node id is not " + freeTokenId.toString() +
                      " (free token)",
                      constraintNetworkView.getFocusNodeId().equals( freeTokenId), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", freeTokenId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Temporal Extent focus node id is not " + freeTokenId.toString() +
                      " (free token)",
                      temporalExtentView.getFocusNodeId().equals( freeTokenId), "not ");

   ViewGenerics.raiseFrame( timelineView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( timelineView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", freeTokenId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Timeline focus node id is not " + freeTokenId.toString() +
                      " (free token)",
                      timelineView.getFocusNodeId().equals( freeTokenId), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   tokenNetworkView.setDisableEntityKeyPathDialog();
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", freeTokenId.toString(),
                                         helper, guiTest);
   List findPathList = tokenNetworkView.getHighlightPathNodesList();
   TokenNetworkTokenNode foundNode =
     (TokenNetworkTokenNode) findPathList.get( findPathList.size() - 1);
   guiTest.assertTrueVerbose( "Token Network focus node id is not " + freeTokenId.toString() +
                      " (free token)", foundNode.getId().equals( freeTokenId), "not ");

   ViewGenerics.raiseFrame(timelineView .getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( timelineView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", slotId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Timeline focus node id is not " + slotId.toString() + " (slot)",
                      timelineView.getFocusNodeId().equals( slotId), "not ");

   PWTestHelper.viewBackgroundItemSelection( timelineView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", timelineId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Timeline focus node id is not " + timelineId.toString() + " (timeline)",
                      timelineView.getFocusNodeId().equals( timelineId), "not ");

   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", variableId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Constraint Network focus node id is not " + variableId.toString() +
                      " (variable)",
                      constraintNetworkView.getFocusNodeId().equals( variableId), "not ");

   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", constraintId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Constraint Network focus node id is not " + constraintId.toString() +
                      " (constraint) - variableNode ! inLayout",
                      constraintNetworkView.getFocusNodeId().equals( constraintId), "not ");
   ConstraintNode constraintNode = (ConstraintNode) constraintNetworkView.getFocusNode();
   VariableNode variableNode = null;
   Iterator varItr = constraintNode.getVariableNodes().iterator();
   while (varItr.hasNext()) {
     VariableNode varNode = (VariableNode)  varItr.next();
     if (varNode.inLayout()) {
       variableNode = varNode;
       break;
     }
   }
   guiTest.assertNotNullVerbose( "Constraint Network in-layout variable node of constraint node " +
                         "is not found", variableNode, "not ");
   variableNode.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
   guiTest.flushAWT(); guiTest.awtSleep();
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", constraintId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Constraint Network focus node id is not " + constraintId.toString() +
                      " (constraint) - variableNode inLayout",
                      constraintNetworkView.getFocusNodeId().equals( constraintId), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, "Find by Key", helper,
                                             guiTest);
   PWTestHelper.handleDialogValueEntry( "Find by Key", ruleInstanceId.toString(),
                                         helper, guiTest);
   guiTest.assertTrueVerbose( "Token Network focus node id is not " + ruleInstanceId.toString() +
                      " (rule instance)",
                      tokenNetworkView.getFocusNodeId().equals( ruleInstanceId), "not ");

   System.err.println( "Method 4 -------------");
   TokenNetworkTokenNode slottedTokenNode = null;
   Iterator nodeKeyItr = tokenNetworkView.getTokenNodeKeyList().iterator();
   while (nodeKeyItr.hasNext()) {
     TokenNetworkTokenNode tokenNode =
       tokenNetworkView.getTokenNode( (Integer) nodeKeyItr.next());
     if (tokenNode.getToken().isSlotted()) {
       slottedTokenNode = tokenNode;
       break;
     }
   }
   slottedTokenNode.doMouseClick( MouseEvent.BUTTON3_MASK, slottedTokenNode.getLocation(),
                                  new Point( 0, 0), tokenNetworkView.getJGoView());
   PWTestHelper.selectViewMenuItem( tokenNetworkView, "Set Active Token", helper, guiTest);
   PwToken activeToken = ((PartialPlanViewSet) tokenNetworkView.getViewSet()).getActiveToken();
   guiTest.assertTrueVerbose( "Slotted token node (id=" + slottedTokenNode.getToken().getId() +
                      ") is not set to active token",
                      activeToken.getId().equals( slottedTokenNode.getToken().getId()), "not ");

   System.err.println( "Method 5 -------------");
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   Integer activeTokenId = activeToken.getId();
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Snap to Active Token",
                                             helper, guiTest);
   guiTest.assertTrueVerbose( "Constraint Network focus node id is not " +
                      activeTokenId.toString() + " (slotted token)",
                      constraintNetworkView.getFocusNodeId().equals( activeTokenId), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Snap to Active Token",
                                             helper, guiTest);
   guiTest.assertTrueVerbose( "Temporal Extent focus node id is not " +
                      activeTokenId.toString() + " (slotted token)",
                      temporalExtentView.getFocusNodeId().equals( activeTokenId), "not ");

   ViewGenerics.raiseFrame( timelineView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( timelineView, "Snap to Active Token",
                                             helper, guiTest);
   guiTest.assertTrueVerbose( "Timeline focus node id is not " +
                      activeTokenId.toString() + " (slotted token)",
                      timelineView.getFocusNodeId().equals( activeTokenId), "not ");

   System.err.println( "Method 6 -------------");
   TokenNode freeTokenNode = null;
   Iterator nodeItr = timelineView.getFreeTokenNodeList().iterator();
   while (nodeItr.hasNext()) {
     TokenNode tokenNode = (TokenNode) nodeItr.next();
     if (tokenNode.getToken().isFree()) {
       freeTokenNode = tokenNode;
       break;
     }
   }
   freeTokenNode.doMouseClick( MouseEvent.BUTTON3_MASK, freeTokenNode.getLocation(),
                                  new Point( 0, 0), timelineView.getJGoView());
   PWTestHelper.selectViewMenuItem( timelineView, "Set Active Token", helper, guiTest);
   activeToken = ((PartialPlanViewSet) timelineView.getViewSet()).getActiveToken();
   guiTest.assertTrueVerbose( "Free token node (id=" + freeTokenNode.getToken().getId() +
                      ") is not set to active token",
                      activeToken.getId().equals( freeTokenNode.getToken().getId()), "not ");

   System.err.println( "Method 7 -------------");
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   activeTokenId = activeToken.getId();
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView, "Snap to Active Token",
                                             helper, guiTest);
   guiTest.assertTrueVerbose( "Constraint Network focus node id is not " +
                      activeTokenId.toString() + " (free token)",
                      constraintNetworkView.getFocusNodeId().equals( activeTokenId), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Snap to Active Token",
                                             helper, guiTest);
   guiTest.assertTrueVerbose( "Temporal Extent focus node id is not " +
                      activeTokenId.toString() + " (free token)",
                      temporalExtentView.getFocusNodeId().equals( activeTokenId), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, "Snap to Active Token",
                                             helper, guiTest);
   guiTest.assertTrueVerbose( "TokenNetwork focus node id is not " +
                      activeTokenId.toString() + " (free token)",
                      tokenNetworkView.getFocusNodeId().equals( activeTokenId), "not ");

   System.err.println( "Method 8 -------------");
   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   int xLoc = 50, yLoc = 10;
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Set Time Scale Line",
                                             new Point( xLoc, yLoc), helper, guiTest);
   int scaleMarkXLoc =
     ((TemporalExtentView.TimeScaleMark) temporalExtentView.getTimeScaleMark()).getXLoc();
   guiTest.assertTrueVerbose( "Temporal Extent View time scale mark is not located at x = " +
                      scaleMarkXLoc, (scaleMarkXLoc == xLoc), "not ");
                      
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Hide Node Labels",
                                             new Point( xLoc, yLoc), helper, guiTest); 
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Show Node Labels",
                                             new Point( xLoc, yLoc), helper, guiTest); 
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Show Earliest",
                                             new Point( xLoc, yLoc), helper, guiTest); 
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Show Latest",
                                             new Point( xLoc, yLoc), helper, guiTest); 
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView, "Show Intervals",
                                             new Point( xLoc, yLoc), helper, guiTest); 

   System.err.println( "Method 9 -------------");
   DBTransactionView dbTransactionView =
     ( DBTransactionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.DB_TRANSACTION_VIEW, viewNameSuffix, guiTest);
   ViewGenerics.raiseFrame( dbTransactionView.getViewFrame());
   PWTestHelper.viewBackgroundItemSelection( dbTransactionView,
                                             "Find Transaction by Entity_Key", helper, guiTest);
   int transIndx = dbTransactionView.getDBTransactionList().size() - 1;
   PwDBTransaction dbTransaction =
     (PwDBTransaction) dbTransactionView.getDBTransactionList().get( transIndx);
   PWTestHelper.handleDialogValueEntry( "Find Transaction by Entity_Key",
                                        dbTransaction.getEntityId().toString(),
                                         helper, guiTest);
   System.err.println( "Method 10 -------------");
   DBTransactionTable dbTable = (DBTransactionTable) dbTransactionView.getDBTransactionTable();
   TableSorter tableSorter = dbTable.getTableSorter();
   for (int colIndx = 0, n = dbTable.getModel().getColumnCount() - 1; colIndx < n; colIndx++) {
//      System.err.println( "sort colIndx " + colIndx + " status " +
//                          tableSorter.getSortingStatus( colIndx));
     String columnName = dbTable.getModel().getColumnName( colIndx);
     guiTest.assertTrueVerbose( "column " + columnName + " sort status is not NOT_SORTED",
                        (tableSorter.getSortingStatus( colIndx) ==
                         TableSorter.NOT_SORTED), "not ");
     helper.enterClickAndLeave( new JTableHeaderMouseEventData
                                ( guiTest, dbTable.getTableHeader(), colIndx, 1));
     guiTest.flushAWT(); guiTest.awtSleep();
     guiTest.assertTrueVerbose( "column " + columnName + " sort status is not ASCENDING",
                        (tableSorter.getSortingStatus( colIndx) ==
                         TableSorter.ASCENDING), "not ");
     helper.enterClickAndLeave( new JTableHeaderMouseEventData
                                ( guiTest, dbTable.getTableHeader(), colIndx, 1));
     guiTest.flushAWT(); guiTest.awtSleep();
     guiTest.assertTrueVerbose( "column " + columnName + " sort status is not DESCENDING",
                        (tableSorter.getSortingStatus( colIndx) ==
                         TableSorter.DESCENDING), "not ");
   }

   System.err.println( "Method 11 -------------");
   DecisionView decisionView =
     (DecisionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.DECISION_VIEW, viewNameSuffix, guiTest);
   ResourceProfileView resourceProfileView =
     (ResourceProfileView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_PROFILE_VIEW, viewNameSuffix, guiTest);
   ResourceTransactionView resourceTransactionView =
     (ResourceTransactionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_TRANSACTION_VIEW, viewNameSuffix, guiTest);

   String currentViewName = ViewConstants.CONSTRAINT_NETWORK_VIEW;
   PartialPlanView currentView = constraintNetworkView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, guiTest);
 
   currentViewName = ViewConstants.DB_TRANSACTION_VIEW;
   currentView = dbTransactionView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, guiTest);
 
   currentViewName = ViewConstants.DECISION_VIEW;
   currentView = decisionView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, guiTest);
 
   currentViewName = ViewConstants.RESOURCE_PROFILE_VIEW;
   currentView = resourceProfileView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, guiTest);
 
   currentViewName = ViewConstants.RESOURCE_TRANSACTION_VIEW;
   currentView = resourceTransactionView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, guiTest);
 
   currentViewName = ViewConstants.TEMPORAL_EXTENT_VIEW;
   currentView = temporalExtentView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, guiTest);
 
   currentViewName = ViewConstants.TIMELINE_VIEW;
   currentView = timelineView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, guiTest);
 
   currentViewName = ViewConstants.TOKEN_NETWORK_VIEW;
   currentView = tokenNetworkView;
   PWTestHelper.openAllExistingViews( currentViewName, currentView, helper, guiTest);
 
   ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
   guiTest.assertTrueVerbose( ViewConstants.CONSTRAINT_NETWORK_VIEW + " is not selected",
                      (constraintNetworkView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( constraintNetworkView,
                                             "Raise Content Filter", helper, guiTest);
   String viewName =  ViewConstants.CONTENT_SPEC_TITLE + " for " + viewNameSuffix;
   guiTest.assertTrueVerbose( viewName + " is not selected",
                      (constraintNetworkView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( resourceProfileView.getViewFrame());
   guiTest.assertTrueVerbose( ViewConstants.RESOURCE_PROFILE_VIEW + " is not selected",
                      (resourceProfileView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( resourceProfileView,
                                             "Raise Content Filter", helper, guiTest);
   guiTest.assertTrueVerbose( viewName + " is not selected",
                      (resourceProfileView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( resourceTransactionView.getViewFrame());
   guiTest.assertTrueVerbose( ViewConstants.RESOURCE_TRANSACTION_VIEW + " is not selected",
                      (resourceTransactionView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( resourceTransactionView,
                                             "Raise Content Filter", helper, guiTest);
   guiTest.assertTrueVerbose( viewName + " is not selected",
                      (resourceTransactionView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
   guiTest.assertTrueVerbose( ViewConstants.TEMPORAL_EXTENT_VIEW + " is not selected",
                      (temporalExtentView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( temporalExtentView,
                                             "Raise Content Filter", helper, guiTest);
   guiTest.assertTrueVerbose( viewName + " is not selected",
                      (temporalExtentView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( timelineView.getViewFrame());
   guiTest.assertTrueVerbose( ViewConstants.TIMELINE_VIEW + " is not selected",
                      (timelineView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( timelineView,
                                             "Raise Content Filter", helper, guiTest);
   guiTest.assertTrueVerbose( viewName + " is not selected",
                      (timelineView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
   guiTest.assertTrueVerbose( ViewConstants.TOKEN_NETWORK_VIEW + " is not selected",
                      (tokenNetworkView.getViewFrame().isSelected() == true), "not ");
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView,
                                             "Raise Content Filter", helper, guiTest);
   guiTest.assertTrueVerbose( viewName + " is not selected",
                      (tokenNetworkView.getViewSet().getContentSpecWindow().
                       isSelected() == true), "not ");

   System.err.println( "Method 12 -------------");
   ViewManager viewMgr = PlanWorks.getPlanWorks().getViewManager();
   viewMenuItemName = "Close All Views";
   PWTestHelper.seqStepsViewStepItemSelection
     ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, guiTest);
   ViewSet viewSet = viewMgr.getViewSet( partialPlan);
   guiTest.assertNullVerbose( "planSeq step " + stepNumber + " does not have 0 views",
                              viewSet, "not ");

   guiTest.invokeAllViewsSelections( ViewConstants.CONSTRAINT_NETWORK_VIEW,
                                     PlanWorksGUITest.CONSTRAINT_NETWORK_VIEW_INDEX,
                                     ViewConstants.DB_TRANSACTION_VIEW,
                                     PlanWorksGUITest.DB_TRANSACTION_VIEW_INDEX,
                                     viewNameSuffix, seqStepsView, partialPlan, viewMgr,
                                     stepNumber);

   guiTest.invokeAllViewsSelections( ViewConstants.DB_TRANSACTION_VIEW,
                                     PlanWorksGUITest.DB_TRANSACTION_VIEW_INDEX,
                                     ViewConstants.DECISION_VIEW,
                                     PlanWorksGUITest.DECISION_VIEW_INDEX,
                                     viewNameSuffix, seqStepsView, partialPlan, viewMgr,
                                     stepNumber);

   guiTest.invokeAllViewsSelections( ViewConstants.DECISION_VIEW,
                                     PlanWorksGUITest.DECISION_VIEW_INDEX,
                                     ViewConstants.RESOURCE_PROFILE_VIEW,
                                     PlanWorksGUITest.RESOURCE_PROFILE_VIEW_INDEX,
                                     viewNameSuffix, seqStepsView, partialPlan, viewMgr,
                                     stepNumber);

   guiTest.invokeAllViewsSelections( ViewConstants.RESOURCE_PROFILE_VIEW,
                                     PlanWorksGUITest.RESOURCE_PROFILE_VIEW_INDEX,
                                     ViewConstants.RESOURCE_TRANSACTION_VIEW,
                                     PlanWorksGUITest.RESOURCE_TRANSACTION_VIEW_INDEX,
                                     viewNameSuffix, seqStepsView, partialPlan, viewMgr,
                                     stepNumber);

   guiTest.invokeAllViewsSelections( ViewConstants.RESOURCE_TRANSACTION_VIEW,
                                     PlanWorksGUITest.RESOURCE_TRANSACTION_VIEW_INDEX,
                                     ViewConstants.TEMPORAL_EXTENT_VIEW,
                                     PlanWorksGUITest.TEMPORAL_EXTENT_VIEW_INDEX,
                                     viewNameSuffix, seqStepsView, partialPlan, viewMgr,
                                     stepNumber);

   guiTest.invokeAllViewsSelections( ViewConstants.TEMPORAL_EXTENT_VIEW,
                                     PlanWorksGUITest.TEMPORAL_EXTENT_VIEW_INDEX,
                                     ViewConstants.TIMELINE_VIEW,
                                     PlanWorksGUITest.TIMELINE_VIEW_INDEX,
                                     viewNameSuffix, seqStepsView, partialPlan, viewMgr,
                                     stepNumber);

   guiTest.invokeAllViewsSelections( ViewConstants.TIMELINE_VIEW,
                                     PlanWorksGUITest.TIMELINE_VIEW_INDEX,
                                     ViewConstants.TOKEN_NETWORK_VIEW,
                                     PlanWorksGUITest.TOKEN_NETWORK_VIEW_INDEX,
                                     viewNameSuffix, seqStepsView, partialPlan, viewMgr,
                                     stepNumber);

   guiTest.invokeAllViewsSelections( ViewConstants.TOKEN_NETWORK_VIEW,
                                     PlanWorksGUITest.TOKEN_NETWORK_VIEW_INDEX,
                                     ViewConstants.CONSTRAINT_NETWORK_VIEW,
                                     PlanWorksGUITest.CONSTRAINT_NETWORK_VIEW_INDEX,
                                     viewNameSuffix, seqStepsView, partialPlan, viewMgr,
                                     stepNumber);

   System.err.println( "Method 13 -------------");
   viewListenerList = guiTest.createViewListenerList();
   viewMenuItemName = "Open All Views";
   PWTestHelper.seqStepsViewStepItemSelection
     ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, guiTest);
   guiTest.viewListenerListWait( PlanWorksGUITest.CONSTRAINT_NETWORK_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.DB_TRANSACTION_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.DECISION_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.RESOURCE_PROFILE_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.RESOURCE_TRANSACTION_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.TEMPORAL_EXTENT_VIEW_INDEX,
                                 viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.TIMELINE_VIEW_INDEX, viewListenerList);
   guiTest.viewListenerListWait( PlanWorksGUITest.TOKEN_NETWORK_VIEW_INDEX, viewListenerList);

   constraintNetworkView = (ConstraintNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.CONSTRAINT_NETWORK_VIEW, viewNameSuffix, guiTest);
   guiTest.createOverviewWindow( ViewConstants.CONSTRAINT_NETWORK_VIEW, constraintNetworkView,
                                 viewNameSuffix);

   resourceProfileView = (ResourceProfileView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_PROFILE_VIEW, viewNameSuffix, guiTest);
   guiTest.createOverviewWindow( ViewConstants.RESOURCE_PROFILE_VIEW, resourceProfileView,
                                 viewNameSuffix);

   resourceTransactionView = (ResourceTransactionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_TRANSACTION_VIEW, viewNameSuffix, guiTest);
   guiTest.createOverviewWindow( ViewConstants.RESOURCE_TRANSACTION_VIEW,
                                 resourceTransactionView, viewNameSuffix);

   temporalExtentView = (TemporalExtentView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TEMPORAL_EXTENT_VIEW, viewNameSuffix, guiTest);
   guiTest.createOverviewWindow( ViewConstants.TEMPORAL_EXTENT_VIEW, temporalExtentView,
                                 viewNameSuffix);

   timelineView = (TimelineView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, guiTest);
   guiTest.createOverviewWindow( ViewConstants.TIMELINE_VIEW, timelineView,
                                 viewNameSuffix);

   tokenNetworkView = (TokenNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TOKEN_NETWORK_VIEW, viewNameSuffix, guiTest);
   guiTest.createOverviewWindow( ViewConstants.TOKEN_NETWORK_VIEW, tokenNetworkView,
                                 viewNameSuffix);


   System.err.println( "Method 14 -------------");
   viewMenuItemName = "Enable Auto Snap";
   PWTestHelper.viewBackgroundItemSelection( timelineView, viewMenuItemName, helper, guiTest);
    TimelineTokenNode theFreeTokenNode = null;
    SlotNode theSlotNode = null;
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        if ((theSlotNode == null) && (slotNode.getSlot().getTokenList().size() > 0)) {
          theSlotNode = slotNode;
        }
      }
    }
    Iterator freeTokenNodeItr = timelineView.getFreeTokenNodeList().iterator();
    while (freeTokenNodeItr.hasNext()) {
      TimelineTokenNode freeTimelineTokenNode = (TimelineTokenNode) freeTokenNodeItr.next();
      if (theFreeTokenNode == null) {
        theFreeTokenNode = freeTimelineTokenNode;
      }
    }
    guiTest.assertNotNullVerbose( "Did not find TimelineView slotNode (SlotNode)",
                          theSlotNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find TimelineView freeTokenNode (TimelineViewTokenNode)",
                          theFreeTokenNode, "not ");

    temporalExtentView.getViewFrame().
      setLocation( 0, (int) temporalExtentView.getViewFrame().getLocation().getY());
    ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
    ViewGenerics.raiseFrame( timelineView.getViewFrame());
    theSlotNode.doUncapturedMouseMove( 0, theSlotNode.getLocation(), new Point( 0, 0),
                                       timelineView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    PwToken baseToken = theSlotNode.getSlot().getBaseToken();
    List mergedTokens = theSlotNode.getSlot().getTokenList();
    mergedTokens.remove( baseToken);
    PwToken mergedToken = (PwToken) mergedTokens.get( 0);
    activeToken = ((PartialPlanViewSet) temporalExtentView.getViewSet()).getActiveToken();
    guiTest.assertTrueVerbose( "Base Token id=" + baseToken.getId() + " is not the active token",
                       activeToken.getId().equals( baseToken.getId()), "not ");
    guiTest.assertTrueVerbose( "Base Token id=" + baseToken.getId() +
                       " is not the focus token node in the Temporal Extent View",
                       temporalExtentView.getFocusNodeId().equals( baseToken.getId()), "not ");
    List secondaryTokens = ((PartialPlanViewSet) temporalExtentView.getViewSet()).
      getSecondaryTokens();
    guiTest.assertTrueVerbose( "Merged Token id=" + mergedToken.getId() +
                       " is not the secondary token node in the Temporal Extent View",
                       secondaryTokens.contains( mergedToken), "not ");

    theFreeTokenNode.doUncapturedMouseMove( 0, theFreeTokenNode.getLocation(), new Point( 0, 0),
                                       timelineView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    PwToken freeToken = theFreeTokenNode.getToken();
    activeToken = ((PartialPlanViewSet) temporalExtentView.getViewSet()).getActiveToken();
    guiTest.assertTrueVerbose( "Free Token id=" + freeToken.getId() + " is not the active token",
                       activeToken.getId().equals( freeToken.getId()), "not ");
    guiTest.assertTrueVerbose( "Free Token id=" + freeToken.getId() +
                       " is not the focus token node in the Temporal Extent View",
                       temporalExtentView.getFocusNodeId().equals( freeToken.getId()), "not ");

   System.err.println( "Method 15 -------------");
   int maxRuleNodes = 4, viewNum = 1;
   List keyList = tokenNetworkView.getRuleInstanceNodeKeyList();
   viewListener01 = new PlanWorksGUITest.ViewListenerWait01( guiTest);
   for (int i = 0; i < maxRuleNodes; i++) {
     RuleInstanceNode ruleNode =
       tokenNetworkView.getRuleInstanceNode( (Integer) keyList.get( i));
     ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
     viewListener01.reset();
     ruleNode.doMouseClickWithListener( MouseEvent.BUTTON3_MASK, ruleNode.getLocation(),
                                        new Point( 0, 0), tokenNetworkView.getJGoView(),
                                        viewListener01);
     viewMenuItemName = "Open Rule Instance View";
     PWTestHelper.selectViewMenuItem( tokenNetworkView, viewMenuItemName, helper, guiTest);
     viewListener01.viewWait();
     RuleInstanceView ruleInstanceView = (RuleInstanceView) PWTestHelper.getPartialPlanView
       ( ViewConstants.RULE_INSTANCE_VIEW, viewNameSuffix + " - " + viewNum, guiTest);
     guiTest.flushAWT(); guiTest.awtSleep();
     viewNum++;
   }

   System.err.println( "Method 16 -------------");
   viewMenuItemName = "Close Rule Instance Views";
   PWTestHelper.viewBackgroundItemSelection( tokenNetworkView, viewMenuItemName, helper, guiTest);
   viewNum = 1;
   viewSet = viewMgr.getViewSet( partialPlan);
   for (int i = 0; i < maxRuleNodes; i++) {
     String ruleInstanceViewName =
       ViewConstants.RULE_INSTANCE_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix +
       " - " + viewNum;
     guiTest.assertFalseVerbose( "'" + ruleInstanceViewName + "' is not closed",
                         viewSet.doesViewFrameExist( ruleInstanceViewName), "not ");
     viewNum++;
   }

   // try{Thread.sleep(4000);}catch(Exception e){}

   ThreadListener threadListener01 = new PlanWorksGUITest.ThreadListenerWait01( guiTest);
   PlanWorks.getPlanWorks().setDeleteSequenceThreadListener( threadListener01);
   PWTestHelper.deleteSequenceFromProject( (String) sequenceUrls.get( 4), helper, guiTest);
   threadListener01.threadWait();
   PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);

   System.err.println( "\nPLANVIZ_11 COMPLETED\n");
   try{Thread.sleep(1000);}catch(Exception e){}
  } // end planViz11

} // end class PlanWorksGUITest11
