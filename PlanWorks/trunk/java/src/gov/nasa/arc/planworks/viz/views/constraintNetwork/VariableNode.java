// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableNode.java,v 1.3 2003-09-23 19:28:16 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 28july03
//

package gov.nasa.arc.planworks.viz.views.constraintNetwork;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.views.VizView;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;


/**
 * <code>VariableNode</code> - JGo widget to render a token variable with a
 *                          label 
 *             Object->JGoObject->JGoArea->TextNode->VariableNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VariableNode extends BasicNode {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private PwVariable variable;
  private TokenNode tokenNode;
  private boolean isFreeToken;
  private VizView vizView;
  private String nodeLabel;
  private List tokenNodeList; // element TokenNode
  private List constraintNodeList; // element ConstraintNode
  private List variableTokenLinkList; // element BasicNodeLink
  private boolean areNeighborsShown;
  private boolean inLayout;
  private boolean hasZeroConstraints;
  private int tokenLinkCount;
  private int constraintLinkCount;
  private boolean isDebug;
  private boolean hasBeenVisited;

  /**
   * <code>VariableNode</code> - constructor 
   *
   * @param variable - <code>PwVariable</code> - 
   * @param tokenNode - <code>TokenNode</code> - 
   * @param variableLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public VariableNode( PwVariable variable, TokenNode tokenNode, Point variableLocation, 
                       Color backgroundColor, boolean isFreeToken, boolean isDraggable,
                       VizView vizView) { 
    super();
    this.variable = variable;
    this.isFreeToken = isFreeToken;
    this.vizView = vizView;
    tokenNodeList = new ArrayList();
    tokenNodeList.add( tokenNode);
    constraintNodeList = new ArrayList();
    variableTokenLinkList = new ArrayList();

    inLayout = false;
    hasZeroConstraints = true;
    if (variable.getConstraintList().size() > 0) {
      hasZeroConstraints = false;
    }
    hasBeenVisited = false;
    resetNode( false);

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( variable.getDomain().toString());
    labelBuf.append( "\nkey=").append( variable.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "VariableNode: " + nodeLabel);

    configure( variableLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( Point variableLocation, Color backgroundColor,
                                boolean isDraggable) {
    boolean isRectangular = false;
    setLabelSpot( JGoObject.Center);
    initialize( variableLocation, nodeLabel, isRectangular);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
    if (hasZeroConstraints) {
      setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    }
  } // end configure


  /**
   * <code>equals</code>
   *
   * @param node - <code>VariableNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( VariableNode node) {
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
   * <code>getVizView</code>
   *
   * @return - <code>VizView</code> - 
   */
  public VizView getVizView() {
    return vizView;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
      String operation = null;
      if (areNeighborsShown) {
        operation = "close";
      } else {
        operation = "open";
      }
    if ((! hasZeroConstraints) && (vizView instanceof ConstraintNetworkView)) {
      StringBuffer tip = new StringBuffer( "<html> ");
      tip.append( variable.getType());
      if (isDebug) {
        tip.append( " linkCnt ").append( String.valueOf( tokenLinkCount +
                                                         constraintLinkCount));
      }
       tip.append( "<br> Mouse-L: ").append( operation);
       return tip.append(" nearest token links/constraints</html>").toString();
    } else {
      return variable.getType();
    }
  } // end getToolTipText

  /**
   * <code>getTokenNodeList</code>
   *
   * @return - <code>List</code> - of TokenNode
   */
  public List getTokenNodeList() {
    return tokenNodeList;
  }

  /**
   * <code>addTokenNode</code>
   *
   * @param tokenNode - <code>TokenNode</code> - 
   */
  public void addTokenNode( TokenNode tokenNode) {
    if (tokenNodeList.indexOf( tokenNode) == -1) {
      tokenNodeList.add( tokenNode);
    }
  }

  /**
   * <code>getConstraintNodeList</code>
   *
   * @return - <code>List</code> - of ConstraintNode
   */
  public List getConstraintNodeList() {
    return constraintNodeList;
  }

  /**
   * <code>addConstraintNode</code>
   *
   * @param constraintNode - <code>ConstraintNode</code> - 
   */
  public void addConstraintNode( ConstraintNode constraintNode) {
    if (constraintNodeList.indexOf( constraintNode) == -1) {
      constraintNodeList.add( constraintNode);
    }
  }

  /**
   * <code>getVariableTokenLinkList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getVariableTokenLinkList() {
    return variableTokenLinkList;
  }

  /**
   * <code>addLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   */
  public void addLink( BasicNodeLink link) {
    if (variableTokenLinkList.indexOf( link) == -1) {
      variableTokenLinkList.add( link);
    }
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
      if (hasZeroConstraints) {
        width = 2;
      }
      setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
      areNeighborsShown = false;
    }
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
   * <code>getLinkCount</code> - tokenLinkCount + constraintLinkCount
   *
   * @return - <code>int</code> - 
   */
  public int getLinkCount() {
    return tokenLinkCount + constraintLinkCount;
  }

  /**
   * <code>resetNode</code> - when closed by token close traversal
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (constraintLinkCount != 0)) {
      System.err.println( "reset variable node: " + variable.getId() +
                          "; constraintLinkCount != 0: " + constraintLinkCount);
    }
    constraintLinkCount = 0;
    if (isDebug && (tokenLinkCount != 0)) {
      System.err.println( "reset variable node: " + variable.getId() +
                          "; tokenLinkCount != 0: " + tokenLinkCount);
    }
    tokenLinkCount = 0;
  } // end resetNode

  /**
   * <code>setNodeOpen</code>
   *
   */
  public void setNodeOpen() {
    areNeighborsShown = true;
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  }

  /**
   * <code>setNodeOpen</code>
   *
   */
  public void setNodeClosed() {
    areNeighborsShown = false;
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  }

  /**
   * <code>setHasBeenVisited</code>
   *
   * @param value - <code>boolean</code> - 
   */
  public void setHasBeenVisited( boolean value) {
    hasBeenVisited =  value;
  }

  /**
   * <code>hasBeenVisited</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean hasBeenVisited() {
    return hasBeenVisited;
  }

  /**
   * <code>doMouseClick</code> - For Constraint Network View, Mouse-left opens/closes
   *            variableNode to show constraintNodes
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
    VariableNode variableNode = (VariableNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if ((! hasZeroConstraints) && (vizView instanceof ConstraintNetworkView)) {
        if (! areNeighborsShown) {
          //System.err.println
          //  ( "doMouseClick: Mouse-L show constraint/token nodes of variable id " +
          //    variableNode.getVariable().getId());
          addVariableNodeTokensAndConstraints( this);
          setNodeOpen();
        } else {
          //System.err.println
          //  ( "doMouseClick: Mouse-L hide constraint/token nodes of variable id " +
          //    variableNode.getVariable().getId());
          removeVariableNodeTokensAndConstraints( this);
          setNodeClosed();
        }
        ((ConstraintNetworkView) vizView).setFocusNode( variableNode);
        return true;
      }
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private void addVariableNodeTokensAndConstraints( VariableNode variableNode) {
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) variableNode.getVizView();
    boolean areNodesChanged = constraintNetworkView.addConstraintNodes( variableNode);
    boolean areLinksChanged =
      constraintNetworkView.addTokenAndConstraintToVariableLinks( variableNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.redraw();
    }
  } // end addVariableNodeTokensAndConstraints

  private void removeVariableNodeTokensAndConstraints( VariableNode variableNode) {
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) variableNode.getVizView();
    boolean areLinksChanged = constraintNetworkView.removeTokenToVariableLinks( variableNode);
    boolean areNodesChanged = constraintNetworkView.removeConstraintNodes( variableNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.redraw();
    }
  } // end removeVariableNodeTokensAndConstraints




} // end class VariableNode
