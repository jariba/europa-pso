package gov.nasa.arc.planworks.viz.partialPlan.temporalExtent;

import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;

class TemporalExtentViewState extends PartialPlanViewState {
  private boolean isShowLabels;
  private int temporalDisplayMode;
  public TemporalExtentViewState(TemporalExtentView view) {
    super(view);
    isShowLabels = view.showLabels();
    temporalDisplayMode = view.displayMode();
  }
  public boolean showingLabels() {return isShowLabels;}
  public int displayMode(){return temporalDisplayMode;}
}
