// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableNavNode.java,v 1.5 2004-02-13 18:56:50 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 13jan04
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
public class VariableNavNode extends ExtendedBasicNode {

  private PwVariable variable;
  private List tokenList; // element PwToken
  private PwToken token;
  private NavigatorView navigatorView;
  private String nodeLabel;
  private boolean isDebug;
  private boolean areNeighborsShown;
  private int tokenLinkCount;
  private int constraintLinkCount;
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
  public VariableNavNode( PwVariable variable, Point variableLocation, Color backgroundColor,
                       boolean isDraggable, PartialPlanView partialPlanView) { 
    super( ViewConstants.ELLIPSE);
    this.variable = variable;
    //THIS NEEDS TO CHANGE
    //tokenList =  variable.getTokenList();
    token = (PwToken) variable.getParent();
    navigatorView = (NavigatorView) partialPlanView;

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( variable.getDomain().toString());
    labelBuf.append( "\nkey=").append( variable.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "VariableNavNode: " + nodeLabel);

    inLayout = false;
    areNeighborsShown = false;
    tokenLinkCount = 0;
    constraintLinkCount = 0;

    configure( variableLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( Point variableLocation, Color backgroundColor,
                                boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( variableLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
  } // end configure

  /**
   * <code>equals</code>
   *
   * @param node - <code>VariableNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( VariableNavNode node) {
    return (this.getVariable().getId().equals( node.getVariable().getId()));
  }

  /**
   * <code>getVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getVariable() {
    return variable;
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
    return variable.getId().toString();
  }

  /**
   * <code>incrTokenLinkCount</code>
   *
   */
  public void incrTokenLinkCount() {
    tokenLinkCount++;
  }

  /**
   * <code>decTokenLinkCount</code>
   *
   */
  public void decTokenLinkCount() {
    tokenLinkCount--;
  }

  /**
   * <code>getTokenLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTokenLinkCount() {
    return tokenLinkCount;
  }

  /**
   * <code>incrConstraintLinkCount</code>
   *
   */
  public void incrConstraintLinkCount() {
    constraintLinkCount++;
  }

  /**
   * <code>decConstraintLinkCount</code>
   *
   */
  public void decConstraintLinkCount() {
    constraintLinkCount--;
  }

  /**
   * <code>getConstraintLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getConstraintLinkCount() {
    return constraintLinkCount;
  }

  /**
   * <code>resetNode</code> - when closed 
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (tokenLinkCount != 0)) {
      System.err.println( "reset token node: " + token.getId() +
                          "; tokenLinkCount != 0: " + tokenLinkCount);
    }
    if (isDebug && (constraintLinkCount != 0)) {
      System.err.println( "reset tokennode: " + token.getId() +
                          "; constraintLinkCount != 0: " + constraintLinkCount);
    }
    tokenLinkCount = 0;
    constraintLinkCount = 0;
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
    StringBuffer tip = new StringBuffer( "<html>");
    NodeGenerics.getVariableNodeToolTipText( variable, navigatorView, tip);
    if (isDebug) {
      tip.append( " linkCntToken ").append( String.valueOf( tokenLinkCount));
      tip.append( " linkCntConstraint ").append( String.valueOf( constraintLinkCount));
    }
    tip.append( "<br> Mouse-L: ").append( operation);
    tip.append("</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview variable node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>");
    tip.append( variable.getDomain().toString());
    tip.append( "<br>key=");
    tip.append( variable.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText


  /**
   * <code>addVariableNavNode</code>
   *
   */
  protected void addVariableNavNode() {
    if (isDebug) {
      System.err.println( "add variableNavNode " + variable.getId());
    }
    if (! inLayout()) {
      inLayout = true;
    }
  } // end addVariableNavNode

  /**
   * <code>removeVariableNavNode</code>
   *
   */
  protected void removeVariableNavNode() {
    if (isDebug) {
      System.err.println( "remove variableNavNode " + variable.getId());
    }
    inLayout = false;
    resetNode( isDebug);
  } // end removeVariableNavNode


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
    VariableNavNode variableNavNode = (VariableNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      boolean areTokensChanged = false;
      boolean areConstraintsChanged = false;
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      if (! areNeighborsShown) {
        areTokensChanged = addVariableTokens();
        areConstraintsChanged = addVariableConstraints();
        areNeighborsShown = true;
      } else {
        areTokensChanged = removeVariableTokens();
        areConstraintsChanged = removeVariableConstraints();
        areNeighborsShown = false;
      }
      if (areTokensChanged || areConstraintsChanged) {
        navigatorView.setLayoutNeeded();
        navigatorView.setFocusNode( this);
        navigatorView.redraw();
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private boolean addVariableTokens() {
    boolean areNodesChanged = addTokenNavNodes();
    boolean areLinksChanged = addTokenToVariableNavLinks();
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addVariableTokens

  private boolean removeVariableTokens() {
    boolean areLinksChanged = removeTokenToVariableNavLinks();
    boolean areNodesChanged = removeTokenNavNodes();
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeVariableTokens

  private boolean addVariableConstraints() {
    boolean areNodesChanged = addConstraintNavNodes();
    boolean areLinksChanged = addVariableToConstraintNavLinks();
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addVariableConstraints

  private boolean removeVariableConstraints() {
    boolean areLinksChanged = removeVariableToConstraintNavLinks();
    boolean areNodesChanged = removeConstraintNavNodes();
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeVariableConstraints


  /**
   * <code>addTokenNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addTokenNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    //Iterator tokenItr = tokenList.iterator();
    //while (tokenItr.hasNext()) {
    //PwToken token = (PwToken) tokenItr.next();
    TokenNavNode tokenNavNode =
      (TokenNavNode) navigatorView.tokenNavNodeMap.get( token.getId());
    if (tokenNavNode == null) {
      Color nodeColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
      if (! token.isFreeToken()) {
        PwTimeline timeline =
          navigatorView.getPartialPlan().getTimeline( token.getTimelineId());
        nodeColor = navigatorView.getTimelineColor( timeline.getId());
      }
      tokenNavNode =
        new TokenNavNode( token, 
                          new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                     ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                          nodeColor, isDraggable, navigatorView);
      navigatorView.tokenNavNodeMap.put( token.getId(), tokenNavNode);
      navigatorView.getJGoDocument().addObjectAtTail( tokenNavNode);
    }
    tokenNavNode.addTokenNavNode();
    areNodesChanged = true;
    //}
    return areNodesChanged;
  } // end addTokenNavNodes

  private boolean removeTokenNavNodes() {
    boolean areNodesChanged = false;
    Iterator tokenItr = tokenList.iterator();
    while (tokenItr.hasNext()) {
      PwToken token = (PwToken) tokenItr.next();
      TokenNavNode tokenNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( token.getId());
      if ((tokenNavNode != null) && tokenNavNode.inLayout() &&
          (tokenNavNode.getSlotLinkCount() == 0) &&
          (tokenNavNode.getVariableLinkCount() == 0)) {
        tokenNavNode.removeTokenNavNode();
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeTokenNavNodes


  /**
   * <code>addTokenToVariableNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addTokenToVariableNavLinks() {
    boolean areLinksChanged = false;
    TokenNavNode tokenNavNode =
      (TokenNavNode) navigatorView.tokenNavNodeMap.get( token.getId());
    if ((tokenNavNode != null) && tokenNavNode.inLayout()) {
      if (navigatorView.addNavigatorLink( tokenNavNode, this, this)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // addTokenToVariableNavLinks

  private boolean removeTokenToVariableNavLinks() {
    boolean areLinksChanged = false;
    TokenNavNode tokenNavNode =
      (TokenNavNode) navigatorView.tokenNavNodeMap.get( token.getId());
    if ((tokenNavNode != null) && tokenNavNode.inLayout()) {
      String linkName = tokenNavNode.getToken().getId().toString() + "->" +
        variable.getId().toString();
      BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
      if ((link != null) && link.inLayout() &&
          tokenNavNode.removeTokenToVariableNavLink( link, this)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end removeTokenToVariableNavLinks

  // **********************************************************


  /**
   * <code>addConstraintNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addConstraintNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    Iterator constraintIterator = variable.getConstraintList().iterator();
    while (constraintIterator.hasNext()) {
      PwConstraint constraint = (PwConstraint) constraintIterator.next();
      ConstraintNavNode constraintNavNode =
        (ConstraintNavNode) navigatorView.constraintNavNodeMap.get( constraint.getId());
      if (constraintNavNode == null) {
        Color nodeColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
        if (! token.isFreeToken()) {
          PwTimeline timeline =
            navigatorView.getPartialPlan().getTimeline( token.getTimelineId());
          nodeColor = navigatorView.getTimelineColor( timeline.getId());
        }

        constraintNavNode =
          new ConstraintNavNode( constraint, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                                        ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                               nodeColor, isDraggable, navigatorView);
        navigatorView.constraintNavNodeMap.put( constraint.getId(), constraintNavNode);
        navigatorView.getJGoDocument().addObjectAtTail( constraintNavNode);
      }
      constraintNavNode.addConstraintNavNode();
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addConstraintNavNodes

  /**
   * <code>removeConstraintNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean removeConstraintNavNodes() {
    boolean areNodesChanged = false;
    Iterator constraintIterator = variable.getConstraintList().iterator();
    while (constraintIterator.hasNext()) {
      PwConstraint constraint = (PwConstraint) constraintIterator.next();
      ConstraintNavNode constraintNavNode =
        (ConstraintNavNode) navigatorView.constraintNavNodeMap.get( constraint.getId());
      if ((constraintNavNode != null) && constraintNavNode.inLayout() &&
          (constraintNavNode.getVariableLinkCount() == 0)) {
        constraintNavNode.removeConstraintNavNode();
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeConstraintNavNodes

  /**
   * <code>addVariableToConstraintNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addVariableToConstraintNavLinks() {
    boolean areLinksChanged = false;
    Iterator constraintIterator = variable.getConstraintList().iterator();
    while (constraintIterator.hasNext()) {
      PwConstraint constraint = (PwConstraint) constraintIterator.next();
      ConstraintNavNode constraintNavNode =
        (ConstraintNavNode) navigatorView.constraintNavNodeMap.get( constraint.getId());
      if ((constraintNavNode != null) && constraintNavNode.inLayout()) {
        if (navigatorView.addNavigatorLink( this, constraintNavNode, this)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end addVariableToConstraintNavLinks

  /**
   * <code>addVariableToConstraintNavLink</code>
   *
   * @param constraintNavNode - <code>ConstraintNavNode</code> - 
   * @param sourceNode - <code>ExtendedBasicNode</code> - 
   * @return - <code>BasicNodeLink</code> - 
   */
  protected BasicNodeLink addVariableToConstraintNavLink( ConstraintNavNode constraintNavNode,
                                                          ExtendedBasicNode sourceNode) {
    BasicNodeLink returnLink = null;
    String linkName = variable.getId().toString() + "->" +
      constraintNavNode.getConstraint().getId().toString();
    BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
    if (link == null) {
      link = new BasicNodeLink( this, constraintNavNode, linkName);
      link.setArrowHeads( false, true);
      incrConstraintLinkCount();
      constraintNavNode.incrVariableLinkCount();
      returnLink = link;
      navigatorView.navLinkMap.put( linkName, link);
      if (isDebug) {
        System.err.println( "add variable=>constraint link " + linkName);
      }
    } else {
      if (! link.inLayout()) {
        link.setInLayout( true);
      }
      link.incrLinkCount();
      incrConstraintLinkCount();
      constraintNavNode.incrVariableLinkCount();
      if (isDebug) {
        System.err.println( "VtoC1 incr link: " + link.toString() + " to " +
                            link.getLinkCount());
      }
    }
    return returnLink;
  } // end addVariableToConstraintNavLink


  /**
   * <code>removeVariableToConstraintNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean removeVariableToConstraintNavLinks() {
    boolean areLinksChanged = false;
    Iterator constraintIterator = variable.getConstraintList().iterator();
    while (constraintIterator.hasNext()) {
      PwConstraint constraint = (PwConstraint) constraintIterator.next();
      ConstraintNavNode constraintNavNode =
        (ConstraintNavNode) navigatorView.constraintNavNodeMap.get( constraint.getId());
      if ((constraintNavNode != null) && constraintNavNode.inLayout()) {
        String linkName = variable.getId().toString() + "->" +
          constraintNavNode.getConstraint().getId().toString();
        BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
        if ((link != null) && link.inLayout() &&
            removeVariableToConstraintNavLink( link, constraintNavNode)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end removeVariableToConstraintNavLinks

  /**
   * <code>removeVariableToConstraintNavLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   * @param constraintNavNode - <code>ConstraintNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean removeVariableToConstraintNavLink( BasicNodeLink link,
                                                       ConstraintNavNode constraintNavNode) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    decConstraintLinkCount();
    constraintNavNode.decVariableLinkCount();
    if (isDebug) {
      System.err.println( "CtoV dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebug) {
        System.err.println( "removeVariableToConstraintNavLink: " + link.toString());
      }
      link.setInLayout( false);
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeVariableToConstraintNavLink

} // end class VariableNavNode

