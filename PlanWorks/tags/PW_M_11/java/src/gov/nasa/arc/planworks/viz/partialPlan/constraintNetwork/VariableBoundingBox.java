package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
  private LinkedList constraintNodes;
  private NewConstraintNetworkLayout layout;
  private boolean visited;
  public VariableBoundingBox(VariableNode varNode, NewConstraintNetworkLayout layout) {
    this.varNode = varNode;
    this.layout = layout;
    visited = false;
    constraintNodes = new LinkedList(varNode.getConstraintNodeList());
  }
  public VariableBoundingBox(VariableNode varNode, List constraints, 
                             NewConstraintNetworkLayout layout) {
    this.varNode = varNode;
    this.layout = layout;
    visited = false;
    this.constraintNodes = new LinkedList(constraints);
  }
  public void addConstraint(ConstraintNode constraint) {
    constraintNodes.addLast(constraint);
  }
  public boolean isVisible() { return varNode.isVisible();}

  public double getHeight() {
    double retval = 0.;
    if(layout.layoutHorizontal()) {
      retval = ConstraintNetworkView.HORIZONTAL_VARIABLE_BAND_Y - 
        ConstraintNetworkView.HORIZONTAL_CONSTRAINT_BAND_Y;
    }
    else {
      if(varNode.isVisible()) {
        ListIterator constraintIterator = constraintNodes.listIterator();
        double constraintsHeight = 0.;
        int visibleConstraints = 0;
        while(constraintIterator.hasNext()) {
          ConstraintNode node = (ConstraintNode) constraintIterator.next();
          if(node.isVisible()) {
            BasicNodeLink link = node.getLinkToNode(varNode);
            if(link != null && link.isVisible()) {
              constraintsHeight += node.getSize().getHeight();
              visibleConstraints++;
            }
          }
        }
        retval = Math.max(varNode.getSize().getHeight() + ConstraintNetworkView.NODE_SPACING,
                          constraintsHeight + (ConstraintNetworkView.NODE_SPACING * visibleConstraints));
      }
    }
    return retval;
  }
  public double getWidth() {
    double retval = 0.;
    if(layout.layoutHorizontal()) {
      if(varNode.isVisible()) {
        ListIterator constraintIterator = constraintNodes.listIterator();
        double constraintsWidth = 0.;
        int visibleConstraints = 0;
        while(constraintIterator.hasNext()) {
          ConstraintNode node = (ConstraintNode) constraintIterator.next();
          if(node.isVisible()) {
            BasicNodeLink link = node.getLinkToNode(varNode);
            if(link != null && link.isVisible()) {
              constraintsWidth += node.getSize().getWidth();
              visibleConstraints++;
            }
          }
        }
        retval = Math.max(varNode.getSize().getWidth() + ConstraintNetworkView.NODE_SPACING,
                          constraintsWidth + (ConstraintNetworkView.NODE_SPACING*visibleConstraints));
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
}

