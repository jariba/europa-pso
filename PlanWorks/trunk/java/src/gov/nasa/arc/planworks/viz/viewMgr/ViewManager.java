package gov.nasa.arc.planworks.viz.viewMgr;

import java.util.HashMap;

import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;

public class ViewManager implements ViewSetRemover {
  private MDIDesktopFrame desktopFrame;
  private HashMap viewSets;
  public ViewManager(MDIDesktopFrame desktopFrame) {
    viewSets = new HashMap();
    this.desktopFrame = desktopFrame;
  }
  public MDIInternalFrame openTimelineView(PwPartialPlan partialPlan, String planName) {
    if(!viewSets.containsKey(partialPlan)) {
      viewSets.put(partialPlan, new ViewSet(desktopFrame, partialPlan, planName, this));
    }
    return ((ViewSet)viewSets.get(partialPlan)).openTimelineView();
  }
  /*
  public MDIInternalFrame openConstraintNetworkView(PwPartialPlan partialPlan, String planName) {
  }
  public MDIInternalFrame openTemporalExtentView(PwPartialPlan partialPlan, String planName) {
  }
  public MDIInternalFrame openTemporalNetworkView(PwPartialPlan partialPlan, String planName) {
  }
  public MDIInternalFrame openTokenNetworkView(PwPartialPlan partialPlan, String planName) {
  }
  */
  public void removeViewSet(PwPartialPlan key) {
    viewSets.remove(key);
  }
}
