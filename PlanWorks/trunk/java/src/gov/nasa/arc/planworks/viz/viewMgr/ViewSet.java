//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ViewSet.java,v 1.3 2003-06-10 20:35:07 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.awt.Container;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.views.VizView;

public class ViewSet implements RedrawNotifier, ContentSpecChecker {
  private MDIDesktopFrame desktopFrame;
  private HashMap views;
  private ContentSpec contentSpec;
  private PwPartialPlan partialPlan;
  private ViewSetRemover remover;
  private String planName;
  public ViewSet(MDIDesktopFrame desktopFrame, PwPartialPlan partialPlan, String planName, 
                 ViewSetRemover remover) {
    this.views = new HashMap();
    this.partialPlan = partialPlan;
    this.planName = planName;
    this.remover = remover;
    this.desktopFrame = desktopFrame;
    this.contentSpec = new ContentSpec(partialPlan.getCollectionName());
  }

  public MDIInternalFrame openTimelineView() {
    if(views.containsKey("timelineView")) {
      return (MDIInternalFrame) views.get("timelineView");
    }
    MDIInternalFrame timelineViewFrame = desktopFrame.createFrame(planName,true, true, true, true);
    Container contentPane = timelineViewFrame.getContentPane();
    contentPane.add(new TimelineView(partialPlan, this));
    views.put("timelineView", timelineViewFrame);
    return timelineViewFrame;
  }

  //  public void addViewFrame(MDIInternalFrame viewFrame) {
  // if(!views.contains(viewFrame)) {
  //   views.add(viewFrame);
  // }
  //}
  public void removeViewFrame(MDIInternalFrame viewFrame) {
    if(views.containsValue(viewFrame)) {
      Container contentPane = viewFrame.getContentPane();
      for(int i = 0; i < contentPane.getComponentCount(); i++) {
        if(contentPane.getComponent(i) instanceof TimelineView) {
          views.remove("timelineView");
        }
      }
    }
    if(views.isEmpty()) {
      remover.removeViewSet(partialPlan);
    }
  }
  public void notifyRedraw() {
    Iterator viewFrameIterator = views.values().iterator();
    while(viewFrameIterator.hasNext()) {
      MDIInternalFrame viewFrame = (MDIInternalFrame) viewFrameIterator.next();
      Container contentPane = viewFrame.getContentPane();
      for(int i = 0; i < contentPane.getComponentCount(); i++) {
        if(contentPane.getComponent(i) instanceof VizView) {
          ((VizView)contentPane.getComponent(i)).redraw();
          break;
        }
      }
    }
  }
  public boolean isInContentSpec(String key)
  {
    return contentSpec.isInContentSpec(key);
  }
}
