// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableNode.java,v 1.3 2003-08-06 01:20:15 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 28july03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.VizView;


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
  private int objectCnt;
  private VizView view;
  private String nodeLabel;
  private List tokenNodeList;
  private List constraintNodeList;
  private List variableTokenLinkList;

  /**
   * <code>VariableNode</code> - constructor 
   *
   * @param variable - <code>PwVariable</code> - 
   * @param tokenNode - <code>TokenNode</code> - 
   * @param variableLocation - <code>Point</code> - 
   * @param objectCnt - <code>int</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param view - <code>VizView</code> - 
   */
  public VariableNode( PwVariable variable, TokenNode tokenNode, Point variableLocation, 
                       int objectCnt, boolean isDraggable, VizView view) { 
    super();
    this.variable = variable;
    this.objectCnt = objectCnt;
    this.view = view;
    tokenNodeList = new ArrayList();
    tokenNodeList.add( tokenNode);
    constraintNodeList = new ArrayList();
    variableTokenLinkList = new ArrayList();
    // debug
    // nodeLabel = variable.getType().substring( 0, 1) + "_" + variable.getKey().toString();
    nodeLabel = variable.getDomain().toString();
    // System.err.println( "VariableNode: " + nodeLabel);

    configure( variableLocation, isDraggable);
  } // end constructor

  private final void configure( Point variableLocation, boolean isDraggable) {
    boolean isRectangular = false;
    setLabelSpot( JGoObject.Center);
    initialize( variableLocation, nodeLabel, isRectangular);
    String backGroundColor = null;
    backGroundColor = ((objectCnt % 2) == 0) ?
      ViewConstants.EVEN_OBJECT_SLOT_BG_COLOR :
      ViewConstants.ODD_OBJECT_SLOT_BG_COLOR;
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
  } // end configure


  /**
   * <code>equals</code>
   *
   * @param node - <code>VariableNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( VariableNode node) {
    return (this.getVariable().getKey().equals( node.getVariable().getKey()));
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
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    return variable.getType();
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
   * <code>setConstraintNodeList</code>
   *
   * @param nodeList - <code>List</code> - of ConstraintNode
   */
  public void setConstraintNodeList( List nodeList) {
    constraintNodeList = nodeList;
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

} // end class VariableNode
