package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;

import gov.nasa.arc.planworks.db.PwVariableContainer;

public interface VariableContainerNode {
  public abstract PwVariableContainer getContainer();
  public abstract List getVariableNodes();
  public abstract boolean isVisible();
  public abstract Dimension getSize();
  public abstract void setLocation(int x, int y);
}
