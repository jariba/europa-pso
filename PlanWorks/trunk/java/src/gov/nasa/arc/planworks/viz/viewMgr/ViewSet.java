//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ViewSet.java,v 1.57 2004-03-12 23:24:17 miatauro Exp $
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
import java.util.ListIterator;
import javax.swing.JButton;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.mdi.MDIFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIWindowBar;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecWindow;

/**
 * <code>ViewSet</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A class to manage the views associated with a partial plan.
 */

//implements MDIWindowBar for the notifyDeleted method
public class ViewSet implements RedrawNotifier, MDIWindowBar {
  private MDIDesktopFrame desktopFrame;
  protected HashMap views;
  //private List views;
  //private PwPartialPlan partialPlan;
  protected ViewableObject viewable;
  private ViewSetRemover remover;
  //private String planName;
  private Object [] constructorArgs;

  protected ContentSpec contentSpec;
  protected MDIInternalFrame contentSpecWindow;

  /**
   * Creates the ViewSet object, creates a new ContentSpec, and creates storage for the new views.
   * @param desktopFrame the desktop into which the views will be added.
   * @param partialPlan the partialPlan to be viewed.
   * @param planName the name of the partial plan.  Used to visually distinguish views across
   *                 partial plans.
   * @param remover the interface which is responsible for removing entire ViewSets.
   */
  public ViewSet(MDIDesktopFrame desktopFrame, ViewableObject viewable, ViewSetRemover remover) {
    this.views = new HashMap();
    this.viewable = viewable;
    this.remover = remover;
    this.desktopFrame = desktopFrame;
    this.contentSpec = null;
    constructorArgs = new Object[2];
    constructorArgs[0] = (ViewableObject) viewable;
    constructorArgs[1] = this;
  }

  public MDIInternalFrame openView(String viewClassName) {
    Class viewClass = null;
    // System.err.println( "ViewSet.openView viewClassName " + viewClassName);
    try {
      viewClass = Class.forName(viewClassName);
    } catch (ClassNotFoundException excp) {
      excp.printStackTrace();
      System.exit(1);
    }
    if(views.containsKey(viewClass)) {
      return getViewByClass(viewClass);
    }
    Constructor [] constructors = viewClass.getDeclaredConstructors();
    String frameViewName = viewClassName.substring( viewClassName.lastIndexOf( ".") + 1);
    MDIInternalFrame viewFrame = desktopFrame.createFrame( frameViewName + " of " +
                                                           viewable.getName(),
                                                           this, true, true, true, true);
    viewFrame.setIconifiable( true);
    views.put(viewClass, viewFrame);
    Container contentPane = viewFrame.getContentPane();
    VizView view = null;
    try {
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

  public MDIInternalFrame openView(String viewClassName, PartialPlanViewState viewState) {
    Class viewClass = null;
    // System.err.println( "ViewSet.openView viewClassName " + viewClassName);
    try {
      viewClass = Class.forName(viewClassName);
    } catch (ClassNotFoundException excp) {
      excp.printStackTrace();
      System.exit(1);
    }
    if(views.containsKey(viewClass)) {
      return getViewByClass(viewClass);
    }
    Constructor [] constructors = viewClass.getDeclaredConstructors();
    String frameViewName = viewClassName.substring( viewClassName.lastIndexOf( ".") + 1);
    MDIInternalFrame viewFrame = desktopFrame.createFrame( frameViewName + " of " +
                                                           viewable.getName(),
                                                           this, true, true, true, true);
    viewFrame.setIconifiable( true);
    views.put(viewClass, viewFrame);
    Container contentPane = viewFrame.getContentPane();
    VizView view = null;
    Object [] stateConstructorArgs = new Object[3];
    stateConstructorArgs[0] = viewable;
    stateConstructorArgs[1] = this;
    stateConstructorArgs[2] = viewState;
    try {
      view = (VizView) constructors[1].newInstance(stateConstructorArgs);
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

  public MDIInternalFrame getViewByClass(Class viewClass) {
    return (MDIInternalFrame) views.get(viewClass);
  }

  /**
   * Removes a view from the ViewSet.  If there are no more views extant, removes the ViewSet.
   * @param MDIInternalFrame the frame containing the view.
   */
  public void removeViewFrame(MDIInternalFrame viewFrame) {
    //System.err.println("in removeViewFrame");
    if(views.containsValue(viewFrame)) {
      //System.err.println("have frame");
      Container contentPane = viewFrame.getContentPane();
      for(int i = 0; i < contentPane.getComponentCount(); i++) {
        if(contentPane.getComponent(i) instanceof VizView) {
          views.remove(contentPane.getComponent(i).getClass());
          String overviewTitle = null;
          VizViewOverview overview =
            ((VizView) contentPane.getComponent(i)).getOverview();
          if (overview != null) {
            overviewTitle = overview.getTitle();
          // System.err.println( "VizView overviewTitle " + overviewTitle);
            MDIInternalFrame overviewFrame = (MDIInternalFrame) views.get( overviewTitle);
            if (overviewFrame != null) {
              try {
                //System.err.println("closing frame");
                overviewFrame.setClosed( true);
              } catch(PropertyVetoException pve){
                pve.printStackTrace();
              }
              //System.err.println("removing frame");
              views.remove( overviewTitle);
            }
          }
        } else if (contentPane.getComponent(i) instanceof VizViewOverview) {
          ((VizViewOverview) contentPane.getComponent(i)).removeNotifyFromViewSet();
          views.remove( ((VizViewOverview) contentPane.getComponent(i)).getTitle());
        }
      }
      //System.err.println( "removeViewFrame " + viewFrame.getTitle());
    }
    if(views.isEmpty()) {
      if (contentSpecWindow != null) {
        try {contentSpecWindow.setClosed(true);}
        catch(PropertyVetoException pve){}
      }
      remover.removeViewSet(viewable);
      //System.err.println("Saving content spec...");
      //partialPlan.setContentSpec(contentSpec.getCurrentSpec());
      if (contentSpec != null) {
        viewable.setContentSpec(contentSpec.getCurrentSpec());
      }
      if(this instanceof SequenceViewSet) {
        //System.err.println("Removing planning sequence and frames...");
        //(new Throwable()).printStackTrace();
        List partialPlans = ((PwPlanningSequence) viewable).getPartialPlansList();
        ListIterator planIterator = partialPlans.listIterator();
        while(planIterator.hasNext()) {
          ViewableObject partialPlan = (ViewableObject) planIterator.next();
          ViewSet set = getViewManager().getViewSet(partialPlan);
          if(set != null) {
            set.close();
            getViewManager().removeViewSet(partialPlan);
          }
        }
        getViewManager().removeViewSet(viewable);
      }
    }
    if(viewFrame.getTitle().indexOf("SequenceStepsView") != -1 && 
       this instanceof SequenceViewSet) {
      close();
      getViewManager().removeViewSet(viewable);
//       try {
//         PlanWorks.getPlanWorks().currentProject.
//           removePlanningSequence(((PwPlanningSequence)viewable).getId());
//         System.gc();
//       }
//       catch(ResourceNotFoundException rnfe) {
//         rnfe.printStackTrace();
//         System.exit(-1);
//       }
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

  public List getValidIds() {
   return contentSpec.getValidIds();
  }

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
        if (contentSpecWindow != null) {
          contentSpecWindow.setClosed(true);
        }
      }
    catch(PropertyVetoException pve){}

    // ((ViewManager) remover).decrementContentSpecWindowCnt();
  }

  /**
   * Iconifies all of the views in the ViewSet.
   *
   *     viewFrame.setIconifiable( true); must be set
   */
  public void iconify() {
    Object [] viewSet = views.values().toArray();
    try
      {
        for(int i = 0; i < viewSet.length; i++) {
          ((MDIInternalFrame)viewSet[i]).setIcon(true);
        }
        if (contentSpecWindow != null) {
          contentSpecWindow.setIcon(true);
        }
      }
    catch(PropertyVetoException pve){}
  }

  /**
   * Shows all of the iconified views in the ViewSet.
   *
   *     viewFrame.setIconifiable( true); must be set
   */
  public void show() {
    Object [] viewSet = views.values().toArray();
    try
      {
        for(int i = 0; i < viewSet.length; i++) {
          ((MDIInternalFrame)viewSet[i]).setIcon( false);
        }
        if (contentSpecWindow != null) {
          contentSpecWindow.setIcon( false);
        }
      }
    catch(PropertyVetoException pve){}
  }

  /**
   * <code>getViewManager</code>
   *
   * @return - <code>ViewManager</code> - 
   */
  public ViewManager getViewManager() {
    return (ViewManager) remover;
  }

  /**
   * Determines whether or not a view already exists.
   * @param viewClassName the name of the view
   * @return boolean whether or not the view exists.
   */
  public boolean viewExists(String viewClassName) {
    //return views.containsKey(viewClassName);
    try {
      return views.containsKey(Class.forName(viewClassName));
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

  /**
   * <code>getContentSpecWindow</code>
   *
   * @return - <code>MDIInternalFrame</code> - 
   */
  public MDIInternalFrame getContentSpecWindow() {
    return contentSpecWindow;
  }

  /**
   * <code>getDesktopFrame</code>
   *
   * @return - <code>MDIDesktopFrame</code> - 
   */
  public MDIDesktopFrame getDesktopFrame() {
    return desktopFrame;
  }

  /**
   * <code>getViews</code> - 
   * @return - <code>HashMap</code> - 
   */
  public HashMap getViews() {
    return views;
  }

  public MDIInternalFrame getView(Object key) {
    return (MDIInternalFrame) views.get(key);
  }
}

