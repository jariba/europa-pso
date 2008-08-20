// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: VariableBoundingBox.java,v 1.9 2004-03-17 01:45:21 taylor Exp $
//
package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;

/*
  VERTICAL LAYOUT:
  t  v  c
  t  v  c
  t  v  c
  t  v  c
  t  v  c
*/
/*
  HORIZONTAL LAYOUT:
  c  c  c  c  c
  v  v  v  v  v
  t  t  t  t  t
*/

public class VariableBoundingBox {
  private VariableNode varNode;
  private ArrayList constraintNodes;
  private NewConstraintNetworkLayout layout;
  private boolean visited;
  private ConstraintNetworkView constraintNetworkView;
  private int zoomDimensionDelta;

  public VariableBoundingBox(VariableNode varNode, NewConstraintNetworkLayout layout,
                             ConstraintNetworkView constraintNetworkView) {
    this.varNode = varNode;
    this.layout = layout;
    this.constraintNetworkView = constraintNetworkView;
    zoomDimensionDelta = 2 * (constraintNetworkView.getOpenJGoPenWidth
                              ( constraintNetworkView.getZoomFactor()) - 2);
    visited = false;
    constraintNodes = new ArrayList(varNode.getConstraintNodeList());
  }
  public VariableBoundingBox(VariableNode varNode, List constraints, 
                             NewConstraintNetworkLayout layout) {
    this.varNode = varNode;
    this.layout = layout;
    visited = false;
    this.constraintNodes = new ArrayList(constraints);
  }
  public void addConstraint(ConstraintNode constraint) {
//     if(!constraintNodes.contains(constraint)) {
//       constraintNodes.addLast(constraint);
//     }
    for(int i = 0; i < constraintNodes.size(); i++) {
      if(((ConstraintNode)constraintNodes.get(i)).equals(constraint)) {
        return;
      }
    }
    constraintNodes.add(constraint);
  }
  public boolean isVisible() { return varNode.isVisible();}

  public VariableNode getVariable(){return varNode;}

  public double getHeight() {
    double retval = 0.;
    if(layout.layoutHorizontal()) {
      retval = ConstraintNetworkView.HORIZONTAL_VARIABLE_BAND_Y - 
        ConstraintNetworkView.HORIZONTAL_CONSTRAINT_BAND_Y;
    }
    else {
      if(varNode.isVisible()) {
        createNecessaryNodes();
        ListIterator constraintIterator = constraintNodes.listIterator();
        double constraintsHeight = 0.;
        int visibleConstraints = 0;
        while(constraintIterator.hasNext()) {
          ConstraintNode node = (ConstraintNode) constraintIterator.next();
          if(node.isVisible()) {
            BasicNodeLink link = node.getLinkToNode(varNode);
            if(link != null && link.isVisible()) {
              constraintsHeight += node.getSize().getHeight();
              constraintsHeight += zoomDimensionDelta;
              visibleConstraints++;
            }
          }
        }
        retval = Math.max(varNode.getSize().getHeight() + zoomDimensionDelta +
                          ConstraintNetworkView.NODE_SPACING,
                          constraintsHeight + (ConstraintNetworkView.NODE_SPACING *
                                               visibleConstraints));
      }
    }
    return retval;
  }

  public double getWidth() {
    double retval = 0.;
    if(layout.layoutHorizontal()) {
      if(varNode.isVisible()) {
        createNecessaryNodes();
        ListIterator constraintIterator = constraintNodes.listIterator();
        double constraintsWidth = 0.;
        int visibleConstraints = 0;
        while(constraintIterator.hasNext()) {
          ConstraintNode node = (ConstraintNode) constraintIterator.next();
          if(node.isVisible()) {
            BasicNodeLink link = node.getLinkToNode(varNode);
            if(link != null && link.isVisible()) {
              constraintsWidth += node.getSize().getWidth();
              constraintsWidth += zoomDimensionDelta;
              visibleConstraints++;
            }
          }
        }
        retval = Math.max(varNode.getSize().getWidth() + zoomDimensionDelta +
                          ConstraintNetworkView.NODE_SPACING,
                          constraintsWidth + (ConstraintNetworkView.NODE_SPACING *
                                              visibleConstraints));
      }
    }
    else {
      retval = ConstraintNetworkView.VERTICAL_CONSTRAINT_BAND_X -
        ConstraintNetworkView.VERTICAL_VARIABLE_BAND_X;
    }
    return retval;
  }
  public void positionNodes(double pos) {
    if(!varNode.isVisible()) {
      return;
    }
    if(constraintNodes.size() > varNode.getConstraintNodeList().size()) {
      System.err.println("Too many constraint nodes in var node " + varNode);
      System.err.println("box:  " + constraintNodes.size());
      System.err.println("node: " + varNode.getConstraintNodeList());
    }
    if(constraintNodes.size() < varNode.getConstraintNodeList().size()) {
      //constraintNodes = new LinkedList(varNode.getConstraintNodeList());
      ListIterator constrNodeIterator = varNode.getConstraintNodeList().listIterator();
      while(constrNodeIterator.hasNext()) {
        addConstraint((ConstraintNode)constrNodeIterator.next());
      }
    }
    createNecessaryNodes();

    if(layout.layoutHorizontal()) {
      positionHorizontal(pos);
    }
    else {
      positionVertical(pos);
    }
  }

  private void positionVertical(double boxY) { //200310211500 happy b-day rory mcgann!
    double boxHeight = getHeight();
    varNode.setLocation((int)ConstraintNetworkView.VERTICAL_VARIABLE_BAND_X,
                        (int)(boxY - (boxHeight / 2)));
    ListIterator constraintIterator = constraintNodes.listIterator();
    double lastConstraintHeight = 0;
    while(constraintIterator.hasNext()) {
      ConstraintNode node = (ConstraintNode) constraintIterator.next();
      if(node.isVisible()) {
        BasicNodeLink link = node.getLinkToNode(varNode);
        if(link != null && link.isVisible()) {
          node.setLocation((int) ConstraintNetworkView.VERTICAL_CONSTRAINT_BAND_X,
                           (int) (boxY - lastConstraintHeight - (node.getSize().getHeight() / 2)));
          lastConstraintHeight += node.getSize().getHeight() + ConstraintNetworkView.NODE_SPACING;
        }
      }
    }
  }

  private void positionHorizontal(double boxX) {
    double boxWidth = getWidth();
    varNode.setLocation((int) (boxX - (boxWidth / 2)),
                        (int) ConstraintNetworkView.HORIZONTAL_VARIABLE_BAND_Y);
    ListIterator constraintIterator = constraintNodes.listIterator();
    double lastConstraintWidth = 0;
    while(constraintIterator.hasNext()) {
      ConstraintNode node = (ConstraintNode) constraintIterator.next();
      if(node.isVisible()) {
        BasicNodeLink link = node.getLinkToNode(varNode);
        if(link != null && link.isVisible()) {
          node.setLocation((int) (boxX - lastConstraintWidth - (node.getSize().getWidth() / 2)),
                           (int) ConstraintNetworkView.HORIZONTAL_CONSTRAINT_BAND_Y);
          lastConstraintWidth += node.getSize().getWidth() + ConstraintNetworkView.NODE_SPACING;
        }
      }
    }
  }
  public void setVisited() {
    visited = true;
  }
  public void clearVisited() {
    visited = false;
  }
  public boolean wasVisited() {
    return visited;
  }

  public boolean equals(VariableBoundingBox v) {
    return varNode.equals(v.varNode);
  }
  
  public String toString() {
    return varNode.toString();
  }
  private void createNecessaryNodes() {
    if(constraintNodes.size() > varNode.getConstraintNodeList().size()) {
      System.err.println("Too many constraint nodes in var node " + varNode);
      System.err.println("box:  " + constraintNodes.size());
      System.err.println("node: " + varNode.getConstraintNodeList());
      for(int i = 0; i < constraintNodes.size(); i++) {
        for(int j = i+1; j < constraintNodes.size(); j++) {
          if(((ConstraintNode)constraintNodes.get(i)).
             equals((ConstraintNode)constraintNodes.get(j))) {
            constraintNodes.remove(j);
          }
        }
      }
    }
    if(constraintNodes.size() < varNode.getConstraintNodeList().size()) {
      //constraintNodes = new LinkedList(varNode.getConstraintNodeList());
      ListIterator constrNodeIterator = varNode.getConstraintNodeList().listIterator();
      while(constrNodeIterator.hasNext()) {
        addConstraint((ConstraintNode)constrNodeIterator.next());
      }
    }
  }
}

