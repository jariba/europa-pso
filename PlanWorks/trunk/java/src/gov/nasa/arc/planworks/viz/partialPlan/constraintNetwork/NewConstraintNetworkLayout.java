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
    horizontalLayout = true;
    this.variableNodes = variableNodes;
    this.constraintNodes = constraintNodes;
    orderedTokenNodes = new ArrayList();
    tokenBoundingBoxes = new ArrayList(tokenNodes.size());
    variableBoundingBoxes = new ArrayList(variableNodes.size());

    List tempTokenNodes = new ArrayList(tokenNodes);
    Collections.sort(tempTokenNodes, new TokenLinkCountComparator());

    while(!tempTokenNodes.isEmpty()) {
      List connectedComponent = new UniqueSet();
      ConstraintNetworkTokenNode firstNode = (ConstraintNetworkTokenNode) tempTokenNodes.remove(0);
      connectedComponent.add(firstNode);
      List connections = firstNode.getConnectedTokenNodes();
      while(!connections.isEmpty()) {
        ConstraintNetworkTokenNode connectedNode =
          (ConstraintNetworkTokenNode) connections.remove(0);
        if(connectedComponent.contains(connectedNode)) {
          continue;
        }
        connectedComponent.add(connectedNode);
        tempTokenNodes.remove(connectedNode);
        List subConnections = connectedNode.getConnectedTokenNodes();
        subConnections.removeAll(connections);
        subConnections.removeAll(connectedComponent);
        connections.addAll(subConnections);
      }
      Collections.sort(connectedComponent, new TokenLinkCountComparator());
      LinkedList subOrdering = new LinkedList();
      while(!connectedComponent.isEmpty()) {
        ConstraintNetworkTokenNode maxNode = 
          (ConstraintNetworkTokenNode) connectedComponent.remove(0);
        List temp = new ArrayList(connectedComponent);
        Collections.sort(temp, new SpecificTokenLinkCountComparator(maxNode));
        subOrdering.add(maxNode);
        boolean switcher = false;
        ListIterator tempIterator = temp.listIterator();
        while(tempIterator.hasNext()) {
          ConstraintNetworkTokenNode fooNode = (ConstraintNetworkTokenNode) tempIterator.next();
          if(switcher) {
            subOrdering.addFirst(fooNode);
          }
          else {
            subOrdering.addLast(fooNode);
          }
          switcher ^= true;
          connectedComponent.remove(fooNode);
        }
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
    //performLayout();
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
    System.err.println("New layout took " + (System.currentTimeMillis() - t1) + "ms");
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
        System.err.println("Positioning box at " + xpos);
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
