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
  private static boolean horizontalLayout;
  public NewConstraintNetworkLayout(List tokenNodes, List variableNodes, List constraintNodes) {
    this.variableNodes = variableNodes;
    this.constraintNodes = constraintNodes;
    orderedTokenNodes = new ArrayList();

    List tempTokenNodes = new ArrayList(tokenNodes);
    Collections.sort(tempTokenNodes, new TokenLinkCountComparator());

    ListIterator printIterator = tempTokenNodes.listIterator();
    System.err.println("---------------------");
    while(printIterator.hasNext()) {
      ConstraintNetworkTokenNode printNode = (ConstraintNetworkTokenNode) printIterator.next();
      System.err.println(printNode + ": " + printNode.getTokenLinkCount());
    }
    System.err.println("---------------------");

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

    printIterator = tokenNodes.listIterator();
    System.err.println("---------------------");
    while(printIterator.hasNext()) {
      ConstraintNetworkTokenNode printNode = (ConstraintNetworkTokenNode) printIterator.next();
      System.err.println(printNode + ": " + printNode.getTokenLinkCount());
    }
    System.err.println("---------------------");
  }
  public static void setLayoutHorizontal() {
    horizontalLayout = true;
  }
  public static void setLayoutVertical() {
    horizontalLayout = false;
  }
  public static boolean layoutHorizontal() {
    return horizontalLayout;
  }
  class TokenLinkCountComparator implements Comparator {
    public TokenLinkCountComparator() {
    }
    public int compare(Object o1, Object o2) {
      ConstraintNetworkTokenNode n1 = (ConstraintNetworkTokenNode) o1;
      ConstraintNetworkTokenNode n2 = (ConstraintNetworkTokenNode) o2;
      return n1.getTokenLinkCount() - n2.getTokenLinkCount();
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
      return n1.getTokenLinkCount(node) - n2.getTokenLinkCount(node);

    }
    public boolean equals(Object o){return false;}
  }
}
