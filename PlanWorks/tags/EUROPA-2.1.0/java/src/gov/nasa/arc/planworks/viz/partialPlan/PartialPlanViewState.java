// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanViewState.java,v 1.5 2004-03-17 01:45:20 taylor Exp $
//
// PlanWorks -- 

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.Point;
import java.util.List;

public class PartialPlanViewState {

  private List contentSpec;
  private Point contentSpecWindowLocation;
  private int currentZoomFactor;

  public PartialPlanViewState( final PartialPlanView view) {
    contentSpec = ((PartialPlanViewSet)view.getViewSet()).getCurrentSpec();
    contentSpecWindowLocation = null;
    currentZoomFactor = view.getZoomFactor();
  }

  public final List getContentSpec(){
    return contentSpec;
  }

  public final Point getContentSpecWindowLocation() {
    return this.contentSpecWindowLocation;
  }

  public final void setContentSpecWindowLocation( final Point loc) {
    this.contentSpecWindowLocation = loc;
  }

  public final int getCurrentZoomFactor() {
    return currentZoomFactor;
  }

}
