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
  private NewConstraintNetworkLayout layout;
  public TokenBoundingBox(NewConstraintNetworkLayout layout, ConstraintNetworkTokenNode tokenNode) {
    this.tokenNode = tokenNode;
    this.layout = layout;
    variableBoundingBoxes = new LinkedList();
    ListIterator variableIterator = tokenNode.getVariableNodeList().listIterator();
    while(variableIterator.hasNext()) {
      variableBoundingBoxes.add(new VariableBoundingBox((VariableNode) variableIterator.next(), 
                                                        layout));
    }
  }
  public TokenBoundingBox(NewConstraintNetworkLayout layout, ConstraintNetworkTokenNode tokenNode,
                          List variables) {
    this.tokenNode = tokenNode;
    this.layout = layout;
    variableBoundingBoxes = new LinkedList();
    ListIterator variableIterator = variables.listIterator();
    while(variableIterator.hasNext()) {
      variableBoundingBoxes.add(new VariableBoundingBox((VariableNode)variableIterator.next(),
                                                        layout));
    }
  }
  public void addVariable(VariableNode variable) {
    variableBoundingBoxes.add(new VariableBoundingBox(variable, layout));
  }
  public boolean isVisible() { return tokenNode.isVisible();}
  public double getHeight() {
    double retval = 0.;
    if(layout.layoutHorizontal()) {
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
        retval = Math.max(tokenNode.getSize().getHeight() + ConstraintNetworkView.NODE_SPACING,
                          varBoxesHeight + ConstraintNetworkView.NODE_SPACING);
      }
    }
    return retval;
  }
  public double getWidth() {
    double retval = 0.;
    if(layout.layoutHorizontal()) {
      if(tokenNode.isVisible()) {
        ListIterator variableBoxIterator = variableBoundingBoxes.listIterator();
        double varBoxesWidth = 0.;
        while(variableBoxIterator.hasNext()) {
          varBoxesWidth += ((VariableBoundingBox)variableBoxIterator.next()).getWidth();
        }
        retval = Math.max(tokenNode.getSize().getWidth() + ConstraintNetworkView.NODE_SPACING,
                          varBoxesWidth + ConstraintNetworkView.NODE_SPACING);
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
    if(layout.layoutHorizontal()) {
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
      varBox.setVisited();
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
      varBox.positionNodes(boxX - lastBoxWidth);
      lastBoxWidth += varBox.getWidth();
      varBox.setVisited();
    }
  }
  public List getVariableBoxes() {
    return variableBoundingBoxes;
  }
}
