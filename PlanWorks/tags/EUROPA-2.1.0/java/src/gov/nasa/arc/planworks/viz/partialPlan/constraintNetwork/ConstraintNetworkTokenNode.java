// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNetworkTokenNode.java,v 1.21 2004-08-07 01:18:27 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- subclassed from TokenNode 30sep03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;


/**
 * <code>ConstraintNetworkTokenNode</code> - TokenNode behavior in the Constraint Network
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNetworkTokenNode extends TokenNode implements VariableContainerNode, OverviewToolTip {

  private PwSlot slot;
  private Map connectedContainerMap;
  private List variableNodeList; // element VariableNode
  private List connectedContainerNodes;
  private boolean areNeighborsShown;
  private boolean hasDiscoveredLinks;
  private int variableLinkCount;
  private int connectedContainerCount;
  private boolean isDebug;
  //private Color backgroundColor;

  /**
   * <code>ConstraintNetworkTokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public ConstraintNetworkTokenNode( PwToken token, PwSlot slot, Point tokenLocation,
                                     Color backgroundColor, boolean isFreeToken,
                                     boolean isDraggable,
                                     PartialPlanView partialPlanView) {
    super( token, slot, tokenLocation, backgroundColor, isFreeToken, isDraggable,
           partialPlanView);
    this.slot = slot;
    variableNodeList = new UniqueSet();
    setAreNeighborsShown( false);
    hasDiscoveredLinks = false;
    variableLinkCount = 0;
    connectedContainerCount = 0;
    connectedContainerMap = new HashMap();
    connectedContainerNodes = new UniqueSet();
    isDebug = false;
    // isDebug = true;
    //this.backgroundColor = backgroundColor;
  } // end constructor

  // public Color getColor(){return backgroundColor;}

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
    StringBuffer tip = new StringBuffer( "<html> ");
    String operation = null;
    if (areNeighborsShown()) {
      operation = "close";
    } else {
      operation = "open";
    }
    if (token != null) {
      tip.append( token.toString());
      if (isDebug) {
	tip.append( " linkCnt ").append( String.valueOf( variableLinkCount));	
      }
      if (partialPlanView.getZoomFactor() > 1) {
        tip.append( "<br>key=");
        tip.append( token.getId().toString());
      }
    } else {
      tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
    }
    // check for free token
    if (slot != null) {
      tip.append( "<br>");
      tip.append( "slot key=");
      tip.append( slot.getId().toString());
    }
    tip.append( "<br> Mouse-L: ").append( operation).append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *                               implements OverviewToolTip 
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    if (token != null) {
      tip.append( getPredicateName());
    } else {
      tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
    }
    tip.append( "<br>key=");
    tip.append( token.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  public PwVariableContainer getContainer() {
    return token;
  }

  /**
   * <code>getVariableNodeList</code>
   *
   * @return - <code>List</code> - of VariableNode
   */
  public List getVariableNodes() {
    return variableNodeList;
  }

  public void addVariableNode(Object v) {
    addVariableNode((VariableNode) v);
  }

  /**
   * <code>addVariableNode</code>
   *
   * @param variableNode - <code>VariableNode</code> - 
   */
  public void addVariableNode( VariableNode variableNode) {
    if(!variableNodeList.contains(variableNode)) {
      variableNodeList.add( variableNode);
    }
  }

  /**
   * <code>setAreNeighborsShown</code>
   *
   * @param areShown - <code>boolean</code> - 
   */
  public void setAreNeighborsShown( boolean areShown) {
    areNeighborsShown = areShown;
  }

  /**
   * <code>areNeighborsShown</code>
   *a
   * @return - <code>boolean</code> - 
   */
  public boolean areNeighborsShown() {
    return areNeighborsShown;
  }

  /**
   * <code>doMouseClick</code> - For Constraint Network View, Mouse-Left opens/closes
   *            tokenNode to show variableNodes.  Mouse-Right: Set Active Token
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    return ConstraintNetworkUtils.containerDoMouseClick(modifiers, docCoords, viewCoords, view, 
                                                        this, 
                                                        (ConstraintNetworkView) partialPlanView);
  } // end doMouseClick   

  public void addContainerNodeVariables(Object n, Object v, boolean doRedraw) {
    addContainerNodeVariables((VariableContainerNode) n, (ConstraintNetworkView) v, doRedraw);
  }

  /**
   * <code>addTokenNodeVariables</code> - protected since
   *                                           needed by ConstraintJGoView
   *
   * @param tokenNode - <code>ConstraintNetworkTokenNode</code> - 
   * @param constraintNetworkView - <code>ConstraintNetworkView</code> - 
   */
  public void addContainerNodeVariables( VariableContainerNode tokenNode,
                                         ConstraintNetworkView constraintNetworkView,
                                         boolean doRedraw) {
    ConstraintNetworkUtils.addContainerNodeVariables(tokenNode, constraintNetworkView, doRedraw);
    int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
  } // end addContainerNodeVariables

  public void removeContainerNodeVariables( VariableContainerNode tokenNode,
                                         ConstraintNetworkView constraintNetworkView) {
    ConstraintNetworkUtils.removeContainerNodeVariables(tokenNode, constraintNetworkView);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end removeContainerNodeVariables

  public void discoverLinkage() {
    connectedContainerCount = ConstraintNetworkUtils.discoverLinkage(this, connectedContainerMap);
    hasDiscoveredLinks = true;
  }

  public int getContainerLinkCount() {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    return connectedContainerCount;
  }

  public int getContainerLinkCount(PwVariableContainer other) {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    Integer retval = (Integer) connectedContainerMap.get(other);
    if(retval == null) {
      return 0;
    }
    return retval.intValue();
  }

  public int getContainerLinkCount(VariableContainerNode other) {
    return getContainerLinkCount(other.getContainer());
  }

  public List getConnectedContainers() {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    return new ArrayList(connectedContainerMap.keySet());
  }

  public void connectNodes(Map containerNodeMap) {
    ConstraintNetworkUtils.connectNodes(containerNodeMap, connectedContainerMap,
                                        connectedContainerNodes);
  }

  public List getConnectedContainerNodes() {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    return new ArrayList(connectedContainerNodes);
  }
  
  public boolean equals(VariableContainerNode n) {
    return token.getId().equals(n.getContainer().getId());
  }
} // end class ConstraintNetworkTokenNode
