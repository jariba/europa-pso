// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceStepsView.java,v 1.45 2004-09-24 22:40:01 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 24sep03
//

package gov.nasa.arc.planworks.viz.sequence.sequenceSteps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoEllipse;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwListener;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.util.ProgressMonitorThread;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>SequenceStepsView</code> - render a histogram of plan data base size
 *                        over sequence steps
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SequenceStepsView extends SequenceView {

  private static Object staticObject = new Object();

  /**
   * constant <code>DB_TOKENS</code>
   *
   */
  protected static final String DB_TOKENS = "numTokens";

  /**
   * constant <code>DB_VARIABLES</code>
   *
   */
  protected static final String DB_VARIABLES = "numVariables";

  /**
   * constant <code>DB_CONSTRAINTS</code>
   *
   */
  protected static final String DB_CONSTRAINTS = "numConstraints";

  /**
   * constant <code>TOKENS_BG_COLOR</code>
   *
   */
  public static final Color TOKENS_BG_COLOR = ColorMap.getColor( "seaGreen1");

  /**
   * constant <code>VARIABLES_BG_COLOR</code>
   *
   */
  public static final Color VARIABLES_BG_COLOR = ColorMap.getColor( "skyBlue");

  /**
   * constant <code>DB_CONSTRAINTS_BG_COLOR</code>
   *
   */
  public static final Color DB_CONSTRAINTS_BG_COLOR = ColorMap.getColor( "lightYellow");

  private static final int WINDOW_WIDTH = 400;
  private static final int WINDOW_HEIGHT= 150;
  private static final int MIN_NUM_STEPS_TO_CALL_EXPAND_FRAME = 30;

  private long startTimeMSecs;
  private PwPlanningSequence planSequence;
  private ViewSet viewSet;
  private SequenceStepsJGoView jGoView;
  private JGoDocument document;
  private Graphics graphics;
  private FontMetrics fontMetrics;
  private Font font;
  private float heightScaleFactor;
  private List stepElementList;
  private List stepRepresentedList;
  private List statusIndicatorList;
  private List stepNumberList;
  private int numOperations;
  private ProgressMonitorThread initPMThread;
  private ProgressMonitorThread redrawPMThread;

  /**
   * variable <code>selectedStepElement</code>
   *
   */
  protected StepElement selectedStepElement;

  /**
   * <code>SequenceChangeListener</code> - 
   *
   */
  class SequenceChangeListener implements PwListener {

    private SequenceStepsView view;

    /**
     * <code>SequenceChangeListener</code> - constructor 
     *
     * @param view - <code>SequenceStepsView</code> - 
     */
    public SequenceChangeListener( final SequenceStepsView view) {
      this.view = view;
    }

    /**
     * <code>fireEvent</code>
     *
     * @param evtName - <code>String</code> - 
     */
    public final void fireEvent( final String evtName) {
      if (evtName.equals( PwPlanningSequence.EVT_PP_ADDED) ||
          evtName.equals( PwPlanningSequence.EVT_PP_REMOVED)) {
        //         System.err.println( "SequenceStepsView.fireEvent  view.redraw()");
        view.redraw();
      }
    }
  }

  /**
   * <code>SequenceStepsView</code> - constructor 
   *                             Use SswingWorker to
   *                             properly render the JGo widgets
   *
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public SequenceStepsView( final ViewableObject planSequence,  final ViewSet viewSet) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    sequenceStepsInit( planSequence, viewSet);
  }

  /**
   * <code>SequenceStepsView</code> - constructor 
   *
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public SequenceStepsView( final ViewableObject planSequence,  final ViewSet viewSet,
                            final ViewListener viewListener) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    sequenceStepsInit( planSequence, viewSet);
    if (viewListener != null) {
      addViewListener( viewListener);
    }
  }

  private void sequenceStepsInit( final ViewableObject planSequence,  final ViewSet viewSet) {
    this.planSequence = (PwPlanningSequence) planSequence;
    this.viewSet = (SequenceViewSet) viewSet;
    statusIndicatorList = new ArrayList();
    stepRepresentedList = new ArrayList();
    stepNumberList = new ArrayList();
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    stepElementList = new ArrayList();
    selectedStepElement = null;

    jGoView = new SequenceStepsJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);
    ViewListener viewListener = null;
    viewFrame = viewSet.openView( this.getClass().getName(), viewListener);
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());

    ((PwPlanningSequence) planSequence).addListener( new SequenceChangeListener( this));

    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor


//   Runnable runInit = new Runnable() {
//       public final void run() {
//         init();
//       }
//     };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use SwingWorker in constructor
   */
  public void init() {
    handleEvent(ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for SequenceStepsView instance to become displayable
    if (! ViewGenerics.displayableWait( SequenceStepsView.this)) {
      closeView( this);
      return;
    }

    this.computeFontMetrics( this);

    document = jGoView.getDocument();

    numOperations = planSequence.getPlanDBSizeList().size() * 2;
    initPMThread =
      createProgressMonitorThread( "Rendering Sequence Steps View ...", 0, numOperations,
			     Thread.currentThread(), this);
    if (! progressMonitorWait( initPMThread, this)) {
      closeView( this);
      return;
    }
    try {
      ViewGenerics.setRedrawCursor( PlanWorks.getPlanWorks());

      numOperations = 0;
      heightScaleFactor = computeHeightScaleFactor();
      if (heightScaleFactor == -1.0f) {
        ViewGenerics.resetRedrawCursor( PlanWorks.getPlanWorks());
        closeView( this);
	initPMThread.setProgressMonitorCancel();
        return;
      }

      boolean isValid = renderHistogram( initPMThread);
      if (! isValid) {
        ViewGenerics.resetRedrawCursor( PlanWorks.getPlanWorks());
        closeView( this);
	initPMThread.setProgressMonitorCancel();
        return;
      }
      // PlannerController creates a view with small number of steps -- do not squish it
      if (planSequence.getPlanDBSizeList().size() > MIN_NUM_STEPS_TO_CALL_EXPAND_FRAME) {
        expandViewFrame( viewFrame,
                         (int) jGoView.getDocumentSize().getWidth(),
                         (int) jGoView.getDocumentSize().getHeight());
      } else {
        viewFrame.setSize( (int) Math.max( viewFrame.getSize().getWidth(), WINDOW_WIDTH),
                           (int) Math.max( viewFrame.getSize().getHeight(), WINDOW_HEIGHT));
      }
      long stopTimeMSecs = System.currentTimeMillis();
      System.err.println( "   ... " + ViewConstants.SEQUENCE_STEPS_VIEW + " elapsed time: " +
                          (stopTimeMSecs -
                           PlanWorks.getPlanWorks().getViewRenderingStartTime
                           ( ViewConstants.SEQUENCE_STEPS_VIEW)) + " msecs.");
    } finally {
      ViewGenerics.resetRedrawCursor( PlanWorks.getPlanWorks());
    }
    startTimeMSecs = 0L;
    initPMThread.setProgressMonitorCancel();
    handleEvent(ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init


  /**
   * <code>redraw</code>
   *
   */
  public final void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority( Thread.MIN_PRIORITY);
    thread.start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public final void run() {
      redrawView();
    } //end run

  } // end class RedrawViewThread

  private void redrawView() {
    synchronized( staticObject) {
      handleEvent(ViewListener.EVT_REDRAW_BEGUN_DRAWING);
      System.err.println( "Redrawing Sequence Steps View ...");
      if (startTimeMSecs == 0L) {
        startTimeMSecs = System.currentTimeMillis();
      }
      try {
        ViewGenerics.setRedrawCursor( PlanWorks.getPlanWorks());

        //document.deleteContents();
        for (Iterator it = statusIndicatorList.listIterator(); it.hasNext();) {
          document.removeObject( (JGoObject) it.next());
        }
        for(Iterator it = stepNumberList.listIterator(); it.hasNext();) {
          document.removeObject((JGoObject) it.next());
        }
        statusIndicatorList.clear();
    
        numOperations = planSequence.getPlanDBSizeList().size();
        redrawPMThread =
          createProgressMonitorThread( "Redrawing Sequence Steps View ...", 0, numOperations,
                                       Thread.currentThread(), this);
        numOperations = 0;
        if (! progressMonitorWait( redrawPMThread, this)) {
          closeView( this);
          return;
        }
        boolean isValid = renderHistogram( redrawPMThread);
        if (! isValid) {
          ViewGenerics.resetRedrawCursor( PlanWorks.getPlanWorks());
          closeView( this);
          redrawPMThread.setProgressMonitorCancel();
          return;
        }

        // PlannerController creates a view with small number of steps -- do not squish it
        if (planSequence.getPlanDBSizeList().size() > MIN_NUM_STEPS_TO_CALL_EXPAND_FRAME) {
          expandViewFrame( viewFrame,
                           (int) jGoView.getDocumentSize().getWidth(),
                           (int) jGoView.getDocumentSize().getHeight());
        }

      } finally {
        ViewGenerics.resetRedrawCursor( PlanWorks.getPlanWorks());
      }
      long stopTimeMSecs = System.currentTimeMillis();
      System.err.println( "   ... " + ViewConstants.SEQUENCE_STEPS_VIEW + " elapsed time: " +
                          (stopTimeMSecs - startTimeMSecs) + " msecs.");
      startTimeMSecs = 0L;
      redrawPMThread.setProgressMonitorCancel();
      handleEvent(ViewListener.EVT_REDRAW_ENDED_DRAWING);
    }
  } // end redrawView


  /**
   * <code>getPlanningSequence</code>
   *
   * @return - <code>PwPlanningSequence</code> - 
   */
  public final PwPlanningSequence getPlanningSequence() {
    return planSequence;
  }

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public final JGoDocument getJGoDocument()  {
    return this.document;
  }

  /**
   * <code>getJGoView</code> - needed for PlanWorksTest
   *
   * @return - <code>JGoView</code> - 
   */
  public final JGoView getJGoView()  {
    return jGoView;
  }

  /**
   * <code>getFontMetrics</code>
   *
   * @return - <code>FontMetrics</code> - 
   */
  public final FontMetrics getFontMetrics()  {
    return fontMetrics;
  }

  /**
   * <code>getStepElementList</code>
   *
   * @return - <code>List</code> - of List of StepElement
   */
  public final List getStepElementList() {
    return stepElementList;
  }

  /**
   * <code>getSelectedStepElement</code>
   *
   * @return - <code>StepElement</code> - 
   */
  public final StepElement getSelectedStepElement() {
    return selectedStepElement;
  }

  /**
   * <code>setSelectedStepElement</code>
   *
   * @param element - <code>StepElement</code> - 
   */
  public final void setSelectedStepElement( final StepElement element) {
    selectedStepElement = element;
  }

  // also called by createRefreshItem with no progressMonitor
  private float computeHeightScaleFactor() {
    int maxDbSize = 0;
    Iterator sizeItr = planSequence.getPlanDBSizeList().iterator();
    while (sizeItr.hasNext()) {
      int [] planDbSizes = (int[]) sizeItr.next();
      int dbSize = planDbSizes[0] + planDbSizes[1] + planDbSizes[2];
//       System.err.println( "dbSize " + dbSize + " " + planDbSizes[0] +
//                           " " + planDbSizes[1] + " " + planDbSizes[2]);
      if (dbSize > maxDbSize) {
        maxDbSize = dbSize;
      }
      if ((initPMThread != null) && (initPMThread.getProgressMonitor() != null) &&
          initPMThread.getProgressMonitor().isCanceled()) {
        String msg = "User Canceled Sequence Steps View Rendering";
        System.err.println( msg);
	initPMThread.setProgressMonitorCancel();
        return -1.0f;
      }
      if ((initPMThread != null) && (initPMThread.getProgressMonitor() != null)) {
        numOperations++;
        initPMThread.getProgressMonitor().setProgress
	  ( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
      }
    }
//     System.err.println( "computeHeightScaleFactor: " +
//                         ViewConstants.STEP_VIEW_Y_MAX / (float) maxDbSize +
//                         " maxDbSize " + maxDbSize);
    return ViewConstants.STEP_VIEW_Y_MAX / (float) maxDbSize;
  } // end computeHeightScaleFactor

  private boolean renderHistogram( ProgressMonitorThread pmThread) {
    // System.err.println( "stepNumbers " + planSequence.getPartialPlanNamesList());
    int x = ViewConstants.STEP_VIEW_X_INIT;
    Iterator sizeItr = planSequence.getPlanDBSizeList().iterator();
    int stepNumber = 0;
    while (sizeItr.hasNext()) {
      int y = ViewConstants.STEP_VIEW_Y_INIT;
      String partialPlanName = "step" + String.valueOf( stepNumber);

      addStepStatusIndicator( stepNumber, x, document);
      
      int [] planDbSizes = (int[]) sizeItr.next();
      if(stepRepresentedList.contains(partialPlanName)) {
        ArrayList elementList = (ArrayList) stepElementList.get( stepNumber);
        for(int i = 0; i < 3; i++) {
          int height = Math.max(1, (int) (planDbSizes[i] * heightScaleFactor));
          StepElement elem = (StepElement) elementList.get(i);
          elem.setHeight(height);
          elem.setLocation(x, y);
          y += height;
        }
      }
      if(!stepRepresentedList.contains( partialPlanName)) {
        int height = Math.max( 1, (int) (planDbSizes[0] * heightScaleFactor));
        List elementList = new ArrayList();
        StepElement stepElement = new StepElement( x, y, height, DB_TOKENS,
                                                   planDbSizes[0], TOKENS_BG_COLOR,
                                                   partialPlanName, planSequence, this);
        elementList.add( stepElement);
        document.addObjectAtTail( stepElement);
        y += height;
        
        height = Math.max( 1, (int) (planDbSizes[1] * heightScaleFactor));
        stepElement = new StepElement( x, y, height, DB_VARIABLES,
                                       planDbSizes[1], VARIABLES_BG_COLOR,
                                       partialPlanName, planSequence, this);
        elementList.add( stepElement);
        document.addObjectAtTail( stepElement);
        y += height;
        
        height = Math.max( 1, (int) (planDbSizes[2] * heightScaleFactor));
        stepElement = new StepElement( x, y, height, DB_CONSTRAINTS,
                                       planDbSizes[2], DB_CONSTRAINTS_BG_COLOR,
                                       partialPlanName, planSequence, this);
        elementList.add( stepElement);
        document.addObjectAtTail( stepElement);
        y += height;

        stepElementList.add( elementList);
        stepRepresentedList.add( partialPlanName);
      }
      // display step number for every 10th step
      if ((stepNumber % 10) == 0) {
        JGoText textObject = new JGoText( new Point( x, y + 4), String.valueOf( stepNumber));
        textObject.setResizable( false);
        textObject.setEditable( false);
        textObject.setDraggable( false);
        textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
        stepNumberList.add(textObject);
        document.addObjectAtTail( textObject);
      }
      x += ViewConstants.STEP_VIEW_STEP_WIDTH;
      if (pmThread.getProgressMonitor().isCanceled()) {
        String msg = "User Canceled Sequence Steps View Rendering";
        System.err.println( msg);
	pmThread.setProgressMonitorCancel();
        return false;
      }
      stepNumber++;
      numOperations++;
      pmThread.getProgressMonitor().setProgress
	( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
    }
    return true;
  } // end renderHistogram

  private void addStepStatusIndicator( final int stepNum, final int x, final JGoDocument doc) {
    JGoEllipse statusIndicator = new JGoEllipse(new Point(x + 4, 
                                                          ViewConstants.STEP_VIEW_Y_INIT - 6),
                                                new Dimension(4, 4));
    statusIndicator.setDraggable( false);
    statusIndicator.setResizable( false);
    statusIndicator.setSelectable( false);
    Color color = (planSequence.isPartialPlanInDb(stepNum) ? ColorMap.getColor("green3") :
                   (planSequence.isPartialPlanInFilesystem(stepNum) ? ColorMap.getColor("yellow") :
                    ColorMap.getColor("red")));
    statusIndicator.setPen( JGoPen.Null);
    statusIndicator.setBrush( JGoBrush.makeStockBrush( color));
    doc.addObjectAtTail( statusIndicator);
    statusIndicatorList.add( statusIndicator);
  }


  /**
   * <code>SequenceStepsJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  public class SequenceStepsJGoView extends JGoView {

    /**
     * <code>SequenceStepsJGoView</code> - constructor 
     *
     */
    public SequenceStepsJGoView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public final void doBackgroundClick( final int modifiers, final Point docCoords,
                                         final Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {

        mouseRightPopupMenu( viewCoords);

      }
    } // end doBackgroundClick

  } // end class SequenceStepsJGoView

  private void mouseRightPopupMenu( final Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem refreshItem = new JMenuItem("Refresh");
    createRefreshItem( refreshItem, this);
    mouseRightPopup.add( refreshItem);

    createZoomItem( jGoView, zoomFactor, mouseRightPopup, this);

    createCloseHideShowViewItems( mouseRightPopup);

    mouseRightPopup.addSeparator();

    List allPartialPlansViewList = new ArrayList();
    List partialPlansOfViews = getPartialPlansOfViews();
    Iterator partialPlanItr = partialPlansOfViews.iterator();
    while (partialPlanItr.hasNext()) {
      PwPartialPlan partialPlan = (PwPartialPlan) partialPlanItr.next();
      List partialPlanViewList = null;
      if ((partialPlanViewList = getPartialPlanViewList( partialPlan)).size() != 0) {
        allPartialPlansViewList.addAll( partialPlanViewList);
        String stepTitle = partialPlan.getPartialPlanName() + " Active Views";
        String stepBackwardTitle = "Step Backward " + stepTitle;
        JMenuItem stepBackwardItem = new JMenuItem( stepBackwardTitle);
        createStepBackwardItem( stepBackwardItem, partialPlanViewList);
        mouseRightPopup.add( stepBackwardItem);

        String stepForwardTitle = "Step Forward " + stepTitle;
        JMenuItem stepForwardItem = new JMenuItem( stepForwardTitle);
        createStepForwardItem( stepForwardItem, partialPlanViewList);
        mouseRightPopup.add( stepForwardItem);
      }
    }
    if (partialPlansOfViews.size() > 1) {
      JMenuItem stepBackwardAllItem = new JMenuItem( "Step Backward All Active Views");
      createStepBackwardItem( stepBackwardAllItem, allPartialPlansViewList);
      mouseRightPopup.add( stepBackwardAllItem);
      JMenuItem stepForwardAllItem = new JMenuItem( "Step Forward All Active Views");
      createStepForwardItem( stepForwardAllItem, allPartialPlansViewList);
      mouseRightPopup.add( stepForwardAllItem);
    }

    ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private List getPartialPlansOfViews() {
    List uniquePartialPlanList = new UniqueSet();
    Object[] viewSetKeys =  PlanWorks.getPlanWorks().getViewManager().getViewSetKeys();
    for (int i = 0, n = viewSetKeys.length; i < n; i++) {
      ViewableObject viewableObject = (ViewableObject) viewSetKeys[i];
      if (viewableObject instanceof PwPartialPlan) {
        uniquePartialPlanList.add( (PwPartialPlan) viewableObject);
      }
    }
    // sort by stepNumber
    List partialPlanList = new ArrayList( uniquePartialPlanList);
    Collections.sort( partialPlanList, new StepNumberComparator());
//    for (int i = 0, n = partialPlanList.size(); i < n; i++) {
//       System.err.println( "getPartialPlansOfViews i " + i + " " +
//                           ((PwPartialPlan) partialPlanList.get( i)).getStepNumber());
//     }
    return partialPlanList;
  } // end getSequenceStepsOfViews


  private class StepNumberComparator implements Comparator {
    public StepNumberComparator() {
    }
    public final int compare( final Object o1, final Object o2) {
      Integer i1 = new Integer( ((PwPartialPlan) o1).getStepNumber());
      Integer i2 = new Integer( ((PwPartialPlan) o2).getStepNumber());
      return i1.compareTo( i2);
    }
    public final boolean equals( final Object o1, final Object o2) {
      Integer i1 =  new Integer( ((PwPartialPlan) o1).getStepNumber());
      Integer i2 = new Integer( ((PwPartialPlan) o2).getStepNumber());
      return i1.equals( i2);
    }
  } // end class StepNumberComparator


  private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                         final SequenceStepsView sequenceStepsView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public final void actionPerformed( final ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( ViewConstants.SEQUENCE_STEPS_VIEW, planSequence,
                                            sequenceStepsView, viewSet, jGoView, viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem


  private void createRefreshItem( JMenuItem refreshItem, 
                                  final SequenceStepsView sequenceStepsView) {
    refreshItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          refreshView();
        }
      });
  }

  /**
   * <code>refreshView</code>
   *
   */
  public void refreshView() {
    planSequence.refresh();
    heightScaleFactor = computeHeightScaleFactor();
    redraw();
  } // end refreshView



} // end class SequenceStepsView
