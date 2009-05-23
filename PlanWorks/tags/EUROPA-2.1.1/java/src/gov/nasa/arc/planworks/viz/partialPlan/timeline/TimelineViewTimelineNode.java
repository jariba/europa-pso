// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineViewTimelineNode.java,v 1.8 2005-05-19 19:22:57 pdaley Exp $
//
// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.partialPlan.timeline;

import java.awt.Color;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>TimelineViewTimelineNode</code> - JGo widget to render a timeline as a rectangle,
 *                                         with a label consisting of the PwObject name,
 *                                         and the PwTimeline name
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *        NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineViewTimelineNode extends TextNode implements OverviewToolTip {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);

  private String timelineName;
  private PwTimeline timeline;
  private PwObject object;
  private int nodeWidth;
  private Color backgroundColor;
  private TimelineView timelineView;

  private List slotNodeList; // element SlotNode

  /**
   * <code>TimelineNode</code> - constructor 
   *
   * @param timelineName - <code>String</code> - 
   * @param timeline - <code>PwTimeline</code> - 
   * @param object - <code>PwObject</code> - 
   * @param timelineLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param timelineView - <code>TimelineView</code> - 
   */
  public TimelineViewTimelineNode( final String timelineName, final PwTimeline timeline, 
                                   final PwObject object, final Point timelineLocation, 
                                   final Color backgroundColor, final TimelineView timelineView) {
    super( timelineName);
    this.timelineName = timelineName;
    this.timeline = timeline;
    this.object = object;
    this.nodeWidth = 0;  //ignore forced nodeWidth
    this.backgroundColor = backgroundColor;
    this.timelineView = timelineView;
    // System.err.println( "TimelineNode: timelineName " + timelineName);
    this.slotNodeList = new ArrayList();

    configure( timelineLocation, nodeWidth, backgroundColor);

  } // end constructor


  public TimelineViewTimelineNode(final String timelineName, final PwTimeline timeline, 
                                  final Point timelineLocation, final int nodeWidth, 
                                  final Color backgroundColor, 
                                  final TimelineView timelineView) {
    super( timelineName);
    this.timelineName = timelineName;
    this.timeline = timeline;
    this.object = null;
    this.nodeWidth = nodeWidth;
    this.backgroundColor = backgroundColor;
    this.timelineView = timelineView;
    // System.err.println( "TimelineNode: timelineName " + timelineName);
    this.slotNodeList = new ArrayList();

    configure( timelineLocation, nodeWidth, backgroundColor);

  }

  private final void configure( Point timelineLocation, int forcedNodeWidth, Color backgroundColor) {
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
    if ( forcedNodeWidth <= 0) {
      setLocation( (int) timelineLocation.getX(), (int) timelineLocation.getY());
      setInsets( NODE_INSETS);
    } else {
      int newInset = forcedNodeWidth - 
                   (int) (getSize().getWidth() - ViewConstants.TIMELINE_VIEW_INSET_SIZE);
      newInset = (newInset / 2) + ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF;

      setInsets( new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,   //top
                             newInset,                                      //left
                             ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,   //bottom
                             newInset));                                    //right
      setLocation( (int) timelineLocation.getX() - ViewConstants.TIMELINE_VIEW_INSET_SIZE + 2,
                   (int) (timelineLocation.getY() - 3));
    }
//  System.err.println( "TimelineViewTimelineNode: configure: New node" + 
//                      " width = " + this.getSize().getWidth());
//  System.err.println( "TimelineViewTimeline: configure:  x = " + timelineLocation.getX() + 
//                       "  y = " + timelineLocation.getY());
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
   * <code>getPwObject</code>
   *
   * @return - <code>PwObect</code> - 
   */
  public PwObject getPwObject() {
    (new Throwable()).printStackTrace();
    return object;
  }

  /**
   * <code>getBackgroundColor</code>
   *
   * @return - <code>Color</code> - 
   */
  public Color getBackgroundColor() {
    return backgroundColor;
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

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( timeline.getName());
    if (timelineView.getZoomFactor() > 1) {
      tip.append( "<br>key=");
      tip.append( timeline.getId().toString());
    }
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( timeline.getName());
    tip.append( "<br>key=");
    tip.append( timeline.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>doMouseClick</code>
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
    TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) obj.getTopLevelObject();
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
          String viewSetKey = timelineView.getNavigatorViewSetKey();
          MDIInternalFrame navigatorFrame = timelineView.openNavigatorViewFrame( viewSetKey);
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = timelineView.getPartialPlan();
          contentPane.add( new NavigatorView( TimelineViewTimelineNode.this.getTimeline(),
                                              partialPlan, timelineView.getViewSet(),
                                              viewSetKey, navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    ViewGenerics.showPopupMenu( mouseRightPopup, timelineView, viewCoords);
  } // end mouseRightPopupMenu


} // end class TimelineNode

 
