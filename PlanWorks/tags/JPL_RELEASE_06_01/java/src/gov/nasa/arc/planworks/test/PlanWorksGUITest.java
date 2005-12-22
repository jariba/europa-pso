//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksGUITest.java,v 1.31 2005-11-10 01:22:10 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.finder.Finder;
import junit.framework.AssertionFailedError;
import junit.framework.TestSuite; 

import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.ThreadListener;
//import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.db.PwPartialPlan;    
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>PlanWorksGUITest</code> - PlanWorks/testCases/planViz.txt contains the script
 *                                 for this test suite 
 * calling JFCTest methods out of listeners invoked from the PlanWorks thread
 * results in IllegalMonitorStateException
 *   "This happens if the test case methods which synchronize the AWTEvent queue 
 *    are called from outside the TestCase thread."  Thus we set wait flags only.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                   NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PlanWorksGUITest extends JFCTestCase implements IdSource {
  
  // there must be a listener for every partial plan view - with this view ordering
  // PlanWorks.PARTIAL_PLAN_VIEW_LIST.size()

  protected static int CONSTRAINT_NETWORK_VIEW_INDEX;
  protected static int DB_TRANSACTION_VIEW_INDEX;
  protected static int DECISION_VIEW_INDEX;
  protected static int RESOURCE_PROFILE_VIEW_INDEX;
  protected static int RESOURCE_TRANSACTION_VIEW_INDEX;
  protected static int TEMPORAL_EXTENT_VIEW_INDEX;
  protected static int TIMELINE_VIEW_INDEX;
  protected static int TOKEN_NETWORK_VIEW_INDEX;

  protected PlanWorks planWorks;
  protected JFCTestHelper helper;

  protected List sequenceUrls; // element String
  protected Point popUpLocation;
  protected int entityIdInt;

  protected boolean viewListenerWait01;
  protected boolean viewListenerWait02;
  protected boolean viewListenerWait03;
  protected boolean viewListenerWait04;
  protected boolean viewListenerWait05;
  protected boolean viewListenerWait06;
  protected boolean viewListenerWait07;
  protected boolean viewListenerWait08;

  protected boolean threadListenerWait01;

  private String test;

  public PlanWorksGUITest(String test) {
    super(test);
    this.test = test;
  }

  // implements IdSource
  public int incEntityIdInt() {
    return ++entityIdInt;
  }
  
  // implements IdSource
  public void resetEntityIdInt() {
    entityIdInt = 0;
  }

  public boolean getViewListenerWait01() {
    return viewListenerWait01;
  }

  public void setViewListenerWait01( boolean value) {
    viewListenerWait01 = value;
  }

  public boolean getViewListenerWait02() {
    return viewListenerWait02;
  }

  public void setViewListenerWait02( boolean value) {
    viewListenerWait02 = value;
  }

  public boolean getViewListenerWait03() {
    return viewListenerWait03;
  }

  public void setViewListenerWait03( boolean value) {
    viewListenerWait03 = value;
  }

  public boolean getViewListenerWait04() {
    return viewListenerWait04;
  }

  public void setViewListenerWait04( boolean value) {
    viewListenerWait04 = value;
  }

  public boolean getViewListenerWait05() {
    return viewListenerWait05;
  }

  public void setViewListenerWait05( boolean value) {
    viewListenerWait05 = value;
  }

  public boolean getViewListenerWait06() {
    return viewListenerWait06;
  }

  public void setViewListenerWait06( boolean value) {
    viewListenerWait06 = value;
  }

  public boolean getViewListenerWait07() {
    return viewListenerWait07;
  }

  public void setViewListenerWait07( boolean value) {
    viewListenerWait07 = value;
  }

  public boolean getViewListenerWait08() {
    return viewListenerWait08;
  }

  public void setViewListenerWait08( boolean value) {
    viewListenerWait08 = value;
  }

  public boolean getThreadListenerWait01() {
    return threadListenerWait01;
  }

  public void setThreadListenerWait01( boolean value) {
    threadListenerWait01 = value;
  }


  public void setUp() throws Exception {
    try {
      helper = new JFCTestHelper();
      this.setHelper( helper);

      // assertNotNull( "before planworks ", null);

      File configFile = new File( System.getProperty( "planworks.config") + ".template");
      ConfigureAndPlugins.processPlanWorksConfigFile( configFile);

      configFile = new File( System.getProperty( "projects.config") + ".template");
      ConfigureAndPlugins.processProjectsConfigFile( configFile);

      CONSTRAINT_NETWORK_VIEW_INDEX =
        PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.CONSTRAINT_NETWORK_VIEW);
      // DB_TRANSACTION_VIEW_INDEX =
//         PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.DB_TRANSACTION_VIEW);
      DECISION_VIEW_INDEX =
        PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.DECISION_VIEW);
      RESOURCE_PROFILE_VIEW_INDEX =
        PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.RESOURCE_PROFILE_VIEW);
      RESOURCE_TRANSACTION_VIEW_INDEX =
        PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.RESOURCE_TRANSACTION_VIEW);
      TEMPORAL_EXTENT_VIEW_INDEX =
        PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.TEMPORAL_EXTENT_VIEW);
      TIMELINE_VIEW_INDEX =
        PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.TIMELINE_VIEW);
      TOKEN_NETWORK_VIEW_INDEX =
        PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf( ViewConstants.TOKEN_NETWORK_VIEW);

      planWorks = new PlanWorks( PlanWorks.buildConstantMenus(),
                                 System.getProperty( "name.application"),
                                 System.getProperty( "boolean.isMaxTestScreen"),
                                 System.getProperty( "os.type"),
                                 System.getProperty( "planworks.root"));
      PlanWorks.setPlanWorks( planWorks);
      popUpLocation = new Point( (int) (PlanWorks.getPlanWorks().getWidth() / 2),
                                 (int) (PlanWorks.getPlanWorks().getHeight() / 2));

      // assertNotNull( "after planworks ", null);

      int numSequences = 7, numSteps = 2;
      // 1-4 used by planViz01 & planViz02 (destructively modified)
      // 5-6 used by planViz03, planViz04 & planViz05
      // 5-7 used by planViz06, planViz07, & planViz08
      sequenceUrls = PWSetupHelper.buildTestData( numSequences, numSteps, this,
                                                  PWTestHelper.GUI_TEST_DIR);

      // System.exit( 0);

      // turn on debugging
      //     JFCEventManager.setDebug(true);
      //     JFCEventManager.setDebugType(JFCEventManager.DEBUG_OUTPUT);
      //     JFCEventManager.setRecording(true);

      flushAWT(); awtSleep();
    } catch (Exception excp) {
      excp.printStackTrace();
      System.exit( -1);
    }
  }

  public void tearDown() throws Exception {
    super.tearDown();
    helper.cleanUp(this);
    //System.exit(0);
  }

  // catch assert errors and Exceptions here, since JUnit seems to not do it 
  public void planVizTests() throws Exception {
    try {
      PlanWorksGUITest01.planViz01( sequenceUrls, planWorks, helper, this);
      PlanWorksGUITest02.planViz02( sequenceUrls, planWorks, helper, this);
      PlanWorksGUITest0304.planViz0304( sequenceUrls, planWorks, helper, this);
      PlanWorksGUITest05.planViz05( sequenceUrls, planWorks, helper, this);
      PlanWorksGUITest06to09.planViz06to09( sequenceUrls, new FrameXAscending(),
                                            planWorks, helper, this);
      PlanWorksGUITest10.planViz10( sequenceUrls, planWorks, helper, this);
      PlanWorksGUITest11.planViz11( sequenceUrls, planWorks, helper, this);
      PlanWorksGUITest12.planViz12( sequenceUrls, planWorks, helper, this);
      // to do
      // PlanWorksGUITest13.planViz13 - Methods 8-15
      //PlanWorksGUITest13.planViz13( sequenceUrls, planWorks, helper, this);
      // to do
      // PlanWorksGUITest14 - PlanWorksGUITest25

      PWTestHelper.exitPlanWorks( helper, this);

    } catch (AssertionFailedError err) {
      err.printStackTrace();
      System.exit( -1);
    } catch (Exception excp) {
      excp.printStackTrace();
      System.exit( -1);
    }
  } // end planVizTests

  public class FrameXAscending implements Comparator {
    public FrameXAscending() {
    }
    public int compare( Object o1, Object o2) {
      Double s1 = new Double( ((MDIInternalFrame) o1).getLocation().getX());
      Double s2 = new Double( ((MDIInternalFrame) o2).getLocation().getX());
      return s1.compareTo( s2);
    }

    public boolean equals( Object o1, Object o2) {
      Double s1 = new Double( ((MDIInternalFrame) o1).getLocation().getX());
      Double s2 = new Double( ((MDIInternalFrame) o2).getLocation().getX());
      return s1.equals( s2);
    }
  } // end class FrameXAscending


//   public static class PwDBTransactionFunctor implements BooleanFunctor {
//     private Integer transId;
//     public PwDBTransactionFunctor(Integer transId){ this.transId = transId; }
//     public boolean func(Object o){return ((PwDBTransaction)o).getId().equals(transId) ;}
//   }



  protected void invokeAllViewsSelections( String viewName1, int viewIndx1, String viewName2,
                                           int viewIndx2, String viewNameSuffix,
                                           SequenceStepsView seqStepsView,
                                           PwPartialPlan partialPlan,
                                           ViewManager viewMgr, int stepNumber)
    throws Exception {
   List viewListenerList = createViewListenerList();

   String viewMenuItemName = "Open " + viewName1;
   PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                               viewMenuItemName, viewListenerList, helper, this);
   viewListenerListWait( viewIndx1, viewListenerList);
   viewMenuItemName = "Open " + viewName2;
   PWTestHelper.seqStepsViewStepItemSelection( seqStepsView, stepNumber,
                                               viewMenuItemName, viewListenerList, helper, this);
   viewListenerListWait( viewIndx2, viewListenerList);
   int numViews = viewMgr.getViewSet( partialPlan).getViews().keySet().size();
   assertTrueVerbose( "planSeq step " + stepNumber + " does not have 2 views",
                      (numViews == 2), "not ");
   PartialPlanView view1 =
     (PartialPlanView) PWTestHelper.getPartialPlanView( viewName1, viewNameSuffix, this);
   PartialPlanView view2 =
     (PartialPlanView) PWTestHelper.getPartialPlanView( viewName2, viewNameSuffix, this);

   viewMenuItemName = "Hide All Views";
   PWTestHelper.viewBackgroundItemSelection( view1, viewMenuItemName, viewListenerList,
                                             helper, this);
   boolean isIconified = view1.getViewFrame().isIcon();
   assertTrueVerbose( viewName1 + " is not iconified", isIconified, "not ");

   PWTestHelper.selectWindow( viewName1.replaceAll( " ", "") + " of " + viewNameSuffix,
                              helper, this);

   isIconified = view1.getViewFrame().isIcon();
   assertTrueVerbose( viewName1 + " is not shown", (isIconified == false), "not ");

   viewMenuItemName = "Show All Views";
   PWTestHelper.viewBackgroundItemSelection( view1, viewMenuItemName, viewListenerList,
                                             helper, this);
   isIconified = view2.getViewFrame().isIcon();
   assertTrueVerbose( viewName2 + " is not shown", (isIconified == false), "not ");

   viewMenuItemName = "Open All Views";
   PWTestHelper.viewBackgroundItemSelection( view1, viewMenuItemName, viewListenerList,
                                             helper, this);
   waitForAllViews( viewName1, viewListenerList);
   numViews = viewMgr.getViewSet( partialPlan).getViews().keySet().size();
   int expectedNumViews = PlanWorks.PARTIAL_PLAN_VIEW_LIST.size();
   assertTrueVerbose( "planSeq step " + stepNumber + " does not have " +
                      expectedNumViews + " views", (numViews == expectedNumViews), "not ");

   viewMenuItemName = "Close All Views";
   PWTestHelper.viewBackgroundItemSelection( view1, viewMenuItemName, viewListenerList,
                                             helper, this);
   ViewSet viewSet = viewMgr.getViewSet( partialPlan);
   assertNullVerbose( "planSeq step " + stepNumber + " does not have 0 views", viewSet, "not ");
  } // end invokeAllViewsSelections

  protected void waitForAllViews( String viewName, List viewListenerList) throws Exception {
    if (viewName.equals( ViewConstants.CONSTRAINT_NETWORK_VIEW)) {
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    }//  else if (viewName.equals( ViewConstants.DB_TRANSACTION_VIEW)) {
//       viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
//       viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
//       viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
//       viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
//       viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
//       viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);}
    else if (viewName.equals( ViewConstants.DECISION_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      //viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.RESOURCE_PROFILE_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      //viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.RESOURCE_TRANSACTION_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      //viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.TEMPORAL_EXTENT_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      //viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TOKEN_NETWORK_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.TIMELINE_VIEW)) {
      viewListenerListWait( CONSTRAINT_NETWORK_VIEW_INDEX, viewListenerList);
      //viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
    } else if (viewName.equals( ViewConstants.TOKEN_NETWORK_VIEW)) {
	//viewListenerListWait( DB_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( DECISION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_PROFILE_VIEW_INDEX, viewListenerList);
      viewListenerListWait( RESOURCE_TRANSACTION_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TEMPORAL_EXTENT_VIEW_INDEX, viewListenerList);
      viewListenerListWait( TIMELINE_VIEW_INDEX, viewListenerList);
    }
  } // end waitForAllViews

  protected void createOverviewWindow( String viewName, PartialPlanView view,
                                       String viewNameSuffix) throws Exception {
    String viewMenuItemName = "Overview Window";
    PWTestHelper.viewBackgroundItemSelection( view, viewMenuItemName, helper, this);
    String overviewViewName = Utilities.trimView( viewName).replaceAll( " ", "") +
      ViewConstants.OVERVIEW_TITLE + viewNameSuffix;
      VizViewOverview viewOverview =
      (VizViewOverview) PWTestHelper.findComponentByName
      ( VizViewOverview.class, overviewViewName, Finder.OP_EQUALS);
    assertNotNullVerbose( overviewViewName + " not found", viewOverview, "not ");
  } // end createOverviewWindow







  public void assertTrueVerbose( String failureMsg, boolean condition, String replacement)
    throws AssertionFailedError {
    if (condition) {
      System.err.println( "AssertTrue: " + failureMsg.replaceAll( replacement, ""));
    } else {
      throw new AssertionFailedError( failureMsg);
    }
  } // end assertTrueVerbose

  public void assertFalseVerbose( String failureMsg, boolean condition, String replacement)
    throws AssertionFailedError {
    if (! condition) {
      System.err.println( "AssertFalse: " + failureMsg.replaceAll( replacement, ""));
    } else {
      throw new AssertionFailedError( failureMsg);
    }
  } // end assertFalseVerbose

  public void assertNotNullVerbose( String failureMsg, Object object, String replacement)
    throws AssertionFailedError{
    if (object != null) {
      System.err.println( "AssertNotNull: " + failureMsg.replaceAll( replacement, ""));
    } else {
      throw new AssertionFailedError( failureMsg);
    }
  } // end assertNotNullVerbose


  public void assertNullVerbose( String failureMsg, Object object, String replacement)
    throws AssertionFailedError{
    if (object == null) {
      System.err.println( "AssertNull: " + failureMsg.replaceAll( replacement, ""));
    } else {
      throw new AssertionFailedError( failureMsg);
    }
  } // end assertNullVerbose



  // needed for calls to PWTestHelper.seqStepsViewStepItemSelection
  protected List createViewListenerList() {
    List viewListenerList = new ArrayList();
    viewListenerList.add( new ViewListenerWait01( this));
    viewListenerList.add( new ViewListenerWait02( this));
    viewListenerList.add( new ViewListenerWait03( this));
    viewListenerList.add( new ViewListenerWait04( this));
    viewListenerList.add( new ViewListenerWait05( this));
    viewListenerList.add( new ViewListenerWait06( this));
    viewListenerList.add( new ViewListenerWait07( this));
    viewListenerList.add( new ViewListenerWait08( this));
    if (viewListenerList.size() != PlanWorks.PARTIAL_PLAN_VIEW_LIST.size()) {
      System.err.println( "createViewListenerList: num listeners not = " +
                           PlanWorks.PARTIAL_PLAN_VIEW_LIST.size());
      System.exit( -1);
    }
    return viewListenerList;
  } // end createViewListenerList

  protected void viewListenerListWait( int viewIndex, List viewListenerList) {
    if (viewIndex == CONSTRAINT_NETWORK_VIEW_INDEX) {
      ((ViewListenerWait01)  viewListenerList.get( CONSTRAINT_NETWORK_VIEW_INDEX)).viewWait();
    } else if (viewIndex == DB_TRANSACTION_VIEW_INDEX) {
      ((ViewListenerWait02)  viewListenerList.get( DB_TRANSACTION_VIEW_INDEX)).viewWait();
    } else if (viewIndex == DECISION_VIEW_INDEX) {
      ((ViewListenerWait03)  viewListenerList.get( DECISION_VIEW_INDEX)).viewWait();
    } else if (viewIndex == RESOURCE_PROFILE_VIEW_INDEX) {
      ((ViewListenerWait04)  viewListenerList.get( RESOURCE_PROFILE_VIEW_INDEX)).viewWait();
    } else if (viewIndex == RESOURCE_TRANSACTION_VIEW_INDEX) {
      ((ViewListenerWait05)  viewListenerList.get( RESOURCE_TRANSACTION_VIEW_INDEX)).viewWait();
    } else if (viewIndex == TEMPORAL_EXTENT_VIEW_INDEX) {
      ((ViewListenerWait06)  viewListenerList.get( TEMPORAL_EXTENT_VIEW_INDEX)).viewWait();
    } else if (viewIndex == TIMELINE_VIEW_INDEX) {
      ((ViewListenerWait07)  viewListenerList.get( TIMELINE_VIEW_INDEX)).viewWait();
    } else if (viewIndex == TOKEN_NETWORK_VIEW_INDEX) {
      ((ViewListenerWait08)  viewListenerList.get(  TOKEN_NETWORK_VIEW_INDEX)).viewWait();
    } else {
      System.err.println( "viewListenerListWait: listener for index " + viewIndex +
                          " not found");
      System.exit( -1);
    }
  } // end viewListenerListWait


  protected void viewListenerListReset( int viewIndex, List viewListenerList) {
    if (viewIndex == CONSTRAINT_NETWORK_VIEW_INDEX) {
      ((ViewListenerWait01)  viewListenerList.get( CONSTRAINT_NETWORK_VIEW_INDEX)).reset();
    } else if (viewIndex == DB_TRANSACTION_VIEW_INDEX) {
      ((ViewListenerWait02)  viewListenerList.get( DB_TRANSACTION_VIEW_INDEX)).reset();
    } else if (viewIndex == DECISION_VIEW_INDEX) {
      ((ViewListenerWait03)  viewListenerList.get( DECISION_VIEW_INDEX)).reset();
    } else if (viewIndex == RESOURCE_PROFILE_VIEW_INDEX) {
      ((ViewListenerWait04)  viewListenerList.get( RESOURCE_PROFILE_VIEW_INDEX)).reset();
    } else if (viewIndex == RESOURCE_TRANSACTION_VIEW_INDEX) {
      ((ViewListenerWait05)  viewListenerList.get( RESOURCE_TRANSACTION_VIEW_INDEX)).reset();
    } else if (viewIndex == TEMPORAL_EXTENT_VIEW_INDEX) {
      ((ViewListenerWait06)  viewListenerList.get( TEMPORAL_EXTENT_VIEW_INDEX)).reset();
    } else if (viewIndex == TIMELINE_VIEW_INDEX) {
      ((ViewListenerWait07)  viewListenerList.get( TIMELINE_VIEW_INDEX)).reset();
    } else if (viewIndex == TOKEN_NETWORK_VIEW_INDEX) {
      ((ViewListenerWait08)  viewListenerList.get(  TOKEN_NETWORK_VIEW_INDEX)).reset();
    } else {
      System.err.println( "viewListenerListReset: listener for index " + viewIndex +
                          " not found");
      System.exit( -1);
    }
  } // end viewListenerListReset


  public static TestSuite suite() {
    TestSuite suite = new TestSuite();
    // this does not work -- 
    // suite.addTest(new PlanWorksGUITest("planViz01"));
    // suite.addTest(new PlanWorksGUITest("planViz02"));

    suite.addTest( new PlanWorksGUITest( "planVizTests"));
    return suite;
  }

//   public static void main(String [] args) {
//     TestRunner.run(suite());
//   }


  public static class ViewListenerWait01 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait01( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait01( false); 
    }
    public void reset() {
      guiTest.setViewListenerWait01( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait01( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait01()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait01 still false");
      }
      guiTest.flushAWT(); guiTest.awtSleep();
    } 
  } // end class ViewListenerWait01

  public static class ViewListenerWait02 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait02( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait02( false);
    }
    public void reset() {
      guiTest.setViewListenerWait02( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait02( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait02()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait02 still false");
      }
      guiTest.flushAWT(); guiTest.awtSleep();
    } 
  } // end class ViewListenerWait02

  public static class ViewListenerWait03 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait03( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait03( false);
    }
    public void reset() {
      guiTest.setViewListenerWait03( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait03( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait03()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait03 still false");
      }
      guiTest.flushAWT(); guiTest.awtSleep();
    } 
  } // end class ViewListenerWait03

  public static class ViewListenerWait04 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait04( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait04( false);
    }
    public void reset() {
      guiTest.setViewListenerWait04( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait04( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait04()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait04 still false");
      }
      guiTest.flushAWT(); guiTest.awtSleep();
    } 
  } // end class ViewListenerWait04

  public static class ViewListenerWait05 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait05( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait05( false);
    }
    public void reset() {
      guiTest.setViewListenerWait05( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait05( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait05()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait05 still false");
      }
      guiTest.flushAWT(); guiTest.awtSleep();
    } 
  } // end class ViewListenerWait05

  public static class ViewListenerWait06 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait06( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait06( false);
    }
    public void reset() {
      guiTest.setViewListenerWait06( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait06( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait06()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait06 still false");
      }
      guiTest.flushAWT(); guiTest.awtSleep();
    } 
  } // end class ViewListenerWait06

  public static class ViewListenerWait07 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait07( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait07( false);
    }
    public void reset() {
      guiTest.setViewListenerWait07( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait07( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait07()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait07 still false");
      }
      guiTest.flushAWT(); guiTest.awtSleep();
    } 
  } // end class ViewListenerWait07

  public static class ViewListenerWait08 extends ViewListener {
    private PlanWorksGUITest guiTest;
    public ViewListenerWait08( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setViewListenerWait08( false);
    }
    public void reset() {
      guiTest.setViewListenerWait08( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setViewListenerWait08( true);
    }
    public void viewWait() {
      while (! guiTest.getViewListenerWait08()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewListenerWait08 still false");
      }
      guiTest.flushAWT(); guiTest.awtSleep();
    } 
  } // end class ViewListenerWait08

  public static class ThreadListenerWait01 extends ThreadListener {
    private PlanWorksGUITest guiTest;
    public ThreadListenerWait01( PlanWorksGUITest guiTest) {
      super();
      this.guiTest = guiTest;
      guiTest.setThreadListenerWait01( false);
    }
    public void reset() {
      guiTest.setThreadListenerWait01( false);
    }
    public void threadEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      guiTest.setThreadListenerWait01( true);
    }
    public void threadWait() {
      while (! guiTest.getThreadListenerWait01()) {
        try {
          Thread.currentThread().sleep(50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "threadListenerWait01 still false");
      }
      guiTest.flushAWT(); guiTest.awtSleep();
    } 
  } // end class ThreadListenerWait01


} // end class PlanWorksGUITest



