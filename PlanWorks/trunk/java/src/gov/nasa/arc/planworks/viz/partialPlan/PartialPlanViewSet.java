// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanViewSet.java,v 1.13 2004-01-12 19:46:21 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.db.util.PartialPlanContentSpec;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.util.ColorStream;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSetRemover;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecWindow;


/**
 * <code>PartialPlanViewSet</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PartialPlanViewSet extends ViewSet {

  private ColorStream colorStream;
  private PwToken activeToken; // in timeline view, the base token
  private List secondaryTokens; // in timeline view, the overloaded tokens
  private int navigatorFrameCnt;


  /**
   * <code>PartialPlanViewSet</code> - constructor 
   *
   * @param desktopFrame - <code>MDIDesktopFrame</code> - 
   * @param viewable - <code>ViewableObject</code> - 
   * @param remover - <code>ViewSetRemover</code> - 
   */
  public PartialPlanViewSet( MDIDesktopFrame desktopFrame, ViewableObject viewable,
                             ViewSetRemover remover) {
    super( desktopFrame, viewable, remover);
    this.colorStream = new ColorStream();
    this.activeToken = null;
    this.contentSpecWindow = desktopFrame.createFrame( ContentSpec.CONTENT_SPEC_TITLE +
                                                       " for " + viewable.getName(),
                                                       this, true, false, false, true);
    Container contentPane = this.contentSpecWindow.getContentPane();
    this.contentSpec = new PartialPlanContentSpec( viewable, this);
    ((PwPartialPlan) viewable).setContentSpec( this.contentSpec.getCurrentSpec());
    contentPane.add( new ContentSpecWindow( this.contentSpecWindow, this.contentSpec, this));
    this.contentSpecWindow.pack();
    String seqUrl = ((PwPartialPlan) viewable).getSequenceUrl();
    int sequenceStepsViewHeight =
      (int) (((MDIInternalFrame) PlanWorks.planWorks.
              sequenceStepsViewMap.get( seqUrl)).getSize().getHeight() * 0.5);
    int delta = Math.min( (int) (((ViewManager) remover).getContentSpecWindowCnt() *
                                 ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4),
                          (int) (PlanWorks.planWorks.getSize().getHeight() -
                                 sequenceStepsViewHeight -
                                 (ViewConstants.MDI_FRAME_DECORATION_HEIGHT * 2)));
    this.contentSpecWindow.setLocation( delta, sequenceStepsViewHeight + delta);
    this.contentSpecWindow.setVisible(true);

    navigatorFrameCnt = 0;
  } // end constructor

  public MDIInternalFrame openView(String viewClassName) {
    MDIInternalFrame retval = super.openView(viewClassName);
//     retval.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK, false),
//                              "foobar");
//     retval.getActionMap().put("foobar", new AbstractAction() {
//         public void actionPerformed(ActionEvent e) {
//           System.err.println("foobar");
//         }
//       });
    return retval;
  }

  public MDIInternalFrame openView(String viewClassName, PartialPlanViewState state) {
    MDIInternalFrame retval = super.openView(viewClassName, state);
    Container contentPane = retval.getContentPane();
    //dreaded equivalent of Big Switch Statement...
//     for(int i = 0; i < contentPane.getComponentCount(); i++) {
//       Component temp = contentPane.getComponent(i);
//       if(temp instanceof TimelineView) {
//         ((TimelineView)temp).setState(state);
//         break;
//       }
//       else if(temp instanceof TokenNetworkView) {
//         ((TokenNetworkView)temp).setState(state); 
//         break;
//       }
//       else if(temp instanceof ConstraintNetworkView) {
//         ((ConstraintNetworkView)temp).setState(state); 
//         break;
//       }
//       else if(temp instanceof TemporalExtentView) {
//         ((TemporalExtentView)temp).setState(state); 
//         break;
//       }
//     }
    return retval;
  }

  /**
   * <code>getColorStream</code> - manages timeline colors
   *
   * @return - <code>ColorStream</code> - 
   */
  public ColorStream getColorStream() {
    return colorStream;
  }

  /** 
   * <code>getActiveToken</code> - user selected view focus
   *
   * @return - <code>PwToken</code>
   */
  public PwToken getActiveToken() {
    return activeToken;
  }

  /**
   * <code>setActiveToken</code> - make this token the view focus
   *
   * @param token - <code>PwToken</code>
   */
  public void setActiveToken( PwToken token) {
    activeToken = token;
  }

  public List getCurrentSpec() {
    return contentSpec.getCurrentSpec();
  }
  
  /**
   * <code>getSecondaryTokens</code> - in timeline view, the overloaded tokens
   *
   * @return - <code>List</code> - 
   */
  public List getSecondaryTokens() {
    return secondaryTokens;
  }

  /**
   * <code>setSecondaryTokens</code> - in timeline view, the overloaded tokens
   *
   * @param tokenList - <code>List</code> 
   */
  public void setSecondaryTokens( List tokenList) {
    secondaryTokens = tokenList;
  }

  /**
   * <code>getNavigatorFrameCnt</code>
   *
   * @return - <code>int</code> - 
   */
  public int getNavigatorFrameCnt() {
    return navigatorFrameCnt;
  }

  /**
   * <code>incrNavigatorFrameCnt</code>
   *
   */
  public void incrNavigatorFrameCnt() {
    navigatorFrameCnt++;
  }

  /**
   * <code>getPartialPlanViews</code>
   *
   * @param numToReturn - <code>int</code> - 0 => return all views
   * @return - <code>List</code> - of PartialPlanView
   */
  public List getPartialPlanViews( int numToReturn) {
    int numFound = 0;
    List partialPlanViewList = new ArrayList();
    List windowKeyList = new ArrayList( views.keySet());
    Iterator windowListItr = windowKeyList.iterator();
    while (windowListItr.hasNext()) {
      Object viewWindowKey = (Object) windowListItr.next();
      MDIInternalFrame viewFrame = (MDIInternalFrame) views.get( viewWindowKey);
      Container contentPane = viewFrame.getContentPane();
      Component[] components = contentPane.getComponents();
      for (int i = 0, n = components.length; i < n; i++) {
        Component component = components[i];
        if (component instanceof PartialPlanView) {
          partialPlanViewList.add( (PartialPlanView) component);
          numFound++;
          if ((numToReturn != 0) && (numFound >= numToReturn)) {
            return partialPlanViewList;
          }
        }
      }
    }
    return partialPlanViewList;
  } // end getAllPartialPlanViews

} // end class PartialPlanViewSet

