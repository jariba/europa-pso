// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableNavNode.java,v 1.14 2004-08-14 01:39:17 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 13jan04
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
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.IncrementalNode;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>VariableNavNode</code> - JGo widget to render a plan variable and its neighbors
 *                                   for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VariableNavNode extends ExtendedBasicNode
  implements IncrementalNode, OverviewToolTip {

  private PwVariable variable;
  private NavigatorView navigatorView;
  private String nodeLabel;
  private boolean isDebug;
  private boolean hasZeroConstraints;
  private int linkCount;
  private boolean inLayout;

  /**
   * <code>VariableNavNode</code> - constructor 
   *
   * @param variable - <code>PwVariable</code> - 
   * @param variableLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public VariableNavNode( final PwVariable variable, final Point variableLocation,
                          final Color backgroundColor, final boolean isDraggable,
                          final PartialPlanView partialPlanView) { 
    super( ViewConstants.PINCHED_RECTANGLE);
    // super( ViewConstants.ELLIPSE);
    this.variable = variable;
    navigatorView = (NavigatorView) partialPlanView;

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( variable.getDomain().toString());
    labelBuf.append( "\nkey=").append( variable.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "VariableNavNode: " + nodeLabel);

    inLayout = false;
    setAreNeighborsShown( false);
    hasZeroConstraints = true;
    if (variable.getConstraintList().size() > 0) {
      hasZeroConstraints = false;
    }
    linkCount = 0;

    configure( variableLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( final Point variableLocation, final Color backgroundColor,
                                final boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( variableLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
    if (hasZeroConstraints) {
      setAreNeighborsShown( true);
      int penWidth = navigatorView.getOpenJGoPenWidth( navigatorView.getZoomFactor());
      setPen( new JGoPen( JGoPen.SOLID, penWidth,  ColorMap.getColor( "black")));
    }
  } // end configure

  /**
   * <code>getId</code> - implements IncrementalNode
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getId() {
    return variable.getId();
  }

  /**
   * <code>getTypeName</code> - implements IncrementalNode
   *
   * @return - <code>String</code> - 
   */
  public final String getTypeName() {
    return "variable";
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
    if (hasZeroConstraints) {
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
      System.err.println( "reset variable node: " + variable.getId() +
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
    PwVariableContainer variableContainer = variable.getParent();
    if (variableContainer instanceof PwTimeline) {
      returnList.add( (PwTimeline) variableContainer);
    } else if (variableContainer instanceof PwToken) {
      returnList.add( (PwToken) variableContainer);
    } else if (variableContainer instanceof PwObject) {
      returnList.add( (PwObject) variableContainer);
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
    returnList.addAll( variable.getConstraintList());
    return returnList;
  }

  /**
   * <code>equals</code>
   *
   * @param node - <code>VariableNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public final boolean equals( final VariableNavNode node) {
    return (this.getVariable().getId().equals( node.getVariable().getId()));
  }

  /**
   * <code>getVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public final PwVariable getVariable() {
    return variable;
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public final String toString() {
    return variable.getId().toString();
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
    StringBuffer tip = new StringBuffer( "<html>");
    boolean isVariableWithConstraints = (! hasZeroConstraints);
    NodeGenerics.getVariableNodeToolTipText( variable, navigatorView, tip);
    if (isDebug) {
      tip.append( " linkCnt ").append( String.valueOf( linkCount));
    }
    if (navigatorView.getZoomFactor() > 1) {
      tip.append( "<br>key=");
      tip.append( variable.getId().toString());
    }
    if (isVariableWithConstraints) {
      tip.append( "<br>Mouse-L: ").append( operation);
    }
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview variable node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public final String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>");
    NodeGenerics.getVariableNodeToolTipText( variable, navigatorView, tip);
    tip.append( "<br>key=");
    tip.append( variable.getId().toString());
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
    VariableNavNode variableNavNode = (VariableNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      boolean areObjectsChanged = false;
      if (! areNeighborsShown()) {
        areObjectsChanged = addVariableObjects( this);
        setAreNeighborsShown( true);
      } else {
        areObjectsChanged = removeVariableObjects( this);
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
   * <code>addVariableObjects</code>
   *
   * @param variableNavNode - <code>VariableNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean addVariableObjects( final VariableNavNode variableNavNode) {
    boolean areNodesChanged =
      NavNodeGenerics.addEntityNavNodes( variableNavNode, navigatorView, isDebug);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.addParentToEntityNavLinks( variableNavNode, navigatorView, isDebug);
     boolean areChildLinksChanged =
       NavNodeGenerics.addEntityToChildNavLinks( variableNavNode, navigatorView, isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    int penWidth = navigatorView.getOpenJGoPenWidth( navigatorView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addVariableObjects

  private boolean removeVariableObjects( final VariableNavNode variableNavNode) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.removeParentToEntityNavLinks( variableNavNode, navigatorView, isDebug);
    boolean areChildLinksChanged =
      NavNodeGenerics.removeEntityToChildNavLinks( variableNavNode, navigatorView, isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged =
      NavNodeGenerics.removeEntityNavNodes( variableNavNode, navigatorView, isDebug);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeVariableObjects

} // end class VariableNavNode

