// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PartialPlanViewMenu.java,v 1.3 2003-11-06 00:02:18 taylor Exp $
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

  public void buildPartialPlanViewMenu( String partialPlanName,
                                        PwPlanningSequence planSequence) {
    int stepNumber = Utilities.getStepNumber( partialPlanName);
    String seqUrl = planSequence.getUrl();
    String seqName = planSequence.getName();
    Iterator viewNamesItr = PlanWorks.planWorks.supportedViewNames.iterator();
    while (viewNamesItr.hasNext()) {
      String viewName = (String) viewNamesItr.next();
      if (viewName.equals( PlanWorks.CONSTRAINT_NETWORK_VIEW)) {
        PartialPlanViewMenuItem constraintNetworkViewItem =
          new PartialPlanViewMenuItem( PlanWorks.CONSTRAINT_NETWORK_VIEW,
                                       seqUrl, seqName, partialPlanName);
        constraintNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.CONSTRAINT_NETWORK_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( constraintNetworkViewItem);
      } else if (viewName.equals( PlanWorks.TEMPORAL_EXTENT_VIEW)) {
        PartialPlanViewMenuItem temporalExtentViewItem =
          new PartialPlanViewMenuItem( PlanWorks.TEMPORAL_EXTENT_VIEW,
                                       seqUrl, seqName, partialPlanName);
        temporalExtentViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.TEMPORAL_EXTENT_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( temporalExtentViewItem);
      } else if (viewName.equals( PlanWorks.TEMPORAL_NETWORK_VIEW)) {
        PartialPlanViewMenuItem temporalNetworkViewItem =
          new PartialPlanViewMenuItem( PlanWorks.TEMPORAL_NETWORK_VIEW,
                                       seqUrl, seqName, partialPlanName);
        temporalNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.TEMPORAL_NETWORK_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( temporalNetworkViewItem);
        temporalNetworkViewItem.setEnabled(false);
      } else if (viewName.equals( PlanWorks.TIMELINE_VIEW)) {
        PartialPlanViewMenuItem timelineViewItem =
          new PartialPlanViewMenuItem( PlanWorks.TIMELINE_VIEW,
                                       seqUrl, seqName, partialPlanName);
        timelineViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.TIMELINE_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( timelineViewItem);
      } else if (viewName.equals( PlanWorks.TOKEN_NETWORK_VIEW)) {
        PartialPlanViewMenuItem tokenNetworkViewItem =
          new PartialPlanViewMenuItem( PlanWorks.TOKEN_NETWORK_VIEW,
                                       seqUrl, seqName, partialPlanName);
        tokenNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.TOKEN_NETWORK_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( tokenNetworkViewItem);
      } else if (viewName.equals( PlanWorks.TRANSACTION_VIEW)) {
        PartialPlanViewMenuItem transactionViewItem =
          new PartialPlanViewMenuItem( PlanWorks.TRANSACTION_VIEW,
                                       seqUrl, seqName, partialPlanName);
        transactionViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.TRANSACTION_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( transactionViewItem);
      }
    }

  } // end buildPartialPlanViewMenu


  private void createPartialPlanViewThread( String viewName,
                                            PartialPlanViewMenuItem menuItem) {
    new CreatePartialPlanViewThread( viewName, menuItem).start();
  } // end createPartialPlanViewThread






} // end PartialPlanViewMenu


