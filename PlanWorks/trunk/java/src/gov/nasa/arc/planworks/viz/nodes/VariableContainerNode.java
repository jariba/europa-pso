package gov.nasa.arc.planworks.viz.nodes;

import java.util.List;

import gov.nasa.arc.planworks.db.PwVariableContainer;

public interface VariableContainerNode {
  public abstract PwVariableContainer getContainer();
  public abstract List getVariableNodes();
}
