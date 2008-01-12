// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TemporalNode.java,v 1.23 2004-06-10 01:36:06 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 21July03
//

package gov.nasa.arc.planworks.viz.partialPlan.temporalExtent;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.Extent;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>TemporalNode</code> - JGo widget to render a token's temporal extents
 *                             with a label consisting of the slot's predicate name.
 *                             create TemporalNode objects, then determine
 *                             layout, then call configure to render them.
 *
 *                             ScaleTimeNoZoom is used since TemporalExtentView is
 *                             scaled by "Zoom View"
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TemporalNode extends BasicNode implements Extent, OverviewToolTip {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  public static final int EARLIEST_START_TIME_MARK = 1;
  public static final int LATEST_START_TIME_MARK = 2;
  public static final int EARLIEST_END_TIME_MARK = 3;
  public static final int LATEST_END_TIME_MARK = 4;
  public static final int MINUS_INFINITY_TIME_MARK = 5;
  public static final int PLUS_INFINITY_TIME_MARK = 6;

  private PwToken token;
  private PwSlot slot;
  private int earliestStartTime;
  private int latestStartTime;
  private int earliestEndTime;
  private int latestEndTime;
  private int earliestDurationTime;
  private int latestDurationTime;
  private Color backgroundColor;
  private boolean isFreeToken;
  private TemporalExtentView temporalExtentView;
  private boolean isEarliestStartMinusInf;
  private boolean isEarliestStartPlusInf;
  private boolean isLatestStartPlusInf;
  private boolean isEarliestEndMinusInf;
  private boolean isEarliestEndPlusInf;
  private boolean isLatestEndPlusInf;

  private String predicateName;
  private String nodeLabel;
  private int nodeLabelWidth;
  private List markAndBridgeList; // elements JGoPolygon & JGoStroke
  private int cellRow; // for layout algorithm
  private String [] labelLines;
  private int temporalDisplayMode;
  private boolean isShowLabels;
  private String tokenId;
  private int temporalNodeXDelta;
  private int temporalNodeYDelta;


  /**
   * <code>TemporalNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param startTimeIntervalDomain - <code>PwDomain</code> - 
   * @param endTimeIntervalDomain - <code>PwDomain</code> - 
   * @param earliestDurationString - <code>String</code> - 
   * @param latestDurationString - <code>String</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isShowLabels - <code>boolean</code> - 
   * @param temporalDisplayMode - <code>int</code> - 
   * @param temporalExtentView - <code>TemporalExtentView</code> - 
   */
  public TemporalNode( PwToken token, PwSlot slot, PwDomain startTimeIntervalDomain,
                       PwDomain endTimeIntervalDomain, String earliestDurationString,
                       String latestDurationString, Color backgroundColor,
                       boolean isFreeToken, boolean isShowLabels, int temporalDisplayMode,
                       TemporalExtentView temporalExtentView) {
    super();
    this.token = token;
    this.slot = slot;
    this.temporalDisplayMode = temporalDisplayMode;
    earliestStartTime = startTimeIntervalDomain.getLowerBoundInt();
    latestStartTime = startTimeIntervalDomain.getUpperBoundInt();
    earliestEndTime = endTimeIntervalDomain.getLowerBoundInt();
    latestEndTime = endTimeIntervalDomain.getUpperBoundInt();
    this.temporalExtentView = temporalExtentView;
    checkIntervalDomains();
    tokenId = "";
    if (token != null) {
      tokenId = token.getId().toString();
    }
//     System.err.println( "Temporal Node: " + tokenId + " eS " +
//                         earliestStartTime + " lS " + latestStartTime + " eE " +
//                         earliestEndTime + " lE " + latestEndTime);
//     System.err.println( "earliestDurationString " + earliestDurationString +
//                         " latestDurationString " + latestDurationString);
    if (earliestDurationString.equals( DbConstants.MINUS_INFINITY)) {
      earliestDurationTime = DbConstants.MINUS_INFINITY_INT;
    } else if (earliestDurationString.equals( DbConstants.PLUS_INFINITY)) {
      earliestDurationTime = DbConstants.PLUS_INFINITY_INT;
    } else {
      earliestDurationTime = Integer.parseInt( earliestDurationString);
    }
    if (latestDurationString.equals( DbConstants.PLUS_INFINITY)) {
      latestDurationTime = DbConstants.PLUS_INFINITY_INT;
    } else if (latestDurationString.equals( DbConstants.MINUS_INFINITY)) {
      latestDurationTime = DbConstants.MINUS_INFINITY_INT;
    } else {
      latestDurationTime = Integer.parseInt( latestDurationString);
    }
    this.backgroundColor = backgroundColor;
    this.isFreeToken = isFreeToken;
    this.isShowLabels = isShowLabels;
    labelLines = TemporalNode.createNodeLabel( token);
    predicateName = labelLines[0];
    StringBuffer labelBuf = new StringBuffer( predicateName);
    labelBuf.append( "\n").append( labelLines[1]);
    nodeLabel = labelBuf.toString();
    nodeLabelWidth = TemporalNode.getNodeLabelWidth( labelLines, temporalExtentView);
    markAndBridgeList = new ArrayList();
    cellRow = Algorithms.NO_ROW;
    temporalNodeXDelta = ViewConstants.TEMPORAL_NODE_X_DELTA *
      temporalExtentView.getZoomFactor();
    temporalNodeYDelta = ViewConstants.TEMPORAL_NODE_Y_DELTA * 
      temporalExtentView.getZoomFactor();
    if (temporalExtentView.getZoomFactor() > 1) {
      temporalNodeXDelta = temporalNodeXDelta / 2;
      temporalNodeYDelta = temporalNodeYDelta / 2;
    }
  } // end constructor

  private void checkIntervalDomains() {
    isEarliestStartMinusInf = false;
    isEarliestStartPlusInf = false;
    if (earliestStartTime == DbConstants.MINUS_INFINITY_INT) {
      isEarliestStartMinusInf = true;
      earliestStartTime = temporalExtentView.getTimeScaleStart();
    } else if (earliestStartTime == DbConstants.PLUS_INFINITY_INT) {
      isEarliestStartPlusInf = true;
      earliestStartTime = temporalExtentView.getTimeScaleEnd();
    }

    isLatestStartPlusInf = false;
    if (latestStartTime == DbConstants.PLUS_INFINITY_INT) {
      isLatestStartPlusInf = true;
      latestStartTime = temporalExtentView.getTimeScaleEnd();
    }

    isEarliestEndMinusInf = false;
    isEarliestEndPlusInf = false;
    if (earliestEndTime == DbConstants.MINUS_INFINITY_INT) {
      isEarliestEndMinusInf = true;
      earliestEndTime = temporalExtentView.getTimeScaleStart();
    } else if (earliestEndTime == DbConstants.PLUS_INFINITY_INT) {
      isEarliestEndPlusInf = true;
      earliestEndTime = temporalExtentView.getTimeScaleEnd();
    }

    isLatestEndPlusInf = false;
    if (latestEndTime == DbConstants.PLUS_INFINITY_INT) {
      isLatestEndPlusInf = true;
      latestEndTime = temporalExtentView.getTimeScaleEnd();
    }
  } // end checkIntervalDomains

  /**
   * <code>createNodeLabel</code>
   *
   * @param token - <code>PwToken</code> - 
   * @return - <code>String[]</code> - 
   */
  public static String [] createNodeLabel( PwToken token) {
    String [] labelLines = new String [2];
    String predicateName = null, nodeLabel2 = null;
    predicateName = token.getPredicateName();
    nodeLabel2 = "key=" + token.getId().toString();
    labelLines[0] = predicateName;
    labelLines[1] = nodeLabel2;
    return labelLines;
  } // end createNodeLabel


  /**
   * <code>getNodeLabelWidth</code>
   *
   * @param labelLines - <code>String[]</code> - 
   * @param temporalExtentView - <code>TemporalExtentView</code> - 
   * @return - <code>int</code> - 
   */
  public static int getNodeLabelWidth( String [] labelLines,
                                       TemporalExtentView temporalExtentView) {
    while(temporalExtentView.getFontMetrics() == null) {
      Thread.yield();
    }
    return Math.max( SwingUtilities.computeStringWidth( temporalExtentView.getFontMetrics(),
                                                        labelLines[0]),
                     SwingUtilities.computeStringWidth( temporalExtentView.getFontMetrics(),
                                                        labelLines[1])) +
      (ViewConstants.TIMELINE_VIEW_INSET_SIZE * 2);
  } // end getNodeLabelWidth


  /**
   * <code>configure</code> - called by TemoralExtentView.layoutTemporalNodes
   *
   */
  public void configure() {
    if (isShowLabels) {
      // set center point of label to earliestStartTime
      Point tokenLocation =
        new Point( temporalExtentView.getJGoRulerView().
                   scaleTimeNoZoom( (double) earliestStartTime),
                   scaleY() + ViewConstants.TEMPORAL_NODE_Y_LABEL_OFFSET);
      boolean isRectangular = true;
      setLabelSpot( JGoObject.Center);
      initialize( tokenLocation, nodeLabel, isRectangular);
      // BasicNode's initial location is its center - move left edge to  earliestStartTime
      int startTime = earliestStartTime;
      if (temporalDisplayMode == TemporalExtentView.SHOW_LATEST) {
        startTime = latestStartTime;
      }
      int newXLoc =
        (int) temporalExtentView.getJGoRulerView().scaleTimeNoZoom
        ( ((double) startTime) + (((nodeLabelWidth -
                                   (ViewConstants.TIMELINE_VIEW_INSET_SIZE * 2))
                                  * 0.5) /
                                 temporalExtentView.getTimeScale()));
      setLocation( newXLoc, (int) getLocation().getY());
      setBrush( JGoBrush.makeStockBrush( backgroundColor));  
      getLabel().setEditable( false);
      setDraggable( false);
      // do not allow user links
      getPort().setVisible( false);
      getLabel().setMultiline( true);
    }

    renderTimeIntervalExtents();
  } // end configure

  private void renderTimeIntervalExtents() {
    int yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_START_OFFSET +
      (temporalExtentView.getZoomFactor() - 1);
    if (! isShowLabels) {
      yLoc -= ViewConstants.TEMPORAL_NODE_Y_DELTA;
    }
    if (temporalDisplayMode != TemporalExtentView.SHOW_LATEST) {
      if (isEarliestStartMinusInf) {
        renderMinusInfinityMark( earliestStartTime, yLoc, MINUS_INFINITY_TIME_MARK);
      } else if (isEarliestStartPlusInf) {
        renderPlusInfinityMark( earliestStartTime, yLoc, PLUS_INFINITY_TIME_MARK);
      } else {
        renderStartMark( earliestStartTime, yLoc, EARLIEST_START_TIME_MARK);
      }
    }
    if (temporalDisplayMode != TemporalExtentView.SHOW_EARLIEST) {
      if (isLatestStartPlusInf) {
        renderPlusInfinityMark( latestStartTime, yLoc, PLUS_INFINITY_TIME_MARK);
      } else {
        renderStartMark( latestStartTime, yLoc, LATEST_START_TIME_MARK);
      }
    }

    renderTimeIntervalBridge();

    if (isShowLabels) {
      yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_END_OFFSET +
      ((temporalExtentView.getZoomFactor() - 1) * 4);
    } else {
      yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_END_OFFSET +
      ((temporalExtentView.getZoomFactor() - 1) * 8);
    }
    if (temporalDisplayMode != TemporalExtentView.SHOW_LATEST) {
      if (isEarliestEndMinusInf) {
        renderMinusInfinityMark( earliestEndTime, yLoc, MINUS_INFINITY_TIME_MARK);
      } else if (isEarliestEndPlusInf) {
        renderPlusInfinityMark( earliestEndTime, yLoc, PLUS_INFINITY_TIME_MARK);
      } else {
        renderEndMark( earliestEndTime, yLoc, EARLIEST_END_TIME_MARK);
      }
    }
    if (temporalDisplayMode != TemporalExtentView.SHOW_EARLIEST) {
      if (isLatestEndPlusInf) {
        renderPlusInfinityMark( latestEndTime, yLoc, PLUS_INFINITY_TIME_MARK);
      } else {
        renderEndMark( latestEndTime, yLoc, LATEST_END_TIME_MARK);
      }
    }
  } // end renderTimeIntervalExtents

  private void renderTimeIntervalBridge() {
    int yLoc;
    int startTime = earliestStartTime;
    int endTime = latestEndTime;
    int startDurationTime = earliestDurationTime;
    int endDurationTime = latestDurationTime;
    if (temporalDisplayMode == TemporalExtentView.SHOW_EARLIEST) {
      endTime = earliestEndTime;
      if (isEarliestStartMinusInf || isEarliestStartPlusInf || isEarliestEndMinusInf) {
        startDurationTime = DbConstants.PLUS_INFINITY_INT;
      } else {
        startDurationTime = earliestEndTime - earliestStartTime;
      }
      endDurationTime = startDurationTime;
    } else if (temporalDisplayMode == TemporalExtentView.SHOW_LATEST) {
      startTime = latestStartTime;
      if (isLatestStartPlusInf || isLatestEndPlusInf) {
        startDurationTime = DbConstants.PLUS_INFINITY_INT;
      } else {
        startDurationTime = latestEndTime - latestStartTime;
      }
      endDurationTime = startDurationTime;
    }
//     System.err.println( "token " + token.getId().toString());
//     System.err.println( "startDurationTime " + startDurationTime + " endDurationTime " +
//                         endDurationTime);
    if (isShowLabels) {
      yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_END_OFFSET +
        ((temporalExtentView.getZoomFactor() - 1) * 3);
      renderBridge( startTime, endTime, yLoc, startDurationTime, endDurationTime);
    } else {
      yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_END_OFFSET +
        ((temporalExtentView.getZoomFactor() - 1) * 4);
      renderThickBridge( startTime, endTime, yLoc - ViewConstants.TEMPORAL_NODE_Y_DELTA,
                         startDurationTime, endDurationTime);
    }
  } // end renderTimeIntervalBridge

  /**
   * <code>getToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken() {
    return token;
  }

  /**
   * <code>getSlot</code>
   *
   * @return - <code>PwSlot</code> - 
   */
  public PwSlot getSlot() {
    return slot;
  }

  /**
   * <code>getMarkAndBridgeList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getMarkAndBridgeList() {
    return markAndBridgeList;
  }

  /**
   * <code>getPredicateName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getPredicateName() {
    return predicateName;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( token.toString());
    if (temporalExtentView.getZoomFactor() > 1) {
      tip.append( "<br>key=");
      tip.append( token.getId().toString());
    }
    // check for free token
    if (slot != null) {
      tip.append( "<br>");
      tip.append( "slot key=");
      tip.append( slot.getId().toString());
    }
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview temporal node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( predicateName);
    tip.append( "<br>key=");
    tip.append( token.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  private int getStartTime() {
    int startTime = 0;
    if (temporalDisplayMode == TemporalExtentView.SHOW_INTERVALS) {
      startTime = earliestStartTime;
    } else if (temporalDisplayMode == TemporalExtentView.SHOW_EARLIEST) {
      startTime = earliestStartTime;
    } else if (temporalDisplayMode == TemporalExtentView.SHOW_LATEST) {
      startTime = latestStartTime;
    }
    return startTime;
  } // end getStartTime

  private int getEndTime() {
    int endTime = 0;
    if (temporalDisplayMode == TemporalExtentView.SHOW_INTERVALS) {
      endTime = latestEndTime;
    } else if (temporalDisplayMode == TemporalExtentView.SHOW_EARLIEST) {
      endTime = earliestEndTime;
    } else if (temporalDisplayMode == TemporalExtentView.SHOW_LATEST) {
      endTime = latestEndTime;
    }
    return endTime;
  } // end getStartTime

  /**
   * <code>getStart</code> - implements Extent
   *
   *           allow for label width as well as time extent
   *
   * @return - <code>int</code> - 
   */
  public int getStart() {
    int xStart =
      temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) getStartTime());
//     if (tokenId.equals( "185") || tokenId.equals( "978")) {
//       System.err.println( "xStart: " + predicateName + " xStart " +
//                           String.valueOf( (xStart - ViewConstants.TIMELINE_VIEW_INSET_SIZE)));
//     }
    return xStart - ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  }

  /**
   * <code>getEnd</code> - implements Extent
   *
   *           allow for label width as well as time extent
   *
   * @return - <code>int</code> - 
   */
  public int getEnd() {
    int xStart =
      temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) getStartTime());
    int xEnd =
      temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) getEndTime());
    int xStartPlusLabel = xStart;
    if (isShowLabels) {
      xStartPlusLabel = xStartPlusLabel + nodeLabelWidth;
    }
//     if (tokenId.equals( "185") || tokenId.equals( "978")) {
//       System.err.println( "xEnd: " + predicateName + " xEnd " +
//                           String.valueOf( (xEnd + ViewConstants.TIMELINE_VIEW_INSET_SIZE)) +
//                           " xStartPlusLabel " +
//                           String.valueOf( (xStartPlusLabel +
//                                           ViewConstants.TIMELINE_VIEW_INSET_SIZE)));
//       System.err.println( "isShowLabels " + isShowLabels + " nodeLabelWidth " +
//                           String.valueOf( nodeLabelWidth));
//     }
    return Math.max( xEnd, xStartPlusLabel) + ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  }

  /**
   * <code>getRow</code> - implements Extent
   *
   * @return - <code>int</code> - 
   */
  public int getRow() {
    return cellRow;
  }

  /**
   * <code>setRow</code> - implements Extent
   *
   * @param row - <code>int</code> - 
   */
  public void setRow( int row) {
    cellRow = row;
  }

  public String toString() {
    return token.getId().toString();
  }

  public boolean equals(TemporalNode other) {
    return token.getId().equals(other.getToken().getId());
  }

  private int scaleY() {
    return temporalExtentView.getStartYLoc() +
      (int) (cellRow * (ViewConstants.TEMPORAL_NODE_CELL_HEIGHT +
                        (temporalExtentView.getZoomFactor() *
                         temporalExtentView.getZoomFactor() * 2)));
  }


  // downward pointing triangle
  private void renderStartMark( int time, int y, int type) {
    TemporalNodeTimeMark downwardMark = new TemporalNodeTimeMark( time, type);
    downwardMark.setDraggable( false);
    downwardMark.setResizable(false);
    int xDown = temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) time);
    // System.err.println( "renderStartMark: time " + time + " xDown " + xDown);
    downwardMark.addPoint( xDown - temporalNodeXDelta, y);
    downwardMark.addPoint( xDown + temporalNodeXDelta, y);
    downwardMark.addPoint( xDown, y + temporalNodeYDelta);
    downwardMark.addPoint( xDown - temporalNodeXDelta, y);
    
    markAndBridgeList.add( downwardMark);
    temporalExtentView.getJGoDocument().addObjectAtTail( downwardMark);
  } // end renderStartMark

  // left pointing triangle
  private void renderMinusInfinityMark( int time, int y, int type) {
    TemporalNodeTimeMark minusInfinityMark =
      new TemporalNodeTimeMark( DbConstants.MINUS_INFINITY_INT, type);
    minusInfinityMark.setDraggable( false);
    minusInfinityMark.setResizable(false);
    // offset to the left to prevent overlap with earliestStartMark
    int xMinusInfinity =
      temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) time) -
      temporalNodeXDelta - 2;
    // System.err.println( "renderMinusInfinityMark: time " + time + " xMinusInfinity " +
    //                     xMinusInfinity);
    minusInfinityMark.addPoint( xMinusInfinity, y);
    minusInfinityMark.addPoint( xMinusInfinity, y + temporalNodeYDelta);
    minusInfinityMark.addPoint( xMinusInfinity - (temporalNodeXDelta * 2),
                                y + (temporalNodeYDelta / 2));
    minusInfinityMark.addPoint( xMinusInfinity, y);
    
    markAndBridgeList.add( minusInfinityMark);
    temporalExtentView.getJGoDocument().addObjectAtTail( minusInfinityMark);
  } // end renderMinusInfinityMark

  // upward pointing triangle
  private void renderEndMark( int time, int y, int type) {
    TemporalNodeTimeMark upwardMark = new TemporalNodeTimeMark( time, type);
    upwardMark.setDraggable( false);
    upwardMark.setResizable(false);
    int xUp = temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) time);
    // System.err.println( "renderEndMark: time " + time + " xUp " + xUp);
    upwardMark.addPoint( xUp, y);
    upwardMark.addPoint( xUp + temporalNodeXDelta,
                         y + temporalNodeYDelta);
    upwardMark.addPoint( xUp - temporalNodeXDelta,
                         y + temporalNodeYDelta);
    upwardMark.addPoint( xUp, y);
    markAndBridgeList.add( upwardMark);
    temporalExtentView.getJGoDocument().addObjectAtTail( upwardMark);
  } // end renderEndMark

  // right pointing triangle
  private void renderPlusInfinityMark( int time, int y, int type) {
    TemporalNodeTimeMark plusInfinityMark =
      new TemporalNodeTimeMark( DbConstants.PLUS_INFINITY_INT, type);
    plusInfinityMark.setDraggable( false);
    plusInfinityMark.setResizable(false);
    // offset to the right to prevent overlap with earliestEndMark
    int xPlusInfinity =
      temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) time) +
      temporalNodeXDelta + 2;
    // System.err.println( "renderPlusInfinityMark: time " + time + " xPlusInfinity " +
    //                     xPlusInfinity);
    plusInfinityMark.addPoint( xPlusInfinity, y);
    plusInfinityMark.addPoint( xPlusInfinity + (temporalNodeXDelta * 2),
                                y + (temporalNodeYDelta / 2));
    plusInfinityMark.addPoint( xPlusInfinity, y + temporalNodeYDelta);
    plusInfinityMark.addPoint( xPlusInfinity, y);
    
    markAndBridgeList.add( plusInfinityMark);
    temporalExtentView.getJGoDocument().addObjectAtTail( plusInfinityMark);
  } // end renderPlusInfinityMark


  private void renderBridge( int startTime, int endTime, int y,
                             int earliestDurationTime, int latestDurationTime) {
    int penWidth = temporalExtentView.getZoomFactor();
    TemporalNodeDurationBridge bridge =
      new TemporalNodeDurationBridge( earliestDurationTime, latestDurationTime, penWidth);
    int xStartTime =
      temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) startTime);
    if (isEarliestStartMinusInf && (temporalDisplayMode != TemporalExtentView.SHOW_LATEST)) {
      xStartTime -= ViewConstants.TEMPORAL_NODE_X_DELTA + 2;
    }
    bridge.addPoint( xStartTime, y);
    int xEndTime =
      temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) endTime);
    if (isLatestEndPlusInf && (temporalDisplayMode != TemporalExtentView.SHOW_EARLIEST)) {
      xEndTime += ViewConstants.TEMPORAL_NODE_X_DELTA + 2;
    }
    bridge.addPoint( xEndTime, y);
    markAndBridgeList.add( bridge);
    temporalExtentView.getJGoDocument().addObjectAtTail( bridge);
  } // end renderBridge

  private void renderThickBridge( int startTime, int endTime, int y,
                                  int earliestDurationTime, int latestDurationTime) {
    int xStartTime =
      temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) startTime);
    if (isEarliestStartMinusInf && (temporalDisplayMode != TemporalExtentView.SHOW_LATEST)) {
      xStartTime -= ViewConstants.TEMPORAL_NODE_X_DELTA + 2;
    }
    int xEndTime =
      temporalExtentView.getJGoRulerView().scaleTimeNoZoom( (double) endTime);
    if (isLatestEndPlusInf && (temporalDisplayMode != TemporalExtentView.SHOW_EARLIEST)) {
      xEndTime += ViewConstants.TEMPORAL_NODE_X_DELTA + 2;
    }
    ThickDurationBridge bridge =
      new ThickDurationBridge( earliestDurationTime, latestDurationTime,
                               xStartTime, y,
                               // minumum width of 2 to allow tooltip to register
                               Math.max( xEndTime - xStartTime, 2),
                               temporalNodeYDelta,
                               backgroundColor, labelLines, this, temporalExtentView);
    markAndBridgeList.add( bridge);
    temporalExtentView.getJGoDocument().addObjectAtTail( bridge);
  } // end renderThickBridge

  /**
   * <code>doMouseClick</code> - Mouse-Right: Set Active Token
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    TemporalNode temporalNode = (TemporalNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
    navigatorItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          String viewSetKey = temporalExtentView.getNavigatorViewSetKey();
          MDIInternalFrame navigatorFrame =
            temporalExtentView.openNavigatorViewFrame( viewSetKey);
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = temporalExtentView.getPartialPlan();
          contentPane.add( new NavigatorView( TemporalNode.this.getToken(), partialPlan,
                                              temporalExtentView.getViewSet(),
                                              viewSetKey, navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    JMenuItem activeTokenItem = new JMenuItem( "Set Active Token");
    final PwToken activeToken = TemporalNode.this.getToken();
    // check for empty slots
    if (activeToken != null) {
      activeTokenItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            ((PartialPlanViewSet) temporalExtentView.getViewSet()).setActiveToken( activeToken);
            ((PartialPlanViewSet) temporalExtentView.getViewSet()).setSecondaryTokens( null);
            System.err.println( "TemporalNode setActiveToken: " +
                                activeToken.getPredicateName() +
                                " (key=" + activeToken.getId().toString() + ")");
          }
        });
      mouseRightPopup.add( activeTokenItem);

      ViewGenerics.showPopupMenu( mouseRightPopup, temporalExtentView, viewCoords);
    }
  } // end mouseRightPopupMenu


} // end class TemporalNode


  
  
