// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanViewState.java,v 1.2 2004-02-10 02:35:54 taylor Exp $
//
// PlanWorks -- 

package gov.nasa.arc.planworks.viz.partialPlan;

import java.util.List;

public class PartialPlanViewState {
  private List contentSpec;
  public PartialPlanViewState(PartialPlanView view) {
    // this returns null - will 09feb04
    // contentSpec = ((PartialPlanViewSet)view.getViewSet()).getCurrentSpec();
    contentSpec = view.getPartialPlan().getContentSpec();
  }
  public List getContentSpec(){
    return contentSpec;
  }
}
