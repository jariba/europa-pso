package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;

public class ConstraintNetworkViewState extends PartialPlanViewState {
  private List modTokens;
  private List modVars;
  private List modConstrs;
  private boolean layoutHorizontal;
  public ConstraintNetworkViewState(ConstraintNetworkView view) {
    super(view);
    layoutHorizontal = view.getNewLayout().layoutHorizontal();
    modTokens = new LinkedList();
    modVars = new LinkedList();
    modConstrs = new LinkedList();
    ListIterator nodeIterator = view.getTokenNodeList().listIterator();
    while(nodeIterator.hasNext()) {
      ConstraintNetworkTokenNode node = (ConstraintNetworkTokenNode) nodeIterator.next();
      if(node.areNeighborsShown()) {
        modTokens.add(node.getToken().getId());
      }
    }
    nodeIterator = view.getVariableNodeList().listIterator();
    while(nodeIterator.hasNext()) {
      VariableNode node = (VariableNode) nodeIterator.next();
      if(node.areNeighborsShown()) {
        modVars.add(node.getVariable().getId());
      }
    }
    nodeIterator = view.getConstraintNodeList().listIterator();
    while(nodeIterator.hasNext()) {
      ConstraintNode node = (ConstraintNode) nodeIterator.next();
      if(node.areNeighborsShown()) {
        modConstrs.add(node.getConstraint().getId());
      }
    }
  }
  public List getModTokens(){return modTokens;}
  public List getModVariables(){return modVars;}
  public List getModConstraints(){return modConstrs;}
  public boolean layoutHorizontal(){return layoutHorizontal;}
}
