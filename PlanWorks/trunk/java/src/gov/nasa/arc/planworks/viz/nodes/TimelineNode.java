// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TimelineNode.java,v 1.17 2004-05-28 20:21:17 taylor Exp $
//
// PlanWorks
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;

import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;

public class TimelineNode extends ExtendedBasicNode {
  
  protected PwTimeline timeline;
  protected PartialPlanView partialPlanView;
  protected String nodeLabel;
  protected boolean isDebug;
  protected Color backgroundColor;
    public TimelineNode( PwTimeline timeline, Point timelineLocation, Color backgroundColor,
                            boolean isDraggable, PartialPlanView partialPlanView) { 
    super( ViewConstants.RIGHT_TRAPEZOID);
    this.timeline = timeline;
    this.partialPlanView = partialPlanView;
    this.backgroundColor = backgroundColor;
    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( timeline.getName());
    labelBuf.append( "\nkey=").append( timeline.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "TimelineNavNode: " + nodeLabel);

    configure( timelineLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( final Point timelineLocation, final Color backgroundColor,
                                final boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( timelineLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
  } // end configure

  public boolean equals( TimelineNode node) {
    return (this.getTimeline().getId().equals( node.getTimeline().getId()));
  }
  
  public PwTimeline getTimeline() {
    return timeline;
  }
  
  public PartialPlanView getPartialPlanView() {
    return partialPlanView;
  }

  public String toString() {
    return timeline.getId().toString();
  }

  public Color getColor(){return backgroundColor;}

}
