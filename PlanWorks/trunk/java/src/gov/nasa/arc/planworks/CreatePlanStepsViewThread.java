// 
// $Id: CreatePlanStepsViewThread.java,v 1.1 2003-09-30 19:18:54 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 30sep03
//

package gov.nasa.arc.planworks;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.viz.sequence.planDbSize.PlanDbSizeView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>CreatePlanStepsViewThread</code> - handles PlanWorks plan steps view actions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class CreatePlanStepsViewThread extends Thread {

  private String viewName;
  private JMenuItem menuItem;

  /**
   * <code>CreatePlanStepsViewThread</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>JMenuItem</code> - 
   */
  public CreatePlanStepsViewThread( String viewName, JMenuItem menuItem) {
    this.viewName = viewName;
    this.menuItem = menuItem;
  }

  /**
   * <code>run</code>
   *
   */
  public void run() {
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    JMenu seqStepsMenu = dynamicMenuBar.disableMenu( PlanWorks.SEQSTEPS_MENU);
    PlanWorks.planWorks.projectMenu.setEnabled( false);

    if (viewName.equals( PlanWorks.PLAN_DB_SIZE_MENU_ITEM)) {
      new PlanDbSizeView();
    }

    PlanWorks.planWorks.projectMenu.setEnabled( true);
    dynamicMenuBar.enableMenu( planSeqMenu);
    dynamicMenuBar.enableMenu( seqStepsMenu);
  }


} // end class CreatePlanStepsViewThread
