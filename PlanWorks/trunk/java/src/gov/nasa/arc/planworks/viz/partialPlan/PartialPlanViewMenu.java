// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PartialPlanViewMenu.java,v 1.12 2004-05-04 01:27:17 taylor Exp $
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
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;


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
   * @param viewListener - <code>ViewListener</code> - 
   */
  public void buildPartialPlanViewMenu( String partialPlanName,
                                        PwPlanningSequence planSequence,
                                        ViewListener viewListener) {
    Iterator viewNamesItr = ViewConstants.PARTIAL_PLAN_VIEW_LIST.iterator();
    while (viewNamesItr.hasNext()) {
      String viewName = (String) viewNamesItr.next();
      PartialPlanViewMenuItem viewItem = 
        createOpenViewItem(viewName, partialPlanName, planSequence, viewListener);
      this.add(viewItem);
    }

  } // end buildPartialPlanViewMenu


  /**
   * <code>CreateOpenViewListener</code> - 
   *
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

  /**
   * <code>createOpenViewItem</code>
   *
   * @param viewName - <code>String</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   * @return - <code>PartialPlanViewMenuItem</code> - 
   */
  public PartialPlanViewMenuItem createOpenViewItem( String viewName, String partialPlanName,
                                                     PwPlanningSequence planSequence,
                                                     ViewListener viewListener) {
    PartialPlanViewMenuItem viewItem = new PartialPlanViewMenuItem("Open " + viewName,
                                                                   planSequence.getUrl(),
                                                                   planSequence.getName(),
                                                                   partialPlanName,
                                                                   viewListener);
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


