// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NewConstraintNetworkLayout.java,v 1.13 2004-03-17 01:45:21 taylor Exp $
//
package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
//import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;

public class NewConstraintNetworkLayout {
  private List orderedTokenNodes;
  private List tokenBoundingBoxes;
  private boolean horizontalLayout;
  public NewConstraintNetworkLayout( List tokenNodes,
                                     ConstraintNetworkView constraintNetworkView) {
    long t1 = System.currentTimeMillis();
    horizontalLayout = true;
    //horizontalLayout = false;
    orderedTokenNodes = new ArrayList();
    tokenBoundingBoxes = new ArrayList(tokenNodes.size());
    List tempTokenNodes = new ArrayList(tokenNodes);

    if(tokenNodes.isEmpty() || tokenNodes.size() == 0 || tempTokenNodes.isEmpty() || 
       tempTokenNodes.size() == 0) {
      return;
    }

    long t2 = System.currentTimeMillis();
    Collections.sort(tempTokenNodes, new TokenLinkCountComparator());
    System.err.println("Spent " + (System.currentTimeMillis() - t2) + "ms in first sort.");
    if(tempTokenNodes.isEmpty()) {
      System.err.println("Attempted to lay out constraint network with no token nodes!");
      return;
    }
    while(tempTokenNodes.size() > 0 && 
          ((VariableContainerNode) tempTokenNodes.get(tempTokenNodes.size() - 1)).
          getContainerLinkCount() == 0) {
      orderedTokenNodes.add(tempTokenNodes.remove(tempTokenNodes.size() - 1));
    }

    while(!tempTokenNodes.isEmpty()) {
      List connectedComponent = new UniqueSet();
      VariableContainerNode firstNode = (VariableContainerNode) tempTokenNodes.get(0);
      if(firstNode.getContainerLinkCount() == 0) {
        orderedTokenNodes.add(firstNode);
        continue;
      }
      assembleConnectedComponent(connectedComponent, tempTokenNodes, firstNode);

      LinkedList subOrdering = new LinkedList();
      firstNode = (VariableContainerNode) connectedComponent.remove(0);
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
      VariableContainerNode node = (VariableContainerNode) orderedIterator.next();
      VariableContainerBoundingBox box = 
        new VariableContainerBoundingBox(this, node, constraintNetworkView);
      tokenBoundingBoxes.add(box);
    }
    System.err.println("Constraint network init took " + (System.currentTimeMillis() - t1) +
                       "ms");
  }

  private void assembleConnectedComponent(List component, List tokenNodes, 
                                          VariableContainerNode node) {
    if(tokenNodes.isEmpty() || !tokenNodes.contains(node)) {
      return;
    }
    component.add(node);
    tokenNodes.remove(node);
    ListIterator connectedNodeIterator = node.getConnectedContainerNodes().listIterator();
    while(connectedNodeIterator.hasNext()) {
      assembleConnectedComponent(component, tokenNodes, 
                                 (VariableContainerNode) connectedNodeIterator.next());
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
      VariableContainerBoundingBox box = (VariableContainerBoundingBox) tokenBoxIterator.next();
      xpos += box.getWidth();
      box.positionNodes(xpos);
    }
  }
  private void performVerticalLayout() {
    ListIterator tokenBoxIterator = tokenBoundingBoxes.listIterator();
    double ypos = 0.;
    while(tokenBoxIterator.hasNext()) {
      VariableContainerBoundingBox box = (VariableContainerBoundingBox) tokenBoxIterator.next();
      ypos += box.getHeight();
      box.positionNodes(ypos);
    }
  }

  class TokenLinkCountComparator implements Comparator {
    public TokenLinkCountComparator() {
    }
    public int compare(Object o1, Object o2) {
      VariableContainerNode n1 = (VariableContainerNode) o1;
      VariableContainerNode n2 = (VariableContainerNode) o2;
      return n2.getContainerLinkCount() - n1.getContainerLinkCount();
    }
    public boolean equals(Object o){return false;}
  }
  
  class SpecificTokenLinkCountComparator implements Comparator {
    private VariableContainerNode node;
    public SpecificTokenLinkCountComparator(VariableContainerNode node) {
      this.node = node;
    }
    public int compare(Object o1, Object o2) {
      VariableContainerNode n1 = (VariableContainerNode) o1;
      VariableContainerNode n2 = (VariableContainerNode) o2;
      return n2.getContainerLinkCount(node) - n1.getContainerLinkCount(node);
    }
    public boolean equals(Object o){return false;}
  }
}
