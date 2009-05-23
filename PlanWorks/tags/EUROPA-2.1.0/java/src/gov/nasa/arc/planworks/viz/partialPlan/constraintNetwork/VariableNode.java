// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableNode.java,v 1.23 2004-08-25 18:41:02 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 28july03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwEnumeratedDomain;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>VariableNode</code> - JGo widget to render a token variable with a
 *                          label 
 *             Object->JGoObject->JGoArea->TextNode->VariableNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VariableNode extends ExtendedBasicNode implements OverviewToolTip {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private PwVariable variable;
  //private TokenNode tokenNode;
  private VariableContainerNode parentNode;
  private PartialPlanView partialPlanView;
  private String nodeLabel;
  private List containerNodeList; // element TokenNode
  private List constraintNodeList; // element ConstraintNode
  private List variableContainerLinkList; // element BasicNodeLink
  private boolean inLayout;
  private boolean hasZeroConstraints;
  private int containerLinkCount;
  private int constraintLinkCount;
  private boolean isDebug;
  private boolean hasBeenVisited;
  private Color backgroundColor;

  /**
   * <code>VariableNode</code> - constructor 
   *
   * @param variable - <code>PwVariable</code> - 
   * @param tokenNode - <code>TokenNode</code> - 
   * @param variableLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public VariableNode( final PwVariable variable, final VariableContainerNode parentNode, 
                       final Point variableLocation, final Color backgroundColor, 
                       final boolean isDraggable, final PartialPlanView partialPlanView) { 
    super( ViewConstants.PINCHED_RECTANGLE);
    this.variable = variable;
    this.partialPlanView = partialPlanView;
    containerNodeList = new ArrayList();
    containerNodeList.add( parentNode);
    constraintNodeList = new ArrayList();
    variableContainerLinkList = new ArrayList();
    this.backgroundColor = backgroundColor;

    inLayout = false;
    hasZeroConstraints = true;
    if (variable.getConstraintList().size() > 0) {
      hasZeroConstraints = false;
    }
    hasBeenVisited = false;

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( variable.getDomain().toString());
    labelBuf.append( "\nkey=").append( variable.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "VariableNode: " + nodeLabel);

    configure( variableLocation, backgroundColor, isDraggable);
  } // end constructor

  /**
   * <code>VariableNode</code> - constructor - for NodeShapes
   *
   * @param name - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @param variableLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public VariableNode( final String name, final Integer id, final Point variableLocation,
                       final Color backgroundColor) {
    super( ViewConstants.PINCHED_RECTANGLE);
    this.variable = null;
    this.partialPlanView = null;
    this.backgroundColor = backgroundColor;

    boolean isDraggable = false;
    inLayout = false;
    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( name);
    labelBuf.append( "\nkey=").append( id.toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "VariableNode: " + nodeLabel);

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
    setAreNeighborsShown( false);
    if (hasZeroConstraints) {
      setAreNeighborsShown( true);
      int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
      setPen( new JGoPen( JGoPen.SOLID, penWidth,  ColorMap.getColor( "black")));
    }
  } // end configure


  public Color getColor(){return backgroundColor;}

  /**
   * <code>getVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getVariable() {
    return variable;
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
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (variable == null) {
      return null;
    }
    String operation = null;
    if (areNeighborsShown()) {
      operation = "close";
    } else {
      operation = "open";
    }
    StringBuffer tip = new StringBuffer( "<html> ");
    boolean isVariableWithConstraints = (! hasZeroConstraints) &&
      (partialPlanView instanceof ConstraintNetworkView);
    NodeGenerics.getVariableNodeToolTipText( variable, partialPlanView, tip);
    if (isDebug) {
      tip.append( " linkCnt ").append( String.valueOf( constraintLinkCount));
    }
    if (partialPlanView.getZoomFactor() > 1) {
      tip.append( "<br>key=");
      tip.append( variable.getId().toString());
    }
    if (isVariableWithConstraints) {
      tip.append( "<br> Mouse-L: ").append( operation);
    }
    return tip.append("</html>").toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview variable node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
     NodeGenerics.getVariableNodeToolTipText( variable, partialPlanView, tip);
    tip.append( "<br>key=");
    tip.append( variable.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getContainerNodeList</code>
   *
   * @return - <code>List</code> - of VariableContainerNode
   */
  public List getContainerNodeList() {
    return containerNodeList;
  }

  /**
   * <code>addTokenNode</code>
   *
   * @param tokenNode - <code>TokenNode</code> - 
   */
  public void addContainerNode( VariableContainerNode parentNode) {
    if (!containerNodeList.contains(parentNode)) {
      containerNodeList.add( parentNode);
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
    if (!constraintNodeList.contains(constraintNode)) {
      constraintNodeList.add( constraintNode);
    }
  }

  /**
   * <code>getVariableTokenLinkList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getVariableContainerLinkList() {
    return variableContainerLinkList;
  }

  /**
   * <code>addLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   */
  public void addLink( BasicNodeLink link) {
    if (!variableContainerLinkList.contains(link)) {
      variableContainerLinkList.add( link);
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
      setAreNeighborsShown( false);
    }
    if (hasZeroConstraints) {
      setAreNeighborsShown( true);
      width = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    }
    setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
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
  public void incrContainerLinkCount() {
    containerLinkCount++;
  }

  /**
   * <code>decTokenLinkCount</code>
   *
   */
  public void decContainerLinkCount() {
    containerLinkCount--;
  }

  /**
   * <code>getTokenLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getContainerLinkCount() {
    return containerLinkCount;
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
   * <code>getLinkCount</code> - containerLinkCount + constraintLinkCount
   *
   * @return - <code>int</code> - 
   */
  public int getLinkCount() {
    return containerLinkCount + constraintLinkCount;
  }

  /**
   * <code>resetNode</code> - when closed by token close traversal
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    setAreNeighborsShown( false);
    if (isDebug && (constraintLinkCount != 0)) {
      System.err.println( "reset variable node: " + variable.getId() +
                          "; constraintLinkCount != 0: " + constraintLinkCount);
    }
    constraintLinkCount = 0;
    if (isDebug && (containerLinkCount != 0)) {
      System.err.println( "reset variable node: " + variable.getId() +
                          "; containerLinkCount != 0: " + containerLinkCount);
    }
    containerLinkCount = 0;
  } // end resetNode

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
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords, JGoView view) {
    if (variable == null) {
      return false;
    }
    JGoObject obj = view.pickDocObject( docCoords, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    VariableNode variableNode = (VariableNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if ((! hasZeroConstraints) && (partialPlanView instanceof ConstraintNetworkView)) {
        if (! areNeighborsShown()) {
          //System.err.println
          //  ( "doMouseClick: Mouse-L show constraint/token nodes of variable id " +
          //    variableNode.getVariable().getId());
          addVariableNodeContainersAndConstraints( this, (ConstraintNetworkView) partialPlanView);
          setAreNeighborsShown( true);
        } else {
          //System.err.println
          //  ( "doMouseClick: Mouse-L hide constraint/token nodes of variable id " +
          //    variableNode.getVariable().getId());
          removeVariableNodeContainersAndConstraints( this,
                                                      (ConstraintNetworkView) partialPlanView);
          setAreNeighborsShown( false);
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
    ConstraintNetworkUtils.mouseRightPopupMenu(viewCoords, this, partialPlanView);
  } // end mouseRightPopupMenu

  /**
   * <code>addVariableNodeTokensAndConstraints</code> - protected since
   *                                           needed by ConstraintJGoView
   *
   * @param variableNode - <code>VariableNode</code> - 
   * @param constraintNetworkView - <code>ConstraintNetworkView</code> - 
   */
  protected void addVariableNodeContainersAndConstraints
    ( final VariableNode variableNode, final ConstraintNetworkView constraintNetworkView) {
    addVariableNodeContainersAndConstraints( variableNode, constraintNetworkView, true);
  } // end addVariableNodeTokensAndConstraints

  protected void addVariableNodeContainersAndConstraints
    ( final VariableNode variableNode, final ConstraintNetworkView constraintNetworkView,
      final boolean doRedraw) {
    constraintNetworkView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areNodesChanged = constraintNetworkView.addConstraintNodes( variableNode);
    boolean areLinksChanged =
      constraintNetworkView.addContainerAndConstraintToVariableLinks( variableNode);
    if (doRedraw && (areNodesChanged || areLinksChanged)) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.setFocusNode( variableNode);
      constraintNetworkView.redraw( true);
    }
    int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
  }

  private void removeVariableNodeContainersAndConstraints
    ( final VariableNode variableNode, final ConstraintNetworkView constraintNetworkView) {
    constraintNetworkView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areLinksChanged = constraintNetworkView.removeContainerToVariableLinks( variableNode);
    boolean areNodesChanged = constraintNetworkView.removeConstraintNodes( variableNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.setFocusNode( variableNode);
      constraintNetworkView.redraw( true);
    }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end removeVariableNodeTokensAndConstraints


  public boolean equals(VariableNode v) {
    return variable.getId().equals(v.getVariable().getId());
  }

} // end class VariableNode
