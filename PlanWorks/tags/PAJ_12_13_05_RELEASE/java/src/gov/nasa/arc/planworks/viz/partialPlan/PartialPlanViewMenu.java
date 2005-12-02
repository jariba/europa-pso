// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PartialPlanViewMenu.java,v 1.20 2005-11-10 01:22:12 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- started 09oct03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPopupMenu;

import gov.nasa.arc.planworks.CreateViewThread;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.CreatePartialPlanException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>PartialPlanViewMenu</code> - create menu items for views of a partial plan
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PartialPlanViewMenu extends JPopupMenu{

  private ViewListener viewListenerWait01;
  private boolean wait01;

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
   * @param viewListenerList - <code>List</code> - 
   * @return - <code>int</code> - 
   */
  public int buildPartialPlanViewMenu( String partialPlanName,
				       PwPlanningSequence planSequence,
				       List viewListenerList) {
    if (viewListenerList.size() != PlanWorks.PARTIAL_PLAN_VIEW_LIST.size()) {
      System.err.println( "buildPartialPlanViewMenu: num view listeners not = " +
                          PlanWorks.PARTIAL_PLAN_VIEW_LIST.size());
      System.exit( -1);
    }
    int numItemsAdded = 0;
    Iterator viewNamesItr = PlanWorks.PARTIAL_PLAN_VIEW_LIST.iterator();
    Iterator viewListenerItr = viewListenerList.iterator();
    while (viewNamesItr.hasNext()) {
      String viewName = (String) viewNamesItr.next();
      ViewListener viewListener = (ViewListener) viewListenerItr.next();
      if (planSequence.doesPartialPlanExist( partialPlanName)){
	PartialPlanViewMenuItem viewItem = createOpenViewItem( viewName, partialPlanName,
							       planSequence, viewListener);
	numItemsAdded++;
	this.add(viewItem);
      }
    }
    return numItemsAdded;
  } // end buildPartialPlanViewMenu

  public boolean getViewListenerWait01() {
    return wait01;
  }

  public void setViewListenerWait01( boolean value) {
    wait01 = value;
  }

  public class ViewListenerWait01 extends ViewListener {
    private PartialPlanViewMenu viewMenu;
    public ViewListenerWait01( PartialPlanViewMenu viewMenu) {
      super();
      this.viewMenu = viewMenu;
      viewMenu.setViewListenerWait01( false); 
    }
    public void reset() {
      viewMenu.setViewListenerWait01( false);
    }
    public void initDrawingEnded() {
      String shortClassName = this.getClass().getName();
      int index = shortClassName.indexOf( "$");
      System.err.println( shortClassName.substring( index + 1) + " released");
      viewMenu.setViewListenerWait01( true);
    }
    public void viewWait() {
      while (! viewMenu.getViewListenerWait01()) {
        try {
          Thread.currentThread().sleep( 50);
        } catch (InterruptedException excp) {
        }
        // System.err.println( "viewMenuWait01 still false");
      }
    } 
  } // end class ViewListenerWait01

   
  /**
   * <code>CreateOpenViewListener</code> - 
   *
   */
  class CreateOpenViewListener implements ActionListener {
    private PartialPlanViewMenu menu;
    private String viewName;
    private ViewSet viewSet;
    private Integer idToFind;
    private PwPartialPlan partialPlan;

    public CreateOpenViewListener(PartialPlanViewMenu menu, String viewName) {
      this.menu = menu;
      this.viewName = viewName;
      this.partialPlan = null;
      this.viewSet = null;
      this.idToFind = null;
    }

    public CreateOpenViewListener(PartialPlanViewMenu menu, String viewName,
                                  PwPartialPlan partialPlan, ViewSet viewSet,
                                  Integer idToFind) {
      this.menu = menu;
      this.viewName = viewName;
      this.partialPlan = partialPlan;
      this.viewSet = viewSet;
      this.idToFind = idToFind;
    }

    public void actionPerformed(ActionEvent e) {
      boolean viewExists = false;
      if ((viewSet != null) && (idToFind != null)) {
        viewExists = viewSet.doesViewFrameExist( viewName);
      }
      
      menu.createPartialPlanViewThread( viewName, (PartialPlanViewMenuItem) e.getSource());

      if ((viewSet != null) && (idToFind != null)) {
        if (! viewExists) {
          menu.createFindIdInViewThread( idToFind, viewName, partialPlan, viewSet,
                                         viewExists);
        } else {
          findIdInView( idToFind, viewName, partialPlan, viewSet, viewExists);
        }
      }
    } // end actionPerformed

  } // end class CreateOpenViewListener


  private void findIdInView( Integer idToFind, String viewName, PwPartialPlan partialPlan,
                             ViewSet viewSet, boolean viewExists) {
    if (! viewExists) {
      viewListenerWait01.viewWait();
    }
    boolean isByKey = true;
    int xLoc = 0;
    MDIInternalFrame viewFrame = viewSet.getViewFrame( viewName);
    if (viewName.equals( ViewConstants.CONSTRAINT_NETWORK_VIEW)) {
      ConstraintNetworkView constraintNetworkView =
        ViewGenerics.getConstraintNetworkView( viewFrame);
      constraintNetworkView.findAndSelectNodeKey( idToFind);
    } else if (viewName.equals( ViewConstants.RESOURCE_PROFILE_VIEW)) {
      ResourceProfileView resourceProfileView = ViewGenerics.getResourceProfileView( viewFrame);
      if (partialPlan.getResource( idToFind) != null) {
        resourceProfileView.findAndSelectResource( partialPlan.getResource( idToFind), xLoc);
      } else if (partialPlan.getResourceTransaction( idToFind) != null) {
        PwResource resource = resourceProfileView.findActiveResourceByTrans
          ( partialPlan.getResourceTransaction( idToFind));
        if (resource != null) {
          resourceProfileView.findAndSelectResource( resource, xLoc);
        }
      }
    } else if (viewName.equals( ViewConstants.RESOURCE_TRANSACTION_VIEW)) {
      ResourceTransactionView resourceTransactionView =
        ViewGenerics.getResourceTransactionView( viewFrame);
      if (partialPlan.getResource( idToFind) != null) {
        resourceTransactionView.findAndSelectResource( partialPlan.getResource( idToFind), xLoc);
      } else if (partialPlan.getResourceTransaction( idToFind) != null) {
        resourceTransactionView.findAndSelectResourceTransaction
          ( partialPlan.getResourceTransaction( idToFind));
      }
    } else if (viewName.equals( ViewConstants.TEMPORAL_EXTENT_VIEW)) {
      TemporalExtentView temporalExtentView = ViewGenerics.getTemporalExtentView( viewFrame);
      temporalExtentView.findAndSelectToken( partialPlan.getToken( idToFind), isByKey);
    } else if (viewName.equals( ViewConstants.TIMELINE_VIEW)) {
      TimelineView timelineView = ViewGenerics.getTimelineView( viewFrame);
      if (partialPlan.getToken( idToFind) != null) {
        timelineView.findAndSelectToken( partialPlan.getToken( idToFind), isByKey);
      } else if (partialPlan.getTimeline( idToFind) != null)
        timelineView.findAndSelectTimeline( partialPlan.getTimeline( idToFind));
    } else if (viewName.equals( ViewConstants.TOKEN_NETWORK_VIEW)) {
      TokenNetworkView tokenNetworkView= ViewGenerics.getTokenNetworkView( viewFrame);
      tokenNetworkView.findAndSelectNode( idToFind, isByKey);
    } else {
      System.err.println( " CreatePartialPlanViewThread.findIdInFrame viewName " +
                          viewName + " not handled");
      System.exit( -1);
    }
  } // end findIdInView


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
   * <code>createOpenViewFindItem</code>
   *
   * @param viewName - <code>String</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param idToFind - <code>Integer</code> - 
   * @return - <code>PartialPlanViewMenuItem</code> - 
   */
  public PartialPlanViewMenuItem createOpenViewFindItem( String viewName, String partialPlanName,
                                                         PwPlanningSequence planSequence,
                                                         ViewListener viewListener,
                                                         ViewSet viewSet, Integer idToFind) {
    boolean viewExists = false;
    if (viewSet != null) {
//       System.err.println( "createOpenViewFindItem: viewName " + viewName + " exists " +
//                           viewSet.doesViewFrameExist( viewName));
      viewExists = viewSet.doesViewFrameExist( viewName);
      viewListenerWait01 = null;
      if (! viewExists) {
        viewListenerWait01 = new ViewListenerWait01( this);
      }
    }
    PartialPlanViewMenuItem viewItem = new PartialPlanViewMenuItem("Open " + viewName,
                                                                   planSequence.getUrl(),
                                                                   planSequence.getName(),
                                                                   partialPlanName,
                                                                   viewListenerWait01);
    PwPartialPlan partialPlan = null;
    try {
      partialPlan = planSequence.getPartialPlan( partialPlanName);
    } catch (ResourceNotFoundException rnfExcep) {
      System.err.println( rnfExcep);
      rnfExcep.printStackTrace();
      return null;
    } catch (CreatePartialPlanException cppExcep) {
      return null;
    }

    viewItem.addActionListener( new CreateOpenViewListener( this, viewName, partialPlan,
                                                            viewSet, idToFind));
    return viewItem;
  } // end createOpenViewFindItem


  /**
   * <code>createPartialPlanViewThread</code>
   *
   * @param viewName - <code>String</code> - 
   * @param menuItem - <code>PartialPlanViewMenuItem</code> - 
   */
  public void createPartialPlanViewThread( final String viewName,
                                           final PartialPlanViewMenuItem menuItem) {
    final CreateViewThread thread = new CreatePartialPlanViewThread(viewName, menuItem);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  } // end createPartialPlanViewThread

  public void createFindIdInViewThread( Integer idToFind, String viewName,
                                        PwPartialPlan partialPlan, ViewSet viewSet,
                                        boolean viewExists) {
    Thread thread =
      new CreateFindIdInViewThread( idToFind, viewName, partialPlan, viewSet, viewExists);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  } // end createFindIdInViewThread


  public class CreateFindIdInViewThread extends Thread {

    private Integer idToFind;
    private String viewName;
    private PwPartialPlan partialPlan;
    private ViewSet viewSet;
    private boolean viewExists;

    public CreateFindIdInViewThread( Integer idToFind, String viewName, PwPartialPlan partialPlan,
                                     ViewSet viewSet, boolean viewExists) {
      this.idToFind = idToFind;
      this.viewName = viewName;
      this.partialPlan = partialPlan;
      this.viewSet = viewSet;
      this.viewExists = viewExists;
    }
      
    public final void run() {
      findIdInView( idToFind, viewName, partialPlan, viewSet, viewExists);
    } // end run

  } // end class CreateFindIdInViewThread



} // end PartialPlanViewMenu


