// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimeScaleView.java,v 1.9 2004-03-10 02:21:21 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 02Feb04
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;


import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;

/**
 * <code>TimeScaleView</code> - a time scale with time ticks
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimeScaleView extends JGoView  {

  private static final int TICK_Y_INCREMENT = 4;
  private static final int TIME_DELTA_INTERATION_CNT = 25;
  private static final int TIME_DELTA_INCREMENT_LARGE = 5;
  private static final int TIME_SCALE_END_DEFAULT = 100;

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
  private double timeScale;
  private int timeDelta;
  private int tickTime;

  /**
   * <code>TimeScaleView</code> - constructor 
   *
   * @param startXLoc - <code>int</code> - 
   * @param startYLoc - <code>int</code> - 
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public TimeScaleView( final int startXLoc, final int startYLoc,
                        final PwPartialPlan partialPlan,
                        final PartialPlanView partialPlanView) {
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
  public final int getTimeScaleStart() {
    return timeScaleStart;
  }

  /**
   * <code>getTimeScaleEnd</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getTimeScaleEnd() {
    return timeScaleEnd;
  }

  /**
   * <code>getTimeScale</code>
   *
   * @return - <code>double</code> - 
   */
  public final double getTimeScale() {
    return timeScale;
  }

  /**
   * <code>collectAndComputeTimeScaleMetrics</code>
   *
   * @param doFreeTokens - <code>boolean</code> - 
   * @param partPlanView - <code>PartialPlanView</code> - 
   * @return - <code>int</code> - 
   */
  public final int collectAndComputeTimeScaleMetrics( final boolean doFreeTokens,
                                                      final PartialPlanView partPlanView) {
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      if (object instanceof PwTimeline) {
        PwTimeline timeline = (PwTimeline) object;
        List slotList = timeline.getSlotList();
        Iterator slotIterator = slotList.iterator();
        PwSlot previousSlot = null;
        int slotCnt = 0;
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          boolean isLastSlot = (! slotIterator.hasNext());
          PwToken token = slot.getBaseToken();
          slotCnt++;
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
    if (timeScaleStart == timeScaleEnd) {
      timeScaleEnd = timeScaleStart + TIME_SCALE_END_DEFAULT;
    }
    return computeTimeScaleMetrics( partPlanView);
  } // end collectAndComputeTimeScaleMetrics


  private void collectTimeScaleMetrics( final PwDomain startTimeIntervalDomain,
                                        final PwDomain endTimeIntervalDomain,
                                        final PwToken token) {
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
    List tokenList = partialPlan.getTokenList();
    Iterator tokenIterator = tokenList.iterator();
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      if (token.isFree()) {
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
    }
  } // end collectFreeTokenMetrics

  private int computeTimeScaleMetrics( final PartialPlanView partPlanView) {
    endXLoc = Math.max( startXLoc +
                        (maxSlots * slotLabelMinLength *
                         partialPlanView.getFontMetrics().charWidth( 'A')),
                        ViewConstants.TEMPORAL_MIN_END_X_LOC);
    timeScale = ((double) (endXLoc - startXLoc)) / ((double) (timeScaleEnd - timeScaleStart));
//     System.err.println( "computeTimeScaleMetrics: startXLoc " + startXLoc +
//                        " endXLoc " + endXLoc);
//     System.err.println( "Temporal Extent View time scale: " + timeScaleStart + " " +
//                        timeScaleEnd + " maxSlots " + maxSlots + " timeScale " + timeScale);
    int timeScaleRange = timeScaleEnd - timeScaleStart;
    timeDelta = 1;
    int maxIterationCnt = TIME_DELTA_INTERATION_CNT, iterationCnt = 0;
    while ((timeDelta * maxSlots) < timeScaleRange) {
      if (timeDelta == 1) {
        timeDelta = 2;
      } else if (timeDelta == 2) {
        timeDelta = TIME_DELTA_INCREMENT_LARGE;
      } else {
        timeDelta *= 2;
      }
//       System.err.println( "range " + timeScaleRange + " maxSlots " +
//                           maxSlots + " timeDelta " + timeDelta);
      iterationCnt++;
      if (iterationCnt > maxIterationCnt) {
        String dialogTitle = null;
        if (partPlanView instanceof TemporalExtentView) {
          dialogTitle = PlanWorks.TEMPORAL_EXTENT_VIEW;
        } else if (partPlanView instanceof ResourceProfileView) {
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
//     System.err.println( " xOrigin " + xOrigin);
    return xOrigin;
  } // end computeTimeScaleMetrics


  /**
   * <code>createTimeScale</code>
   *
   */
  public final void createTimeScale() {
    int xLoc = (int) scaleTime( (double) tickTime);
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
    timeScaleRuler.setSelectable( false);

    boolean isUpperLabel = true;
    while (tickTime < timeScaleEnd) {
      timeScaleRuler.addPoint( xLoc, yRuler);
      timeScaleRuler.addPoint( xLoc, yRuler + tickHeight);
      timeScaleRuler.addPoint( xLoc, yRuler);
      addTickLabel( tickTime, xLoc, yLabel + TICK_Y_INCREMENT);
      tickTime += timeDelta;
      xLoc = (int) scaleTime( (double) tickTime);
      isUpperLabel = (! isUpperLabel);
      if (isUpperLabel) {
        yLabel = yLabelUpper;
      } else {
        yLabel = yLabelLower;
      }
    }
    timeScaleRuler.addPoint( xLoc, yRuler);
    timeScaleRuler.addPoint( xLoc, yRuler + tickHeight);
    addTickLabel( tickTime, xLoc, yLabel + TICK_Y_INCREMENT);

    getDocument().addObjectAtTail( timeScaleRuler);
  } // end createTimeScale

  private void addTickLabel( final int time, final int x, final int y) {
    String text = String.valueOf( time);
    Point textLoc = new Point( x, y);
    JGoText textObject = new JGoText( textLoc, text);
    textObject.setResizable( false);
    textObject.setEditable( false);
    textObject.setDraggable( false);
    textObject.setSelectable( false);
    textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
    getDocument().addObjectAtTail( textObject);
  } // end addTickLabel


  /**
   * <code>scaleTime</code>
   *
   * @param time - <code>double</code> - 
   * @return - <code>int</code> - 
   */
  public final int scaleTime( final double time) {
    return xOrigin + (int) (timeScale * time);
  }

  /**
   * <code>scaleXLoc</code>
   *
   * @param xLoc - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public final int  scaleXLoc( final int xLoc) {
    return (int) Math.round( (xLoc - xOrigin) / timeScale);
  }


} // end class TimeScaleView
