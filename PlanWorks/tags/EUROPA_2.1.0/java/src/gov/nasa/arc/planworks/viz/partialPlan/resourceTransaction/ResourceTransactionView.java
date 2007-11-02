// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ResourceTransactionView.java,v 1.30 2005-06-01 17:19:11 pdaley Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 04feb04
//

package gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;    
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwToken;
// testing
// import gov.nasa.arc.planworks.db.PwIntervalDomain;
// import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
// import gov.nasa.arc.planworks.db.impl.PwResourceImpl;
// import gov.nasa.arc.planworks.db.impl.PwResourceInstantImpl;
// import gov.nasa.arc.planworks.db.impl.PwResourceTransactionImpl;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.ResourceNameNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.partialPlan.ResourceView;
import gov.nasa.arc.planworks.viz.partialPlan.TimeScaleView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.util.AskNodeByKey;
import gov.nasa.arc.planworks.viz.util.ProgressMonitorThread;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>ResourceTransactionView</code> - render the transactions of a
 *                partial plan's resources
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ResourceTransactionView extends ResourceView  {

  private static final int SLEEP_FOR_50MS = 50;

  private List resourceTransactionSetList; // element ResourceTransactionSet
  private ProgressMonitorThread progressMonThread;

  /**
   * <code>ResourceTransactionView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   */
  public ResourceTransactionView( final ViewableObject partialPlan, final ViewSet vSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet,
           ViewConstants.RESOURCE_TRANSACTION_VIEW);
  } // end constructor


  /**
   * <code>ResourceTransactionView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param state - <code>PartialPlanViewState</code> - 
   */
  public ResourceTransactionView( final ViewableObject partialPlan, final ViewSet vSet, 
                                  final PartialPlanViewState state) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet, state,
           ViewConstants.RESOURCE_TRANSACTION_VIEW);
  }

  /**
   * <code>ResourceTransactionView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public ResourceTransactionView( final ViewableObject partialPlan, final ViewSet vSet,
                                  final ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet, viewListener,
           ViewConstants.RESOURCE_TRANSACTION_VIEW);
  } // end constructor

  /**
   * <code>ResourceTransactionView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param state - <code>PartialPlanViewState</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public ResourceTransactionView( final ViewableObject partialPlan, final ViewSet vSet, 
                                  final PartialPlanViewState state,
                                  final ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet, state, viewListener,
           ViewConstants.RESOURCE_TRANSACTION_VIEW);
  }

  /**
   * <code>getState</code>
   *
   * @return - <code>PartialPlanViewState</code> - 
   */
  public final PartialPlanViewState getState() {
    return new ResourceTransactionViewState( this);
  }

//   public boolean showLabels(){return isShowLabels;}
//   public int displayMode(){return temporalDisplayMode;}

  /**
   * <code>setState</code>
   *
   * @param state - <code>PartialPlanViewState</code> - 
   */
  public final void setState( final PartialPlanViewState state) {
    super.setState( state);
    ResourceTransactionViewState resourceState = (ResourceTransactionViewState) state;
//     isShowLabels = state.showingLabels();
//     temporalDisplayMode = state.displayMode();
  }

  /**
   * <code>createTimeScaleView</code>
   *
   */
  protected final void createTimeScaleView() {
    boolean doFreeTokens = false;
    boolean isTimelineView = false;
    this.getJGoRulerView().collectAndComputeTimeScaleMetrics( isTimelineView, doFreeTokens, this);
    initialTimeScaleEnd = getTimeScaleEnd();
    this.getJGoRulerView().createTimeScale( isTimelineView);
  } // end createTimeScaleView

  /**
   * <code>computeMaxResourceLabelWidth</code>
   *
   * @return - <code>int</code> - 
   */
  protected final int computeMaxResourceLabelWidth() {
    boolean isNamesOnly = true;
    int maxWidth = ViewConstants.JGO_SCROLL_BAR_WIDTH * 2;
    // resourceList will come from partialPlan
    List resourceList = partialPlan.getResourceList(); //createDummyData( isNamesOnly);
    Iterator resourceItr = resourceList.iterator();
    while (resourceItr.hasNext()) {
      PwResource resource = (PwResource) resourceItr.next();
      int width = ResourceTransactionSet.getNodeLabelWidth( resource.getName(), this);
      if (width > maxWidth) {
        maxWidth = width;
      }
    }
    return maxWidth;
  } // end computeMaxResourceLabelWidth

  /**
   * <code>renderResourceExtent</code>
   *
   */
  protected final void renderResourceExtent() {
    this.getJGoExtentDocument().deleteContents();
    this.getJGoLevelScaleDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    if (resourceTransactionSetList != null) {
      resourceTransactionSetList.clear();
    }
    resourceTransactionSetList = new UniqueSet();

    createResourceTransactionSets();

    // boolean showDialog = true;
    // isContentSpecRendered( PlanWorks.RESOURCE_TRANSACTION_VIEW, showDialog);

  } // end createResourceTransactionView

  /**
   * <code>getResourceTransactionSetList</code>
   *
   * @return - <code>List</code> - of ResourceTransactionSet
   */
  public final List getResourceTransactionSetList() {
    return resourceTransactionSetList;
  }

  /**
   * <code>getCurrentYLoc</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getCurrentYLoc() {
    return currentYLoc;
  }

  /**
   * <code>setCurrentYLoc</code>
   *
   * @param yLoc - <code>int</code> - 
   */
  public final void setCurrentYLoc( int yLoc) {
    currentYLoc = yLoc;
  }

//   private List createDummyData( final boolean isNameOnly) {
//     int startTime = 0, endTime = 0;
//     if (! isNameOnly) {
//       startTime = getTimeScaleStart();
//       endTime = getTimeScaleEnd();
//     }
//     List resourceList = new ArrayList();
//     List resourceInstantList = new ArrayList();
//     PwIntervalDomain instantDomain =
//       (PwIntervalDomain) new PwIntervalDomainImpl ( "type", "10", "10");
//     resourceInstantList.add
//       ( new PwResourceInstantImpl( new Integer( 99011), instantDomain, 4., 6.));
//     resourceInstantList.add
//       ( new PwResourceInstantImpl( new Integer( 99012),
//                                    new PwIntervalDomainImpl( "type", "20", "20"), 10., 14.));
//     resourceInstantList.add
//       ( new PwResourceInstantImpl( new Integer( 99013),
//                                    new PwIntervalDomainImpl( "type", "30", "30"), -2., 0.));
//     resourceInstantList.add
//       ( new PwResourceInstantImpl( new Integer( 99014),
//                                    new PwIntervalDomainImpl( "type", "50", "50"), 2., 4.));

//     UniqueSet transactionSet = new UniqueSet();
//     transactionSet.add
//       ( new PwResourceTransactionImpl( new Integer( 990111),
//                                        new PwIntervalDomainImpl( "type", "10", "50"), 2., 2.));
//     transactionSet.add
//       ( new PwResourceTransactionImpl( new Integer( 990112),
//                                        new PwIntervalDomainImpl( "type", "20", "50"), 6., 6.));
//     transactionSet.add
//       ( new PwResourceTransactionImpl( new Integer( 990113),
//                                        new PwIntervalDomainImpl( "type", "30", "50"), -8., -8.));

//     PwResource dummyResource =
//       new PwResourceImpl( new Integer( 9901), "Resource1", 4., 0., 12., startTime, endTime,
//                           transactionSet, resourceInstantList);
//     resourceList.add( dummyResource);

//     transactionSet = new UniqueSet();
//     transactionSet.add
//       ( new PwResourceTransactionImpl( new Integer( 990111),
//                                        new PwIntervalDomainImpl( "type", "10", "50"), 2., 2.));
//     transactionSet.add
//       ( new PwResourceTransactionImpl( new Integer( 990112),
//                                        new PwIntervalDomainImpl( "type", "20", "50"), 6., 6.));
//     transactionSet.add
//       ( new PwResourceTransactionImpl( new Integer( 990113),
//                                        new PwIntervalDomainImpl( "type", "30", "50"), -8., -8.));
//     transactionSet.add
//       ( new PwResourceTransactionImpl( new Integer( 990114),
//                                        new PwIntervalDomainImpl( "type", "40", "50"), 4., 4.));
//     transactionSet.add
//       ( new PwResourceTransactionImpl( new Integer( 990115),
//                                        new PwIntervalDomainImpl( "type", "50", "60"), -4., -4.));
        
//     dummyResource =
//       new PwResourceImpl( new Integer( 9902), "Resource2", 4., 0., 12., startTime, endTime,
//                           transactionSet, resourceInstantList);
//     resourceList.add( dummyResource);
//     dummyResource =
//       new PwResourceImpl( new Integer( 9903), "ResourceThree", 4., 0., 12., startTime, endTime,
//                           transactionSet, resourceInstantList);
//     resourceList.add( dummyResource);
//     dummyResource =
//       new PwResourceImpl( new Integer( 9904), "ResourceFour", 4., 0., 12., startTime, endTime,
//                           transactionSet, resourceInstantList);
//     resourceList.add( dummyResource);
//     dummyResource =
//       new PwResourceImpl( new Integer( 9905), "ResourceFive", 4., 0., 12., startTime, endTime,
//                           transactionSet, resourceInstantList);
//     resourceList.add( dummyResource);
//     return resourceList;
//   } // end createDummyData


  private void createResourceTransactionSets() {
    boolean isNamesOnly = false;
    currentYLoc = 0;
    List resourceList = partialPlan.getResourceList(); //createDummyData( isNamesOnly);
    progressMonThread =
      createProgressMonitorThread( "Rendering Resource Transaction View ...", 0, resourceList.size(),
			     Thread.currentThread(), this);
    if (! progressMonitorWait( progressMonThread, this)) {
      closeView( this);
      return;
    }
    int horizonEnd = getTimeScaleEnd();
    int numResources = 0;
    Iterator resourceItr = resourceList.iterator();
    while (resourceItr.hasNext()) {
      PwResource resource = (PwResource) resourceItr.next();
      ResourceTransactionSet resourceTransactionSet =
        new ResourceTransactionSet( resource, horizonEnd,
                                    ViewConstants.VIEW_BACKGROUND_COLOR, this);
      // System.err.println( "resourceTransactionSet " + resourceTransactionSet);
      this.getJGoExtentDocument().addObjectAtTail( resourceTransactionSet);
      resourceTransactionSetList.add( resourceTransactionSet);
      if (progressMonThread.getProgressMonitor().isCanceled()) {
        String msg = "User Canceled Resource Transaction View Rendering";
        System.err.println( msg);
	progressMonThread.setProgressMonitorCancel();
        closeView( this);
        return;
      }
      numResources++;
      progressMonThread.getProgressMonitor().setProgress( numResources *
							  ViewConstants.MONITOR_MIN_MAX_SCALING);
    }
    progressMonThread.setProgressMonitorCancel();
  } // end createResourceTransactionSets


//   private void iterateOverNodes() {
//     int numResourceTransactionSets = resourceTransactionSetList.size();
//     //System.err.println( "iterateOverNodes: numResourceTransactionSets " + numResourceTransactionSets);
//     Iterator resourceIterator = resourceTransactionSetList.iterator();
//     while (resourceIterator.hasNext()) {
//       ResourceTransactionSet resourceTransactionSet = (ResourceTransactionSet) resourceIterator.next();
//       System.err.println( "name '" + resourceTransactionSet.getPredicateName() + "' location " +
//                           resourceTransactionSet.getLocation());
//     }
//   } // end iterateOverNodes


//   private void iterateOverJGoDocument() {
//     JGoListPosition position = jGoExtentView.getDocument().getFirstObjectPos();
//     int cnt = 0;
//     while (position != null) {
//       JGoObject object = jGoExtentView.getDocument().getObjectAtPos( position);
//       position = jGoExtentView.getDocument().getNextObjectPosAtTop( position);
//       //System.err.println( "iterateOverJGoDoc: position " + position +
//       //                   " className " + object.getClass().getName());
//       if (object instanceof ResourceTransactionSet) {
//         ResourceTransactionSet resourceTransactionSet = (ResourceTransactionSet) object;

//       }
//       cnt += 1;
// //       if (cnt > 100) {
// //         break;
// //       }
//     }
//     //System.err.println( "iterateOverJGoDoc: cnt " + cnt);
//   } // end iterateOverJGoDocument

  /**
   * <code>findNearestResource</code>
   *
   * @param dCoords - <code>Point</code> - 
   * @return - <code>PwResource</code> - 
   */
  protected final PwResource findNearestResource( final Point dCoords) {
    int docY = (int) dCoords.getY();
    PwResource resourceCandidate = null;
    Iterator reourceTransSetItr = resourceTransactionSetList.iterator();
    while (reourceTransSetItr.hasNext()) {
      ResourceTransactionSet resourceTransSet =
        (ResourceTransactionSet) reourceTransSetItr.next();
      if (docY >= resourceTransSet.getTransactionSetYOrigin()) {
        resourceCandidate = resourceTransSet.getResource();
      } else {
        break;
      }
    }
    return resourceCandidate;
  } // end findNearestResource


  /**
   * <code>mouseRightPopupMenu</code>
   *
   * @param resource - <code>PwResource</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  protected final void mouseRightPopupMenu( final PwResource resource, final Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
    createNodeByKeyItem( nodeByKeyItem);
    mouseRightPopup.add( nodeByKeyItem);

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         viewListenerList, ViewConstants.RESOURCE_TRANSACTION_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
    createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);

    if (resource != null) {
      String timeMarkTitle = "Set Time Scale Line";
      if (viewSet.doesViewFrameExist( ViewConstants.RESOURCE_TRANSACTION_VIEW)) {
        timeMarkTitle = timeMarkTitle.concat( "/Snap to Resource Profile");
      }
      JMenuItem timeMarkItem = new JMenuItem( timeMarkTitle);
      createTimeMarkItem( timeMarkItem, resource);
      mouseRightPopup.add( timeMarkItem);
    }

    if (((PartialPlanViewSet) this.getViewSet()).getActiveResource() != null) {
      JMenuItem activeResourceItem = new JMenuItem( "Snap to Active Resource");
      createActiveResourceItem( activeResourceItem);
      mouseRightPopup.add( activeResourceItem);
    }

    if (((PartialPlanViewSet) this.getViewSet()).getActiveToken() != null) {
      JMenuItem activeResourceTransItem =
        new JMenuItem( "Snap to Active Resource Transaction");
      createActiveResourceTransItem( activeResourceTransItem);
      mouseRightPopup.add( activeResourceTransItem);
    }

    if (viewSet.doesViewFrameExist( ViewConstants.NAVIGATOR_VIEW)) {
      mouseRightPopup.addSeparator();
      JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
      createCloseNavigatorWindowsItem( closeWindowsItem);
      mouseRightPopup.add( closeWindowsItem);
    }
    createAllViewItems( partialPlan, partialPlanName, planSequence, viewListenerList,
                        mouseRightPopup);

    createStepAllViewItems( partialPlan, mouseRightPopup);

    ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createActiveResourceTransItem( final JMenuItem activeResourceTransItem) {
    activeResourceTransItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) ResourceTransactionView.this.getViewSet()).getActiveToken();
          if ((activeToken != null) && (activeToken instanceof PwResourceTransaction)) {
              findAndSelectResourceTransaction( (PwResourceTransaction) activeToken);
          } else {
            JOptionPane.showMessageDialog
              ( PlanWorks.getPlanWorks(), "Active token is not a resource transaction",
              "Active Resource Transaction Not Found", JOptionPane.INFORMATION_MESSAGE);
          }
        }
      });
  } // end createActiveResourceTransItem


  private void createActiveResourceItem( final JMenuItem activeResourceItem) {
    activeResourceItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          PwResource activeResource = 
            ((PartialPlanViewSet) ResourceTransactionView.this.getViewSet()).getActiveResource();
          if (activeResource != null) {
            int xLoc = 0;
            findAndSelectResource ( activeResource, xLoc);
          }
        }
      });
  } // end createActiveResourceItem

  private void createNodeByKeyItem( final JMenuItem nodeByKeyItem) {
    nodeByKeyItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          AskNodeByKey nodeByKeyDialog =
            new AskNodeByKey( "Find by Key", "key (int)", ResourceTransactionView.this);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());
            PwResource resourceToFind = partialPlan.getResource( nodeKey);
            if (resourceToFind != null) {
              int xLoc = 0;
              findAndSelectResource( resourceToFind, xLoc);
            } else {
              PwResourceTransaction resourceTransactionToFind =
                partialPlan.getResourceTransaction( nodeKey);
              if (resourceTransactionToFind != null) {
                findAndSelectResourceTransaction( resourceTransactionToFind);
              }
            }
          }
        }
      });
  } // end createNodeByKeyItem


  /**
   * <code>findAndSelectResource</code>
   *
   * @param resourceToFind - <code>PwResource</code> - 
   * @param xLoc - <code>int</code> - 
   */
  public final void findAndSelectResource( final PwResource resourceToFind,
                                           final int xLoc) {
    boolean isResourceFound = false;
    Iterator resourceSetListItr = resourceTransactionSetList.iterator();
    while (resourceSetListItr.hasNext()) {
      ResourceTransactionSet resourceTransactionSet =
        (ResourceTransactionSet) resourceSetListItr.next();
//       System.err.println( "resourceToFind id = " + resourceToFind.getId() +
//                           " resource id = " + resourceTransactionSet.getResource().getId());
      if (resourceTransactionSet.getResource().getId().equals( resourceToFind.getId())) {
        System.err.println( "ResourceTransactionView found resource: " +
                            resourceToFind.getName() + 
                            " (key=" + resourceToFind.getId().toString() + ")");
        isResourceFound = true;
        this.getJGoLevelScaleViewSelection().clearSelection();
        this.getJGoExtentViewSelection().clearSelection();
        this.getJGoExtentViewHScrollBar().
          setValue( Math.max( 0, (int) (xLoc - (this.getJGoExtentViewSize().getWidth() / 2))));
        this.getJGoExtentViewVScrollBar().
          setValue( Math.max( 0, (int) (findResourceYLoc( resourceToFind) -
                                   (this.getJGoExtentViewSize().getHeight() / 2))));
        Iterator nameNodeItr = resourceNameNodeList.iterator();
        while (nameNodeItr.hasNext()) {
          ResourceNameNode nameNode = (ResourceNameNode) nameNodeItr.next();
          if (nameNode.getResource().getId().equals( resourceToFind.getId())) {
            this.getJGoLevelScaleViewSelection().extendSelection( nameNode);
            break;
          }
        }
      }
    }
    if (! isResourceFound) {
      // Content Spec filtering may cause this to happen
      String message = "Resource '" +  resourceToFind.getName() +
        "' (key=" + resourceToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Resource Not Found in ResourceTransactionView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectResource


  private int findResourceYLoc( final PwResource resourceToFind) {
    int yLoc = 0;
    Iterator resourceTransSetItr = resourceTransactionSetList.iterator();
    while (resourceTransSetItr.hasNext()) {
      ResourceTransactionSet resourceTransSet =
        (ResourceTransactionSet) resourceTransSetItr.next();
      if (resourceToFind.getId().equals( resourceTransSet.getResource().getId())) {
        return resourceTransSet.getTransactionSetYOrigin();
      }
    }
    return yLoc;
  } // end findResourceYLoc


  /**
   * <code>findAndSelectResourceTransaction</code>
   *
   * @param resourceTransToFind - <code>PwResourceTransaction</code> - 
   */
  public final void findAndSelectResourceTransaction ( final PwResourceTransaction
                                                       resourceTransToFind) {
    boolean isResourceTransFound = false;
    if ((resourceTransToFind.getParentId() != null) &&
        (! resourceTransToFind.getParentId().equals( DbConstants.NO_ID))) {
      PwResource resourceToFind =
        (PwResource) partialPlan.getObject( resourceTransToFind.getParentId());
//       System.err.println( "findAndSelectResourceTransaction resourceToFind " +
//                           resourceToFind);
      Iterator resourceSetListItr = resourceTransactionSetList.iterator();
      foundIt:
      while (resourceSetListItr.hasNext()) {
        ResourceTransactionSet resourceTransactionSet =
          (ResourceTransactionSet) resourceSetListItr.next();
        if (resourceTransactionSet.getResource().getId().equals( resourceToFind.getId())) {
          Iterator transNodeItr =
            resourceTransactionSet.getTransactionNodeList().iterator();
          while (transNodeItr.hasNext()) {
            ResourceTransactionNode transNode = (ResourceTransactionNode) transNodeItr.next();
            if (transNode.getTransaction().getId().equals( resourceTransToFind.getId())) {
              System.err.println( "ResourceTransactionView found resourceTransaction: " +
                                  resourceTransToFind.getName() + 
                                  " (key=" + resourceTransToFind.getId().toString() + ")");
              isResourceTransFound = true;
              this.getJGoLevelScaleViewSelection().clearSelection();
              this.getJGoExtentViewSelection().clearSelection();
              this.getJGoExtentViewHScrollBar().
                setValue( Math.max( 0, (int) (transNode.getLocation().getX() -
                                              (this.getJGoExtentViewSize().getWidth() / 2))));
              this.getJGoExtentViewVScrollBar().
                setValue( Math.max( 0, (int) (transNode.getLocation().getY() -
                                              (this.getJGoExtentViewSize().getHeight() / 2))));
              this.getJGoExtentViewSelection().extendSelection( transNode);
              break foundIt;
            }
          }
        }
      }
      if (! isResourceTransFound) {
        // Content Spec filtering may cause this to happen
        String message = "Resource Transaction'" +  resourceTransToFind.getName() +
          "' (key=" + resourceTransToFind.getId().toString() + ") not found.";
        JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                       "Resource TransactionNot Found in ResourceTransactionView",
                                       JOptionPane.ERROR_MESSAGE);
        System.err.println( message);
      }
    }
  } // end findAndSelectResourceTransaction


  private void createTimeMarkItem( final JMenuItem timeMarkItem,
                                   final  PwResource resourceToFind) {
    timeMarkItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          int xLoc = (int) ResourceTransactionView.docCoords.getX();
//           System.err.println( "doMouseClick: xLoc " + xLoc + " time " +
//                               jGoRulerView.scaleXLoc( xLoc));
          createTimeMark( xLoc);

          // draw mark in ResourceProfileView & scroll to same resource
          if (viewSet.doesViewFrameExist( ViewConstants.RESOURCE_PROFILE_VIEW)) {
            ViewListener viewListener = null;
           MDIInternalFrame resourceProfileFrame =
              viewSet.openView( PlanWorks. getViewClassName
                                ( ViewConstants.RESOURCE_PROFILE_VIEW), viewListener);
            ResourceProfileView resourceProfileView = null;
            Container contentPane = resourceProfileFrame.getContentPane();
            for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
              // System.err.println( "i " + i + " " +
              //                    contentPane.getComponent( i).getClass().getName());
              if (contentPane.getComponent( i) instanceof ResourceProfileView) {
                resourceProfileView =
                  (ResourceProfileView) contentPane.getComponent( i);
              }
            }
            if (resourceProfileView != null) {
              resourceProfileView.createTimeMark( xLoc);
              // scroll ResourceProfileView to resourceToFind and timeMark
              resourceProfileView.findAndSelectResource( resourceToFind, xLoc);
            }
          }
        }
      });
  } // end createTimeMarkItem


  private void createOverviewWindowItem( final JMenuItem overviewWindowItem,
                                         final ResourceTransactionView resourceTransactionView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public final void actionPerformed( final ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( ViewConstants.RESOURCE_TRANSACTION_VIEW, partialPlan,
                                            resourceTransactionView, viewSet, jGoExtentView,
                                            viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem

  /**
   * <code>createTimeMark</code> -- allow ResourceProfile to set same time mark here
   *
   * @param xLoc - <code>int</code> - 
   */
  public final void createTimeMark( final int xLoc) {
    if (timeScaleMark != null) {
      this.getJGoExtentDocument().removeObject( timeScaleMark);
      // jGoExtentView.validate();
    } 
    timeScaleMark = new TimeScaleMark( xLoc);
    timeScaleMark.addPoint( xLoc, 0);
    timeScaleMark.addPoint( xLoc, currentYLoc + 2);
    this.getJGoExtentDocument().addObjectAtTail( timeScaleMark);
  } // end createTimeMark


    

} // end class ResourceTransactionView
 



