// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanViewSet.java,v 1.1 2003-09-25 23:52:44 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.partialPlan;

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
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.util.ColorStream;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSetRemover;


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


  public PartialPlanViewSet( MDIDesktopFrame desktopFrame, PwPartialPlan partialPlan,
                             ViewSetRemover remover) {
    super( desktopFrame, partialPlan, remover);
    this.colorStream = new ColorStream();
    this.activeToken = null;
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

