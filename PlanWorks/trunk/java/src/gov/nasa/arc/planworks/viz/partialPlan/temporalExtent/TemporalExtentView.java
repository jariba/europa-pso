// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TemporalExtentView.java,v 1.43 2004-04-22 19:26:25 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 21July03
//

package gov.nasa.arc.planworks.viz.partialPlan.temporalExtent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoView;


import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.partialPlan.TimeScaleView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>TemporalExtentView</code> - render the temporal extents of a
 *                partial plan's tokens
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TemporalExtentView extends PartialPlanView  {

  protected static final int SHOW_INTERVALS = 0;
  protected static final int SHOW_EARLIEST = 1;
  protected static final int SHOW_LATEST = 2;
  private static final String SHOW_INTERVALS_LABEL = "Show Intervals";
  private static final String SHOW_EARLIEST_LABEL = "Show Earliest";
  private static final String SHOW_LATEST_LABEL = "Show Latest";

  private long startTimeMSecs;
  private ViewSet viewSet;
  private MDIInternalFrame viewFrame;
  private ExtentView jGoExtentView;
  private TimeScaleView jGoRulerView;
  private RulerPanel rulerPanel;
  // temporalNodeList & tmpTemporalNodeList used by JFCUnit test case
  private List temporalNodeList; // element TemporalNode
  private List tmpTemporalNodeList; // element TemporalNode
  private int startXLoc;
  private int startYLoc;
  private int maxCellRow;
  private JGoStroke timeScaleMark;
  private static Point docCoords;
  private boolean isShowLabels;
  private int temporalDisplayMode;
  private boolean isStepButtonView;
  private ViewListener viewListener;


  /**
   * <code>TemporalExtentView</code> - constructor 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TemporalExtentView( ViewableObject partialPlan, ViewSet viewSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    temporalExtentViewInit(viewSet);
    isStepButtonView = false;

    SwingUtilities.invokeLater( runInit);
  } // end constructor

  /**
   * <code>TemporalExtentView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param s - <code>PartialPlanViewState</code> - 
   */
  public TemporalExtentView(ViewableObject partialPlan, ViewSet viewSet, 
                            PartialPlanViewState s) {
    super((PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    temporalExtentViewInit(viewSet);
    isStepButtonView = true;
    setState(s);
    SwingUtilities.invokeLater(runInit);
  }

  public TemporalExtentView( ViewableObject partialPlan, ViewSet viewSet,
                             ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    temporalExtentViewInit(viewSet);
    isStepButtonView = false;
    if (viewListener != null) {
      addViewListener( viewListener);
    }

    SwingUtilities.invokeLater( runInit);
  } // end constructor

  private void temporalExtentViewInit(ViewSet viewSet) {
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.viewListener = null;

    startXLoc = ViewConstants.TIMELINE_VIEW_X_INIT * 2;
    startYLoc = ViewConstants.TIMELINE_VIEW_Y_INIT;
    maxCellRow = 0;
    timeScaleMark = null;
    isShowLabels = true;
    temporalDisplayMode = SHOW_INTERVALS;
    viewFrame = viewSet.openView( this.getClass().getName(), viewListener);
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());
   
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    
    jGoExtentView = new ExtentView();
    jGoExtentView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoExtentView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    
    add( jGoExtentView, BorderLayout.NORTH);
    jGoExtentView.validate();
    jGoExtentView.setVisible( true);
    
    rulerPanel = new RulerPanel();
    rulerPanel.setLayout( new BoxLayout( rulerPanel, BoxLayout.Y_AXIS));

    int startY = 2;
    jGoRulerView = new TimeScaleView( startXLoc, startY, partialPlan, this);
    jGoRulerView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoRulerView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    jGoRulerView.validate();
    jGoRulerView.setVisible( true);
    
    rulerPanel.add( jGoRulerView, BorderLayout.NORTH);
    add( rulerPanel, BorderLayout.SOUTH);
    
    this.setVisible( true);
  } // end temporalExtentViewInit

  public PartialPlanViewState getState() {
    return new TemporalExtentViewState(this);
  }

  public boolean showLabels(){return isShowLabels;}
  public int displayMode(){return temporalDisplayMode;}

  public void setState(PartialPlanViewState s) {
    super.setState(s);
    if(s == null) {
      return;
    }
    zoomFactor = s.getCurrentZoomFactor();
    boolean isSetState = true;
    zoomView( jGoExtentView, isSetState, this);

    TemporalExtentViewState state = (TemporalExtentViewState)s;
    isShowLabels = state.showingLabels();
    temporalDisplayMode = state.displayMode();
  } // end setState

  Runnable runInit = new Runnable() {
      public void run() {
        init();
      }
    };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoExtentView exists and is part of a visible window".
   *     int extentScrollExtent = jGoExtentView.getHorizontalScrollBar().getSize().getWidth();
   *    called by componentShown method on the JFrame
   *    JGoExtentView.setVisible( true) must be completed -- use runInit in constructor
   */
  public void init() {
    handleEvent(ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for TemporalExtentView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "timelineView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);

    boolean doFreeTokens = true;
    jGoRulerView.collectAndComputeTimeScaleMetrics( doFreeTokens, this);
    // in case zoomFactor != 1
    if (zoomFactor > 1) {
      jGoRulerView.computeTimeScaleMetrics( zoomFactor, this);
    }
    jGoRulerView.createTimeScale();

    boolean isRedraw = false;
    renderTemporalExtent( isRedraw);

    if (! isStepButtonView) {
      expandViewFrame( viewFrame,
                       (int) Math.max( jGoExtentView.getDocumentSize().getWidth(),
                                       jGoRulerView.getDocumentSize().getWidth()),
                       (int) (jGoExtentView.getDocumentSize().getHeight() +
                              jGoRulerView.getDocumentSize().getHeight()));
    }
    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

    addStepButtons( jGoExtentView);
    if (! isStepButtonView) {
      expandViewFrameForStepButtons( viewFrame, jGoExtentView);
    }

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    handleEvent(ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init

  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *                       setVisible(true | false)
   *                       according to the Content Spec enabled ids
   *
   */
  public void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  /**
   * <code>RedrawViewThread</code> - execute redraw in a new thread
   *
   */
  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      handleEvent(ViewListener.EVT_REDRAW_BEGUN_DRAWING);
      try {
        ViewGenerics.setRedrawCursor( viewFrame);
        // redraw jGoRulerView, in case zoomFactor changed
        jGoRulerView.computeTimeScaleMetrics( TemporalExtentView.this.getZoomFactor(),
                                              TemporalExtentView.this);
        jGoRulerView.createTimeScale();

        boolean isRedraw = true;
        renderTemporalExtent( isRedraw);
        addStepButtons( jGoExtentView);
        // causes bottom view edge to creep off screen
        //       if (! isStepButtonView) {
        //         expandViewFrameForStepButtons( viewFrame, jGoExtentView);
        //       }
      } finally {
        ViewGenerics.resetRedrawCursor( viewFrame);
      }
    handleEvent(ViewListener.EVT_REDRAW_ENDED_DRAWING);
    } //end run

  } // end class RedrawViewThread

  private void renderTemporalExtent( boolean isRedraw) {
    jGoExtentView.getDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    temporalNodeList = null;
    //tmpTemporalNodeList = new ArrayList();
    if(tmpTemporalNodeList != null) {
      tmpTemporalNodeList.clear();
    }
    tmpTemporalNodeList = new UniqueSet();

    createTemporalNodes();
    boolean showDialog = true;
    isContentSpecRendered( PlanWorks.TEMPORAL_EXTENT_VIEW, showDialog);

    layoutTemporalNodes();
    // equalize view widths so scrollbars are equal
    equalizeViewWidths( isRedraw);

  } // end createTemporalExtentView

  /**
   * <code>getJGoView</code>
   *
   * @return - <code>JGoView</code> - 
   */
  public JGoView getJGoView()  {
    return this.jGoExtentView;
  }

  /**
   * <code>getJGoDocument</code> - 
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoExtentView.getDocument();
  }

  /**
   * <code>getTemporalNodeList</code>
   *
   * @return - <code>List</code> - of TemporalNode
   */
  public List getTemporalNodeList() {
    return temporalNodeList;
  }

  /**
   * <code>getTimeScale</code>
   *
   * @return - <code>double</code> - 
   */
  public double getTimeScale() {
    return jGoRulerView.getTimeScaleNoZoom();
  }

  /**
   * <code>getTimeScaleStart</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTimeScaleStart() {
    return jGoRulerView.getTimeScaleStart();
  }

  /**
   * <code>getTimeScaleEnd</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTimeScaleEnd() {
    return jGoRulerView.getTimeScaleEnd();
  }

  /**
   * <code>getStartYLoc</code>
   *
   * @return - <code>int</code> - 
   */
  public int getStartYLoc() {
    return startYLoc;
  }

  /**
   * <code>getJGoRulerView</code>
   *
   * @return - <code>TimeScaleView</code> - 
   */
  public TimeScaleView getJGoRulerView() {
    return jGoRulerView;
  }

  private void createTemporalNodes() {
    List tokenList = partialPlan.getTokenList();
    Iterator tokenIterator = tokenList.iterator();
    Color backgroundColor = null;
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
//       if (token.isSlotted() && (! token.isBaseToken())) {
//         // non-slotted, non-base tokens - not displayed, put in displayedTokenIds
//         isTokenInContentSpec( token);
//         continue;
//       }
      boolean isFreeToken = false;
      PwSlot slot = null;
      if (! token.isFree()) { // slotted base tokens, resourceTransactions, other tokens
        if (token.getSlotId() != null) {
          slot = partialPlan.getSlot( token.getSlotId());
        }
        backgroundColor = getTimelineColor( token.getParentId());
      } else { // free tokens
        isFreeToken = true;
        backgroundColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
      }
      if (isTokenInContentSpec( token)) {
        PwDomain startTimeIntervalDomain = token.getStartVariable().getDomain();
        PwDomain endTimeIntervalDomain = token.getEndVariable().getDomain();
        if ((startTimeIntervalDomain != null) && (endTimeIntervalDomain != null)) {
          String earliestDurationString =
            NodeGenerics.getShortestDuration( isFreeToken, token, startTimeIntervalDomain,
                                              endTimeIntervalDomain);
          String latestDurationString =
            NodeGenerics.getLongestDuration( isFreeToken, token, startTimeIntervalDomain,
                                             endTimeIntervalDomain);
          // System.err.println( "token id " + token.getId());
          TemporalNode temporalNode = 
            new TemporalNode( token, slot, startTimeIntervalDomain, endTimeIntervalDomain,
                              earliestDurationString, latestDurationString,
                              backgroundColor, isFreeToken, isShowLabels,
                              temporalDisplayMode, this); 
          tmpTemporalNodeList.add( temporalNode);
          jGoExtentView.getDocument().addObjectAtTail( temporalNode);
        }
      }
    }
    temporalNodeList = tmpTemporalNodeList;
  } // end createTemporalNodes

  private void layoutTemporalNodes() {
    /*List extents = new ArrayList();
    Iterator temporalNodeIterator = temporalNodeList.iterator();
    while (temporalNodeIterator.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeIterator.next();
      extents.add( temporalNode);
      }*/
    List extents = new ArrayList(temporalNodeList);
    // do the layout -- compute cellRow for each node
    List results =
      Algorithms.allocateRows
      ( jGoRulerView.scaleTimeNoZoom( (double) jGoRulerView.getTimeScaleStart()),
        jGoRulerView.scaleTimeNoZoom( (double) jGoRulerView.getTimeScaleEnd()), extents);
//     List results =
//       Algorithms.betterAllocateRows
//       ( jGoRulerView.scaleTimeNoZoom( (double) jGoRulerView.getTimeScaleStart()),
//         jGoRulerView.scaleTimeNoZoom( (double) jGoRulerView.getTimeScaleEnd()), extents);
    if (temporalNodeList.size() != results.size()) {
      String message = String.valueOf( temporalNodeList.size() - results.size()) +
        " nodes not successfully allocated";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Temporal Extent View Layout Exception",
                                     JOptionPane.ERROR_MESSAGE);
      return;
    }
    for (Iterator it = extents.iterator(); it.hasNext();) {
      TemporalNode temporalNode = (TemporalNode) it.next();
      // System.err.println( temporalNode.getPredicateName() + " cellRow " +
      //                     temporalNode.getRow());
      if (temporalNode.getRow() > maxCellRow) {
        maxCellRow = temporalNode.getRow();
      }
      // render the node
      temporalNode.configure();
    }
  } // end layoutTemporalNodes


  private void iterateOverNodes() {
    int numTemporalNodes = temporalNodeList.size();
    //System.err.println( "iterateOverNodes: numTemporalNodes " + numTemporalNodes);
    Iterator temporalIterator = temporalNodeList.iterator();
    while (temporalIterator.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalIterator.next();
      System.err.println( "name '" + temporalNode.getPredicateName() + "' location " +
                          temporalNode.getLocation());
    }
  } // end iterateOverNodes


  private void iterateOverJGoDocument() {
    JGoListPosition position = jGoExtentView.getDocument().getFirstObjectPos();
    int cnt = 0;
    while (position != null) {
      JGoObject object = jGoExtentView.getDocument().getObjectAtPos( position);
      position = jGoExtentView.getDocument().getNextObjectPosAtTop( position);
      //System.err.println( "iterateOverJGoDoc: position " + position +
      //                   " className " + object.getClass().getName());
      if (object instanceof TemporalNode) {
        TemporalNode temporalNode = (TemporalNode) object;

      }
      cnt += 1;
//       if (cnt > 100) {
//         break;
//       }
    }
    //System.err.println( "iterateOverJGoDoc: cnt " + cnt);
  } // end iterateOverJGoDocument


  // write a line at the max horizontal extent in each view, and
  // at max vertical extent in jGoExtentView
  private void equalizeViewWidths( boolean isRedraw) {
    Dimension extentViewDocument = jGoExtentView.getDocumentSize();
    Dimension rulerViewDocument = jGoRulerView.getDocumentSize();
//     System.err.println( "extentViewDocumentWidth B" + extentViewDocument.getWidth() +
//                         " rulerViewDocumentWidth B" + rulerViewDocument.getWidth());
    int xRulerMargin = ViewConstants.TIMELINE_VIEW_X_INIT;
    int jGoDocBorderWidth = ViewConstants.JGO_DOC_BORDER_WIDTH;
    if (isRedraw) {
      xRulerMargin = 0;
    }
    int maxWidth = Math.max( (int) extentViewDocument.getWidth() - jGoDocBorderWidth,
                             (int) rulerViewDocument.getWidth() + xRulerMargin -
                             jGoDocBorderWidth);
//     System.err.println( "maxWidth " + maxWidth);
    JGoStroke maxViewWidthPoint = new JGoStroke();
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
    // make mark invisible
    maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1, 
                                          ViewConstants.VIEW_BACKGROUND_COLOR));
    jGoExtentView.getDocument().addObjectAtTail( maxViewWidthPoint);
    // always put mark at max y location, so on redraw jGoRulerView does not expand
    JGoStroke maxViewHeightPoint = new JGoStroke();
    int maxYLoc = startYLoc + ((maxCellRow + 1) *
                               ViewConstants.TEMPORAL_NODE_CELL_HEIGHT) + 2;
    maxViewHeightPoint.addPoint( maxWidth, maxYLoc);
    maxViewHeightPoint.addPoint( maxWidth - ViewConstants.TIMELINE_VIEW_X_INIT,
                                 maxYLoc);
    // make mark invisible
    maxViewHeightPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                           ViewConstants.VIEW_BACKGROUND_COLOR));
    jGoExtentView.getDocument().addObjectAtTail( maxViewHeightPoint);

    if (! isRedraw) {
      maxViewWidthPoint = new JGoStroke();
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
      // make mark invisible
      maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                            ViewConstants.VIEW_BACKGROUND_COLOR));
      jGoRulerView.getDocument().addObjectAtTail( maxViewWidthPoint);
    }
//     extentViewDocument = jGoExtentView.getDocumentSize();
//     rulerViewDocument = jGoRulerView.getDocumentSize();
//     System.err.println( "extentViewDocumentWidth A" + extentViewDocument.getWidth() +
//                         " rulerViewDocumentWidth A" + rulerViewDocument.getWidth());
  } // end equalizeViewWidths



  /**
   * <code>RulerPanel</code> - require ruler view panel to be of fixed height
   *
   */
  class RulerPanel extends JPanel {

    /**
     * <code>RulerPanel</code> - constructor 
     *
     */
    public RulerPanel() {
      super();
    }

    /**
     * <code>getMinimumSize</code>
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMinimumSize() {
      return new Dimension( (int) TemporalExtentView.this.getSize().getWidth(),
                            (int) jGoRulerView.getDocumentSize().getHeight() +
                            (int) jGoRulerView.getHorizontalScrollBar().getSize().getHeight());
    }

    /**
     * <code>getMaximumSize</code>
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMaximumSize() {
      return new Dimension( (int) TemporalExtentView.this.getSize().getWidth(),
                            (int) jGoRulerView.getDocumentSize().getHeight() +
                            (int) jGoRulerView.getHorizontalScrollBar().getSize().getHeight());
    }

  } // end class RulerPanel


  /**
   * <code>ScrollBarListener</code> - keep both jGoExtentView & jGoRulerView aligned,
   *                                  when user moves one scroll bar
   *
   */
  class ScrollBarListener implements AdjustmentListener {

    /**
     * <code>adjustmentValueChanged</code> - keep both jGoExtentView & jGoRulerView
     *                                aligned, even when user moves one scroll bar
     *
     * @param event - <code>AdjustmentEvent</code> - 
     */
    public void adjustmentValueChanged( AdjustmentEvent event) {
      JScrollBar source = (JScrollBar) event.getSource();
      // to get immediate incremental adjustment, rather than waiting for
      // final position, comment out next check
      // if (! source.getValueIsAdjusting()) {
//         System.err.println( "adjustmentValueChanged " + source.getValue());
//         System.err.println( "\njGoExtentView " +
//                             jGoExtentView.getHorizontalScrollBar().getValue());
//         System.err.println( "jGoRulerView " +
//                             jGoRulerView.getHorizontalScrollBar().getValue());
        int newPostion = source.getValue();
//         System.err.println( "newPostion " + newPostion);
        int timeScaleViewPosition = 0, extentViewPosition = 0;
        if (source.getParent() instanceof TimeScaleView) {
          timeScaleViewPosition = newPostion;
          extentViewPosition = (int) ((double) zoomFactor * newPostion);
        } else {
          timeScaleViewPosition = (int) (newPostion / (double) zoomFactor);
          extentViewPosition = newPostion;
        }
        if (extentViewPosition != jGoExtentView.getHorizontalScrollBar().getValue()) {
          jGoExtentView.getHorizontalScrollBar().setValue( extentViewPosition);
        } else if (timeScaleViewPosition != jGoRulerView.getHorizontalScrollBar().getValue()) {
          jGoRulerView.getHorizontalScrollBar().setValue( timeScaleViewPosition);
        }
        // }
    } // end adjustmentValueChanged 

  } // end class ScrollBarListener 


  /**
   * <code>ExtentView</code> - subclass doBackgroundClick to handle drawing
   *                               vertical time marks on view
   *
   */
  public class ExtentView extends JGoView {

    /**
     * <code>TemporalExtent</code> - constructor 
     *
     */
    public ExtentView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *                             1) draws vertical line in extent view to
     *                                focus that time point across all temporal nodes
     *                             2) snap to active token
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        TemporalExtentView.docCoords = docCoords;
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick


  } // end class ExtentView


  private void mouseRightPopupMenu( Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem showLabelsItem = null;
    if (isShowLabels) {
      showLabelsItem = new JMenuItem( "Hide Node Labels");
    } else {
      showLabelsItem = new JMenuItem( "Show Node Labels");
    }
    createShowLabelsItem( showLabelsItem);
    mouseRightPopup.add( showLabelsItem);

    createTemporalDisplayItems( mouseRightPopup);

    mouseRightPopup.addSeparator();

    JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
    createNodeByKeyItem( nodeByKeyItem);
    mouseRightPopup.add( nodeByKeyItem);

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         PlanWorks.TEMPORAL_EXTENT_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
    createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);
    
    JMenuItem timeMarkItem = new JMenuItem( "Set Time Scale Line");
    createTimeMarkItem( timeMarkItem);
    mouseRightPopup.add( timeMarkItem);

    if (((PartialPlanViewSet) this.getViewSet()).getActiveToken() != null) {
      JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
      createActiveTokenItem( activeTokenItem);
      mouseRightPopup.add( activeTokenItem);
    }

    this.createZoomItem( jGoExtentView, zoomFactor, mouseRightPopup, this);

    if (doesViewFrameExist( PlanWorks.NAVIGATOR_VIEW)) {
      mouseRightPopup.addSeparator();
      JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
      createCloseNavigatorWindowsItem( closeWindowsItem);
      mouseRightPopup.add( closeWindowsItem);
    }
    createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu


  private void createTemporalDisplayItems( JPopupMenu mouseRightPopup) {
    JMenuItem showIntervalsItem = null;
    JMenuItem showEarliestItem = null;
    JMenuItem showLatestItem = null;
    if (temporalDisplayMode == SHOW_INTERVALS) {
      showEarliestItem = new JMenuItem( SHOW_EARLIEST_LABEL);
      createShowEarliestItem( showEarliestItem);
      mouseRightPopup.add( showEarliestItem);
      showLatestItem = new JMenuItem( SHOW_LATEST_LABEL);
      createShowLatestItem( showLatestItem);
      mouseRightPopup.add( showLatestItem);
    } else if (temporalDisplayMode == SHOW_EARLIEST) {
      showIntervalsItem = new JMenuItem( SHOW_INTERVALS_LABEL);
      createShowIntervalsItem( showIntervalsItem);
      mouseRightPopup.add( showIntervalsItem);
      showLatestItem = new JMenuItem( SHOW_LATEST_LABEL);
      createShowLatestItem( showLatestItem);
      mouseRightPopup.add( showLatestItem);
    } else if (temporalDisplayMode == SHOW_LATEST) {
      showEarliestItem = new JMenuItem( SHOW_EARLIEST_LABEL);
      createShowEarliestItem( showEarliestItem);
      mouseRightPopup.add( showEarliestItem);
      showIntervalsItem = new JMenuItem( SHOW_INTERVALS_LABEL);
      createShowIntervalsItem( showIntervalsItem);
      mouseRightPopup.add( showIntervalsItem);
    }
  } // end createTemporalDisplayItems
  

  private void createShowIntervalsItem( JMenuItem showIntervalsItem) {
    showIntervalsItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          temporalDisplayMode = SHOW_INTERVALS;
          TemporalExtentView.this.redraw();
        }
      });
  } // end createShowIntervalsItem


  private void createShowEarliestItem( JMenuItem showEarliestItem) {
    showEarliestItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          temporalDisplayMode = SHOW_EARLIEST;
          TemporalExtentView.this.redraw();
        }
      });
  } // end createShowEarliestItem


  private void createShowLatestItem( JMenuItem showLatestItem) {
    showLatestItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          temporalDisplayMode = SHOW_LATEST;
          TemporalExtentView.this.redraw();
        }
      });
  } // end createShowLatestItem


  private void createShowLabelsItem( JMenuItem showLabelsItem) {
    showLabelsItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          if (isShowLabels) {
            isShowLabels = false;
          } else {
            isShowLabels = true;
          }
          TemporalExtentView.this.redraw();
        }
      });
  } // end createShowLabelsItem


  private void createActiveTokenItem( JMenuItem activeTokenItem) {
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) TemporalExtentView.this.getViewSet()).getActiveToken();
          if (activeToken != null) {
            boolean isByKey = false;
            PwSlot slot = null;
            findAndSelectToken( activeToken, slot, isByKey);
          }
        }
      });
  } // end createActiveTokenItem


  private void createNodeByKeyItem( JMenuItem nodeByKeyItem) {
    nodeByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskNodeByKey nodeByKeyDialog =
            new AskNodeByKey( "Find by Key", "key (int)", TemporalExtentView.this);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());
            PwToken tokenToFind = partialPlan.getToken( nodeKey);
            boolean isByKey = true;
            PwSlot slot = null;
            findAndSelectToken( tokenToFind, slot, isByKey);
          }
        }
      });
  } // end createNodeByKeyItem


  /**
   * <code>findAndSelectToken</code> - handles empty slots and free tokens, as well
   *                                   as slotted tokens
   *
   * @param tokenToFind - <code>PwToken</code> - null for empty slots
   * @param slotToFind - <code>PwSlot</code> - null for free tokens
   * @param isByKey - <code>boolean</code> - 
   */
  public void findAndSelectToken( PwToken tokenToFind, PwSlot slotToFind, boolean isByKey) {
    boolean isTokenFound = false;
    boolean isHighlightNode = true;
    Iterator temporalNodeListItr = temporalNodeList.iterator();
    TemporalNode temporalNode = null;
    // System.err.println( "findAndSelectToken: enter");
    foundMatch:
    while (temporalNodeListItr.hasNext()) {
      temporalNode = (TemporalNode) temporalNodeListItr.next();
      if (temporalNode.getToken() != null) {


        if ((tokenToFind != null) &&
            temporalNode.getToken().getId().equals( tokenToFind.getId())) {
          isTokenFound = true;
          break;          
        }
//         if (temporalNode.getSlot() != null) {
//           // check overloaded tokens, since only base tokens are rendered
//           Iterator tokenListItr = temporalNode.getSlot().getTokenList().iterator();
//           while (tokenListItr.hasNext()) {
//             PwToken token = (PwToken) tokenListItr.next();
//             if ((tokenToFind != null) && token.getId().equals( tokenToFind.getId())) {
//               isTokenFound = true;
//               break foundMatch;
//             }
//           }
//         } else if ((tokenToFind != null) &&
//                    temporalNode.getToken().getId().equals( tokenToFind.getId())) {
//           // free token
//           isTokenFound = true;
//           break;          
//         }
      } else if ((slotToFind != null) &&
                 temporalNode.getSlot().getId().equals( slotToFind.getId()))  {
        // empty slot
        isTokenFound = true;
        break;          
      }
    }
    if (isTokenFound) {
      if (tokenToFind != null) {
        System.err.println( "TemporalExtentView found token: " +
                            tokenToFind.getPredicateName() +
                            " (key=" + tokenToFind.getId().toString() + ")");
      }
      NodeGenerics.focusViewOnNode( temporalNode, isHighlightNode, jGoExtentView);
      if (! isByKey) {
        NodeGenerics.selectSecondaryNodes
          ( NodeGenerics.mapTokensToTokenNodes
            (((PartialPlanViewSet) TemporalExtentView.this.getViewSet()).
             getSecondaryTokens(), temporalNodeList), jGoExtentView);
      }
    } else {
      // Content Spec filtering may cause this to happen
      String message = "Token " + tokenToFind.getPredicateName() +
        " (key=" + tokenToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Token Not Found in TemporalExtentView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectToken


  private void createTimeMarkItem( JMenuItem timeMarkItem) {
    timeMarkItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          int xLoc = (int) TemporalExtentView.docCoords.getX();
          // System.err.println( "doMouseClick: xLoc " + xLoc + " time " + scaleXLoc( xLoc));
          if (timeScaleMark != null) {
            jGoExtentView.getDocument().removeObject( timeScaleMark);
            // jGoExtentView.validate();
          }
          timeScaleMark = new TimeScaleMark( xLoc, getZoomFactor());
          timeScaleMark.addPoint( xLoc, 0);
          timeScaleMark.addPoint( xLoc, ((maxCellRow + 1) *
                                         ViewConstants.TEMPORAL_NODE_CELL_HEIGHT *
                                         getZoomFactor()));
          jGoExtentView.getDocument().addObjectAtTail( timeScaleMark);
        }
      });
  } // end createTimeMarkItem


  private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                         final TemporalExtentView temporalExtentView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public void actionPerformed( ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( PlanWorks.TEMPORAL_EXTENT_VIEW, partialPlan,
                                            temporalExtentView, viewSet, jGoExtentView,
                                            viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem

  /**
   * <code>TimeScaleMark</code> - color the mark and provide its time value
   *                              as a tool tip.
   *
   */
  class TimeScaleMark extends JGoStroke {

    private int xLoc;

    /**
     * <code>TimeScaleMark</code> - constructor 
     *
     * @param xLoc - <code>int</code> - 
     * @param penWidth - <code>int</code> - 
     */
    public TimeScaleMark( int xLoc, int penWidth) {
      super();
      this.xLoc = xLoc;
      setDraggable( false);
      setResizable( false);
      setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "red")));
    }

    public String getToolTipText() {
      return String.valueOf( jGoRulerView.scaleXLocNoZoom( xLoc));
    }

  } // end class TimeScaleMark


    

} // end class TemporalExtentView
 
