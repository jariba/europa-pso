//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ViewSet.java,v 1.28 2003-09-16 19:29:13 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.awt.Container;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;

import gov.nasa.arc.planworks.mdi.MDIFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIWindowBar;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.util.ColorStream;
import gov.nasa.arc.planworks.viz.views.VizView;
import gov.nasa.arc.planworks.viz.views.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.views.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.views.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.ContentSpecWindow;

/**
 * <code>ViewSet</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A class to manage the views associated with a partial plan.
 */

//maybe the hashmap should be changed.  is that much flexibility really necessary?
//the MDIWindowBar is just for the notifyDeleted method
public class ViewSet implements RedrawNotifier, MDIWindowBar {
  private MDIDesktopFrame desktopFrame;
  private HashMap views;
  private ContentSpec contentSpec;
  private PwPartialPlan partialPlan;
  private ViewSetRemover remover;
  private String planName;
  private MDIInternalFrame contentSpecWindow;
  private ColorStream colorStream;

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
    this.contentSpec = new ContentSpec(partialPlan, this);
    this.contentSpecWindow = 
      desktopFrame.createFrame("Content specification for ".concat(planName), this, true, false,
                               false, true);
    Container contentPane = this.contentSpecWindow.getContentPane();
    contentPane.add(new ContentSpecWindow(this.contentSpecWindow, contentSpec));
    this.contentSpecWindow.pack();
    this.contentSpecWindow.setVisible(true);
    this.colorStream = new ColorStream();
  }

  /**
   * Opens a new TimelineView, stuffs it in an MDIInternalFrame, adds it to the hash of frames,
   * then returns it.  If a TimelineView already exists for this partial plan, returns that.
   * @return MDIInternalFrame the frame containing the vew.
   */
  public MDIInternalFrame openTimelineView( long startTimeMSecs) {
    if(viewExists(ViewManager.TIMELINE_VIEW)) {
      return (MDIInternalFrame) views.get(ViewManager.TIMELINE_VIEW);
    }
    MDIInternalFrame timelineViewFrame = 
      desktopFrame.createFrame("Timeline view of ".concat(planName), this, true, true, true, true);
    views.put(ViewManager.TIMELINE_VIEW, timelineViewFrame);
    Container contentPane = timelineViewFrame.getContentPane();
    contentPane.add(new TimelineView(partialPlan, startTimeMSecs, this));
    return timelineViewFrame;
  }

  /**
   * Opens a new TokenNetworkView, stuffs it in an MDIInternalFrame, adds it to the hash
   * of frames, then returns it.  If a TokenNetworkView already exists for this partial
   * plan, returns that.
   * @return MDIInternalFrame the frame containing the vew.
   */
  public MDIInternalFrame openTokenNetworkView( long startTimeMSecs) {
    if(viewExists(ViewManager.TNET_VIEW)) {
      return (MDIInternalFrame) views.get(ViewManager.TNET_VIEW);
    }
    MDIInternalFrame tokenNetworkViewFrame = 
      desktopFrame.createFrame("Token Network view of ".concat(planName), this, true, true,
                               true, true);
    views.put(ViewManager.TNET_VIEW, tokenNetworkViewFrame);
    Container contentPane = tokenNetworkViewFrame.getContentPane();
    contentPane.add(new TokenNetworkView(partialPlan, startTimeMSecs, this));
    return tokenNetworkViewFrame;
  }

  /**
   * Opens a new TemporalExtentView, stuffs it in an MDIInternalFrame, adds it to the hash
   * of frames, then returns it.  If a TemporalExtentView already exists for this partial
   * plan, returns that.
   * @return MDIInternalFrame the frame containing the vew.
   */
  public MDIInternalFrame openTemporalExtentView( long startTimeMSecs) {
    if(viewExists(ViewManager.TEMPEXT_VIEW)) {
      return (MDIInternalFrame) views.get(ViewManager.TEMPEXT_VIEW);
    }
    MDIInternalFrame temporalExtentViewFrame = 
      desktopFrame.createFrame("Temporal Extent view of ".concat(planName), this, true, true,
                               true, true);
    views.put(ViewManager.TEMPEXT_VIEW, temporalExtentViewFrame);
    Container contentPane = temporalExtentViewFrame.getContentPane();
    contentPane.add(new TemporalExtentView(partialPlan, startTimeMSecs, this));
    return temporalExtentViewFrame;
  }

  /**
   * Opens a new ConstraintNetworkView, stuffs it in an MDIInternalFrame, adds it to the hash
   * of frames, then returns it.  If a ConstraintNetworkView already exists for this partial
   * plan, returns that.
   * @return MDIInternalFrame the frame containing the vew.
   */
  public MDIInternalFrame openConstraintNetworkView( long startTimeMSecs) {
    if(viewExists(ViewManager.CNET_VIEW)) {
      return (MDIInternalFrame) views.get(ViewManager.CNET_VIEW);
    }
    MDIInternalFrame constraintNetworkViewFrame = 
      desktopFrame.createFrame("Constraint Network view of ".concat(planName), this, true, true,
                               true, true);
    views.put(ViewManager.CNET_VIEW, constraintNetworkViewFrame);
    Container contentPane = constraintNetworkViewFrame.getContentPane();
    contentPane.add(new ConstraintNetworkView(partialPlan, startTimeMSecs, this));
    return constraintNetworkViewFrame;
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
          views.remove(ViewManager.TIMELINE_VIEW);
        } else if(contentPane.getComponent(i) instanceof TokenNetworkView) {
          views.remove(ViewManager.TNET_VIEW);
        } else if(contentPane.getComponent(i) instanceof TemporalExtentView) {
          views.remove(ViewManager.TEMPEXT_VIEW);
        } else if(contentPane.getComponent(i) instanceof ConstraintNetworkView) {
          views.remove(ViewManager.CNET_VIEW);
        }
      }
    }
    if(views.isEmpty()) {
      try {contentSpecWindow.setClosed(true);}
      catch(PropertyVetoException pve){}
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
  public List getValidTokenIds() {
    return contentSpec.getValidTokenIds();
  }
  public void printSpec() {
    contentSpec.printSpec();
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
  
  public void notifyDeleted(MDIFrame frame) {
    removeViewFrame((MDIInternalFrame) frame);
  }
  public void add(JButton button) {
  }

  public ColorStream getColorStream() {
    return colorStream;
  }

}
