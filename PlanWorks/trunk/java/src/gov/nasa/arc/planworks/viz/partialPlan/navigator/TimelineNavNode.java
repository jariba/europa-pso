// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TimelineNavNode.java,v 1.5 2004-02-17 22:24:36 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- started 06jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.Color;
import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>TimelineNavNode</code> - JGo widget to render a plan timeline and its neighbors
 *                                   for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineNavNode extends TimelineNode {

  private boolean areNeighborsShown;
  private int objectLinkCount;
  private int slotLinkCount;
  private boolean inLayout;

  /**
   * <code>TimelineNavNode</code> - constructor 
   *
   * @param timeline - <code>PwTimeline</code> - 
   * @param timelineLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public TimelineNavNode( final PwTimeline timeline, final Point timelineLocation, 
                          final Color backgroundColor, final boolean isDraggable, 
                          final PartialPlanView partialPlanView) { 
    super(timeline, timelineLocation, backgroundColor, isDraggable, partialPlanView);

    isDebug = false;
    // isDebug = true;

    inLayout = false;
    areNeighborsShown = false;
    objectLinkCount = 0;
    slotLinkCount = 0;
  } // end constructor

  public boolean inLayout() {
    return inLayout;
  }

  public void setInLayout( boolean value) {
    int width = 1;
    inLayout = value;
    if (value == false) {
      setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
      areNeighborsShown = false;
    }
  }

  /**
   * <code>setAreNeighborsShown</code>
   *
   * @param value - <code>boolean</code> - 
   */
  public void setAreNeighborsShown( boolean value) {
    areNeighborsShown = value;
  }

  /**
   * <code>incrObjectLinkCount</code>
   *
   */
  public void incrObjectLinkCount() {
    objectLinkCount++;
  }

  /**
   * <code>decObjectLinkCount</code>
   *
   */
  public void decObjectLinkCount() {
    objectLinkCount--;
  }

  /**
   * <code>getObjectLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getObjectLinkCount() {
    return objectLinkCount;
  }

  /**
   * <code>incrSlotLinkCount</code>
   *
   */
  public void incrSlotLinkCount() {
    slotLinkCount++;
  }

  /**
   * <code>decSlotLinkCount</code>
   *
   */
  public void decSlotLinkCount() {
    slotLinkCount--;
  }

  /**
   * <code>getSlotLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getSlotLinkCount() {
    return slotLinkCount;
  }

  /**
   * <code>resetNode</code> - when closed 
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (objectLinkCount != 0)) {
      System.err.println( "reset timeline node: " + timeline.getId() +
                          "; objectLinkCount != 0: " + objectLinkCount);
    }
    if (isDebug && (slotLinkCount != 0)) {
      System.err.println( "reset timeline node: " + timeline.getId() +
                          "; slotLinkCount != 0: " + slotLinkCount);
    }
    objectLinkCount = 0;
    slotLinkCount = 0;
  } // end resetNode

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    String operation = "";
    if (areNeighborsShown) {
      operation = "close";
    } else {
      operation = "open";
    }
    StringBuffer tip = new StringBuffer( "<html>timeline<br>");
    if (isDebug) {
      tip.append( " linkCntObj ").append( String.valueOf( objectLinkCount));
      tip.append( " linkCntSlot ").append( String.valueOf( slotLinkCount));
      tip.append( "<br>");
    }
    tip.append( "Mouse-L: ").append( operation);
    tip.append("</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview timeline node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>timeline<br>");
    tip.append( timeline.getName());
    tip.append( "<br>key=");
    tip.append( timeline.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText


  /**
   * <code>doMouseClick</code> - For Model Network View, Mouse-left opens/closes
   *            constarintNode to show variableNodes 
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point dc, Point vc, JGoView view) {
    JGoObject obj = view.pickDocObject( dc, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    TimelineNavNode timelineNode = (TimelineNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      NavigatorView navigatorView = (NavigatorView) partialPlanView;
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      boolean areObjectsChanged = false;
      boolean areSlotsChanged = false;
      if (! areNeighborsShown) {
        areObjectsChanged = addTimelineObjects( this, navigatorView);
        areSlotsChanged = addTimelineSlots( this, navigatorView);
        areNeighborsShown = true;
      } else {
        areObjectsChanged = removeTimelineObjects( this, navigatorView);
        areSlotsChanged = removeTimelineSlots( this, navigatorView);
        areNeighborsShown = false;
      }
      if (areObjectsChanged || areSlotsChanged) {
        navigatorView.setLayoutNeeded();
        navigatorView.setFocusNode( this);
        navigatorView.redraw();
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private boolean addTimelineObjects( TimelineNavNode timelineNavNode,
                                      NavigatorView navigatorView) {
    boolean areNodesChanged = navigatorView.addObjectNavNodes( timelineNavNode);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      navigatorView.addObjectToTimelineNavLinks( timelineNavNode);
     boolean areChildLinksChanged =
      navigatorView.addTimelineToObjectNavLinks( timelineNavNode);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addTimelineObjects

  private boolean removeTimelineObjects( TimelineNavNode timelineNavNode,
                                         NavigatorView navigatorView) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      navigatorView.removeObjectToTimelineNavLinks( timelineNavNode);
    boolean areChildLinksChanged =
      navigatorView.removeTimelineToObjectNavLinks( timelineNavNode);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged = navigatorView.removeObjectNavNodes( timelineNavNode);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeTimelineObjects

  private boolean addTimelineSlots( TimelineNavNode timelineNavNode,
                                    NavigatorView navigatorView) {
    boolean areNodesChanged = navigatorView.addSlotNavNodes( timelineNavNode);
    boolean areLinksChanged = navigatorView.addTimelineToSlotNavLinks( timelineNavNode);
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addTimelineSlots

  private boolean removeTimelineSlots( TimelineNavNode timelineNavNode,
                                       NavigatorView navigatorView) {
    boolean areLinksChanged = navigatorView.removeTimelineToSlotNavLinks( timelineNavNode);
    boolean areNodesChanged = navigatorView.removeSlotNavNodes( timelineNavNode);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeTimelineSlots


} // end class TimelineNavNode
