package gov.nasa.arc.planworks.viz.partialPlan;

import java.util.List;

public class PartialPlanViewState {
  private List contentSpec;
  public PartialPlanViewState(PartialPlanView view) {
    contentSpec = ((PartialPlanViewSet)view.getViewSet()).getCurrentSpec();
  }
  public List getContentSpec(){return contentSpec;}
}
