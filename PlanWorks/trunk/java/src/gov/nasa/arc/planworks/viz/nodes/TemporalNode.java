// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TemporalNode.java,v 1.6 2003-08-28 23:31:39 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- started 21July03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.Extent;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.temporalExtent.TemporalExtentView;


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
  private PwDomain durationIntervalDomain;
  private int objectCnt;
  private boolean isFreeToken;
  private TemporalExtentView view;
  private boolean isEarliestStartMinusInf;
  private boolean isLatestEndPlusInf;

  private String predicateName;
  private String nodeLabel;
  private List markAndBridgeList; // elements JGoPolygon & JGoStroke
  private int cellRow; // for layout algorithm

  /**
   * <code>TemporalNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param startTimeIntervalDomain - <code>PwDomain</code> - 
   * @param endTimeIntervalDomain - <code>PwDomain</code> -  
   * @param durationIntervalDomain - <code>PwDomain</code> -  
   * @param objectCnt - <code>int</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param view - <code>TemporalExtentView</code> - 
   */
  public TemporalNode( PwToken token, PwSlot slot, PwDomain startTimeIntervalDomain,
                       PwDomain endTimeIntervalDomain, PwDomain durationIntervalDomain,
                       int objectCnt, boolean isFreeToken, TemporalExtentView view) {
    super();
    this.token = token;
    this.slot = slot;
    earliestStartTime = startTimeIntervalDomain.getLowerBoundInt();
    isEarliestStartMinusInf = false;
    if (earliestStartTime == PwDomain.MINUS_INFINITY_INT) {
      isEarliestStartMinusInf = true;
      earliestStartTime = view.getTimeScaleStart();
    }
    latestStartTime = startTimeIntervalDomain.getUpperBoundInt();
    earliestEndTime = endTimeIntervalDomain.getLowerBoundInt();
    latestEndTime = endTimeIntervalDomain.getUpperBoundInt();
    isLatestEndPlusInf = false;
    if (latestEndTime == PwDomain.PLUS_INFINITY_INT) {
      isLatestEndPlusInf = true;
      latestEndTime = view.getTimeScaleEnd();
    }
    String tokenId = "";
    if (token != null) {
      tokenId = token.getId().toString();
    }
//     System.err.println( "Temporal Node: " + tokenId + " eS " +
//                         earliestStartTime + " lS " + latestStartTime + " eE " +
//                         earliestEndTime + " lE " + latestEndTime);
    this.durationIntervalDomain = durationIntervalDomain;
    this.objectCnt = objectCnt;
    this.isFreeToken = isFreeToken;
    this.view = view;
    if (token != null) {
      predicateName = token.getPredicate().getName();
      // nodeLabel = predicateName + " " + token.getId().toString();
      nodeLabel = predicateName;
    } else {
      predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
      nodeLabel = predicateName;
    }
    markAndBridgeList = new ArrayList();
    cellRow = Algorithms.NO_ROW;
  } // end constructor


  public void configure() {
    int midpointTime = earliestStartTime + (latestEndTime - earliestStartTime) / 2;
    Point tokenLocation = new Point( view.scaleTime( midpointTime),
                                     scaleY() +
                                     ViewConstants.TEMPORAL_NODE_Y_LABEL_OFFSET);
    boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( tokenLocation, nodeLabel, isRectangular);
    // BasicNode's initial location is its center
    setLocation( Math.max( (int) (view.scaleTime( earliestStartTime) +
                                  (getSize().getWidth() * 0.5)),
                           (int) (getLocation().getX())),
                 (int) getLocation().getY());

    String backGroundColor = null;
    if (isFreeToken) {
      backGroundColor = ViewConstants.FREE_TOKEN_BG_COLOR;
    } else {
      backGroundColor = ((objectCnt % 2) == 0) ?
        ViewConstants.EVEN_OBJECT_SLOT_BG_COLOR :
        ViewConstants.ODD_OBJECT_SLOT_BG_COLOR;
    }
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    setDraggable( false);
    // do not allow user links
    getPort().setVisible( false);

    // render time interval extents
    int yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_START_OFFSET;
    // if (isFreeToken) {
    if (isEarliestStartMinusInf) {
      renderMinusInfinityMark( view.getTimeScaleStart(), yLoc);
    } else {
      renderStartMark( earliestStartTime, yLoc);
    }
    renderStartMark( latestStartTime, yLoc);
    yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_END_OFFSET;
    renderEndMark( earliestEndTime, yLoc);
    // if (isFreeToken) {
    if (isLatestEndPlusInf) {
      renderPlusInfinityMark( view.getTimeScaleEnd(), yLoc);
    } else {
      renderEndMark( latestEndTime, yLoc);
    }
//     if (isFreeToken) {
//       renderBridge( view.getTimeScaleStart(), view.getTimeScaleEnd(), yLoc,
//                     durationIntervalDomain);
//     } else {
      renderBridge( earliestStartTime, latestEndTime, yLoc, durationIntervalDomain);
//     }
  } // end configure


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
    if (token != null) {
      return token.toString();
    } else {
      return "";
    }
  } // end getToolTipText


  /**
   * <code>getStart</code> - implements Extent
   *
   *           allow for label width as well as time extent
   *
   * @return - <code>int</code> - 
   */
  public int getStart() {
    //return Math.min( view.scaleTime( earliestStartTime),
    //                 (int) (getLocation().getX() - getSize().getWidth() * 0.5));
    return earliestStartTime;
  }

  /**
   * <code>getEnd</code> - implements Extent
   *
   *           allow for label width as well as time extent
   *
   * @return - <code>int</code> - 
   */
  public int getEnd() {
    //return Math.max( view.scaleTime( latestEndTime),
    //                 (int) (getLocation().getX() + getSize().getWidth() * 0.5));
    return latestEndTime;
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

  private int scaleY() {
    return view.getStartYLoc() +
      (int) (cellRow * ViewConstants.TEMPORAL_NODE_CELL_HEIGHT);
  }


  // downward pointing triangle
  private void renderStartMark( int time, int y) {
    TemporalNodeTimeMark downwardMark = new TemporalNodeTimeMark( time);
    downwardMark.setDraggable( false);
    downwardMark.setResizable(false);
    int xDown = view.scaleTime( time);
    downwardMark.addPoint( xDown - ViewConstants.TEMPORAL_NODE_X_DELTA, y);
    downwardMark.addPoint( xDown + ViewConstants.TEMPORAL_NODE_X_DELTA, y);
    downwardMark.addPoint( xDown, y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    downwardMark.addPoint( xDown - ViewConstants.TEMPORAL_NODE_X_DELTA, y);
    
    markAndBridgeList.add( downwardMark);
    view.getJGoDocument().addObjectAtTail( downwardMark);
  } // end renderStartMark

  // left pointing triangle
  private void renderMinusInfinityMark( int time, int y) {
    TemporalNodeTimeMark minusInfinityMark =
      new TemporalNodeTimeMark( PwDomain.MINUS_INFINITY_INT);
    minusInfinityMark.setDraggable( false);
    minusInfinityMark.setResizable(false);
    int xMinusInfinity = view.scaleTime( time);
    minusInfinityMark.addPoint( xMinusInfinity, y);
    minusInfinityMark.addPoint( xMinusInfinity, y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    minusInfinityMark.addPoint( xMinusInfinity - (ViewConstants.TEMPORAL_NODE_X_DELTA * 2),
                                y + (ViewConstants.TEMPORAL_NODE_Y_DELTA / 2));
    minusInfinityMark.addPoint( xMinusInfinity, y);
    
    markAndBridgeList.add( minusInfinityMark);
    view.getJGoDocument().addObjectAtTail( minusInfinityMark);
  } // end renderMinusInfinityMark

  // upward pointing triangle
  private void renderEndMark( int time, int y) {
    TemporalNodeTimeMark upwardMark = new TemporalNodeTimeMark( time);
    upwardMark.setDraggable( false);
    upwardMark.setResizable(false);
    int xUp = view.scaleTime( time);
    upwardMark.addPoint( xUp, y);
    upwardMark.addPoint( xUp + ViewConstants.TEMPORAL_NODE_X_DELTA,
                         y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    upwardMark.addPoint( xUp - ViewConstants.TEMPORAL_NODE_X_DELTA,
                         y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    upwardMark.addPoint( xUp, y);
    markAndBridgeList.add( upwardMark);
    view.getJGoDocument().addObjectAtTail( upwardMark);
  } // end renderEndMark

  // right pointing triangle
  private void renderPlusInfinityMark( int time, int y) {
    TemporalNodeTimeMark plusInfinityMark =
      new TemporalNodeTimeMark( PwDomain.PLUS_INFINITY_INT);
    plusInfinityMark.setDraggable( false);
    plusInfinityMark.setResizable(false);
    int xPlusInfinity = view.scaleTime( time);
    plusInfinityMark.addPoint( xPlusInfinity, y);
    plusInfinityMark.addPoint( xPlusInfinity + (ViewConstants.TEMPORAL_NODE_X_DELTA * 2),
                                y + (ViewConstants.TEMPORAL_NODE_Y_DELTA / 2));
    plusInfinityMark.addPoint( xPlusInfinity, y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    plusInfinityMark.addPoint( xPlusInfinity, y);
    
    markAndBridgeList.add( plusInfinityMark);
    view.getJGoDocument().addObjectAtTail( plusInfinityMark);
  } // end renderPlusInfinityMark


  private void renderBridge( int startTime, int endTime, int y,
                             PwDomain durationIntervalDomain) {
    int lineWidth = 1;
    TemporalNodeDurationBridge bridge =
      new TemporalNodeDurationBridge( durationIntervalDomain.getLowerBoundInt(),
                                      durationIntervalDomain.getUpperBoundInt());
    bridge.setDraggable( false);
    bridge.setResizable(false);
    bridge.setPen( new JGoPen( JGoPen.SOLID, lineWidth, ColorMap.getColor( "black")));
    bridge.addPoint( view.scaleTime( startTime), y);
    bridge.addPoint( view.scaleTime( endTime), y);
    markAndBridgeList.add( bridge);
    view.getJGoDocument().addObjectAtTail( bridge);
  } // end renderBridge

} // end class TemporalNode


  
  
