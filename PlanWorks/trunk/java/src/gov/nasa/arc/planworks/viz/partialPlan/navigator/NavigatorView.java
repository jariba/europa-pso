// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NavigatorView.java,v 1.20 2004-03-23 18:23:26 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 06jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.ResourceNameNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkObjectNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkResourceNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionNode;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.SlotNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineViewTimelineNode;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>NavigatorView</code> - render a partial plan's node network
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class NavigatorView extends PartialPlanView {

  private JGoObject initialNode;
  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private MDIInternalFrame navigatorFrame;
  private NavigatorJGoView jGoView;
  private JGoDocument jGoDocument;
  private boolean isLayoutNeeded;
  private ExtendedBasicNode focusNode;
  private boolean isDebugPrint;

  /**
   * variable <code>timelineColorMap</code>
   *
   */
  protected Map timelineColorMap;

  /**
   * variable <code>entityNavNodeMap</code>
   *
   */
  protected Map entityNavNodeMap; 

  /**
   * variable <code>navLinkMap</code>
   *
   */
  protected Map navLinkMap;

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param objectNode - <code>ConstraintNetworkObjectNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final ConstraintNetworkObjectNode objectNode,
                        final ViewableObject partialPlan, final ViewSet viewSet,
                        final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = objectNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param timelineNode - <code>TimelineViewTimelineNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final TimelineViewTimelineNode timelineNode,
                        final ViewableObject partialPlan, final ViewSet viewSet,
                        final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = timelineNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param timelineNode - <code>ConstraintNetworkTimelineNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final ConstraintNetworkTimelineNode timelineNode,
                        final ViewableObject partialPlan, final ViewSet viewSet,
                        final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = timelineNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param resourceNode - <code>ConstraintNetworkResourceNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final ConstraintNetworkResourceNode resourceNode,
                        final ViewableObject partialPlan, final ViewSet viewSet,
                        final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = resourceNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  public NavigatorView( final ResourceNameNode resourceNode,
                        final ViewableObject partialPlan, final ViewSet viewSet,
                        final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = resourceNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param slotNode - <code>SlotNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final SlotNode slotNode, final ViewableObject partialPlan,
                        final ViewSet viewSet, final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = slotNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param tokenNode - <code>TokenNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final TokenNode tokenNode, final ViewableObject partialPlan,
                        final ViewSet viewSet, final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = tokenNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  public NavigatorView( final ResourceTransactionNode resourceTransactionNode,
                        final ViewableObject partialPlan, final ViewSet viewSet,
                        final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = resourceTransactionNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param temporalNode - <code>TemporalNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final TemporalNode temporalNode, final ViewableObject partialPlan,
                        final ViewSet viewSet, final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = temporalNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param variableNode - <code>VariableNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final VariableNode variableNode, final ViewableObject partialPlan,
                        final ViewSet viewSet, final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = variableNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param constraintNode - <code>ConstraintNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final ConstraintNode constraintNode, final ViewableObject partialPlan,
                        final ViewSet viewSet, final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = constraintNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  private void commonConstructor() {
    System.err.println( "Render Navigator View ...");
    this.startTimeMSecs = System.currentTimeMillis();
    // isDebugPrint = true;
    isDebugPrint = false;
    //timelineColorMap = createTimelineColorMap();

    navLinkMap = new HashMap();
    entityNavNodeMap = new HashMap();

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new NavigatorJGoView( this);
    jGoView.addViewListener(createViewListener());
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);
    SwingUtilities.invokeLater( runInit);
  } // end commonConstructor

  Runnable runInit = new Runnable() {
      public final void run() {
        init();
      }
    };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo navigator,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use runInit in constructor
   */
  public final void init() {
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for NavigatorView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep( 50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "navigatorView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);

    jGoDocument = jGoView.getDocument();
    jGoDocument.addDocumentListener(createDocListener());
    renderInitialNode();

    NavigatorViewLayout layout = new NavigatorViewLayout( jGoDocument, startTimeMSecs);
    layout.performLayout();

    MDIInternalFrame contentFilterFrame = viewSet.getContentSpecWindow();
    int contentFilterMaxY = (int) (contentFilterFrame.getLocation().getY() +
                                   contentFilterFrame.getSize().getHeight());
    int delta = Math.min( ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4 *
                          ((PartialPlanViewSet) viewSet).getNavigatorFrameCnt(),
                          (int) (PlanWorks.getPlanWorks().getSize().getHeight() -
                                 contentFilterMaxY -
                                 (ViewConstants.MDI_FRAME_DECORATION_HEIGHT * 2)));
    navigatorFrame.setLocation
      ( ViewConstants.INTERNAL_FRAME_X_DELTA + delta, contentFilterMaxY + delta);

//     Rectangle documentBounds = jGoView.getDocument().computeBounds();
//     jGoView.getDocument().setDocumentSize( (int) documentBounds.getWidth() +
//                                            (ViewConstants.TIMELINE_VIEW_X_INIT * 2),
//                                            (int) documentBounds.getHeight() +
//                                            (ViewConstants.TIMELINE_VIEW_Y_INIT * 2));
    int maxViewWidth = (int) jGoView.getDocumentSize().getWidth();
    int maxViewHeight = (int) jGoView.getDocumentSize().getHeight();

//     navigatorFrame.setSize
//       ( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
//         maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);

    expandViewFrame( navigatorFrame, maxViewWidth, maxViewHeight);

    startTimeMSecs = 0L;
    isLayoutNeeded = false;
    focusNode = null;
    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *                       setVisible(true | false)
   *                       according to the Content Spec enabled ids
   *
   */
  public final void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority( Thread.MIN_PRIORITY);
    thread.start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public final void run() {
      redrawView();
    } //end run

  } // end class RedrawViewThread

  private void redrawView() {
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    System.err.println( "Redrawing Navigator View ...");
    if (startTimeMSecs == 0L) {
      startTimeMSecs = System.currentTimeMillis();
    }
    this.setVisible( false);

    // content spec apply/reset do not change layout, only TokenNode/
    // variableNode/constraintNode opening/closing
    setNodesLinksVisible();

    if (isLayoutNeeded) {
      NavigatorViewLayout layout = new NavigatorViewLayout( jGoDocument, startTimeMSecs);
      layout.performLayout();

      // do not highlight node, if it has been removed
      //       boolean isHighlightNode = ((focusNode instanceof ConstraintNetworkTokenNode) ||
      //                                  ((focusNode instanceof VariableNode) &&
      //                                   (((VariableNode) focusNode).inLayout())) ||
      //                                  ((focusNode instanceof ConstraintNode) &&
      //                                   (((ConstraintNode) focusNode).inLayout())));
      boolean isHighlightNode = true;
      NodeGenerics.focusViewOnNode( focusNode, isHighlightNode, jGoView);
      isLayoutNeeded = false;
    }
    startTimeMSecs = 0L;
    this.setVisible( true);
    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end createTemporalExtentView

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public final JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>setStartTimeMSecs</code>
   *
   * @param msecs - <code>long</code> - 
   */
  protected final void setStartTimeMSecs( final long msecs) {
    startTimeMSecs = msecs;
  }

  /**
   * <code>setLayoutNeeded</code>
   *
   */
  public final void setLayoutNeeded() {
    isLayoutNeeded = true;
  }

  /**
   * <code>isLayoutNeeded</code>> - 
   *
   * @return - <code>boolean</code> - 
   */
  public final boolean isLayoutNeeded() {
    return isLayoutNeeded;
  }

  /**
   * <code>getPartialPlan</code>
   *
   * @return - <code>PwPartialPlan</code> - 
   */
  public final PwPartialPlan getPartialPlan() {
    return partialPlan;
  }

  /**
   * <code>setFocusNode</code>
   *
   * @param node - <code>ExtendedBasicNode</code> - 
   */
  public final void setFocusNode( final ExtendedBasicNode node) {
    this.focusNode = node;
  }

  private void renderInitialNode() {
    ExtendedBasicNode node = null;
    if (initialNode instanceof ConstraintNetworkObjectNode) {
      PwObject object = ((ConstraintNetworkObjectNode) initialNode).getObject();
      node = addEntityNavNode( object, isDebugPrint);
    } else if ((initialNode instanceof ConstraintNetworkResourceNode) ||
               (initialNode instanceof ResourceNameNode)) {
      PwResource resource = null;
      if (initialNode instanceof ConstraintNetworkResourceNode) {
        resource = ((ConstraintNetworkResourceNode) initialNode).getResource();
      } else if (initialNode instanceof ResourceNameNode) {
        resource = ((ResourceNameNode) initialNode).getResource();
      }
      node = addEntityNavNode( resource , isDebugPrint);
    } else if ((initialNode instanceof TimelineViewTimelineNode) ||
               (initialNode instanceof ConstraintNetworkTimelineNode)) {
      PwTimeline timeline = null;
      if (initialNode instanceof TimelineViewTimelineNode) {
        timeline = ((TimelineViewTimelineNode) initialNode).getTimeline();
      } else if (initialNode instanceof ConstraintNetworkTimelineNode) {
        timeline = ((ConstraintNetworkTimelineNode) initialNode).getTimeline();
      }
      node = addEntityNavNode( timeline, isDebugPrint);
    } else if (initialNode instanceof SlotNode) {
      // TimelineView.SlotNode
      PwSlot slot = ((SlotNode) initialNode).getSlot();
      node = addEntityNavNode( slot, isDebugPrint);
    } else if ((initialNode instanceof TokenNode) ||
               (initialNode instanceof TemporalNode) ||
               (initialNode instanceof ResourceTransactionNode)) {
      PwToken token = null;
      if (initialNode instanceof TokenNode) {
        token = ((TokenNode) initialNode).getToken();
      } else if (initialNode instanceof TemporalNode) {
        token = ((TemporalNode) initialNode).getToken();
      } else if (initialNode instanceof ResourceTransactionNode) {
        token = ((ResourceTransactionNode) initialNode).getTransaction();
      }
      node = addEntityNavNode( token, isDebugPrint);
    } else if (initialNode instanceof VariableNode) {
      PwVariable variable = ((VariableNode) initialNode).getVariable();
      node = addEntityNavNode( variable, isDebugPrint);
    } else if (initialNode instanceof ConstraintNode) {
      PwConstraint constraint = ((ConstraintNode) initialNode).getConstraint();
      node = addEntityNavNode( constraint, isDebugPrint);
    } else {
      System.err.println( " NavigatorView.renderInitialNode: " + initialNode +
                          " not handled");
      System.exit( 1);
    }
    NavNode navNode = (NavNode) node;
    NavNodeGenerics.addEntityNavNodes( navNode, this, isDebugPrint);
    NavNodeGenerics.addParentToEntityNavLinks( navNode, this, isDebugPrint);
    NavNodeGenerics.addEntityToChildNavLinks( navNode, this, isDebugPrint);

    int penWidth = this.getOpenJGoPenWidth( this.getZoomFactor());
    node.setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    node.setAreNeighborsShown( true);
  } // end renderInitialNode


  /**
   * <code>addEntityNavNode</code>
   *
   * @param object - <code>PwEntity</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>ExtendedBasicNode</code> - 
   */
  protected final ExtendedBasicNode addEntityNavNode( final PwEntity object,
                                                      final boolean isDebugPrint) {
    ExtendedBasicNode node = null;
    if (object instanceof PwTimeline) {
      node = addTimelineNavNode( (PwTimeline) object);
    } else if (object instanceof PwResource) {
      node = addResourceNavNode( (PwResource) object);
    } else if (object instanceof PwSlot) {
      node = addSlotNavNode( (PwSlot) object);
    } else if (object instanceof PwToken) {
      node = addTokenNavNode( (PwToken) object);
    } else if (object instanceof PwVariable) {
      node = addVariableNavNode( (PwVariable) object);
    } else if (object instanceof PwConstraint) {
      node = addConstraintNavNode( (PwConstraint) object);
    } else if (object instanceof PwObject) {
      node = addModelClassNavNode( (PwObject) object);
    } else {
      System.err.println( "\nNavigatorView.addEntityNavNode " + object + " not handled");
      try {
        throw new Exception();
      } catch (Exception e) { e.printStackTrace(); }
    }
    NavNode navNode = (NavNode) node;
    if (isDebugPrint) {
      System.err.println( "add " + navNode.getTypeName() + "NavNode " + navNode.getId());
    }
    if (! navNode.inLayout()) {
      navNode.setInLayout( true);
    }
    return node;
  } // end addNavNode

  /**
   * <code>addModelClassNavNode</code>
   *
   * @param object - <code>PwObject</code> - 
   * @return - <code>ModelClassNavNode</code> - 
   */
  protected final ModelClassNavNode addModelClassNavNode( final PwObject object) {
    boolean isDraggable = true;
    ModelClassNavNode objectNavNode =
      (ModelClassNavNode) entityNavNodeMap.get( object.getId());
    if (objectNavNode == null) {
      objectNavNode =
        new ModelClassNavNode( object, 
                               new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                          ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                               getTimelineColor( object.getId()),
                               isDraggable, this);
      entityNavNodeMap.put( object.getId(), objectNavNode);
      jGoDocument.addObjectAtTail( objectNavNode);
    }
    return objectNavNode;
  } // end addModelClassNavNode

  /**
   * <code>addTimelineNavNode</code>
   *
   * @param timeline - <code>PwTimeline</code> - 
   * @return - <code>TimelineNavNode</code> - 
   */
  protected final TimelineNavNode addTimelineNavNode( final PwTimeline timeline) {
    boolean isDraggable = true;
    TimelineNavNode timelineNavNode =
      (TimelineNavNode) entityNavNodeMap.get( timeline.getId());
    if (timelineNavNode == null) {
      timelineNavNode =
        new TimelineNavNode( timeline, 
                             new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                        ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                             getTimelineColor( timeline.getId()),
                             isDraggable, this);
      entityNavNodeMap.put( timeline.getId(), timelineNavNode);
      jGoDocument.addObjectAtTail( timelineNavNode);
    }
    return timelineNavNode;
  } // end addTimelineNavNode

  /**
   * <code>addResourceNavNode</code>
   *
   * @param resource - <code>PwResource</code> - 
   * @return - <code>ResourceNavNode</code> - 
   */
  protected final ResourceNavNode addResourceNavNode( final PwResource resource) {
    boolean isDraggable = true;
    ResourceNavNode resourceNavNode =
      (ResourceNavNode) entityNavNodeMap.get( resource.getId());
    if (resourceNavNode == null) {
      resourceNavNode =
        new ResourceNavNode( resource, 
                             new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                        ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                             getTimelineColor( resource.getId()),
                             isDraggable, this);
      entityNavNodeMap.put( resource.getId(), resourceNavNode);
      jGoDocument.addObjectAtTail( resourceNavNode);
    }
    return resourceNavNode;
  } // end addResourceNavNode

  /**
   * <code>addSlotNavNode</code>
   *
   * @param slot - <code>PwSlot</code> - 
   * @return - <code>SlotNavNode</code> - 
   */
  protected final SlotNavNode addSlotNavNode( final PwSlot slot) {
    boolean isDraggable = true;
    PwTimeline timeline = partialPlan.getTimeline( slot.getTimelineId());

//     System.err.println( "addSlotNavNode id = " + slot.getId());
//     System.err.println( "entityNavNodeMap ");
//     List keyList = new ArrayList( entityNavNodeMap.keySet());
//     Iterator keyItr = keyList.iterator();
//     while (keyItr.hasNext()) {
//       Integer key = (Integer) keyItr.next();
//       System.err.println( "  key " + key + " " + entityNavNodeMap.get( key).getClass());
//     }

    SlotNavNode slotNavNode =
      (SlotNavNode) entityNavNodeMap.get( slot.getId());
    if (slotNavNode == null) {
      slotNavNode =
        new SlotNavNode( slot, 
                         new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                    ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                         getTimelineColor( timeline.getId()),
                         isDraggable, this);
      entityNavNodeMap.put( slot.getId(), slotNavNode);
      jGoDocument.addObjectAtTail( slotNavNode);
    }
    return slotNavNode;
  } // end addSlotNavNode

  /**
   * <code>addTokenNavNode</code>
   *
   * @param token - <code>PwToken</code> - 
   * @return - <code>TokenNavNode</code> - 
   */
  protected final TokenNavNode addTokenNavNode( final PwToken token) {
    // TokenNetwork.TokenNode  TemporalExtent.TemporalNode
    // System.err.println( "addTokenNavNode " + token.getClass());
    boolean isDraggable = true;
    TokenNavNode tokenNavNode =
      (TokenNavNode) entityNavNodeMap.get( token.getId());
    if (tokenNavNode == null) {
      tokenNavNode =
        new TokenNavNode( token, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                            ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                          NavNodeGenerics.getTokenColor( token, this), isDraggable, this);
      entityNavNodeMap.put( token.getId(), tokenNavNode);
      jGoDocument.addObjectAtTail( tokenNavNode);
    }
    return tokenNavNode;
  } // end addTokenNavNode

  /**
   * <code>addVariableNavNode</code>
   *
   * @param variable - <code>PwVariable</code> - 
   * @return - <code>VariableNavNode</code> - 
   */
  protected final VariableNavNode addVariableNavNode( final PwVariable variable) {
    // ConstraintNetwork.VariableNode
    boolean isDraggable = true;
    VariableNavNode variableNavNode =
      (VariableNavNode) entityNavNodeMap.get( variable.getId());
    if (variableNavNode == null) {
      variableNavNode =
        new VariableNavNode( variable, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                                  ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                             NavNodeGenerics.getVariableColor( variable, this), isDraggable,
                             this);
      entityNavNodeMap.put( variable.getId(), variableNavNode);
      jGoDocument.addObjectAtTail( variableNavNode);
    }
    return variableNavNode;
  } // end renderVariableNode

  /**
   * <code>addConstraintNavNode</code>
   *
   * @param constraint - <code>PwConstraint</code> - 
   * @return - <code>ConstraintNavNode</code> - 
   */
  protected final ConstraintNavNode addConstraintNavNode( final PwConstraint constraint) {
    // ConstraintNetwork.ConstraintNode
    boolean isDraggable = true;
    PwVariable variable = (PwVariable) constraint.getVariablesList().get( 0);
    ConstraintNavNode constraintNavNode =
      (ConstraintNavNode) entityNavNodeMap.get( constraint.getId());
    if (constraintNavNode == null) {
      constraintNavNode =
        new ConstraintNavNode( constraint, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                                      ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                               NavNodeGenerics.getVariableColor( variable, this), isDraggable,
                               this);
      entityNavNodeMap.put( constraint.getId(), constraintNavNode);
      jGoDocument.addObjectAtTail( constraintNavNode);
    }
    return constraintNavNode;
  } // end renderConstraintNode

  /**
   * <code>addNavigatorLinkNew</code>
   *
   * @param fromNode - <code>ExtendedBasicNode</code> - 
   * @param link - <code>BasicNodeLink</code> - 
   * @param linkType - <code>String</code> - 
   * @return - <code>boolean</code> - 
   */
  protected final boolean addNavigatorLinkNew( final ExtendedBasicNode fromNode,
                                               final BasicNodeLink link, final String linkType) {
    boolean areLinksChanged = false;
    if (link != null) {
      // links are always behind any nodes
      // jGoDocument.addObjectAtHead( link);
      //jGoDocument.addObjectAtTail( link);
      jGoDocument.insertObjectBefore( jGoDocument.findObject( fromNode), link);

      link.setInLayout( true);
      link.incrLinkCount();
      if (isDebugPrint) {
        System.err.println( linkType + " incr link: " + link.toString() + " to " +
                            link.getLinkCount());
      }
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end addNavigatorLinkNew


  // ***********************************************


  private void setNodesLinksVisible() {
    List objectNodeKeyList = new ArrayList( entityNavNodeMap.keySet());
    Iterator objectNodeKeyItr = objectNodeKeyList.iterator();
    while (objectNodeKeyItr.hasNext()) {
      ExtendedBasicNode objectNavNode =
        (ExtendedBasicNode) entityNavNodeMap.get( (Integer) objectNodeKeyItr.next());
      if (((NavNode) objectNavNode).inLayout()) {
        objectNavNode.setVisible( true);
      } else {
        objectNavNode.setVisible( false);
      }
    }
    List navLinkKeyList = new ArrayList( navLinkMap.keySet());
    Iterator navLinkKeyItr = navLinkKeyList.iterator();
    while (navLinkKeyItr.hasNext()) {
      BasicNodeLink navLink =
        (BasicNodeLink) navLinkMap.get( (String) navLinkKeyItr.next());
      if (navLink.inLayout()) {
        navLink.setVisible( true);
        if (isDebugPrint && (navLink.getMidLabel() != null)) {
          navLink.getMidLabel().setVisible( true);
        }
      } else {
        navLink.setVisible( false);
        if (isDebugPrint && (navLink.getMidLabel() != null)) {
          navLink.getMidLabel().setVisible( false);
        }
      }
    }
  } // end setNodesLinksVisible


  /**
   * <code>NavigatorJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  public class NavigatorJGoView extends JGoView {

    private NavigatorView navigatorView;

    /**
     * <code>NavigatorJGoView</code> - constructor 
     *
     */
    public NavigatorJGoView( NavigatorView navigatorView) {
      super();
      this.navigatorView = navigatorView;
    }

    /**
     * <code>resetOpenNodes</code> - reset the nodes bounding rectangles highlight width
     *                               to the current zoom factor
     *
     */
    public void resetOpenNodes() {
      int penWidth = navigatorView.getOpenJGoPenWidth( navigatorView.getZoomFactor());
      Iterator navigatorNodeItr = entityNavNodeMap.values().iterator();
      while (navigatorNodeItr.hasNext()) {
        ExtendedBasicNode navigatorNode = (ExtendedBasicNode) navigatorNodeItr.next();
        if (navigatorNode.areNeighborsShown()) {
          navigatorNode.setPen( new JGoPen( JGoPen.SOLID, penWidth,
                                            ColorMap.getColor( "black")));
        }
        // force links to be redrawn to eliminate gaps when changing zoom factor
        navigatorNode.setLocation( navigatorNode.getLocation());
      }
    } // end resetOpenNodes

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public final void doBackgroundClick( final int modifiers, final Point docCoords,
                                         final Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick

  } // end class NavigatorJGoView

  private void mouseRightPopupMenu( final Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         PlanWorks.NAVIGATOR_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    this.createZoomItem( jGoView, zoomFactor, mouseRightPopup, this);

    createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createOverviewWindowItem( final JMenuItem overviewWindowItem,
                                         final NavigatorView navigatorView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public final void actionPerformed( final ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( navigatorFrame, partialPlan, navigatorView,
                                            viewSet, jGoView, viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem


} // end class NavigatorView
