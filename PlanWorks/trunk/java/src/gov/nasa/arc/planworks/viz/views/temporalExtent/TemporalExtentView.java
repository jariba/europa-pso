// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TemporalExtentView.java,v 1.11 2003-09-02 21:49:17 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 21July03
//

package gov.nasa.arc.planworks.viz.views.temporalExtent;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;


import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.SlotNode;
import gov.nasa.arc.planworks.viz.nodes.TemporalNode;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.views.VizView;


/**
 * <code>TemporalExtentView</code> - render a partial plan's timelines and slots
 *                JPanel->VizView->TemporalExtentView
 *                JComponent->JGoView
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TemporalExtentView extends VizView {

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private JGoView jGoView;
  private String viewName;
  private JGoDocument jGoDocument;
  private JGoSelection jGoSelection;
  // temporalNodeList & tmpTemporalNodeList used by JFCUnit test case
  private List temporalNodeList; // element TemporalNode
  private List tmpTemporalNodeList; // element TemporalNode
  private Font font;
  private FontMetrics fontMetrics;
  private int slotLabelMinLength;
  private int timeScaleStart;
  private int timeScaleEnd;
  private int maxSlots;
  private int startXLoc;
  private int xOrigin;
  private int endXLoc;
  private int startYLoc;
  private int timeDelta;
  private int tickTime;
  private int maxCellRow;
  private float timeScale;
  private int maxViewWidth;
  private int maxViewHeight;

  /**
   * <code>TemporalExtentView</code> - constructor - called by ViewSet.openTemporalExtentView.
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TemporalExtentView( PwPartialPlan partialPlan, long startTimeMSecs,
                             ViewSet viewSet) {
    super( partialPlan);
    this.partialPlan = partialPlan;
    this.startTimeMSecs = startTimeMSecs;
    this.viewSet = viewSet;
    viewName = "temporalExtentView";
    temporalNodeList = new ArrayList();
    tmpTemporalNodeList = new ArrayList();

    startXLoc = ViewConstants.TIMELINE_VIEW_X_INIT * 2;
    startYLoc = ViewConstants.TIMELINE_VIEW_Y_INIT;
    maxCellRow = 0;
    maxViewWidth = PlanWorks.INTERNAL_FRAME_WIDTH;
    maxViewHeight = PlanWorks.INTERNAL_FRAME_HEIGHT;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;

    jGoView = new JGoView();
    jGoSelection = new JGoSelection( jGoView);
    jGoView.setBackground( ColorMap.getColor( "lightGray"));
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);

    SwingUtilities.invokeLater( runInit);
  } // end constructor

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
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use runInit in constructor
   */
  public void init() {
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for TemporalExtentView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "timelineView displayable " + this.isDisplayable());
    }
    Graphics graphics = ((JPanel) this).getGraphics();
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    // does nothing
    // jGoView.setFont( font);
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();

    jGoDocument = jGoView.getDocument();

    collectAndComputeTimeScaleMetrics();
    createTemporalNodes();
    layoutTemporalNodes();
    createTimeScale();
    // setVisible( true | false) depending on ContentSpec
    setNodesVisible();
    expandViewFrame( viewSet, viewName, maxViewWidth, maxViewHeight);

    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

    long stopTimeMSecs = (new Date()).getTime();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *                       setVisible(true | false)
   *                       according to the Content Spec enabled ids
   *
   */
  public void redraw() {
    new RedrawViewThread().start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      redrawView();
    } //end run

  } // end class RedrawViewThread

  private void redrawView() {
    // setVisible(true | false) depending on ids
    setNodesVisible();
    expandViewFrame( viewSet, viewName, maxViewWidth, maxViewHeight);
  } // end redrawView

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>getFontMetrics</code>
   *
   * @return - <code>FontMetrics</code> - 
   */
  public FontMetrics getFontMetrics()  {
    return fontMetrics;
  }

  /**
   * <code>getSlotLabelMinLength</code> - pad labels with blanks up to min size,
   *                           initially that of "empty" label, then base it on
   *                           length of time interval string.  This prevents
   *                           time interval strings of adjacent slots from
   *                           overlaying each other.
   *
   * @return - <code>int</code> - 
   */
  public int getSlotLabelMinLength() {
    return slotLabelMinLength;
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
   * <code>getTimeScaleStart</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTimeScaleStart() {
    return timeScaleStart;
  }

  /**
   * <code>getTimeScaleEnd</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTimeScaleEnd() {
    return timeScaleEnd;
  }

  /**
   * <code>scaleTime</code>
   *
   * @param time - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public int scaleTime( int time) {
    return xOrigin + (int) (timeScale * time);
  }

  /**
   * <code>getStartYLoc</code>
   *
   * @return - <code>int</code> - 
   */
  public int getStartYLoc() {
    return startYLoc;
  }

  private void collectAndComputeTimeScaleMetrics() {
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    boolean alwaysReturnEnd = true;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      List timelineList = object.getTimelineList();
      Iterator timelineIterator = timelineList.iterator();
      while (timelineIterator.hasNext()) {
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        List slotList = timeline.getSlotList();
        Iterator slotIterator = slotList.iterator();
        PwToken previousToken = null;
        int slotCnt = 0;
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          boolean isLastSlot = (! slotIterator.hasNext());
          PwToken token = slot.getBaseToken();
          slotCnt++;
          PwDomain[] intervalArray =
            SlotNode.getStartEndIntervals( this, slot, previousToken, isLastSlot,
                                           alwaysReturnEnd);
          collectTimeScaleMetrics( intervalArray[0], intervalArray[1]);
          previousToken = token;
        }
        if (slotCnt > maxSlots) {
          maxSlots = slotCnt;
        }
      }
    }
    collectFreeTokenMetrics();

    computeTimeScaleMetrics();

  } // collectAndComputeTimeScaleMetrics

  private void collectTimeScaleMetrics( PwDomain startTimeIntervalDomain,
                                        PwDomain endTimeIntervalDomain) {
    if (startTimeIntervalDomain != null) {
//       System.err.println( "collectTimeScaleMetrics earliest " +
//                           startTimeIntervalDomain.getLowerBound() + " latest " +
//                           startTimeIntervalDomain.getUpperBound());
      int earliestTime = startTimeIntervalDomain.getLowerBoundInt();
      if ((earliestTime != PwDomain.MINUS_INFINITY_INT) &&
          (earliestTime < timeScaleStart)) {
        timeScaleStart = earliestTime;
      }
      int latestTime = startTimeIntervalDomain.getUpperBoundInt();
      if ((latestTime != PwDomain.PLUS_INFINITY_INT) &&
          (latestTime != PwDomain.MINUS_INFINITY_INT) &&
          (latestTime < timeScaleStart)) {
        timeScaleStart = latestTime;
      }
    }
    if (endTimeIntervalDomain != null) {
//       System.err.println( "collectTimeScaleMetrics latest " +
//                           endTimeIntervalDomain.getUpperBound() + " earliest " +
//                           endTimeIntervalDomain.getLowerBound());
      int latestTime = endTimeIntervalDomain.getUpperBoundInt();
      if ((latestTime != PwDomain.PLUS_INFINITY_INT) &&
          (latestTime > timeScaleEnd)) {
        timeScaleEnd = latestTime;
      }
      int earliestTime = endTimeIntervalDomain.getLowerBoundInt();
      if ((earliestTime != PwDomain.MINUS_INFINITY_INT) &&
          (earliestTime != PwDomain.PLUS_INFINITY_INT) &&
          (earliestTime > timeScaleEnd)) {
        timeScaleEnd = earliestTime;
      }
    }
  } // end collectTimeScaleMetrics

  private void computeTimeScaleMetrics() {
    endXLoc = startXLoc + (maxSlots * slotLabelMinLength * fontMetrics.charWidth( 'A'));
    timeScale = ((float) (endXLoc - startXLoc)) / ((float) (timeScaleEnd - timeScaleStart));
    System.err.println( "computeTimeScaleMetrics: startXLoc " + startXLoc +
                        " endXLoc " + endXLoc);
    System.err.println( "Temporal Extent View time scale: " + timeScaleStart + " " +
                        timeScaleEnd + " maxSlots " + maxSlots + " timeScale " + timeScale);
    int timeScaleRange = timeScaleEnd - timeScaleStart;
    timeDelta = 5;
    int maxIterationCnt = 25, iterationCnt = 0;
    while ((timeDelta * maxSlots) < timeScaleRange) {
      timeDelta *= 2;
//       System.err.println( "range " + timeScaleRange + " maxSlots " +
//                           maxSlots + " timeDelta " + timeDelta);
      iterationCnt++;
      if (iterationCnt > maxIterationCnt) {
        String message = "Range (" + timeScaleRange + ") execeeds functionality";
        JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                       "Temporal Extent View Exception",
                                       JOptionPane.ERROR_MESSAGE);
        System.err.println( message);
        System.exit( 1);
      }
    }
    tickTime = 0;
    xOrigin = startXLoc;
    int scaleStart = timeScaleStart;
    if ((scaleStart < 0) && ((scaleStart % timeDelta) == 0)) {
      scaleStart -= 1;
    }
    while (scaleStart < tickTime) {
      tickTime -= timeDelta;
      xOrigin += (int) timeScale * timeDelta; 
    }
  } // end computeTimeScaleMetrics

  private void collectFreeTokenMetrics() {
    List freeTokenList = partialPlan.getFreeTokenList();
    Iterator freeTokenItr = freeTokenList.iterator();
    while (freeTokenItr.hasNext()) {
      PwToken token = (PwToken) freeTokenItr.next();
      PwDomain startTimeIntervalDomain = token.getStartVariable().getDomain();
      PwDomain endTimeIntervalDomain = token.getEndVariable().getDomain();

      if (startTimeIntervalDomain != null) {
//         System.err.println( "collectFreeTokenMetrics earliest " +
//                             startTimeIntervalDomain.getLowerBound() + " latest " +
//                             startTimeIntervalDomain.getUpperBound());
        int earliestTime = startTimeIntervalDomain.getLowerBoundInt();
        if ((earliestTime != PwDomain.MINUS_INFINITY_INT) &&
            (earliestTime < timeScaleStart)) {
          timeScaleStart = earliestTime;
        }
        int latestTime = startTimeIntervalDomain.getUpperBoundInt();
        if ((latestTime != PwDomain.PLUS_INFINITY_INT) &&
            (latestTime < timeScaleStart)) {
          timeScaleStart = latestTime;
        }
      }
      if (endTimeIntervalDomain != null) {
//         System.err.println( "collectFreeTokenMetrics latest " +
//                             endTimeIntervalDomain.getUpperBound() + " earliest " +
//                             endTimeIntervalDomain.getLowerBound());
        int latestTime = endTimeIntervalDomain.getUpperBoundInt();
        if ((latestTime != PwDomain.PLUS_INFINITY_INT) &&
            (latestTime > timeScaleEnd)) {
          timeScaleEnd = latestTime;
        }
        int earliestTime = endTimeIntervalDomain.getLowerBoundInt();
        if ((earliestTime != PwDomain.MINUS_INFINITY_INT) &&
            (earliestTime > timeScaleEnd)) {
          timeScaleEnd = earliestTime;
        }
      }
    }
  } // end collectFreeTokenMetrics

  private void createTimeScale() {
    // int xLoc = startXLoc;
    int xLoc = (int) scaleTime( tickTime);
    int yLoc = startYLoc + ((maxCellRow + 1) * ViewConstants.TEMPORAL_NODE_CELL_HEIGHT) + 2;
    int scaleWidth = 2, tickHeight = ViewConstants.TIMELINE_VIEW_Y_INIT / 2;
    JGoStroke timeScaleRuler = new JGoStroke();
    timeScaleRuler.setPen( new JGoPen( JGoPen.SOLID, scaleWidth, ColorMap.getColor( "black")));
    timeScaleRuler.setDraggable( false);
    timeScaleRuler.setResizable( false);
    while (tickTime < timeScaleEnd) {
      timeScaleRuler.addPoint( xLoc, yLoc);
      timeScaleRuler.addPoint( xLoc, yLoc + tickHeight);
      timeScaleRuler.addPoint( xLoc, yLoc);
      addTickLabel( tickTime, xLoc, yLoc + 4);
      tickTime += timeDelta;
      xLoc = (int) scaleTime( tickTime);
    }
    timeScaleRuler.addPoint( xLoc, yLoc);
    timeScaleRuler.addPoint( xLoc, yLoc + tickHeight);
    addTickLabel( tickTime, xLoc, yLoc + 4);
    int maxHeight = yLoc + ViewConstants.TIMELINE_VIEW_Y_INIT;
    if (maxHeight > maxViewHeight) {
      maxViewHeight = maxHeight;
    }
    int maxWidth = xLoc + ViewConstants.TIMELINE_VIEW_X_INIT;
    if (maxWidth > maxViewWidth) {
      maxViewWidth = maxWidth;
    }
    jGoDocument.addObjectAtTail( timeScaleRuler);
  } // end createTimeScale

  private void addTickLabel( int tickTime, int x, int y) {
    String text = String.valueOf( tickTime);
    Point textLoc = new Point( x, y);
    JGoText textObject = new JGoText( textLoc, ViewConstants.TIMELINE_VIEW_FONT_SIZE,
                                      text, ViewConstants.TIMELINE_VIEW_FONT_NAME,
                                      ViewConstants.TIMELINE_VIEW_IS_FONT_BOLD,
                                      ViewConstants.TIMELINE_VIEW_IS_FONT_UNDERLINED,
                                      ViewConstants.TIMELINE_VIEW_IS_FONT_ITALIC,
                                      ViewConstants.TIMELINE_VIEW_TEXT_ALIGNMENT,
                                      ViewConstants.TIMELINE_VIEW_IS_TEXT_MULTILINE,
                                      ViewConstants.TIMELINE_VIEW_IS_TEXT_EDITABLE);
    textObject.setResizable( false);
    textObject.setEditable( false);
    textObject.setDraggable( false);
    textObject.setBkColor( ColorMap.getColor( "lightGray"));
    jGoDocument.addObjectAtTail( textObject);
  } // end addTickLabel


  private void createTemporalNodes() {
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    int objectCnt = 0;
    boolean alwaysReturnEnd = true, isFreeToken = false;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      List timelineList = object.getTimelineList();
      Iterator timelineIterator = timelineList.iterator();
      while (timelineIterator.hasNext()) {
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        List slotList = timeline.getSlotList();
        Iterator slotIterator = slotList.iterator();
        PwToken previousToken = null;
        boolean isFirstSlot = true;
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          boolean isLastSlot = (! slotIterator.hasNext());
          PwToken token = slot.getBaseToken();
          if ((token == null) && (isFirstSlot || isLastSlot)) {
            // discard leading and trailing empty slots
          } else {
            PwDomain[] intervalArray =
              SlotNode.getStartEndIntervals( this, slot, previousToken, isLastSlot,
                                             alwaysReturnEnd);
            PwDomain startTimeIntervalDomain = intervalArray[0];
            PwDomain endTimeIntervalDomain = intervalArray[1];
            String earliestDurationString =
              SlotNode.getShortestDuration( slot, startTimeIntervalDomain,
                                            endTimeIntervalDomain);
            String latestDurationString =
              SlotNode.getLongestDuration( slot, startTimeIntervalDomain,
                                           endTimeIntervalDomain);
            TemporalNode temporalNode = 
              new TemporalNode( token, slot, startTimeIntervalDomain, endTimeIntervalDomain,
                                earliestDurationString, latestDurationString, objectCnt,
                                isFreeToken, this); 
            tmpTemporalNodeList.add( temporalNode);
            jGoDocument.addObjectAtTail( temporalNode);
            previousToken = token;
          }
          isFirstSlot = false;
        }
      }
      objectCnt += 1;
    }

    createFreeTokenTemporalNodes();

    temporalNodeList = tmpTemporalNodeList;
  } // end createTemporalNodes


  private void createFreeTokenTemporalNodes() {
    List freeTokenList = partialPlan.getFreeTokenList();
    // System.err.println( "temporal extent view freeTokenList " + freeTokenList);
    Iterator freeTokenItr = freeTokenList.iterator();
    boolean isFreeToken = true; int objectCnt = -1;
    PwSlot slot = null;
    while (freeTokenItr.hasNext()) {
      PwToken token = (PwToken) freeTokenItr.next();
      PwDomain startTimeIntervalDomain = token.getStartVariable().getDomain();
      PwDomain endTimeIntervalDomain = token.getEndVariable().getDomain();
      String earliestDurationString =
        SlotNode.getShortestDuration( slot, startTimeIntervalDomain,
                                      endTimeIntervalDomain);
      String latestDurationString =
        SlotNode.getLongestDuration( slot, startTimeIntervalDomain,
                                     endTimeIntervalDomain);
      TemporalNode temporalNode = 
        new TemporalNode( token, slot, startTimeIntervalDomain, endTimeIntervalDomain,
                          earliestDurationString, latestDurationString, objectCnt,
                          isFreeToken, this); 
      tmpTemporalNodeList.add(temporalNode );
      // nodes are always in front of any links
      jGoDocument.addObjectAtTail( temporalNode);
    }
  } // end createFreeTokenTemporalNodes


  private void layoutTemporalNodes() {
    /*List extents = new ArrayList();
    Iterator temporalNodeIterator = temporalNodeList.iterator();
    while (temporalNodeIterator.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeIterator.next();
      extents.add( temporalNode);
      }*/
    List extents = new ArrayList(temporalNodeList);
    // do the layout -- compute cellRow for each node
    List results = Algorithms.allocateRows( scaleTime( timeScaleStart),
                                            scaleTime( timeScaleEnd), extents);
    //List results = Algorithms.betterAllocateRows(scaleTime(timeScaleStart),
    //                                            scaleTime(timeScaleEnd), extents);
    if (temporalNodeList.size() != results.size()) {
      String message = String.valueOf( temporalNodeList.size() - results.size()) +
        " nodes not successfully allocated";
      JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
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
    Iterator temporalNodeIterator = temporalNodeList.iterator();
    while (temporalNodeIterator.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeIterator.next();
      int maxHeight = (int) temporalNode.getLocation().getX() +
        (int) temporalNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_INIT;
      if (maxHeight > maxViewHeight) {
        maxViewHeight = maxHeight;
      }
      int maxWidth = (int) temporalNode.getLocation().getX() +
        ViewConstants.TIMELINE_VIEW_X_INIT;
      if (maxWidth > maxViewWidth) {
        maxViewWidth = maxWidth;
      }
    }
  } // end layoutTemporalNodes


  // are temporal extents in content spec, in terms of their tokens --
  // set them visible
  private void setNodesVisible() {
    // print content spec
    // System.err.println( "TemporalExtentView - contentSpec");
    // viewSet.printSpec();
    validTokenIds = viewSet.getValidTokenIds();
    displayedTokenIds = new ArrayList();
    Iterator temporalNodeIterator = temporalNodeList.iterator();
    while (temporalNodeIterator.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeIterator.next();
      Iterator markBridgeItr = temporalNode.getMarkAndBridgeList().iterator();
      if (temporalNode.getToken() != null) { // not an empty slot
        if (isTokenInContentSpec( temporalNode.getToken())) {
          temporalNode.setVisible( true);
          while (markBridgeItr.hasNext()) {
            ((JGoStroke) markBridgeItr.next()).setVisible( true);
          }
        } else {
          temporalNode.setVisible( false);
          while (markBridgeItr.hasNext()) {
            ((JGoStroke) markBridgeItr.next()).setVisible( false);
          }
        }
      }
      // overloaded tokens on slot - not displayed, put in list
      PwSlot slot = (PwSlot) temporalNode.getSlot();
      // check for free tokens (no slots!)
      if (slot != null) {
        List tokenList = slot.getTokenList();
        if (tokenList.size() > 1) {
          for (int i = 1, n = tokenList.size(); i < n; i++) {
            isTokenInContentSpec( (PwToken) tokenList.get( i));
          }
        }
      }
    }
    boolean showDialog = true;
    isContentSpecRendered( "Temporal Extent View", showDialog);
  } // end setNodesVisible


  private void iterateOverNodes() {
    int numTemporalNodes = temporalNodeList.size();
    System.err.println( "iterateOverNodes: numTemporalNodes " + numTemporalNodes);
    Iterator temporalIterator = temporalNodeList.iterator();
    while (temporalIterator.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalIterator.next();
      System.err.println( "name '" + temporalNode.getPredicateName() + "' location " +
                          temporalNode.getLocation());
    }
  } // end iterateOverNodes


  private void iterateOverJGoDocument() {
    JGoListPosition position = jGoDocument.getFirstObjectPos();
    int cnt = 0;
    while (position != null) {
      JGoObject object = jGoDocument.getObjectAtPos( position);
      position = jGoDocument.getNextObjectPosAtTop( position);
      System.err.println( "iterateOverJGoDoc: position " + position +
                          " className " + object.getClass().getName());
      if (object instanceof TemporalNode) {
        TemporalNode temporalNode = (TemporalNode) object;

      }
      cnt += 1;
//       if (cnt > 100) {
//         break;
//       }
    }
    System.err.println( "iterateOverJGoDoc: cnt " + cnt);
  } // end iterateOverJGoDocument



} // end class TemporalExtentView
 
