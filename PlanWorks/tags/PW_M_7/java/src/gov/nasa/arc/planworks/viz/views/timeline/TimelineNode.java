// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineNode.java,v 1.2 2003-09-23 19:28:17 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.views.timeline;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.VizView;


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
  private VizView vizView;

  private List slotNodeList; // element SlotNode

  /**
   * <code>TimelineNode</code> - constructor 
   *
   * @param timelineName - <code>String</code> - 
   * @param timeline - <code>PwTimeline</code> - 
   * @param timelineLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TimelineNode( String timelineName, PwTimeline timeline, Point timelineLocation,
                       Color backgroundColor, VizView vizView) {
    super( timelineName);
    this.timelineName = timelineName;
    this.timeline = timeline;
    this.vizView = vizView;
    // System.err.println( "TimelineNode: timelineName " + timelineName);
    this.slotNodeList = new ArrayList();

    configure( timelineLocation, backgroundColor);

  } // end constructor


  private final void configure( Point timelineLocation, Color backgroundColor) {
    setBrush( JGoBrush.makeStockBrush( backgroundColor));
    getLabel().setEditable( false);
    getLabel().setBold( true);
    getLabel().setMultiline( true);
    getLabel().setAlignment( JGoText.ALIGN_CENTER);
    setDraggable( false);
    // do not allow user links
    getTopPort().setVisible( false);
    getLeftPort().setVisible( false);
    getBottomPort().setVisible( false);
    getRightPort().setVisible( false);
    setLocation( (int) timelineLocation.getX(), (int) timelineLocation.getY());
    setInsets( NODE_INSETS);
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

 
