// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TemporalNode.java,v 1.4 2003-08-20 18:52:36 taylor Exp $
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
import com.nwoods.jgo.JGoPolygon;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwDomain;
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
  private int xOrigin;
  private int yOrigin;
  private float timeScale;
  private int objectCnt;
  private TemporalExtentView view;

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
   * @param xLocOrigin - <code>int</code> - 
   * @param yOrigin - <code>int</code> - 
   * @param timeScale - <code>float</code> - 
   * @param objectCnt - <code>int</code> - 
   * @param view - <code>TemporalExtentView</code> - 
   */
  public TemporalNode( PwToken token, PwSlot slot, PwDomain startTimeIntervalDomain,
                       PwDomain endTimeIntervalDomain, int xOrigin, int yOrigin,
                       float timeScale, int objectCnt, TemporalExtentView view) {
    super();
    this.token = token;
    this.slot = slot;
    earliestStartTime = Integer.parseInt( startTimeIntervalDomain.getLowerBound());
    latestStartTime = Integer.parseInt( startTimeIntervalDomain.getUpperBound());
    earliestEndTime = Integer.parseInt( endTimeIntervalDomain.getLowerBound());
    latestEndTime = Integer.parseInt( endTimeIntervalDomain.getUpperBound());
    this.xOrigin = xOrigin;
    this.yOrigin = yOrigin;
    this.timeScale = timeScale;
    this.objectCnt = objectCnt;
    this.view = view;
    if (token != null) {
      predicateName = token.getPredicate().getName();
      // nodeLabel = predicateName + " " + token.getId().toString();
      nodeLabel = predicateName;
    } else {
      predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
      nodeLabel = predicateName;
    }
//     System.err.println( "TemporalNode: " + nodeLabel + " " + earliestStartTime +
//                         " " + latestStartTime + " " + earliestEndTime + " " +
//                         latestEndTime);
    markAndBridgeList = new ArrayList();
    cellRow = Algorithms.NO_ROW;
  } // end constructor


  public void configure() {
    int midpointTime = earliestStartTime + (latestEndTime - earliestStartTime) / 2;
    Point tokenLocation = new Point( scaleTime( midpointTime),
                                     scaleY() + ViewConstants.TEMPORAL_NODE_Y_LABEL_OFFSET);
    boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( tokenLocation, nodeLabel, isRectangular);
    // BasicNode's initial location is its center
    setLocation( Math.max( (int) (scaleTime( earliestStartTime) +
                                  (getSize().getWidth() * 0.5)),
                           (int) (getLocation().getX())),
                 (int) getLocation().getY());

    String backGroundColor = null;
    backGroundColor = ((objectCnt % 2) == 0) ?
      ViewConstants.EVEN_OBJECT_SLOT_BG_COLOR :
      ViewConstants.ODD_OBJECT_SLOT_BG_COLOR;
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    setDraggable( false);
    // do not allow user links
    getPort().setVisible( false);

    // render time interval extents
    int yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_START_OFFSET;
    renderStartMark( earliestStartTime, yLoc);
    renderStartMark( latestStartTime, yLoc);
    yLoc = scaleY() + ViewConstants.TEMPORAL_NODE_Y_END_OFFSET;
    renderEndMark( earliestEndTime, yLoc);
    renderEndMark( latestEndTime, yLoc);
    renderBridge( earliestStartTime, latestEndTime, yLoc);
  } // end configure


  private int scaleTime( int time) {
    return xOrigin + (int) (timeScale * time);
  }

  private int scaleY() {
    return yOrigin + (int) (cellRow * ViewConstants.TEMPORAL_NODE_CELL_HEIGHT);
  }

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
    return token.toString();
  } // end getToolTipText


  /**
   * <code>getStart</code> - implements Extent
   *
   *           allow for label width as well as time extent
   *
   * @return - <code>int</code> - 
   */
  public int getStart() {
    return Math.min( scaleTime( earliestStartTime),
                     (int) (getLocation().getX() - getSize().getWidth() * 0.5));
  }

  /**
   * <code>getEnd</code> - implements Extent
   *
   *           allow for label width as well as time extent
   *
   * @return - <code>int</code> - 
   */
  public int getEnd() {
    return Math.max( scaleTime( latestEndTime),
                     (int) (getLocation().getX() + getSize().getWidth() * 0.5));
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


  // downward pointing triangle
  private void renderStartMark( int time, int y) {
    JGoPolygon downwardMark = new JGoPolygon();
    downwardMark.setDraggable( false);
    downwardMark.setResizable(false);
    int xDown = scaleTime( time);
    downwardMark.addPoint( xDown - ViewConstants.TEMPORAL_NODE_X_DELTA, y);
    downwardMark.addPoint( xDown + ViewConstants.TEMPORAL_NODE_X_DELTA, y);
    downwardMark.addPoint( xDown, y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    downwardMark.addPoint( xDown - ViewConstants.TEMPORAL_NODE_X_DELTA, y);
    markAndBridgeList.add( downwardMark);
    view.getJGoDocument().addObjectAtTail( downwardMark);
  } // end renderEndMark

  // upward pointing triangle
  private void renderEndMark( int time, int y) {
    JGoPolygon upwardMark = new JGoPolygon();
    upwardMark.setDraggable( false);
    upwardMark.setResizable(false);
    int xUp = scaleTime( time);
    upwardMark.addPoint( xUp, y);
    upwardMark.addPoint( xUp + ViewConstants.TEMPORAL_NODE_X_DELTA,
                         y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    upwardMark.addPoint( xUp - ViewConstants.TEMPORAL_NODE_X_DELTA,
                         y + ViewConstants.TEMPORAL_NODE_Y_DELTA);
    upwardMark.addPoint( xUp, y);
    markAndBridgeList.add( upwardMark);
    view.getJGoDocument().addObjectAtTail( upwardMark);
  } // end renderStartMark

  private void renderBridge( int startTime, int endTime, int y) {
    int lineWidth = 1;
    JGoStroke bridge = new JGoStroke();
    bridge.setDraggable( false);
    bridge.setResizable(false);
    bridge.setPen( new JGoPen( JGoPen.SOLID, lineWidth, ColorMap.getColor( "black")));
    bridge.addPoint( scaleTime( startTime), y);
    bridge.addPoint( scaleTime( endTime), y);
    markAndBridgeList.add( bridge);
    view.getJGoDocument().addObjectAtTail( bridge);
  } // end renderBridge

} // end class TemporalNode


  
  
