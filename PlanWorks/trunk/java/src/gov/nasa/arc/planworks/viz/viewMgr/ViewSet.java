//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ViewSet.java,v 1.11 2003-06-16 18:50:39 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.awt.Container;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.views.VizView;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.ContentSpecWindow;

/**
 * <code>ViewSet</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A class to manage the views associated with a partial plan.
 */

//maybe the hashmap should be changed.  is that much flexibility really necessary?
public class ViewSet implements RedrawNotifier, ContentSpecChecker {
  private MDIDesktopFrame desktopFrame;
  private HashMap views;
  private ContentSpec contentSpec;
  private PwPartialPlan partialPlan;
  private ViewSetRemover remover;
  private String planName;
  private MDIInternalFrame contentSpecWindow;

  /**
   * Creates the ViewSet object, creates a new ContentSpec, and creates storage for the new views.
   * @param desktopFrame the desktop into which the views will be added.
   * @param partialPlan the partialPlan to be viewed.
   * @param planName the name of the partial plan.  Used to visually distinguish views across
   *                 partial plans.
   * @param remover the interface which is responsible for removing entire ViewSets.
   */
  public ViewSet(MDIDesktopFrame desktopFrame, PwPartialPlan partialPlan, String planName, 
                 ViewSetRemover remover) {
    this.views = new HashMap();
    this.partialPlan = partialPlan;
    this.planName = planName;
    this.remover = remover;
    this.desktopFrame = desktopFrame;
    this.contentSpec = new ContentSpec(partialPlan.getCollectionName(), this);
    //change the arguments to something more sensible
    System.err.println(planName);
    this.contentSpecWindow = 
      desktopFrame.createFrame("Content specification for ".concat(planName), true, true, true,
                               true);
    Container contentPane = this.contentSpecWindow.getContentPane();
    contentPane.add(new ContentSpecWindow(this.contentSpecWindow, contentSpec));
    this.contentSpecWindow.pack();
    this.contentSpecWindow.setVisible(true);
  }

  /**
   * Opens a new TimelineView, stuffs it in an MDIInternalFrame, adds it to the hash of frames,
   * then returns it.  If a TimelineView already exists for this partial plan, returns that.
   * @return MDIInternalFrame the frame containing the vew.
   */
  public MDIInternalFrame openTimelineView() {
    if(viewExists("timelineView")) {
      return (MDIInternalFrame) views.get("timelineView");
    }
    MDIInternalFrame timelineViewFrame = desktopFrame.createFrame("Timeline view of ".concat(planName),true, true, true, true);
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

  /**
   * Removes a view from the ViewSet.  If there are no more views extant, removes the ViewSet.
   * @param MDIInternalFrame the frame containing the view.
   */
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
  /**
   * Notifies all open views of a change in the ContentSpec, and therefore a need to redraw.
   */
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
  /**
   * Determines whether or not a key is in the current content specification.  Used by VizView to
   * determine which elements to draw.
   * @param key the key being checked.
   * @return boolean the truth value of the statement "This key is in the specification."
   */
  public boolean isInContentSpec(String key) {
    return contentSpec.isInContentSpec(key);
  }
  public void printSpec() {
    contentSpec.printSpec();
  }
  /**
   * Closes all of the views in the ViewSet.
   */
  public void close() {
    Collection viewSet = views.values();
    Iterator viewIterator = viewSet.iterator();
    try
      {
        while(viewIterator.hasNext()) {
          ((MDIInternalFrame)viewIterator.next()).setClosed(true);
        }
        contentSpecWindow.setClosed(true);
      }
    catch(PropertyVetoException pve){}
  }
  /**
   * Determines whether or not a view already exists.
   * @param viewName the name of the view
   * @return boolean whether or not the view exists.
   */
  public boolean viewExists(String viewName) {
    return views.containsKey(viewName);
  }
}
