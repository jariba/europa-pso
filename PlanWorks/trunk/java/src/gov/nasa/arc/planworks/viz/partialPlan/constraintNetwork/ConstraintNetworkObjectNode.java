// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintNetworkObjectNode.java,v 1.10 2004-06-10 01:36:01 taylor Exp $
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.ObjectNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


public class ConstraintNetworkObjectNode extends ObjectNode implements VariableContainerNode, OverviewToolTip {

  private Map connectedContainerMap;
  private List variableNodeList;
  private List connectedContainerNodes;
  private boolean areNeighborsShown;
  private boolean hasDiscoveredLinks;
  private int variableLinkCount;
  private int connectedContainerCount;
  private PartialPlanView partialPlanView;

  public ConstraintNetworkObjectNode(PwObject obj, Point objLocation, boolean isDraggable,
                                     PartialPlanView partialPlanView) {
    super(obj, objLocation, partialPlanView.getTimelineColor( obj.getId()),
          isDraggable, partialPlanView);
    this.partialPlanView = partialPlanView;
    variableNodeList = new UniqueSet();
    setAreNeighborsShown( false);
    hasDiscoveredLinks = false;
    variableLinkCount = 0;
    connectedContainerCount = 0;
    connectedContainerMap = new HashMap();
    connectedContainerNodes = new UniqueSet();
  }

  public void incrVariableLinkCount() {
    variableLinkCount++;
  }

  public void decVariableLinkCount() {
    variableLinkCount--;
  }

  public int getVariableLinkCount() {
    return variableLinkCount;
  }

  public String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html> ");
    String operation = null;
    if (areNeighborsShown()) {
      operation = "close";
    } else {
      operation = "open";
    }
    if (object != null) {
      // tip.append( object.toString());
      // tip.append( "object");
      tip.append( object.getName());
      if (partialPlanView.getZoomFactor() > 1) {
        tip.append( "<br>key=");
        tip.append( object.getId().toString());
      }
    } else {
      tip.append( "This is a bug");
    }
    tip.append( "<br>Mouse-L: ").append( operation);
    tip.append( "</html>");
    return tip.toString();
  }

  public String getToolTipText(boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    if (object != null) {
      tip.append( object.getName());
    } else {
      tip.append( "This is a bug");
    }
    tip.append( "<br>key=");
    tip.append( object.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  }

  public List getVariableNodes() { 
    return variableNodeList;
  }

  public void addVariableNode(Object v) {
    addVariableNode((VariableNode)v);
  }

  public void addVariableNode(VariableNode varNode) {
    if(!variableNodeList.contains(varNode)) {
      variableNodeList.add(varNode);
    }
  }

  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    return ConstraintNetworkUtils.containerDoMouseClick(modifiers, docCoords, viewCoords, view,
                                                        this, 
                                                        (ConstraintNetworkView) partialPlanView);
  } // end doMouseClick 

  public void mouseRightPopupMenu( Point viewCoords) {
    ConstraintNetworkUtils.mouseRightPopupMenu(viewCoords, this, partialPlanView);
  } // end mouseRightPopupMenu

  public void addContainerNodeVariables(Object n, Object v) {
    addContainerNodeVariables((VariableContainerNode) n, (ConstraintNetworkView) v);
  }

  public void addContainerNodeVariables( VariableContainerNode objNode,
                                            ConstraintNetworkView constraintNetworkView) {
    ConstraintNetworkUtils.addContainerNodeVariables(objNode, constraintNetworkView);
    int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth,  ColorMap.getColor( "black")));
  } // end addTokenNodeVariables 

  public void removeContainerNodeVariables( VariableContainerNode objNode,
                                             ConstraintNetworkView constraintNetworkView) {
    ConstraintNetworkUtils.removeContainerNodeVariables(objNode, constraintNetworkView);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end adremoveTokenNodeVariables

  public PwVariableContainer getContainer() {
    return object;
  } 

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
    return object.getId().equals(n.getContainer().getId());
  }
}
