package gov.nasa.arc.planworks;

import javax.swing.JMenu;

import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;

public class NewSequenceThread extends Thread {
  public NewSequenceThread() {
  }

  public void run() {
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu(PlanWorks.PLANSEQ_MENU);
    PlanWorks.projectMenu.setEnabled(false);
    newSequence();
    PlanWorks.planWorks.projectMenu.setEnabled(true);
    PlanWorks.planWorks.setProjectMenuEnabled(PlanWorks.DELSEQ_MENU_ITEM, true);
    dynamicMenuBar.enableMenu(planSeqMenu);
  }

  private void newSequence() {
    PlanWorks.planWorks.executeDialog.show();
  }
}
