package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import java.util.Map;

import com.nwoods.jgo.JGoPen;

import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;

public interface VariableContainerNode {
  public abstract PwVariableContainer getContainer();
  public abstract List getVariableNodes();
  public abstract boolean isVisible();
  public abstract Dimension getSize();
  public abstract void setLocation(int x, int y);
  public abstract int getContainerLinkCount();
  public abstract int getContainerLinkCount(VariableContainerNode other);
  public abstract List getConnectedContainerNodes();
  public abstract boolean areNeighborsShown();
  public abstract void setAreNeighborsShown(boolean v);
  public abstract void setPen(JGoPen pen);
  public abstract void discoverLinkage();
  public abstract void connectNodes(Map m);
  public abstract Color getColor();
  public abstract void addVariableNode(Object n);
  public abstract void incrVariableLinkCount();
  public abstract void decVariableLinkCount();
  public abstract void setVisible(boolean b);
  public abstract void addContainerNodeVariables(Object p, Object v, boolean doRedraw);
  public abstract void mouseRightPopupMenu(Point p);
  public abstract void addContainerNodeVariables(VariableContainerNode n,
                                                 ConstraintNetworkView v, boolean doRedraw);
  public abstract void removeContainerNodeVariables(VariableContainerNode n,
                                                    ConstraintNetworkView v);
}
