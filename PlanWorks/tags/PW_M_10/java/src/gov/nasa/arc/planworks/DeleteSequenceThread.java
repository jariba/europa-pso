// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DeleteSequenceThread.java,v 1.5 2003-11-03 19:02:39 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- split off from PlanWorks.java 29sep03
//

package gov.nasa.arc.planworks;

import java.util.List;
import java.util.ListIterator;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>DeleteSequenceThread</code> - handles PlanWorks delete sequence action
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class DeleteSequenceThread extends Thread {

  /**
   * <code>DeleteSequenceThread</code> - constructor 
   *
   */
  public DeleteSequenceThread() {
  }

  /**
   * <code>run</code>
   *
   */
  public void run() {
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.planWorks.projectMenu.setEnabled(false);

    deleteSequence();

    PlanWorks.planWorks.projectMenu.setEnabled(true);
    dynamicMenuBar.enableMenu( planSeqMenu);
  }

  private void deleteSequence() {
    List sequenceNames = PlanWorks.planWorks.currentProject.listPlanningSequences();
    Object[] options = new Object[sequenceNames.size()];
    for (int i = 0, n = sequenceNames.size(); i < n; i++) {
      options[i] = (String) sequenceNames.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( PlanWorks.planWorks, "", "Delete Sequence", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          String sequenceName = (String) sequenceNames.get( i);
          System.out.println( "Deleting Sequence: " + sequenceName + " ...");
          long startTimeMSecs = System.currentTimeMillis();
          try {
            PwPlanningSequence seq =
              PlanWorks.planWorks.currentProject.getPlanningSequence( sequenceName);
              if (PlanWorks.planWorks.viewManager.getViewSet( seq) != null) {
                PlanWorks.planWorks.viewManager.getViewSet( seq).close();
                PlanWorks.planWorks.viewManager.removeViewSet( seq);
              }
            PlanWorks.planWorks.currentProject.deletePlanningSequence( sequenceName);
            ListIterator partialPlanIterator = seq.getPartialPlansList().listIterator();
            while (partialPlanIterator.hasNext()) {
              PwPartialPlan plan = (PwPartialPlan) partialPlanIterator.next();
              if (PlanWorks.planWorks.viewManager.getViewSet(plan) != null) {
                PlanWorks.planWorks.viewManager.getViewSet(plan).close();
                PlanWorks.planWorks.viewManager.removeViewSet(plan);
              }
            }
            seq.delete();
            MDIDynamicMenuBar dynamicMenuBar =
              (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
            JMenu sequenceMenu = null;
            for (int j = 0; j < dynamicMenuBar.getMenuCount(); j++) {
              if (dynamicMenuBar.getMenu(j).getText().equals( PlanWorks.PLANSEQ_MENU)) {
                sequenceMenu = dynamicMenuBar.getMenu(j);
              }
            }
            if(sequenceMenu == null) {
              throw new Exception("Failed to find Planning Sequence menu when deleting sequence.");
            }
            String menuName = (String) PlanWorks.planWorks.sequenceNameMap.get(sequenceName);
            for (int j = 0; j < sequenceMenu.getItemCount(); j++) {
              if (sequenceMenu.getItem(j).getText().equals(menuName)) {
                sequenceMenu.remove(j);
                break;
              }
            }
            if (sequenceMenu.getItemCount() == 0) {
              dynamicMenuBar.remove(sequenceMenu);
              PlanWorks.planWorks.setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, false);
            }
            long stopTimeMSecs = System.currentTimeMillis();
            System.err.println( "   ... elapsed time: " +
                                (stopTimeMSecs - startTimeMSecs) + " msecs.");
          } catch (ResourceNotFoundException rnfExcep) {
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
            rnfExcep.printStackTrace();
            return;
          } catch (Exception excep) {
            excep.printStackTrace();
            System.err.println( " delete: excep " + excep);
            int index = excep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.planWorks, excep.getMessage().substring( index + 1),
               "Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( excep);
            excep.printStackTrace();
          }
          break;
        }
      }
    }
  }

} // end class DeleteSequenceThread
