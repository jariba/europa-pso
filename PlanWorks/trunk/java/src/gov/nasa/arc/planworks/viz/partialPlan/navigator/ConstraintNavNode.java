// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNavNode.java,v 1.3 2004-02-13 00:26:24 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- started 14jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.Color;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>ConstraintNavNode</code> - JGo widget to render a plan constraint and its neighbors
 *                                   for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNavNode extends ExtendedBasicNode {

  private PwConstraint constraint;
  private List variableList; // element PwVariable
  private PwVariable variable;
  private NavigatorView navigatorView;
  private String nodeLabel;
  private boolean isDebug;
  private boolean areNeighborsShown;
  private int variableLinkCount;
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
  public ConstraintNavNode( PwConstraint constraint, Point constraintLocation,
                            Color backgroundColor, boolean isDraggable,
                            PartialPlanView partialPlanView) { 
    super( ViewConstants.DIAMOND);
    this.constraint = constraint;
    variableList =  constraint.getVariablesList();
    variable = (PwVariable) variableList.get( 0);
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
    areNeighborsShown = false;
    variableLinkCount = 0;

    configure( constraintLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( Point constraintLocation, Color backgroundColor,
                                boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( constraintLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
    if (isUnaryConstraint) {
      setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    }
  } // end configure

  /**
   * <code>equals</code>
   *
   * @param node - <code>ConstraintNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( ConstraintNavNode node) {
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
      if (isUnaryConstraint) {
        width = 2;
      }
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
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    return constraint.getId().toString();
  }

  /**
   * <code>incrVariableLinkCount</code>
   *
   */
  public void incrVariableLinkCount() {
    variableLinkCount++;
  }

  /**
   * <code>decVariableLinkCount</code>
   *
   */
  public void decVariableLinkCount() {
    variableLinkCount--;
  }

  /**
   * <code>getVariableLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getVariableLinkCount() {
    return variableLinkCount;
  }

  /**
   * <code>resetNode</code> - when closed 
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (variableLinkCount != 0)) {
      System.err.println( "reset variable node: " + variable.getId() +
                          "; variableLinkCount != 0: " + variableLinkCount);
    }
    variableLinkCount = 0;
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
    if (! isUnaryConstraint) {
      StringBuffer tip = new StringBuffer( "<html> ");
      tip.append( constraint.getType());
      if (isDebug) {
        tip.append( " linkCntVariable ").append( String.valueOf( variableLinkCount));
      }
      tip.append( "<br> Mouse-L: ").append( operation);
      tip.append("</html>");
      return tip.toString();
    } else {
      return constraint.getType();
    }
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview constraint node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( constraint.getName());
    tip.append( "<br>key=");
    tip.append( constraint.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText


  /**
   * <code>addConstraintNavNode</code>
   *
   */
  protected void addConstraintNavNode() {
    if (isDebug) {
      System.err.println( "add constraintNavNode " + constraint.getId());
    }
    if (! inLayout()) {
      inLayout = true;
    }
  } // end addConstraintNavNode

  /**
   * <code>removeConstraintNavNode</code>
   *
   */
  protected void removeConstraintNavNode() {
    if (isDebug) {
      System.err.println( "remove constraintNavNode " + constraint.getId());
    }
    inLayout = false;
    resetNode( isDebug);
  } // end removeConstraintNavNode


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
  public boolean doMouseClick( int modifiers, Point dc, Point vc, JGoView view) {
    JGoObject obj = view.pickDocObject( dc, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    ConstraintNavNode constraintNavNode = (ConstraintNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      boolean areVariablesChanged = false;
      boolean areConstraintsChanged = false;
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      if (! areNeighborsShown) {
        areVariablesChanged = addConstraintVariables();
        areNeighborsShown = true;
      } else {
        areVariablesChanged = removeConstraintVariables();
        areNeighborsShown = false;
      }
      if (areVariablesChanged) {
        navigatorView.setLayoutNeeded();
        navigatorView.setFocusNode( this);
        navigatorView.redraw();
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private boolean addConstraintVariables() {
    boolean areNodesChanged = addVariableNavNodes();
    boolean areLinksChanged = addVariableToConstraintNavLinks();
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addConstraintVariables

  private boolean removeConstraintVariables() {
    boolean areLinksChanged = removeVariableToConstraintNavLinks();
    boolean areNodesChanged = removeVariableNavNodes();
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeConstraintVariables

  /**
   * <code>addVariableNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addVariableNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    Iterator variableItr = variableList.iterator();
    while (variableItr.hasNext()) {
      PwVariable variable = (PwVariable) variableItr.next();
      VariableNavNode variableNavNode =
        (VariableNavNode) navigatorView.variableNavNodeMap.get( variable.getId());
      if (variableNavNode == null) {
        Color nodeColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
        //THIS NEEDS TO CHANGE!!!
        PwToken token = (PwToken) variable.getTokenList().get( 0);
        if (! token.isFreeToken()) {
          PwTimeline timeline =
            navigatorView.getPartialPlan().getTimeline( token.getTimelineId());
          nodeColor = navigatorView.getTimelineColor( timeline.getId());
        }
        variableNavNode =
          new VariableNavNode( variable, 
                            new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                       ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                            nodeColor, isDraggable, navigatorView);
        navigatorView.variableNavNodeMap.put( variable.getId(), variableNavNode);
        navigatorView.getJGoDocument().addObjectAtTail( variableNavNode);
      }
      variableNavNode.addVariableNavNode();
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addVariableNavNodes

  private boolean removeVariableNavNodes() {
    boolean areNodesChanged = false;
    Iterator variableItr = variableList.iterator();
    while (variableItr.hasNext()) {
      PwVariable variable = (PwVariable) variableItr.next();
      VariableNavNode variableNavNode =
        (VariableNavNode) navigatorView.variableNavNodeMap.get( variable.getId());
      if ((variableNavNode != null) && variableNavNode.inLayout() &&
          (variableNavNode.getTokenLinkCount() == 0) &&
          (variableNavNode.getConstraintLinkCount() == 0)) {
        variableNavNode.removeVariableNavNode();
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeVariableNavNodes


  /**
   * <code>addVariableToConstraintNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addVariableToConstraintNavLinks() {
    boolean areLinksChanged = false;
    Iterator variableItr = variableList.iterator();
    while (variableItr.hasNext()) {
      PwVariable variable = (PwVariable) variableItr.next();
      VariableNavNode variableNavNode =
        (VariableNavNode) navigatorView.variableNavNodeMap.get( variable.getId());
      if ((variableNavNode != null) && variableNavNode.inLayout()) {
        if (navigatorView.addNavigatorLink( variableNavNode, this, this)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // addVariableToConstraintNavLinks

  private boolean removeVariableToConstraintNavLinks() {
    boolean areLinksChanged = false;
    Iterator variableItr = variableList.iterator();
    while (variableItr.hasNext()) {
      PwVariable variable = (PwVariable) variableItr.next();
      VariableNavNode variableNavNode =
        (VariableNavNode) navigatorView.variableNavNodeMap.get( variable.getId());
      if ((variableNavNode != null) && variableNavNode.inLayout()) {
        String linkName = variableNavNode.getVariable().getId().toString() + "->" +
          constraint.getId().toString();
        BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
        if ((link != null) && link.inLayout() &&
            variableNavNode.removeVariableToConstraintNavLink( link, this)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end removeVariableToConstraintNavLinks


} // end class ConstraintNavNode

