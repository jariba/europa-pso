// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineNode.java,v 1.4 2004-02-05 23:25:54 miatauro Exp $
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
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>TimelineNode</code> - JGo widget to render a timeline as a rectangle,
 *                             with a label consisting of the PwObject name,
 *                             and the PwTimeline name
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
  private PwObject object;
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
  public TimelineNode( String timelineName, PwTimeline timeline, PwObject object,
                       Point timelineLocation, Color backgroundColor,
                       TimelineView timelineView) {
    super( timelineName);
    this.timelineName = timelineName;
    this.timeline = timeline;
    this.object = object;
    this.backgroundColor = backgroundColor;
    this.timelineView = timelineView;
    // System.err.println( "TimelineNode: timelineName " + timelineName);
    this.slotNodeList = new ArrayList();

    configure( timelineLocation, backgroundColor);

  } // end constructor


  public TimelineNode(String timelineName, PwTimeline timeline, Point timelineLocation, 
                      Color backgroundColor, TimelineView timelineView) {
        super( timelineName);
    this.timelineName = timelineName;
    this.timeline = timeline;
    this.object = null;
    this.backgroundColor = backgroundColor;
    this.timelineView = timelineView;
    // System.err.println( "TimelineNode: timelineName " + timelineName);
    this.slotNodeList = new ArrayList();

    configure( timelineLocation, backgroundColor);

  }

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
    TimelineNode timelineNode = (TimelineNode) obj.getTopLevelObject();
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
          MDIInternalFrame navigatorFrame = timelineView.openNavigatorViewFrame();
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = timelineView.getPartialPlan();
          contentPane.add( new NavigatorView( TimelineNode.this, partialPlan,
                                              timelineView.getViewSet(),
                                              navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, timelineView, viewCoords);
  } // end mouseRightPopupMenu


} // end class TimelineNode

 
