// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DeleteSequenceThread.java,v 1.12 2004-08-25 18:40:58 taylor Exp $
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
import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>DeleteSequenceThread</code> - handles PlanWorks delete sequence action
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class DeleteSequenceThread extends ThreadWithProgressMonitor {

  private boolean doProgMonitor;

  /**
   * <code>DeleteSequenceThread</code> - constructor 
   *
   */
  public DeleteSequenceThread( ThreadListener threadListener) {
    doProgMonitor = true;
    if (System.getProperty("ant.target.test").equals( "true")) {
      doProgMonitor = false;
    }
    if (threadListener != null) {
      addThreadListener( threadListener);
    }
  }

  /**
   * <code>run</code>
   *
   */
  public void run() {
    handleEvent( ThreadListener.EVT_THREAD_BEGUN);
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
    JMenu planSeqMenu = dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
    PlanWorks.getPlanWorks().projectMenu.setEnabled(false);

    if (doProgMonitor) {
      progressMonitorThread( "Deleting sequence ...", 0, 6);
      if (! progressMonitorWait()) {
        return;
      }
      progressMonitor.setProgress( 3 * ViewConstants.MONITOR_MIN_MAX_SCALING);
    }

    deleteSequence();

    PlanWorks.getPlanWorks().projectMenu.setEnabled(true);
    dynamicMenuBar.enableMenu( planSeqMenu);

    if (doProgMonitor) {
      isProgressMonitorCancel = true;
    }
    handleEvent( ThreadListener.EVT_THREAD_ENDED);
  } // end run

  private void deleteSequence() {
    List sequenceNames = PlanWorks.getPlanWorks().currentProject.listPlanningSequences();
    if (sequenceNames.size() != 0) {
      Object[] options = new Object[sequenceNames.size()];
      for (int i = 0, n = sequenceNames.size(); i < n; i++) {
        options[i] = (String) sequenceNames.get( i);
      }
      Object response = JOptionPane.showInputDialog
        ( PlanWorks.getPlanWorks(), "", "Delete Sequence", JOptionPane.QUESTION_MESSAGE,
          null, options, options[0]);
      if (response instanceof String) {
        for (int i = 0, n = options.length; i < n; i++) {
          if (((String) options[i]).equals( response)) {
            String sequenceName = (String) sequenceNames.get( i);
            System.out.println( "Deleting Sequence: " + sequenceName + " ...");
            long startTimeMSecs = System.currentTimeMillis();
            try {
              PwPlanningSequence seq =
                PlanWorks.getPlanWorks().currentProject.getPlanningSequence( sequenceName);
              if (PlanWorks.getPlanWorks().viewManager.getViewSet( seq) != null) {
                PlanWorks.getPlanWorks().viewManager.getViewSet( seq).close();
                PlanWorks.getPlanWorks().viewManager.removeViewSet( seq);
              }
              PlanWorks.getPlanWorks().currentProject.deletePlanningSequence( sequenceName);
              ListIterator partialPlanIterator = seq.getPartialPlansList().listIterator();
              while (partialPlanIterator.hasNext()) {
                PwPartialPlan plan = (PwPartialPlan) partialPlanIterator.next();
                if (PlanWorks.getPlanWorks().viewManager.getViewSet(plan) != null) {
                  PlanWorks.getPlanWorks().viewManager.getViewSet(plan).close();
                  PlanWorks.getPlanWorks().viewManager.removeViewSet(plan);
                }
              }
              seq.delete();
              MDIDynamicMenuBar dynamicMenuBar =
                (MDIDynamicMenuBar) PlanWorks.getPlanWorks().getJMenuBar();
              JMenu sequenceMenu = null;
              for (int j = 0; j < dynamicMenuBar.getMenuCount(); j++) {
                if (dynamicMenuBar.getMenu(j).getText().equals( PlanWorks.PLANSEQ_MENU)) {
                  sequenceMenu = dynamicMenuBar.getMenu(j);
                }
              }
              if(sequenceMenu == null) {
                throw new Exception("Failed to find Planning Sequence menu when deleting sequence.");
              }
              String menuName =
                (String) PlanWorks.getPlanWorks().getSequenceMenuName( sequenceName);
              for (int j = 0; j < sequenceMenu.getItemCount(); j++) {
                if (sequenceMenu.getItem(j).getText().equals(menuName)) {
                  sequenceMenu.remove(j);
                  break;
                }
              }
              if (sequenceMenu.getItemCount() == 0) {
		dynamicMenuBar.disableMenu( PlanWorks.PLANSEQ_MENU);
                // dynamicMenuBar.remove(sequenceMenu);
                PlanWorks.getPlanWorks().setProjectMenuEnabled( PlanWorks.DELSEQ_MENU_ITEM, false);
              }
              long stopTimeMSecs = System.currentTimeMillis();
              System.err.println( "   ... elapsed time: " +
                                  (stopTimeMSecs - startTimeMSecs) + " msecs.");
            } catch (ResourceNotFoundException rnfExcep) {
              int index = rnfExcep.getMessage().indexOf( ":");
              JOptionPane.showMessageDialog
                (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
                 "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
              System.err.println( rnfExcep);
              rnfExcep.printStackTrace();
              return;
            } catch (Exception excep) {
              excep.printStackTrace();
              System.err.println( " delete: excep " + excep);
              int index = excep.getMessage().indexOf( ":");
              JOptionPane.showMessageDialog
                (PlanWorks.getPlanWorks(), excep.getMessage().substring( index + 1),
                 "Exception", JOptionPane.ERROR_MESSAGE);
              System.err.println( excep);
              excep.printStackTrace();
            }
            break;
          }
        }
      }
    } else {
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), "Project: " +
         PlanWorks.getPlanWorks().getCurrentProjectName(), "No Sequences Available", 
         JOptionPane.INFORMATION_MESSAGE);
    }
  }

} // end class DeleteSequenceThread
