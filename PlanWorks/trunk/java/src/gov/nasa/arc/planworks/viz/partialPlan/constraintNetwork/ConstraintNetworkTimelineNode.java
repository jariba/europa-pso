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
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;

public class ConstraintNetworkTimelineNode extends TimelineNode {

  private Map connectedContainerMap;
  private List variableNodeList;
  private List connectedContainerNodes;
  private boolean areNeighborsShown;
  private boolean hasDiscoveredLinks;
  private int variableLinkCount;
  private int connectedContainerCount;
  private Color backgroundColor;

  public ConstraintNetworkTimelineNode(PwTimeline tl, Point tlLocation, Color backgroundColor,
                                       boolean isDraggable, PartialPlanView partialPlanView) {
    super(tl, tlLocation, backgroundColor, isDraggable, partialPlanView);
    this.backgroundColor = backgroundColor;
    variableNodeList = new UniqueSet();
    areNeighborsShown = false;
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
    if (areNeighborsShown) {
      operation = "close";
    } else {
      operation = "open";
    }
    if (timeline != null) {
      tip.append( timeline.toString());
    } else {
      tip.append( "This is a bug");
    }
    tip.append( "<br> Mouse-L: ").append( operation).append( "</html>");
    return tip.toString();
  }

  public String getToolTipText(boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    if (timeline != null) {
      tip.append( timeline.getName());
    } else {
      tip.append( "This is a bug");
    }
    tip.append( "<br>key=");
    tip.append( timeline.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  }

  public List getVariableNodes() { 
    return variableNodeList;
  }

  public void addVariableNode(Object v) {
    addVariableNode((VariableNode) v);
  }
  public void addVariableNode(VariableNode varNode) {
    if(!variableNodeList.contains(varNode)) {
      variableNodeList.add(varNode);
    }
  }

  public void setAreNeighborsShown( boolean areShown) {
    areNeighborsShown = areShown;
  }

  public boolean areNeighborsShown() {
    return areNeighborsShown;
  }

  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    // System.err.println( "ConstraintNetworkTokenNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    ConstraintNetworkTimelineNode tlNode =
      (ConstraintNetworkTimelineNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (! areNeighborsShown) {
        //System.err.println( "doMouseClick: Mouse-L show variable nodes of " +
        //                    tokenNode.getPredicateName());
        addContainerNodeVariables( this, (ConstraintNetworkView) partialPlanView);
        areNeighborsShown = true;
      } else {
        // System.err.println( "doMouseClick: Mouse-L hide variable nodes of " +
        //                    tokenNode.getPredicateName());
        removeContainerNodeVariables( this, (ConstraintNetworkView) partialPlanView);
        areNeighborsShown = false;
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      super.doMouseClick( modifiers, docCoords, viewCoords, view);
      return true;
    }
    return false;
  } // end doMouseClick 

  public void addContainerNodeVariables(Object n, Object v) {
    addContainerNodeVariables((VariableContainerNode) n, (ConstraintNetworkView) v);
  }

  protected void addContainerNodeVariables( VariableContainerNode objNode,
                                             ConstraintNetworkView constraintNetworkView) {
    constraintNetworkView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areNodesChanged = constraintNetworkView.addVariableNodes( objNode);
    boolean areLinksChanged = constraintNetworkView.addVariableToContainerLinks( objNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.setFocusNode( (JGoArea) objNode);
      constraintNetworkView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  } // end addTokenNodeVariables 

    private void removeContainerNodeVariables( VariableContainerNode objNode,
                                           ConstraintNetworkView constraintNetworkView) {
    constraintNetworkView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areLinksChanged = constraintNetworkView.removeVariableToContainerLinks( objNode);
    boolean areNodesChanged = constraintNetworkView.removeVariableNodes( objNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.setFocusNode( (JGoArea)objNode);
      constraintNetworkView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end adremoveTokenNodeVariables

  public void discoverLinkage() {
    ListIterator varIterator = timeline.getVariables().listIterator();
    while(varIterator.hasNext()) {
      PwVariable var = (PwVariable) varIterator.next();
      ListIterator constraintIterator = var.getConstraintList().listIterator();
      while(constraintIterator.hasNext()) {
	PwConstraint constr = (PwConstraint) constraintIterator.next();
	ListIterator constrainedVarIterator = constr.getVariablesList().listIterator();
	while(constrainedVarIterator.hasNext()) {
	  PwVariable constrVar = (PwVariable) constrainedVarIterator.next();
	  if(constrVar.equals(var)) {
	    continue;
	  }
          PwVariableContainer varContainer = (PwVariableContainer) constrVar.getParent();
          if(!connectedContainerMap.containsKey(varContainer)) {
            connectedContainerMap.put(varContainer, new Integer(0));
          }
          connectedContainerMap.put(varContainer, new Integer(((Integer)connectedContainerMap.
                                                               get(varContainer)).intValue() + 1));
          connectedContainerCount++;
	}
      }
    }
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
    Iterator containerIterator = connectedContainerMap.keySet().iterator();
    while(containerIterator.hasNext()) {
      PwVariableContainer otherCont = (PwVariableContainer) containerIterator.next();
      if(containerNodeMap.containsKey(otherCont.getId())) {
        connectedContainerNodes.add(containerNodeMap.get(otherCont.getId()));
      }
    }
  }

  public List getConnectedContainerNodes() {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    return new ArrayList(connectedContainerNodes);
  }
  
  public boolean equals(VariableContainerNode n) {
    return timeline.getId().equals(n.getContainer().getId());
  }
}
