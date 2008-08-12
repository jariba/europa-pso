// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineView.java,v 1.72 2005-06-01 17:16:06 pdaley Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz.partialPlan.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoListPosition;
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
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.partialPlan.TimeScaleView;
import gov.nasa.arc.planworks.viz.util.AskNodeByKey;
import gov.nasa.arc.planworks.viz.util.ProgressMonitorThread;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>TimelineView</code> - render a partial plan's timelines and slots
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineView extends PartialPlanView {

  private static Object staticObject = new Object();

  protected static final int SHOW_INTERVALS = 0;
  protected static final int SHOW_EARLIEST = 1;
  protected static final int SHOW_LATEST = 2;
                                                                           
  private static final String SHOW_INTERVALS_LABEL = "Show Intervals";
  private static final String SHOW_EARLIEST_LABEL = "Show Earliest";
  private static final String SHOW_LATEST_LABEL = "Show Latest";
                                                                           
  private static final double LABEL_MIN_LEN_FIRST_SLOT_FACTOR = 1.25;

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private TimelineJGoView jGoView;
  private TimeScaleView jGoRulerView;
  private RulerPanel rulerPanel;
  private int horizonStart;
  private int horizonEnd;
  private int extendedHorizonEnd;
  private int startXLoc;
  private int startYLoc;
  private JGoStroke timeScaleMark;
  private static Point docCoords;
  private JGoDocument jGoDocument;
  private List timelineNodeList; // element TimelineNode
  private List freeTokenNodeList; // element TokenNode
  private int slotLabelMinLength;
  private int slotNodeMaxWidth;
  private int timelineNodeMaxWidth;
  private int slotNodeScaleFactor;
  private JGoArea mouseOverNode;
  private boolean isAutoSnapEnabled;
  private boolean isStepButtonView;
  private Integer focusNodeId;
  private ProgressMonitorThread progressMonThread;
  private int maxCellRow;
  private int timelineDisplayMode;

  /**
   * <code>TimelineView</code> - constructor - 
   *                             Use SwingWorker to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TimelineView( ViewableObject partialPlan,  ViewSet viewSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    timelineViewInit( (PwPartialPlan) partialPlan, viewSet);
    isStepButtonView = false;
    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor

  /**
   * <code>TimelineView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param s - <code>PartialPlanViewState</code> - 
   */
  public TimelineView(ViewableObject partialPlan, ViewSet viewSet,
                      PartialPlanViewState s) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    timelineViewInit( (PwPartialPlan) partialPlan, viewSet);
    isStepButtonView = true;
    setState(s);
    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  }

  /**
   * <code>TimelineView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public TimelineView( ViewableObject partialPlan, ViewSet viewSet,
                       ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    timelineViewInit( (PwPartialPlan) partialPlan, viewSet);
    isStepButtonView = false;
    if (viewListener != null) {
      addViewListener( viewListener);
    }
    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  }

  /**
   * <code>TimelineView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param s - <code>PartialPlanViewState</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public TimelineView(ViewableObject partialPlan, ViewSet viewSet,
                      PartialPlanViewState s, ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    timelineViewInit( (PwPartialPlan) partialPlan, viewSet);
    isStepButtonView = true;
    if (viewListener != null) {
      addViewListener( viewListener);
    }
    setState(s);
    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  }

  private void timelineViewInit(ViewableObject partialPlan, ViewSet viewSet) {
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;

    startXLoc = ViewConstants.TIMELINE_VIEW_X_INIT * 2;
    startYLoc = ViewConstants.TIMELINE_VIEW_Y_INIT;
    horizonStart = 0;
    horizonEnd = 0;
    extendedHorizonEnd = 0;
    ViewListener viewListener = null;
    viewFrame = viewSet.openView( this.getClass().getName(), viewListener);
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());
    viewName = ViewConstants.TIMELINE_VIEW;
    focusNodeId = null;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
    mouseOverNode = null;
    isAutoSnapEnabled = false;

    jGoView = new TimelineJGoView();
    jGoView.addViewListener( createViewListener());
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);

    jGoView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    rulerPanel = new RulerPanel();
    rulerPanel.setLayout( new BoxLayout( rulerPanel, BoxLayout.Y_AXIS));
                                                                                  
    int startY = 2;
    jGoRulerView = new TimeScaleView( startXLoc, startY, (PwPartialPlan) partialPlan, this);
    jGoRulerView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoRulerView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    jGoRulerView.validate();
    jGoRulerView.setVisible( false );
                                                                                
    rulerPanel.add( jGoRulerView, BorderLayout.NORTH);
    add( rulerPanel, BorderLayout.SOUTH);

    this.setVisible( true);
  }

  public void setState( PartialPlanViewState s) {
    super.setState( s);
    if(s == null) {
      return;
    }
    zoomFactor = s.getCurrentZoomFactor();
    boolean isSetState = true;
    zoomView( jGoView, isSetState, this);
  } // end setState

//   Runnable runInit = new Runnable() {
//       public void run() {
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
    // wait for TimelineView instance to become displayable
    if (! ViewGenerics.displayableWait( TimelineView.this)) {
      closeView( this);
      return;
    }

    this.computeFontMetrics( this);

                                                                          
    jGoDocument = jGoView.getDocument();
    jGoDocument.addDocumentListener( createDocListener());

    // create all nodes
    boolean isRedraw = false;
    boolean isValid = renderTimelineAndSlotNodes( isRedraw);
    boolean doFreeTokens = false;
    boolean isTimelineView = true;
    if (isValid) {
      jGoRulerView.collectAndComputeTimeScaleMetrics( isTimelineView, doFreeTokens, this);
      //get initial horizon start and end 
      horizonStart = jGoRulerView.getTimeScaleStart();
      horizonEnd = jGoRulerView.getTimeScaleEnd();
      extendedHorizonEnd = horizonEnd + slotNodeScaleFactor * 
                                ViewConstants.TIMELINE_EXTEND_HORIZON_UNITS;
      jGoRulerView.setTimeScaleEnd( extendedHorizonEnd );
    
      // in case zoomFactor != 1
      if (zoomFactor > 1) {
        jGoRulerView.computeTimeScaleMetrics( isTimelineView, zoomFactor, this);  
      }
      jGoRulerView.createTimeScale( isTimelineView);
      if (timelineDisplayMode != SHOW_INTERVALS) {
        // equalize view widths so scrollbars are equal
        equalizeViewWidths( isRedraw);
      }

      if (! isStepButtonView) {
        expandViewFrame( viewFrame, (int) jGoView.getDocumentSize().getWidth(),
                         (int) jGoView.getDocumentSize().getHeight());
      }
      // print out info for created nodes
      // iterateOverJGoDocument(); // slower - many more nodes to go thru
      // iterateOverNodes();

      addStepButtons( jGoView);
      if (! isStepButtonView) {
        expandViewFrameForStepButtons( viewFrame, jGoView);
      }

      long stopTimeMSecs = System.currentTimeMillis();
      System.err.println( "   ... " + ViewConstants.TIMELINE_VIEW + " elapsed time: " +
                          (stopTimeMSecs -
                           PlanWorks.getPlanWorks().getViewRenderingStartTime
                           ( ViewConstants.TIMELINE_VIEW)) + " msecs.");
      startTimeMSecs = 0L;
    } else {
      closeView( this);
      return;
    }
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

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      synchronized( staticObject) {
        handleEvent(ViewListener.EVT_REDRAW_BEGUN_DRAWING);
        System.err.println( "Redrawing Timeline View ...");
        if (startTimeMSecs == 0L) {
          startTimeMSecs = System.currentTimeMillis();
        }
        try {
          ViewGenerics.setRedrawCursor( viewFrame);
          boolean isRedraw = true;
          renderTimelineAndSlotNodes( isRedraw);
          //horizonStart = jGoRulerView.getTimeScaleStart();
          //horizonEnd = jGoRulerView.getTimeScaleEnd();
          //Extend the horizon beyond the start of the last node 
          //and adjust the ruler accordingly
          //extendedHorizonEnd = horizonEnd + slotNodeScaleFactor * 
          //                          ViewConstants.TIMELINE_EXTEND_HORIZON_UNITS;
          //jGoRulerView.setTimeScaleEnd( extendedHorizonEnd );
          // redraw jGoRulerView, in case zoomFactor changed
          boolean isTimelineView = true;
          jGoRulerView.computeTimeScaleMetrics( isTimelineView, TimelineView.this.getZoomFactor(),
                                                TimelineView.this);
          jGoRulerView.createTimeScale( isTimelineView);
          if (timelineDisplayMode != SHOW_INTERVALS) {
            // equalize view widths so scrollbars are equal
            equalizeViewWidths( isRedraw);
          }

          addStepButtons( jGoView);
          // causes bottom view edge to creep off screen
          //       if (! isStepButtonView) {
          //         expandViewFrameForStepButtons( viewFrame, jGoView);
          //       }
        } finally {
          ViewGenerics.resetRedrawCursor( viewFrame);
        }
        long stopTimeMSecs = System.currentTimeMillis();
        System.err.println( "   ... " + ViewConstants.TIMELINE_VIEW + " elapsed time: " +
                            (stopTimeMSecs - startTimeMSecs) + " msecs.");
        startTimeMSecs = 0L;
        handleEvent(ViewListener.EVT_REDRAW_ENDED_DRAWING);
      }
    } //end run

  } // end class RedrawViewThread

  private boolean renderTimelineAndSlotNodes( boolean isRedraw) {
    jGoView.getDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    timelineNodeList = new ArrayList();
    freeTokenNodeList = new ArrayList();

    boolean isValid = createTimelineAndSlotNodes( isRedraw);
    if (isValid) {
      boolean showDialog = true;
      isContentSpecRendered( ViewConstants.TIMELINE_VIEW, showDialog);
    }
    return isValid;
  } // end renderTimelineAndSlotNodes

  /**
   * <code>getJGoView</code> - 
   *
   * @return - <code>JGoView</code> - 
   */
  public JGoView getJGoView()  {
    return jGoView;
  }

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>getTimelineNodeList</code>
   *
   * @return - <code>List</code> - of TimelineNode
   */
  public List getTimelineNodeList() {
    return timelineNodeList;
  }

  /**
   * <code>getFreeTokenNodeList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getFreeTokenNodeList() {
    return freeTokenNodeList;
  }

  /**
   * <code>getMouseOverNode</code> - node over which the mouse is moving - SlotNode/TokenNode
   *
   * @return - <code>JGoArea</code> - 
   */
  public JGoArea getMouseOverNode() {
    return mouseOverNode;
  }

  /**
   * <code>setMouseOverNode</code>
   *
   * @param mouseOverNode - <code>JGoArea</code> - 
   */
  public void setMouseOverNode( JGoArea mouseOverNode) {
    this.mouseOverNode = mouseOverNode;
  }

  /**
   * <code>isAutoSnapEnabled</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isAutoSnapEnabled() {
    return isAutoSnapEnabled;
  }

  /**
   * <code>getFocusNodeId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getFocusNodeId() {
    return focusNodeId;
  }

  private boolean createTimelineAndSlotNodes( boolean isRedraw) {
    int numTimelines = 0;
    int maxTimelineNodeWidth = 0;
    int maxSlotNodeWidth = 0;
    int minSlotDuration = DbConstants.PLUS_INFINITY_INT;
    //get width of longest timeline node
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      if (object.getObjectType() == DbConstants.O_TIMELINE) {
        PwTimeline timeline = (PwTimeline) object;
        String timelineName = timeline.getName();
        String parentName = null;
        if(timeline.getParent() != null) {
          parentName = timeline.getParent().getName();
        }
        String timelineNodeName = parentName + " : " + timelineName;
        String timelineKey = "timeline key=" + timeline.getId().toString();
        int nodeWidth = Math.max( SwingUtilities.computeStringWidth( this.fontMetrics,
                                                                     timelineNodeName),
                                  SwingUtilities.computeStringWidth( this.fontMetrics,
                                                                     timelineKey));
        if (nodeWidth > maxTimelineNodeWidth) {
          maxTimelineNodeWidth = nodeWidth;
        }
        //get width of longest slot node
        List slotList = timeline.getSlotList();
        Iterator slotIterator = slotList.iterator();
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          PwToken token = slot.getBaseToken();
          if ( token != null) {
            String slotNodeLabel = token.getPredicateName() + 
                                   "(" + slot.getTokenList().size() + ")";
            String keyValue = "\nslot key=" + slot.getId().toString();
            nodeWidth = Math.max( SwingUtilities.computeStringWidth( this.fontMetrics,
                                                                     slotNodeLabel),
                                  SwingUtilities.computeStringWidth( this.fontMetrics,
                                                                     keyValue));
            if (nodeWidth > maxSlotNodeWidth) {
              maxSlotNodeWidth = nodeWidth;
            }

            //get duration depending on EARLIEST, LATEST, or INTERVAL mode
            int duration;
            if ( minSlotDuration > 1 ) {
              duration = computeDuration( slot);
              if (duration < minSlotDuration) {
                minSlotDuration = duration;
              }
//            System.err.println( "createTimelineAndSlotNodes: minSlotDuration " + minSlotDuration);
            }
//          System.err.println( "createTimelineAndSlotNodes: maxSlotNodeWidth " + maxSlotNodeWidth);
          }
        }
        numTimelines++;
      }
    }
    //no timelines to view
    if (numTimelines == 0) {
      String message = "No timeline to view";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Timeline View Exception",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
      return false;
    }

//  System.err.println( "createTimelineAndSlotNodes: numTimelines " + numTimelines + 
//                      "   maxTimelineNodeWidth " + maxTimelineNodeWidth);
    slotNodeMaxWidth = maxSlotNodeWidth + ViewConstants.TIMELINE_VIEW_INSET_SIZE;
    timelineNodeMaxWidth = maxTimelineNodeWidth + ViewConstants.TIMELINE_VIEW_INSET_SIZE;
    if ( minSlotDuration < horizonEnd ) {
      slotNodeScaleFactor = minSlotDuration;
    } else {
      slotNodeScaleFactor = 1;
    }
    numTimelines++; // for free tokens
    //This is OK until we have more than one row for free tokens
    maxCellRow = numTimelines;  
    String title = "Rendering";
    if (isRedraw) {
      title = "Redrawing";
    }
    progressMonThread =
      createProgressMonitorThread( title + " Timeline View ...", 0, numTimelines,
			     Thread.currentThread(), this);
    if (! progressMonitorWait( progressMonThread, this)) {
      return false;
    }
    boolean isValid = true;
    numTimelines = 0;
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT;
    //time scale starts after timeline nodes
//  System.err.println( "createTimelineAndSlotNodes: slotNodeMaxWidth " + slotNodeMaxWidth);
//  System.err.println( "createTimelineAndSlotNodes: slotNodeScaleFactor " + slotNodeScaleFactor);
    //Extend the horizon beyond the start of the last node 
    //and adjust the ruler accordingly
    extendedHorizonEnd = horizonEnd + slotNodeScaleFactor * 
                                    ViewConstants.TIMELINE_EXTEND_HORIZON_UNITS;
    jGoRulerView.setTimeScaleEnd( extendedHorizonEnd );
    jGoRulerView.setStartXLoc(x + timelineNodeMaxWidth);
    jGoRulerView.setSlotNodeWidth( slotNodeMaxWidth );
    jGoRulerView.setSlotNodeScaleFactor( slotNodeScaleFactor );

    objectIterator = objectList.iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      if(object.getObjectType() == DbConstants.O_TIMELINE) {
        x = ViewConstants.TIMELINE_VIEW_X_INIT;
        PwTimeline timeline = (PwTimeline) object;
        String timelineName = timeline.getName();
        String parentName = null;
        if(timeline.getParent() != null) {
          parentName = timeline.getParent().getName();
        }
        String timelineLabel = "";
        if(parentName != null) {
          timelineLabel += parentName + " : ";
        }
        timelineLabel += timelineName + "\ntimeline key=" + timeline.getId().toString();
        Color timelineColor = getTimelineColor(timeline.getId());
        TimelineViewTimelineNode timelineNode =
          new TimelineViewTimelineNode(timelineLabel, timeline, new Point(x, y), maxTimelineNodeWidth,
                                       timelineColor, this);
        timelineNodeList.add(timelineNode);
        jGoDocument.addObjectAtTail(timelineNode);
        x += timelineNode.getSize().getWidth();
        isValid = createSlotNodes(timeline, timelineNode, x, y, timelineColor);
        if(! isValid) {
	  progressMonThread.setProgressMonitorCancel();
          return isValid;
        }
        y += ViewConstants.TIMELINE_VIEW_Y_DELTA;
        if (progressMonThread.getProgressMonitor().isCanceled()) {
          String msg = "User Canceled Timeline View Rendering";
          System.err.println( msg);
	  progressMonThread.setProgressMonitorCancel();
          return false;
        }
        numTimelines++;
        progressMonThread.getProgressMonitor().setProgress
	  ( numTimelines * ViewConstants.MONITOR_MIN_MAX_SCALING);
      }
    }
    isValid = createFreeTokenNodes( x, y);
    progressMonThread.setProgressMonitorCancel();
    return isValid;

  } // end createTimelineAndSlotNodes

  private boolean createFreeTokenNodes( int x, int y) {
    boolean isValid = true;
    y += ViewConstants.TIMELINE_VIEW_Y_INIT;
    List tokenList = partialPlan.getTokenList();
    Iterator tokenIterator = tokenList.iterator();
    boolean isFreeToken = true, isDraggable = false;
    PwSlot slot = null;
    Color backgroundColor = ViewConstants.FREE_TOKEN_BG_COLOR;
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      if (token.isSlotted()) {
        continue;
      } else if ((! token.isSlotted()) && (! token.isFree())) {
        // resourceTransactions - not displayed, put in displayedTokenIds
        isTokenInContentSpec( token);
        continue;
      } else { // free tokens
        if (isTokenInContentSpec( token))  {
          // increment by half the label width, since x is center, not left edge
          int divisor = 2;
          if (x == ViewConstants.TIMELINE_VIEW_X_INIT) { divisor = 1; }
          x = x +  Math.max( SwingUtilities.computeStringWidth
                             ( this.fontMetrics, token.getPredicateName()) / divisor,
                             SwingUtilities.computeStringWidth
                             ( this.fontMetrics, "key=" + token.getId().toString()) / divisor);
          TimelineTokenNode freeTokenNode =
            new TimelineTokenNode( token, slot, new Point( x, y), backgroundColor,
                                   isFreeToken, isDraggable, this);
          freeTokenNodeList.add( freeTokenNode);
          jGoDocument.addObjectAtTail( freeTokenNode);
          x = x + (int) freeTokenNode.getSize().getWidth();
        }
      }
    }
    return isValid;
  } // end createFreeTokenNodes

  private boolean createSlotNodes( PwTimeline timeline, TimelineViewTimelineNode timelineNode,
                                   int x, int y, Color backgroundColor) {
    boolean isValid = computeTimeIntervalLabelSize( timeline);
    if (! isValid) {
      return isValid;
    }
    List slotList = timeline.getSlotList();
    Iterator slotIterator = slotList.iterator();
    SlotNode previousSlotNode = null;
    SlotNode slotNode = null;
    boolean isFirstSlot = true;
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      // overloaded tokens on slot - not displayed, put in displayedTokenIds
      List tokenList = slot.getTokenList();
//       for (int i = 1, n = tokenList.size(); i < n; i++) {
//         isTokenInContentSpec( (PwToken) tokenList.get( i));
//       }
//       System.err.println( "createSlotNodes: slot " + slot.getId());
//       for (int i = 0, n = tokenList.size(); i < n; i++) {
//         PwToken tmpToken = (PwToken) tokenList.get( i);
//         System.err.println( "createSlotNodes: token " + tmpToken.getId() + " " +
//                             tmpToken.toString());
//       }
      tokenList.remove(slot.getBaseToken());
      for(int i = 0; i < tokenList.size(); i++) {
        isTokenInContentSpec((PwToken) tokenList.get(i));
      }
      boolean isLastSlot = (! slotIterator.hasNext());
      PwToken token = slot.getBaseToken();
//    if (token != null) {
//      System.err.println( "createSlotNodes: base token " + token.getId() + " " +
//                         token.toString());
//    }
      if ((token == null) && (isFirstSlot || isLastSlot)) {
//      if ( isFirstSlot) {
//        System.err.println( "createSlotNodes: First Slot is empty " );
//      }
        // discard leading and trailing empty slots (planworks/test/data/emptySlots)
      } else {
        // check for empty slots - always show them
        if ((token == null) ||
            (token != null) && isTokenInContentSpec( token)) {
          String slotNodeLabel = getSlotNodeLabel( token, slot, isFirstSlot);
          slotNode = new SlotNode( slotNodeLabel, slot, timeline, new Point( x, y),
                                   previousSlotNode, isFirstSlot, isLastSlot,
                                   backgroundColor, slotNodeMaxWidth, slotNodeScaleFactor,
                                   horizonStart, extendedHorizonEnd, 
                                   timelineDisplayMode, this);
          timelineNode.addToSlotNodeList( slotNode);
//        System.err.println( "createSlotNodes: x " + x + " y " + y + " width " +
//                         slotNode.getSize().getWidth());
          jGoDocument.addObjectAtTail( slotNode);
          previousSlotNode = slotNode;
          // x += slotNode.getSize().getWidth();
          // SlotNode code alters location due to Content Spec filtering
          // System.err.println( "old x " + (x + (int) slotNode.getSize().getWidth()));
          if ( timelineDisplayMode == SHOW_INTERVALS ) {
            //In show intervals mode compute the position of the next node.
            //Otherwise, the position of the node will be computed by the slot node.
            x = (int) (slotNode.getLocation().getX() + slotNode.getSize().getWidth()) +
              ViewConstants.TIMELINE_VIEW_INSET_SIZE - 2;
          }
//        System.err.println( "createSlotNodes: new x = " + x + "\n");
          isFirstSlot = false;
        }
      }
    }
    return isValid;
  } // end createSlotNodes


  // perform two integrity checks:
  //   1) two successive empty slots must not occur
  //   2) earliest start times must be monotonically increasing
  // while computing time interval max label size
  private boolean computeTimeIntervalLabelSize( PwTimeline timeline) {
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
    List slotList = timeline.getSlotList();
    ListIterator slotIterator = slotList.listIterator();
    PwSlot previousSlot = null;
    SlotNode slotNode = null;
    boolean foundEmptySlot = false;
    boolean foundNonMonotonicError = false;
    int earliestStartTime = DbConstants.MINUS_INFINITY_INT;
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      PwToken token = slot.getBaseToken();
      if (token == null) {
        if (foundEmptySlot) {
          String message = "Two successive empty slots found in timeline '" +
            timeline.getName() + "' (id = " + timeline.getId() + ")";
          JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                         "Timeline View Exception",
                                         JOptionPane.ERROR_MESSAGE);
          System.err.println( message);
          return false;
        } else {
          foundEmptySlot = true;
        }
      } else {
        foundEmptySlot = false;
      }
      boolean isLastSlot = (! slotIterator.hasNext());
      PwDomain startTimeIntervalDomain = slot.getStartTime();
      PwDomain endTimeIntervalDomain = slot.getEndTime();
      if ((endTimeIntervalDomain != null) &&
          ((endTimeIntervalDomain.toString().length() +
            ViewConstants.TIME_INTERVAL_STRINGS_OVERLAP_OFFSET) >
           slotLabelMinLength)) {
        slotLabelMinLength = endTimeIntervalDomain.toString().length() +
          ViewConstants.TIME_INTERVAL_STRINGS_OVERLAP_OFFSET;
      }
      if (startTimeIntervalDomain != null) {
        if (startTimeIntervalDomain.getLowerBoundInt() >= earliestStartTime) {
          earliestStartTime = startTimeIntervalDomain.getLowerBoundInt();
        } else {
          PwToken previousToken = previousSlot.getBaseToken();
          boolean continueRendering =
            outputNonMonotonicError( slot, previousSlot, token, previousToken, timeline,
                                     foundNonMonotonicError);
          foundNonMonotonicError = true;
          if (! continueRendering) {
            return false;
          }
        }
      }
      previousSlot = slot;
    }
    return true;
  } // end computeTimeIntervalLabelSize

  private boolean outputNonMonotonicError( PwSlot slot, PwSlot previousSlot, PwToken token,
                                           PwToken previousToken, PwTimeline timeline,
                                           boolean foundNonMonotonicError) {
    boolean continueRendering = false;
    String previousTokenIdString = "", tokenIdString = "";
    String previousPredicateName = "-empty-", predicateName = "-empty-";
    // check for empty slots
    if (previousToken != null) {
      previousTokenIdString = previousToken.getId().toString();
      previousPredicateName = previousToken.getPredicateName();
    }
    if (token != null) {
      tokenIdString = token.getId().toString();
      predicateName = token.getPredicateName();
    }
    String message = "Earliest start times are not monotonically increasing " +
      "in timeline '" + timeline.getName() + "' (id = " + timeline.getId() + ")" +
      "\npreviousPredicate = '" + previousPredicateName +
      "', slotId = " + previousSlot.getId() + ", tokenId = " + previousTokenIdString + 
      ", start = " + previousSlot.getStartTime() + ", end = " + previousSlot.getEndTime() +
      "\npredicate = '" + predicateName + "', slotId = " + slot.getId() +
      ", tokenId = " + tokenIdString + ", start = " + slot.getStartTime() + ", end = " + 
      slot.getEndTime();
    System.err.println( "\n" + message);

//     JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
//                                    "Timeline View Exception",
//                                    JOptionPane.ERROR_MESSAGE);
    if (! foundNonMonotonicError) {
      int response  = JOptionPane.showConfirmDialog( PlanWorks.getPlanWorks(), message,
                                                     "Timeline View Exception -- Continue ?",
                                                     JOptionPane.YES_NO_OPTION);
      if (response == JOptionPane.YES_OPTION) {
        continueRendering = true;
      }
    } else {
      continueRendering = true;
    }
    return continueRendering;
  } // end outputNonMonotonicError


  // get the duration of this slot for the current timeline display mode
  // if either bound is plus or minus infinity, return plus infinity for duration
  // returns duration of infinity for interval mode, since this mode is not grounded
  // and results are not used.
  private int computeDuration(PwSlot slot) {
    int startTime = 0;
    int endTime = 0;
    int duration = DbConstants.PLUS_INFINITY_INT;
    PwDomain startTimeIntervalDomain = slot.getStartTime();
    PwDomain endTimeIntervalDomain = slot.getEndTime();

    if (timelineDisplayMode == TimelineView.SHOW_EARLIEST) {
      startTime = startTimeIntervalDomain.getLowerBoundInt();
      endTime = endTimeIntervalDomain.getLowerBoundInt();
    } else if (timelineDisplayMode == TimelineView.SHOW_LATEST) {
      startTime = startTimeIntervalDomain.getUpperBoundInt();
      endTime = endTimeIntervalDomain.getUpperBoundInt();
    } else {
      return duration;  //INTERVAL view - return plus infinity
      
    }
    if ( startTime == DbConstants.MINUS_INFINITY_INT || 
        startTime == DbConstants.PLUS_INFINITY_INT ||
        endTime == DbConstants.MINUS_INFINITY_INT ||
        endTime == DbConstants.PLUS_INFINITY_INT ) {
      return duration;  //plus infinity
    }
    if ( endTime >= startTime ) {
      duration = endTime - startTime;
    }
    return duration;
  }


// make all timeline nodes the same width
//  private int computeTimelineNodesWidth( List timelineList, String objectName) {
//    int maxNodeWidth = 0;
//    Iterator timelineIterator = timelineList.iterator();
//    while (timelineIterator.hasNext()) {
//      PwTimeline timeline = (PwTimeline) timelineIterator.next();
//      String timelineName = timeline.getName();
//      String timelineNodeName = objectName + " : " + timelineName;
//      String timelineKey = "timeline key=" + timeline.getId().toString();
//      int nodeWidth = Math.max( SwingUtilities.computeStringWidth( this.fontMetrics,
//                                                                   timelineNodeName),
//                                SwingUtilities.computeStringWidth( this.fontMetrics,
//                                                                   timelineKey));
//      if (nodeWidth > maxNodeWidth) {
//        maxNodeWidth = nodeWidth;
//      }
//    }
//    return maxNodeWidth + 2 *  ViewConstants.TIMELINE_VIEW_INSET_SIZE;
//  } // end computeTimelineNodesWidth


  /**
   * <code>getSlotNodeLabel</code> - pad slot label with blanks so that time
   *                       interval lables underneath, do not overlap with
   *                       adjacent slots
   *
   * @param token - <code>PwToken</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param  isFirstSlot - <code>boolean</code> -  
   * @return - <code>String</code> - 
   */
  public String getSlotNodeLabel( PwToken token, PwSlot slot, boolean isFirstSlot) {
    PwDomain endTimeIntervalDomain = slot.getEndTime();
    int endTime = endTimeIntervalDomain.getUpperBoundInt();
    StringBuffer label = null;
    String keyValue = "\nslot key=" + slot.getId().toString();
    if (token == null) { // empty slot
      label = new StringBuffer( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
    } else {
      label = new StringBuffer( token.getPredicateName());
      label.append( " (").append( String.valueOf( slot.getTokenList().size()));
      label.append( ")");
    }
    if ( timelineDisplayMode == SHOW_LATEST && endTime == DbConstants.PLUS_INFINITY_INT) {
      label.append( "      ---> ");
      label.append( DbConstants.PLUS_INFINITY_UNIC);
    }
    int labelMinLength = slotLabelMinLength;
    PwDomain startTimeIntervalDomain = slot.getStartTime();
    if ((! startTimeIntervalDomain.getLowerBound().equals
         ( startTimeIntervalDomain.getUpperBound())) && isFirstSlot) {
      // start interval is [1234 1235], rather than {1234},
      // first slot has left alignment to left edge of slot
      labelMinLength = (int) (labelMinLength * LABEL_MIN_LEN_FIRST_SLOT_FACTOR);
    }
    int nodeLength = Math.max( label.length(), keyValue.length());
//     System.err.println( "getSlotNodeLabel nodeLength " + nodeLength +
//                         " labelMinLength " + labelMinLength);
//     System.err.println( "getSlotNodeLabel label B '" + label + "'");
    if (nodeLength < labelMinLength) { 
      boolean prepend = true;
      for (int i = 0, n = labelMinLength - nodeLength; i < n; i++) {
        if (prepend) {
          label.insert( 0, " ");
        } else {
          label.append( " ");
        }
        prepend = (! prepend);
      }
    }
//     System.err.println( "getSlotNodeLabel label A '" + label + "'");
    return label.toString() + keyValue;
  } // end getSlotNodeLabel

  private void iterateOverNodes() {
    int numTimelineNodes = timelineNodeList.size();
    //System.err.println( "iterateOverNodes: numTimelineNodes " + numTimelineNodes);
    Iterator timelineIterator = timelineNodeList.iterator();
    while (timelineIterator.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineIterator.next();
      //System.err.println( "name '" + timelineNode.getTimelineName() + "' location " +
      //                    timelineNode.getLocation());
      int numSlotNodes = timelineNode.getSlotNodeList().size();
      //System.err.println( "numSlotNodes " + numSlotNodes); 
      Iterator slotIterator = timelineNode.getSlotNodeList().iterator();
      while (slotIterator.hasNext()) {
        SlotNode slotNode = (SlotNode) slotIterator.next();
        //System.err.println( "name '" + slotNode.getPredicateName() + "' location " +
        //                    slotNode.getLocation());
        //System.err.println( "startInterval " + slotNode.getStartTimeIntervalString());
        if (! slotIterator.hasNext()) {
          //System.err.println( "endInterval " + slotNode.getEndTimeIntervalString());
        }
      }
    }
  } // end iterateOverNodes


  private void iterateOverJGoDocument() {
    JGoListPosition position = jGoDocument.getFirstObjectPos();
    int cnt = 0;
    while (position != null) {
      JGoObject object = jGoDocument.getObjectAtPos( position);
      position = jGoDocument.getNextObjectPosAtTop( position);
      //System.err.println( "iterateOverJGoDoc: position " + position +
      //                    " className " + object.getClass().getName());
      if (object instanceof SlotNode) {
        SlotNode slotNode = (SlotNode) object;

      }
      cnt += 1;
//       if (cnt > 100) {
//         break;
//       }
    }
    //System.err.println( "iterateOverJGoDoc: cnt " + cnt);
  } // end iterateOverJGoDocument


  // write a line at the max horizontal timeline in each view, and
  // at max vertical timeline in jGoView
  private void equalizeViewWidths( boolean isRedraw) {
    Dimension timelineViewDocument = jGoView.getDocumentSize();
    //timelineViewDocument = jGoView.docToViewCoords(timelineViewDocument);
    Dimension rulerViewDocument = jGoRulerView.getDocumentSize();
//  System.err.println( "timelineViewDocumentWidth B " + timelineViewDocument.getWidth() +
//                      " rulerViewDocumentWidth B " + rulerViewDocument.getWidth());
    int xRulerMargin = ViewConstants.TIMELINE_VIEW_X_INIT;
    int jGoDocBorderWidth = ViewConstants.JGO_DOC_BORDER_WIDTH;
    if (isRedraw) {
      xRulerMargin = 0;
    }
    //since ruler is not scaled but recreated, rulerView document size must be
    //in same scale as TimelineView document before equalizing view width.
    //adjust ruler view by zoom factor to make same scale
    int maxWidth = Math.max( (int) timelineViewDocument.getWidth() - jGoDocBorderWidth,
                             (int) (rulerViewDocument.getWidth() * zoomFactor) + 
                             xRulerMargin - jGoDocBorderWidth);
//  System.err.println( "equalizeViewWidths: maxWidth " + maxWidth);
    JGoStroke maxViewWidthPoint = new JGoStroke();
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
    // make mark invisible
    maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                          ViewConstants.VIEW_BACKGROUND_COLOR));
    jGoView.getDocument().addObjectAtTail( maxViewWidthPoint);
    // always put mark at max y location, so on redraw jGoRulerView does not expand
    JGoStroke maxViewHeightPoint = new JGoStroke();
    int maxYLoc = startYLoc + ((maxCellRow + 1) *
                               ViewConstants.TEMPORAL_NODE_CELL_HEIGHT) + 2;
//  System.err.println( "equalizeViewWidths: maxCellRow " + maxCellRow +
//                       " maxYLoc " + maxYLoc );
    maxViewHeightPoint.addPoint( maxWidth, maxYLoc);
    maxViewHeightPoint.addPoint( maxWidth - ViewConstants.TIMELINE_VIEW_X_INIT,
                                 maxYLoc);
    // make mark invisible
    maxViewHeightPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                           ViewConstants.VIEW_BACKGROUND_COLOR));
    jGoView.getDocument().addObjectAtTail( maxViewHeightPoint);
                                                                                             
    if (! isRedraw ) {
      maxViewWidthPoint = new JGoStroke();
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
      // make mark invisible
      maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                            ViewConstants.VIEW_BACKGROUND_COLOR));
      jGoRulerView.getDocument().addObjectAtTail( maxViewWidthPoint);
    }
       timelineViewDocument = jGoView.getDocumentSize();
       rulerViewDocument = jGoRulerView.getDocumentSize();
//     System.err.println( "timelineViewDocumentWidth A " + timelineViewDocument.getWidth() +
//                         " rulerViewDocumentWidth A " + rulerViewDocument.getWidth());
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
      return new Dimension( (int) TimelineView.this.getSize().getWidth(),
                            (int) jGoRulerView.getDocumentSize().getHeight() +
                            (int) jGoRulerView.getHorizontalScrollBar().getSize().getHeight());
    }
                                                                                  
    /**
     * <code>getMaximumSize</code>
     *
     * @return - <code>Dimension</code> -
     */
    public Dimension getMaximumSize() {
      return new Dimension( (int) TimelineView.this.getSize().getWidth(),
                            (int) jGoRulerView.getDocumentSize().getHeight() +
                            (int) jGoRulerView.getHorizontalScrollBar().getSize().getHeight());
    }
                                                                                  
  } // end class RulerPanel

  /**
   * <code>ScrollBarListener</code> - keep both jGoView & jGoRulerView aligned,
   *                                  when user moves one scroll bar
   *
   */
  class ScrollBarListener implements AdjustmentListener {
                                                                                  
    /**
     * <code>adjustmentValueChanged</code> - keep both jGoView & jGoRulerView
     *                                aligned, even when user moves one scroll bar     *
     * @param event - <code>AdjustmentEvent</code> -
     */
    public void adjustmentValueChanged( AdjustmentEvent event) {
      JScrollBar source = (JScrollBar) event.getSource();
      // to get immediate incremental adjustment, rather than waiting for
      // final position, comment out next check
      // if (! source.getValueIsAdjusting()) {
//         System.err.println( "adjustmentValueChanged " + source.getValue());
//        System.err.println( "\njGoView " +
//                             jGoView.getHorizontalScrollBar().getValue());
//         System.err.println( "jGoRulerView " +
//                             jGoRulerView.getHorizontalScrollBar().getValue());
        int newPostion = source.getValue();
//         System.err.println( "newPostion " + newPostion);
        int timeScaleViewPosition = 0, timelineViewPosition = 0;
        if (source.getParent() instanceof TimeScaleView) {
          timeScaleViewPosition = newPostion;
          timelineViewPosition = (int) ((double) zoomFactor * newPostion);
        } else {
          timeScaleViewPosition = (int) (newPostion / (double) zoomFactor);
          timelineViewPosition = newPostion;
        }
        if (timelineViewPosition != jGoView.getHorizontalScrollBar().getValue()) {
          jGoView.getHorizontalScrollBar().setValue( timelineViewPosition);
        } else if (timeScaleViewPosition != jGoRulerView.getHorizontalScrollBar().getValue()) {
          jGoRulerView.getHorizontalScrollBar().setValue( timeScaleViewPosition);
        }
        // }
    } // end adjustmentValueChanged
                                                                                  
  } // end class ScrollBarListener
                                                                                  
  /**
   * <code>TimelineJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  public class TimelineJGoView extends JGoView {

    /**
     * <code>TimelineJGoView</code> - constructor 
     *
     */
    public TimelineJGoView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *                                 1) snap to active token
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        TimelineView.docCoords = docCoords;
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick

  } // end class TimelineJGoView


  private void mouseRightPopupMenu( Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    String className = PlanWorks.getViewClassName( ViewConstants.TEMPORAL_EXTENT_VIEW);
    if (viewSet.viewExists( className)) {
      if (! isAutoSnapEnabled) {
        JMenuItem enableAutoSnapItem = new JMenuItem( "Enable Auto Snap");
        createEnableAutoSnapItem( enableAutoSnapItem);
        mouseRightPopup.add( enableAutoSnapItem);
      } else {
        JMenuItem disableAutoSnapItem = new JMenuItem( "Disable Auto Snap");
        createDisableAutoSnapItem( disableAutoSnapItem);
        mouseRightPopup.add( disableAutoSnapItem);
      }
      mouseRightPopup.addSeparator();
    }

    createTemporalDisplayItems( mouseRightPopup);
                                                                           
    mouseRightPopup.addSeparator();

    JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
    createNodeByKeyItem( nodeByKeyItem);
    mouseRightPopup.add( nodeByKeyItem);

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         viewListenerList, ViewConstants.TIMELINE_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
    createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);
    
    if (((PartialPlanViewSet) this.getViewSet()).getActiveToken() != null) {
      JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
      createActiveTokenItem( activeTokenItem);
      mouseRightPopup.add( activeTokenItem);
    }

    this.createZoomItem( jGoView, zoomFactor, mouseRightPopup, this);

    if (viewSet.doesViewFrameExist( ViewConstants.NAVIGATOR_VIEW)) {
      mouseRightPopup.addSeparator();
      JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
      createCloseNavigatorWindowsItem( closeWindowsItem);
      mouseRightPopup.add( closeWindowsItem);
    }
    createAllViewItems( partialPlan, partialPlanName, planSequence, viewListenerList,
                        mouseRightPopup);

    createStepAllViewItems( partialPlan, mouseRightPopup);

    ViewGenerics.createConfigPopupItems( ViewConstants.TIMELINE_VIEW, this, mouseRightPopup);

    ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createEnableAutoSnapItem( JMenuItem enableAutoSnapItem) {
    enableAutoSnapItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          isAutoSnapEnabled = true;
        }
      });
  } // end createEnableAutoSnapItem

  private void createDisableAutoSnapItem( JMenuItem disableAutoSnapItem) {
    disableAutoSnapItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          isAutoSnapEnabled = false;
        }
      });
  } // end createDisableAutoSnapItem

  private void createTemporalDisplayItems( JPopupMenu mouseRightPopup) {
    JMenuItem showIntervalsItem = null;
    JMenuItem showEarliestItem = null;
    JMenuItem showLatestItem = null;
    JMenuItem timeMarkItem = null;
    if (timelineDisplayMode == SHOW_INTERVALS) {
      showEarliestItem = new JMenuItem( SHOW_EARLIEST_LABEL);
      createShowEarliestItem( showEarliestItem);
      mouseRightPopup.add( showEarliestItem);
      showLatestItem = new JMenuItem( SHOW_LATEST_LABEL);
      createShowLatestItem( showLatestItem);
      mouseRightPopup.add( showLatestItem);
    } else if (timelineDisplayMode == SHOW_EARLIEST) {
      showIntervalsItem = new JMenuItem( SHOW_INTERVALS_LABEL);
      createShowIntervalsItem( showIntervalsItem);
      mouseRightPopup.add( showIntervalsItem);
      showLatestItem = new JMenuItem( SHOW_LATEST_LABEL);
      createShowLatestItem( showLatestItem);
      mouseRightPopup.add( showLatestItem);
      timeMarkItem = new JMenuItem( "Set Time Scale Line");
      createTimeMarkItem( timeMarkItem);
      mouseRightPopup.add( timeMarkItem);
    } else if (timelineDisplayMode == SHOW_LATEST) {
      showIntervalsItem = new JMenuItem( SHOW_INTERVALS_LABEL);
      createShowIntervalsItem( showIntervalsItem);
      mouseRightPopup.add( showIntervalsItem);
      showEarliestItem = new JMenuItem( SHOW_EARLIEST_LABEL);
      createShowEarliestItem( showEarliestItem);
      mouseRightPopup.add( showEarliestItem);
      timeMarkItem = new JMenuItem( "Set Time Scale Line");
      createTimeMarkItem( timeMarkItem);
      mouseRightPopup.add( timeMarkItem);
    }
  } // end createTemporalDisplayItems
                                                                           
  private void createShowIntervalsItem( JMenuItem showIntervalsItem) {
    showIntervalsItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          timelineDisplayMode = SHOW_INTERVALS;
          jGoView.getHorizontalScrollBar().setValue( 0);
          jGoView.getVerticalScrollBar().setValue( 0);
          jGoRulerView.setVisible( false );
          TimelineView.this.redraw();
        }
      });
  } // end createShowIntervalsItem
                                                                           
                                                                           
  private void createShowEarliestItem( JMenuItem showEarliestItem) {
    showEarliestItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          timelineDisplayMode = SHOW_EARLIEST;
          jGoView.getHorizontalScrollBar().setValue( 0);
          jGoView.getVerticalScrollBar().setValue( 0);
          jGoRulerView.setVisible( true );
          TimelineView.this.redraw();
        }
      });
  } // end createShowEarliestItem
                                                                           
                                                                           
  private void createShowLatestItem( JMenuItem showLatestItem) {
    showLatestItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          timelineDisplayMode = SHOW_LATEST;
          jGoView.getHorizontalScrollBar().setValue( 0);
          jGoView.getVerticalScrollBar().setValue( 0);
          jGoRulerView.setVisible( true );
          TimelineView.this.redraw();
        }
      });
  } // end createShowLatestItem
                                                                           
  private void createNodeByKeyItem( JMenuItem nodeByKeyItem) {
    nodeByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskNodeByKey nodeByKeyDialog =
            new AskNodeByKey( "Find by Key", "key (int)", TimelineView.this);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());
            PwToken tokenToFind = partialPlan.getToken( nodeKey);
            if (tokenToFind != null) {
              boolean isByKey = true;
              findAndSelectToken( tokenToFind, isByKey);
            } else {
              PwSlot slotToFind = partialPlan.getSlot( nodeKey);
              if (slotToFind != null) {
                findAndSelectSlot( slotToFind);
              } else {
                PwTimeline timelineToFind = partialPlan.getTimeline( nodeKey);
                if (timelineToFind != null) {
                  findAndSelectTimeline( timelineToFind);
                }
              }
            }
          }
        }
      });
  } // end createNodeByKeyItem

  private void createActiveTokenItem( JMenuItem activeTokenItem) {
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) TimelineView.this.getViewSet()).getActiveToken();
          if (activeToken != null) {
            boolean isByKey = false;
            findAndSelectToken( activeToken, isByKey);
          }
        }
      });
  } // end createActiveTokenItem

  /**
   * <code>findAndSelectToken</code> - public for DecisionView
   *
   * @param tokenToFind - <code>PwToken</code> - 
   * @param isByKey - <code>boolean</code> - 
   */
  public void findAndSelectToken( PwToken tokenToFind, boolean isByKey) {
    boolean isTokenFound = false, isHighlightNode = true;
    Iterator timelineNodeListItr = timelineNodeList.iterator();
    foundIt:
    while (timelineNodeListItr.hasNext()) {
      TimelineViewTimelineNode timelineNode =
        (TimelineViewTimelineNode) timelineNodeListItr.next();
      Iterator slotNodeListItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeListItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeListItr.next();
        List tokenList = slotNode.getSlot().getTokenList();
        if (tokenList == null) { // empty slot
          continue;
        }
        Iterator tokenItr = tokenList.iterator();
        while (tokenItr.hasNext()) {
          PwToken token = (PwToken) tokenItr.next();
          if (token.getId().equals( tokenToFind.getId())) {
            System.err.println( "TimelineView found token: " +
                                tokenToFind.getPredicateName() +
                                " (key=" + tokenToFind.getId().toString() + ")");
            focusNodeId = token.getId();
            NodeGenerics.focusViewOnNode( slotNode, isHighlightNode, jGoView);
            // secondary nodes do not apply here
            isTokenFound = true;
            break foundIt;
          }
        }
      }
    }
    if (! isTokenFound) {
      Iterator freeTokenNodeItr = freeTokenNodeList.iterator();
      while (freeTokenNodeItr.hasNext()) {
        TokenNode freeTokenNode = (TokenNode) freeTokenNodeItr.next();
        if (freeTokenNode.getToken().getId().equals( tokenToFind.getId())) {
          System.err.println( "TimelineView found token: " +
                              tokenToFind.getPredicateName() +                   
                              " (key=" + tokenToFind.getId().toString() + ")");
          focusNodeId = freeTokenNode.getToken().getId();
          NodeGenerics.focusViewOnNode( freeTokenNode, isHighlightNode, jGoView);
          // secondary nodes do not apply here
          isTokenFound = true;
          break;
        }
      }
    }
    if (! isTokenFound) {
      String message = null;
      if (tokenToFind instanceof PwResourceTransaction) {
        message = "Sorry, \"" + tokenToFind.getId().toString() + "\" " +
          "is a valid key, but is not available in this view.";
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), message, "Key Not Available", JOptionPane.ERROR_MESSAGE);
      } else {
        // Content Spec filtering may cause this to happen
        message = "Token " + tokenToFind.getPredicateName() + " (key=" +
          tokenToFind.getId().toString() + ") not found.";
        JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                       "Token Not Currently Found in TimelineView",
                                       JOptionPane.ERROR_MESSAGE);
      }
      System.err.println( message);
    }
  } // end findAndSelectToken


  private void findAndSelectSlot( PwSlot slotToFind) {
    boolean isSlotFound = false;
    boolean isHighlightNode = true;
    Iterator timelineNodeListItr = timelineNodeList.iterator();
    foundIt:
    while (timelineNodeListItr.hasNext()) {
      TimelineViewTimelineNode timelineNode =
        (TimelineViewTimelineNode) timelineNodeListItr.next();
      Iterator slotNodeListItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeListItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeListItr.next();
        if (slotNode.getSlot().getId().equals( slotToFind.getId())) {
          System.err.println( "TimelineView found slot: " +
                              slotNode.getPredicateName() +
                              " (key=" + slotToFind.getId().toString() + ")");
          focusNodeId = slotNode.getSlot().getId();
          NodeGenerics.focusViewOnNode( slotNode, isHighlightNode, jGoView);
          // secondary nodes do not apply here
          isSlotFound = true;
          break foundIt;
        }
      }
    }
    if (! isSlotFound) {
      // Content Spec filtering may cause this to happen
      String message = "Slot (key=" + slotToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Slot Not Currently Found in TimelineView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectSlot


  /**
   * <code>findAndSelectTimeline</code> - public for DecisionView
   *
   * @param timelineToFind - <code>PwTimeline</code> - 
   */
  public void findAndSelectTimeline( PwTimeline timelineToFind) {
    boolean isTimelineFound = false;
    boolean isHighlightNode = true;
    Iterator timelineNodeListItr = timelineNodeList.iterator();
    while (timelineNodeListItr.hasNext()) {
      TimelineViewTimelineNode timelineNode =
        (TimelineViewTimelineNode) timelineNodeListItr.next();
      if (timelineNode.getTimeline().getId().equals( timelineToFind.getId())) {
        System.err.println( "TimelineView found timeline: " +
                            timelineNode.getTimeline().getName() +
                            " (key=" + timelineToFind.getId().toString() + ")");
        focusNodeId = timelineNode.getTimeline().getId();
        NodeGenerics.focusViewOnNode( timelineNode, isHighlightNode, jGoView);
        isTimelineFound = true;
        break;
      }
    }
    if (! isTimelineFound) {
      // Content Spec filtering may cause this to happen
      String message = "Timeline (key=" + timelineToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Timeline Not Currently Found in TimelineView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectTimeline

  private void createTimeMarkItem( JMenuItem timeMarkItem) {
    timeMarkItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          int xLoc = (int) TimelineView.docCoords.getX();
           //System.err.println( "doMouseClick: xLoc " + xLoc );          
          if (timeScaleMark != null) {
            jGoView.getDocument().removeObject( timeScaleMark);
          }
          timeScaleMark = new TimeScaleMark( xLoc, getZoomFactor());
          timeScaleMark.addPoint( xLoc, 0);
          timeScaleMark.addPoint( xLoc, ((maxCellRow + 1) *
                                         ViewConstants.TEMPORAL_NODE_CELL_HEIGHT *
                                         getZoomFactor()));
          jGoView.getDocument().addObjectAtTail( timeScaleMark);
        }
      });
  } // end createTimeMarkItem


  private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                         final TimelineView timelineView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public void actionPerformed( ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( ViewConstants.TIMELINE_VIEW, partialPlan,
                                            timelineView, viewSet, jGoView, viewCoords);
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
  public class TimeScaleMark extends JGoStroke {
                                                                                             
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
                                                                                             
    /**
     * <code>getXLoc</code>
     *
     * @return - <code>int</code> -
     */
    public int getXLoc() {
      return xLoc;
    }
                                                                                             
    /**
     * <code>getToolTipText</code>
     *
     * @return - <code>String</code> -
     */
    public String getToolTipText() {
      return String.valueOf( jGoRulerView.scaleXLocNoZoom( xLoc));
    }
                                                                                             
  } // end class TimeScaleMark
                                                                                             
} // end class TimelineView
 



