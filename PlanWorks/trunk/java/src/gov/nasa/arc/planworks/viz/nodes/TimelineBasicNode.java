// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineBasicNode.java,v 1.1 2003-08-06 01:20:14 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 04aug03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.VizView;


/**
 * <code>TimelineBasicNode</code> - JGo widget to render a timeline as a rectangle,
 *                             with a label consisting of the PwObject name,
 *                             and the PwTimeline name.  Extends BasicNode,
 *                             rather than TextNode, so it can be handled by
 *                             JGo Layout.
 *             Object->JGoObject->JGoArea->BasicNode->TimelineBasicNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *        NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineBasicNode extends BasicNode {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);

  private String timelineName;
  private PwTimeline timeline;
  private int objectCnt;
  private VizView vizView;

  private List slotNodeList; // element SlotNode

  /**
   * <code>TimelineBasicNode</code> - constructor 
   *
   * @param timelineName - <code>String</code> - 
   * @param timeline - <code>PwTimeline</code> - 
   * @param timelineLocation - <code>Point</code> - 
   * @param objectCnt - <code>int</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TimelineBasicNode( String timelineName, PwTimeline timeline, Point timelineLocation,
                       int objectCnt,  boolean isDraggable, VizView vizView) {
    super();
    this.timelineName = timelineName;
    this.timeline = timeline;
    this.objectCnt = objectCnt;
    this.vizView = vizView;
    // System.err.println( "TimelineBasicNode: timelineName " + timelineName);
    this.slotNodeList = new ArrayList();

    configure( timelineLocation, timelineName, isDraggable);

  } // end constructor


  private final void configure( Point timelineLocation, String timelineName,
                                boolean isDraggable) {
   boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( timelineLocation, timelineName, isRectangular);
    String backGroundColor = null;
    backGroundColor = ((objectCnt % 2) == 0) ?
      ViewConstants.EVEN_OBJECT_TIMELINE_BG_COLOR :
      ViewConstants.ODD_OBJECT_TIMELINE_BG_COLOR;
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
  } // end configure

  /**
   * <code>getTimeline</code>
   *
   * @return - <code>PwTimeline</code> - 
   */
  public PwTimeline getTimeline() {
    return timeline;
  }

  /**
   * <code>getTimelineName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getTimelineName() {
    return timelineName;
  }


  // Event handlers to subclass

  // doMouseClick - single user click
  // doMouseDblClick - double user click
  // doUncapturedMouseMove - mouse over object
  // getToolTipText - return string to display in Tool Tip


} // end class TimelineBasicNode

 
