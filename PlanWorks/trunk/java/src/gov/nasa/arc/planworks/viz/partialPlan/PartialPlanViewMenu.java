// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PartialPlanViewMenu.java,v 1.10 2004-03-12 23:22:34 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- started 09oct03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.JPopupMenu;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.Utilities;


/**
 * <code>PartialPlanViewMenu</code> - create menu items for views of a partial plan
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PartialPlanViewMenu extends JPopupMenu{

  /**
   * <code>PartialPlanViewMenu</code> - constructor 
   *
   */
  public PartialPlanViewMenu() {
    super();
  }

  /**
   * <code>buildPartialPlanViewMenu</code>
   *
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   */
  public void buildPartialPlanViewMenu( String partialPlanName,
                                        PwPlanningSequence planSequence) {
    Iterator viewNamesItr = PlanWorks.PARTIAL_PLAN_VIEW_LIST.iterator();
    while (viewNamesItr.hasNext()) {
      String viewName = (String) viewNamesItr.next();
      PartialPlanViewMenuItem viewItem = 
        createOpenViewItem(viewName, partialPlanName, planSequence);
      this.add(viewItem);
    }

  } // end buildPartialPlanViewMenu


  /**
   * <code>createOpenViewItem</code>
   *
   * @param viewName - <code>String</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @return - <code>PartialPlanViewMenuItem</code> - 
   */
  class CreateOpenViewListener implements ActionListener {
    private PartialPlanViewMenu menu;
    private String viewName;
    public CreateOpenViewListener(PartialPlanViewMenu menu, String viewName){
      this.menu = menu;
      this.viewName = viewName;
    }
    public void actionPerformed(ActionEvent e) {
      menu.createPartialPlanViewThread(viewName, (PartialPlanViewMenuItem) e.getSource());
    }
  }

  public PartialPlanViewMenuItem createOpenViewItem( String viewName, String partialPlanName,
                                                     PwPlanningSequence planSequence) {
    PartialPlanViewMenuItem viewItem = new PartialPlanViewMenuItem("Open " + viewName,
                                                                   planSequence.getUrl(),
                                                                   planSequence.getName(),
                                                                   partialPlanName);
    viewItem.addActionListener(new CreateOpenViewListener(this, viewName));
    return viewItem;
  } // end createOpenViewItem


  /**
   * <code>createPartialPlanViewThread</code>
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>PartialPlanViewMenuItem</code> - 
   */
  public static void createPartialPlanViewThread( String viewName,
                                                  PartialPlanViewMenuItem menuItem) {
    Thread thread = new CreatePartialPlanViewThread(viewName, menuItem);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  } // end createPartialPlanViewThread
} // end PartialPlanViewMenu


