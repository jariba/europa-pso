// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintNetworkViewState.java,v 1.3 2004-02-19 21:57:52 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;

public class ConstraintNetworkViewState extends PartialPlanViewState {
  private List modContainers;
  private List modVars;
  private List modConstrs;
  private boolean layoutHorizontal;
  public ConstraintNetworkViewState(ConstraintNetworkView view) {
    super(view);
    layoutHorizontal = view.getNewLayout().layoutHorizontal();
    modContainers = new LinkedList();
    modVars = new LinkedList();
    modConstrs = new LinkedList();
    ListIterator nodeIterator = view.getContainerNodeList().listIterator();
    while(nodeIterator.hasNext()) {
      VariableContainerNode node = (VariableContainerNode) nodeIterator.next();
      if(node.areNeighborsShown()) {
        modContainers.add(node.getContainer().getId());
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
  public List getModContainers(){return modContainers;}
  public List getModVariables(){return modVars;}
  public List getModConstraints(){return modConstrs;}
  public boolean layoutHorizontal(){return layoutHorizontal;}
}
