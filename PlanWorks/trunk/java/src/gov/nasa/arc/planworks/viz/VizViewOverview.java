// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VizViewOverview.java,v 1.2 2003-11-20 19:11:23 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17nov03
//

package gov.nasa.arc.planworks.viz;

import java.awt.Point;
import java.awt.event.MouseEvent;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.Overview;

import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.SlotNode;


/**
 * <code>VizViewOverview</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *               NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VizViewOverview extends Overview {

  private String overviewTitle; // key for viewSet hash map
  private VizView vizView;

  /**
   * <code>VizViewOverview</code> - constructor 
   *
   * @param overviewTitle - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public VizViewOverview( String overviewTitle, VizView vizView) {
    super();
    this.overviewTitle = overviewTitle;
    this.vizView = vizView;
  }

  /**
   * <code>getTitle</code> - key for viewSet hash map
   *
   * @return - <code>String</code> - 
   */
  public String getTitle() {
    return overviewTitle;
  }

  /**
   * <code>getVizView</code>
   *
   * @return - <code>VizView</code> - 
   */
  public VizView getVizView() {
    return vizView;
  }

  // when the Overview window is no longer needed, make sure the observed view doesn't keep
  // any references to this view
//   public void removeNotify()
//   {
//     removeListeners();
//     myObserved = null;
//     super.removeNotify();
//   }

  /**
   * I believe the problem may be related to focus changes in the
   * JDesktopPane.  When focus changes between different JInternalFrames on
   * the desktop it appears that the parent component of the JInternalFrame
   * changes, so momentarily it has no parent.  This causes
   * JGoOverview.removeNotify() to be called which sets the observed JGoView
   * to null:
   *
   * You should subclass JGoOverview and override this method to do nothing.
   * Then make sure these operations are performed when the JInternalFrame
   * holdiong the JGoOverview is closed.
   */
  public void removeNotify() {
    // System.err.println( "VizViewOverview.removeNotify");
    // System.err.println( "myObserved " + getObserved());
//     removeListeners();
//     myObserved = null;
//     super.removeNotify();
  }  

  /**
   * <code>removeNotifyFromViewSet</code>
   *
   */
  public void removeNotifyFromViewSet() {
    OverviewRectangle overviewRect = getOverviewRect();
    JGoView observed = getObserved();
    // System.err.println( "removeNotifyFromViewSet");
    if (observed != null && overviewRect != null) {
      observed.getDocument().removeDocumentListener(this);
      observed.removeViewListener(overviewRect);
      observed.getCanvas().removeComponentListener(overviewRect);
    }
  }

  // show tooltips, so users might get a clue about which object is which
  // even though the objects are so small
  /**
   * <code>getToolTipText</code> -
   *            show tooltips, so users might get a clue about which object is which
   *            even though the objects are so small.
   *            Show the node label, not its tool tip.
   *
   * @param evt - <code>MouseEvent</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( MouseEvent evt) {
    if (getObserved() == null) return null;
    boolean isOverview = true;
    Point p = new Point(evt.getPoint());
    convertViewToDoc(p);
    String tip = null;
    JGoObject obj = getObserved().pickDocObject(p, false);

    while (obj != null) {
      if (obj instanceof ConstraintNetworkTokenNode) {
        tip = ((ConstraintNetworkTokenNode) obj).getToolTipText( isOverview);
      } else if (obj instanceof ConstraintNode) {
        tip = ((ConstraintNode) obj).getToolTipText( isOverview);
      } else if (obj instanceof VariableNode) {
        tip = ((VariableNode) obj).getToolTipText( isOverview);
      } else if (obj instanceof TemporalNode) {
        tip = ((TemporalNode) obj).getToolTipText( isOverview);
      } else if (obj instanceof TokenNode) {
        tip = ((TokenNode) obj).getToolTipText( isOverview);
      } else if (obj instanceof SlotNode) {
        tip = ((SlotNode) obj).getToolTipText( isOverview);
      } else {
        tip = obj.getToolTipText();
      }
      if (tip != null) {
        return tip;
      } else {
        obj = obj.getParent();
      }
    }
    return null;
  }

} // end class VizViewOverview

