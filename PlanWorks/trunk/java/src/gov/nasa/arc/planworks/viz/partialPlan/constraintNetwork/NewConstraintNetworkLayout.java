package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;

public class NewConstraintNetworkLayout {
  private List orderedTokenNodes;
  private List variableNodes;
  private List constraintNodes;
  private List tokenBoundingBoxes;
  private List variableBoundingBoxes;
  private boolean horizontalLayout;
  public NewConstraintNetworkLayout(List tokenNodes, List variableNodes, List constraintNodes) {
    long t1 = System.currentTimeMillis();
    horizontalLayout = true;
    //horizontalLayout = false;
    this.variableNodes = variableNodes;
    this.constraintNodes = constraintNodes;
    orderedTokenNodes = new ArrayList();
    tokenBoundingBoxes = new ArrayList(tokenNodes.size());
    variableBoundingBoxes = new ArrayList(variableNodes.size());

    List tempTokenNodes = new ArrayList(tokenNodes);

    long t2 = System.currentTimeMillis();
    Collections.sort(tempTokenNodes, new TokenLinkCountComparator());
    System.err.println("Spent " + (System.currentTimeMillis() - t2) + "ms in first sort.");

    while(((ConstraintNetworkTokenNode) tempTokenNodes.get(tempTokenNodes.size() - 1)).getTokenLinkCount() == 0) {
      orderedTokenNodes.add(tempTokenNodes.remove(tempTokenNodes.size() - 1));
    }

    while(!tempTokenNodes.isEmpty()) {
      List connectedComponent = new UniqueSet();
      ConstraintNetworkTokenNode firstNode = (ConstraintNetworkTokenNode) tempTokenNodes.get(0);
      if(firstNode.getTokenLinkCount() == 0) {
        orderedTokenNodes.add(firstNode);
        continue;
      }
      assembleConnectedComponent(connectedComponent, tempTokenNodes, firstNode);

      //Collections.sort(connectedComponent, new TokenLinkCountComparator());
      LinkedList subOrdering = new LinkedList();
      firstNode = (ConstraintNetworkTokenNode) connectedComponent.remove(0);
      Collections.sort(connectedComponent, new SpecificTokenLinkCountComparator(firstNode));
      subOrdering.add(firstNode);
      boolean switcher = false;
      ListIterator connCompIterator = connectedComponent.listIterator();
      while(connCompIterator.hasNext()) {
        if(switcher) {
          subOrdering.addFirst(connCompIterator.next());
        }
        else {
          subOrdering.addLast(connCompIterator.next());
        }
        switcher ^= true;
      }
      orderedTokenNodes.addAll(subOrdering);
    }

    ListIterator orderedIterator = orderedTokenNodes.listIterator();
    while(orderedIterator.hasNext()) {
      ConstraintNetworkTokenNode node = (ConstraintNetworkTokenNode) orderedIterator.next();
      TokenBoundingBox box = new TokenBoundingBox(this, node);
      variableBoundingBoxes.addAll(box.getVariableBoxes());
      tokenBoundingBoxes.add(box);
    }
    System.err.println("Constraint network init took " + (System.currentTimeMillis() - t1) +
                       "ms");
    //performLayout();
  }

  private void assembleConnectedComponent(List component, List tokenNodes, 
                                          ConstraintNetworkTokenNode node) {
    if(tokenNodes.isEmpty() || !tokenNodes.contains(node)) {
      return;
    }
    component.add(node);
    tokenNodes.remove(node);
    ListIterator connectedNodeIterator = node.getConnectedTokenNodes().listIterator();
    while(connectedNodeIterator.hasNext()) {
      assembleConnectedComponent(component, tokenNodes, 
                                 (ConstraintNetworkTokenNode) connectedNodeIterator.next());
    }
  }
  public void setLayoutHorizontal() {
    horizontalLayout = true;
  }
  public void setLayoutVertical() {
    horizontalLayout = false;
  }
  public  boolean layoutHorizontal() {
    return horizontalLayout;
  }

  public void performLayout() {
    long t1 = System.currentTimeMillis();
    ListIterator varBoxIterator = variableBoundingBoxes.listIterator();
    while(varBoxIterator.hasNext()) {
      ((VariableBoundingBox)varBoxIterator.next()).clearVisited();
    }
    if(horizontalLayout) {
      performHorizontalLayout();
    }
    else {
      performVerticalLayout();
    }
    System.err.println("   ... New layout took " + (System.currentTimeMillis() - t1) + "ms");
  }
  private void performHorizontalLayout() {
    ListIterator tokenBoxIterator = tokenBoundingBoxes.listIterator();
    double xpos = 0.;
    while(tokenBoxIterator.hasNext()) {
      TokenBoundingBox box = (TokenBoundingBox) tokenBoxIterator.next();
      xpos += box.getWidth();
      box.positionNodes(xpos);
    }
    ListIterator variableBoxIterator = variableBoundingBoxes.listIterator();
    while(variableBoxIterator.hasNext()) {
      VariableBoundingBox box = (VariableBoundingBox) variableBoxIterator.next();
      if(box.isVisible() && !box.wasVisited()) {
        xpos += box.getWidth();
        box.positionNodes(xpos);
      }
    }
  }
  private void performVerticalLayout() {
    ListIterator tokenBoxIterator = tokenBoundingBoxes.listIterator();
    double ypos = 0.;
    while(tokenBoxIterator.hasNext()) {
      TokenBoundingBox box = (TokenBoundingBox) tokenBoxIterator.next();
      ypos += box.getHeight();
      box.positionNodes(ypos);
    }
    ListIterator variableBoxIterator = variableBoundingBoxes.listIterator();
    while(variableBoxIterator.hasNext()) {
      VariableBoundingBox box = (VariableBoundingBox) variableBoxIterator.next();
      if(box.isVisible() && !box.wasVisited()) {
        ypos += box.getHeight();
        box.positionNodes(ypos);
      }
    }
  }

  class TokenLinkCountComparator implements Comparator {
    public TokenLinkCountComparator() {
    }
    public int compare(Object o1, Object o2) {
      ConstraintNetworkTokenNode n1 = (ConstraintNetworkTokenNode) o1;
      ConstraintNetworkTokenNode n2 = (ConstraintNetworkTokenNode) o2;
      return n2.getTokenLinkCount() - n1.getTokenLinkCount();
    }
    public boolean equals(Object o){return false;}
  }
  
  class SpecificTokenLinkCountComparator implements Comparator {
    private ConstraintNetworkTokenNode node;
    public SpecificTokenLinkCountComparator(ConstraintNetworkTokenNode node) {
      this.node = node;
    }
    public int compare(Object o1, Object o2) {
      ConstraintNetworkTokenNode n1 = (ConstraintNetworkTokenNode) o1;
      ConstraintNetworkTokenNode n2 = (ConstraintNetworkTokenNode) o2;
      return n2.getTokenLinkCount(node) - n1.getTokenLinkCount(node);
    }
    public boolean equals(Object o){return false;}
  }
}
