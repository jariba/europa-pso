// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TimelineTokenNode.java,v 1.6 2004-05-08 01:44:16 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 30dec03
//

package gov.nasa.arc.planworks.viz.partialPlan.timeline;

import java.awt.Color;
import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;

/**
 * <code>TimelineTokenNode</code> - handle Auto Snap to Temporal Extent View for free tokens
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineTokenNode extends TokenNode {

  private TimelineView timelineView;
  private ViewListener viewListener;


  /**
   * <code>TimelineTokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public TimelineTokenNode( PwToken token, PwSlot slot, Point tokenLocation,
                            Color backgroundColor, boolean isFreeToken, boolean isDraggable,
                            PartialPlanView partialPlanView) {
    super( token, slot, tokenLocation, backgroundColor, isFreeToken, isDraggable,
           partialPlanView);
    this.timelineView = (TimelineView) partialPlanView;
    this.viewListener = null;
  } // end constructor

  /**
   * <code>doUncapturedMouseMove</code> -- handles Auto-Snap of TemporalExtentView
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doUncapturedMouseMove( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    TokenNode tokenNode = (TokenNode) obj.getTopLevelObject();
    JGoArea currentMouseOverNode = timelineView.getMouseOverNode();
    if ((currentMouseOverNode == null) ||
        ((currentMouseOverNode != null) &&
         ((currentMouseOverNode instanceof SlotNode) ||
          ((currentMouseOverNode instanceof TokenNode) &&
           (! ((TokenNode) currentMouseOverNode).getToken().getId().equals
            ( tokenNode.getToken().getId())))))) {
      timelineView.setMouseOverNode( tokenNode);
      String className = PlanWorks.getViewClassName( ViewConstants.TEMPORAL_EXTENT_VIEW);
      if (timelineView.isAutoSnapEnabled() &&
          timelineView.getViewSet().viewExists( className)) {
        PwSlot slot = null; // free token has no slot
        PwToken freeToken = tokenNode.getToken();
        ((PartialPlanViewSet) timelineView.getViewSet()).setActiveToken( freeToken);
        NodeGenerics.setSecondaryTokensForSlot
          ( freeToken, slot, (PartialPlanViewSet) timelineView.getViewSet());
        TemporalExtentView temporalExtentView =
          ViewGenerics.getTemporalExtentView( timelineView.getViewSet().
                                              openView( className, viewListener));
        boolean isByKey = false;
        temporalExtentView.findAndSelectToken( freeToken, isByKey);
      }
      return true;
    } else {
      return false;
    }
  } // end doUncapturedMouseMove


} // end class TimelineTokenNode

