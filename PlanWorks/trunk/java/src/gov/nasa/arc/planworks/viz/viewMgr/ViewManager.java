//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ViewManager.java,v 1.21 2003-11-06 00:02:19 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

//import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;

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
  private Object [] constructorArgs;
  private int contentSpecWindowCnt;

  /**
   * Creates the ViewManager object and prepares it for adding views.
   */
  public ViewManager(MDIDesktopFrame desktopFrame) {
    viewSets = new HashMap();
    this.desktopFrame = desktopFrame;
    this.contentSpecWindowCnt = 0;
  }

  public MDIInternalFrame openView(ViewableObject viewable, String viewClassName) {
    if (!viewSets.containsKey(viewable)) {
	if (viewable instanceof PwPartialPlan) {
	    viewSets.put(viewable,
                         new PartialPlanViewSet(desktopFrame,
                                                (PwPartialPlan) viewable, this));
            contentSpecWindowCnt++;
	}
	else if (viewable instanceof PwPlanningSequence) {
	    viewSets.put(viewable,
                         new SequenceViewSet(desktopFrame,
                                             (PwPlanningSequence) viewable, this));
            contentSpecWindowCnt++;
	}
	else {
	    viewSets.put(viewable, new ViewSet(desktopFrame, viewable, this));
	}
    }
    return ((ViewSet)viewSets.get(viewable)).openView(viewClassName);
  }

//   /**
//    * Opens a TimelineView.  If one exists, it is setSelected(true).
//    * @param partialPlan The PwPartialPlan with which this view is associated.
//    * @param planName The name of the plan.  This is used as the title of the view windows so
//    *                 they are visually distinct across partial plans.
//    * @return MDIInternalFrame the frame containing the newly created or selected view.
//    */
//   public MDIInternalFrame openTimelineView(PwPartialPlan partialPlan, String planName,
//                                            long startTimeMSecs) {
//     if(!viewSets.containsKey(partialPlan)) {
//       viewSets.put(partialPlan, new ViewSet(desktopFrame, partialPlan, planName, this));
//     }
//     return ((ViewSet)viewSets.get(partialPlan)).openTimelineView( startTimeMSecs);
//   }

//   /**
//    * Opens a TokenNetworkView.  If one exists, it is setSelected(true).
//    * @param partialPlan The PwPartialPlan with which this view is associated.
//    * @param planName The name of the plan.  This is used as the title of the view windows so
//    *                 they are visually distinct across partial plans.
//    * @return MDIInternalFrame the frame containing the newly created or selected view.
//    */
//   public MDIInternalFrame openTokenNetworkView(PwPartialPlan partialPlan, String planName,
//                                                long startTimeMSecs) {
//     if(!viewSets.containsKey(partialPlan)) {
//       viewSets.put(partialPlan, new ViewSet(desktopFrame, partialPlan, planName, this));
//     }
//     return ((ViewSet)viewSets.get(partialPlan)).openTokenNetworkView( startTimeMSecs);
//   }

//   /**
//    * Opens a TemporalExtentView.  If one exists, it is setSelected(true).
//    * @param partialPlan The PwPartialPlan with which this view is associated.
//    * @param planName The name of the plan.  This is used as the title of the view windows so
//    *                 they are visually distinct across partial plans.
//    * @return MDIInternalFrame the frame containing the newly created or selected view.
//    */
//   public MDIInternalFrame openTemporalExtentView(PwPartialPlan partialPlan, String planName,
//                                                  long startTimeMSecs) {
//     if(!viewSets.containsKey(partialPlan)) {
//       viewSets.put(partialPlan, new ViewSet(desktopFrame, partialPlan, planName, this));
//     }
//     return ((ViewSet)viewSets.get(partialPlan)).openTemporalExtentView( startTimeMSecs);
//   }

//   /**
//    * Opens a ConstrintNetworkView.  If one exists, it is setSelected(true).
//    * @param partialPlan The PwPartialPlan with which this view is associated.
//    * @param planName The name of the plan.  This is used as the title of the view windows so
//    *                 they are visually distinct across partial plans.
//    * @return MDIInternalFrame the frame containing the newly created or selected view.
//    */
//   public MDIInternalFrame openConstraintNetworkView(PwPartialPlan partialPlan, String planName,
//                                                     long startTimeMSecs) {
//     if(!viewSets.containsKey(partialPlan)) {
//       viewSets.put(partialPlan, new ViewSet(desktopFrame, partialPlan, planName, this));
//     }
//     return ((ViewSet)viewSets.get(partialPlan)).openConstraintNetworkView( startTimeMSecs);
//   } 

  /*
  public MDIInternalFrame openTemporalNetworkView(PwPartialPlan partialPlan, String planName) {
  }
  */
  /**
   * Removes all views associated with a partial plan.
   * @param key The partial plan whose views are going away.
   */
  //public void removeViewSet(PwPartialPlan key) {
  public void removeViewSet(ViewableObject key) {
    viewSets.remove(key);
  }
  /**
   * Clears all of the view sets.
   */
  public void clearViewSets() {
    Object [] viewSetss = viewSets.values().toArray();
    for(int i = 0; i < viewSetss.length; i++) {
      ((ViewSet)viewSetss[i]).close();
    }
    viewSets.clear();
  }
  /**
   * Gets the ViewSet associated with a particular partial plan.
   * @param partialPlan the PwPartialPlan whose ViewSet is needed.
   * @return ViewSet the ViewSet associated with the partial plan.
   */
  //public ViewSet getViewSet(PwPartialPlan partialPlan) {
  public ViewSet getViewSet(ViewableObject viewable) {
    //if(viewSets.containsKey(partialPlan)) {
    //  return (ViewSet) viewSets.get(partialPlan);
    //}
    return (ViewSet) viewSets.get(viewable);
      //return null;
  }

  /**
   * <code>getContentSpecWindowCnt</code>
   *
   * @return - <code>int</code> - 
   */
  public int getContentSpecWindowCnt() {
    return this.contentSpecWindowCnt;
  }

  /**
   * <code>decrementContentSpecWindowCnt</code>
   *
   */
  public void decrementContentSpecWindowCnt() {
    this.contentSpecWindowCnt--;
  }


}
