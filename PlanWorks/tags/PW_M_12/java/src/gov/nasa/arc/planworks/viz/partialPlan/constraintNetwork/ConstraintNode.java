// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNode.java,v 1.10 2004-01-16 19:05:36 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 29july03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>ConstraintNode</code> - JGo widget to render a variable constraint with a
 *                          label 
 *             Object->JGoObject->JGoArea->TextNode->ConstraintNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNode extends ExtendedBasicNode {

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
  private List constraintVariableLinkList; // element BasicNodeLink
  //fix me.
  private Map constraintVariableLinkMap;
  private boolean areNeighborsShown;
  private boolean inLayout;
  private boolean isUnaryConstraint;
  private int variableLinkCount;
  private boolean isDebug;
  private boolean hasBeenVisited;
  private Color backgroundColor;

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
                         boolean isFreeToken, boolean isDraggable,
                         PartialPlanView partialPlanView) { 
    super( ViewConstants.DIAMOND);
    this.constraint = constraint;
    this.variableNode = variableNode;
    this.isFreeToken = isFreeToken;
    this.partialPlanView = partialPlanView;
    variableNodeList = new ArrayList();
    variableNodeList.add( variableNode);
    constraintVariableLinkList = new ArrayList();
    constraintVariableLinkMap = new HashMap();

    this.backgroundColor = backgroundColor;

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

  public Color getColor(){return backgroundColor;}

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
       return tip.append("</html>").toString();
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


  public void setAreNeighborsShown(boolean shown) {
    areNeighborsShown = shown;
  }
  public boolean areNeighborsShown() {
    return areNeighborsShown;
  }

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
    if (!variableNodeList.contains(variableNode)) {
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
    if(!constraintVariableLinkList.contains(link)) {
      constraintVariableLinkList.add( link);
      constraintVariableLinkMap.put(link.getToNode(), link);
    }
  }

  /**
   * <code>getLinkToNode</code>
   *
   * @param node - <code>BasicNode</code> - 
   * @return - <code>BasicNodeLink</code> - 
   */
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
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords, JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
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
      mouseRightPopupMenu( viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

    private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();
    JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
    navigatorItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          MDIInternalFrame navigatorFrame = partialPlanView.openNavigatorViewFrame();
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = partialPlanView.getPartialPlan();
          contentPane.add( new NavigatorView( ConstraintNode.this, partialPlan,
                                              partialPlanView.getViewSet(),
                                              navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, partialPlanView, viewCoords);
  } // end mouseRightPopupMenu

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


  public boolean equals(ConstraintNode c) {
    return constraint.getId().equals(c.getConstraint().getId());
  }

} // end class ConstraintNode
