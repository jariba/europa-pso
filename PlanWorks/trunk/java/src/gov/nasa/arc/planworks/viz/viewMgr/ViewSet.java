//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ViewSet.java,v 1.34 2003-09-26 22:47:08 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.awt.Container;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;

import gov.nasa.arc.planworks.mdi.MDIFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIWindowBar;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.ContentSpecWindow;

/**
 * <code>ViewSet</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A class to manage the views associated with a partial plan.
 */

//implements MDIWindowBar for the notifyDeleted method
public class ViewSet implements RedrawNotifier, MDIWindowBar {
  private MDIDesktopFrame desktopFrame;
  private HashMap views;
  //private List views;
  private ContentSpec contentSpec;
  //private PwPartialPlan partialPlan;
  private ViewableObject viewable;
  private ViewSetRemover remover;
  //private String planName;
  private MDIInternalFrame contentSpecWindow;
  private Object [] constructorArgs;
  //private ColorStream colorStream;
  //private PwToken activeToken; // in timeline view, the base token
  //private List secondaryTokens; // in timeline view, the overloaded tokens

  /**
   * Creates the ViewSet object, creates a new ContentSpec, and creates storage for the new views.
   * @param desktopFrame the desktop into which the views will be added.
   * @param partialPlan the partialPlan to be viewed.
   * @param planName the name of the partial plan.  Used to visually distinguish views across
   *                 partial plans.
   * @param remover the interface which is responsible for removing entire ViewSets.
   */
  public ViewSet(MDIDesktopFrame desktopFrame,/* PwPartialPlan partialPlan*/ViewableObject viewable,
                 /*String planName,*/ ViewSetRemover remover) {
    this.views = new HashMap();//new LinkedList(); 
    //this.partialPlan = partialPlan;
    this.viewable = viewable;
    //this.planName = planName;
    this.remover = remover;
    this.desktopFrame = desktopFrame;
    /*content spec specified in PartialPlanViewSet and SequenceViewSet*/
    //this.contentSpec = new ContentSpec(viewable, this);
    this.contentSpec = null;
    constructorArgs = new Object[2];
    constructorArgs[0] = (ViewableObject) viewable;
    constructorArgs[1] = this;
    //this.contentSpecWindow = 
    //  desktopFrame.createFrame("Content specification for " + viewable.getName(), this, true, false,
    //                          false, true);
    //Container contentPane = this.contentSpecWindow.getContentPane();
    //contentPane.add(new ContentSpecWindow(this.contentSpecWindow, contentSpec));
    //this.contentSpecWindow.pack();
    //this.contentSpecWindow.setVisible(true);
    //this.colorStream = new ColorStream();
    //this.activeToken = null;
  }

  public MDIInternalFrame openView(String viewName) {
    Class viewClass = null;
    try {
      viewClass = Class.forName(viewName);
    } catch (ClassNotFoundException excp) {
	excp.printStackTrace();
      System.exit(1);
    }
    if(views.containsKey(viewClass)) {
      return getViewByClass(viewClass);
    }
    Constructor [] constructors = viewClass.getDeclaredConstructors();
    MDIInternalFrame viewFrame = desktopFrame.createFrame(viewName + " view of " +
                                                          viewable.getName(),
                                                          this, true, true, true, true);
    views.put(viewClass, viewFrame);
    Container contentPane = viewFrame.getContentPane();
    VizView view = null;
    try {
	System.err.println("Class " + viewName + " has " + constructors.length + " constructors.");
	System.err.println("First constructor has " + constructors[0].getParameterTypes().length +
			   " arguments.");
	for(int i = 0; i < constructors[0].getParameterTypes().length; i++) {
	    System.err.println((constructors[0].getParameterTypes())[i].getName());
	}
	System.err.println("---------------");
	System.err.println(constructorArgs[0].getClass().getName());
	System.err.println(constructorArgs[1].getClass().getName());
      view = (VizView) constructors[0].newInstance(constructorArgs);
    } 
    catch (InvocationTargetException ite) {
	ite.getCause().printStackTrace();
	System.exit(-1);
    }
    catch (Exception excp) {
	excp.printStackTrace();
	System.exit(1);
    }
    contentPane.add(view);
    return viewFrame;
  }

  private MDIInternalFrame getViewByClass(Class viewClass) {
    return (MDIInternalFrame) views.get(viewClass);
  }

//   /**
//    * Opens a new TimelineView, stuffs it in an MDIInternalFrame, adds it to the hash of frames,
//    * then returns it.  If a TimelineView already exists for this partial plan, returns that.
//    * @return MDIInternalFrame the frame containing the vew.
//    */
//   public MDIInternalFrame openTimelineView( long startTimeMSecs) {
//     if(viewExists(ViewManager.TIMELINE_VIEW)) {
//       return (MDIInternalFrame) views.get(ViewManager.TIMELINE_VIEW);
//     }
//     MDIInternalFrame timelineViewFrame = 
//       desktopFrame.createFrame("Timeline view of ".concat(planName), this, true, true, true, true);
//     views.put(ViewManager.TIMELINE_VIEW, timelineViewFrame);
//     Container contentPane = timelineViewFrame.getContentPane();
//     contentPane.add(new TimelineView(partialPlan, startTimeMSecs, this));
//     return timelineViewFrame;
//   }

//   /**
//    * Opens a new TokenNetworkView, stuffs it in an MDIInternalFrame, adds it to the hash
//    * of frames, then returns it.  If a TokenNetworkView already exists for this partial
//    * plan, returns that.
//    * @return MDIInternalFrame the frame containing the vew.
//    */
//   public MDIInternalFrame openTokenNetworkView( long startTimeMSecs) {
//     if(viewExists(ViewManager.TNET_VIEW)) {
//       return (MDIInternalFrame) views.get(ViewManager.TNET_VIEW);
//     }
//     MDIInternalFrame tokenNetworkViewFrame = 
//       desktopFrame.createFrame("Token Network view of ".concat(planName), this, true, true,
//                                true, true);
//     views.put(ViewManager.TNET_VIEW, tokenNetworkViewFrame);
//     Container contentPane = tokenNetworkViewFrame.getContentPane();
//     contentPane.add(new TokenNetworkView(partialPlan, startTimeMSecs, this));
//     return tokenNetworkViewFrame;
//   }

//   /**
//    * Opens a new TemporalExtentView, stuffs it in an MDIInternalFrame, adds it to the hash
//    * of frames, then returns it.  If a TemporalExtentView already exists for this partial
//    * plan, returns that.
//    * @return MDIInternalFrame the frame containing the vew.
//    */
//   public MDIInternalFrame openTemporalExtentView( long startTimeMSecs) {
//     if(viewExists(ViewManager.TEMPEXT_VIEW)) {
//       return (MDIInternalFrame) views.get(ViewManager.TEMPEXT_VIEW);
//     }
//     MDIInternalFrame temporalExtentViewFrame = 
//       desktopFrame.createFrame("Temporal Extent view of ".concat(planName), this, true, true,
//                                true, true);
//     views.put(ViewManager.TEMPEXT_VIEW, temporalExtentViewFrame);
//     Container contentPane = temporalExtentViewFrame.getContentPane();
//     contentPane.add(new TemporalExtentView(partialPlan, startTimeMSecs, this));
//     return temporalExtentViewFrame;
//   }

//   /**
//    * Opens a new ConstraintNetworkView, stuffs it in an MDIInternalFrame, adds it to the hash
//    * of frames, then returns it.  If a ConstraintNetworkView already exists for this partial
//    * plan, returns that.
//    * @return MDIInternalFrame the frame containing the vew.
//    */
//   public MDIInternalFrame openConstraintNetworkView( long startTimeMSecs) {
//     if(viewExists(ViewManager.CNET_VIEW)) {
//       return (MDIInternalFrame) views.get(ViewManager.CNET_VIEW);
//     }
//     MDIInternalFrame constraintNetworkViewFrame = 
//       desktopFrame.createFrame("Constraint Network view of ".concat(planName), this, true, true,
//                                true, true);
//     views.put(ViewManager.CNET_VIEW, constraintNetworkViewFrame);
//     Container contentPane = constraintNetworkViewFrame.getContentPane();
//     contentPane.add(new ConstraintNetworkView(partialPlan, startTimeMSecs, this));
//     return constraintNetworkViewFrame;
//   }

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
        if(contentPane.getComponent(i) instanceof VizView) {
          views.remove(contentPane.getComponent(i).getClass());
        }
//         if(contentPane.getComponent(i) instanceof TimelineView) {
//           views.remove(ViewManager.TIMELINE_VIEW);
//         } else if(contentPane.getComponent(i) instanceof TokenNetworkView) {
//           views.remove(ViewManager.TNET_VIEW);
//         } else if(contentPane.getComponent(i) instanceof TemporalExtentView) {
//           views.remove(ViewManager.TEMPEXT_VIEW);
//         } else if(contentPane.getComponent(i) instanceof ConstraintNetworkView) {
//           views.remove(ViewManager.CNET_VIEW);
//         }
      }
    }
    if(views.isEmpty()) {
      //try {contentSpecWindow.setClosed(true);}
      //catch(PropertyVetoException pve){}
      remover.removeViewSet(viewable);
      //System.err.println("Saving content spec...");
      //partialPlan.setContentSpec(contentSpec.getCurrentSpec());
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
  //public List getValidIds() {
  //  return contentSpec.getValidIds();
  //}
  public List getValidIds() {
    return new ArrayList();
  }

  //public void printSpec() {
  //  contentSpec.printSpec();
  //}
  /**
   * Closes all of the views in the ViewSet.
   */
  public void close() {
    Object [] viewSet = views.values().toArray();
    try
      {
        for(int i = 0; i < viewSet.length; i++) {
          ((MDIInternalFrame)viewSet[i]).setClosed(true);
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
    //return views.containsKey(viewName);
    try {
      return views.containsKey(Class.forName(viewName));
    } catch (ClassNotFoundException excp) {
	excp.printStackTrace();
      System.exit(1);
    }
    return false;
  }
  
  public void notifyDeleted(MDIFrame frame) {
    removeViewFrame((MDIInternalFrame) frame);
  }
  public void add(JButton button) {
  }


}

