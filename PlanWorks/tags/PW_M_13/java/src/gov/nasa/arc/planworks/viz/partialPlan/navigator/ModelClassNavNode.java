// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ModelClassNavNode.java,v 1.9 2004-02-26 19:02:00 taylor Exp $
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
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
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
public class ModelClassNavNode extends ObjectNode implements NavNode {

  private PwObject object;
  private NavigatorView navigatorView;

  private boolean areNeighborsShown;
  private int linkCount;
  private boolean inLayout;
  private boolean hasSingleTimeline;
  private boolean isDebugPrint;

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

    this.object = object;
    navigatorView = (NavigatorView) partialPlanView;

    StringBuffer labelBuf = new StringBuffer( object.getName());
    labelBuf.append( "\nkey=").append( object.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "ModelClassNavNode: " + nodeLabel);
    hasSingleTimeline = false;
    if (object.getComponentList().size() == 1) {
      hasSingleTimeline = true;
    }
    isDebugPrint = false;
    // isDebugPrint = true;

    inLayout = false;
    areNeighborsShown = false;
    linkCount = 0;

    if (hasSingleTimeline) {
      setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor("black")));
    }
  } // end constructor

  /**
   * <code>getId</code> - implements NavNode
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getId() {
    return object.getId();
  }

  /**
   * <code>getTypeName</code> - implements NavNode
   *
   * @return - <code>String</code> - 
   */
  public final String getTypeName() {
    return "object";
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
      if (hasSingleTimeline) {
        width = 2;
      }
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
      System.err.println( "reset object node: " + object.getId() +
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
    if (object.getParent() != null) {
      returnList.add( object.getParent());
    }
    return returnList;
  }

  /**
   * <code>getComponentEntityList</code> - implements NavNode
   *
   * @return - <code>List</code> - of PwEntity
   */
  public final List getComponentEntityList() {
    List returnList = new ArrayList();
    returnList.addAll( object.getComponentList());
    returnList.addAll( ((PwVariableContainer) object).getVariables());
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
    StringBuffer tip = new StringBuffer( "<html>object<br>");
    if (isDebugPrint) {
      tip.append( " linkCnt ").append( String.valueOf( linkCount));
      tip.append( "<br>");
    }
    if (! hasSingleTimeline) {
      tip.append( "Mouse-L: ").append( operation);
    }
    return tip.append( "</html>").toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview object node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public final String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>");
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
  public final boolean doMouseClick( final int modifiers, final Point dc, final Point vc,
                                     final JGoView view) {
    JGoObject obj = view.pickDocObject( dc, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    ModelClassNavNode objectNode = (ModelClassNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (! hasSingleTimeline) {
         NavigatorView navigatorView = (NavigatorView) partialPlanView;
         navigatorView.setStartTimeMSecs( System.currentTimeMillis());
         boolean areObjectsChanged = false;
        if (! areNeighborsShown) {
          areObjectsChanged = addObjects( this);
          areNeighborsShown = true;
        } else {
          areObjectsChanged = removeObjects( this);
          areNeighborsShown = false;
        }
        if (areObjectsChanged) {
          navigatorView.setLayoutNeeded();
          navigatorView.setFocusNode( this);
          navigatorView.redraw();
        }
        return true;
      }
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private boolean addObjects( final ModelClassNavNode objectNode) {
    boolean areNodesChanged =
      NavNodeGenerics.addEntityNavNodes( objectNode, navigatorView, isDebugPrint);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.addParentToEntityNavLinks( objectNode, navigatorView, isDebugPrint);
    boolean areChildLinksChanged =
      NavNodeGenerics.addEntityToChildNavLinks( objectNode, navigatorView, isDebugPrint);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addObjectObjects

  private boolean removeObjects( final ModelClassNavNode objectNode) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.removeParentToEntityNavLinks( objectNode, navigatorView, isDebugPrint);
    boolean areChildLinksChanged =
      NavNodeGenerics.removeEntityToChildNavLinks( objectNode, navigatorView, isDebugPrint);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged =
      NavNodeGenerics.removeEntityNavNodes( objectNode, navigatorView, isDebugPrint);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeObjectObjects


} // end class ModelClassNavNode
