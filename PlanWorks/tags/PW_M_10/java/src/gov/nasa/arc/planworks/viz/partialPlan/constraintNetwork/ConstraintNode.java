// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNode.java,v 1.5 2003-11-20 19:11:24 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 29july03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDrawable;
import com.nwoods.jgo.JGoEllipse;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/com/nwoods/jgo/examples/Diamond.class
import com.nwoods.jgo.examples.Diamond;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


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
  private boolean isFreeToken;
  private PartialPlanView partialPlanView;
  private String nodeLabel;
  private List variableNodeList; // element VariableNode
  private boolean isDiamond = true;
  private List constraintVariableLinkList; // element BasicNodeLink
  //fix me.
  private Map constraintVariableLinkMap;
  private boolean areNeighborsShown;
  private boolean inLayout;
  private boolean isUnaryConstraint;
  private int variableLinkCount;
  private boolean isDebug;
  private boolean hasBeenVisited;

  /**
   * <code>ConstraintNode</code> - constructor 
   *
   * @param constraint - <code>PwConstraint</code> - 
   * @param variableNode - <code>VariableNode</code> - 
   * @param constraintLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public ConstraintNode( PwConstraint constraint, VariableNode variableNode,
                         Point constraintLocation, Color backgroundColor,
                         boolean isFreeToken, boolean isDraggable, PartialPlanView partialPlanView) { 
    super();
    this.constraint = constraint;
    this.variableNode = variableNode;
    this.isFreeToken = isFreeToken;
    this.partialPlanView = partialPlanView;
    variableNodeList = new ArrayList();
    variableNodeList.add( variableNode);
    constraintVariableLinkList = new ArrayList();
    constraintVariableLinkMap = new HashMap();

    inLayout = false;
    isUnaryConstraint = true;
    if (constraint.getVariablesList().size() > 1) {
      isUnaryConstraint = false;
    }
    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( constraint.getName());
    labelBuf.append( "\nkey=").append( constraint.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "ConstraintNode: " + nodeLabel);

    hasBeenVisited = false;
    resetNode( false);

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
   * <code>getPartialPlanView</code>
   *
   * @return - <code>PartialPlanView</code> - 
   */
  public PartialPlanView getPartialPlanView() {
    return partialPlanView;
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
   * <code>getLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getLinkCount() {
    return variableLinkCount;
  }

  /**
   * <code>resetNode</code> - when closed by token close traversal
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (variableLinkCount != 0)) {
      System.err.println( "reset constraint node: " + constraint.getId() +
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
      String operation = null;
      if (areNeighborsShown) {
        operation = "close";
      } else {
        operation = "open";
      }
    if ((! isUnaryConstraint)  && (partialPlanView instanceof ConstraintNetworkView)) {
      StringBuffer tip = new StringBuffer( "<html> ");
      tip.append( constraint.getType());
      if (isDebug) {
        tip.append( " linkCnt ").append( String.valueOf( variableLinkCount));
      }
       tip.append( "<br> Mouse-L: ").append( operation);
       return tip.append(" nearest token variables</html>").toString();
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
      constraintVariableLinkMap.put(link.getToNode(), link);
    }
  }

  public BasicNodeLink getLinkToNode(BasicNode node) {
    return (BasicNodeLink) constraintVariableLinkMap.get(node);
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
   *            constarintNode to show variableNodes 
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
    ConstraintNode constraintNode = (ConstraintNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if ((! isUnaryConstraint) && (partialPlanView instanceof ConstraintNetworkView)) {
        if (! areNeighborsShown) {
          //System.err.println( "doMouseClick: Mouse-L show variable nodes of constraint id " +
          //                    constraintNode.getConstraint().getId());
          addConstraintNodeVariables( this, (ConstraintNetworkView) partialPlanView);
          areNeighborsShown = true;
        } else {
          //System.err.println( "doMouseClick: Mouse-L hide variable nodes of constraint id " +
          //                    constraintNode.getConstraint().getId());
          removeConstraintNodeVariables( this, (ConstraintNetworkView) partialPlanView);
          areNeighborsShown = false;
        }
        return true;
      }
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private void addConstraintNodeVariables( ConstraintNode constraintNode,
                                           ConstraintNetworkView constraintNetworkView) {
    constraintNetworkView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areNodesChanged = constraintNetworkView.addVariableNodes( constraintNode);
    boolean areLinksChanged =
      constraintNetworkView.addConstraintToVariableLinks( constraintNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.setFocusNode( constraintNode);
      constraintNetworkView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  } // end addConstraintNodeVariables

  private void removeConstraintNodeVariables( ConstraintNode constraintNode,
                                           ConstraintNetworkView constraintNetworkView) {
    constraintNetworkView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areLinksChanged =
      constraintNetworkView.removeConstraintToVariableLinks( constraintNode);
    boolean areNodesChanged = constraintNetworkView.removeVariableNodes( constraintNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.setFocusNode( constraintNode);
      constraintNetworkView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end addConstraintnodeVariablesConstraints


} // end class ConstraintNode
