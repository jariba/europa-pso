package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;

public class NewConstraintNetworkLayout {
  private List orderedTokenNodes;
  private List variableNodes;
  private List constraintNodes;
  public NewConstraintNetworkLayout(List tokenNodes, List variableNodes, List constraintNodes) {
    this.variableNodes = variableNodes;
    this.constraintNodes = constraintNodes;

    List tempTokenNodes = new ArrayList(tokenNodes);
    Collections.sort(tempTokenNodes, new TokenLinkCountComparator());
    while(tempTokenNodes.size() != 0) {
      ConstraintNetworkTokenNode node = (ConstraintNetworkTokenNode) tempTokenNodes.remove(0);
      List subSortNodes = new ArrayList(tempTokenNodes);
      Collections.sort(subSortNodes, new SpecificTokenLinkCountComparator(node));
      
    }
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
