// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGo3DRect;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.VizView;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;


/**
 * <code>TimelineNode</code> -
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


  /**
   * <code>TimelineNode</code> - constructor 
   *
   * @param timelineName - <code>String</code> - 
   * @param timeline - <code>PwTimeline</code> - 
   * @param view - <code>VizView</code> - 
   */
  public TimelineNode( String timelineName, PwTimeline timeline, Point timelineLocation,
                       TimelineView view) {
    super( timelineName);
    this.timelineName = timelineName;
    this.timeline = timeline;
    this.timelineLocation = timelineLocation;
    this.view = view;
    // System.err.println( "TimelineNode: timelineName " + timelineName);
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

  // Event handlers to subclass

  // doMouseClick - single user click
  // doMouseDblClick - double user click
  // doUncapturedMouseMove - mouse over object
  // getToolTipText - return string to display in Tool Tip


} // end class TimelineNode

 
