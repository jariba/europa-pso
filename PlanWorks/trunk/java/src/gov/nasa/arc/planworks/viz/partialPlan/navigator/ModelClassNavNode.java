// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ModelClassNavNode.java,v 1.7 2004-02-17 22:24:35 miatauro Exp $
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
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.ObjectNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>ModelClassNavNode</code> - JGo widget to render a plan object (class model)
 *                                   and its neighbors for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ModelClassNavNode extends ObjectNode {

  private boolean areNeighborsShown;
  private int timelineLinkCount;
  private boolean inLayout;
  private boolean hasSingleTimeline;

  /**
   * <code>ModelClassNavNode</code> - constructor 
   *
   * @param object - <code>PwObject</code> - 
   * @param objectLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public ModelClassNavNode( final PwObject object, final Point objectLocation, 
                            final Color backgroundColor, final boolean isDraggable, 
                            final PartialPlanView partialPlanView) { 
    super(object, objectLocation, backgroundColor, isDraggable, partialPlanView);

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( object.getName());
    labelBuf.append( "\nkey=").append( object.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "ModelClassNavNode: " + nodeLabel);
    hasSingleTimeline = false;
    if (object.getComponentList().size() == 1) {
      hasSingleTimeline = true;
    }

    inLayout = false;
    areNeighborsShown = false;
    timelineLinkCount = 0;

    if(hasSingleTimeline) {
      setPen(new JGoPen(JGoPen.SOLID, 2, ColorMap.getColor("black")));
    }
  } // end constructor

  /**
   * <code>inLayout</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean inLayout() {
    return inLayout;
  }

  /**
   * <code>setInLayout</code>
   *
   * @param value - <code>boolean</code> - 
   */
  public void setInLayout( boolean value) {
    int width = 1;
    inLayout = value;
    if (value == false) {
      if (hasSingleTimeline) {
        width = 2;
      }
      setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
      areNeighborsShown = false;
    }
  }

  /**
   * <code>incrTimelineLinkCount</code>
   *
   */
  public void incrTimelineLinkCount() {
    timelineLinkCount++;
  }

  /**
   * <code>decTimelineLinkCount</code>
   *
   */
  public void decTimelineLinkCount() {
    timelineLinkCount--;
  }

  /**
   * <code>getTimelineLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTimelineLinkCount() {
    return timelineLinkCount;
  }

  /**
   * <code>resetNode</code> - when closed by token close traversal
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (timelineLinkCount != 0)) {
      System.err.println( "reset object node: " + object.getId() +
                          "; timelineLinkCount != 0: " + timelineLinkCount);
    }
    timelineLinkCount = 0;
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
    StringBuffer tip = new StringBuffer( "<html>object<br>");
    if (isDebug) {
      tip.append( " linkCnt ").append( String.valueOf( timelineLinkCount));
      tip.append( "<br>");
    }
    if (! hasSingleTimeline) {
      tip.append( "Mouse-L: ").append( operation);
    }
    return tip.append("</html>").toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview object node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>object<br>");
    tip.append( object.getName());
    tip.append( "<br>key=");
    tip.append( object.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText


  /**
   * <code>doMouseClick</code> - For Navigator View, Mouse-left opens/closes
   *            objectNode to show timelineNodes 
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
    ModelClassNavNode objectNode = (ModelClassNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (! hasSingleTimeline) {
        ((NavigatorView) partialPlanView).setStartTimeMSecs( System.currentTimeMillis());
        if (! areNeighborsShown) {
          //System.err.println( "doMouseClick: Mouse-L show object nodes of object id " +
          //                    objectNode.getObject().getId());
          addObjectTimelines( this, (NavigatorView) partialPlanView);
          areNeighborsShown = true;
        } else {
          //System.err.println( "doMouseClick: Mouse-L hide timeline nodes of object id " +
          //                    objectNode.getObject().getId());
          removeObjectTimelines( this, (NavigatorView) partialPlanView);
          areNeighborsShown = false;
        }
        return true;
      }
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private void addObjectTimelines( ModelClassNavNode objectNode,
                                   NavigatorView navigatorView) {
    boolean areNodesChanged = navigatorView.addTimelineNavNodes( objectNode);
    boolean areLinksChanged =
      navigatorView.addObjectToTimelineNavLinks( objectNode);
    if (areNodesChanged || areLinksChanged) {
      navigatorView.setLayoutNeeded();
      navigatorView.setFocusNode( objectNode);
      navigatorView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  } // end addObjectTimelines

  private void removeObjectTimelines( ModelClassNavNode objectNode,
                                      NavigatorView navigatorView) {
    boolean areLinksChanged =
      navigatorView.removeObjectToTimelineNavLinks( objectNode);
    boolean areNodesChanged = navigatorView.removeTimelineNavNodes( objectNode);
    if (areNodesChanged || areLinksChanged) {
      navigatorView.setLayoutNeeded();
      navigatorView.setFocusNode( objectNode);
      navigatorView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end removeObjectTimelines


} // end class ModelClassNavNode
