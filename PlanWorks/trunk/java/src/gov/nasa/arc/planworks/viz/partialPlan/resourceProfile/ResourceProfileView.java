// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ResourceProfileView.java,v 1.15 2004-03-23 18:23:41 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26Jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.resourceProfile;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;    
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import gov.nasa.arc.planworks.PlanWorks;
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

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.ResourceNameNode;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.partialPlan.ResourceView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>ResourceProfileView</code> - render the profiles of a
 *                partial plan's resources
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ResourceProfileView extends ResourceView  {

  private List resourceProfileList; // element ResourceProfile

  /**
   * <code>ResourceProfileView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   */
  public ResourceProfileView( final ViewableObject partialPlan, final ViewSet vSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet);
  } // end constructor


  /**
   * <code>ResourceProfileView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param state - <code>PartialPlanViewState</code> - 
   */
  public ResourceProfileView( final ViewableObject partialPlan, final ViewSet vSet, 
                              final PartialPlanViewState state) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet, state);
  }

  /**
   * <code>getState</code>
   *
   * @return - <code>PartialPlanViewState</code> - 
   */
  public final PartialPlanViewState getState() {
    return new ResourceProfileViewState( this);
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
    ResourceProfileViewState resourceState = (ResourceProfileViewState) state;
//     isShowLabels = state.showingLabels();
//     temporalDisplayMode = state.displayMode();
  }

  /**
   * <code>getResourceProfileList</code>
   *
   * @return - <code>List</code> - of ResourceProfile
   */
  public final List getResourceProfileList() {
    return resourceProfileList;
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

  /**
   * <code>computeMaxResourceLabelWidth</code>
   *
   * @return - <code>int</code> - 
   */
  protected final int computeMaxResourceLabelWidth() {
    boolean isNamesOnly = true;
    int maxWidth = ViewConstants.JGO_SCROLL_BAR_WIDTH * 2;
    List resourceList = partialPlan.getResourceList(); //createDummyData( isNamesOnly);
    Iterator resourceItr = resourceList.iterator();
    while (resourceItr.hasNext()) {
      PwResource resource = (PwResource) resourceItr.next();
      // System.err.println( "resource " + resource.getName());
      int width = ResourceProfile.getNodeLabelWidth( resource.getName(), this);
      // System.err.println( "  labelWidth " + width);
      if (width > maxWidth) {
        maxWidth = width;
      }
      int tickLabelMaxWidth =
        ResourceProfile.getTickLabelMaxWidth( resource, levelScaleFontMetrics);
      // System.err.println( "  tickLabelMaxWidth " + tickLabelMaxWidth);
      if (tickLabelMaxWidth > maxWidth) {
        maxWidth = tickLabelMaxWidth;
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
    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    if (resourceProfileList != null) {
      resourceProfileList.clear();
    }
    resourceProfileList = new UniqueSet();
    createResourceProfiles();
    // boolean showDialog = true;
    // isContentSpecRendered( PlanWorks.RESOURCE_PROFILE_VIEW, showDialog);

  } // end renderResourceExtent

  private void createResourceProfiles() {
    boolean isNamesOnly = false;
    currentYLoc = 0;
    List resourceList = partialPlan.getResourceList();
    Iterator resourceItr = resourceList.iterator();
    while (resourceItr.hasNext()) {
      PwResource resource = (PwResource) resourceItr.next();
      ResourceProfile resourceProfile =
        new ResourceProfile( resource, ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR),
                             levelScaleFontMetrics, this);
      // System.err.println( "resourceProfile " + resourceProfile);
      this.getJGoExtentDocument().addObjectAtTail( resourceProfile);
      resourceProfileList.add( resourceProfile);
    }

  } // end createResourceProfiles

  /**
   * <code>findNearestResource</code>
   *
   * @param dCoords - <code>Point</code> - 
   * @return - <code>PwResource</code> - 
   */
  protected final PwResource findNearestResource( final Point dCoords) {
    int docY = (int) dCoords.getY();
    PwResource resourceCandidate = null;
    Iterator reourceProfileItr = resourceProfileList.iterator();
    while (reourceProfileItr.hasNext()) {
      ResourceProfile resourceProfile = (ResourceProfile) reourceProfileItr.next();
      if (docY >= resourceProfile.getProfileYOrigin()) {
        resourceCandidate = resourceProfile.getResource();
      } else {
        break;
      }
    }
    return resourceCandidate;
  } // end findNearestResourceProfile


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
                         PlanWorks.RESOURCE_PROFILE_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
    createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);

    String timeMarkTitle = "Set Time Scale Line";
    if (doesViewFrameExist( PlanWorks.RESOURCE_TRANSACTION_VIEW)) {
      timeMarkTitle = timeMarkTitle.concat( "/Snap to Resource Transactions");
    }
    JMenuItem timeMarkItem = new JMenuItem( timeMarkTitle);
    createTimeMarkItem( timeMarkItem, resource);
    mouseRightPopup.add( timeMarkItem);

    if (((PartialPlanViewSet) this.getViewSet()).getActiveResource() != null) {
      JMenuItem activeResourceItem = new JMenuItem( "Snap to Active Resource");
      createActiveResourceItem( activeResourceItem);
      mouseRightPopup.add( activeResourceItem);
    }

    if (((PartialPlanViewSet) this.getViewSet()).getActiveToken() != null) {
      JMenuItem activeResourceByTransItem =
        new JMenuItem( "Snap to Active Resource by Transaction");
      createActiveResourceByTransItem( activeResourceByTransItem);
      mouseRightPopup.add( activeResourceByTransItem);
    }

    if (doesViewFrameExist( PlanWorks.NAVIGATOR_VIEW)) {
      mouseRightPopup.addSeparator();
      JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
      createCloseNavigatorWindowsItem( closeWindowsItem);
      mouseRightPopup.add( closeWindowsItem);
    }
    createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu


  private void createActiveResourceByTransItem( final JMenuItem activeResourceByTransItem) {
    activeResourceByTransItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) ResourceProfileView.this.getViewSet()).getActiveToken();
          if ((activeToken != null) && (activeToken instanceof PwResourceTransaction)) {
            int xLoc = 0;
            PwResource activeResource =
              ResourceProfileView.this.findActiveResourceByTrans
              ( (PwResourceTransaction) activeToken);
            if (activeResource != null) {
              findAndSelectResource ( activeResource, xLoc);
            }
          } else {
            JOptionPane.showMessageDialog
              ( PlanWorks.getPlanWorks(), "Active token is not a resource transaction",
              "Active Resource Not Found", JOptionPane.INFORMATION_MESSAGE);
          }
        }
      });
  } // end createActiveResourceItem

  private PwResource findActiveResourceByTrans( final PwResourceTransaction activeResourceTransaction) {
    Iterator resourceItr = partialPlan.getResourceList().iterator();
    while (resourceItr.hasNext()) {
      PwResource resource = (PwResource) resourceItr.next();
      Iterator transSetItr = resource.getTransactionSet().iterator();
      while (transSetItr.hasNext()) {
        PwResourceTransaction transaction = (PwResourceTransaction) transSetItr.next();
        if (transaction.getId().equals( activeResourceTransaction.getId())) {
          return resource;
        }
      }
    }
    return null;
  } // end findActiveResourceByTrans

  private void createActiveResourceItem( final JMenuItem activeResourceItem) {
    activeResourceItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          PwResource activeResource = 
            ((PartialPlanViewSet) ResourceProfileView.this.getViewSet()).getActiveResource();
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
            new AskNodeByKey( "Find by Key", "key (int)", ResourceProfileView.this);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());
            PwResource resourceToFind = partialPlan.getResource( nodeKey);
            if (resourceToFind != null) {
              int xLoc = 0;
              findAndSelectResource( resourceToFind, xLoc);
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
    Iterator resourceSetListItr = resourceProfileList.iterator();
    while (resourceSetListItr.hasNext()) {
      ResourceProfile resourceProfile =
        (ResourceProfile) resourceSetListItr.next();
//       System.err.println( "resourceToFind id = " + resourceToFind.getId() +
//                           " resource id = " + resourceProfile.getResource().getId());
      if (resourceProfile.getResource().getId().equals( resourceToFind.getId())) {
        System.err.println( "ResourceProfileView found resource: " +
                            resourceToFind.getName() + 
                            " (key=" + resourceToFind.getId().toString() + ")");
        isResourceFound = true;
        this.getJGoLevelScaleViewSelection().clearSelection();
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
                                     "Resource Not Found in ResourceProfileView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectResource


  private int findResourceYLoc( final PwResource resourceToFind) {
    int yLoc = 0;
    Iterator resourceProfileItr = resourceProfileList.iterator();
    while (resourceProfileItr.hasNext()) {
      ResourceProfile resourceProfile =
        (ResourceProfile) resourceProfileItr.next();
      if (resourceToFind.getId().equals( resourceProfile.getResource().getId())) {
        return resourceProfile.getProfileYOrigin();
      }
    }
    return yLoc;
  } // end findResourceYLoc


  private void createTimeMarkItem( final JMenuItem timeMarkItem,
                                   final  PwResource resourceToFind) {
    timeMarkItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          int xLoc = (int) ResourceProfileView.docCoords.getX();
//           System.err.println( "doMouseClick: xLoc " + xLoc + " time " +
//                               jGoRulerView.scaleXLoc( xLoc));
          createTimeMark( xLoc);

          // draw mark in ResourceTransactionView & scroll to same resource
          if (doesViewFrameExist( PlanWorks.RESOURCE_TRANSACTION_VIEW)) {
            MDIInternalFrame resourceTransFrame =
              viewSet.openView( PlanWorks.getViewClassName
                                ( PlanWorks.RESOURCE_TRANSACTION_VIEW));
            ResourceTransactionView resourceTransactionView = null;
            Container contentPane = resourceTransFrame.getContentPane();
            for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
              // System.err.println( "i " + i + " " +
              //                    contentPane.getComponent( i).getClass().getName());
              if (contentPane.getComponent( i) instanceof ResourceTransactionView) {
                resourceTransactionView =
                  (ResourceTransactionView) contentPane.getComponent( i);
              }
            }
            if (resourceTransactionView != null) {
              resourceTransactionView.createTimeMark( xLoc);
              // scroll ResourceTransactionView to resourceToFind and timeMark
              resourceTransactionView.findAndSelectResource( resourceToFind, xLoc);
            }
          }
        }
      });
  } // end createTimeMarkItem


  private void createOverviewWindowItem( final JMenuItem overviewWindowItem,
                                         final ResourceProfileView resourceProfileView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public final void actionPerformed( final ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( PlanWorks.RESOURCE_PROFILE_VIEW, partialPlan,
                                            resourceProfileView, viewSet, jGoExtentView,
                                            viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem

  /**
   * <code>createTimeMark</code> -- allow ResourceTransaction to set same time mark here
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

    

} // end class ResourceProfileView
 



