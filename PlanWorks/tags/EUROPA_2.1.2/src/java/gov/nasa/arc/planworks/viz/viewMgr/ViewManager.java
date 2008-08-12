//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ViewManager.java,v 1.27 2004-10-07 20:19:15 taylor Exp $
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
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
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
  // private int contentSpecWindowCnt;

  /**
   * Creates the ViewManager object and prepares it for adding views.
   */
  public ViewManager(MDIDesktopFrame desktopFrame) {
    viewSets = new HashMap();
    this.desktopFrame = desktopFrame;
    // this.contentSpecWindowCnt = 0;
  }

  public MDIInternalFrame openView(ViewableObject viewable, String viewClassName,
                                   ViewListener viewListener) {
    if (!viewSets.containsKey(viewable)) {
	if (viewable instanceof PwPartialPlan) {
	    viewSets.put(viewable,
                         new PartialPlanViewSet(desktopFrame,
                                                (PwPartialPlan) viewable, this));
            // contentSpecWindowCnt++;
	}
	else if (viewable instanceof PwPlanningSequence) {
	    viewSets.put(viewable,
                         new SequenceViewSet(desktopFrame,
                                             (PwPlanningSequence) viewable, this));
            // contentSpecWindowCnt++;
	}
	else {
	    viewSets.put(viewable, new ViewSet(desktopFrame, viewable, this));
	}
    }
    return ((ViewSet)viewSets.get(viewable)).openView(viewClassName, viewListener);
  }

  public MDIInternalFrame openView(ViewableObject viewable, String viewClassName, 
                                   PartialPlanViewState state) {
    // not sure how to handle this
    ViewListener viewListener = null;
    if (!viewSets.containsKey(viewable)) {
	if (viewable instanceof PwPartialPlan) {
	    viewSets.put(viewable,
                         new PartialPlanViewSet(desktopFrame,
                                                (PwPartialPlan) viewable, state, this));
            // contentSpecWindowCnt++;
	}
	else if (viewable instanceof PwPlanningSequence) {
	    viewSets.put(viewable,
                         new SequenceViewSet(desktopFrame,
                                             (PwPlanningSequence) viewable, this));
            // contentSpecWindowCnt++;
	}
	else {
	    viewSets.put(viewable, new ViewSet(desktopFrame, viewable, this));
	}
    }
    if(viewable instanceof PwPartialPlan) {
      ((PartialPlanViewSet)viewSets.get(viewable)).openView(viewClassName, state);
    }
    return ((ViewSet)viewSets.get(viewable)).openView(viewClassName, viewListener);
  }

  public MDIInternalFrame openView(ViewableObject viewable, String viewClassName, 
                                   PartialPlanViewState state, ViewListener viewListener) {
    if (!viewSets.containsKey(viewable)) {
	if (viewable instanceof PwPartialPlan) {
	    viewSets.put(viewable,
                         new PartialPlanViewSet(desktopFrame,
                                                (PwPartialPlan) viewable, state, this));
            // contentSpecWindowCnt++;
	}
	else if (viewable instanceof PwPlanningSequence) {
	    viewSets.put(viewable,
                         new SequenceViewSet(desktopFrame,
                                             (PwPlanningSequence) viewable, this));
            // contentSpecWindowCnt++;
	}
	else {
	    viewSets.put(viewable, new ViewSet(desktopFrame, viewable, this));
	}
    }
    if(viewable instanceof PwPartialPlan) {
      ((PartialPlanViewSet)viewSets.get(viewable)).openView(viewClassName, state, viewListener);
    }
    return ((ViewSet)viewSets.get(viewable)).openView(viewClassName, viewListener);
  }

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
    return (ViewSet) viewSets.get(viewable);
      //return null;
  }

  public  Object [] getViewSetKeys() {
    return viewSets.keySet().toArray();
  }

//   /**
//    * <code>getContentSpecWindowCnt</code>
//    *
//    * @return - <code>int</code> - 
//    */
//   public int getContentSpecWindowCnt() {
//     return this.contentSpecWindowCnt;
//   }

//   /**
//    * <code>decrementContentSpecWindowCnt</code>
//    *
//    */
//   public void decrementContentSpecWindowCnt() {
//     this.contentSpecWindowCnt--;
//   }

}

