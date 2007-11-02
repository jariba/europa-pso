// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanViewSet.java,v 1.24 2004-10-07 20:19:07 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResource;
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
import gov.nasa.arc.planworks.viz.ViewListener;
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

  private static Object staticObject = new Object();

  private ColorStream colorStream;
  private PwToken activeToken; // in timeline view, the base token
  private List secondaryTokens; // in timeline view, the overloaded tokens
  private PwResource activeResource; // in resource extent view
  private int navigatorFrameCnt;
  private int ruleFrameCnt;
  private Map unaryResourceFrameCnt;
  private MDIDesktopFrame desktopFrame;
  private PartialPlanViewState state;


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
    this.desktopFrame = desktopFrame;
    this.state = null; // view initiated from SequenceStepsView
    commonConstructor();
  }

  /**
   * <code>PartialPlanViewSet</code> - constructor 
   *
   * @param desktopFrame - <code>MDIDesktopFrame</code> - 
   * @param viewable - <code>ViewableObject</code> - 
   * @param state - <code>PartialPlanViewState</code> - 
   * @param remover - <code>ViewSetRemover</code> - 
   */
  public PartialPlanViewSet( MDIDesktopFrame desktopFrame, ViewableObject viewable,
                             PartialPlanViewState state, ViewSetRemover remover) {
    super( desktopFrame, viewable, remover);
    this.desktopFrame = desktopFrame;
    this.state = state; // view initiated by View step buttons
    commonConstructor();
  }

  private void commonConstructor() {
    synchronized( staticObject) {
      if (((PwPartialPlan) viewable).isDummyPartialPlan()) {
	// using dummy partial plan for DBTransactionView when step files are not
	// in database or on disk
	return;
      }
      this.colorStream = new ColorStream();

      //this is to ensure that the colors always come out in the right order.
      //it works because objectIds are stored in a TreeMap
      Iterator objIt = ((PwPartialPlan)viewable).getObjectList().iterator();
      while(objIt.hasNext()) {
        colorStream.getColor(((PwObject)objIt.next()).getId());
      }

      this.activeResource = null;
      Point windowLocation = null;
      if (this.contentSpecWindow != null) {
        windowLocation = this.contentSpecWindow.getLocation();
      }
      this.contentSpecWindow = desktopFrame.createFrame( ViewConstants.CONTENT_SPEC_TITLE +
                                                         " for " + viewable.getName(),
                                                         this, true, false, false, true);
      Container contentPane = this.contentSpecWindow.getContentPane();
      this.contentSpec = new PartialPlanContentSpec( viewable, this);
      ((PwPartialPlan) viewable).setContentSpec( this.contentSpec.getCurrentSpec());
      contentPane.add( new ContentSpecWindow( this.contentSpecWindow, this.contentSpec, this));
      this.contentSpecWindow.pack();

      if (state == null) { // do not relocate for step buttons creation
        String seqUrl = ((PwPartialPlan) viewable).getSequenceUrl();
        int sequenceStepsViewHeight =
          (int) ((PlanWorks.getPlanWorks().
                  getSequenceStepsViewFrame( seqUrl).getSize().getHeight() * 0.5) +
                 (ViewConstants.MDI_FRAME_DECORATION_HEIGHT / 2.0));
        int delta = 0;
        // do not use deltas -- causes windows to slip off screen
        //       int delta = Math.min( (int) (((ViewManager) remover).getContentSpecWindowCnt() *
        //                                    ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4),
        //                             (int) (PlanWorks.getPlanWorks().getSize().getHeight() -
        //                                    sequenceStepsViewHeight -
        //                                    (ViewConstants.MDI_FRAME_DECORATION_HEIGHT * 2)));
        this.contentSpecWindow.setLocation( delta, sequenceStepsViewHeight + delta);
      } else {
	if (state.getContentSpecWindowLocation() != null) {
	  this.contentSpecWindow.setLocation( state.getContentSpecWindowLocation());
	}
      }
      this.contentSpecWindow.setVisible(true);

      navigatorFrameCnt = 0;
      ruleFrameCnt = 0;
      unaryResourceFrameCnt = new HashMap();
    }
  } // end commonConstructor

  public MDIInternalFrame openView(String viewClassName, ViewListener viewListener) {
    MDIInternalFrame retval = super.openView( viewClassName, viewListener);
    return retval;
  }

  public MDIInternalFrame openView(String viewClassName, PartialPlanViewState state) {
    MDIInternalFrame retval = super.openView(viewClassName, state);
    Container contentPane = retval.getContentPane();
    return retval;
  }

  public MDIInternalFrame openView(String viewClassName, PartialPlanViewState state,
                                   ViewListener viewListener) {
    MDIInternalFrame retval = super.openView(viewClassName, state, viewListener);
    Container contentPane = retval.getContentPane();
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

 /** 
   * <code>getActiveResource</code> - user selected view focus
   *
   * @return - <code>PwResource</code>
   */
  public PwResource getActiveResource() {
    return activeResource;
  }

  /**
   * <code>setActiveResource</code> - make this resource the view focus
   *
   * @param resource - <code>PwResource</code>
   */
  public void setActiveResource( PwResource resource) {
    activeResource = resource;
  }

  public List getCurrentSpec() {
    if (contentSpec != null) {
      return contentSpec.getCurrentSpec();
    } else {
      return null;
    }
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
   * <code>setNavigatorFrameCnt</code>
   *
   * @param cnt - <code>int</code> - 
   */
  public void setNavigatorFrameCnt( int cnt) {
    navigatorFrameCnt = cnt;
  }

  /**
   * <code>getRuleFrameCnt</code>
   *
   * @return - <code>int</code> - 
   */
  public int getRuleFrameCnt() {
    return ruleFrameCnt;
  }

  /**
   * <code>incrRuleFrameCnt</code>
   *
   */
  public void incrRuleFrameCnt() {
    ruleFrameCnt++;
  }

  /**
   * <code>setRuleFrameCnt</code>
   *
   * @param cnt - <code>int</code> - 
   */
  public void setRuleFrameCnt( int cnt) {
    ruleFrameCnt = cnt;
  }

  /**
   * <code>getUnaryResourceFrameCnt</code>
   *
   * @param name - <code>String</code> - 
   * @return - <code>int</code> - 
   */
  public int getUnaryResourceFrameCnt( String name) {
    return ((Integer) unaryResourceFrameCnt.get( name)).intValue();
  }

  public void incrUnaryResourceFrameCnt( String name) {
    if (unaryResourceFrameCnt.get( name) == null) {
      unaryResourceFrameCnt.put( name, new Integer( 1));
    } else {
      Integer cnt = (Integer) unaryResourceFrameCnt.get( name);
      setUnaryResourceFrameCnt( name, cnt.intValue() + 1);
    }
  }

  /**
   * <code>setUnaryResourceFrameCnt</code>
   *
   * @param name - <code>String</code> - 
   * @param value - <code>int</code> - 
   */
  public void setUnaryResourceFrameCnt( String name, int value) {
    unaryResourceFrameCnt.put( name, new Integer( value));
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
  } // end getPartialPlanViews

} // end class PartialPlanViewSet

