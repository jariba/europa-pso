// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineNode.java,v 1.4 2003-06-19 00:31:20 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;


/**
 * <code>TimelineNode</code> - JGo widget to render a timeline as a rectangle,
 *                             with a label consisting of the PwObject name,
 *                             and the PwTimeline name
 *             Object->JGoObject->JGoArea->TextNode->TimelineNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *        NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineNode extends TextNode {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);

  private String timelineName;
  private PwTimeline timeline;
  private Point timelineLocation;
  private TimelineView view;

  private List slotNodeList; // element SlotNode

  /**
   * <code>TimelineNode</code> - constructor 
   *
   * @param timelineName - <code>String</code> - 
   * @param timeline - <code>PwTimeline</code> - 
   * @param timelineLocation - <code>Point</code> - 
   * @param view - <code>TimelineView</code> - 
   */
  public TimelineNode( String timelineName, PwTimeline timeline, Point timelineLocation,
                       TimelineView view) {
    super( timelineName);
    this.timelineName = timelineName;
    this.timeline = timeline;
    this.timelineLocation = timelineLocation;
    this.view = view;
    // System.err.println( "TimelineNode: timelineName " + timelineName);
    this.slotNodeList = new ArrayList();

    configure();

  } // end constructor


  private final void configure() {
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( "gray60")));  
    getLabel().setEditable( false);
    setDraggable( false);
    // do not allow links
    getTopPort().setVisible( false);
    getLeftPort().setVisible( false);
    getBottomPort().setVisible( false);
    getRightPort().setVisible( false);
    setLocation( (int) timelineLocation.getX(), (int) timelineLocation.getY());
    setInsets( NODE_INSETS);
  } // end configure

  /**
   * <code>getTimelineName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getTimelineName() {
    return timelineName;
  }

  /**
   * <code>getTimelineLocation</code>
   *
   * @return - <code>Point</code> - 
   */
  public Point getTimelineLocation() {
    return timelineLocation;
  }

  /**
   * <code>getSlotNodeList</code> - return list of this timeline's slot node objects
   *
   * @return - <code>List</code> - of SlotNode
   */
  public List getSlotNodeList() {
    return slotNodeList;
  }

  /**
   * <code>addToSlotNodeList</code>
   *
   * @param slotNode - <code>SlotNode</code> - 
   */
  public void addToSlotNodeList( SlotNode slotNode) {
    slotNodeList.add( slotNode);
  }

  // Event handlers to subclass

  // doMouseClick - single user click
  // doMouseDblClick - double user click
  // doUncapturedMouseMove - mouse over object
  // getToolTipText - return string to display in Tool Tip


} // end class TimelineNode

 
