// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ModelClassNavNode.java,v 1.14 2004-08-14 01:39:15 taylor Exp $
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
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.nodes.IncrementalNode;
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
public class ModelClassNavNode extends ObjectNode implements IncrementalNode, OverviewToolTip {

  private PwObject object;
  private NavigatorView navigatorView;

  private int linkCount;
  private boolean inLayout;
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
    isDebugPrint = false;
    // isDebugPrint = true;

    inLayout = false;
    setAreNeighborsShown( false);
    linkCount = 0;
  } // end constructor

  /**
   * <code>getId</code> - implements IncrementalNode
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getId() {
    return object.getId();
  }

  /**
   * <code>getTypeName</code> - implements IncrementalNode
   *
   * @return - <code>String</code> - 
   */
  public final String getTypeName() {
    return "object";
  }

  /**
   * <code>incrLinkCount</code> - implements IncrementalNode
   *
   */
  public final void incrLinkCount() {
    linkCount++;
  }

  /**
   * <code>decLinkCount</code> - implements IncrementalNode
   *
   */
  public final void decLinkCount() {
    linkCount--;
  }

  /**
   * <code>getLinkCount</code> - implements IncrementalNode
   *
   * @return - <code>int</code> - 
   */
  public final int getLinkCount() {
    return linkCount;
  }

  /**
   * <code>inLayout</code> - implements IncrementalNode
   *
   * @return - <code>boolean</code> - 
   */
  public final boolean inLayout() {
    return inLayout;
  }

  /**
   * <code>setInLayout</code> - implements IncrementalNode
   *
   * @param value - <code>boolean</code> - 
   */
  public final void setInLayout( final boolean value) {
    int width = 1;
    inLayout = value;
    if (value == false) {
      setAreNeighborsShown( false);
    }
    setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
  }

  /**
   * <code>resetNode</code> - implements IncrementalNode
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public final void resetNode( final boolean isDebug) {
    setAreNeighborsShown( false);
    if (isDebug && (linkCount != 0)) {
      System.err.println( "reset object node: " + object.getId() +
                          "; linkCount != 0: " + linkCount);
    }
    linkCount = 0;
  } // end resetNode

  /**
   * <code>getParentEntityList</code> - implements IncrementalNode
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
   * <code>getComponentEntityList</code> - implements IncrementalNode
   *
   * @return - <code>List</code> - of PwEntity
   */
  public final List getComponentEntityList() {
    List returnList = new ArrayList();
    returnList.addAll( object.getComponentList());
    returnList.addAll( ((PwVariableContainer) object).getVariables());
    returnList.addAll( object.getTokens());
    return returnList;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getToolTipText() {
    String operation = "";
    if (areNeighborsShown()) {
      operation = "close";
    } else {
      operation = "open";
    }
    // StringBuffer tip = new StringBuffer( "<html>object<br>");
    StringBuffer tip = new StringBuffer( "<html>");
    tip.append( object.getName());
    if (partialPlanView.getZoomFactor() > 1) {
      tip.append( "<br>key=");
      tip.append( object.getId().toString());
    }
    if (isDebugPrint) {
      tip.append( "<br>linkCnt ").append( String.valueOf( linkCount));
    }
    tip.append( "<br>Mouse-L: ").append( operation);
    return tip.append( "</html>").toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview object node
   *                               implements OverviewToolTip
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
      NavigatorView navigatorView = (NavigatorView) partialPlanView;
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      boolean areObjectsChanged = false;
      if (! areNeighborsShown()) {
        areObjectsChanged = addObjects( this);
        setAreNeighborsShown( true);
      } else {
        areObjectsChanged = removeObjects( this);
        setAreNeighborsShown( false);
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

  /**
   * <code>addObjects</code>
   *
   * @param objectNode - <code>ModelClassNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean addObjects( final ModelClassNavNode objectNode) {
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
    int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
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
