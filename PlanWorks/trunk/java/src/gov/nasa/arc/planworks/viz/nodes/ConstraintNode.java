// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNode.java,v 1.5 2003-08-12 22:54:44 miatauro Exp $
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
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/com/nwoods/jgo/examples/Diamond.class
import com.nwoods.jgo.examples.Diamond;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwConstraint;
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
  private List constraintVariableLinkList;

  /**
   * <code>ConstraintNode</code> - constructor 
   *
   * @param constraint - <code>PwConstraint</code> - 
   * @param variableNode - <code>VariableNode</code> - 
   * @param constraintLocation - <code>Point</code> - 
   * @param objectCnt - <code>int</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param view - <code>VizView</code> - 
   */
  public ConstraintNode( PwConstraint constraint, VariableNode variableNode,
                         Point constraintLocation, int objectCnt, boolean isDraggable,
                         VizView view) { 
    super();
    this.constraint = constraint;
    this.variableNode = variableNode;
    this.objectCnt = objectCnt;
    this.view = view;
    variableNodeList = new ArrayList();
    variableNodeList.add( variableNode);
    constraintVariableLinkList = new ArrayList();
    // debug
    // nodeLabel = constraint.getType().substring( 0, 1) + "_" +
    //   constraint.getKey().toString();
    nodeLabel = constraint.getName();
    // System.err.println( "ConstraintNode: " + nodeLabel);

    configure( constraintLocation, isDraggable);
  } // end constructor

  private final void configure( Point constraintLocation, boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( constraintLocation, nodeLabel);
    // 
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


  // extend BasicNode to use Diamond

  /**
   * <code>initialize</code> - modified from BasicNode to handle Diamond node shape
   *
   * @param loc - <code>Point</code> - 
   * @param labeltext - <code>String</code> - 
   */
  public void initialize(Point loc, String labeltext) {
    // the area as a whole is not directly selectable using a mouse,
    // but the area can be selected by trying to select any of its
    // children, all of whom are currently !isSelectable().
    setSelectable(false);
    setGrabChildSelection(true);
    // the user can move this node around
    setDraggable(true);
    // the user cannot resize this node
    setResizable(false);

    // create the circle/ellipse around and behind the port
    myDrawable = createDrawable();
    // can't setLocation until myDrawable exists
    setLocation(loc);

    // if there is a string, create a label with a transparent
    // background that is centered
    if (labeltext != null) {
      myLabel = createLabel(labeltext);
    }

    // create a Port, which knows how to make sure
    // connected JGoLinks have a reasonable end point
    // myPort = new BasicNodePort();
    myPort = new BasicNodePortWDiamond();
    myPort.setSize(7, 7);
    if (getLabelSpot() == Center) {
      getPort().setStyle(JGoPort.StyleHidden);
    } else {
      getPort().setStyle(JGoPort.StyleEllipse);
    }

    // add all the children to the area
    addObjectAtHead(myDrawable);
    addObjectAtTail(myPort);
    if (myLabel != null) {
      addObjectAtTail(myLabel);
    }
  }


  /**
   * <code>createDrawable</code> - modified from BasicNode to handle Diamond node shape
   *
   * @return - <code>JGoDrawable</code> - 
   */
  public JGoDrawable createDrawable() {
    JGoDrawable d;
    if (isRectangular()) {
      d = new JGoRectangle();
    } else if (isDiamond) {
      d = new Diamond();
    } else {
      d = new JGoEllipse();
    }
    d.setSelectable(false);
    d.setDraggable(false);
    d.setSize(20, 20);
    return d;
  } // end createDrawable

  // end extending BasicNode 


  /**
   * <code>equals</code>
   *
   * @param node - <code>ConstraintNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( ConstraintNode node) {
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

  /**
   * <code>getConstraintVariableLinkList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getConstraintVariableLinkList() {
    return constraintVariableLinkList;
  }

  /**
   * <code>addLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   */
  public void addLink( BasicNodeLink link) {
    if (constraintVariableLinkList.indexOf( link) == -1) {
      constraintVariableLinkList.add( link);
    }
  }


} // end class ConstraintNode
