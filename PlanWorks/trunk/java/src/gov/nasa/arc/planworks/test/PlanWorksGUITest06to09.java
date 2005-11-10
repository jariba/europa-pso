//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest06to09.java,v 1.3 2005-11-10 01:22:10 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.Finder;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.ThreadListener;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIDesktopPane;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
//import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.DBTransactionQueryView;
//import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.StepQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
//import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;


public class PlanWorksGUITest06to09 {

  private PlanWorksGUITest06to09() {
  }

  public static void planViz06to09( List sequenceUrls,
                                    PlanWorksGUITest.FrameXAscending frameXAscending,
                                    PlanWorks planWorks, JFCTestHelper helper,
                                    PlanWorksGUITest guiTest) throws Exception {
    // dependent sequence of tests
    planViz06( sequenceUrls, planWorks, helper, guiTest);
    planViz07( planWorks, helper, guiTest);
    planViz08( frameXAscending, planWorks, helper, guiTest);
    planViz09( sequenceUrls, planWorks, helper, guiTest); 
  } // end planViz06to09

  public static void planViz06( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
                                PlanWorksGUITest guiTest) throws Exception {
    // try{Thread.sleep(2000);}catch(Exception e){}
     String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                     System.getProperty("file.separator") +
                                     sequenceUrls.get( 4));
    // System.err.println( "planViz06 sequenceUrls.get( 4) " + sequenceUrls.get( 4));
    PWTestHelper.createProject( PWTestHelper.PROJECT1, sequenceDirectory, helper, guiTest,
                                planWorks);
    // try{Thread.sleep(2000);}catch(Exception e){}
    PWTestHelper.addSequencesToProject( sequenceFileArray, helper, guiTest, planWorks);

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 5));
    // System.err.println( "planViz06 sequenceUrls.get( 5) " + sequenceUrls.get( 5));
    PWTestHelper.addPlanSequence( helper, guiTest, planWorks);
    PWTestHelper.addSequencesToProject( sequenceFileArray, helper, guiTest, planWorks);

    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                      sequenceUrls.get( 6));
    // System.err.println( "planViz06 sequenceUrls.get( 6) " + sequenceUrls.get( 6));
    PWTestHelper.addPlanSequence( helper, guiTest, planWorks);
    PWTestHelper.addSequencesToProject( sequenceFileArray, helper, guiTest, planWorks);

    guiTest.assertTrueVerbose( "PlanWorks title does not contain '" + PWTestHelper.PROJECT1,
                               planWorks.getTitle().endsWith( PWTestHelper.PROJECT1), "not ");

    // 3 sequences under Planning Sequence
    int sequenceCount = 0;
    JMenu  planSequenceMenu = PWTestHelper.getPlanSequenceMenu();
    for (int i = 0, n = planSequenceMenu.getItemCount(); i < n; i++) {
      JMenuItem menuItem = planSequenceMenu.getItem( i);
      System.err.println( "Planning Sequence->" + menuItem.getText());
      if (menuItem.getText().startsWith( PWTestHelper.SEQUENCE_NAME)) {
        sequenceCount++;
      }
    }
    guiTest.assertTrueVerbose( "There are not 3 'sequence' plans under 'Planning Sequence'",
                               (sequenceCount == 3), "not ");

    System.err.println( "\nPLANVIZ_06 COMPLETED\n");
  } // end planViz06

  public static void planViz07( PlanWorks planWorks, JFCTestHelper helper,
                                PlanWorksGUITest guiTest) throws Exception {
    ViewListener viewListener01 = new PlanWorksGUITest.ViewListenerWait01( guiTest);
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME, viewListener01,
                                       helper, guiTest);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME, helper, guiTest);
    int stepNumber = 1;
    String viewMenuItemName = "Open " + ViewConstants.TIMELINE_VIEW;
    List viewListenerList = guiTest.createViewListenerList();
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, guiTest);
    guiTest.viewListenerListWait( PlanWorksGUITest.TIMELINE_VIEW_INDEX, viewListenerList);

    // then sequence (1) timeline view
    viewListener01.reset();
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (1)",
                                        viewListener01, helper, guiTest);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView1 =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (1)", helper, guiTest);
    guiTest.viewListenerListReset( PlanWorksGUITest.TIMELINE_VIEW_INDEX, viewListenerList);
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView1, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, guiTest);
    guiTest.viewListenerListWait( PlanWorksGUITest.TIMELINE_VIEW_INDEX, viewListenerList);
    // then sequence (2) timeline view
    viewListener01.reset();
    PWTestHelper.openSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (2)",
                                        viewListener01, helper, guiTest);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView2 =
      PWTestHelper.getSequenceStepsView( PWTestHelper.SEQUENCE_NAME + " (2)", helper, guiTest);
    guiTest.viewListenerListReset( PlanWorksGUITest.TIMELINE_VIEW_INDEX, viewListenerList);
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView2, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, guiTest);
    guiTest.viewListenerListWait( PlanWorksGUITest.TIMELINE_VIEW_INDEX, viewListenerList);
    // then sequence (2) timeline view
    // now select the timeline view from the first sequence
    // view exists, so no wait needed
    PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                                viewMenuItemName, viewListenerList,
                                                helper, guiTest);
    // try{Thread.sleep(2000);}catch(Exception e){}

   // post condition: seqStepsView timeline view has focus
    String timelineViewName =
      seqStepsView.getName().replaceFirst( ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", ""),
                                           ViewConstants.TIMELINE_VIEW.replaceAll( " ", ""));
    timelineViewName = timelineViewName.concat( System.getProperty( "file.separator") +
                                                "step" + String.valueOf( stepNumber));
    TimelineView timelineView = (TimelineView) PWTestHelper.findComponentByName
      ( TimelineView.class, timelineViewName, Finder.OP_EQUALS);
    guiTest.assertNotNullVerbose( ViewConstants.TIMELINE_VIEW + " for " +
                                  PWTestHelper.SEQUENCE_NAME + " not found", timelineView,
                                  "not ");
    int stackIndex = PWTestHelper.getStackIndex( timelineView.getViewFrame());
    // System.err.println( "  stackIndex " + stackIndex);
    guiTest.assertTrueVerbose( ViewConstants.TIMELINE_VIEW + " for " +
                               PWTestHelper.SEQUENCE_NAME +
                               " is not at top of window stack order",
                               (stackIndex == 0), "not ");

    // post condition: 3 content filter and 3 timeline view windows exist
    JMenu  windowMenu = PWTestHelper.findMenu( PlanWorks.WINDOW_MENU);
    guiTest.assertNotNullVerbose( "Window menu not found", windowMenu, "not ");
    helper.enterClickAndLeave( new MouseEventData( guiTest, windowMenu));
    JMenuItem stackTopMenuItem = null; // the first timeline one in the list
    int contentFilterCount = 0, timelineViewCount = 0;
    for (int i = 0, n = windowMenu.getMenuComponentCount(); i < n; i++) {
      if (windowMenu.getMenuComponent( i) instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) windowMenu.getMenuComponent( i);
        // System.err.println( "  menu component i " + i + " " + menuItem.getText());
        if (menuItem.getText().startsWith( ViewConstants.CONTENT_SPEC_TITLE)) {
          contentFilterCount++;
        } else if (menuItem.getText().startsWith( ViewConstants.TIMELINE_VIEW.
                                                  replaceAll( " ", ""))) {
          if (stackTopMenuItem == null) {
            stackTopMenuItem = menuItem;
          }
          timelineViewCount++;
        }
      }
    }
    guiTest.assertTrueVerbose( "There are not 3 '" +  ViewConstants.CONTENT_SPEC_TITLE +
                        "' windows (found " + String.valueOf( contentFilterCount) + ")",
                        (contentFilterCount == 3), "not ");
    guiTest.assertTrueVerbose( "There are not 3 '" +  ViewConstants.TIMELINE_VIEW +
                        "' windows (found " + String.valueOf( timelineViewCount) + ")",
                        (timelineViewCount == 3), "not ");


    // this is needed to free up Window menu and allow Project->Delete to be available
    helper.enterClickAndLeave( new MouseEventData( guiTest, stackTopMenuItem));
    
    // try{Thread.sleep(2000);}catch(Exception e){}
    System.err.println( "\nPLANVIZ_07 COMPLETED\n");
  } // end planViz07

  public static void planViz08(  PlanWorksGUITest.FrameXAscending frameXAscending,
                                 PlanWorks planWorks, JFCTestHelper helper,
                                 PlanWorksGUITest guiTest) throws Exception {
    JMenuItem tileItem =
      PWTestHelper.findMenuItem( PlanWorks.WINDOW_MENU, PlanWorks.TILE_WINDOWS_MENU_ITEM,
                                 helper, guiTest);
    guiTest.assertNotNullVerbose( "'Window->Tile Windows' not found:", tileItem, "not ");
    helper.enterClickAndLeave( new MouseEventData( guiTest, tileItem));
    guiTest.flushAWT(); guiTest.awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}
    // post condition:  The ContentFilter and SequenceQuery windows are tiled
    // across the top row of the frame.  The TimelineView and SequenceStepsView
    // windows are tiled in the second thru fourth rows.
    List contentFilterFrames =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.CONTENT_SPEC_TITLE);
    // List sequenceQueryFrames =
//       PWTestHelper.getInternalFramesByPrefixName( ViewConstants.SEQUENCE_QUERY_TITLE);
    guiTest.assertTrueVerbose( "There are not 3 '" +  ViewConstants.CONTENT_SPEC_TITLE +
                        "' windows (found " + contentFilterFrames.size() + ")",
                        (contentFilterFrames.size() == 3), "not ");
//     guiTest.assertTrueVerbose( "There are not 3 '" +  ViewConstants.SEQUENCE_QUERY_TITLE +
//                         "' windows (found " + sequenceQueryFrames.size() + ")",
//                         (sequenceQueryFrames.size() == 3), "not ");
    double firstRowYLocation = 0.0, secondRowYLocation = 0.0;
    //contentFilterFrames.addAll( sequenceQueryFrames);
    Iterator contentFilterItr = contentFilterFrames.iterator();
    while (contentFilterItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) contentFilterItr.next();
//       System.err.println( frame.getTitle() + " y " + frame.getLocation().getY() +
//                           " height " + frame.getSize().getHeight());
      if (frame.getSize().getHeight() > secondRowYLocation) {
        secondRowYLocation = frame.getSize().getHeight();
      }
      guiTest.assertTrueVerbose( frame.getTitle() + " frame y location not in first row",
                                 (frame.getLocation().getY() == firstRowYLocation), "not ");
    }
//     System.err.println("secondRowYLocation " + secondRowYLocation);
    String timelineViewPrefix =
      Utilities.trimView( ViewConstants.TIMELINE_VIEW).replaceAll( " ", "");
    List timelineViewFrames = PWTestHelper.getInternalFramesByPrefixName( timelineViewPrefix);
    String seqStepsViewPrefix =
      Utilities.trimView( ViewConstants.SEQUENCE_STEPS_VIEW).replaceAll( " ", "");
    List sequenceStepsFrames = PWTestHelper.getInternalFramesByPrefixName( seqStepsViewPrefix);
    guiTest.assertTrueVerbose( "There are not 3 '" +  ViewConstants.TIMELINE_VIEW +
                               "' windows (found " + timelineViewFrames.size() + ")",
                               (timelineViewFrames.size() == 3), "not ");
    guiTest.assertTrueVerbose( "There are not 3 '" +  ViewConstants.SEQUENCE_STEPS_VIEW +
                        "' windows (found " + sequenceStepsFrames.size() + ")",
                        (sequenceStepsFrames.size() == 3), "not ");
    timelineViewFrames.addAll( sequenceStepsFrames);
    Iterator timelineViewItr = timelineViewFrames.iterator();
    while (timelineViewItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) timelineViewItr.next();
//       System.err.println( frame.getTitle() + " y " + frame.getLocation().getY() +
//                           " height " + frame.getSize().getHeight());
      guiTest.assertTrueVerbose( frame.getTitle() + " frame y location not >= second row",
                                 (frame.getLocation().getY() >= secondRowYLocation), "not ");
    }
    // "Window->Cascade".
    JMenuItem cascadeItem =
      PWTestHelper.findMenuItem( PlanWorks.WINDOW_MENU, PlanWorks.CASCADE_WINDOWS_MENU_ITEM,
                                 helper, guiTest);
    guiTest.assertNotNullVerbose( "'Window->Cascade Windows' not found:", cascadeItem, "not ");
    helper.enterClickAndLeave( new MouseEventData( guiTest, cascadeItem));
    guiTest.flushAWT(); guiTest.awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}

    List cascadedFrames = new ArrayList();
    Container contentPane = PlanWorks.getPlanWorks().getContentPane();
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      Component component = (Component) contentPane.getComponent( i);
      if (component instanceof MDIDesktopPane) {
        JInternalFrame[] frames = ((MDIDesktopPane) component).getAllFrames();
        for (int j = 0, m = frames.length; j < m; j++) {
          // System.err.println( "j " + j + " " + ((MDIInternalFrame) frames[j]).getTitle());
          MDIInternalFrame frame = (MDIInternalFrame) frames[j];
//           System.err.println( "frame " + frame.getTitle() + " y " +
//                               frame.getLocation().getY() +
//                               " height " + frame.getSize().getHeight());
          if ((frame.getTitle().startsWith( ViewConstants.CONTENT_SPEC_TITLE))) {
              //(frame.getTitle().startsWith( ViewConstants.SEQUENCE_QUERY_TITLE))) {
            guiTest.assertTrueVerbose( frame.getTitle() + " not in first row",
                                       (frame.getLocation().getY() == firstRowYLocation),
                                       "not ");
          } else if ((frame.getTitle().startsWith( timelineViewPrefix)) ||
                     (frame.getTitle().startsWith( seqStepsViewPrefix))) {
            cascadedFrames.add( frame);
          }
        }
        break;
      }
    }
    // sort by x, then check that x & y vlaues are incrementally increasing
    double frameX = 0.0, frameY = 0.0;
    Collections.sort( cascadedFrames, frameXAscending);
    Iterator frameItr = cascadedFrames.iterator();
    while (frameItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) frameItr.next();
//       System.err.println( "frame " + frame.getTitle() + " x " +
//                           frame.getLocation().getX() + " y " + frame.getLocation().getY());
      guiTest.assertTrueVerbose( frame.getTitle() + " not cascaded in X",
                                 ((frame.getLocation().getX() > frameX) ||
                                  (frame.getLocation().getX() == 0.0)), "not ");
      frameX = frame.getLocation().getX();
      guiTest.assertTrueVerbose( frame.getTitle() + " not cascaded in Y",
                                 (frame.getLocation().getY() > frameY), "not ");
      frameY = frame.getLocation().getY();
    }

    System.err.println( "\nPLANVIZ_08 COMPLETED\n");
  } // end planViz08

  public static void planViz09( List sequenceUrls, PlanWorks planWorks, JFCTestHelper helper,
                                PlanWorksGUITest guiTest) throws Exception {
    //  Use "SequenceQuery for test-seq-2" to create a "Steps" and a
    //      "Transactions" QueryResult for ..." window.
    String sequenceName = PWTestHelper.SEQUENCE_NAME + " (2)";
    //String viewName = ViewConstants.SEQUENCE_QUERY_TITLE + " for " + sequenceName;
    int stepNumber = 1;
//     SequenceQueryWindow  seqQueryWindow =
//       PWTestHelper.getSequenceQueryWindow( sequenceName, helper, guiTest);

//     PWTestHelper.selectComboBoxItem( seqQueryWindow,
//                                      SequenceQueryWindow.MajorTypeComboBox.class,
//                                      SequenceQueryWindow.QUERY_FOR_STEPS,
//                                      helper, guiTest);
//     PWTestHelper.selectComboBoxItem( seqQueryWindow,
//                                      SequenceQueryWindow.MinorTypeComboBox.class,
//                                      SequenceQueryWindow.STEPS_WHERE_CONSTRAINT_TRANSACTED,
//                                      helper, guiTest);
//     PWTestHelper.selectComboBoxItem( seqQueryWindow,
//                                      SequenceQueryWindow.ConstraintTransComboBox.class,
//                                      SequenceQueryWindow.CONSTRAINT_TRANSACTION_TYPE_ALL,
//                                      helper, guiTest);
     ViewListener viewListener01 = new PlanWorksGUITest.ViewListenerWait01( guiTest);
//     PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
//                                      SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
//     viewListener01.viewWait();
//     int resultsWindowCount = 1;
//     StepQueryView stepQueryResults =
//       (StepQueryView) PWTestHelper.getQueryResultsWindow( StepQueryView.class,
//                                                           sequenceName, resultsWindowCount,
//                                                           helper, guiTest);
//     // try{Thread.sleep(2000);}catch(Exception e){}

//     PWTestHelper.selectComboBoxItem( seqQueryWindow,
//                                      SequenceQueryWindow.MajorTypeComboBox.class,
//                                      SequenceQueryWindow.QUERY_FOR_TRANSACTIONS,
//                                      helper, guiTest);
//     PWTestHelper.selectComboBoxItem( seqQueryWindow,
//                                      SequenceQueryWindow.MinorTypeComboBox.class,
//                                      SequenceQueryWindow.TRANSACTIONS_FOR_TOKEN,
//                                      helper, guiTest);
    PwPlanningSequence planSeq =
      planWorks.getCurrentProject().getPlanningSequence( (String) sequenceUrls.get( 5));
    PwPartialPlan partialPlan = planSeq.getPartialPlan( stepNumber);
    PwToken token = (PwToken) partialPlan.getTokenList().get( 0);
    // String fieldName = "Key";
//     PWTestHelper.setSequenceQueryField( seqQueryWindow, fieldName,
//                                         token.getId().toString(), helper, guiTest);
//     viewListener01.reset();
//     PWTestHelper.applySequenceQuery( seqQueryWindow, viewListener01,
//                                      SequenceQueryWindow.APPLY_QUERY_BUTTON, helper, guiTest);
//     viewListener01.viewWait();
//     resultsWindowCount = 2;
//     DBTransactionQueryView transQueryResults =
//       (DBTransactionQueryView) PWTestHelper.getQueryResultsWindow
//       ( DBTransactionQueryView.class, sequenceName, resultsWindowCount, helper, guiTest);
    // try{Thread.sleep(2000);}catch(Exception e){}

    // delete seq2 & seq3
    ThreadListener threadListener01 = new PlanWorksGUITest.ThreadListenerWait01( guiTest);
    PlanWorks.getPlanWorks().setDeleteSequenceThreadListener( threadListener01);
    PWTestHelper.deleteSequenceFromProject( (String) sequenceUrls.get( 5), helper, guiTest);
    threadListener01.threadWait();
    threadListener01.reset();
    PlanWorks.getPlanWorks().setDeleteSequenceThreadListener( threadListener01);
    PWTestHelper.deleteSequenceFromProject( (String) sequenceUrls.get( 6), helper, guiTest);
    threadListener01.threadWait();
    // Method:2 - Four windows remain: the ContentFilter, the SequenceQuery,
    //  the TimelineView, and the SequenceStepsView of "test-seq-1".
    int totalFrameCnt = 4;
    int internalFrameCnt = PWTestHelper.getAllInternalFrames().size();
    guiTest.assertTrueVerbose( "There are not " +  totalFrameCnt + " (found " +
                               internalFrameCnt + ")", (internalFrameCnt == totalFrameCnt),
                               "not ");
    int contentFilterCnt =
      PWTestHelper.getInternalFramesByPrefixName( ViewConstants.CONTENT_SPEC_TITLE).size();
//     int sequenceQueryCnt =
//       PWTestHelper.getInternalFramesByPrefixName( ViewConstants.SEQUENCE_QUERY_TITLE).size();
    int timelineViewCnt = PWTestHelper.getInternalFramesByPrefixName
      ( ViewConstants.TIMELINE_VIEW.replaceAll( " ", "")).size();
    int sequenceStepsViewCnt = PWTestHelper.getInternalFramesByPrefixName
        ( ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", "")).size();
    guiTest.assertTrueVerbose( "There is not 1 " + ViewConstants.CONTENT_SPEC_TITLE +
                               " window, " +
                               //" 1 " + ViewConstants.SEQUENCE_QUERY_TITLE + " window, " +
                               " 1 " + ViewConstants.TIMELINE_VIEW + " window, and " +
                               " 1 " + ViewConstants.SEQUENCE_STEPS_VIEW + " window.",
                               ((contentFilterCnt + /*sequenceQueryCnt +*/ timelineViewCnt +
                                 sequenceStepsViewCnt) == totalFrameCnt), "not ");

    // Re-add "test-seq-3" sequence by using menu-bar selection of
    //  "Project->Add Sequence ...".  Then open the sequence using
    //  "Planning Sequence->test-seq-3"
    String sequenceDirectory =  System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + PWTestHelper.GUI_TEST_DIR;
    File [] sequenceFileArray = new File [1];
    sequenceFileArray[0] = new File( sequenceDirectory +
                                      System.getProperty("file.separator") +
                                     sequenceUrls.get( 6));
    PWTestHelper.addPlanSequence( helper, guiTest, planWorks);
    PWTestHelper.addSequencesToProject( sequenceFileArray, helper, guiTest, planWorks);

    sequenceName = PWTestHelper.SEQUENCE_NAME + " (1)";
    viewListener01.reset();
    PWTestHelper.openSequenceStepsView( sequenceName, viewListener01, helper, guiTest);
    viewListener01.viewWait();
    SequenceStepsView seqStepsView =
      PWTestHelper.getSequenceStepsView( sequenceName, helper, guiTest);
    totalFrameCnt = 6;
    int totalSeqQueryCnt = 2, totalSeqStepsCnt = 2;
    internalFrameCnt = PWTestHelper.getAllInternalFrames().size();
    guiTest.assertTrueVerbose( "There are not " +  String.valueOf( totalFrameCnt) +
                               " frames (found " + String.valueOf( internalFrameCnt) + ")",
                               (internalFrameCnt == totalFrameCnt), "not ");
//     sequenceQueryCnt =
//       PWTestHelper.getInternalFramesByPrefixName( ViewConstants.SEQUENCE_QUERY_TITLE).size();
    sequenceStepsViewCnt = PWTestHelper.getInternalFramesByPrefixName
        ( ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", "")).size();
//     guiTest.assertTrueVerbose( "There are not 2 " + ViewConstants.SEQUENCE_QUERY_TITLE +
//                                " windows", (sequenceQueryCnt == totalSeqQueryCnt), "not ");
    guiTest.assertTrueVerbose( "There are not 2 " + ViewConstants.SEQUENCE_STEPS_VIEW +
                               " windows", (sequenceStepsViewCnt == totalSeqStepsCnt), "not ");
    guiTest.assertTrueVerbose( "The re-loaded sequence ("  + sequenceName +
                               ") is not the original" + " third sequence ",
                               seqStepsView.getPlanningSequence().getUrl().
                               equals( (String) sequenceUrls.get( 6)), "not ");
    //try{Thread.sleep(2000);}catch(Exception e){}

    threadListener01.reset();
    PlanWorks.getPlanWorks().setDeleteSequenceThreadListener( threadListener01);
    PWTestHelper.deleteSequenceFromProject( (String) sequenceUrls.get( 4), helper, guiTest);
    threadListener01.threadWait();
    PWTestHelper.deleteProject( PWTestHelper.PROJECT1, helper, guiTest);

    System.err.println( "\nPLANVIZ_09 COMPLETED\n");
  } // end planViz09


} // end class PlanWorksGUITest06to09
