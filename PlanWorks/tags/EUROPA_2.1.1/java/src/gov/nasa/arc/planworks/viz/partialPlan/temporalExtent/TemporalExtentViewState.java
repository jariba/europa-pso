// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TemporalExtentViewState.java,v 1.2 2004-02-03 20:43:58 taylor Exp $
//
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
