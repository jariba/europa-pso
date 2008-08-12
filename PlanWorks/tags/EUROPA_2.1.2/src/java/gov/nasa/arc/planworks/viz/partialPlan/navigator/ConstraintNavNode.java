// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNavNode.java,v 1.12 2004-08-14 01:39:15 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 14jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.IncrementalNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>ConstraintNavNode</code> - JGo widget to render a plan constraint and its neighbors
 *                                   for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNavNode extends ExtendedBasicNode
  implements IncrementalNode, OverviewToolTip {

  private PwConstraint constraint;
  private NavigatorView navigatorView;
  private String nodeLabel;
  private boolean isDebug;
  private int linkCount;
  private boolean inLayout;
  private boolean isUnaryConstraint;

  /**
   * <code>ConstraintNavNode</code> - constructor 
   *
   * @param constraint - <code>PwConstraint</code> - 
   * @param constraintLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public ConstraintNavNode( final PwConstraint constraint, final Point constraintLocation,
                            final Color backgroundColor, final boolean isDraggable,
                            final PartialPlanView partialPlanView) { 
    super( ViewConstants.DIAMOND);
    this.constraint = constraint;
    navigatorView = (NavigatorView) partialPlanView;

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( constraint.getName());
    labelBuf.append( "\nkey=").append( constraint.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "ConstraintNavNode: " + nodeLabel);

    inLayout = false;
    isUnaryConstraint = true;
    if (constraint.getVariablesList().size() > 1) {
      isUnaryConstraint = false;
    }
    setAreNeighborsShown( false);
    linkCount = 0;

    configure( constraintLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( final Point constraintLocation, final Color backgroundColor,
                                final boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( constraintLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
    if (isUnaryConstraint) {
      setAreNeighborsShown( true);
      int penWidth = navigatorView.getOpenJGoPenWidth( navigatorView.getZoomFactor());
      setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    }
  } // end configure

  /**
   * <code>getId</code> - implements IncrementalNode
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getId() {
    return constraint.getId();
  }

  /**
   * <code>getTypeName</code> - implements IncrementalNode
   *
   * @return - <code>String</code> - 
   */
  public final String getTypeName() {
    return "constraint";
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
    if (isUnaryConstraint) {
      setAreNeighborsShown( true);
      width = navigatorView.getOpenJGoPenWidth( navigatorView.getZoomFactor());
    }
    setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
  }

  /**
   * <code>resetNode</code> - implements IncrementalNode
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public final void resetNode( final boolean isDbg) {
    setAreNeighborsShown( false);
    if (isDbg && (linkCount != 0)) {
      System.err.println( "reset constraint node: " + constraint.getId() +
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
    returnList.addAll( constraint.getVariablesList());
    return returnList;
  }

  /**
   * <code>getComponentEntityList</code> - implements IncrementalNode
   *
   * @return - <code>List</code> - of PwEntity
   */
  public final List getComponentEntityList() {
    List returnList = new ArrayList();
    return returnList;
  }

  /**
   * <code>equals</code>
   *
   * @param node - <code>ConstraintNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public final boolean equals( final ConstraintNavNode node) {
    return (this.getConstraint().getId().equals( node.getConstraint().getId()));
  }

  /**
   * <code>getConstraint</code>
   *
   * @return - <code>PwConstraint</code> - 
   */
  public PwConstraint getConstraint() {
    return constraint;
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public final String toString() {
    return constraint.getId().toString();
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
    if (! isUnaryConstraint) {
      // StringBuffer tip = new StringBuffer( "<html>constraint <br>");
      StringBuffer tip = new StringBuffer( "<html>");
      tip.append( constraint.getType());
      if (isDebug) {
        tip.append( " linkCntVariable ").append( String.valueOf( linkCount));
      }
      if (navigatorView.getZoomFactor() > 1) {
        tip.append( "<br>");
        tip.append( constraint.getName());
        tip.append( "<br>key=");
        tip.append( constraint.getId().toString());
      }
      tip.append( "<br> Mouse-L: ").append( operation);
      tip.append( "</html>");
      return tip.toString();
    } else {
      return constraint.getType();
    }
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview constraint node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public final String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>");
    tip.append( constraint.getType());
    tip.append( "<br>");
    tip.append( constraint.getName());
    tip.append( "<br>key=");
    tip.append( constraint.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>doMouseClick</code> - For Model Network View, Mouse-left opens/closes
   *            constarintNode to show constraintNodes 
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
    ConstraintNavNode constraintNavNode = (ConstraintNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      boolean areObjectsChanged = false;
      if (! areNeighborsShown()) {
        areObjectsChanged = addConstraintObjects( this);
        setAreNeighborsShown( true);
      } else {
        areObjectsChanged = removeConstraintObjects( this);
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

  protected boolean addConstraintObjects( final ConstraintNavNode constraintNavNode) {
    boolean areNodesChanged =
      NavNodeGenerics.addEntityNavNodes( constraintNavNode, navigatorView, isDebug);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.addParentToEntityNavLinks( constraintNavNode, navigatorView, isDebug);
     boolean areChildLinksChanged =
       NavNodeGenerics.addEntityToChildNavLinks( constraintNavNode, navigatorView, isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    int penWidth = navigatorView.getOpenJGoPenWidth( navigatorView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addConstraintObjects

  private boolean removeConstraintObjects( final ConstraintNavNode constraintNavNode) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.removeParentToEntityNavLinks( constraintNavNode, navigatorView, isDebug);
    boolean areChildLinksChanged =
      NavNodeGenerics.removeEntityToChildNavLinks( constraintNavNode, navigatorView, isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged =
      NavNodeGenerics.removeEntityNavNodes( constraintNavNode, navigatorView, isDebug);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeConstraintObjects


} // end class ConstraintNavNode

