// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceView.java,v 1.5 2004-09-21 01:07:08 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.sequence;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.CreatePartialPlanException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>SequenceView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SequenceView extends VizView {

  protected PwPlanningSequence planSequence;

  /**
   * <code>SequenceView</code> - constructor 
   *
   */
  public SequenceView( PwPlanningSequence planSequence, ViewSet viewSet) {
    super( viewSet);
    this.planSequence = planSequence;

    // Utilities.printFontNames();
  }

  /**
   * <code>getPlanSequence</code>
   *
   * @return - <code>PwPlanningSequence</code> - 
   */
  public final PwPlanningSequence getPlanSequence() {
    return planSequence;
  }

  /**
   * <code>getPartialPlan</code>
   *
   * @param stepNumber - <code>int</code> - 
   * @return - <code>PwPartialPlan</code> - 
   */
  protected PwPartialPlan getPartialPlan( int stepNumber) {
    PwPartialPlan partialPlan = null;
    try {
      partialPlan = planSequence.getPartialPlan( stepNumber);
    } catch (IndexOutOfBoundsException excp) {
    } catch (ResourceNotFoundException excpR) {
    } catch (CreatePartialPlanException excpPP) {
    }
    return partialPlan;
  } //  getPartialPlan

  /**
   * <code>createCloseHideShowViewItems</code>
   *
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   */
  public void createCloseHideShowViewItems( JPopupMenu mouseRightPopup) {
    List loadedPartialPlans = planSequence.getPartialPlansList();
    Iterator partialPlanItr = loadedPartialPlans.iterator();
    boolean areThereViewSets = false;
    while (partialPlanItr.hasNext()) {
      PwPartialPlan partialPlan = (PwPartialPlan) partialPlanItr.next();
      if (viewSet.getViewManager().getViewSet( partialPlan) != null) {
        areThereViewSets = true;
        break;
      }
    }
    if (areThereViewSets) {
      mouseRightPopup.addSeparator();
      String allViewsName = "All " + planSequence.getName() + "/step Views";
      JMenuItem closeAllItem = new JMenuItem( ("Close " + allViewsName));
      createCloseAllItem( closeAllItem, loadedPartialPlans);
      mouseRightPopup.add( closeAllItem);

      JMenuItem hideAllItem = new JMenuItem( ("Hide " + allViewsName));
      createHideAllItem( hideAllItem, loadedPartialPlans);
      mouseRightPopup.add( hideAllItem);

      JMenuItem showAllItem = new JMenuItem( ("Show " + allViewsName));
      createShowAllItem( showAllItem, loadedPartialPlans);
      mouseRightPopup.add( showAllItem);

    }
  } // end createCloseHideShowViewItems

  private void createCloseAllItem( JMenuItem closeAllItem, final List loadedPartialPlans) {
    closeAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          Iterator partialPlanItr = loadedPartialPlans.iterator();
          while (partialPlanItr.hasNext()) {
            PwPartialPlan partialPlan = (PwPartialPlan) partialPlanItr.next();
            PartialPlanViewSet partialPlanViewSet =
              (PartialPlanViewSet) viewSet.getViewManager().getViewSet( partialPlan);
            if (partialPlanViewSet != null) {
              partialPlanViewSet.close();
            }
          }
        }
      });
  } // end createCloseAllItem
 
  private void createHideAllItem( JMenuItem hideAllItem, final List loadedPartialPlans) {
    hideAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          Iterator partialPlanItr = loadedPartialPlans.iterator();
          while (partialPlanItr.hasNext()) {
            PwPartialPlan partialPlan = (PwPartialPlan) partialPlanItr.next();
            PartialPlanViewSet partialPlanViewSet =
              (PartialPlanViewSet) viewSet.getViewManager().getViewSet( partialPlan);
            if (partialPlanViewSet != null) {
              partialPlanViewSet.iconify();
            }
          }
        }
      });
  } // end createHideAllItem
 
  private void createShowAllItem( JMenuItem showAllItem, final List loadedPartialPlans) {
    showAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          Iterator partialPlanItr = loadedPartialPlans.iterator();
          while (partialPlanItr.hasNext()) {
            PwPartialPlan partialPlan = (PwPartialPlan) partialPlanItr.next();
            PartialPlanViewSet partialPlanViewSet =
              (PartialPlanViewSet) viewSet.getViewManager().getViewSet( partialPlan);
            if (partialPlanViewSet != null) {
              partialPlanViewSet.show();
            }
          }
        }
      });
  } // end createShowAllItem
 
} // end class SequenceView

