package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class NavigatorLayout {
  private List navigatorNodes;
  public NavigatorLayout() {
  }
  
  private List getCycles() throws Exception {
    List retval = new ArrayList(); //list of lists of nodes
    List visibleNodes = getVisibleNodes();
    Collections.sort(visibleNodes, new LinkCountComparator());
    while(!visibleNodes.isEmpty()) {
      LinkedList cycle = new LinkedList();
      NavigatorNode firstNode = (NavigatorNode) visibleNodes.remove(0);
      buildCycle(cycle, firstNode, firstNode);
    }
    return retval;
  }

  private void buildCycle(LinkedList cycle, NavigatorNode firstNode, NavigatorNode node) 
    throws Exception {
    if(node == null) {
      return;
    }
    if(firstNode.equals(node)) {
      return;
    }
    cycle.addLast(node);
    node.setVisited(true);
    List edges = node.getVisibleEdges();
    if(edges.size() == 0) {
      if(cycle.size() == 1) {
        return;
      }
      else {
        throw new Exception("Error, reached unreachable node: " + node);
      }
    }
    
  }

  private List getVisibleNodes() {
    List retval = new ArrayList();
    ListIterator nodeIterator = navigatorNodes.listIterator();
    while(nodeIterator.hasNext()) {
      NavigatorNode node = (NavigatorNode) nodeIterator.next();
      if(node.isVisible()) {
        node.setVisited(false);
        retval.add(node);
      }
    }
    return retval;
  }


  public ObjectBoundingCircle getBoundingCircle(NavigatorNode node) {
    return null;
  }
}

class LinkCountComparator implements Comparator {
  public LinkCountComparator(){}
  public int compare(Object o1, Object o2) {
    NavigatorNode n1 = (NavigatorNode) o1;
    NavigatorNode n2 = (NavigatorNode) o2;
    return n2.getVisibleLinkCount() - n1.getVisibleLinkCount();
  }
  public boolean equals(Object o){return false;}
}
