// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TemporalExtentView.java,v 1.4 2003-09-30 19:18:56 taylor Exp $
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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
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
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
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

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private ExtentView jGoExtentView;
  private JGoView jGoRulerView;
  private RulerPanel rulerPanel;
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
  private JGoStroke timeScaleMark;
  private static Point docCoords;


  /**
   * <code>TemporalExtentView</code> - constructor 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TemporalExtentView( ViewableObject partialPlan, ViewSet viewSet) {
    super( (PwPartialPlan)partialPlan, (PartialPlanViewSet)viewSet);
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (PartialPlanViewSet) viewSet;

    startXLoc = ViewConstants.TIMELINE_VIEW_X_INIT * 2;
    startYLoc = ViewConstants.TIMELINE_VIEW_Y_INIT;
    maxCellRow = 0;
    timeScaleMark = null;
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;

    jGoExtentView = new ExtentView();
    // jGoSelection = new JGoSelection( jGoExtentView);
    jGoExtentView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoExtentView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());

    add( jGoExtentView, BorderLayout.NORTH);
    jGoExtentView.validate();
    jGoExtentView.setVisible( true);

    rulerPanel = new RulerPanel();
    rulerPanel.setLayout( new BoxLayout( rulerPanel, BoxLayout.Y_AXIS));


    // jGoRulerView = new RulerView();
    jGoRulerView = new JGoView();
    jGoRulerView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoRulerView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    rulerPanel.add( jGoRulerView, BorderLayout.NORTH);
    jGoRulerView.validate();
    jGoRulerView.setVisible( true);
    this.setVisible( true);

    add( rulerPanel, BorderLayout.SOUTH);

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
   *    "Cannot measure text until a JGoExtentView exists and is part of a visible window".
   *     int extentScrollExtent = jGoExtentView.getHorizontalScrollBar().getSize().getWidth();
   *    called by componentShown method on the JFrame
   *    JGoExtentView.setVisible( true) must be completed -- use runInit in constructor
   */
  public void init() {
    jGoExtentView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
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
    // jGoExtentView.setFont( font);
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();

    collectAndComputeTimeScaleMetrics();
    createTimeScale();
    boolean isRedraw = false;
    renderTemporalExtent( isRedraw);

    expandViewFrame( this.getClass().getName(),
                     (int) Math.max( jGoExtentView.getDocumentSize().getWidth(),
                                     jGoRulerView.getDocumentSize().getWidth()),
                     (int) (jGoExtentView.getDocumentSize().getHeight() +
                            jGoRulerView.getDocumentSize().getHeight()));

    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    jGoExtentView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
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

  /**
   * <code>RedrawViewThread</code> - execute redraw in a new thread
   *
   */
  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      boolean isRedraw = true;
      renderTemporalExtent( isRedraw);
    } //end run

  } // end class RedrawViewThread

  private void renderTemporalExtent( boolean isRedraw) {
    jGoExtentView.getDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    temporalNodeList = null;
    tmpTemporalNodeList = new ArrayList();

    createTemporalNodes();

    boolean showDialog = true;
    isContentSpecRendered( PlanWorks.TEMPORAL_EXTENT_VIEW, showDialog);

    layoutTemporalNodes();
    // equalize view widths so scrollbars are equal
    equalizeViewWidths( isRedraw);

  } // end createTemporalExtentView

  /**
   * <code>getJGoDocument</code> - the temporal extent view document
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoExtentView.getDocument();
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
   * <code>scaleTime</code> - convert time to view x location
   *
   * @param time - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public int scaleTime( int time) {
    return xOrigin + (int) (timeScale * time);
  }

  /**
   * <code>scaleXLoc</code> - convert from view x location to time
   *
   * @param xLoc - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public int  scaleXLoc( int xLoc) {
    return (int) ((xLoc - xOrigin) / timeScale);
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
        PwSlot previousSlot = null;
        int slotCnt = 0;
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          boolean isLastSlot = (! slotIterator.hasNext());
          PwToken token = slot.getBaseToken();
          slotCnt++;
          PwDomain[] intervalArray =
            NodeGenerics.getStartEndIntervals( this, slot, previousSlot, isLastSlot,
                                               alwaysReturnEnd);
          collectTimeScaleMetrics( intervalArray[0], intervalArray[1]);
          previousSlot = slot;
        }
        if (slotCnt > maxSlots) {
          maxSlots = slotCnt;
        }
      }
    }
    maxSlots = Math.max( maxSlots, ViewConstants.TEMPORAL_MIN_MAX_SLOTS);

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
    endXLoc = Math.max( startXLoc +
                        (maxSlots * slotLabelMinLength * fontMetrics.charWidth( 'A')),
                        ViewConstants.TEMPORAL_MIN_END_X_LOC);
    timeScale = ((float) (endXLoc - startXLoc)) / ((float) (timeScaleEnd - timeScaleStart));
    //System.err.println( "computeTimeScaleMetrics: startXLoc " + startXLoc +
    //                    " endXLoc " + endXLoc);
    //System.err.println( "Temporal Extent View time scale: " + timeScaleStart + " " +
    //                   timeScaleEnd + " maxSlots " + maxSlots + " timeScale " + timeScale);
    int timeScaleRange = timeScaleEnd - timeScaleStart;
    timeDelta = 1;
    int maxIterationCnt = 25, iterationCnt = 0;
    while ((timeDelta * maxSlots) < timeScaleRange) {
      if (timeDelta == 1) {
        timeDelta = 2;
      } else if (timeDelta == 2) {
        timeDelta = 5;
      } else {
        timeDelta *= 2;
      }
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
      xOrigin += (int) (timeScale * timeDelta);
//       System.err.println( "scaleStart " + scaleStart + " tickTime " + tickTime +
//                           " xOrigin " + xOrigin);
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
    int xLoc = (int) scaleTime( tickTime);
    // System.err.println( "createTimeScale: xLoc " + xLoc);
    int yRuler = startYLoc;
    int yLabelUpper = yRuler, yLabelLower = yRuler;
    if ((timeScaleEnd - timeScaleStart) > ViewConstants.TEMPORAL_LARGE_LABEL_RANGE) {
      yLabelLower = yRuler + ViewConstants.TIMELINE_VIEW_Y_INIT;
    }
    int yLabel = yLabelUpper;
    int scaleWidth = 2, tickHeight = ViewConstants.TIMELINE_VIEW_Y_INIT / 2;
    JGoStroke timeScaleRuler = new JGoStroke();
    timeScaleRuler.setPen( new JGoPen( JGoPen.SOLID, scaleWidth, ColorMap.getColor( "black")));
    timeScaleRuler.setDraggable( false);
    timeScaleRuler.setResizable( false);
    boolean isUpperLabel = true;
    while (tickTime < timeScaleEnd) {
      timeScaleRuler.addPoint( xLoc, yRuler);
      timeScaleRuler.addPoint( xLoc, yRuler + tickHeight);
      timeScaleRuler.addPoint( xLoc, yRuler);
      addTickLabel( tickTime, xLoc, yLabel + 4);
      tickTime += timeDelta;
      xLoc = (int) scaleTime( tickTime);
      isUpperLabel = (! isUpperLabel);
      if (isUpperLabel) {
        yLabel = yLabelUpper;
      } else {
        yLabel = yLabelLower;
      }
    }
    timeScaleRuler.addPoint( xLoc, yRuler);
    timeScaleRuler.addPoint( xLoc, yRuler + tickHeight);
    addTickLabel( tickTime, xLoc, yLabel + 4);

    jGoRulerView.getDocument().addObjectAtTail( timeScaleRuler);
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
    textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoRulerView.getDocument().addObjectAtTail( textObject);
  } // end addTickLabel


  private void createTemporalNodes() {
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    int timelineCnt = 0;
    boolean alwaysReturnEnd = true, isFreeToken = false;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      List timelineList = object.getTimelineList();
      Iterator timelineIterator = timelineList.iterator();
      while (timelineIterator.hasNext()) {
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        Color timelineColor =
          ((PartialPlanViewSet) viewSet).getColorStream().getColor( timelineCnt);
        List slotList = timeline.getSlotList();
        Iterator slotIterator = slotList.iterator();
        PwSlot previousSlot = null;
        boolean isFirstSlot = true;
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          // overloaded tokens on slot - not displayed, put in displayedTokenIds
          List tokenList = slot.getTokenList();
          for (int i = 1, n = tokenList.size(); i < n; i++) {
            isTokenInContentSpec( (PwToken) tokenList.get( i));
          }
          boolean isLastSlot = (! slotIterator.hasNext());
          PwToken token = slot.getBaseToken();
          if ((token == null) && (isFirstSlot || isLastSlot)) {
            // discard leading and trailing empty slots (planworks/test/data/emptySlots)
          } else {
            // check for embedded empty slots - always show them, unless free standing
            if ((token == null) ||
                (token != null) && isTokenInContentSpec( token)) {
              PwDomain[] intervalArray =
                NodeGenerics.getStartEndIntervals( this, slot, previousSlot, isLastSlot,
                                                   alwaysReturnEnd);
              PwDomain startTimeIntervalDomain = intervalArray[0];
              PwDomain endTimeIntervalDomain = intervalArray[1];
              if ((startTimeIntervalDomain != null) && (endTimeIntervalDomain != null)) {
                String earliestDurationString =
                  NodeGenerics.getShortestDuration( slot, startTimeIntervalDomain,
                                                    endTimeIntervalDomain);
                String latestDurationString =
                  NodeGenerics.getLongestDuration( slot, startTimeIntervalDomain,
                                                   endTimeIntervalDomain);
                TemporalNode temporalNode = 
                  new TemporalNode( token, slot, startTimeIntervalDomain, endTimeIntervalDomain,
                                    earliestDurationString, latestDurationString,
                                    timelineColor, isFreeToken, this); 
                tmpTemporalNodeList.add( temporalNode);
                jGoExtentView.getDocument().addObjectAtTail( temporalNode);
                previousSlot = slot;
              }
            }
          }
          isFirstSlot = false;
        }
        timelineCnt++;
      }
    }

    createFreeTokenTemporalNodes();

    temporalNodeList = tmpTemporalNodeList;
  } // end createTemporalNodes


  private void createFreeTokenTemporalNodes() {
    List freeTokenList = partialPlan.getFreeTokenList();
    // System.err.println( "temporal extent view freeTokenList " + freeTokenList);
    Iterator freeTokenItr = freeTokenList.iterator();
    boolean isFreeToken = true;
    PwSlot slot = null;
    Color backgroundColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
    while (freeTokenItr.hasNext()) {
      PwToken token = (PwToken) freeTokenItr.next();
      if (isTokenInContentSpec( token)) {
        PwDomain startTimeIntervalDomain = token.getStartVariable().getDomain();
        PwDomain endTimeIntervalDomain = token.getEndVariable().getDomain();
        String earliestDurationString =
          NodeGenerics.getShortestDuration( slot, startTimeIntervalDomain,
                                            endTimeIntervalDomain);
        String latestDurationString =
          NodeGenerics.getLongestDuration( slot, startTimeIntervalDomain,
                                           endTimeIntervalDomain);
        TemporalNode temporalNode = 
          new TemporalNode( token, slot, startTimeIntervalDomain, endTimeIntervalDomain,
                            earliestDurationString, latestDurationString,
                            backgroundColor, isFreeToken, this); 
        tmpTemporalNodeList.add(temporalNode );
        // nodes are always in front of any links
        jGoExtentView.getDocument().addObjectAtTail( temporalNode);
      }
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
  } // end layoutTemporalNodes


  // are temporal extents in content spec, in terms of their tokens --
  // set them visible
//   private void setNodesVisible() {
//     // print content spec
//     // System.err.println( "TemporalExtentView - contentSpec");
//     // viewSet.printSpec();
//     validTokenIds = viewSet.getValidIds();
//     displayedTokenIds = new ArrayList();
//     Iterator temporalNodeIterator = temporalNodeList.iterator();
//     while (temporalNodeIterator.hasNext()) {
//       TemporalNode temporalNode = (TemporalNode) temporalNodeIterator.next();
//       Iterator markBridgeItr = temporalNode.getMarkAndBridgeList().iterator();
//       if (temporalNode.getToken() != null) { // not an empty slot
//         if (isTokenInContentSpec( temporalNode.getToken())) {
//           temporalNode.setVisible( true);
//           while (markBridgeItr.hasNext()) {
//             ((JGoStroke) markBridgeItr.next()).setVisible( true);
//           }
//         } else {
//           temporalNode.setVisible( false);
//           while (markBridgeItr.hasNext()) {
//             ((JGoStroke) markBridgeItr.next()).setVisible( false);
//           }
//         }
//       }
//       // overloaded tokens on slot - not displayed, put in list
//       PwSlot slot = (PwSlot) temporalNode.getSlot();
//       // check for free tokens (no slots!)
//       if (slot != null) {
//         List tokenList = slot.getTokenList();
//         if (tokenList.size() > 1) {
//           for (int i = 1, n = tokenList.size(); i < n; i++) {
//             isTokenInContentSpec( (PwToken) tokenList.get( i));
//           }
//         }
//       }
//     }
//     boolean showDialog = true;
//     isContentSpecRendered( PlanWorks.TEMPORAL_EXTENT_VIEW, showDialog);
//   } // end setNodesVisible


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
//         System.err.println( "jGoExtentView " +
//                             jGoExtentView.getHorizontalScrollBar().getValue());
//         System.err.println( "jGoRulerView " +
//                             jGoRulerView.getHorizontalScrollBar().getValue());
        int newPostion = source.getValue();
        if (newPostion != jGoExtentView.getHorizontalScrollBar().getValue()) {
          jGoExtentView.getHorizontalScrollBar().setValue( newPostion);
        } else if (newPostion != jGoRulerView.getHorizontalScrollBar().getValue()) {
          jGoRulerView.getHorizontalScrollBar().setValue( newPostion);
        }
        // }
    } // end adjustmentValueChanged 

  } // end class ScrollBarListener 


  /**
   * <code>ExtentView</code> - subclass doBackgroundClick to handle drawing
   *                               vertical time marks on view
   *
   */
  class ExtentView extends JGoView {

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
    JPopupMenu mouseRightPopup = new JPopupMenu();
    JMenuItem timeMarkItem = new JMenuItem( "Set Time Scale Line");
    createTimeMarkItem( timeMarkItem);
    mouseRightPopup.add( timeMarkItem);

    JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
    createActiveTokenItem( activeTokenItem);
    mouseRightPopup.add( activeTokenItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu


  private void createActiveTokenItem( JMenuItem activeTokenItem) {
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) TemporalExtentView.this.getViewSet()).getActiveToken();
          boolean isTokenFound = false;
          if (activeToken != null) {
            Iterator temporalNodeListItr = temporalNodeList.iterator();
            foundMatch:
            while (temporalNodeListItr.hasNext()) {
              TemporalNode temporalNode = (TemporalNode) temporalNodeListItr.next();
              if (temporalNode.getToken() != null) {
                // check overloaded tokens, since only base tokens are rendered
                Iterator tokenListItr = temporalNode.getSlot().getTokenList().iterator();
                while (tokenListItr.hasNext()) {
                  if (((PwToken) tokenListItr.next()).getId().equals( activeToken.getId())) {
                    System.err.println( "TemporalExtentView snapToActiveToken: " +
                                        activeToken.getPredicate().getName());
                    NodeGenerics.focusViewOnNode( temporalNode, jGoExtentView);
                    isTokenFound = true;
                    break foundMatch;
                  }
                }
              }
            }
            if (isTokenFound) {
                // only base tokens are rendered
//               NodeGenerics.selectSecondaryNodes
//                 ( NodeGenerics.mapTokensToTokenNodes
//                   (TemporalExtentView.this.getViewSet().getSecondaryTokens(),
//                    temporalNodeList),
//                   jGoExtentView);
            } else {
              String message = "active token '" + activeToken.getPredicate().getName() +
                "' not found in TemporalExtentView";
              JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                             "Active Token Not Found",
                                             JOptionPane.ERROR_MESSAGE);
              System.err.println( message);
              System.exit( 1);
            }
          }
        }
      });
  } // end createActiveTokenItem


  private void createTimeMarkItem( JMenuItem timeMarkItem) {
    timeMarkItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          int xLoc = (int) TemporalExtentView.docCoords.getX();
          // System.err.println( "doMouseClick: xLoc " + xLoc + " time " + scaleXLoc( xLoc));
          if (timeScaleMark != null) {
            jGoExtentView.getDocument().removeObject( timeScaleMark);
            // jGoExtentView.validate();
          } 
          timeScaleMark = new TimeScaleMark( xLoc);
          timeScaleMark.addPoint( xLoc, startYLoc);
          timeScaleMark.addPoint( xLoc, startYLoc +
                                  ((maxCellRow + 1) *
                                   ViewConstants.TEMPORAL_NODE_CELL_HEIGHT) + 2);
          jGoExtentView.getDocument().addObjectAtTail( timeScaleMark);
        }
      });
  } // end createTimeMarkItem


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
     */
    public TimeScaleMark( int xLoc) {
      super();
      this.xLoc = xLoc;
      setDraggable( false);
      setResizable( false);
      setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "red")));
    }

    public String getToolTipText() {
      return String.valueOf( scaleXLoc( xLoc) + 1);
    }

  } // end class TimeScaleMark


} // end class TemporalExtentView
 



