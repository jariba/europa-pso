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
public class TokenBoundingBox {
  private ConstraintNetworkTokenNode tokenNode;
  private LinkedList variableBoundingBoxes;
  public TokenBoundingBox(ConstraintNetworkTokenNode tokenNode) {
    this.tokenNode = tokenNode;
    ListIterator variableIterator = tokenNode.getVariableNodeList().listIterator();
    while(variableIterator.hasNext()) {
      variableBoundingBoxes.add(new VariableBoundingBox((VariableNode) variableIterator.next()));
    }
  }
  public TokenBoundingBox(ConstraintNetworkTokenNode tokenNode, List variables) {
    this.tokenNode = tokenNode;
    ListIterator variableIterator = variables.listIterator();
    while(variableIterator.hasNext()) {
      variableBoundingBoxes.add(new VariableBoundingBox((VariableNode)variableIterator.next()));
    }
  }
  public void addVariable(VariableNode variable) {
    variableBoundingBoxes.add(new VariableBoundingBox(variable));
  }
  public double getHeight() {
    double retval = 0.;
    if(NewConstraintNetworkLayout.layoutHorizontal()) {
      retval = ConstraintNetworkView.HORIZONTAL_CONSTRAINT_BAND_Y -
        ConstraintNetworkView.HORIZONTAL_TOKEN_BAND_Y;
    }
    else {
      if(tokenNode.isVisible()) {
        ListIterator variableBoxIterator = variableBoundingBoxes.listIterator();
        double varBoxesHeight = 0.;
        while(variableBoxIterator.hasNext()) {
          varBoxesHeight += ((VariableBoundingBox)variableBoxIterator.next()).getHeight();
        }
        retval = Math.max(tokenNode.getSize().getHeight(), varBoxesHeight);
      }
    }
    return retval;
  }
  public double getWidth() {
    double retval = 0.;
    if(NewConstraintNetworkLayout.layoutHorizontal()) {
      if(tokenNode.isVisible()) {
        ListIterator variableBoxIterator = variableBoundingBoxes.listIterator();
        double varBoxesWidth = 0.;
        while(variableBoxIterator.hasNext()) {
          varBoxesWidth += ((VariableBoundingBox)variableBoxIterator.next()).getHeight();
        }
        retval = Math.max(tokenNode.getSize().getWidth(), varBoxesWidth);
      }
    }
    else {
      retval = ConstraintNetworkView.VERTICAL_CONSTRAINT_BAND_X -
        ConstraintNetworkView.VERTICAL_TOKEN_BAND_X;
    }
    return retval;
  }
  public void positionNodes(double pos) {
    if(!tokenNode.isVisible()) {
      return;
    }
    if(NewConstraintNetworkLayout.layoutHorizontal()) {
      positionHorizontal(pos);
    }
    else {
      positionVertical(pos);
    }
  }
  private void positionVertical(double boxY) {
    double boxHeight = getHeight();
    tokenNode.setLocation((int) ConstraintNetworkView.VERTICAL_TOKEN_BAND_X,
                          (int) (boxY - (boxHeight / 2)));
    double lastBoxHeight = 0.;
    ListIterator varBoxIterator = variableBoundingBoxes.listIterator();
    while(varBoxIterator.hasNext()) {
      VariableBoundingBox varBox = (VariableBoundingBox) varBoxIterator.next();
      lastBoxHeight += varBox.getHeight();
      varBox.positionNodes(boxY - lastBoxHeight);
    }
  }
  private void positionHorizontal(double boxX) {
    double boxWidth = getWidth();
    tokenNode.setLocation((int) (boxX - (boxWidth / 2)),
                          (int) ConstraintNetworkView.HORIZONTAL_TOKEN_BAND_Y);
    double lastBoxWidth = 0.;
    ListIterator varBoxIterator = variableBoundingBoxes.listIterator();
    while(varBoxIterator.hasNext()) {
      VariableBoundingBox varBox = (VariableBoundingBox) varBoxIterator.next();
      lastBoxWidth += varBox.getWidth();
      varBox.positionNodes(boxX - lastBoxWidth);
    }
  }
}
