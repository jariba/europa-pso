// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimeScaleView.java,v 1.2 2004-02-03 22:44:20 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 02Feb04
//

package gov.nasa.arc.planworks.viz.partialPlan;

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
import com.nwoods.jgo.JGoText;
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
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>TimeScaleView</code> - a time scale with time ticks
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimeScaleView extends JGoView  {

  private int startXLoc;
  private int startYLoc;
  private PwPartialPlan partialPlan;
  private PartialPlanView partialPlanView;
  private int maxSlots;
  private int xOrigin;
  private int endXLoc;
  private int timeScaleStart;
  private int timeScaleEnd;
  private int slotLabelMinLength;
  private float timeScale;
  private int timeDelta;
  private int tickTime;

  /**
   * <code>TimeScaleView</code> - constructor 
   *
   * @param startXLoc - <code>int</code> - 
   * @param startYLoc - <code>int</code> - 
   * @param partialPlan - <code>PwPartialPlan</code> - 
   */
  public TimeScaleView( int startXLoc, int startYLoc, PwPartialPlan partialPlan,
                        PartialPlanView partialPlanView) {
    super();
    this.startXLoc = startXLoc;
    this.startYLoc = startYLoc;
    this.partialPlan = partialPlan;
    this.partialPlanView = partialPlanView;
    maxSlots = 0;
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
  } // end constructor

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
   * <code>getTimeScale</code>
   *
   * @return - <code>float</code> - 
   */
  public float getTimeScale() {
    return timeScale;
  }

  /**
   * <code>collectAndComputeTimeScaleMetrics</code>
   *
   * @param doFreeTokens - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   * @return - <code>int</code> - 
   */
  public int collectAndComputeTimeScaleMetrics( boolean doFreeTokens,
                                                PartialPlanView partialPlanView) {
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
//           PwDomain[] intervalArray =
//             NodeGenerics.getStartEndIntervals( slot, previousSlot, isLastSlot,
//                                                alwaysReturnEnd);
//           collectTimeScaleMetrics( intervalArray[0], intervalArray[1], token);
          collectTimeScaleMetrics(slot.getStartTime(), slot.getEndTime(), token);
          previousSlot = slot;
        }
        if (slotCnt > maxSlots) {
          maxSlots = slotCnt;
        }
      }
    }
    maxSlots = Math.max( maxSlots, ViewConstants.TEMPORAL_MIN_MAX_SLOTS);

    if (doFreeTokens) {
      collectFreeTokenMetrics();
    }
    return computeTimeScaleMetrics( partialPlanView);
  } // end collectAndComputeTimeScaleMetrics


  private void collectTimeScaleMetrics( PwDomain startTimeIntervalDomain,
                                        PwDomain endTimeIntervalDomain, PwToken token) {
    int leftMarginTime = 0;
    if (startTimeIntervalDomain != null) {
//       System.err.println( "collectTimeScaleMetrics earliest " +
//                           startTimeIntervalDomain.getLowerBound() + " latest " +
//                           startTimeIntervalDomain.getUpperBound());
      int earliestTime = startTimeIntervalDomain.getLowerBoundInt();
      leftMarginTime = earliestTime;
      if ((earliestTime != DbConstants.MINUS_INFINITY_INT) &&
          (earliestTime < timeScaleStart)) {
          timeScaleStart = earliestTime;
      }
      int latestTime = startTimeIntervalDomain.getUpperBoundInt();
      if (leftMarginTime == DbConstants.MINUS_INFINITY_INT) {
        leftMarginTime = latestTime;
      }
      if ((latestTime != DbConstants.PLUS_INFINITY_INT) &&
          (latestTime != DbConstants.MINUS_INFINITY_INT) &&
          (latestTime < timeScaleStart)) {
        timeScaleStart = latestTime;
      }
    }
    if (endTimeIntervalDomain != null) {
//       System.err.println( "collectTimeScaleMetrics latest " +
//                           endTimeIntervalDomain.getUpperBound() + " earliest " +
//                           endTimeIntervalDomain.getLowerBound());
      int latestTime = endTimeIntervalDomain.getUpperBoundInt();
      if (latestTime != DbConstants.PLUS_INFINITY_INT) {
        if (latestTime > timeScaleEnd) {
          timeScaleEnd = latestTime;
        }
      }
      int earliestTime = endTimeIntervalDomain.getLowerBoundInt();
      if ((earliestTime != DbConstants.MINUS_INFINITY_INT) &&
          (earliestTime != DbConstants.PLUS_INFINITY_INT) &&
          (earliestTime > timeScaleEnd)) {
        timeScaleEnd = earliestTime;
      }
    }
  } // end collectTimeScaleMetrics

  private void collectFreeTokenMetrics() {
    List freeTokenList = partialPlan.getFreeTokenList();
    Iterator freeTokenItr = freeTokenList.iterator();
    while (freeTokenItr.hasNext()) {
      PwToken token = (PwToken) freeTokenItr.next();
      PwDomain startTimeIntervalDomain = token.getStartVariable().getDomain();
      PwDomain endTimeIntervalDomain = token.getEndVariable().getDomain();

      int leftMarginTime = 0;
      if (startTimeIntervalDomain != null) {
//         System.err.println( "collectFreeTokenMetrics earliest " +
//                             startTimeIntervalDomain.getLowerBound() + " latest " +
//                             startTimeIntervalDomain.getUpperBound());
        int earliestTime = startTimeIntervalDomain.getLowerBoundInt();
        leftMarginTime = earliestTime;
        if ((earliestTime != DbConstants.MINUS_INFINITY_INT) &&
            (earliestTime < timeScaleStart)) {
            timeScaleStart = earliestTime;
        }
        int latestTime = startTimeIntervalDomain.getUpperBoundInt();
        if (leftMarginTime == DbConstants.MINUS_INFINITY_INT) {
          leftMarginTime = latestTime;
        }
        if ((latestTime != DbConstants.PLUS_INFINITY_INT) &&
            (latestTime < timeScaleStart)) {
          timeScaleStart = latestTime;
        }
      }
      if (endTimeIntervalDomain != null) {
//         System.err.println( "collectFreeTokenMetrics latest " +
//                             endTimeIntervalDomain.getUpperBound() + " earliest " +
//                             endTimeIntervalDomain.getLowerBound());
        int latestTime = endTimeIntervalDomain.getUpperBoundInt();
        if (latestTime != DbConstants.PLUS_INFINITY_INT) {
          if (latestTime > timeScaleEnd) {
            timeScaleEnd = latestTime;
          }
        }
        int earliestTime = endTimeIntervalDomain.getLowerBoundInt();
        if ((earliestTime != DbConstants.MINUS_INFINITY_INT) &&
            (earliestTime > timeScaleEnd)) {
          timeScaleEnd = earliestTime;
        }
      }
    }
  } // end collectFreeTokenMetrics

  private int computeTimeScaleMetrics( PartialPlanView partialPlanView) {
    endXLoc = Math.max( startXLoc +
                        (maxSlots * slotLabelMinLength *
                         partialPlanView.getFontMetrics().charWidth( 'A')),
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
        String dialogTitle = null;
        if (partialPlanView instanceof TemporalExtentView) {
          dialogTitle = PlanWorks.TEMPORAL_EXTENT_VIEW;
        } else if (partialPlanView instanceof ResourceProfileView) {
          dialogTitle = PlanWorks.RESOURCE_PROFILE_VIEW;
        } else {
          System.err.println( "TimeScaleView.computeTimeScaleMetrics: view " +
                              partialPlanView + " not handled");
          System.exit( 1);
        }
        String message = "Range (" + timeScaleRange + ") execeeds functionality";
        JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                       dialogTitle + " Exception",
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
    // System.err.println( " xOrigin " + xOrigin);
    return xOrigin;
  } // end computeTimeScaleMetrics


  /**
   * <code>createTimeScale</code>
   *
   */
  public void createTimeScale() {
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

    getDocument().addObjectAtTail( timeScaleRuler);
  } // end createTimeScale

  private void addTickLabel( int tickTime, int x, int y) {
    String text = String.valueOf( tickTime);
    Point textLoc = new Point( x, y);
    JGoText textObject = new JGoText( textLoc, text);
    textObject.setResizable( false);
    textObject.setEditable( false);
    textObject.setDraggable( false);
    textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
    getDocument().addObjectAtTail( textObject);
  } // end addTickLabel


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
   * <code>scaleXLoc</code>
   *
   * @param xLoc - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public int  scaleXLoc( int xLoc) {
    return (int) ((xLoc - xOrigin) / timeScale);
  }


} // end class TimeScaleView
