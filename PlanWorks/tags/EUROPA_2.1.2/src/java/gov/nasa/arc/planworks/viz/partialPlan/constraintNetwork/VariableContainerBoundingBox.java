// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: VariableContainerBoundingBox.java,v 1.2 2004-03-17 01:45:21 taylor Exp $
//
package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;

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
public class VariableContainerBoundingBox {
  private VariableContainerNode varCont;
  private ArrayList variableBoundingBoxes;
  private NewConstraintNetworkLayout layout;
  private ConstraintNetworkView constraintNetworkView;
  private int zoomDimensionDelta;

  public VariableContainerBoundingBox(final NewConstraintNetworkLayout layout, 
                                      final VariableContainerNode node,
                                      final ConstraintNetworkView constraintNetworkView) {
    this.varCont = node;
    this.layout = layout;
    this.constraintNetworkView = constraintNetworkView;
    zoomDimensionDelta = 2 * (constraintNetworkView.getOpenJGoPenWidth
                              ( constraintNetworkView.getZoomFactor()) - 2);
    variableBoundingBoxes = new ArrayList(varCont.getVariableNodes().size());
    ListIterator variableIterator = varCont.getVariableNodes().listIterator();
    while(variableIterator.hasNext()) {
      variableBoundingBoxes.add(new VariableBoundingBox((VariableNode) variableIterator.next(), 
                                                        layout, constraintNetworkView));
    }
  }
  public VariableContainerBoundingBox(final NewConstraintNetworkLayout layout, 
                                      final VariableContainerNode node,
                                      final List variables) {
    this.varCont = node;
    this.layout = layout;
    variableBoundingBoxes = new ArrayList();
    ListIterator variableIterator = variables.listIterator();
    while(variableIterator.hasNext()) {
      variableBoundingBoxes.add(new VariableBoundingBox((VariableNode)variableIterator.next(),
                                                        layout, constraintNetworkView));
    }
  }

  public void addVariable(VariableNode variable) {
    VariableBoundingBox temp = new VariableBoundingBox(variable, layout, constraintNetworkView);
    //apparently this isn't calling .equals() correctly.  have to do it by hand...
    //     if(!variableBoundingBoxes.contains(temp)) {
    //       variableBoundingBoxes.add(temp);
    //     }
    for(int i = 0; i < variableBoundingBoxes.size(); i++) {
      if(((VariableBoundingBox)variableBoundingBoxes.get(i)).equals(temp)) {
        return;
      }
    }
    variableBoundingBoxes.add(temp);
  }
  public boolean isVisible() { return varCont.isVisible();}
  
  public double getHeight() {
    double retval = 0.;
    if(layout.layoutHorizontal()) {
      retval = ConstraintNetworkView.HORIZONTAL_TOKEN_BAND_Y -
        ConstraintNetworkView.HORIZONTAL_CONSTRAINT_BAND_Y;
    }
    else {
      if(varCont.isVisible()) {
        createNecessaryNodes();
        ListIterator variableBoxIterator = variableBoundingBoxes.listIterator();
        double varBoxesHeight = 0.;
        while(variableBoxIterator.hasNext()) {
          varBoxesHeight += ((VariableBoundingBox)variableBoxIterator.next()).getHeight();
          varBoxesHeight += zoomDimensionDelta;
        }
        retval = Math.max(varCont.getSize().getHeight() + ConstraintNetworkView.NODE_SPACING,
                          varBoxesHeight + ConstraintNetworkView.NODE_SPACING);
      }
    }
    return retval;
  }

  public double getWidth() {
    double retval = 0.;
    if(layout.layoutHorizontal()) {
      if(varCont.isVisible()) {
        createNecessaryNodes();
        ListIterator variableBoxIterator = variableBoundingBoxes.listIterator();
        double varBoxesWidth = 0.;
        while(variableBoxIterator.hasNext()) {
          varBoxesWidth += ((VariableBoundingBox)variableBoxIterator.next()).getWidth();
          varBoxesWidth += zoomDimensionDelta;
        }
        retval = Math.max(varCont.getSize().getWidth() + ConstraintNetworkView.NODE_SPACING,
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
    if(!varCont.isVisible()) {
      return;
    }
    createNecessaryNodes();
    if(layout.layoutHorizontal()) {
      positionHorizontal(pos);
    }
    else {
      positionVertical(pos);
    }
  }
  private void positionVertical(double boxY) {
    double boxHeight = getHeight();
    varCont.setLocation((int) ConstraintNetworkView.VERTICAL_TOKEN_BAND_X,
                        (int) (boxY - (boxHeight / 2)));
    double lastBoxHeight = 0.;
    ListIterator varBoxIterator = variableBoundingBoxes.listIterator();
    while(varBoxIterator.hasNext()) {
      VariableBoundingBox varBox = (VariableBoundingBox) varBoxIterator.next();
      varBox.positionNodes(boxY - lastBoxHeight);
      lastBoxHeight += varBox.getHeight();
      varBox.setVisited();
    }
  }
  private void positionHorizontal(double boxX) {
    double boxWidth = getWidth();
    varCont.setLocation((int) (boxX - (boxWidth / 2)),
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
  private void createNecessaryNodes() {
    if(variableBoundingBoxes.size() > varCont.getVariableNodes().size()) {
      System.err.println("Too many bounding boxes in token " + varCont);
      System.err.println("box:  " + variableBoundingBoxes);
      System.err.println("node: " + varCont.getVariableNodes());
      for(int i = 0; i < variableBoundingBoxes.size(); i++) {
        for(int j = i + 1; j < variableBoundingBoxes.size(); j++) {
          if(((VariableBoundingBox)variableBoundingBoxes.get(i)).
             equals((VariableBoundingBox)variableBoundingBoxes.get(j))) {
            variableBoundingBoxes.remove(j);
          }
        }
      }
      return;
    }
    if(variableBoundingBoxes.size() < varCont.getVariableNodes().size()) {
      ListIterator variableIterator = varCont.getVariableNodes().listIterator();
      while(variableIterator.hasNext()) {
        addVariable((VariableNode) variableIterator.next());
      }
    }
  }
  public List getVariableBoxes() {
    return variableBoundingBoxes;
  }
}
