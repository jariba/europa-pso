// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PartialPlanViewMenu.java,v 1.1 2003-10-09 22:07:45 taylor Exp $
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
    int stepNumber = Integer.parseInt( partialPlanName.substring( 4)); // discard prefix "step"
    String seqUrl = planSequence.getUrl();
    String seqName = planSequence.getName();
    Iterator viewNamesItr = PlanWorks.planWorks.supportedViewNames.iterator();
    while (viewNamesItr.hasNext()) {
      String viewName = (String) viewNamesItr.next();
      if (viewName.equals( PlanWorks.CONSTRAINT_NETWORK_VIEW)) {
        PartialPlanViewMenuItem constraintNetworkViewItem =
          new PartialPlanViewMenuItem( Utilities.trimView( PlanWorks.CONSTRAINT_NETWORK_VIEW),
                                       seqUrl, seqName, partialPlanName);
        constraintNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.CONSTRAINT_NETWORK_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( constraintNetworkViewItem);
      } else if (viewName.equals( PlanWorks.TEMPORAL_EXTENT_VIEW)) {
        PartialPlanViewMenuItem temporalExtentViewItem =
          new PartialPlanViewMenuItem( Utilities.trimView( PlanWorks.TEMPORAL_EXTENT_VIEW),
                                       seqUrl, seqName, partialPlanName);
        temporalExtentViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.TEMPORAL_EXTENT_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( temporalExtentViewItem);
      } else if (viewName.equals( PlanWorks.TEMPORAL_NETWORK_VIEW)) {
        PartialPlanViewMenuItem temporalNetworkViewItem =
          new PartialPlanViewMenuItem( Utilities.trimView( PlanWorks.TEMPORAL_NETWORK_VIEW),
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
          new PartialPlanViewMenuItem( Utilities.trimView( PlanWorks.TIMELINE_VIEW),
                                       seqUrl, seqName, partialPlanName);
        timelineViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.TIMELINE_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( timelineViewItem);
      } else if (viewName.equals( PlanWorks.TOKEN_NETWORK_VIEW)) {
        PartialPlanViewMenuItem tokenNetworkViewItem =
          new PartialPlanViewMenuItem( Utilities.trimView( PlanWorks.TOKEN_NETWORK_VIEW),
                                       seqUrl, seqName, partialPlanName);
        tokenNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PartialPlanViewMenu.this.createPartialPlanViewThread
                ( PlanWorks.TOKEN_NETWORK_VIEW, (PartialPlanViewMenuItem) e.getSource());
            }});
        this.add( tokenNetworkViewItem);
      }
    }

  } // end buildPartialPlanViewMenu


  private void createPartialPlanViewThread( String viewName,
                                            PartialPlanViewMenuItem menuItem) {
    new CreatePartialPlanViewThread( viewName, menuItem).start();
  } // end createPartialPlanViewThread






} // end PartialPlanViewMenu




//       try {
//         partialPlan = planSequence.getPartialPlan( partialPlanName);
//         transactionList = planSequence.getTransactionsList( stepNumber);
//         // System.err.println( "stepNum " + stepNumber);
//         // if (transactionList != null) {
//         //   System.err.println( "transactionList size " + transactionList.size());
//         // }
//       } catch (ResourceNotFoundException rnfExcep) {
//         int index = rnfExcep.getMessage().indexOf( ":");
//         JOptionPane.showMessageDialog
//           (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
//            "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
//         System.err.println( rnfExcep);
//         rnfExcep.printStackTrace();
//         System.exit( -1);
//       }
