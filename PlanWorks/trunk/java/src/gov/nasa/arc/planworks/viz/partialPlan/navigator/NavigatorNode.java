package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.util.List;

public interface NavigatorNode {
  public List getChildren();
  public List getVisibleChildren();
  public List getVisibleEdges();
  public int getVisibleLinkCount();
  public boolean beenVisited();
  public void setVisited(boolean visited);
  public boolean isVisible();
  public void setVisible(boolean visible);
}
