package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ObjectBoundingCircle {
  private List childCircles;
  private double x;
  private double y;
  private NavigatorNode node;
  private NavigatorLayout layout;
  public ObjectBoundingCircle(NavigatorLayout layout, NavigatorNode node) {
    List childNodes = node.getChildren();
    childCircles = new ArrayList(childNodes.size());
    this.node = node;
    ListIterator childIterator = childNodes.listIterator();
    while(childIterator.hasNext()) {
      NavigatorNode child = (NavigatorNode) childIterator.next();
      ObjectBoundingCircle childCircle = layout.getBoundingCircle(child);
      if(childCircle == null) {
        childCircle = new ObjectBoundingCircle(layout, child);
      }
      childNodes.add(childCircle);
    }
  }
  public double radius() {
    double maxChildRad = 0.0;
    ListIterator childIterator = childCircles.listIterator();
    while(childIterator.hasNext()) {
      ObjectBoundingCircle child = (ObjectBoundingCircle) childIterator.next();
      
    }
    return 0.;
  }
}
