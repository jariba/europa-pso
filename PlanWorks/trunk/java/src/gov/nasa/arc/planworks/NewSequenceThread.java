// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NewSequenceThread.java,v 1.4 2004-04-22 19:26:17 taylor Exp $
//
package gov.nasa.arc.planworks;

import javax.swing.JMenu;

import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;

public class NewSequenceThread extends Thread {
  public NewSequenceThread() {
  }

  public void run() {
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu(PlanWorks.PLANSEQ_MENU);
    PlanWorks.projectMenu.setEnabled(false);
    newSequence();
    PlanWorks.getPlanWorks().projectMenu.setEnabled(true);
    PlanWorks.getPlanWorks().setProjectMenuEnabled(PlanWorks.DELSEQ_MENU_ITEM, true);
    dynamicMenuBar.enableMenu(planSeqMenu);
  }

  private void newSequence() {
    //PlanWorks.getPlanWorks().executeDialog.show();
  }
}
