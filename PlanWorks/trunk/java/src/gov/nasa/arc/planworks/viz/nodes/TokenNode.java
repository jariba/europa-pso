// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNode.java,v 1.18 2003-09-16 19:29:13 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 20june03
//

package gov.nasa.arc.planworks.viz.nodes;

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
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.VizView;
import gov.nasa.arc.planworks.viz.views.constraintNetwork.ConstraintNetworkView;


/**
 * <code>TokenNode</code> - JGo widget to render a token with a
 *                          label consisting of the slot's predicate name.
 *             Object->JGoObject->JGoArea->TextNode->TokenNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNode extends BasicNode {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private PwToken token;
  private Point tokenLocation;
  private boolean isFreeToken;
  private VizView vizView;
  private String predicateName;
  private String nodeLabel;
  private List variableNodeList; // element VariableNode
  private boolean areNeighborsShown;
  private int variableLinkCount;

  /**
   * <code>TokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TokenNode( PwToken token, Point tokenLocation, Color backgroundColor,
                    boolean isFreeToken, boolean isDraggable, VizView vizView) {
    super();
    this.token = token;
    this.tokenLocation = tokenLocation;
    this.isFreeToken = isFreeToken;
    this.vizView = vizView;
    if (token != null) {
      predicateName = token.getPredicate().getName();
    } else {
      predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
    }
    // debug
    // nodeLabel = predicateName + " " + token.getId().toString();
    nodeLabel = predicateName;
    
    // System.err.println( "TokenNode: " + nodeLabel);
    variableNodeList = new ArrayList();
    areNeighborsShown = false;
    variableLinkCount = 0;
    
    configure( tokenLocation, backgroundColor, isDraggable);
  } // end constructor

  public TokenNode() {
    super();
  }
  private final void configure( Point tokenLocation, Color backgroundColor,
                                boolean isDraggable) {
    boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( tokenLocation, nodeLabel, isRectangular);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
  } // end configure

  /**
   * <code>equals</code>
   *
   * @param node - <code>TokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( TokenNode node) {
    return (this.getToken().getId().equals( node.getToken().getId()));
  }

  /**
   * <code>getToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken() {
    return token;
  }

  /**
   * <code>isFreeToken</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isFreeToken() {
    return isFreeToken;
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
   * <code>getPredicateName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getPredicateName() {
    return predicateName;
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    return token.getId().toString();
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
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (vizView instanceof ConstraintNetworkView) {
      String opereration = null;
      if (areNeighborsShown) {
        opereration = "close";
      } else {
        opereration = "open";
      }
      return "<html> " + token.toString() +
        "<br> Mouse-L: " + opereration + " nearest variables</html>";
    } else {
      return token.toString();
    }
  } // end getToolTipText


  /**
   * <code>getVariableNodeList</code>
   *
   * @return - <code>List</code> - of VariableNode
   */
  public List getVariableNodeList() {
    return variableNodeList;
  }

  /**
   * <code>addVariableNode</code>
   *
   * @param variableNode - <code>VariableNode</code> - 
   */
  public void addVariableNode( VariableNode variableNode) {
    variableNodeList.add( variableNode);
  }

  /**
   * <code>doMouseClick</code> - For Constraint Network View, Mouse-left opens/closes
   *            tokenNode to show variableNodes
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
    TokenNode tokenNode = (TokenNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (vizView instanceof ConstraintNetworkView) {
        if (! areNeighborsShown) {
          //System.err.println( "doMouseClick: Mouse-L show variable nodes of " +
          //                    tokenNode.getPredicateName());
          addTokenNodeVariables( this);
          setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
          areNeighborsShown = true;
        } else {
          //System.err.println( "doMouseClick: Mouse-L hide variable nodes of " +
          //                    tokenNode.getPredicateName());
          removeTokenNodeVariables( this);
          setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
          areNeighborsShown = false;
        }
        return true;
      }
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private void addTokenNodeVariables( TokenNode tokenNode) {
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) tokenNode.getVizView();
    boolean areNodesChanged = constraintNetworkView.addVariableNodes( tokenNode);
    boolean areLinksChanged = constraintNetworkView.addVariableToTokenLinks( tokenNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.redraw();
    }
  } // end addTokenNodeVariables

  private void removeTokenNodeVariables( TokenNode tokenNode) {
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) tokenNode.getVizView();
    boolean areLinksChanged = constraintNetworkView.removeVariableToTokenLinks( tokenNode);
    boolean areNodesChanged = constraintNetworkView.removeVariableNodes( tokenNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.redraw();
    }
  } // end adremoveTokenNodeVariables



} // end class TokenNode
