// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanViewState.java,v 1.3 2004-02-10 17:49:13 miatauro Exp $
//
// PlanWorks -- 

package gov.nasa.arc.planworks.viz.partialPlan;

import java.util.List;

public class PartialPlanViewState {
  private List contentSpec;
  public PartialPlanViewState(PartialPlanView view) {
    contentSpec = ((PartialPlanViewSet)view.getViewSet()).getCurrentSpec();
  }
  public List getContentSpec(){
    return contentSpec;
  }
}
