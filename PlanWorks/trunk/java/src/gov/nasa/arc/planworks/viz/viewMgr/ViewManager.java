//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ViewManager.java,v 1.7 2003-07-09 16:50:47 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

/**
 * <code>ViewManager</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A class to manage the various views.  A user can have, at any time, one each of up to five
 * different views per partial plan in the current project.  The ViewManager keeps track of 
 * ViewSets by their associted PwPartialPlan.
 */

public class ViewManager implements ViewSetRemover {
  private MDIDesktopFrame desktopFrame;
  private HashMap viewSets;

  /**
   * Creates the ViewManager object and prepares it for adding views.
   */
  public ViewManager(MDIDesktopFrame desktopFrame) {
    viewSets = new HashMap();
    this.desktopFrame = desktopFrame;
  }

  /**
   * Opens a TimelineView.  If one exists, it is setSelected(true).
   * @param partialPlan The PwPartialPlan with which this view is associated.
   * @param planName The name of the plan.  This is used as the title of the view windows so
   *                 they are visually distinct across partial plans.
   * @return MDIInternalFrame the frame containing the newly created or selected view.
   */
  public MDIInternalFrame openTimelineView(PwPartialPlan partialPlan, String planName) 
  throws SQLException {
    if(!viewSets.containsKey(partialPlan)) {
      viewSets.put(partialPlan, new ViewSet(desktopFrame, partialPlan, planName, this));
    }
    return ((ViewSet)viewSets.get(partialPlan)).openTimelineView();
  }

  /**
   * Opens a TokenNetworkView.  If one exists, it is setSelected(true).
   * @param partialPlan The PwPartialPlan with which this view is associated.
   * @param planName The name of the plan.  This is used as the title of the view windows so
   *                 they are visually distinct across partial plans.
   * @return MDIInternalFrame the frame containing the newly created or selected view.
   */
  public MDIInternalFrame openTokenNetworkView(PwPartialPlan partialPlan, String planName)
  throws SQLException {
    if(!viewSets.containsKey(partialPlan)) {
      viewSets.put(partialPlan, new ViewSet(desktopFrame, partialPlan, planName, this));
    }
    return ((ViewSet)viewSets.get(partialPlan)).openTokenNetworkView();
  }

  /*
  public MDIInternalFrame openConstraintNetworkView(PwPartialPlan partialPlan, String planName) {
  }
  public MDIInternalFrame openTemporalExtentView(PwPartialPlan partialPlan, String planName) {
  }
  public MDIInternalFrame openTemporalNetworkView(PwPartialPlan partialPlan, String planName) {
  }
  */
  /**
   * Removes all views associated with a partial plan.
   * @param key The partial plan whose views are going away.
   */
  public void removeViewSet(PwPartialPlan key) {
    viewSets.remove(key);
  }
  /**
   * Clears all of the view sets.
   */
  public void clearViewSets() {
    Collection viewSetss = viewSets.values();
    Iterator viewSetIterator = viewSetss.iterator();
    while(viewSetIterator.hasNext()) {
      ((ViewSet)viewSetIterator.next()).close();
    }
    viewSets.clear();
  }
  /**
   * Gets the ViewSet associated with a particular partial plan.
   * @param partialPlan the PwPartialPlan whose ViewSet is needed.
   * @return ViewSet the ViewSet associated with the partial plan.
   */
  public ViewSet getViewSet(PwPartialPlan partialPlan) {
    if(viewSets.containsKey(partialPlan)) {
      return (ViewSet) viewSets.get(partialPlan);
    }
    return null;
  }
}
