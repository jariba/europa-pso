// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNode.java,v 1.13 2003-08-12 22:54:45 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- started 20june03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
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
  private int objectCnt;
  private boolean isFreeToken;
  private String viewName;
  private VizView vizView;
  private String predicateName;
  private String nodeLabel;
  private List variableNodeList; // element VariableNode

  /**
   * <code>TokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param objectCnt - <code>int</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param viewName - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TokenNode( PwToken token, Point tokenLocation, int objectCnt, boolean isFreeToken,
                    boolean isDraggable, String viewName, VizView vizView) {
    super();
    this.token = token;
    this.tokenLocation = tokenLocation;
    this.objectCnt = objectCnt;
    this.isFreeToken = isFreeToken;
    this.viewName = viewName;
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
    configure( tokenLocation, isDraggable);
  } // end constructor

  public TokenNode() {
    super();
  }
  private final void configure( Point tokenLocation, boolean isDraggable) {
    boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( tokenLocation, nodeLabel, isRectangular);
    String backGroundColor = null;
    if (isFreeToken) {
      backGroundColor = ViewConstants.FREE_TOKEN_BG_COLOR;
    } else {
      backGroundColor = ((objectCnt % 2) == 0) ?
        ViewConstants.EVEN_OBJECT_SLOT_BG_COLOR :
        ViewConstants.ODD_OBJECT_SLOT_BG_COLOR;
    }
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
  } // end configure

  public String toString() {
    return token.getId().toString();
  }

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
   * <code>getObjectCnt</code>
   *
   * @return - <code>int</code> - 
   */
  public int getObjectCnt() {
    return objectCnt;
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
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    return token.toString();
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
   * <code>setVariableNodeList</code>
   *
   * @param nodeList - <code>List</code> - of VariableNode
   */
  public void setVariableNodeList( List nodeList) {
    this.variableNodeList = nodeList;
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
   * <code>doMouseClick</code> - For Constraint Network View, Mouse-left opens
   *            tokenNode to show variableNodes and constraintNodes
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point dc, Point vc, JGoView view) {
    if (viewName.equals( "constraintNetworkView")) {
      JGoObject obj = view.pickDocObject( dc, false);
//         System.err.println( "doMouseClick obj class " +
//                             obj.getTopLevelObject().getClass().getName());
      TokenNode tokenNode = (TokenNode) obj.getTopLevelObject();
      System.err.println( "doMouseClick: token predicate name " +
                          tokenNode.getPredicateName());
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        addTokenNodeVariablesConstraints( tokenNode);
        return true;
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        removeTokenNodeVariablesConstraints( tokenNode);
        return true;
      }
    }
    return false;
  } // end doMouseClick   

  private void addTokenNodeVariablesConstraints( TokenNode tokenNode) {
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) tokenNode.getVizView();
    constraintNetworkView.createVariableAndConstraintNodes( tokenNode);
    constraintNetworkView.createTokenVariableConstraintLinks( tokenNode);
    constraintNetworkView.redraw();
  } // end addTokenNodeVariablesConstraints

  private void removeTokenNodeVariablesConstraints( TokenNode tokenNode) {
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) tokenNode.getVizView();
    constraintNetworkView.removeTokenVariableConstraintLinks( tokenNode);
    constraintNetworkView.removeVariableAndConstraintNodes( tokenNode);
    constraintNetworkView.redraw();
  } // end addTokenNodeVariablesConstraints


} // end class TokenNode
