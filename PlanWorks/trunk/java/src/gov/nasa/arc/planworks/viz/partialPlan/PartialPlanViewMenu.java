// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PartialPlanViewMenu.java,v 1.8 2004-02-03 20:43:54 taylor Exp $
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
      if (viewName.equals( PlanWorks.CONSTRAINT_NETWORK_VIEW)) {
        PartialPlanViewMenuItem constraintNetworkViewItem =
          createOpenViewItem( viewName, partialPlanName, planSequence);
        this.add( constraintNetworkViewItem);

      } else if (viewName.equals( PlanWorks.RESOURCE_PROFILE_VIEW)) {
        PartialPlanViewMenuItem resourceProfileViewItem =
          createOpenViewItem( viewName, partialPlanName, planSequence);
        this.add( resourceProfileViewItem);

      } else if (viewName.equals( PlanWorks.TEMPORAL_EXTENT_VIEW)) {
        PartialPlanViewMenuItem temporalExtentViewItem =
          createOpenViewItem( viewName, partialPlanName, planSequence);
        this.add( temporalExtentViewItem);

      } else if (viewName.equals( PlanWorks.TEMPORAL_NETWORK_VIEW)) {
        PartialPlanViewMenuItem temporalNetworkViewItem =
          createOpenViewItem( viewName, partialPlanName, planSequence);
        temporalNetworkViewItem.setEnabled(false);
        this.add( temporalNetworkViewItem);

      } else if (viewName.equals( PlanWorks.TIMELINE_VIEW)) {
        PartialPlanViewMenuItem timelineViewItem =
          createOpenViewItem( viewName, partialPlanName, planSequence);
        this.add( timelineViewItem);

      } else if (viewName.equals( PlanWorks.TOKEN_NETWORK_VIEW)) {
        PartialPlanViewMenuItem tokenNetworkViewItem =
          createOpenViewItem( viewName, partialPlanName, planSequence);
        this.add( tokenNetworkViewItem);

      } else if (viewName.equals( PlanWorks.DB_TRANSACTION_VIEW)) {
        PartialPlanViewMenuItem transactionViewItem =
          createOpenViewItem( viewName, partialPlanName, planSequence);
        this.add( transactionViewItem);
      }
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
  public PartialPlanViewMenuItem createOpenViewItem( String viewName, String partialPlanName,
                                                     PwPlanningSequence planSequence) {
    String seqUrl = planSequence.getUrl();
    String seqName = planSequence.getName();
    if (viewName.equals( PlanWorks.CONSTRAINT_NETWORK_VIEW)) {
      PartialPlanViewMenuItem constraintNetworkViewItem =
        new PartialPlanViewMenuItem( "Open " + PlanWorks.CONSTRAINT_NETWORK_VIEW,
                                     seqUrl, seqName, partialPlanName);
      constraintNetworkViewItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent e) {
            PartialPlanViewMenu.this.createPartialPlanViewThread
              ( PlanWorks.CONSTRAINT_NETWORK_VIEW, (PartialPlanViewMenuItem) e.getSource());
          }});
      return constraintNetworkViewItem;
    } else if (viewName.equals( PlanWorks.RESOURCE_PROFILE_VIEW)) {
      PartialPlanViewMenuItem resourceProfileViewItem =
        new PartialPlanViewMenuItem( "Open " + PlanWorks.RESOURCE_PROFILE_VIEW,
                                     seqUrl, seqName, partialPlanName);
      resourceProfileViewItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent e) {
            PartialPlanViewMenu.this.createPartialPlanViewThread
              ( PlanWorks.RESOURCE_PROFILE_VIEW, (PartialPlanViewMenuItem) e.getSource());
          }});
      return resourceProfileViewItem;
    } else if (viewName.equals( PlanWorks.TEMPORAL_EXTENT_VIEW)) {
      PartialPlanViewMenuItem temporalExtentViewItem =
        new PartialPlanViewMenuItem( "Open " + PlanWorks.TEMPORAL_EXTENT_VIEW,
                                     seqUrl, seqName, partialPlanName);
      temporalExtentViewItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent e) {
            PartialPlanViewMenu.this.createPartialPlanViewThread
              ( PlanWorks.TEMPORAL_EXTENT_VIEW, (PartialPlanViewMenuItem) e.getSource());
          }});
      return temporalExtentViewItem;
    } else if (viewName.equals( PlanWorks.TEMPORAL_NETWORK_VIEW)) {
      PartialPlanViewMenuItem temporalNetworkViewItem =
        new PartialPlanViewMenuItem( "Open " + PlanWorks.TEMPORAL_NETWORK_VIEW,
                                     seqUrl, seqName, partialPlanName);
      temporalNetworkViewItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent e) {
            PartialPlanViewMenu.this.createPartialPlanViewThread
              ( PlanWorks.TEMPORAL_NETWORK_VIEW, (PartialPlanViewMenuItem) e.getSource());
          }});
      temporalNetworkViewItem.setEnabled( false);
      return temporalNetworkViewItem;
    } else if (viewName.equals( PlanWorks.TIMELINE_VIEW)) {
      PartialPlanViewMenuItem timelineViewItem =
        new PartialPlanViewMenuItem( "Open " + PlanWorks.TIMELINE_VIEW,
                                     seqUrl, seqName, partialPlanName);
      timelineViewItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent e) {
            PartialPlanViewMenu.this.createPartialPlanViewThread
              ( PlanWorks.TIMELINE_VIEW, (PartialPlanViewMenuItem) e.getSource());
          }});
      return timelineViewItem;
    } else if (viewName.equals( PlanWorks.TOKEN_NETWORK_VIEW)) {
      PartialPlanViewMenuItem tokenNetworkViewItem =
        new PartialPlanViewMenuItem( "Open " + PlanWorks.TOKEN_NETWORK_VIEW,
                                     seqUrl, seqName, partialPlanName);
      tokenNetworkViewItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent e) {
            PartialPlanViewMenu.this.createPartialPlanViewThread
              ( PlanWorks.TOKEN_NETWORK_VIEW, (PartialPlanViewMenuItem) e.getSource());
          }});
      return tokenNetworkViewItem;
    } else if (viewName.equals( PlanWorks.DB_TRANSACTION_VIEW)) {
      PartialPlanViewMenuItem transactionViewItem =
        new PartialPlanViewMenuItem( "Open " + PlanWorks.DB_TRANSACTION_VIEW,
                                     seqUrl, seqName, partialPlanName);
      transactionViewItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent e) {
            PartialPlanViewMenu.this.createPartialPlanViewThread
              ( PlanWorks.DB_TRANSACTION_VIEW, (PartialPlanViewMenuItem) e.getSource());
          }});
      return transactionViewItem;
    }
    return null;
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


