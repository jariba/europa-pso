//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest10.java,v 1.3 2006-10-03 16:14:17 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.examples.Overview;

import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.finder.Finder;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.ThreadListener;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwConstraint;
//import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.db.PwDecision;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.util.SQLDB;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkObjectNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkResourceNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkRuleInstanceNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
//import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;    
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNode;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNodeDurationBridge;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNodeTimeMark;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.SlotNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineViewTimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
//import gov.nasa.arc.planworks.viz.util.DBTransactionTable;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


public class PlanWorksGUITest10 {

  private PlanWorksGUITest10() {
  }

  public static void planViz10( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
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
    String viewMenuItemName = "Open " + ViewConstants.CONSTRAINT_NETWORK_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, guiTest);
   String viewNameSuffix = PWTestHelper.SEQUENCE_NAME +
     System.getProperty( "file.separator") + "step" + String.valueOf( stepNumber);
   guiTest.viewListenerListWait( PlanWorksGUITest.CONSTRAINT_NETWORK_VIEW_INDEX,
                                 viewListenerList);
   ConstraintNetworkView constraintNetworkView =
     (ConstraintNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.CONSTRAINT_NETWORK_VIEW, viewNameSuffix, guiTest);
    viewList.add( constraintNetworkView);

    viewMenuItemName = "Open " + ViewConstants.TEMPORAL_EXTENT_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, guiTest);
    guiTest.viewListenerListWait( PlanWorksGUITest.TEMPORAL_EXTENT_VIEW_INDEX,
                                  viewListenerList);
    TemporalExtentView temporalExtentView =
      (TemporalExtentView) PWTestHelper.getPartialPlanView
      ( ViewConstants.TEMPORAL_EXTENT_VIEW, viewNameSuffix, guiTest);
    viewList.add( temporalExtentView);

    viewMenuItemName = "Open " + ViewConstants.TIMELINE_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, guiTest);
    guiTest.viewListenerListWait( PlanWorksGUITest.TIMELINE_VIEW_INDEX, viewListenerList);
    TimelineView timelineView = (TimelineView) PWTestHelper.getPartialPlanView
      ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, guiTest);
    viewList.add( timelineView);

    viewMenuItemName = "Open " + ViewConstants.TOKEN_NETWORK_VIEW;
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, guiTest);
    guiTest.viewListenerListWait( PlanWorksGUITest.TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    TokenNetworkView tokenNetworkView = (TokenNetworkView) PWTestHelper.getPartialPlanView
      ( ViewConstants.TOKEN_NETWORK_VIEW, viewNameSuffix, guiTest);
    viewList.add( tokenNetworkView);

    // viewMenuItemName = "Open " + ViewConstants.DB_TRANSACTION_VIEW;
//     PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
//                                                 viewMenuItemName, viewListenerList,
//                                                 helper, guiTest);
//     guiTest.viewListenerListWait( PlanWorksGUITest.DB_TRANSACTION_VIEW_INDEX, viewListenerList);
//      DBTransactionView dbTransactionView =
//        (DBTransactionView) PWTestHelper.getPartialPlanView
//        ( ViewConstants.DB_TRANSACTION_VIEW, viewNameSuffix, guiTest);
//     viewList.add( dbTransactionView);

    PwPlanningSequence planSeq =
      planWorks.getCurrentProject().getPlanningSequence( (String) sequenceUrls.get( seqUrlIndex));
    PwPartialPlan partialPlan = planSeq.getPartialPlan( stepNumber);

    planViz10CNet( constraintNetworkView, stepNumber, planSeq, partialPlan, planWorks,
                   helper, guiTest);

    planViz10TempExt( temporalExtentView, stepNumber, planSeq, partialPlan, planWorks,
                      helper, guiTest);

    planViz10Timeline( timelineView, stepNumber, planSeq, partialPlan, planWorks,
                       helper, guiTest);

    planViz10TokNet( tokenNetworkView, stepNumber, planSeq, partialPlan, planWorks,
                     helper, guiTest);

//     planViz10DBTrans( dbTransactionView, stepNumber, planSeq, partialPlan, planWorks,
//                       helper, guiTest);

    // no need to wait on these calls, since all the views currently exist
    viewMenuItemName = "Hide All Views";
    PWTestHelper.seqStepsViewStepItemSelection
      ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, guiTest);
    // try{Thread.sleep(2000);}catch(Exception e){}
    Iterator viewItr = viewList.iterator();
    while (viewItr.hasNext()) {
      MDIInternalFrame frame = ((PartialPlanView) viewItr.next()).getViewFrame();
      guiTest.assertTrueVerbose( frame.getTitle() + " is not iconified", (frame.isIcon() == true),
                         " not");
    }

    viewMenuItemName = "Show All Views";
    PWTestHelper.seqStepsViewStepItemSelection
      ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, guiTest);
    // try{Thread.sleep(2000);}catch(Exception e){}
    viewItr = viewList.iterator();
    while (viewItr.hasNext()) {
      MDIInternalFrame frame = ((PartialPlanView) viewItr.next()).getViewFrame();
      guiTest.assertTrueVerbose( frame.getTitle() + " is not shown", (frame.isIcon() == false),
                         " not");
    }

    viewMenuItemName = "Close All Views";
    PWTestHelper.seqStepsViewStepItemSelection
      ( seqStepsView, stepNumber, viewMenuItemName, viewListenerList, helper, guiTest);
    // viewSet is set to null, if there are no views
    ViewSet viewSet = PlanWorks.getPlanWorks().getViewManager().getViewSet( partialPlan);
    guiTest.assertNullVerbose( "Partial Plan view set is not null", viewSet, " not");

    PWTestHelper.viewBackgroundItemSelection( seqStepsView, "Overview Window", helper, guiTest);
    String overviewViewName =
      Utilities.trimView( ViewConstants.SEQUENCE_STEPS_VIEW).replaceAll( " ", "") +
      ViewConstants.OVERVIEW_TITLE + planSeq.getName();
    VizViewOverview seqStepsOverview =
      (VizViewOverview) PWTestHelper.findComponentByName
      ( VizViewOverview.class, overviewViewName, Finder.OP_EQUALS);
    guiTest.assertNotNullVerbose( overviewViewName + " not found", seqStepsOverview, "not ");

    Overview.OverviewRectangle rectangle = seqStepsOverview.getOverviewRect();
    System.err.println( "OverviewRectangle location " + rectangle.getLocation());
    // try{Thread.sleep(4000);}catch(Exception e){}

    ThreadListener threadListener01 = new PlanWorksGUITest.ThreadListenerWait01( guiTest);
    PlanWorks.getPlanWorks().setDeleteSequenceThreadListener( threadListener01);
    PWTestHelper.deleteSequenceFromProject( (String) sequenceUrls.get( 4), helper, guiTest);
    threadListener01.threadWait();
    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);

    System.err.println( "\nPLANVIZ_10 COMPLETED\n");
  } // end planViz10

  public static void planViz10CNet( ConstraintNetworkView constraintNetworkView, int stepNumber,
                                    PwPlanningSequence planSeq,  PwPartialPlan partialPlan,
                                    PlanWorks planWorks, JFCTestHelper helper,
                                    PlanWorksGUITest guiTest)
    throws Exception {
    ViewGenerics.raiseFrame( constraintNetworkView.getViewFrame());
    // open interval token, resource, variable, and constraint nodes in ConstraintNetworkView
    int numTokensOpened = 3, numResourcesOpened = 1, numResTransactionsOpened = 1;
    int numTimelinesOpened = 1, numObjectsOpened = 1, numRuleInstancesOpened = 1;
    ConstraintNetworkTokenNode baseTokenNode = null;
    ConstraintNetworkTokenNode mergedTokenNode = null;
    ConstraintNetworkTokenNode freeTokenNode = null;
    ConstraintNetworkTokenNode resTransactionNode = null;
    ConstraintNetworkResourceNode resourceNode = null;
    ConstraintNetworkTimelineNode timelineNode = null;
    ConstraintNetworkObjectNode objectNode = null;
    ConstraintNetworkRuleInstanceNode ruleInstanceNode = null;
    
    int numTokenNodes = 0, numResourceNodes = 0, numResTransactionNodes = 0;
    int numTimelineNodes = 0, numObjectNodes = 0, numRuleInstanceNodes = 0;
    Iterator containerNodeItr = constraintNetworkView.getContainerNodeList().iterator();
    while (containerNodeItr.hasNext()) {
      VariableContainerNode contNode = (VariableContainerNode) containerNodeItr.next();
      if (contNode instanceof ConstraintNetworkTokenNode) {
        ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) contNode;
//         System.err.println( "tokenNode " + tokenNode.getClass().getName() +
//                             " token " + tokenNode.getToken().getClass().getName());
        if (tokenNode.getToken() instanceof PwResourceTransaction) {
          if (resTransactionNode == null) {
            resTransactionNode = (ConstraintNetworkTokenNode) contNode;
          }
          numResTransactionNodes++;
        } else if (tokenNode.getToken() instanceof PwToken) {
          PwToken token = (PwToken) tokenNode.getToken();
          boolean isFree = token.isFree(), isBaseToken = token.isBaseToken();
          int slotTokenCnt = 0;
          if (token.getSlotId() != DbConstants.NO_ID) {
            slotTokenCnt =
              partialPlan.getSlot( token.getSlotId()).getTokenList().size();
          }
          if ((! isFree ) && isBaseToken && (slotTokenCnt > 1) && (baseTokenNode == null)) {
            baseTokenNode = tokenNode;
          } else if (isFree && (freeTokenNode == null)) {
            freeTokenNode = tokenNode;
          }
        }
        numTokenNodes++;
      } else if (contNode instanceof ConstraintNetworkResourceNode) {
        if (resourceNode == null) {
          resourceNode = (ConstraintNetworkResourceNode) contNode;
        }
        numResourceNodes++;
      } else if (contNode instanceof ConstraintNetworkTimelineNode) {
        if (timelineNode == null) {
          timelineNode = (ConstraintNetworkTimelineNode) contNode;
        }
        numTimelineNodes++;
      } else if (contNode instanceof ConstraintNetworkObjectNode) {
        if (objectNode == null) {
          objectNode = (ConstraintNetworkObjectNode) contNode;
        }
        numObjectNodes++;
      } else if (contNode instanceof ConstraintNetworkRuleInstanceNode) {
        if (ruleInstanceNode == null) {
          ruleInstanceNode = (ConstraintNetworkRuleInstanceNode) contNode;
        }
        numRuleInstanceNodes++;
      }
    }
    // find mergedTokenNode
    containerNodeItr = constraintNetworkView.getContainerNodeList().iterator();
    while (containerNodeItr.hasNext()) {
      VariableContainerNode contNode = (VariableContainerNode) containerNodeItr.next();
      if (contNode instanceof ConstraintNetworkTokenNode) {
        ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) contNode;
        if (tokenNode.getToken() instanceof PwToken) {
          PwToken token = (PwToken) tokenNode.getToken();
          boolean isFree = token.isFree(), isBaseToken = token.isBaseToken();
          if ((! isFree) && (! isBaseToken) && (mergedTokenNode == null) &&
                     (baseTokenNode != null) &&
                     (baseTokenNode.getToken().getSlotId().equals( token.getSlotId()))) {
            mergedTokenNode = tokenNode;
            break;
          }
        }
      }
    }
    // try{Thread.sleep(6000);}catch(Exception e){}

    guiTest.assertNotNullVerbose( "Did not find ConstraintNetworkTokenNode baseTokenNode",
                          baseTokenNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetworkTokenNode mergedTokenNode",
                          mergedTokenNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetworkTokenNode freeTokenNode",
                          freeTokenNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetworkResourceNode resourceNode",
                          resourceNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetworkTokenNode resTransactionNode",
                          resTransactionNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetworkTimelineNode timelineNode",
                          timelineNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetworkObjectNode objectNode",
                          objectNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetworkRuleInstanceNode ruleInstanceNode",
                          ruleInstanceNode, "not ");
    guiTest.assertTrueVerbose
      ( "Number of partial plan interval tokens and resource transactions (" +
        partialPlan.getTokenList().size() +
        ") not equal number of ContraintNetwork interval token and resource transaction " +
        "nodes (" + numTokenNodes + ")", (partialPlan.getTokenList().size() == numTokenNodes),
        "not ");
    guiTest.assertTrueVerbose
      ( "Number of partial plan resources (" + partialPlan.getResourceList().size() +
        ") not equal number of ContraintNetwork resource nodes (" + numResourceNodes +
        ")", (partialPlan.getResourceList().size() == numResourceNodes), "not ");
    guiTest.assertTrueVerbose
      ( "Number of partial plan resourceTransactions (" +
        partialPlan.getResTransactionList().size() + ") not equal number of ContraintNetwork " +
        "resourceTransaction nodes (" + numResTransactionNodes + ")",
        (partialPlan.getResTransactionList().size() == numResTransactionNodes), "not ");
    guiTest.assertTrueVerbose
      ( "Number of partial plan timelines (" + partialPlan.getTimelineList().size() +
        ") not equal number of ContraintNetwork timeline nodes (" + numTimelineNodes +
        ")", (partialPlan.getTimelineList().size() == numTimelineNodes), "not ");
    int numNodes = numObjectNodes + numResourceNodes + numTimelineNodes;
    guiTest.assertTrueVerbose
      ( "Number of partial plan objects, timelines, & resources (" +
        partialPlan.getObjectList().size() +
        ") not equal number of ContraintNetwork object, timeline, & resources nodes (" +
        numNodes + ")", (partialPlan.getObjectList().size() == numNodes), "not ");
    guiTest.assertTrueVerbose
      ( "Number of partial plan rule instances (" + partialPlan.getRuleInstanceList().size() +
        ") not equal number of ContraintNetwork rule instance nodes (" + numRuleInstanceNodes +
        ")", (partialPlan.getRuleInstanceList().size() == numRuleInstanceNodes), "not ");
    guiTest.assertTrueVerbose
      ( "Token id=" + mergedTokenNode.getToken().getId() + " is not a merged " +
        " token of base token id=" + baseTokenNode.getToken().getId(),
        (mergedTokenNode.getToken().getSlotId().equals
         ( baseTokenNode.getToken().getSlotId())), "not ");

    baseTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, baseTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    mergedTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, mergedTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    freeTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, freeTokenNode.getLocation(),
                                new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    resourceNode.doMouseClick( MouseEvent.BUTTON1_MASK, resourceNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    resTransactionNode.doMouseClick( MouseEvent.BUTTON1_MASK, resTransactionNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    timelineNode.doMouseClick( MouseEvent.BUTTON1_MASK, timelineNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    objectNode.doMouseClick( MouseEvent.BUTTON1_MASK, objectNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    ruleInstanceNode.doMouseClick( MouseEvent.BUTTON1_MASK, ruleInstanceNode.getLocation(),
                                   new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    int numVarNodesInView = constraintNetworkView.getVariableNodeList().size();
    int numVarNodesOpened =
      (numTokensOpened * PWSetupHelper.NUM_VARS_PER_TOKEN) +
      (numResourcesOpened * PWSetupHelper.NUM_VARS_PER_RESOURCE) +
      (numResTransactionsOpened * PWSetupHelper.NUM_VARS_PER_RESOURCE_TRANS) +
      (numTimelinesOpened * PWSetupHelper.NUM_VARS_PER_TIMELINE) +
      (numObjectsOpened * PWSetupHelper.NUM_VARS_PER_OBJECT) +
      (numRuleInstancesOpened * PWSetupHelper.NUM_VARS_PER_RULE_INSTANCE);
    guiTest.assertTrueVerbose( "Number of ContraintNetwork variable nodes (" + numVarNodesInView +
                       ") not equal number of token (interval & resTrans) and resource " +
                       "variables opened (" + numVarNodesOpened + ")",
                       (numVarNodesOpened == numVarNodesInView), "not ");
    // open token type = START_VAR variables
    int numVariablesOpened = 3;
    VariableNode variableNode1 = null;
    VariableNode variableNode2 = null;
    VariableNode variableNode3 = null;
    int numVariableNodes = 0;
    Iterator variableNodeItr = constraintNetworkView.getVariableNodeList().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode varNode = (VariableNode) variableNodeItr.next();
      if (varNode.getVariable().getType().equals( DbConstants.START_VAR)) {
        Iterator containNodeItr = varNode.getContainerNodeList().iterator();
        while (containNodeItr.hasNext()) {
          VariableContainerNode containNode = (VariableContainerNode) containNodeItr.next();
          if (containNode instanceof ConstraintNetworkTokenNode) {
            ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) containNode;
            if (tokenNode.getToken().getId().equals( baseTokenNode.getToken().getId())) {
              variableNode1 = varNode;
            } else if (tokenNode.getToken().getId().equals
                       ( mergedTokenNode.getToken().getId())) {
              variableNode2 = varNode;
            } else if (tokenNode.getToken().getId().equals( freeTokenNode.getToken().getId())) {
              variableNode3 = varNode;
            }
          }
        }
      }
      numVariableNodes++;
    }
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetwork VariableNode variableNode1",
                   variableNode1, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetwork VariableNode variableNode2",
                   variableNode2, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetwork VariableNode variableNode3",
                   variableNode3, "not ");
    variableNode1.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    variableNode2.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode2.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    variableNode3.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode3.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    int numContNodesInView = constraintNetworkView.getConstraintNodeList().size();
    int numContNodesOpened =
      (numTokensOpened * PWSetupHelper.NUM_CONSTRAINTS_PER_TOKEN) +
      (numResourcesOpened * PWSetupHelper.NUM_CONSTRAINTS_PER_RESOURCE);
    guiTest.assertTrueVerbose( "Number of ContraintNetwork constraint nodes (" + numContNodesInView +
                       ") not equal number of token (interval & resTrans) and resource " +
                       "constraints opened (" + numContNodesOpened + ")",
                       (numContNodesOpened == numContNodesInView), "not ");
//     System.err.println( "ContraintNetworkView: " + numVariablesOpened +
//                         " variables opened ==>> " +
//                         numContNodesInView + " constraint nodes visible");

    ConstraintNode constraintNode1 = null;
    ConstraintNode constraintNode2 = null;
    Iterator constraintNodeItr = constraintNetworkView.getConstraintNodeList().iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode contNode = (ConstraintNode) constraintNodeItr.next();
      if (! contNode.areNeighborsShown()) {
        if (constraintNode1 == null) {
          constraintNode1 = contNode;
        } else if (constraintNode2 == null) {
          constraintNode2 = contNode;
          break;
        }
      }
    }
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetwork ConstraintNode constraintNode1",
                   constraintNode1, "not ");
    guiTest.assertNotNullVerbose( "Did not find ConstraintNetwork ConstraintNode constraintNode2",
                   constraintNode2, "not ");
    constraintNode1.doMouseClick( MouseEvent.BUTTON1_MASK, constraintNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    constraintNode2.doMouseClick( MouseEvent.BUTTON1_MASK, constraintNode2.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();

    // try{Thread.sleep(6000);}catch(Exception e){}

    // now close them in reverse order
    constraintNode1.doMouseClick( MouseEvent.BUTTON1_MASK, constraintNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    constraintNode2.doMouseClick( MouseEvent.BUTTON1_MASK, constraintNode2.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    variableNode1.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode1.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    variableNode2.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode2.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    variableNode3.doMouseClick( MouseEvent.BUTTON1_MASK, variableNode3.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    baseTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, baseTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    mergedTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, mergedTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    freeTokenNode.doMouseClick( MouseEvent.BUTTON1_MASK, freeTokenNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    resourceNode.doMouseClick( MouseEvent.BUTTON1_MASK, resourceNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    resTransactionNode.doMouseClick( MouseEvent.BUTTON1_MASK, resTransactionNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    timelineNode.doMouseClick( MouseEvent.BUTTON1_MASK, timelineNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    objectNode.doMouseClick( MouseEvent.BUTTON1_MASK, objectNode.getLocation(),
                             new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
    ruleInstanceNode.doMouseClick( MouseEvent.BUTTON1_MASK, ruleInstanceNode.getLocation(),
                                   new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();

    guiTest.assertTrueVerbose( "constraintNode1 is not closed",
                        (! constraintNode1.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "constraintNode2 is not closed",
                        (! constraintNode2.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "variableNode1 is not closed",
                        (! variableNode1.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "variableNode2 is not closed",
                        (! variableNode2.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "variableNode3 is not closed",
                       (! variableNode3.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "baseTokenNode is not closed",
                       (! baseTokenNode.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "mergedTokenNode is not closed",
                       (! mergedTokenNode.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "freeTokenNode is not closed",
                       (! freeTokenNode.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "resourceNode is not closed",
                       (! resourceNode.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "resTransactionNode is not closed",
                        (! resTransactionNode.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "timelineNode is not closed",
                       (! timelineNode.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "objectNode is not closed",
                       (! objectNode.areNeighborsShown()), "not ");
    guiTest.assertTrueVerbose( "ruleInstanceNode is not closed",
                       (! ruleInstanceNode.areNeighborsShown()), "not ");

//     System.err.println( "baseTokenNode.getToolTipText() " + baseTokenNode.getToolTipText());
//     System.err.println( "baseTokenNode.getText() " + baseTokenNode.getText());
    String toolTipText = baseTokenNode.getToolTipText();
    String labelText = baseTokenNode.getText();
    String predSubString = baseTokenNode.getPredicateName();
    String predParamSubString = predSubString + " (";
    String slotSubString = "slot key=" + baseTokenNode.getToken().getSlotId().toString();
    String tokenSubString = "key=" + baseTokenNode.getToken().getId().toString();
    String mouseSubString = "Mouse-L: open";
    guiTest.assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + predParamSubString + "'",
                       (toolTipText.indexOf( predParamSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + slotSubString + "'",
                       (toolTipText.indexOf( slotSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") label does not contain '" + tokenSubString + "'",
                       (labelText.indexOf( tokenSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") label does not contain '" + predSubString + "'",
                       (labelText.indexOf( predSubString) >= 0), "not ");

//     System.err.println( "resTransactionNode.getToolTipText() " +
//                         resTransactionNode.getToolTipText());
//     System.err.println( "resTransactionNode.getText() " + resTransactionNode.getText());
    toolTipText = resTransactionNode.getToolTipText();
    labelText = resTransactionNode.getText();
    String nameSubString = resTransactionNode.getToken().getName();
    String resourceSubString = "key=" + resTransactionNode.getToken().getId().toString();
    guiTest.assertTrueVerbose( "ConstraintNetworkView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + nameSubString + "'",
                       (toolTipText.indexOf( nameSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" + resourceSubString + "'",
                       (labelText.indexOf( resourceSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" + nameSubString + "'",
                       (labelText.indexOf( nameSubString) >= 0), "not ");

//     System.err.println( "resourceNode.getToolTipText() " + resourceNode.getToolTipText());
//     System.err.println( "resourceNode.getText() " + resourceNode.getText());
    toolTipText = resourceNode.getToolTipText();
    labelText = resourceNode.getText();
    nameSubString = resourceNode.getResource().getName();
    resourceSubString = "key=" + resourceNode.getResource().getId().toString();
    guiTest.assertTrueVerbose( "ConstraintNetworkView resource node (" +
                       resourceNode.getResource().getId().toString() +
                       ") tool tip does not contain '" + nameSubString + "'",
                       (toolTipText.indexOf( nameSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView resource node (" +
                       resourceNode.getResource().getId().toString() +
                       ") tool tip does not contain '" + mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView resource node (" +
                       resourceNode.getResource().getId().toString() +
                       ") label does not contain '" + resourceSubString + "'",
                       (labelText.indexOf( resourceSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView resource node (" +
                       resourceNode.getResource().getId().toString() +
                       ") label does not contain '" + nameSubString + "'",
                       (labelText.indexOf( nameSubString) >= 0), "not ");

//     System.err.println( "variableNode1.getToolTipText() " + variableNode1.getToolTipText());
//     System.err.println( "variableNode1.getText() " + variableNode1.getText());
    toolTipText = variableNode1.getToolTipText();
    labelText = variableNode1.getText();
    String typeSubString = variableNode1.getVariable().getType();
    String variableSubString = "key=" + variableNode1.getVariable().getId().toString();
    String domainSubString = variableNode1.getVariable().getDomain().toString();
    guiTest.assertTrueVerbose( "ConstraintNetworkView variable node (" +
                       variableNode1.getVariable().getId().toString() +
                       ") tool tip does not contain '" + typeSubString + "'",
                       (toolTipText.indexOf( typeSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView variable node (" +
                       variableNode1.getVariable().getId().toString() +
                       ") tool tip does not contain '" +mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView variable node (" +
                       variableNode1.getVariable().getId().toString() +
                       ") label does not contain '" + variableSubString + "'",
                       (labelText.indexOf( variableSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView variable node (" +
                       variableNode1.getVariable().getId().toString() +
                       ") label does not contain '" + domainSubString + "'",
                       (labelText.indexOf( domainSubString) >= 0), "not ");

//     System.err.println( "constraintNode1.getToolTipText() " + constraintNode1.getToolTipText());
//     System.err.println( "constraintNode1.getText() " + constraintNode1.getText());
    toolTipText = constraintNode1.getToolTipText();
    labelText = constraintNode1.getText();
    typeSubString = constraintNode1.getConstraint().getType();
    String constraintSubString = "key=" + constraintNode1.getConstraint().getId().toString();
    nameSubString = constraintNode1.getConstraint().getName();
    guiTest.assertTrueVerbose( "ConstraintNetworkView constraint node (" +
                       constraintNode1.getConstraint().getId().toString() +
                       ") tool tip does not contain '" + typeSubString + "'",
                       (toolTipText.indexOf( typeSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView constraint node (" +
                       constraintNode1.getConstraint().getId().toString() +
                       ") tool tip does not contain '" + mouseSubString + "'",
                       (toolTipText.indexOf( mouseSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView constraint node (" +
                       constraintNode1.getConstraint().getId().toString() +
                       ") label does not contain '" + constraintSubString + "'",
                       (labelText.indexOf( constraintSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView constraint node (" +
                       constraintNode1.getConstraint().getId().toString() +
                       ") label does not contain '" + nameSubString + "'",
                       (labelText.indexOf( nameSubString) >= 0), "not ");

    labelText = ruleInstanceNode.getText();
    toolTipText = ruleInstanceNode.getToolTipText();
    String keyString = "key=" + ruleInstanceNode.getRuleInstance().getId().toString();
    String ruleString = "rule ";
    guiTest.assertTrueVerbose( "ConstraintNetworkView rule instance node (" +
                       ruleInstanceNode.getRuleInstance().getId().toString() +
                       ") label does not contain '" + ruleString + "'",
                       (labelText.indexOf( ruleString) >= 0), "not ");
    guiTest.assertTrueVerbose( "ConstraintNetworkView rule instance node (" +
                       ruleInstanceNode.getRuleInstance().getId().toString() +
                       ") label does not contain '" + keyString + "'",
                       (labelText.indexOf( keyString) >= 0), "not ");
  } // end planViz10CNet

 
  public static void planViz10TempExt( TemporalExtentView temporalExtentView, int stepNumber,
                                       PwPlanningSequence planSeq,  PwPartialPlan partialPlan,
                                       PlanWorks planWorks, JFCTestHelper helper,
                                       PlanWorksGUITest guiTest)
    throws Exception {
    ViewGenerics.raiseFrame( temporalExtentView.getViewFrame());
    // try{Thread.sleep(6000);}catch(Exception e){}
    TemporalNode baseTokenNode = null;
    TemporalNode mergedTokenNode = null;
    TemporalNode freeTokenNode = null;
    TemporalNode resTransactionNode = null;
    int numIntTokenNodes = 0, numResTransNodes = 0;
    Iterator temporalNodeItr = temporalExtentView.getTemporalNodeList().iterator();
    while (temporalNodeItr.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeItr.next();
      PwToken token = temporalNode.getToken();
      if (token instanceof PwResourceTransaction) {
        if (resTransactionNode == null) {
          resTransactionNode = temporalNode;
        }
        numResTransNodes++;
      } else {
        int slotTokenCnt = 0;
        if (token.getSlotId() != DbConstants.NO_ID) {
          slotTokenCnt =
            partialPlan.getSlot( token.getSlotId()).getTokenList().size();
        }
        if ((! token.isFree()) && token.isBaseToken() && (slotTokenCnt > 1) &&
            (baseTokenNode == null)) {
          baseTokenNode = temporalNode;
        } else if (token.isFree() && (freeTokenNode == null)) {
          freeTokenNode = temporalNode;
        }
        numIntTokenNodes++;
      }
    }
    // find mergedTokenNode
    temporalNodeItr = temporalExtentView.getTemporalNodeList().iterator();
    while (temporalNodeItr.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeItr.next();
      PwToken token = temporalNode.getToken();
      boolean isFree = token.isFree(), isBaseToken = token.isBaseToken();
      if ((! token.isFree()) && (! token.isBaseToken()) &&
          (mergedTokenNode == null) && (baseTokenNode != null) &&
          (baseTokenNode.getToken().getSlotId().equals
           (temporalNode.getToken().getSlotId()))) {
          mergedTokenNode = temporalNode;
          break;
      }
    }

    guiTest.assertNotNullVerbose( "Did not find TemporalExtentView baseToken TemporalNode",
                   baseTokenNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find TemporalExtentView mergedToken TemporalNode",
                   mergedTokenNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find TemporalExtentView freeToken TemporalNode",
                   freeTokenNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find TemporalExtentView resourceTransaction TemporalNode",
                   resTransactionNode, "not ");
    guiTest.assertTrueVerbose( "Number of partial plan interval tokens (" +
                       partialPlan.getTokenList().size() +
                       ") not equal number of TemporalExtent " +
                       "temporal nodes (" + (numIntTokenNodes + numResTransNodes) + ")",
                       (partialPlan.getTokenList().size() ==
                        (numIntTokenNodes + numResTransNodes)), "not ");
    guiTest.assertTrueVerbose( "mergedToken id=" + mergedTokenNode.getToken().getId() +
                       " is not a merged token of baseToken id=" +
                       baseTokenNode.getToken().getId(),
                       (mergedTokenNode.getToken().getSlotId().equals
                        ( baseTokenNode.getToken().getSlotId())), "not ");

//     System.err.println( "baseTokenNode.getToolTipText() " + baseTokenNode.getToolTipText());
//     System.err.println( "baseTokenNode.getText() " + baseTokenNode.getText());
    String toolTipText = baseTokenNode.getToolTipText();
    String labelText = baseTokenNode.getText();
    String predSubString = baseTokenNode.getPredicateName();
    String predParamSubString = predSubString + " (";
    String slotSubString = "slot key=" + baseTokenNode.getToken().getSlotId().toString();
    String tokenSubString = "key=" + baseTokenNode.getToken().getId().toString();
    guiTest.assertTrueVerbose( "TemporalExtentView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + predParamSubString + "'",
                       (toolTipText.indexOf( predParamSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TemporalExtentView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" + slotSubString + "'",
                       (toolTipText.indexOf( slotSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TemporalExtentView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") label does not contain '" + tokenSubString + "'",
                       (labelText.indexOf( tokenSubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TemporalExtentView token node (" +
                       baseTokenNode.getToken().getId().toString() +
                       ") label does not contain '" + predSubString + "'",
                       (labelText.indexOf( predSubString) >= 0), "not ");

//     System.err.println( "resTransactionNode.getToolTipText() " +
//                         resTransactionNode.getToolTipText());
//     System.err.println( "resTransactionNode.getText() " + resTransactionNode.getText());
    toolTipText = resTransactionNode.getToolTipText();
    labelText = resTransactionNode.getText();
    String nameSubString = resTransactionNode.getToken().getName();
    String resourceSubString = "key=" + resTransactionNode.getToken().getId().toString();
    guiTest.assertTrueVerbose( "TemporalExtentView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       nameSubString + "'", (toolTipText.indexOf( nameSubString) >= 0),
                       "not ");
    guiTest.assertTrueVerbose( "TemporalExtentView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       resourceSubString + "'", (labelText.indexOf( resourceSubString) >= 0),
                       "not ");
    guiTest.assertTrueVerbose( "TemporalExtentView resTransaction node (" +
                       resTransactionNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       nameSubString + "'", (labelText.indexOf( nameSubString) >= 0), "not ");
    
    Iterator markBridgeItr = baseTokenNode.getMarkAndBridgeList().iterator();
    int markBridgeCnt = 0;
    int earliestStartTime = 0, latestStartTime = 0, earliestEndTime = 0;
    int latestEndTime = 0, minDurationTime = 0, maxDurationTime = 0;
    while (markBridgeItr.hasNext()) {
      JGoStroke markOrBridge = (JGoStroke) markBridgeItr.next();
      if (markOrBridge instanceof TemporalNodeTimeMark) {
        TemporalNodeTimeMark timeMark = (TemporalNodeTimeMark) markOrBridge;
        if (timeMark.getType() == TemporalNode.EARLIEST_START_TIME_MARK) {
          earliestStartTime = Integer.parseInt( timeMark.getToolTipText());
        } else if (timeMark.getType() == TemporalNode.LATEST_START_TIME_MARK) {
          latestStartTime = Integer.parseInt( timeMark.getToolTipText());
        } else if (timeMark.getType() == TemporalNode.EARLIEST_END_TIME_MARK) {
          earliestEndTime = Integer.parseInt( timeMark.getToolTipText());
        } else if (timeMark.getType() == TemporalNode.LATEST_END_TIME_MARK) {
          latestEndTime = Integer.parseInt( timeMark.getToolTipText());
        }
        markBridgeCnt++;
      } else if (markOrBridge instanceof TemporalNodeDurationBridge) {
        TemporalNodeDurationBridge bridge = (TemporalNodeDurationBridge) markOrBridge;
        String durationString = bridge.getToolTipText();
        durationString = durationString.substring( 1, durationString.length() - 1);
        StringTokenizer durationTokens = new StringTokenizer( durationString, ",");
        minDurationTime = Integer.parseInt( durationTokens.nextToken().replaceAll( " ", ""));
        maxDurationTime = Integer.parseInt( durationTokens.nextToken().replaceAll( " ", ""));
        markBridgeCnt++;
      }
    }
    guiTest.assertTrueVerbose
      ( "TemporalExtentView baseTokenNode does not show four time marks " +
        " and duration bridge", (markBridgeCnt == 5), "not ");
    guiTest.assertTrueVerbose
      ( "TemporalExtentView baseTokenNode latestStartTime (" + latestStartTime +
        ") not >= earliestStartTime (" + earliestStartTime + ")",
        (latestStartTime >= earliestStartTime), "not ");
    guiTest.assertTrueVerbose
      ( "TemporalExtentView baseTokenNode latestEndTime (" + latestEndTime +
        ") not >= earliestEndTime (" + earliestEndTime + ")",
        (latestEndTime >= earliestEndTime), "not ");
    guiTest.assertTrueVerbose
      ( "TemporalExtentView baseTokenNode maxDurationTime (" + maxDurationTime +
        ") not consistent with " +
        "start/end times", (maxDurationTime == (latestEndTime - earliestStartTime)),
        "not ");
    guiTest.assertTrueVerbose
      ( "TemporalExtentView baseTokenNode minDurationTime (" + minDurationTime +
        ")  not consistent with " + "start/end times",
        (minDurationTime == (earliestEndTime - latestStartTime)), "not ");
  } // end planViz10TempExt

  public static void planViz10Timeline( TimelineView timelineView, int stepNumber,
                                        PwPlanningSequence planSeq,  PwPartialPlan partialPlan,
                                        PlanWorks planWorks, JFCTestHelper helper,
                                        PlanWorksGUITest guiTest)
    throws Exception {
    ViewGenerics.raiseFrame( timelineView.getViewFrame());
    // try{Thread.sleep(6000);}catch(Exception e){}
    TimelineViewTimelineNode theTimelineNode = null;
    TimelineTokenNode theFreeTokenNode = null;
    SlotNode theSlotNode = null;
    SlotNode theEmptySlotNode = null;
    int numTimelineNodes = 0, numSlotNodes = 0, numFreeTokenNodes = 0;
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      if (theTimelineNode == null) {
        theTimelineNode = timelineNode;
      }
      numTimelineNodes++;
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        if ((theSlotNode == null) && (slotNode.getSlot().getTokenList().size() > 0)) {
          theSlotNode = slotNode;
        }
        if ((theEmptySlotNode == null) && (slotNode.getSlot().getTokenList().size() == 0)) {
          theEmptySlotNode = slotNode;
        }
        numSlotNodes++;
      }
    }
    Iterator freeTokenNodeItr = timelineView.getFreeTokenNodeList().iterator();
    while (freeTokenNodeItr.hasNext()) {
      TimelineTokenNode freeTokenNode = (TimelineTokenNode) freeTokenNodeItr.next();
      if (theFreeTokenNode == null) {
        theFreeTokenNode = freeTokenNode;
      }
      numFreeTokenNodes++;
    }
    guiTest.assertNotNullVerbose( "Did not find TimelineView timelineNode (TimelineViewTimelineNode)",
                          theTimelineNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find TimelineView slotNode (SlotNode)",
                          theSlotNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find TimelineView emptySlotNode (SlotNode)",
                          theEmptySlotNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find TimelineView freeTokenNode (TimelineViewTokenNode)",
                          theFreeTokenNode, "not ");
    guiTest.assertTrueVerbose( "Number of partial plan timelines (" +
                       partialPlan.getTimelineList().size() +
                       ") not equal to number of TimelineView timeline nodes ("
                       + numTimelineNodes + ")",
                       (partialPlan.getTimelineList().size() == numTimelineNodes), "not ");
    guiTest.assertTrueVerbose( "Number of partial plan slots (" + partialPlan.getSlotList().size() +
                       ") not equal to number of TimelineView slot nodes (" + numSlotNodes + ")",
                       (partialPlan.getSlotList().size() == numSlotNodes), "not ");
    guiTest.assertTrueVerbose( "Number of partial plan free tokens (" +
                       partialPlan.getFreeTokenList().size() +
                       ") not equal to number of TimelineView free token nodes (" +
                       + numFreeTokenNodes + ")",
                       (partialPlan.getFreeTokenList().size() == numFreeTokenNodes), "not ");

//     System.err.println( "theTimelineNode labelText " + theTimelineNode.getText());
//     System.err.println( "theTimelineNode toolTipText " + theTimelineNode.getToolTipText());
    String labelText = theTimelineNode.getText();
    String toolTipText = theTimelineNode.getToolTipText();
    String nameString = theTimelineNode.getTimeline().getParent().getName() + " : " +
      theTimelineNode.getTimeline().getName();
    String nameSubString = theTimelineNode.getTimeline().getName();
    String keySubString = "key=" + theTimelineNode.getTimeline().getId().toString();
    guiTest.assertTrueVerbose( "TimelineView timelineNode (" +
                       theTimelineNode.getTimeline().getId().toString() +
                       ") label does not contain '" +
                       nameString + "'", (labelText.indexOf( nameString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TimelineView timelineNode (" +
                       theTimelineNode.getTimeline().getId().toString() +
                       ") label does not contain '" +
                       keySubString + "'", (labelText.indexOf( keySubString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TimelineView timelineNode (" +
                       theTimelineNode.getTimeline().getId().toString() +
                       ") tool tip does not contain '" +
                       nameSubString + "'", (toolTipText.indexOf( nameSubString) >= 0), "not ");

//     System.err.println( "theSlotNode labelText " + theSlotNode.getText());
//     System.err.println( "theSlotNode toolTipText " + theSlotNode.getToolTipText());
    labelText = theSlotNode.getText();
    toolTipText = theSlotNode.getToolTipText();
    nameSubString = theSlotNode.getPredicateName() + " (" +
      theSlotNode.getSlot().getTokenList().size() + ")";
    keySubString = "slot key=" + theSlotNode.getSlot().getId().toString();
    StringBuffer toolTipStrBuf = new StringBuffer( "");
    NodeGenerics.getSlotNodeToolTipText( theSlotNode.getSlot(), toolTipStrBuf);
    String toolTipStr = toolTipStrBuf.toString();
    toolTipStr = toolTipStr.replaceAll( "<html> ", "");
    toolTipStr = toolTipStr.replaceAll( " </html>", "");
    guiTest.assertTrueVerbose( "TimelineView slotNode (" +
                       theSlotNode.getSlot().getId().toString() +
                       ") label does not contain '" +
                       nameSubString + "'", (labelText.indexOf( nameSubString) >= 0),
                       "not ");
    guiTest.assertTrueVerbose( "TimelineView slotNode (" +
                       theSlotNode.getSlot().getId().toString() +
                       ") label does not contain '" +  keySubString + "'",
                       (labelText.indexOf( keySubString) >= 0), "not ");
     guiTest.assertTrueVerbose( "TimelineView slotNode (" +
                       theSlotNode.getSlot().getId().toString() +
                       ") tooltip does not contain '" +  toolTipStr + "'",
                       (toolTipText.indexOf( toolTipStr) >= 0), "not ");

//     System.err.println( "theEmptySlotNode labelText " + theEmptySlotNode.getText());
//     System.err.println( "theEmptySlotNode toolTipText " + theEmptySlotNode.getToolTipText());
    labelText = theEmptySlotNode.getText();
    toolTipText = theEmptySlotNode.getToolTipText();
    nameString = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
    keySubString = "key=" + theEmptySlotNode.getSlot().getId().toString();
    guiTest.assertTrueVerbose( "TimelineView empty slotNode (" +
                       theEmptySlotNode.getSlot().getId().toString() +
                       ") label does not contain '" +
                       nameString + "'", (labelText.indexOf( nameString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TimelineView empty slotNode (" +
                       theEmptySlotNode.getSlot().getId().toString() +
                       ") label does not contain '" +  keySubString + "'",
                       (labelText.indexOf( keySubString) >= 0), "not ");
    // no tool tip

//     System.err.println( "theFreeTokenNode labelText " + theFreeTokenNode.getText());
//     System.err.println( "theFreeTokenNode toolTipText " + theFreeTokenNode.getToolTipText());
    labelText = theFreeTokenNode.getText();
    toolTipText = theFreeTokenNode.getToolTipText();
    nameString = theFreeTokenNode.getToken().getPredicateName();
    keySubString = "key=" + theFreeTokenNode.getToken().getId().toString();
    toolTipStr = theFreeTokenNode.getToken().toString();
    guiTest.assertTrueVerbose( "TimelineView freeTokenNode (" +
                       theFreeTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       nameString + "'", (labelText.indexOf( nameString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TimelineView freeTokenNode (" +
                       theFreeTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +  keySubString + "'",
                       (labelText.indexOf( keySubString) >= 0), "not ");
     guiTest.assertTrueVerbose( "TimelineView freeTokenNode (" +
                       theFreeTokenNode.getToken().getId().toString() +
                       ") tooltip does not contain '" +  toolTipStr + "'",
                       (toolTipText.indexOf( toolTipStr) >= 0), "not ");
  } // end planViz10Timeline


  private static void planViz10TokNet( TokenNetworkView tokenNetworkView, int stepNumber,
                                       PwPlanningSequence planSeq,  PwPartialPlan partialPlan,
                                       PlanWorks planWorks, JFCTestHelper helper,
                                       PlanWorksGUITest guiTest)
    throws Exception {
    ViewGenerics.raiseFrame( tokenNetworkView.getViewFrame());
    // try{Thread.sleep(6000);}catch(Exception e){}
    TokenNetworkTokenNode slottedTokenNode = null, freeTokenNode = null, resTransactionNode = null;
    RuleInstanceNode ruleInstanceNode = null;
    int numSlottedTokenNodes = 0, numFreeTokenNodes = 0, numResTransactionNodes = 0;
    int numRuleInstanceNodes = 0;
    Iterator tokenNodeKeyItr = tokenNetworkView.getTokenNodeKeyList().iterator();
    while (tokenNodeKeyItr.hasNext()) {
      TokenNetworkTokenNode tokenNode =
        tokenNetworkView.getTokenNode( (Integer) tokenNodeKeyItr.next());
      if (tokenNode.getToken() instanceof PwResourceTransaction) {
        if (resTransactionNode == null) {
          resTransactionNode = tokenNode;
        }
        numResTransactionNodes++;
      } else if (tokenNode.getToken() instanceof PwToken) {
        PwToken token = (PwToken) tokenNode.getToken();
        if (token.isSlotted()) {
          if (slottedTokenNode == null) {
            slottedTokenNode = tokenNode;
          }
          numSlottedTokenNodes++;
        }
        if (token.isFree()) {
          if (freeTokenNode == null) {
            freeTokenNode = tokenNode;
          }
          numFreeTokenNodes++;
        }
      }
    }

    Iterator ruleInstKeyItr = tokenNetworkView.getRuleInstanceNodeKeyList().iterator();
    while (ruleInstKeyItr.hasNext()) {
      RuleInstanceNode ruleInstNode =
        tokenNetworkView.getRuleInstanceNode( (Integer) ruleInstKeyItr.next());
        if (ruleInstanceNode == null) {
          ruleInstanceNode = ruleInstNode;
        }
        numRuleInstanceNodes++;
    }
    // try{Thread.sleep(20000);}catch(Exception e){}

    List rootTokens = new ArrayList();
    Iterator tokenIterator = partialPlan.getTokenList().iterator();
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      Integer masterTokenId = partialPlan.getMasterTokenId( token.getId());
      if (masterTokenId == null) {
        rootTokens.add( token);
      }
    }

    System.err.println( "numSlottedTokenNodes " + numSlottedTokenNodes +
                        "numFreeTokenNodes " + numFreeTokenNodes +
                        " numResTransactionNodes " + numResTransactionNodes);
    System.err.println( "pp tokenCnt " + partialPlan.getTokenList().size() +
                        " master tokenCnt " + rootTokens.size() +
                        " pp resTrans " + partialPlan.getResTransactionList().size());
    guiTest.assertNotNullVerbose( "Did not find TokenNetworkView slotted TokenNode (TokenNode)",
                          slottedTokenNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find TokenNetworkView free TokenNode (TokenNode)",
                          freeTokenNode, "not ");
//     guiTest.assertNotNullVerbose( "Did not find TokenNetworkView resTransactionNode (TokenNode)",
//                           resTransactionNode, "not ");
    guiTest.assertNotNullVerbose( "Did not find TokenNetworkView ruleInstanceNode (RuleInstanceNode)",
                          ruleInstanceNode, "not ");
  // guiTest.assertTrueVerbose( "Number of partial plan interval tokens and resource transactions (" +
  //                     partialPlan.getTokenList().size() +
    guiTest.assertTrueVerbose( "Number of partial plan root token nodes (" + rootTokens.size() +
                       ") not equal to number of TokenNetworkView slotted token, " +
                       "free token, and resource transaction nodes (" +
                       (numSlottedTokenNodes + numFreeTokenNodes + numResTransactionNodes) +
                       // ")", (partialPlan.getTokenList().size() ==
                       ")", (rootTokens.size() ==
                             (numSlottedTokenNodes + numFreeTokenNodes +
                              numResTransactionNodes)), "not ");
    // token network view is now incremental -> just root nodes are rendered
//     guiTest.assertTrueVerbose
//       ( "Number of partial plan slotted interval tokens (" +
//         (partialPlan.getTokenList().size() - partialPlan.getFreeTokenList().size() -
//          partialPlan.getResTransactionList().size()) +
//         ") not equal to number of TokenNetworkView slotted interval token nodes (" +
//         numSlottedTokenNodes + ")", ((partialPlan.getTokenList().size() -
//                                       partialPlan.getFreeTokenList().size() -
//                                       partialPlan.getResTransactionList().size()) ==
//                                      numSlottedTokenNodes), "not ");
//     guiTest.assertTrueVerbose
//       ( "Number of partial plan free interval tokens (" +
//         partialPlan.getFreeTokenList().size() +
//         ") not equal to number of TokenNetworkView slotted interval token nodes (" +
//         numFreeTokenNodes + ")", (partialPlan.getFreeTokenList().size() ==
//                                      numFreeTokenNodes), "not ");
//     guiTest.assertTrueVerbose( "Number of partial plan resource transactions (" +
//                        partialPlan.getResTransactionList().size() +
//                        ") not equal to number of TokenNetworkView resource transaction nodes (" +
//                        numResTransactionNodes + ")",
//                        partialPlan.getResTransactionList().size() ==
//                        numResTransactionNodes, "not ");
//     guiTest.assertTrueVerbose( "Number of partial plan rule instances (" +
//                        partialPlan.getRuleInstanceList().size() +
//                        ") not equal to number of TokenNetworkView rule instance nodes (" +
//                        numRuleInstanceNodes + ")",
//                        partialPlan.getRuleInstanceList().size() ==
//                        numRuleInstanceNodes, "not ");
 
    System.err.println( "slottedTokenNode labelText " + slottedTokenNode.getText());
    System.err.println( "slottedTokenNode toolTipText " + slottedTokenNode.getToolTipText());
    String labelText = slottedTokenNode.getText();
    String toolTipText = slottedTokenNode.getToolTipText();
    String predArgsString = slottedTokenNode.getToken().toString();
    String predString = slottedTokenNode.getToken().getPredicateName();
    String keyString = "key=" + slottedTokenNode.getToken().getId().toString();
    String slotKeyString = "slot key=" + slottedTokenNode.getToken().getSlotId().toString();
    guiTest.assertTrueVerbose( "TokenNetworkView slotted token node (" +
                       slottedTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       predString + "'", (labelText.indexOf( predString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TokenNetworkView slotted token node (" +
                       slottedTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       keyString + "'", (labelText.indexOf( keyString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TokenNetworkView slotted token node (" +
                       slottedTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       predArgsString + "'", (toolTipText.indexOf( predArgsString) >= 0),
                       "not ");
//     guiTest.assertTrueVerbose( "TokenNetworkView slotted token node (" +
//                        slottedTokenNode.getToken().getId().toString() +
//                        ") tool tip does not contain '" +
//                        slotKeyString + "'", (toolTipText.indexOf( slotKeyString) >= 0),
//                        "not ");

    System.err.println( "freeTokenNode labelText " + freeTokenNode.getText());
    System.err.println( "freeTokenNode toolTipText " + freeTokenNode.getToolTipText());
    labelText = freeTokenNode.getText();
    toolTipText = freeTokenNode.getToolTipText();
    predArgsString = freeTokenNode.getToken().toString();
    predString = freeTokenNode.getToken().getPredicateName();
    keyString = "key=" + freeTokenNode.getToken().getId().toString();
    slotKeyString = "slot key=";
    guiTest.assertTrueVerbose( "TokenNetworkView free token node (" +
                       freeTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       predString + "'", (labelText.indexOf( predString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TokenNetworkView free token node (" +
                       freeTokenNode.getToken().getId().toString() +
                       ") label does not contain '" +
                       keyString + "'", (labelText.indexOf( keyString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TokenNetworkView free token node (" +
                       freeTokenNode.getToken().getId().toString() +
                       ") tool tip does not contain '" +
                       predArgsString + "'", (toolTipText.indexOf( predArgsString) >= 0),
                       "not ");
//     guiTest.assertFalseVerbose( "TokenNetworkView free token node (" +
//                        freeTokenNode.getToken().getId().toString() +
//                        ") tool tip does not contain '" +
//                        slotKeyString + "'", (toolTipText.indexOf( slotKeyString) >= 0),
//                        "not ");

//     System.err.println( "resTransactionNode labelText " + resTransactionNode.getText());
//     System.err.println( "resTransactionNode toolTipText " + resTransactionNode.getToolTipText());
//     labelText = resTransactionNode.getText();
//     toolTipText = resTransactionNode.getToolTipText();
//     predArgsString = resTransactionNode.getToken().toString();
//     predString = resTransactionNode.getToken().getPredicateName();
//     keyString = "key=" + resTransactionNode.getToken().getId().toString();
//     slotKeyString = "slot key=";
//     guiTest.assertTrueVerbose( "TokenNetworkView resource transaction node (" +
//                        resTransactionNode.getToken().getId().toString() +
//                        ") label does not contain '" +
//                        predString + "'", (labelText.indexOf( predString) >= 0), "not ");
//     guiTest.assertTrueVerbose( "TokenNetworkView resource transaction node (" +
//                        resTransactionNode.getToken().getId().toString() +
//                        ") label does not contain '" +
//                        keyString + "'", (labelText.indexOf( keyString) >= 0), "not ");
//     guiTest.assertTrueVerbose( "TokenNetworkView resource transaction node (" +
//                        resTransactionNode.getToken().getId().toString() +
//                        ") tool tip does not contain '" +
//                        predArgsString + "'", (toolTipText.indexOf( predArgsString) >= 0),
//                        "not ");
//     guiTest.assertFalseVerbose( "TokenNetworkView resource transaction node (" +
//                        resTransactionNode.getToken().getId().toString() +
//                        ") tool tip does not contain '" +
//                        slotKeyString + "'", (toolTipText.indexOf( slotKeyString) >= 0),
//                        "not ");

    System.err.println( "ruleInstanceNode labelText " + ruleInstanceNode.getText());
    System.err.println( "ruleInstanceNode toolTipText " + ruleInstanceNode.getToolTipText());
    labelText = ruleInstanceNode.getText();
    toolTipText = ruleInstanceNode.getToolTipText();
    keyString = "key=" + ruleInstanceNode.getRuleInstance().getId().toString();
    String ruleString = "rule ";
    guiTest.assertTrueVerbose( "TokenNetworkView rule instances node (" +
                       ruleInstanceNode.getRuleInstance().getId().toString() +
                       ") label does not contain '" + ruleString + "'",
                       (labelText.indexOf( ruleString) >= 0), "not ");
    guiTest.assertTrueVerbose( "TokenNetworkView rule instances node (" +
                       ruleInstanceNode.getRuleInstance().getId().toString() +
                       ") label does not contain '" + keyString + "'",
                       (labelText.indexOf( keyString) >= 0), "not ");
  } // end planViz10TokNet

//   private static void planViz10DBTrans( DBTransactionView dbTransactionView, int stepNumber,
//                                         PwPlanningSequence planSeq,  PwPartialPlan partialPlan,
//                                         PlanWorks planWorks, JFCTestHelper helper,
//                                         PlanWorksGUITest guiTest)
//     throws Exception {
//     ViewGenerics.raiseFrame( dbTransactionView.getViewFrame());

//     // try{Thread.sleep(6000);}catch(Exception e){}

//     List transactionList = planSeq.getTransactionsList( partialPlan.getId());
//     int numTransactions = transactionList.size();
//     int numTransactionEntries = dbTransactionView.getDBTransactionList().size();
//     guiTest.assertTrueVerbose
//       ( "Number of partial plan step " + stepNumber + " db transactions (" + numTransactions +
//         ") not equal to number of DBTransactionView entries (" +
//         numTransactionEntries + ")", (numTransactions == numTransactionEntries), "not ");
//     List transactionNameList = SQLDB.queryTransactionNameList();
//     TableModel tableModel = ((DBTransactionTable) dbTransactionView.getDBTransactionTable()).
//       getTableSorter().getTableModel();
//     String transName = null, fieldObjName = null;
//     String variableNamePrefix = DbConstants.VARIABLE_ALL_TYPES.substring
//       ( 0, DbConstants.VARIABLE_ALL_TYPES.length() - 1);
//     List decisionList = null;
//     try {
//       decisionList = planSeq.getOpenDecisionsForStep( stepNumber);
//     } catch ( ResourceNotFoundException rnfExcep) {
//       int index = rnfExcep.getMessage().indexOf( ":");
//       JOptionPane.showMessageDialog
//         (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
//          "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
//       System.err.println( rnfExcep);
//       rnfExcep.printStackTrace();
//     }
//     int columnCnt = tableModel.getColumnCount() - 1; // last column is empty
//     for (int rowIndx = 0; rowIndx < tableModel.getRowCount(); rowIndx++) {
//       for (int colIndx = 0; colIndx < columnCnt; colIndx++) {
//         String columnName = tableModel.getColumnName( colIndx);
// //         System.err.println( "rowIndx " + rowIndx + " colIndx " + colIndx + " columnName " +
// //                            columnName + " tableModel " + tableModel);
//         String transField =
//           ((String) tableModel.getValueAt( rowIndx, colIndx)).replaceAll( " ", "");
//         if (columnName.equals( ViewConstants.DB_TRANSACTION_KEY_HEADER)) {
//           Integer fieldTransId = new Integer( Integer.parseInt( transField));
//           PwDBTransaction ppTrans =
//             (PwDBTransaction) CollectionUtils.findFirst
//             ( new PlanWorksGUITest.PwDBTransactionFunctor( fieldTransId), transactionList);
// //           guiTest.assertNotNullVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
// //                                 ViewConstants.DB_TRANSACTION_KEY_HEADER + "' " + fieldTransId +
// //                                 " not found", ppTrans, " not");
//           guiTest.assertNotNull( "Transaction entry " + (rowIndx + 1) + " '" +
//                          ViewConstants.DB_TRANSACTION_KEY_HEADER + "' " + fieldTransId +
//                          " not found", ppTrans);
//         } else if (columnName.replaceAll( " ", "").equals
//                    ( ViewConstants.DB_TRANSACTION_NAME_HEADER)) {
//           transName = transField;
//           boolean isValidName = transactionNameList.contains( transName);
// //           guiTest.assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
// //                              ViewConstants.DB_TRANSACTION_NAME_HEADER + "' '" +
// //                              transName + "' not found", isValidName, " not");
//           guiTest.assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
//                       ViewConstants.DB_TRANSACTION_NAME_HEADER + "' '" +
//                       transName + "' not found", isValidName);
//         } else if (columnName.equals( ViewConstants.DB_TRANSACTION_SOURCE_HEADER)) {
//           boolean isValidSource =
//             DbConstants.SOURCE_USER.equals( transField) ||
//             DbConstants.SOURCE_SYSTEM.equals( transField) ||
//             DbConstants.SOURCE_UNKNOWN.equals( transField);
// //           guiTest.assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
// //                              ViewConstants.DB_TRANSACTION_SOURCE_HEADER + "' '" +
// //                              transField + "' not found", isValidSource, " not");
//           guiTest.assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
//                       ViewConstants.DB_TRANSACTION_SOURCE_HEADER + "' '" +
//                       transField + "' not found", isValidSource);
//         } else if (columnName.equals( ViewConstants.DB_TRANSACTION_ENTITY_KEY_HEADER)) {
//           Integer objectId = new Integer( Integer.parseInt( transField));
//           PwToken tokenObject = partialPlan.getToken( objectId);
//           PwConstraint constraintObject = partialPlan.getConstraint( objectId);
//           PwVariable variableObject = partialPlan.getVariable( objectId);
//           PwDecision decisionObject = null;
//           Iterator decisionItr = decisionList.iterator();
//           while (decisionItr.hasNext()) {
//             PwDecision decision = (PwDecision) decisionItr.next();
//             if (decision.getId().equals( objectId)) {
//               decisionObject = decision;
//             }
//           }
// //           guiTest.assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
// //                              ViewConstants.DB_TRANSACTION_ENTITY_KEY_HEADER +
// //                              "' '" + objectId + "' not found",
// //                              ((tokenObject != null) || (constraintObject != null) ||
// //                               (variableObject != null) || (decisionObject != null)),
// //                              " not");
//           guiTest.assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
//                       ViewConstants.DB_TRANSACTION_ENTITY_KEY_HEADER +
//                       "' '" + objectId + "' not found",
//                       ((tokenObject != null) || (constraintObject != null) ||
//                        (variableObject != null) || (decisionObject != null)));

//         } else if (columnName.equals( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER)) {
//           int fieldStepNumber = Integer.parseInt( transField);
// //           guiTest.assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
// //                              ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER
// //                              + "' '" + fieldStepNumber +
// //                              "' not found", (fieldStepNumber == stepNumber), " not");
//           guiTest.assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
//                       ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER + "' '" + fieldStepNumber +
//                       "' not found", (fieldStepNumber == stepNumber));
//         } else if (columnName.equals( ViewConstants.DB_TRANSACTION_PARENT_HEADER)) {
//           if (transName.indexOf( variableNamePrefix) >= 0) {
// //             guiTest.assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
// //                                ViewConstants.DB_TRANSACTION_PARENT_HEADER +
// //                                "' '" + transField +
// //                                "' not non-empty", (! transField.equals( "")), " not");
//             guiTest.assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
//                         ViewConstants.DB_TRANSACTION_PARENT_HEADER + "' '" + transField +
//                         "' not non-empty", (! transField.equals( "")));
//           } else {
// //             guiTest.assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
// //                                ViewConstants.DB_TRANSACTION_PARENT_HEADER +
// //                                "' '" + transField +
// //                                "' not empty", transField.equals( ""), " not");
//             guiTest.assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
//                         ViewConstants.DB_TRANSACTION_PARENT_HEADER + "' '" + transField +
//                         "' not empty", transField.equals( ""));
//           }
//         } else if (columnName.equals( ViewConstants.DB_TRANSACTION_ENTITY_NAME_HEADER)) {
//           fieldObjName = transField;
//         } else if (columnName.equals( ViewConstants.DB_TRANSACTION_PARAMETER_HEADER)) {
//           String fieldParamName = transField;
//           if ((transName.indexOf( variableNamePrefix) >= 0) &&
//               fieldObjName.equals( DbConstants.PARAMETER_VAR)) {
// //             guiTest.assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
// //                                ViewConstants.DB_TRANSACTION_PARAMETER_HEADER + 
// //                                "' '" + fieldParamName +
// //                                "' not non-empty", (! fieldParamName.equals( "")), " not");
//             guiTest.assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
//                         ViewConstants.DB_TRANSACTION_PARAMETER_HEADER + "' '" +
//                         fieldParamName + "' not non-empty", (! fieldParamName.equals( "")));
//           } else {
// //             guiTest.assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
// //                                ViewConstants.DB_TRANSACTION_PARAMETER_HEADER + 
// //                                "' '" + fieldParamName +
// //                                "' not empty", fieldParamName.equals( ""), " not");
//             guiTest.assertTrue( "Transaction entry " + (rowIndx + 1) + " '" +
//                         ViewConstants.DB_TRANSACTION_PARAMETER_HEADER + "' '" +
//                         fieldParamName + "' not empty", fieldParamName.equals( ""));
//           }
//         } else {
//           guiTest.assertTrueVerbose( "Transaction entry " + (rowIndx + 1) + " '" +
//                              columnName + "' not handled", false, " not");
//         }
//       }
//     }
//   } // end planViz10DBTrans

} // end class PlanWorksGUITest10
