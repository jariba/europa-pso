// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TemporalNode.java,v 1.14 2004-01-17 01:22:54 taylor Exp $
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
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>TemporalNode</code> - JGo widget to render a token's temporal extents
 *                             with a label consisting of the slot's predicate name.
 *                             create TemporalNode objects, then determine
 *                             layout, then call configure to render them
 *             Object->JGoObject->JGoArea->TextNode->TemporalNode

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TemporalNode extends BasicNode implements Extent {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

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
    isEarliestStartMinusInf = false;
    if (earliestStartTime == DbConstants.MINUS_INFINITY_INT) {
      isEarliestStartMinusInf = true;
      earliestStartTime = temporalExtentView.getTimeScaleStart();
    }
    latestStartTime = startTimeIntervalDomain.getUpperBoundInt();
    isLatestStartPlusInf = false;
    if (latestStartTime == DbConstants.PLUS_INFINITY_INT) {
      isLatestStartPlusInf = true;
      latestStartTime = temporalExtentView.getTimeScaleEnd();
    }
    earliestEndTime = endTimeIntervalDomain.getLowerBoundInt();
    isEarliestEndMinusInf = false;
    isEarliestEndPlusInf = false;
    if (earliestEndTime == DbConstants.MINUS_INFINITY_INT) {
      isEarliestEndMinusInf = true;
      earliestEndTime = temporalExtentView.getTimeScaleStart();
    } else if (earliestEndTime == DbConstants.PLUS_INFINITY_INT) {
      isEarliestEndPlusInf = true;
      earliestEndTime = temporalExtentView.getTimeScaleEnd();
    }
    latestEndTime = endTimeIntervalDomain.getUpperBoundInt();
    isLatestEndPlusInf = false;
    if (latestEndTime == DbConstants.PLUS_INFINITY_INT) {
      isLatestEndPlusInf = true;
      latestEndTime = temporalExtentView.getTimeScaleEnd();
    }
    tokenId = "";
    if (token != null) {
      tokenId = token.getId().toString();
    }
//     System.err.println( "Temporal Node: " + tokenId + " eS " +
//                         earliestStartTime + " lS " + latestStartTime + " eE " +
//                         earliestEndTime + " lE " + latestEndTime);
    if (earliestDurationString.equals( DbConstants.MINUS_INFINITY)) {
      earliestDurationTime = DbConstants.MINUS_INFINITY_INT;
    } else {
      earliestDurationTime = Integer.parseInt( earliestDurationString);
    }
    if (latestDurationString.equals( DbConstants.PLUS_INFINITY)) {
      latestDurationTime = DbConstants.PLUS_INFINITY_INT;
    } else {
      latestDurationTime = Integer.parseInt( latestDurationString);
    }

    this.backgroundColor = backgroundColor;
    this.isFreeToken = isFreeToken;
    this.isShowLabels = isShowLabels;
    this.temporalExtentView = temporalExtentView;
    labelLines = TemporalNode.createNodeLabel( token);
    predicateName = labelLines[0];
    StringBuffer labelBuf = new StringBuffer( predicateName);
    labelBuf.append( "\n").append( labelLines[1]);
    nodeLabel = labelBuf.toString();
    nodeLabelWidth = TemporalNode.getNodeLabelWidth( labelLines, temporalExtentView);
    markAndBridgeList = new ArrayList();
    cellRow = Algorithms.NO_ROW;
  } // end constructor


  /**
   * <code>createNodeLabel</code>
   *
   * @param token - <code>PwToken</code> - 
   * @return - <code>String[]</code> - 
   */
  public static String [] createNodeLabel( PwToken token) {
    String [] labelLines = new String [2];
    String predicateName = null, nodeLabel2 = null;
    if (token != null) {
      predicateName = token.getPredicateName();
      nodeLabel2 = "key=" + token.getId().toString();
    } else {
      predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
      nodeLabel2 = "";
    }
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
      //       int midpointTime = earliestStartTime + (latestEndTime - earliestStartTime) / 2;
      //       Point tokenLocation = new Point( temporalExtentView.scaleTime( midpointTime),
      //                                        scaleY() +
      //                                        ViewConstants.TEMPORAL_NODE_Y_LABEL_OFFSET);
      // set center point of label to earliestStartTime
      Point tokenLocation = new Point( temporalExtentView.scaleTime( earliestStartTime),
                                       scaleY() +
                                       ViewConstants.TEMPORAL_NODE_Y_LABEL_OFFSET);
      boolean isRectangular = true;
      setLabelSpot( JGoObject.Center);
      initialize( tokenLocation, nodeLabel, isRectangular);
      // BasicNode's initial location is its center - move left edge to  earliestStartTime
      //       int newXLoc =
      //         (int) temporalExtentView.scaleTime
      //         ( (int) (earliestStartTime + temporalExtentView.scaleXLoc
      //                  ( (int) (getSize().getWidth() * 0.5))));

      //       int newXLoc =
      //         (int) temporalExtentView.scaleTime
      //         ( (int) (earliestStartTime + ((getSize().getWidth() * 0.5) /
      //                                       temporalExtentView.getTimeScale())));

      int startTime = earliestStartTime;
      if (temporalDisplayMode == TemporalExtentView.SHOW_LATEST) {
        startTime = latestStartTime;
      }
      int newXLoc =
        (int) temporalExtentView.scaleTime
        ( (int) (startTime + (((nodeLabelWidth -
                                (ViewConstants.TIMELINE_VIEW_INSET_SIZE * 2))
                               * 0.5) /
                              temporalExtentView.getTimeScale())));
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
    int yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_START_OFFSET;
    if (! isShowLabels) {
      yLoc -= ViewConstants.TEMPORAL_NODE_Y_DELTA;
    }
    if (temporalDisplayMode != TemporalExtentView.SHOW_LATEST) {
      if (isEarliestStartMinusInf) {
        renderMinusInfinityMark( earliestStartTime, yLoc);
      } else {
        renderStartMark( earliestStartTime, yLoc);
      }
    }
    if (temporalDisplayMode != TemporalExtentView.SHOW_EARLIEST) {
      if (isLatestStartPlusInf) {
        renderPlusInfinityMark( latestStartTime, yLoc);
      } else {
        renderStartMark( latestStartTime, yLoc);
      }
    }
    yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_END_OFFSET;
    if (temporalDisplayMode != TemporalExtentView.SHOW_LATEST) {
      if (isEarliestEndMinusInf) {
        renderMinusInfinityMark( earliestEndTime, yLoc);
      } else if (isEarliestEndPlusInf) {
        renderPlusInfinityMark( earliestEndTime, yLoc);
      } else {
        renderEndMark( earliestEndTime, yLoc);
      }
    }
    if (temporalDisplayMode != TemporalExtentView.SHOW_EARLIEST) {
      if (isLatestEndPlusInf) {
        renderPlusInfinityMark( latestEndTime, yLoc);
      } else {
        renderEndMark( latestEndTime, yLoc);
      }
    }
    int startTime = earliestStartTime;
    int endTime = latestEndTime;
    int startDurationTime = earliestDurationTime;
    int endDurationTime = latestDurationTime;
    if (temporalDisplayMode == TemporalExtentView.SHOW_EARLIEST) {
      endTime = earliestEndTime;
      if (isEarliestStartMinusInf || isEarliestEndMinusInf) {
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
    if (isShowLabels) {
      renderBridge( startTime, endTime, yLoc, startDurationTime, endDurationTime);
    } else {
      renderThickBridge( startTime, endTime, yLoc - ViewConstants.TEMPORAL_NODE_Y_DELTA,
                         startDurationTime, endDurationTime);
    }
  } // end renderTimeIntervalExtents


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
    if (token != null) {
      tip.append( token.toString());
    } else {
      tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
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
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    if (token != null) {
      tip.append( predicateName);
      tip.append( "<br>key=");
      tip.append( token.getId().toString());
    } else {
      tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
    }
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
    int xStart = temporalExtentView.scaleTime( getStartTime());
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
    int xStart = temporalExtentView.scaleTime( getStartTime());
    int xEnd = temporalExtentView.scaleTime( getEndTime());
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
    if(token == null) {
      return "-empty-";
    }
    return token.getId().toString();
  }

  public boolean equals(TemporalNode other) {
    return token.getId().equals(other.getToken().getId());
  }

  private int scaleY() {
    return temporalExtentView.getStartYLoc() +
      (int) (cellRow * ViewConstants.TEMPORAL_NODE_CELL_HEIGHT);
  }


  // downward pointing triangle
  private void renderStartMark( int time, int y) {
    TemporalNodeTimeMark downwardMark = new TemporalNodeTimeMark( time);
    downwardMark.setDraggable( false);
    downwardMark.setResizable(false);
    int xDown = temporalExtentView.scaleTime( time);
    // System.err.println( "renderStartMark: time " + time + " xDown " + xDown);
    downwardMark.addPoint( xDown - ViewConstants.TEMPORAL_NODE_X_DELTA, y);
    downwardMark.addPoint( xDown + ViewConstants.TEMPORAL_NODE_X_DELTA, y);
    downwardMark.addPoint( xDown, y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    downwardMark.addPoint( xDown - ViewConstants.TEMPORAL_NODE_X_DELTA, y);
    
    markAndBridgeList.add( downwardMark);
    temporalExtentView.getJGoDocument().addObjectAtTail( downwardMark);
  } // end renderStartMark

  // left pointing triangle
  private void renderMinusInfinityMark( int time, int y) {
    TemporalNodeTimeMark minusInfinityMark =
      new TemporalNodeTimeMark( DbConstants.MINUS_INFINITY_INT);
    minusInfinityMark.setDraggable( false);
    minusInfinityMark.setResizable(false);
    // offset to the left to prevent overlap with earliestStartMark
    int xMinusInfinity = temporalExtentView.scaleTime( time) -
      ViewConstants.TEMPORAL_NODE_X_DELTA - 2;
    // System.err.println( "renderMinusInfinityMark: time " + time + " xMinusInfinity " +
    //                     xMinusInfinity);
    minusInfinityMark.addPoint( xMinusInfinity, y);
    minusInfinityMark.addPoint( xMinusInfinity, y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    minusInfinityMark.addPoint( xMinusInfinity - (ViewConstants.TEMPORAL_NODE_X_DELTA * 2),
                                y + (ViewConstants.TEMPORAL_NODE_Y_DELTA / 2));
    minusInfinityMark.addPoint( xMinusInfinity, y);
    
    markAndBridgeList.add( minusInfinityMark);
    temporalExtentView.getJGoDocument().addObjectAtTail( minusInfinityMark);
  } // end renderMinusInfinityMark

  // upward pointing triangle
  private void renderEndMark( int time, int y) {
    TemporalNodeTimeMark upwardMark = new TemporalNodeTimeMark( time);
    upwardMark.setDraggable( false);
    upwardMark.setResizable(false);
    int xUp = temporalExtentView.scaleTime( time);
    // System.err.println( "renderEndMark: time " + time + " xUp " + xUp);
    upwardMark.addPoint( xUp, y);
    upwardMark.addPoint( xUp + ViewConstants.TEMPORAL_NODE_X_DELTA,
                         y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    upwardMark.addPoint( xUp - ViewConstants.TEMPORAL_NODE_X_DELTA,
                         y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    upwardMark.addPoint( xUp, y);
    markAndBridgeList.add( upwardMark);
    temporalExtentView.getJGoDocument().addObjectAtTail( upwardMark);
  } // end renderEndMark

  // right pointing triangle
  private void renderPlusInfinityMark( int time, int y) {
    TemporalNodeTimeMark plusInfinityMark =
      new TemporalNodeTimeMark( DbConstants.PLUS_INFINITY_INT);
    plusInfinityMark.setDraggable( false);
    plusInfinityMark.setResizable(false);
    // offset to the right to prevent overlap with earliestEndMark
    int xPlusInfinity = temporalExtentView.scaleTime( time) +
      ViewConstants.TEMPORAL_NODE_X_DELTA + 2;
    // System.err.println( "renderPlusInfinityMark: time " + time + " xPlusInfinity " +
    //                     xPlusInfinity);
    plusInfinityMark.addPoint( xPlusInfinity, y);
    plusInfinityMark.addPoint( xPlusInfinity + (ViewConstants.TEMPORAL_NODE_X_DELTA * 2),
                                y + (ViewConstants.TEMPORAL_NODE_Y_DELTA / 2));
    plusInfinityMark.addPoint( xPlusInfinity, y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    plusInfinityMark.addPoint( xPlusInfinity, y);
    
    markAndBridgeList.add( plusInfinityMark);
    temporalExtentView.getJGoDocument().addObjectAtTail( plusInfinityMark);
  } // end renderPlusInfinityMark


  private void renderBridge( int startTime, int endTime, int y,
                             int earliestDurationTime, int latestDurationTime) {
    int lineWidth = 1;
    TemporalNodeDurationBridge bridge =
      new TemporalNodeDurationBridge( earliestDurationTime, latestDurationTime);
    int xStartTime = temporalExtentView.scaleTime( startTime);
    if (isEarliestStartMinusInf && (temporalDisplayMode != TemporalExtentView.SHOW_LATEST)) {
      xStartTime -= ViewConstants.TEMPORAL_NODE_X_DELTA + 2;
    }
    bridge.addPoint( xStartTime, y);
    int xEndTime = temporalExtentView.scaleTime( endTime);
    if (isLatestEndPlusInf && (temporalDisplayMode != TemporalExtentView.SHOW_EARLIEST)) {
      xEndTime += ViewConstants.TEMPORAL_NODE_X_DELTA + 2;
    }
    bridge.addPoint( xEndTime, y);
    markAndBridgeList.add( bridge);
    temporalExtentView.getJGoDocument().addObjectAtTail( bridge);
  } // end renderBridge

  private void renderThickBridge( int startTime, int endTime, int y,
                                  int earliestDurationTime, int latestDurationTime) {
    int lineWidth = 1;
    int xStartTime = temporalExtentView.scaleTime( startTime);
    if (isEarliestStartMinusInf && (temporalDisplayMode != TemporalExtentView.SHOW_LATEST)) {
      xStartTime -= ViewConstants.TEMPORAL_NODE_X_DELTA + 2;
    }
    int xEndTime = temporalExtentView.scaleTime( endTime);
    if (isLatestEndPlusInf && (temporalDisplayMode != TemporalExtentView.SHOW_EARLIEST)) {
      xEndTime += ViewConstants.TEMPORAL_NODE_X_DELTA + 2;
    }
    ThickDurationBridge bridge =
      new ThickDurationBridge( earliestDurationTime, latestDurationTime,
                               xStartTime, y,
                               // minumum width of 2 to allow tooltip to register
                               Math.max( xEndTime - xStartTime, 2),
                               ViewConstants.TEMPORAL_NODE_Y_DELTA,
                               backgroundColor, labelLines, this, temporalExtentView);
    markAndBridgeList.add( bridge);
    temporalExtentView.getJGoDocument().addObjectAtTail( bridge);
  } // end renderBridge

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
          MDIInternalFrame navigatorFrame = temporalExtentView.openNavigatorViewFrame();
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = temporalExtentView.getPartialPlan();
          contentPane.add( new NavigatorView( TemporalNode.this, partialPlan,
                                              temporalExtentView.getViewSet(),
                                              navigatorFrame));
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

      NodeGenerics.showPopupMenu( mouseRightPopup, temporalExtentView, viewCoords);
    }
  } // end mouseRightPopupMenu


} // end class TemporalNode


  
  
