// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TimelineNavNode.java,v 1.6 2004-02-25 02:30:16 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 06jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
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
public class TimelineNavNode extends TimelineNode implements NavNode {

  private PwTimeline timeline;
  private NavigatorView navigatorView;

  private boolean areNeighborsShown;
  private int linkCount;
  private boolean inLayout;
  private boolean isDebugPrint;

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
    this.timeline = timeline;
    navigatorView = (NavigatorView) partialPlanView;
    isDebugPrint = false;
    // isDebugPrint = true;

    inLayout = false;
    areNeighborsShown = false;
    linkCount = 0;
  } // end constructor

  /**
   * <code>getId</code> - implements NavNode
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getId() {
    return timeline.getId();
  }

  /**
   * <code>getTypeName</code> - implements NavNode
   *
   * @return - <code>String</code> - 
   */
  public final String getTypeName() {
    return "timeline";
  }

  /**
   * <code>incrLinkCount</code> - implements NavNode
   *
   */
  public final void incrLinkCount() {
    linkCount++;
  }

  /**
   * <code>decLinkCount</code> - implements NavNode
   *
   */
  public final void decLinkCount() {
    linkCount--;
  }

  /**
   * <code>getLinkCount</code> - implements NavNode
   *
   * @return - <code>int</code> - 
   */
  public final int getLinkCount() {
    return linkCount;
  }

  /**
   * <code>inLayout</code> - implements NavNode
   *
   * @return - <code>boolean</code> - 
   */
  public final boolean inLayout() {
    return inLayout;
  }

  /**
   * <code>setInLayout</code> - implements NavNode
   *
   * @param value - <code>boolean</code> - 
   */
  public final void setInLayout( final boolean value) {
    int width = 1;
    inLayout = value;
    if (value == false) {
      setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
      areNeighborsShown = false;
    }
  }

  /**
   * <code>resetNode</code> - implements NavNode
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public final void resetNode( final boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (linkCount != 0)) {
      System.err.println( "reset timeline node: " + timeline.getId() +
                          "; linkCount != 0: " + linkCount);
    }
    linkCount = 0;
  } // end resetNode

  /**
   * <code>setAreNeighborsShown</code> - implements NavNode
   *
   * @param value - <code>boolean</code> - 
   */
  public final void setAreNeighborsShown( final boolean value) {
    areNeighborsShown = value;
  }

  /**
   * <code>getParentEntityList</code> - implements NavNode
   *
   * @return - <code>List</code> - of PwEntity
   */
  public final List getParentEntityList() {
    List returnList = new ArrayList();
    if (timeline.getParent() != null) {
      returnList.add( timeline.getParent());
    }
    return returnList;
  }

  /**
   * <code>getComponentEntityList</code> - implements NavNode
   *
   * @return - <code>List</code> - of PwEntity (PwObject & PwSlot)
   */
  public final List getComponentEntityList() {
    List returnList = new ArrayList();
    returnList.addAll( timeline.getComponentList());
    returnList.addAll( timeline.getSlotList());
    return returnList;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getToolTipText() {
    String operation = "";
    if (areNeighborsShown) {
      operation = "close";
    } else {
      operation = "open";
    }
    StringBuffer tip = new StringBuffer( "<html>timeline<br>");
    if (isDebug) {
      tip.append( " linkCnt ").append( String.valueOf( linkCount));
      tip.append( "<br>");
    }
    tip.append( "Mouse-L: ").append( operation);
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview timeline node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public final String getToolTipText( final boolean isOverview) {
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
  public final boolean doMouseClick( final int modifiers, final Point dc, final Point vc,
                                     final JGoView view) {
    JGoObject obj = view.pickDocObject( dc, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    TimelineNavNode timelineNode = (TimelineNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      NavigatorView navigatorView = (NavigatorView) partialPlanView;
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      boolean areObjectsChanged = false;
      if (! areNeighborsShown) {
        areObjectsChanged = addTimelineObjects( this);
        areNeighborsShown = true;
      } else {
        areObjectsChanged = removeTimelineObjects( this);
        areNeighborsShown = false;
      }
      if (areObjectsChanged) {
        navigatorView.setLayoutNeeded();
        navigatorView.setFocusNode( this);
        navigatorView.redraw();
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private boolean addTimelineObjects( final TimelineNavNode timelineNavNode) {
    boolean areNodesChanged =
      NavNodeGenerics.addEntityNavNodes( timelineNavNode, navigatorView, isDebugPrint);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.addParentToEntityNavLinks( timelineNavNode, navigatorView, isDebugPrint);
     boolean areChildLinksChanged =
       NavNodeGenerics.addEntityToChildNavLinks( timelineNavNode, navigatorView, isDebugPrint);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addTimelineObjects

  private boolean removeTimelineObjects( final TimelineNavNode timelineNavNode) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.removeParentToEntityNavLinks( timelineNavNode, navigatorView,
                                                    isDebugPrint);
    boolean areChildLinksChanged =
      NavNodeGenerics.removeEntityToChildNavLinks( timelineNavNode, navigatorView,
                                                   isDebugPrint);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged =
      NavNodeGenerics.removeEntityNavNodes( timelineNavNode, navigatorView, isDebugPrint);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeTimelineObjects


} // end class TimelineNavNode
