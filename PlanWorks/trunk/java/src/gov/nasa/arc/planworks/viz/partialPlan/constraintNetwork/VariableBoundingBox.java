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
      retval = ConstraintNetworkView.HORIZONTAL_CONSTRAINT_BAND_Y - 
        ConstraintNetworkView.HORIZONTAL_VARIABLE_BAND_Y;
    }
    else {
      if(varNode.isVisible()) {
        ListIterator constraintIterator = constraintNodes.listIterator();
        double topConstraint = Double.MIN_VALUE;
        double bottomConstraint = Double.MAX_VALUE;
        int visibleConstraints = 0;
        while(constraintIterator.hasNext()) {
          ConstraintNode node = (ConstraintNode) constraintIterator.next();
          if(node.isVisible()) {
            if(node.getLocation().getY() > topConstraint) {
              topConstraint = node.getLocation().getY();
            }
            if(node.getLocation().getY() < bottomConstraint) {
              bottomConstraint = node.getLocation().getY();
            }
            visibleConstraints++;
          }
        }
        retval = Math.max(varNode.getSize().getHeight(), (topConstraint - bottomConstraint) + 
                          (ConstraintNetworkView.NODE_SPACING * (visibleConstraints - 1)));
      }
    }
    return retval;
  }
  public double getWidth() {
    double retval = 0.;
    if(layout.layoutHorizontal()) {
      if(varNode.isVisible()) {
        ListIterator constraintIterator = constraintNodes.listIterator();
        double constraintsWidth = 0;
        int visibleConstraints = 0;
        while(constraintIterator.hasNext()) {
          ConstraintNode node = (ConstraintNode) constraintIterator.next();
          if(node.isVisible()) {
            constraintsWidth += node.getSize().getWidth();
            visibleConstraints++;
          }
        }
        retval = Math.max(varNode.getSize().getWidth(), constraintsWidth + 
                          (ConstraintNetworkView.NODE_SPACING * (visibleConstraints - 1)));
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
    //varNode.setLocation(new Point(ConstraintNetworkView.VERTICAL_VARIABLE_BAND_X, 
    //                              boxY - (boxHeight / 2)));
    varNode.setLocation((int)ConstraintNetworkView.VERTICAL_VARIABLE_BAND_X,
                        (int)(boxY - (boxHeight / 2)));
    int visibleConstraints = 0;
    ListIterator constraintIterator = constraintNodes.listIterator();
    while(constraintIterator.hasNext()) {
      ConstraintNode node = (ConstraintNode) constraintIterator.next();
      if(node.isVisible()) {
        visibleConstraints++;
      }
    }
    double constraintYInc = 
      boxHeight / (((ConstraintNode)constraintNodes.get(0)).getSize().getHeight() * visibleConstraints);
    constraintIterator = constraintNodes.listIterator();
    int multiplier = 0;
    while(constraintIterator.hasNext()) {
      ConstraintNode node = (ConstraintNode) constraintIterator.next();
      if(node.isVisible()) {
        //node.setLocation(new Point(ConstraintNetworkView.VERTICAL_CONSTRAINT_BAND_X,
        //                           boxY - (constraintYInc * multiplier)));
        node.setLocation((int) ConstraintNetworkView.VERTICAL_CONSTRAINT_BAND_X,
                         (int)(boxY - (constraintYInc * multiplier)));
      }
    }
  }

  private void positionHorizontal(double boxX) {
    double boxWidth = getWidth();
    //varNode.setLocation(new Point(boxX - (boxWidth / 2),
    //                              ConstraintNetworkView.HORIZONTAL_VARIABLE_BAND_Y));
    varNode.setLocation((int) (boxX - (boxWidth / 2)),
                        (int) ConstraintNetworkView.HORIZONTAL_VARIABLE_BAND_Y);
    int visibleConstraints = 0;
    ListIterator constraintIterator = constraintNodes.listIterator();
    while(constraintIterator.hasNext()) {
      ConstraintNode node = (ConstraintNode) constraintIterator.next();
      if(node.isVisible()) {
        visibleConstraints++;
      }
    }
    double constraintXInc = boxWidth / visibleConstraints;
    constraintIterator = constraintNodes.listIterator();
    int multiplier = 0;
    while(constraintIterator.hasNext()) {
      ConstraintNode node = (ConstraintNode) constraintIterator.next();
      if(node.isVisible()) {
        //node.setLocation(new Point(boxX - (constraintXInc * multiplier), 
        //                           ConstraintNetworkView.HORIZONTAL_CONSTRAINT_BAND_Y));
        node.setLocation((int) (boxX - (constraintXInc * multiplier)),
                         (int) ConstraintNetworkView.HORIZONTAL_CONSTRAINT_BAND_Y);
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

