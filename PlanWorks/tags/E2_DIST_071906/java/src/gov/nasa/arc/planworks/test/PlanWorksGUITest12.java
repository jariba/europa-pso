//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest12.java,v 1.5 2005-11-10 01:22:10 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
//import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;    
import gov.nasa.arc.planworks.viz.partialPlan.decision.DecisionView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.SlotNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineViewTimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecWindow;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.GroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.LogicComboBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.MergeBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.NegationCheckBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.PredicateBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.PredicateGroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TimeIntervalBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TimeIntervalGroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TimelineBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TimelineGroupBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.TokenTypeBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.UniqueKeyBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.UniqueKeyGroupBox;

public class PlanWorksGUITest12 {

  private static ConstraintNetworkView constraintNetworkView;
  private static TemporalExtentView temporalExtentView;
  private static TimelineView timelineView;
  private static TokenNetworkView tokenNetworkView;

  private PlanWorksGUITest12() {
  }

  public static void planViz12( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
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
   guiTest.viewListenerListWait( PlanWorksGUITest.DB_TRANSACTION_VIEW_INDEX, viewListenerList);
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

   ViewSet viewSet = PlanWorks.getPlanWorks().getViewManager().getViewSet( partialPlan);
//    DBTransactionView dbTransactionView =
//      (DBTransactionView) PWTestHelper.getPartialPlanView
//      ( ViewConstants.DB_TRANSACTION_VIEW, viewNameSuffix, guiTest);
//    dbTransactionView.getViewFrame().setClosed( true);
   DecisionView decisionView =
     (DecisionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.DECISION_VIEW, viewNameSuffix, guiTest);
   decisionView.getViewFrame().setClosed( true);
   ResourceProfileView resourceProfileView =
     (ResourceProfileView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_PROFILE_VIEW, viewNameSuffix, guiTest);
   resourceProfileView.getViewFrame().setClosed( true);
   ResourceTransactionView resourceTransactionView =
     (ResourceTransactionView) PWTestHelper.getPartialPlanView
     ( ViewConstants.RESOURCE_TRANSACTION_VIEW, viewNameSuffix, guiTest);
   resourceTransactionView.getViewFrame().setClosed( true);
   JMenuItem tileItem =
     PWTestHelper.findMenuItem( PlanWorks.WINDOW_MENU, PlanWorks.TILE_WINDOWS_MENU_ITEM,
                                helper, guiTest);
   guiTest.assertNotNullVerbose( "'Window->Tile Windows' not found:", tileItem, "not ");
   helper.enterClickAndLeave( new MouseEventData( guiTest, tileItem));
   guiTest.flushAWT(); guiTest.awtSleep();

   ContentSpecWindow contentSpecWindow =
     PWTestHelper.getContentSpecWindow( viewNameSuffix, helper, guiTest);
   constraintNetworkView =
     (ConstraintNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.CONSTRAINT_NETWORK_VIEW, viewNameSuffix, guiTest);
   temporalExtentView = (TemporalExtentView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TEMPORAL_EXTENT_VIEW, viewNameSuffix, guiTest);
   timelineView = (TimelineView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, guiTest);
   tokenNetworkView = (TokenNetworkView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TOKEN_NETWORK_VIEW, viewNameSuffix, guiTest);

   // since Token Network is incremental, do "Find Key" to ensure that all the
   // tokens are rendered
   boolean isByKey = true;
   Iterator temporalNodeItr = temporalExtentView.getTemporalNodeList().iterator();
   while (temporalNodeItr.hasNext()) {
     TemporalNode temporalNode = (TemporalNode) temporalNodeItr.next();
     PwToken token = temporalNode.getToken();
     tokenNetworkView.findAndSelectNode( token.getId(), isByKey);
     guiTest.flushAWT(); guiTest.awtSleep();
   }

   validateConstraintsOpen( constraintNetworkView, helper, guiTest);

   confirmSpecForViews( contentSpecWindow, viewNameSuffix, planWorks, helper, guiTest);

   // try{Thread.sleep(4000);}catch(Exception e){}

   PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);

   System.err.println( "\nPLANVIZ_12 COMPLETED\n");
  } // end planViz12


  private static void validateConstraintsOpen( ConstraintNetworkView constraintNetworkView,
                                               JFCTestHelper helper,
                                               PlanWorksGUITest guiTest)
    throws Exception {
    List containerNodeList = constraintNetworkView.getContainerNodeList();
    ConstraintNetworkTokenNode t1483 = null;
    Iterator containerNodeItr = containerNodeList.iterator();
    while (containerNodeItr.hasNext()) {
      VariableContainerNode containNode = (VariableContainerNode) containerNodeItr.next();
      // System.err.println( "containNode " + containNode.getClass().getName());
      if (containNode instanceof ConstraintNetworkTokenNode) {
        ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) containNode;
        if (((PwToken) tokenNode.getContainer()).getId().equals( new Integer( 1483))) {
          t1483 = tokenNode;
          break;
        }
      }
    }
    guiTest.assertTrueVerbose( "tokenNode id=1483 not found", (t1483 != null), "not ");
    t1483.doMouseClick( MouseEvent.BUTTON1_MASK,
                        new Point( (int) t1483.getLocation().getX(),
                                   (int) t1483.getLocation().getY()),
                        new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();

    List variableNodeList = constraintNetworkView.getVariableNodeList();
    VariableNode v1487 = null;
    Iterator variableNodeItr = variableNodeList.iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (variableNode.getVariable().getId().equals( new Integer( 1487)) &&
          variableNode.isVisible()) {
        v1487 = variableNode;
        break;
      }
    }
    guiTest.assertTrueVerbose( "variableNode id=1487 not found", (v1487 != null), "not ");
    v1487.doMouseClick( MouseEvent.BUTTON1_MASK,
                        new Point( (int) v1487.getLocation().getX(),
                                   (int) v1487.getLocation().getY()),
                        new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();

    List constraintNodeList = constraintNetworkView.getConstraintNodeList();
    ConstraintNode c1502 = null;
    Iterator constraintNodeItr = constraintNodeList.iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      if (constraintNode.getConstraint().getId().equals( new Integer( 1502)) &&
          constraintNode.isVisible()) {
        c1502 = constraintNode;
        break;
      }
    }
    guiTest.assertTrueVerbose( "constraintNode id=1502 not found", (c1502 != null), "not ");
    c1502.doMouseClick( MouseEvent.BUTTON1_MASK,
                       new Point( (int) c1502.getLocation().getX(),
                                  (int) c1502.getLocation().getY()),
                       new Point( 0, 0), constraintNetworkView.getJGoView());
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end validateConstraintsOpen


  private static void confirmSpecForViews( ContentSpecWindow contentSpecWindow,
                                           String viewNameSuffix, PlanWorks planWorks,
                                           JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {
    JButton activateFilterButton = null;
    JButton resetFilterButton = null;
    GroupBox timelineGroup = null;
    GroupBox predicateGroup = null;
    GroupBox timeIntervalGroup = null;
    MergeBox mergeBox = null;
    TokenTypeBox tokenTypeBox = null;
    UniqueKeyGroupBox uniqueKeyGroupBox = null;
    for(int i = 0; i < contentSpecWindow.getComponentCount(); i++) {
      if(contentSpecWindow.getComponent(i) instanceof TimelineGroupBox) {
        timelineGroup = (GroupBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof PredicateGroupBox) {
        predicateGroup = (GroupBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof TimeIntervalGroupBox) {
        timeIntervalGroup = (GroupBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof MergeBox) {
        mergeBox = (MergeBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof TokenTypeBox) {
        tokenTypeBox = (TokenTypeBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof UniqueKeyGroupBox) {
        uniqueKeyGroupBox = (UniqueKeyGroupBox) contentSpecWindow.getComponent(i);
      }
      else if(contentSpecWindow.getComponent(i) instanceof JPanel) {
        JPanel panel = (JPanel)contentSpecWindow.getComponent(i);
        for(int j = 0; j < panel.getComponentCount(); j++) {
          if(panel.getComponent(j) instanceof JButton) {
            if(((JButton)panel.getComponent(j)).getText().equals("Apply Filter")) {
              activateFilterButton = (JButton) panel.getComponent(j);
            }
            else if(((JButton)panel.getComponent(j)).getText().equals("Reset Filter")) {
              resetFilterButton = (JButton) panel.getComponent(j);
            }
          }
        }
      }
    }
    guiTest.assertNotNullVerbose("Did not find \"Apply Filter\" button.",
                                 activateFilterButton, "not ");
    guiTest.assertNotNullVerbose("Did not find \"Reset Filter\" button.",
                                 resetFilterButton, "not ");
    guiTest.assertNotNullVerbose("Did not find Timeline GroupBox.", timelineGroup, "not ");
    guiTest.assertNotNullVerbose("Did not find Predicate GroupBox.", predicateGroup, "not ");
    guiTest.assertNotNullVerbose("Did not find Time Interval GroupBox.",
                                 timeIntervalGroup, "not ");
    guiTest.assertNotNullVerbose("Did not find Merge Box.", mergeBox, "not ");
    guiTest.assertNotNullVerbose("Did not find Token Type Box.", tokenTypeBox, "not ");
    guiTest.assertNotNullVerbose("Did not find Unique Key GroupBox.",
                                 uniqueKeyGroupBox, "not ");

    // try{Thread.sleep(4000);}catch(Exception e){}

    System.err.println( "Method 1 -------------");
    // appply predicate 
    int predicateGroupIndx = 0;
    Object[] predicateGroupBoxes = getPredicateGroupBoxes( predicateGroup, predicateGroupIndx);
    JComboBox predKeyBox0 = (JComboBox) predicateGroupBoxes[0];
    NegationCheckBox predNegationBox0 = (NegationCheckBox) predicateGroupBoxes[1];
    LogicComboBox predLogicBox0 = (LogicComboBox) predicateGroupBoxes[2];

    guiTest.assertNotNullVerbose("Did not find predicate field.", predKeyBox0, "not ");
    guiTest.assertNotNullVerbose("Did not find negation check box.", predNegationBox0, "not ");
    int predicateNodeIndex = 1; // predicate1253
    predKeyBox0.setSelectedIndex( predicateNodeIndex);
    System.err.println( "selected predicate name " + predKeyBox0.getSelectedItem());
    String selectedPredicateName = (String) predKeyBox0.getSelectedItem();
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();

    List selectedPredicateNameList = new ArrayList();
    selectedPredicateNameList.add( selectedPredicateName);
    boolean isToBeFound = true;
    validateTimelineViewPredicate( selectedPredicateNameList, timelineView, isToBeFound,
                                   planWorks, helper, guiTest);
    validateConstraintNetPredicate( selectedPredicateNameList, isToBeFound,
                                    planWorks, helper, guiTest);
    validateTemporalExtentPredicate( selectedPredicateNameList, isToBeFound,
                                    planWorks, helper, guiTest);
    validateTokenNetworkPredicate( selectedPredicateNameList, isToBeFound,
                                   planWorks, helper, guiTest);

    System.err.println( "Method 2 -------------");
    // not predicate
    predNegationBox0.setSelected(true);
    System.err.println( "selected NOT 'selected predicate name'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();

    isToBeFound = false;
    validateTimelineViewPredicate( selectedPredicateNameList, timelineView, isToBeFound,
                                   planWorks, helper, guiTest);
    validateConstraintNetPredicate( selectedPredicateNameList,isToBeFound,
                                    planWorks, helper, guiTest);
    validateTemporalExtentPredicate( selectedPredicateNameList, isToBeFound,
                                    planWorks, helper, guiTest);
    validateTokenNetworkPredicate( selectedPredicateNameList, isToBeFound,
                                   planWorks, helper, guiTest);

    System.err.println( "Method 3 -------------");
    predicateGroupIndx = 1;
    predicateGroupBoxes = getPredicateGroupBoxes( predicateGroup, predicateGroupIndx);
    JComboBox predKeyBox1 = (JComboBox) predicateGroupBoxes[0];
    NegationCheckBox predNegationBox1 = (NegationCheckBox) predicateGroupBoxes[1];
    LogicComboBox predLogicBox1 = (LogicComboBox) predicateGroupBoxes[2];
    predLogicBox1.setSelectedIndex( 1);
    System.err.println( "selected predLogicBox1 " + predLogicBox1.getSelectedItem());
    predNegationBox1.setSelected( true);
    System.err.println( "selected NOT 'selected predicate name 1'");
    predicateNodeIndex = 5; // predicate1335
    predKeyBox1.setSelectedIndex( predicateNodeIndex);
    System.err.println( "selected predicate name 1 " + predKeyBox1.getSelectedItem());
    String selectedPredicateName1 = (String) predKeyBox1.getSelectedItem();
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();

    isToBeFound = false;
    selectedPredicateNameList.add( selectedPredicateName1);
    validateTimelineViewPredicate( selectedPredicateNameList, timelineView, isToBeFound,
                                   planWorks, helper, guiTest);
    validateConstraintNetPredicate( selectedPredicateNameList,isToBeFound,
                                    planWorks, helper, guiTest);
    validateTemporalExtentPredicate( selectedPredicateNameList, isToBeFound,
                                    planWorks, helper, guiTest);
    validateTokenNetworkPredicate( selectedPredicateNameList, isToBeFound,
                                   planWorks, helper, guiTest);

    System.err.println( "Method 4 -------------");
    predLogicBox1.setSelectedIndex( 2);
    System.err.println( "selected predLogicBox1 " + predLogicBox1.getSelectedItem());
    predNegationBox0.setSelected( false);
    System.err.println( "inverted NOT 'selected predicate name 0'");
    predNegationBox1.setSelected( false);
    System.err.println( "inverted NOT 'selected predicate name 1'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();
    isToBeFound = true;
    validateTimelineViewPredicate( selectedPredicateNameList, timelineView, isToBeFound,
                                   planWorks, helper, guiTest);
    validateConstraintNetPredicate( selectedPredicateNameList,isToBeFound,
                                    planWorks, helper, guiTest);
    validateTemporalExtentPredicate( selectedPredicateNameList, isToBeFound,
                                    planWorks, helper, guiTest);
    validateTokenNetworkPredicate( selectedPredicateNameList, isToBeFound,
                                   planWorks, helper, guiTest);

    System.err.println( "Method 5 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(guiTest, resetFilterButton));
    System.err.println( "Reset Filter");
    guiTest.flushAWT(); guiTest.awtSleep();

    predicateGroupIndx = 0;
    predicateGroupBoxes = getPredicateGroupBoxes( predicateGroup, predicateGroupIndx);
    predKeyBox0 = (JComboBox) predicateGroupBoxes[0];
    predNegationBox0 = (NegationCheckBox) predicateGroupBoxes[1];
    predLogicBox0 = (LogicComboBox) predicateGroupBoxes[2];
    // System.err.println( "predKeyBox0.getSelectedItem() " + predKeyBox0.getSelectedItem());
    guiTest.assertTrueVerbose("Reset Filter did not reset predicate text box",
                      (predKeyBox0.getSelectedItem().equals( "")), "not ");
    guiTest.assertTrueVerbose("Reset Filter did not reset predicate negation check box",
                      (! predNegationBox0.isSelected()), "not ");
    guiTest.assertTrueVerbose("Reset Filter did not reset predicate logic check box",
                      (predLogicBox0.getSelectedItem().equals( "")), "not ");

    System.err.println( "Method 6 -------------");
    // apply timeline
    int timelineGroupIndx = 0;
    Object[] timelineGroupBoxes = getTimelineGroupBoxes( timelineGroup, timelineGroupIndx);
    JComboBox keyBox0 = (JComboBox) timelineGroupBoxes[0];
    NegationCheckBox negationBox0 = (NegationCheckBox) timelineGroupBoxes[1];
    LogicComboBox logicBox0 = (LogicComboBox) timelineGroupBoxes[2];
    guiTest.assertNotNullVerbose("Did not find timeline field.", keyBox0, "not ");
    guiTest.assertNotNullVerbose("Did not find negation check box.", negationBox0, "not ");

    int timelineNodeIndex = 3;
    keyBox0.setSelectedIndex( timelineNodeIndex);
    System.err.println( "selected timeline name " + keyBox0.getSelectedItem());
    String selectedTimelineName = (String) keyBox0.getSelectedItem();
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();

    boolean hasSlotsSelected = true; isToBeFound = true;
    List selectedTimelineNameList = new ArrayList();
    selectedTimelineNameList.add( selectedTimelineName);
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected,
                                  planWorks, helper, guiTest);
    validateConstraintNetTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                   helper, guiTest);
    validateTemporalExtentTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                    helper, guiTest);
    validateTokenNetworkTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                  helper, guiTest);

    System.err.println( "Method 7 -------------");
    // not timeline 
    negationBox0.setSelected(true);
    System.err.println( "selected NOT 'selected timeline name'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();

    hasSlotsSelected = false; isToBeFound = false;
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected,
                                  planWorks, helper, guiTest);
    validateConstraintNetTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                   helper, guiTest);
    validateTemporalExtentTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                    helper, guiTest);
    validateTokenNetworkTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                  helper, guiTest);

    timelineGroupIndx = 1;
    timelineGroupBoxes = getTimelineGroupBoxes( timelineGroup, timelineGroupIndx);
    JComboBox keyBox1 = (JComboBox) timelineGroupBoxes[0];
    NegationCheckBox negationBox1 = (NegationCheckBox) timelineGroupBoxes[1];
    LogicComboBox logicBox1 = (LogicComboBox) timelineGroupBoxes[2];
    logicBox1.setSelectedIndex( 1);
    System.err.println( "selected logicBox1 " + logicBox1.getSelectedItem());
    negationBox1.setSelected( true);
    System.err.println( "selected NOT 'selected timeline name 1'");
    timelineNodeIndex = 1;
    keyBox1.setSelectedIndex( timelineNodeIndex);
    System.err.println( "selected timeline name 1 " + keyBox1.getSelectedItem());
    String selectedTimelineName1 = (String) keyBox1.getSelectedItem();
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();

    selectedTimelineNameList.add( selectedTimelineName1);
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected,
                                  planWorks, helper, guiTest);
    validateConstraintNetTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                   helper, guiTest);
    validateTemporalExtentTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                    helper, guiTest);
    validateTokenNetworkTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                  helper, guiTest);

    // AND => OR & invert NOTs
    logicBox1.setSelectedIndex( 2);
    System.err.println( "selected logicBox1 " + logicBox1.getSelectedItem());
    negationBox0.setSelected( false);
    System.err.println( "inverted NOT 'selected timeline name 0'");
    negationBox1.setSelected( false);
    System.err.println( "inverted NOT 'selected timeline name 1'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();
    hasSlotsSelected = true; isToBeFound = true;
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected,
                                  planWorks, helper, guiTest);
    validateConstraintNetTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                   helper, guiTest);
    validateTemporalExtentTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                    helper, guiTest);
    validateTokenNetworkTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                  helper, guiTest);

    System.err.println( "Method 8 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(guiTest, resetFilterButton));
    System.err.println( "Reset Filter");
    guiTest.flushAWT(); guiTest.awtSleep();

    timelineGroupIndx = 0;
    timelineGroupBoxes = getTimelineGroupBoxes( timelineGroup, timelineGroupIndx);
    keyBox0 = (JComboBox) timelineGroupBoxes[0];
    negationBox0 = (NegationCheckBox) timelineGroupBoxes[1];
    logicBox0 = (LogicComboBox) timelineGroupBoxes[2];
    // System.err.println( "keyBox.getSelectedItem() " + keyBox.getSelectedItem());
    guiTest.assertTrueVerbose("Reset Filter did not reset timeline text box",
                      (keyBox0.getSelectedItem().equals( "")), "not ");
    guiTest.assertTrueVerbose("Reset Filter did not reset timeline negation check box",
                      (! negationBox0.isSelected()), "not ");
    guiTest.assertTrueVerbose("Reset Filter did not reset timeline logic check box",
                      (logicBox0.getSelectedItem().equals( "")), "not ");
    selectedTimelineNameList = new ArrayList();
    selectedTimelineNameList.add( "noTimelineSelected");
    hasSlotsSelected = false; isToBeFound = true;
    validateTimelineViewTimeline( selectedTimelineNameList, timelineView, hasSlotsSelected,
                                  planWorks, helper, guiTest);
    validateConstraintNetTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                   helper, guiTest);
    validateTemporalExtentTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                    helper, guiTest);
    validateTokenNetworkTimeline( selectedTimelineNameList, isToBeFound, planWorks,
                                  helper, guiTest);

    System.err.println( "Method 9 -------------");
    // apply time interval
    int timeIntervalGroupIndx = 0;
    Object[] timeIntervalGroupBoxes = getTimeIntervalGroupBoxes( timeIntervalGroup,
                                                                 timeIntervalGroupIndx);
    JTextField startBox0 = (JTextField) timeIntervalGroupBoxes[0];
    JTextField endBox0 = (JTextField) timeIntervalGroupBoxes[1];
    NegationCheckBox intNegationBox0 = (NegationCheckBox) timeIntervalGroupBoxes[2];
    LogicComboBox intLogicBox0 = (LogicComboBox) timeIntervalGroupBoxes[3];
    guiTest.assertNotNullVerbose("Did not find timeInterval start field.", startBox0, "not ");
    guiTest.assertNotNullVerbose("Did not find timeInterval end field.", endBox0, "not ");
    guiTest.assertNotNullVerbose("Did not find negation check box.", intNegationBox0, "not ");
    guiTest.assertNotNullVerbose("Did not find logic box.", intLogicBox0, "not ");

    System.err.println( "Method 10 -------------");
    String startStr0 = "40", endStr0 = "60";
    System.err.println( "startStr0 " + startStr0 + " endStr0 " + endStr0);
    startBox0.setText( startStr0);
    endBox0.setText( endStr0);
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();
    int numSlotsToFind = 2;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView, planWorks, helper, guiTest);
    int numTokensToFind = 24;
    validateConstraintNetTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTemporalExtentTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTokenNetworkTokens( numTokensToFind, planWorks, helper, guiTest);

    intNegationBox0.setSelected( true);
    System.err.println( "selected NOT 'selected time interval 0'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();
    numSlotsToFind = 4;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView, planWorks, helper, guiTest);
    numTokensToFind = 16;
    validateConstraintNetTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTemporalExtentTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTokenNetworkTokens( numTokensToFind, planWorks, helper, guiTest);

    timeIntervalGroupIndx = 1;
    timeIntervalGroupBoxes = getTimeIntervalGroupBoxes( timeIntervalGroup, timeIntervalGroupIndx);
    JTextField startBox1 = (JTextField) timeIntervalGroupBoxes[0];
    JTextField endBox1 = (JTextField) timeIntervalGroupBoxes[1];
    NegationCheckBox intNegationBox1 = (NegationCheckBox) timeIntervalGroupBoxes[2];
    LogicComboBox intLogicBox1 = (LogicComboBox) timeIntervalGroupBoxes[3];
    guiTest.assertNotNullVerbose("Did not find timeInterval start field.", startBox1, "not ");
    guiTest.assertNotNullVerbose("Did not find timeInterval end field.", endBox1, "not ");
    guiTest.assertNotNullVerbose("Did not find negation check box.", intNegationBox1, "not ");
    guiTest.assertNotNullVerbose("Did not find logic box.", intLogicBox1, "not ");

    intLogicBox1.setSelectedIndex( 1);
    System.err.println( "selected intLogicBox1 " + intLogicBox1.getSelectedItem());
    intNegationBox1.setSelected( true);
    System.err.println( "selected NOT 'selected time interval 1'");
    String startStr1 = "80", endStr1 = "100";
    System.err.println( "startStr1 " + startStr1 + " endStr1 " + endStr1);
    startBox1.setText( startStr1);
    endBox1.setText( endStr1);
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();
    numSlotsToFind = 2;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView, planWorks, helper, guiTest);
    numTokensToFind = 8;
    validateConstraintNetTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTemporalExtentTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTokenNetworkTokens( numTokensToFind, planWorks, helper, guiTest);

    intLogicBox1.setSelectedIndex( 2);
    System.err.println( "selected intLogicbox1 " + intLogicBox1.getSelectedItem());
    intNegationBox0.setSelected( false);
    System.err.println( "inverted NOT 'selected time interval 0'");
    intNegationBox1.setSelected( false);
    System.err.println( "inverted NOT 'selected time interval 1'");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();
    numSlotsToFind = 4;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView, planWorks, helper, guiTest);
    numTokensToFind = 32;
    validateConstraintNetTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTemporalExtentTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTokenNetworkTokens( numTokensToFind, planWorks, helper, guiTest);

    System.err.println( "Method 11 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(guiTest, resetFilterButton));
    System.err.println( "Reset Filter");
    guiTest.flushAWT(); guiTest.awtSleep();

    timeIntervalGroupIndx = 0;
    timeIntervalGroupBoxes = getTimeIntervalGroupBoxes( timeIntervalGroup,
                                                        timeIntervalGroupIndx);
    startBox0 = (JTextField) timeIntervalGroupBoxes[0];
    endBox0 = (JTextField) timeIntervalGroupBoxes[1];
    intNegationBox0 = (NegationCheckBox) timeIntervalGroupBoxes[2];
    intLogicBox0 = (LogicComboBox) timeIntervalGroupBoxes[3];
    guiTest.assertTrueVerbose("Reset Filter did not reset time interval start box",
                      (startBox0.getText().equals( "")), "not ");
    guiTest.assertTrueVerbose("Reset Filter did not reset time interval end box",
                      (endBox0.getText().equals( "")), "not ");
    guiTest.assertTrueVerbose("Reset Filter did not reset time interval negation check box",
                      (! negationBox0.isSelected()), "not ");
    guiTest.assertTrueVerbose("Reset Filter did not reset time interval logic check box",
                      (logicBox0.getSelectedItem().equals( "")), "not ");
    numSlotsToFind = 6;
    validateTimelineViewTimeInterval( numSlotsToFind, timelineView, planWorks, helper, guiTest);
    numTokensToFind = 40;
    validateConstraintNetTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTemporalExtentTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTokenNetworkTokens( numTokensToFind, planWorks, helper, guiTest);

    System.err.println( "Method 12 -------------");
    // mergeBox
    mergeBox.getMergeCheckBox().setSelected( true);
    guiTest.assertTrueVerbose("Content Filter: merge check box is not selected",
                              mergeBox.getMergeCheckBox().isSelected(), "not ");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();
   // "Merge tokens" does not effect the TimelineView
    numTokensToFind = 36;
    validateConstraintNetTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTemporalExtentTokens( numTokensToFind, planWorks, helper, guiTest);
    validateTokenNetworkTokens( numTokensToFind, planWorks, helper, guiTest);

    System.err.println( "Method 13 -------------");
    // "View slotted"
    mergeBox.getMergeCheckBox().setSelected( false);
    guiTest.assertTrueVerbose("Content Filter: merge check box is not unchecked",
                              (! mergeBox.getMergeCheckBox().isSelected()), "not ");
    helper.enterClickAndLeave(new MouseEventData(guiTest, tokenTypeBox.getSlottedButton()));
    System.err.println( "Content Filter: 'view slotted' selected");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();
    boolean isBaseTokenOnly = false;
    int numSlottedTokens = 24;
    int numFreeTokens = 16;
    guiTest.assertTrueVerbose("TimelineView slotted token count is not " + numSlottedTokens,
                      (getTimelineViewSlottedTokenList
                       ( timelineView, "", isBaseTokenOnly, planWorks,
                         helper, guiTest).size() == numSlottedTokens), "not ");
    guiTest.assertTrueVerbose("TimelineView free token count is not zero",
                              (getTimelineViewFreeTokenList( timelineView).size() == 0),
                              "not ");
    validateConstraintNetTokens( numSlottedTokens, planWorks, helper, guiTest);
    validateTemporalExtentTokens( numSlottedTokens, planWorks, helper, guiTest);
    validateTokenNetworkTokens( numSlottedTokens, planWorks, helper, guiTest);

    // "View free tokens"
    helper.enterClickAndLeave(new MouseEventData(guiTest, tokenTypeBox.getFreeTokensButton()));
    System.err.println( "Content Filter: 'view slotted' selected");
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();
    guiTest.assertTrueVerbose("TimelineView slotted token count is not zero",
                      (getTimelineViewSlottedTokenList( timelineView, "",
                                                        isBaseTokenOnly, planWorks,
                                                        helper, guiTest).size() == 0), "not ");
     System.err.println( "TimelineView free token count " +
                         getTimelineViewFreeTokenList( timelineView).size());
    guiTest.assertTrueVerbose("TimelineView free token count is not " + numFreeTokens,
                              (getTimelineViewFreeTokenList( timelineView).size() ==
                               numFreeTokens), "not ");
    validateConstraintNetTokens( numFreeTokens, planWorks, helper, guiTest);
    validateTemporalExtentTokens( numFreeTokens, planWorks, helper, guiTest);
    validateTokenNetworkTokens( numFreeTokens, planWorks, helper, guiTest);
  
    System.err.println( "Method 14 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(guiTest, resetFilterButton));
    System.err.println( "Reset Filter");
    guiTest.flushAWT(); guiTest.awtSleep();
    guiTest.assertTrueVerbose("TimelineView slotted token count is not " + numSlottedTokens,
                              (getTimelineViewSlottedTokenList
                               ( timelineView, "", isBaseTokenOnly, planWorks,
                                 helper, guiTest).size() == numSlottedTokens), "not ");
    guiTest.assertTrueVerbose("TimelineView free token count is not " + numFreeTokens,
                              (getTimelineViewFreeTokenList( timelineView).size() ==
                               numFreeTokens), "not ");
    validateConstraintNetTokens( (numSlottedTokens + numFreeTokens), planWorks, helper, guiTest);
    validateTemporalExtentTokens( (numSlottedTokens + numFreeTokens), planWorks, helper, guiTest);
    validateTokenNetworkTokens( (numSlottedTokens + numFreeTokens), planWorks, helper, guiTest);

    System.err.println( "Method 15 -------------");
    // require Add
    timelineGroupIndx = 0;
    timelineGroupBoxes = getTimelineGroupBoxes( timelineGroup, timelineGroupIndx);
    keyBox0 = (JComboBox) timelineGroupBoxes[0];
    negationBox0 = (NegationCheckBox) timelineGroupBoxes[1];
    keyBox0.setSelectedIndex( timelineNodeIndex);
    System.err.println( "selected timeline name " + keyBox0.getSelectedItem());
    selectedTimelineName = (String) keyBox0.getSelectedItem();
    negationBox0.setSelected(true);
    System.err.println( "selected NOT 'selected timeline name'");
    int uniqueKeyBoxIndexToFind = 0;
    Object[] uniqueKeyButtons = getUniqueKeyGroupBoxes( uniqueKeyGroupBox,
                                                        uniqueKeyBoxIndexToFind);
    JRadioButton requireButton0 = (JRadioButton) uniqueKeyButtons[0];
    JRadioButton excludeButton0 = (JRadioButton) uniqueKeyButtons[1];
    JTextField keyField0 = (JTextField) uniqueKeyButtons[2];
    JButton addButton0 = (JButton) uniqueKeyButtons[3];
    JButton removeButton0 = (JButton) uniqueKeyButtons[4];
    requireButton0.doClick();
    isBaseTokenOnly = true;
    List slottedTokens = getTimelineViewSlottedTokenList( timelineView, selectedTimelineName,
                                                          isBaseTokenOnly, planWorks,
                                                          helper, guiTest);
    PwToken requiredToken1 = (PwToken) slottedTokens.get( 0);
    PwToken requiredToken2 = (PwToken) slottedTokens.get( 3);
    PWTestHelper.setQueryField( keyField0, requiredToken1.getId().toString(), helper, guiTest);
    addButton0.doClick();

    uniqueKeyBoxIndexToFind = 1;
    uniqueKeyButtons = getUniqueKeyGroupBoxes( uniqueKeyGroupBox, uniqueKeyBoxIndexToFind);
    JRadioButton requireButton1 = (JRadioButton) uniqueKeyButtons[0];
    JRadioButton excludeButton1 = (JRadioButton) uniqueKeyButtons[1];
    JTextField keyField1 = (JTextField) uniqueKeyButtons[2];
    JButton addButton1 = (JButton) uniqueKeyButtons[3];
    JButton removeButton1 = (JButton) uniqueKeyButtons[4];
    requireButton1.doClick();
    PWTestHelper.setQueryField( keyField1, requiredToken2.getId().toString(), helper, guiTest);
    addButton1.doClick();
   
    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();

    isBaseTokenOnly = true;
    slottedTokens = getTimelineViewSlottedTokenList( timelineView, selectedTimelineName,
                                                     isBaseTokenOnly, planWorks,
                                                     helper, guiTest);
    System.err.println( "slottedTokens.size() " + slottedTokens.size());

    // try{Thread.sleep(4000);}catch(Exception e){}

    guiTest.assertTrueVerbose( "Timeline '" + selectedTimelineName +
                               "' does not show only two non-empty slots",
                               (slottedTokens.size() == 2), "not ");
    guiTest.assertTrueVerbose( "Timeline '" + selectedTimelineName + "' does not show token '" +
                               requiredToken1 + "'", slottedTokens.contains( requiredToken1),
                               "not ");
    guiTest.assertTrueVerbose( "Timeline '" + selectedTimelineName + "' does not show token '" +
                               requiredToken2 + "'", slottedTokens.contains( requiredToken2),
                               "not ");
    guiTest.assertTrueVerbose( "Constraint Network View does not show token '" +
                               requiredToken1 + "'",
                               constraintNetTokenShown( requiredToken1), "not ");
    guiTest.assertTrueVerbose( "Constraint Network View does not show token '" +
                               requiredToken2 + "'",
                               constraintNetTokenShown( requiredToken2), "not ");
    guiTest.assertTrueVerbose( "Temporal Extent View does not show token '" +
                               requiredToken1 + "'",
                               temporalExtentTokenShown( requiredToken1), "not ");
    guiTest.assertTrueVerbose( "Temporal Extent View does not show token '" +
                               requiredToken2 + "'",
                               temporalExtentTokenShown( requiredToken2), "not ");
    guiTest.assertTrueVerbose( "Token Network View does not show token '" +
                               requiredToken1 + "'",
                               tokenNetworkTokenShown( requiredToken1), "not ");
    guiTest.assertTrueVerbose( "TokenNetwork View does not show token '" +
                               requiredToken2 + "'",
                               tokenNetworkTokenShown( requiredToken2), "not ");

    System.err.println( "Method 16 -------------");
    // Select "Remove" for the second required key, and change "require"
    // to "exclude" for the first key.  Uncheck the "NOT" for the
    // selected timeline.  Apply it.
    removeButton1.doClick();
    excludeButton0.doClick();
    negationBox0.setSelected( false);

    System.err.println( "Apply Filter");
    helper.enterClickAndLeave(new MouseEventData(guiTest, activateFilterButton));
    guiTest.flushAWT(); guiTest.awtSleep();

    slottedTokens = getTimelineViewSlottedTokenList( timelineView, selectedTimelineName,
                                                     isBaseTokenOnly, planWorks,
                                                     helper, guiTest);
    System.err.println( "slottedTokens.size() " + slottedTokens.size());

    guiTest.assertTrueVerbose( "Timeline '" + selectedTimelineName +
                               "' does not show only four non-empty slots",
                               (slottedTokens.size() == 4), "not ");
    guiTest.assertFalseVerbose( "Timeline '" + selectedTimelineName +
                                "' does not exclude token '" + requiredToken1 + "'",
                                slottedTokens.contains( requiredToken1),
                               "not ");
    guiTest.assertTrueVerbose( "Timeline '" + selectedTimelineName + "' does not show token '" +
                               requiredToken2 + "'", slottedTokens.contains( requiredToken2),
                               "not ");
    guiTest.assertTrueVerbose( "Constraint Network View does not exclude token '" +
                               requiredToken1 + "'",
                               (! constraintNetTokenShown( requiredToken1)), "not ");
    guiTest.assertTrueVerbose( "Constraint Network View does not show token '" +
                               requiredToken2 + "'",
                               constraintNetTokenShown( requiredToken2), "not ");
    guiTest.assertTrueVerbose( "Temproal Extent View does not exclude token '" +
                               requiredToken1 + "'",
                               (! temporalExtentTokenShown( requiredToken1)), "not ");
    guiTest.assertTrueVerbose( "Temporal Extent View does not show token '" +
                               requiredToken2 + "'",
                               temporalExtentTokenShown( requiredToken2), "not ");
    guiTest.assertTrueVerbose( "Token Network View does not exclude token '" +
                               requiredToken1 + "'",
                               (! tokenNetworkTokenShown( requiredToken1)), "not ");
    guiTest.assertTrueVerbose( "Token Network View does not show token '" +
                               requiredToken2 + "'",
                               tokenNetworkTokenShown( requiredToken2), "not ");

    System.err.println( "Method 17 -------------");
    // reset
    helper.enterClickAndLeave(new MouseEventData(guiTest, resetFilterButton));
    System.err.println( "Reset Filter");
    guiTest.flushAWT(); guiTest.awtSleep();
    
    System.err.println( "Method 18 -------------");
    // Click Mouse-Right on a background area of the Content Filter dialog,
    //  and choose either opening an individual partial plan view or all
    //  the partial plan views.
    // delete a view first
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) PWTestHelper.getPartialPlanView
      ( ViewConstants.CONSTRAINT_NETWORK_VIEW, viewNameSuffix, guiTest);
    timelineView = (TimelineView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, guiTest);
    constraintNetworkView.getViewSet().removeViewFrame( timelineView.getViewFrame());
    try { timelineView.getViewFrame().setClosed( true); } 
    catch ( PropertyVetoException pve) { }
    guiTest.flushAWT(); guiTest.awtSleep();
    boolean timelineViewExists =
      constraintNetworkView.getViewSet().viewExists
      ( (String) PlanWorks.VIEW_CLASS_NAME_MAP.get( ViewConstants.TIMELINE_VIEW));

    guiTest.assertTrueVerbose( "Timeline View has not been deleted",
                               (! timelineViewExists), "not ");

    List viewListenerList = guiTest.createViewListenerList();
    contentSpecWindow.mouseRightPopupMenu( viewListenerList, new Point( 0, 0));
    guiTest.flushAWT(); guiTest.awtSleep();
    String frameMenuItemName = "Open Timeline View";
    PWTestHelper.selectFrameMenuItem( contentSpecWindow.getFrame(), frameMenuItemName,
                                      helper, guiTest);
    guiTest.viewListenerListWait( PlanWorksGUITest.TIMELINE_VIEW_INDEX, viewListenerList);
    timelineView = (TimelineView) PWTestHelper.getPartialPlanView
     ( ViewConstants.TIMELINE_VIEW, viewNameSuffix, guiTest);
    guiTest.assertTrueVerbose( "Timeline View has not been created",
                               (timelineView != null), "not ");

    contentSpecWindow.mouseRightPopupMenu( viewListenerList, new Point( 0, 0));
    guiTest.flushAWT(); guiTest.awtSleep();
    frameMenuItemName = "Hide All Views";
    PWTestHelper.selectFrameMenuItem( contentSpecWindow.getFrame(), frameMenuItemName,
                                      helper, guiTest);
    guiTest.assertTrueVerbose( "Timeline View has not been hidden",
                               timelineView.getViewFrame().isIcon(), "not ");

    contentSpecWindow.mouseRightPopupMenu( viewListenerList, new Point( 0, 0));
    guiTest.flushAWT(); guiTest.awtSleep();
    frameMenuItemName = "Show All Views";
    PWTestHelper.selectFrameMenuItem( contentSpecWindow.getFrame(), frameMenuItemName,
                                      helper, guiTest);
    guiTest.assertTrueVerbose( "Timeline View has not been shown",
                               (! timelineView.getViewFrame().isIcon()), "not ");
    try{Thread.sleep(1000);}catch(Exception e){} 


  } // end confirmSpecForViews


  private static Object[] getPredicateGroupBoxes( GroupBox predicateGroup,
                                                  int predicateBoxIndexToFind) {
    JComboBox keyBox = null;
    NegationCheckBox negationBox = null;
    LogicComboBox logicBox = null;
    int predicateBoxIndex = 0;
    for(int i = 0; i < predicateGroup.getComponentCount(); i++) {
      if(predicateGroup.getComponent(i) instanceof PredicateBox) {
        PredicateBox predicateBox = (PredicateBox) predicateGroup.getComponent(i);
        for(int j = 0; j < predicateBox.getComponentCount(); j++) {
          if(predicateBox.getComponent(j) instanceof LogicComboBox) {
            if (predicateBoxIndex == predicateBoxIndexToFind) {
              // System.err.println("predicateBoxIndex " + predicateBoxIndex);
              for(int k = 0; k < predicateBox.getComponentCount(); k++) {
                // System.err.println(predicateBox.getComponentCount() + ":" + k);
                if(predicateBox.getComponent(k) instanceof NegationCheckBox) {
                  negationBox = (NegationCheckBox) predicateBox.getComponent(k);
                }
                else if (predicateBox.getComponent(k) instanceof LogicComboBox) {
                  logicBox = (LogicComboBox) predicateBox.getComponent(k);
                }
                else if(predicateBox.getComponent(k) instanceof JComboBox) {
                  keyBox = (JComboBox) predicateBox.getComponent(k);
                }
              }
            }
          }
        }
      }
      predicateBoxIndex++;
    }
    return new Object[] { keyBox, negationBox, logicBox};
  } // end getPredicateGroupBoxes

  private static Object[] getTimelineGroupBoxes( GroupBox timelineGroup,
                                                 int timelineBoxIndexToFind) {
    JComboBox keyBox = null;
    NegationCheckBox negationBox = null;
    LogicComboBox logicBox = null;
    int timelineBoxIndex = 0;
    for(int i = 0; i < timelineGroup.getComponentCount(); i++) {
      if(timelineGroup.getComponent(i) instanceof TimelineBox) {
        TimelineBox timelineBox = (TimelineBox) timelineGroup.getComponent(i);
        for(int j = 0; j < timelineBox.getComponentCount(); j++) {
          if(timelineBox.getComponent(j) instanceof LogicComboBox) {
            if (timelineBoxIndex == timelineBoxIndexToFind) {
              // System.err.println("timelineBoxIndex " + timelineBoxIndex);
              for(int k = 0; k < timelineBox.getComponentCount(); k++) {
                // System.err.println(timelineBox.getComponentCount() + ":" + k +
                //                   " class " + timelineBox.getComponent(k).getClass().getName());
                if(timelineBox.getComponent(k) instanceof NegationCheckBox) {
                  negationBox = (NegationCheckBox) timelineBox.getComponent(k);
                }
                else if (timelineBox.getComponent(k) instanceof LogicComboBox) {
                  logicBox = (LogicComboBox) timelineBox.getComponent(k);
                }
                else if(timelineBox.getComponent(k) instanceof JComboBox) {
                  keyBox = (JComboBox) timelineBox.getComponent(k);
                }
              }
            }
          }
        }
      }
      timelineBoxIndex++;
    }
    return new Object[] { keyBox, negationBox, logicBox};
  } // end getTimelineGroupBoxes

  private static Object[] getTimeIntervalGroupBoxes( GroupBox timeIntervalGroup,
                                                     int timeIntervalBoxIndexToFind) {
    JTextField startBox = null, endBox = null;
    NegationCheckBox negationBox = null;
    LogicComboBox logicBox = null;
    int timeIntervalBoxIndex = 0;
    for(int i = 0; i < timeIntervalGroup.getComponentCount(); i++) {
      if(timeIntervalGroup.getComponent(i) instanceof TimeIntervalBox) {
        TimeIntervalBox tiBox = (TimeIntervalBox) timeIntervalGroup.getComponent(i);
        for(int j = 0; j < tiBox.getComponentCount(); j++) {
          if(tiBox.getComponent(j) instanceof LogicComboBox) {
            if (timeIntervalBoxIndex == timeIntervalBoxIndexToFind) {
              // System.err.println("timeIntervalBoxIndex " + timeIntervalBoxIndex);
              for(int k = 0; k < tiBox.getComponentCount(); k++) {
                // System.err.println(tiBox.getComponentCount() + ":" + k);
                if(tiBox.getComponent(k) instanceof NegationCheckBox) {
                  negationBox = (NegationCheckBox) tiBox.getComponent(k);
                }
                else if (tiBox.getComponent(k) instanceof LogicComboBox) {
                  logicBox = (LogicComboBox) tiBox.getComponent(k);
                }
                else if(tiBox.getComponent(k) instanceof JLabel) {
                  JLabel tempLabel = (JLabel) tiBox.getComponent(k);
                  if(tempLabel.getText().trim().equals("Time Interval Start")) {
                    k++;
                    startBox = (JTextField) tiBox.getComponent(k);
                  }
                  else if(tempLabel.getText().trim().equals("End")) {
                    k++;
                    endBox = (JTextField) tiBox.getComponent(k);
                  }
                }
              }
            }
          }
        }
      }
      timeIntervalBoxIndex++;
    }
    return new Object[] { startBox, endBox, negationBox, logicBox};
  } // end getTimeIntervalGroupBoxes

  private static Object[] getUniqueKeyGroupBoxes( GroupBox uniqueKeyGroup,
                                                  int uniqueKeyBoxIndexToFind) {
    JRadioButton requireButton = null;
    JRadioButton excludeButton = null;
    JTextField keyField = null;
    JButton addButton = null;
    JButton removeButton = null;
    int uniqueKeyBoxIndex = 0;
    for(int i = 0; i < uniqueKeyGroup.getComponentCount(); i++) {
      if(uniqueKeyGroup.getComponent(i) instanceof UniqueKeyBox) {
        UniqueKeyBox ukBox = (UniqueKeyBox) uniqueKeyGroup.getComponent(i);
        if (uniqueKeyBoxIndex == uniqueKeyBoxIndexToFind) {
          System.err.println("uniqueKeyBoxIndex " + uniqueKeyBoxIndex);
          requireButton = ukBox.getRequireButton();
          addButton = ukBox.getAddButton();
          keyField = ukBox.getKeyField();
          excludeButton = ukBox.getExcludeButton();
          removeButton = ukBox.getRemoveButton();
        }
      }
      uniqueKeyBoxIndex++;
    }
    return new Object[] { requireButton, excludeButton, keyField, addButton, removeButton};
  } // end getUniqueKeyGroupBoxes

  private static void validateTimelineViewPredicate( List selectedPredicateNameList,
                                                     TimelineView timelineView,
                                                     boolean isToBeFound, PlanWorks planWorks,
                                                     JFCTestHelper helper,
                                                     PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    List predFoundInTimeline = new ArrayList();
    for (int i = 0; i < selectedPredicateNameList.size(); i++) {
      predFoundInTimeline.add( null);
    }
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      // System.err.println( "timelineNode " + timelineNode.getLabel().getText());
      int numSlots = 0;
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        numSlots++;
        List tokenList = slotNode.getSlot().getTokenList();
        Iterator tokenItr = tokenList.iterator();
        while (tokenItr.hasNext()) {
          PwToken token = (PwToken) tokenItr.next();
          String predicateName = token.getPredicateName();
          if (selectedPredicateNameList.contains( predicateName)) {
            predFoundInTimeline.set( selectedPredicateNameList.indexOf( predicateName),
                                     timelineNode.getTimeline().getName());
            break;
          }
        }
      }
//       System.err.println( "Timeline " + timelineNode.getTimeline().getName() +
//                           " slots " + numSlots);
    }
    Iterator freeTokenNodeItr = timelineView.getFreeTokenNodeList().iterator();
    while (freeTokenNodeItr.hasNext()) {
      TimelineTokenNode freeTokenNode = (TimelineTokenNode) freeTokenNodeItr.next();
      String predicateName = freeTokenNode.getPredicateName();
      if (selectedPredicateNameList.contains( predicateName)) {
        predFoundInTimeline.set( selectedPredicateNameList.indexOf( predicateName), "free token");
        break;
      }
    }
    Iterator selectedPredItr = selectedPredicateNameList.iterator();
    Iterator foundTimelineItr = predFoundInTimeline.iterator();
    while (selectedPredItr.hasNext()) {
      String foundTimeline = (String) foundTimelineItr.next();
      if (isToBeFound) {
        guiTest.assertNotNullVerbose( "Predicate " + (String) selectedPredItr.next() +
                              " is not found in timeline " + foundTimeline, foundTimeline, "not ");
      } else {
        guiTest.assertNullVerbose( "Predicate " + (String) selectedPredItr.next() +
                           " is not absent in all timelines", foundTimeline, "not ");
      }
    }
  } // end validateTimelineViewPredicate

  private static void validateTimelineViewTimeline( List selectedTimelineNameList,
                                                    TimelineView timelineView,
                                                    boolean hasSlotsSelected,
                                                    PlanWorks planWorks, JFCTestHelper helper,
                                                    PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      // System.err.println( "timelineNode " + timelineNode.getLabel().getText());
      boolean hasSlots = ! hasSlotsSelected;
      if (selectedTimelineNameList.contains( timelineNode.getTimeline().getName())) {
        hasSlots = hasSlotsSelected;
      }
      int numSlots = 0;
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        // System.err.println( "  slotNode isVisible " + slotNode.isVisible());
        numSlots++;
      }
      System.err.println( "  numSlots " + numSlots);
      if (hasSlots) {
        guiTest.assertTrueVerbose( "Timeline " + timelineNode.getTimeline().getName() +
                           " does not have slots", (numSlots != 0), "not ");
      } else {
        guiTest.assertTrueVerbose( "Timeline " + timelineNode.getTimeline().getName() +
                           " does not have zero slots", (numSlots == 0), "not ");
      }
    }
  } // end validateTimelineViewTimeline

  private static void validateTimelineViewTimeInterval( int numSlotsToFind,
                                                        TimelineView timelineView,
                                                        PlanWorks planWorks,
                                                        JFCTestHelper helper,
                                                        PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      // System.err.println( "timelineNode " + timelineNode.getLabel().getText());
      int numSlots = 0;
      Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeItr.next();
        // System.err.println( "  slotNode isVisible " + slotNode.isVisible());
        numSlots++;
      }
//       System.err.println( "Timeline " + timelineNode.getTimeline().getName() +
//                           " slots " + numSlots);
      guiTest.assertTrueVerbose( "Timeline " + timelineNode.getTimeline().getName() +
                         " does not have " + numSlotsToFind + " slots",
                         (numSlots == numSlotsToFind), "not ");
    }
  } // end validateTimelineViewTimeInterval

  private static List getTimelineViewSlottedTokenList( TimelineView timelineView,
                                                       String timelineName,
                                                       boolean isBaseTokenOnly,
                                                       PlanWorks planWorks,
                                                       JFCTestHelper helper,
                                                       PlanWorksGUITest guiTest) {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    List tokenList = new ArrayList();
    List slotIdList = new ArrayList();
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      if (timelineName.equals( "") ||
          ((! timelineName.equals( "")) &&
           (timelineNode.getTimelineName().indexOf( timelineName) >= 0))) {
        Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
        while (slotNodeItr.hasNext()) {
          PwSlot slot = ((SlotNode) slotNodeItr.next()).getSlot();
          if (isBaseTokenOnly) {
            if (! slotIdList.contains( slot.getId())) {
              if (! slot.isEmpty()) {
                tokenList.add( slot.getBaseToken());
              }
              slotIdList.add( slot.getId());
            }
          } else {
            Iterator tokenItr = slot.getTokenList().iterator();
            while (tokenItr.hasNext()) {
              tokenList.add( (PwToken) tokenItr.next());
            }
          }
        }
      }
    }
    return tokenList;
  } // end getTimelineViewSlottedTokenList

  private static List getTimelineViewFreeTokenList( TimelineView timelineView) {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    List tokenList = new ArrayList();
    Iterator freeTokenNodeItr = timelineView.getFreeTokenNodeList().iterator();
    while (freeTokenNodeItr.hasNext()) {
      TimelineTokenNode freeTimelineTokenNode = (TimelineTokenNode) freeTokenNodeItr.next();
      tokenList.add( freeTimelineTokenNode.getToken());
    }
    return tokenList;
  } // end getTimelineViewFreeTokenList

  private static void validateConstraintNetPredicate( List selectedPredicateNameList,
                                                      boolean isToBeFound,
                                                      PlanWorks planWorks,
                                                      JFCTestHelper helper,
                                                      PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    List predFoundInConstraintNetwork = new ArrayList();
    Iterator containerNodeItr = constraintNetworkView.getContainerNodeList().iterator();
    while (containerNodeItr.hasNext()) {
      VariableContainerNode containerNode = (VariableContainerNode) containerNodeItr.next();
      // System.err.println( "containerNode " + containerNode.getLabel().getText());
      if (containerNode instanceof ConstraintNetworkTokenNode) {
        ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) containerNode;
        PwToken token = tokenNode.getToken();
        String predicateName = token.getPredicateName();
        if ((selectedPredicateNameList.contains( predicateName)) &&
            tokenNode.isVisible()) {
          predFoundInConstraintNetwork.add( predicateName);
        }
      }
    }
    Iterator selectedPredItr = selectedPredicateNameList.iterator();
    while (selectedPredItr.hasNext()) {
      String selectedPred = (String) selectedPredItr.next();
      if (isToBeFound) {
        guiTest.assertTrueVerbose( "Predicate " + selectedPred +
                                   " is not found in Constraint Network",
                                   predFoundInConstraintNetwork.contains( selectedPred),
                                   "not ");
      } else {
        guiTest.assertTrueVerbose( "Predicate " + selectedPred +
                                   " is not absent in Constraint Network",
                                   (! predFoundInConstraintNetwork.contains( selectedPred)),
                                   "not ");
      }
    }
  } // end validateConstraintNetPredicate

  private static void validateConstraintNetTimeline( List selectedTimelineNameList,
                                                     boolean isToBeFound,
                                                     PlanWorks planWorks,
                                                     JFCTestHelper helper,
                                                     PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    validateConstraintNetPredicate( getSelectedPredicateNames( selectedTimelineNameList),
                                    isToBeFound, planWorks, helper, guiTest);
  } // end validateConstraintNetTimeline

  private static List getSelectedPredicateNames( List selectedTimelineNameList) {
    List selectedPredicateNames = new ArrayList();
    Iterator timelineNodeItr = timelineView.getTimelineNodeList().iterator();
    while (timelineNodeItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeItr.next();
      if (selectedTimelineNameList.contains( timelineNode.getTimeline().getName()) ||
          selectedTimelineNameList.contains( "noTimelineSelected")) {
        Iterator slotNodeItr = timelineNode.getSlotNodeList().iterator();
        while (slotNodeItr.hasNext()) {
          SlotNode slotNode = (SlotNode) slotNodeItr.next();
          List tokenList = slotNode.getSlot().getTokenList();
          Iterator tokenItr = tokenList.iterator();
          while (tokenItr.hasNext()) {
            PwToken token = (PwToken) tokenItr.next();
            String predicateName = token.getPredicateName();
            selectedPredicateNames.add( predicateName);
          }
        }
      }
    }
    return selectedPredicateNames;
  } // end getSelectedPredicateNames

  private static void validateConstraintNetTokens( int numTokensToFind,
                                                        PlanWorks planWorks,
                                                        JFCTestHelper helper,
                                                        PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    int numTokens = 0;
    Iterator containerNodeItr = constraintNetworkView.getContainerNodeList().iterator();
    while (containerNodeItr.hasNext()) {
      VariableContainerNode containerNode = (VariableContainerNode) containerNodeItr.next();
      // System.err.println( "containerNode " + containerNode.getLabel().getText());
      if (containerNode instanceof ConstraintNetworkTokenNode) {
        ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) containerNode;
        if (tokenNode.isVisible()) {
          PwToken token = tokenNode.getToken();
          if (! (token instanceof PwResourceTransaction)) {
            numTokens++;
          }
        }
      }
    }
    guiTest.assertTrueVerbose( "Constraint Network View does not have " + numTokensToFind + 
                               " tokens", (numTokens == numTokensToFind), "not ");
  } // end validateConstraintNetTokens

  private static boolean constraintNetTokenShown( PwToken requiredToken) {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    boolean isFound = false;
    Iterator containerNodeItr = constraintNetworkView.getContainerNodeList().iterator();
    while (containerNodeItr.hasNext()) {
      VariableContainerNode containerNode = (VariableContainerNode) containerNodeItr.next();
      // System.err.println( "containerNode " + containerNode.getLabel().getText());
      if (containerNode instanceof ConstraintNetworkTokenNode) {
        ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) containerNode;
        if (tokenNode.isVisible()) {
          PwToken token = tokenNode.getToken();
          if (token.getId().equals( requiredToken.getId())) {
            return true;
          }
        }
      }
    }
    return isFound;
  } // end constraintNetTokenShown

  private static void validateTemporalExtentPredicate( List selectedPredicateNameList,
                                                       boolean isToBeFound,
                                                       PlanWorks planWorks,
                                                       JFCTestHelper helper,
                                                       PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    List predFoundInTemporalExtent = new ArrayList();
    Iterator temporalNodeItr = temporalExtentView.getTemporalNodeList().iterator();
    while (temporalNodeItr.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeItr.next();
      // System.err.println( "temporalNode " + temporalNode.getLabel().getText());
      PwToken token = temporalNode.getToken();
      String predicateName = token.getPredicateName();
      if ((selectedPredicateNameList.contains( predicateName)) &&
          temporalNode.isVisible()) {
        predFoundInTemporalExtent.add( predicateName);
      }
    }
    Iterator selectedPredItr = selectedPredicateNameList.iterator();
    while (selectedPredItr.hasNext()) {
      String selectedPred = (String) selectedPredItr.next();
      if (isToBeFound) {
        guiTest.assertTrueVerbose( "Predicate " + selectedPred +
                                   " is not found in Temporal Extent",
                                   predFoundInTemporalExtent.contains( selectedPred),
                                   "not ");
      } else {
        guiTest.assertTrueVerbose( "Predicate " + selectedPred +
                                   " is not absent in Temporal Extent",
                                   (! predFoundInTemporalExtent.contains( selectedPred)),
                                   "not ");
      }
    }
  } // end validateTemporalExtentPredicate

  private static void validateTemporalExtentTimeline( List selectedTimelineNameList,
                                                      boolean isToBeFound,
                                                      PlanWorks planWorks,
                                                      JFCTestHelper helper,
                                                      PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    validateTemporalExtentPredicate( getSelectedPredicateNames( selectedTimelineNameList),
                                     isToBeFound, planWorks, helper, guiTest);
  } // end validateTemporalExtentTimeline

  private static void validateTemporalExtentTokens( int numTokensToFind,
                                                    PlanWorks planWorks,
                                                    JFCTestHelper helper,
                                                    PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    int numTokens = 0;
    Iterator temporalNodeItr = temporalExtentView.getTemporalNodeList().iterator();
    while (temporalNodeItr.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeItr.next();
      // System.err.println( "temporalNode " + temporalNode.getLabel().getText());
      if (temporalNode.isVisible()) {
        PwToken token = temporalNode.getToken();
        if (! (token instanceof PwResourceTransaction)) {
          numTokens++;
        }
      }
    }
    guiTest.assertTrueVerbose( "Temporal Extent View does not have " + numTokensToFind + 
                               " tokens", (numTokens == numTokensToFind), "not ");
  } // end validateTemporalExtentTokens

  private static boolean temporalExtentTokenShown( PwToken requiredToken) {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    boolean isFound = false;
    Iterator temporalNodeItr = temporalExtentView.getTemporalNodeList().iterator();
    while (temporalNodeItr.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeItr.next();
      // System.err.println( "temporalNode " + temporalNode.getLabel().getText());
      if (temporalNode.isVisible()) {
        PwToken token = temporalNode.getToken();
        if (token.getId().equals( requiredToken.getId())) {
          return true;
        }
      }
    }
    return isFound;
  } // end temporalExtentTokenShown

  private static void validateTokenNetworkPredicate( List selectedPredicateNameList,
                                                     boolean isToBeFound,
                                                     PlanWorks planWorks,
                                                     JFCTestHelper helper,
                                                     PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    List predFoundInTokenNetwork = new ArrayList();
    Iterator tokenNodeItr = tokenNetworkView.getTokenNodeValueList().iterator();
    while (tokenNodeItr.hasNext()) {
      TokenNetworkTokenNode tokenNode = (TokenNetworkTokenNode) tokenNodeItr.next();
      // System.err.println( "tokenNode " + tokenNode.getLabel().getText());
      PwToken token = tokenNode.getToken();
      String predicateName = token.getPredicateName();
      if ((selectedPredicateNameList.contains( predicateName)) &&
          tokenNode.isVisible()) {
        predFoundInTokenNetwork.add( predicateName);
      }
    }
    Iterator selectedPredItr = selectedPredicateNameList.iterator();
    while (selectedPredItr.hasNext()) {
      String selectedPred = (String) selectedPredItr.next();
      if (isToBeFound) {
        guiTest.assertTrueVerbose( "Predicate " + selectedPred +
                                   " is not found in Token Network",
                                   predFoundInTokenNetwork.contains( selectedPred),
                                   "not ");
      } else {
        guiTest.assertTrueVerbose( "Predicate " + selectedPred +
                                   " is not absent in Token Network",
                                   (! predFoundInTokenNetwork.contains( selectedPred)), "not ");
      }
    }
  } // end validateTokenNetworkPredicate

  private static void validateTokenNetworkTimeline( List selectedTimelineNameList,
                                                    boolean isToBeFound,
                                                    PlanWorks planWorks,
                                                    JFCTestHelper helper,
                                                    PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    validateTokenNetworkPredicate( getSelectedPredicateNames( selectedTimelineNameList),
                                   isToBeFound, planWorks, helper, guiTest);
  } // end validateTokenNetworkTimeline

  private static void validateTokenNetworkTokens( int numTokensToFind,
                                                  PlanWorks planWorks,
                                                  JFCTestHelper helper,
                                                  PlanWorksGUITest guiTest)
    throws Exception {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    int numTokens = 0;
    Iterator tokenNodeItr = tokenNetworkView.getTokenNodeValueList().iterator();
    while (tokenNodeItr.hasNext()) {
      TokenNetworkTokenNode tokenNode = (TokenNetworkTokenNode) tokenNodeItr.next();
      // System.err.println( "tokenNode " + tokenNode.getLabel().getText());
      if (tokenNode.isVisible()) {
        PwToken token = tokenNode.getToken();
        if (! (token instanceof PwResourceTransaction)) {
          numTokens++;
        }
      }
    }
    guiTest.assertTrueVerbose( "Token Network View does not have " + numTokensToFind + 
                               " tokens", (numTokens == numTokensToFind), "not ");
  } // end validateTokenNetworkTokens

  private static boolean tokenNetworkTokenShown( PwToken requiredToken) {
    try{Thread.sleep(500);}catch(Exception e){} // more time needed
    boolean isFound = false;
    Iterator tokenNodeItr = tokenNetworkView.getTokenNodeValueList().iterator();
    while (tokenNodeItr.hasNext()) {
      TokenNetworkTokenNode tokenNode = (TokenNetworkTokenNode) tokenNodeItr.next();
      // System.err.println( "tokenNode " + tokenNode.getLabel().getText());
      if (tokenNode.isVisible()) {
        PwToken token = tokenNode.getToken();
        if (token.getId().equals( requiredToken.getId())) {
          return true;
        }
      }
    }
    return isFound;
  } // end tokenNetworkTokenShown




} // end class PlanWorksGUITest12
