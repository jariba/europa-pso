// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanViewSet.java,v 1.2 2003-09-28 00:19:30 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.util.PartialPlanContentSpec;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.util.ColorStream;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSetRemover;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.ContentSpecWindow;


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


  public PartialPlanViewSet( MDIDesktopFrame desktopFrame, ViewableObject viewable,
                             ViewSetRemover remover) {
    super( desktopFrame, viewable, remover);
    this.colorStream = new ColorStream();
    this.activeToken = null;
    this.contentSpecWindow = desktopFrame.createFrame( "Content specification for " +
                                                       viewable.getName(),
                                                       this, true, false, false, true);
    Container contentPane = this.contentSpecWindow.getContentPane();
    this.contentSpec = new PartialPlanContentSpec( viewable, this);
    ((PwPartialPlan) viewable).setContentSpec( this.contentSpec.getCurrentSpec());
    contentPane.add( new ContentSpecWindow( this.contentSpecWindow, this.contentSpec));
    this.contentSpecWindow.pack();
    this.contentSpecWindow.setVisible(true);
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



} // end class PartialPlanViewSet

