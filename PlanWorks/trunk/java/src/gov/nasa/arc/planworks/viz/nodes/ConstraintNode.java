// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNode.java,v 1.3 2003-07-30 23:56:00 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 29july03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDrawable;
import com.nwoods.jgo.JGoEllipse;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPolygon;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/com/nwoods/jgo/examples/Diamond.class
import com.nwoods.jgo.examples.Diamond;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.VizView;


/**
 * <code>ConstraintNode</code> - JGo widget to render a variable constraint with a
 *                          label 
 *             Object->JGoObject->JGoArea->TextNode->ConstraintNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNode extends BasicNode {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private PwConstraint constraint;
  private VariableNode variableNode;
  private int objectCnt;
  private VizView view;
  private String nodeLabel;
  private List variableNodeList;
  private boolean isDiamond = true;

  public ConstraintNode( PwConstraint constraint, VariableNode variableNode,
                         Point constraintLocation, int objectCnt, VizView view) { 
    super();
    this.constraint = constraint;
    this.variableNode = variableNode;
    this.objectCnt = objectCnt;
    this.view = view;
    variableNodeList = new ArrayList();
    variableNodeList.add( variableNode);
    // debug
    // nodeLabel = constraint.getType().substring( 0, 1) + "_" +
    //   constraint.getKey().toString();
    nodeLabel = constraint.getName();
    // System.err.println( "ConstraintNode: " + nodeLabel);

    configure( constraintLocation);
  } // end constructor

  private final void configure( Point constraintLocation) {
    setLabelSpot( JGoObject.Center);
    initialize( constraintLocation, nodeLabel);
    String backGroundColor = null;
    backGroundColor = ((objectCnt % 2) == 0) ?
      ViewConstants.EVEN_OBJECT_SLOT_BG_COLOR :
      ViewConstants.ODD_OBJECT_SLOT_BG_COLOR;
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    setDraggable( false);
    // do not allow user links
    getPort().setVisible( false);
  } // end configure


  // extend BasicNode to use Diamond

  public JGoDrawable createDrawable() {
    JGoDrawable d;
    if (isRectangular())
      d = new JGoRectangle();
    else if (isDiamond)
      d = new Diamond();
    else
      d = new JGoEllipse();
    d.setSelectable(false);
    d.setDraggable(false);
    d.setSize(20, 20);
    return d;
  } // end createDrawable
  

  /**
   * <code>equals</code>
   *
   * @param node - <code>ConstraintNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( ConstraintNode node) {
    return (this.getConstraint().getKey().equals( node.getConstraint().getKey()));
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
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    return constraint.getType();
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
    if (variableNodeList.indexOf( variableNode) == -1) {
      variableNodeList.add( variableNode);
    }
  }


} // end class ConstraintNode
