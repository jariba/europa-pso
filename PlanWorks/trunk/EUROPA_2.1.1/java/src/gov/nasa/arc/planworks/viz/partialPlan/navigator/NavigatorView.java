// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NavigatorView.java,v 1.40 2004-09-30 22:04:49 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 06jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.BorderLayout;
import java.awt.Container;
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

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.StringViewSetKey;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.IncrementalNode;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.FindEntityPathAdapter;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkObjectNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkResourceNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkRuleInstanceNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTimelineNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkTokenNode;
import gov.nasa.arc.planworks.viz.util.AskNodeByKey;
import gov.nasa.arc.planworks.viz.util.AskQueryTwoEntityKeysClasses;
import gov.nasa.arc.planworks.viz.util.FindEntityPath;
import gov.nasa.arc.planworks.viz.util.ProgressMonitorThread;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>NavigatorView</code> - render a partial plan's node network
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class NavigatorView extends PartialPlanView
  implements StringViewSetKey, FindEntityPathAdapter {

  private PwEntity initialEntity;
  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private MDIInternalFrame navigatorFrame;
  private NavigatorJGoView jGoView;
  private JGoDocument jGoDocument;
  private boolean isLayoutNeeded;
  private ExtendedBasicNode focusNode;
  private boolean isDebugPrint;
  private String viewSetKey;
  private List highlightPathNodesList;
  private List highlightPathLinksList;
  private ProgressMonitorThread redrawPMThread;
  private boolean didRenderInitialNodeChange;
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
   * @param entity - <code>PwEntity</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewSetKey - <code>String</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final PwEntity entity, final ViewableObject partialPlan,
                        final ViewSet viewSet, final String viewSetKey,
                        final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialEntity = entity;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.viewSetKey = viewSetKey;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param node - <code>VariableContainerNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewSetKey - <code>String</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( final VariableContainerNode node , final ViewableObject partialPlan,
                        final ViewSet viewSet, final String viewSetKey,
                        final MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    PwEntity entity = null;
    if (node instanceof ConstraintNetworkObjectNode) {
      entity = ((ConstraintNetworkObjectNode) node).getObject();
    } else if (node instanceof ConstraintNetworkRuleInstanceNode) {
      entity = ((ConstraintNetworkRuleInstanceNode) node).getRuleInstance();
    } else if (node instanceof ConstraintNetworkResourceNode) {
      entity = ((ConstraintNetworkResourceNode) node).getResource();
    } else if (node instanceof ConstraintNetworkTimelineNode) {
      entity = ((ConstraintNetworkTimelineNode) node).getTimeline();
    } else if (node instanceof ConstraintNetworkTokenNode) {
      entity = ((ConstraintNetworkTokenNode) node).getToken();
    } else {
      System.err.println( "NavigatorView node " + node.getClass().getName() + " not handled");
      System.exit( -1);
    }
    this.initialEntity = entity;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.viewSetKey = viewSetKey;
    this.navigatorFrame = navigatorFrame;

    commonConstructor();
  } // end constructor

  private void commonConstructor() {
    System.err.println( "Render Navigator View ...");
    this.startTimeMSecs = System.currentTimeMillis();
    // isDebugPrint = true;
    isDebugPrint = false;
    //timelineColorMap = createTimelineColorMap();
    highlightPathNodesList = null;
    highlightPathLinksList = null;
    viewFrame = navigatorFrame;
    this.setName( navigatorFrame.getTitle());
    viewName = ViewConstants.NAVIGATOR_VIEW;

    navLinkMap = new HashMap();
    entityNavNodeMap = new HashMap();

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new NavigatorJGoView( this);
    jGoView.addViewListener( createViewListener());
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);
    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end commonConstructor

//   Runnable runInit = new Runnable() {
//       public final void run() {
//         init();
//       }
//     };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo navigator,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use SwingWorker in constructor
   */
  public final void init() {
    // wait for NavigatorView instance to become displayable
    if (! ViewGenerics.displayableWait( NavigatorView.this)) {
      closeView( this);
      return;
    }
    this.computeFontMetrics( this);

    jGoDocument = jGoView.getDocument();
    jGoDocument.addDocumentListener( createDocListener());
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
      ( (ViewConstants.INTERNAL_FRAME_X_DELTA / 2) + delta, contentFilterMaxY + delta);

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

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... " + ViewConstants.NAVIGATOR_VIEW + " elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    startTimeMSecs = 0L;
    isLayoutNeeded = false;
    focusNode = null;
    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();
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
      try {
        ViewGenerics.setRedrawCursor( navigatorFrame);
        redrawView();
      } finally {
        ViewGenerics.resetRedrawCursor( navigatorFrame);
      }
    } //end run

  } // end class RedrawViewThread

  private void redrawView() {
    System.err.println( "Redrawing Navigator View ...");
    if (startTimeMSecs == 0L) {
      startTimeMSecs = System.currentTimeMillis();
    }
    this.setVisible( false);

    redrawPMThread =
      createProgressMonitorThread( "Redrawing Navigator View ...", 0, 6, Thread.currentThread(),
			   this);
    if (! progressMonitorWait( redrawPMThread, this)) {
      System.err.println( "progressMonitorWait failed");
      closeView( this);
      return;
    }
    redrawPMThread.getProgressMonitor().setProgress( 3 * ViewConstants.MONITOR_MIN_MAX_SCALING);
    // content spec apply/reset do not change layout, only TokenNode/
    // variableNode/constraintNode opening/closing
    setNodesLinksVisible();

    if (isLayoutNeeded) {
      NavigatorViewLayout layout = new NavigatorViewLayout( jGoDocument, startTimeMSecs);
      layout.performLayout();

      isLayoutNeeded = false;
    }

    if ((focusNode == null) && (highlightPathNodesList != null) &&
        (highlightPathLinksList != null)) {
      NodeGenerics.highlightPathNodes( highlightPathNodesList, jGoView);
      NodeGenerics.highlightPathLinks( highlightPathLinksList, this, jGoView);
    } else if (focusNode != null) {
      // do not highlight node, if it has been removed
      NodeGenerics.focusViewOnNode( focusNode, ((IncrementalNode) focusNode).inLayout(),
				    jGoView);
    }

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... " + ViewConstants.NAVIGATOR_VIEW + " elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    startTimeMSecs = 0L;
    this.setVisible( true);
    redrawPMThread.setProgressMonitorCancel();
  } // end redrawView

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

  /**
   * <code>getViewSetKey</code> - implements StringViewSetKey
   *
   * @return - <code>String</code> - 
   */
  public final String getViewSetKey() {
    return viewSetKey;
  }


  private ExtendedBasicNode renderInitialNode( PwEntity entity) {
    initialEntity = entity;
    return renderInitialNode();
  }

  private ExtendedBasicNode renderInitialNode() {
    ExtendedBasicNode node = addEntityNavNode( initialEntity, isDebugPrint);
    IncrementalNode navNode = (IncrementalNode) node;
    boolean  areNodesChanged = NavNodeGenerics.addEntityNavNodes( navNode, this, isDebugPrint);
    boolean areParentLinksChanged =
      NavNodeGenerics.addParentToEntityNavLinks( navNode, this, isDebugPrint);
    boolean areChildLinksChanged =
      NavNodeGenerics.addEntityToChildNavLinks( navNode, this, isDebugPrint);
    if (areNodesChanged || areParentLinksChanged || areChildLinksChanged) {
      didRenderInitialNodeChange = true;
    } else {
      didRenderInitialNodeChange = false;
    }
    int penWidth = this.getOpenJGoPenWidth( this.getZoomFactor());
    node.setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    node.setAreNeighborsShown( true);
    return node;
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
    } else if (object instanceof PwRuleInstance) {
      node = addRuleInstanceNavNode( (PwRuleInstance) object);
    } else if (object instanceof PwObject) {
      node = addModelClassNavNode( (PwObject) object);
    } else {
      System.err.println( "\nNavigatorView.addEntityNavNode " + object + " not handled");
      try {
        throw new Exception();
      } catch (Exception e) { e.printStackTrace(); }
    }
    IncrementalNode navNode = (IncrementalNode) node;
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
   * <code>addRuleInstanceNavNode</code>
   *
   * @param ruleInstance - <code>PwRuleInstance</code> - 
   * @return - <code>RuleInstanceNavNode</code> - 
   */
  protected final RuleInstanceNavNode addRuleInstanceNavNode
    ( final PwRuleInstance ruleInstance) {
    boolean isDraggable = true;
    RuleInstanceNavNode ruleInstanceNavNode =
      (RuleInstanceNavNode) entityNavNodeMap.get( ruleInstance.getId());
    if (ruleInstanceNavNode == null) {
      ruleInstanceNavNode =
        new RuleInstanceNavNode( ruleInstance,
                                 new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                            ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                                 ViewConstants.RULE_INSTANCE_BG_COLOR, isDraggable, this);
      entityNavNodeMap.put( ruleInstance.getId(), ruleInstanceNavNode);
      jGoDocument.addObjectAtTail( ruleInstanceNavNode);
    }
    return ruleInstanceNavNode;
  } // end addRuleInstanceNavNode

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
    // System.err.println( "addTokenNavNode id " + token.getId() + " " + token.getClass());
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
      if (((IncrementalNode) objectNavNode).inLayout()) {
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
     * @param navigatorView - <code>NavigatorView</code> - 
     */
    public NavigatorJGoView( final NavigatorView navigatorView) {
      super();
      this.navigatorView = navigatorView;
    }

    /**
     * <code>resetOpenNodes</code> - reset the nodes bounding rectangles highlight width
     *                               to the current zoom factor
     *
     */
    public final void resetOpenNodes() {
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
        NodeGenerics.unhighlightPathLinks( NavigatorView.this);
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        NodeGenerics.unhighlightPathLinks( NavigatorView.this);
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick

  } // end class NavigatorJGoView

  private void mouseRightPopupMenu( final Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
    createNodeByKeyItem( nodeByKeyItem);
    mouseRightPopup.add( nodeByKeyItem);

    JMenuItem findEntityPathItem = new JMenuItem( "Find Entity Path");
    createFindEntityPathItem( findEntityPathItem);
    mouseRightPopup.add( findEntityPathItem);

    if (highlightPathNodesList != null) {
      JMenuItem highlightPathItem = new JMenuItem( "Highlight Current Path");
      createHighlightPathItem( highlightPathItem, highlightPathNodesList,
                               highlightPathLinksList);
      mouseRightPopup.add( highlightPathItem);
    }

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         viewListenerList, ViewConstants.NAVIGATOR_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    this.createZoomItem( jGoView, zoomFactor, mouseRightPopup, this);

    createAllViewItems( partialPlan, partialPlanName, planSequence, viewListenerList,
                        mouseRightPopup);

    ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createNodeByKeyItem( JMenuItem nodeByKeyItem) {
    nodeByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskNodeByKey nodeByKeyDialog =
            new AskNodeByKey( "Find by Key", "key (int)", NavigatorView.this);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            highlightPathNodesList = null;
            highlightPathLinksList = null;
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());
            Integer entityKey1 = null, entityKey2 = null; List pathClasses = null;
            boolean doPathExists = false;
            int maxPathLength = Integer.MAX_VALUE;
            MDIInternalFrame frame = null;
            FindEntityPath findEntityPath =
              new FindEntityPath( entityKey1, entityKey2, pathClasses, doPathExists,
                                  maxPathLength, partialPlan, NavigatorView.this, frame);
            List entityKeyList = new ArrayList();
            entityKeyList.add( nodeKey);
            findEntityPath.setEntityKeyList( entityKeyList);
           renderEntityPathNodes( findEntityPath);
          }
        }
      });
  } // end createNodeByKeyItem

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

  private void createFindEntityPathItem( JMenuItem findEntityPathItem) {
    findEntityPathItem.addActionListener( new ActionListener() {
	public void actionPerformed( ActionEvent evt) {
          MDIInternalFrame twoEntityKeysWindow = 
            PlanWorks.getPlanWorks().createFrame( "Find Path in " + navigatorFrame.getTitle(),
                                                  getViewSet(), true, true, false, false);
          Container contentPane = twoEntityKeysWindow.getContentPane();
          AskQueryTwoEntityKeysClasses twoEntityKeysContent =
            new AskQueryTwoEntityKeysClasses( twoEntityKeysWindow, partialPlan,
                                              NavigatorView.this);
          contentPane.add( twoEntityKeysContent);
          twoEntityKeysWindow.pack();
	}
      });
  } // createFindEntityPathItem

  /**
   * <code>invokeFindEntityPathClasses</code>
   *
   * @param entityKey1 - <code>Integer</code> - 
   * @param entityKey2 - <code>Integer</code> - 
   * @param pathClasses - <code>List</code> - 
   * @param doPathExists - <code>boolean</code> - 
   * @param maxPathLength - <code>int</code> - 
   * @param dialogWindowFrame - <code>MDIInternalFrame</code> - 
   */
  public void invokeFindEntityPathClasses( final Integer entityKey1,
                                           final Integer entityKey2,
                                           final List pathClasses,
                                           final boolean doPathExists,
                                           final int maxPathLength,
                                           final MDIInternalFrame dialogWindowFrame) {
//     System.err.println( "invoke entityKey1 " + entityKey1);
//     System.err.println( "invoke entityKey2 " + entityKey2);
//     for (int i = 0, n = pathClasses.size(); i < n; i++) {
//       System.err.println( "invoke pathClass i=" + i + " " +
//                           ((Class) pathClasses.get( i)).getName());
//     }
//     System.err.println( "invoke doPathExists " + doPathExists);
//     System.err.println( "invoke maxPathLength " + maxPathLength);
    if ((entityKey1 == null) || (entityKey2 == null) || (pathClasses == null)) {
      return;
    }
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          FindEntityPath findEntityPath =
            new FindEntityPath( entityKey1, entityKey2, pathClasses, doPathExists,
                                maxPathLength, partialPlan, NavigatorView.this,
                                dialogWindowFrame);
          findEntityPath.invokeAndWait();
         return null;
        }
      };
    worker.start();
  } // end invokeFindEntityPathClasses

  /**
   * <code>renderEntityPathNodes</code>
   *
   * @param findEntityPath - <code>FindEntityPath</code> - 
   * @return - <code>List</code> - 
   */
  public List renderEntityPathNodes( final FindEntityPath findEntityPath) {
    boolean isLayoutNeeded = false;
    List nodeList =  new ArrayList(); List linkList  =  new ArrayList();
    Iterator entityItr = findEntityPath.getEntityKeyList().iterator();
    PwEntity entity = null;
    boolean isFirstNode = true;
    while (entityItr.hasNext()) {
      Integer entityKey = (Integer) entityItr.next();
      // System.err.println( "key " + entityKey);
      if ((entity = partialPlan.getToken( entityKey)) != null) {
	TokenNavNode tokenNode = null;
	if (isFirstNode) {
	  isFirstNode = false; tokenNode = (TokenNavNode) renderInitialNode( entity);
	  if (didRenderInitialNodeChange) { isLayoutNeeded = true; }
	} else {
	  PwToken token = (PwToken) entity;
	  tokenNode = addTokenNavNode( token);
	  if (! tokenNode.areNeighborsShown()) {
	    if (tokenNode.addTokenObjects( tokenNode)) { isLayoutNeeded = true; }
	    tokenNode.setAreNeighborsShown( true);
	  }
	}
	nodeList.add( tokenNode);
      } else if ((entity = partialPlan.getVariable( entityKey)) != null) {
	VariableNavNode variableNode = null;
	if (isFirstNode) {
	  isFirstNode = false; variableNode = (VariableNavNode) renderInitialNode( entity);
	  if (didRenderInitialNodeChange) { isLayoutNeeded = true; }
	} else {
	  PwVariable variable = (PwVariable) entity;
	  variableNode = addVariableNavNode( variable);
	  if (! variableNode.areNeighborsShown()) {
	    if (variableNode.addVariableObjects( variableNode)) { isLayoutNeeded = true; }
	    variableNode.setAreNeighborsShown( true);
	  }
	}
	nodeList.add( variableNode);
      } else if ((entity = partialPlan.getConstraint( entityKey)) != null) {
	ConstraintNavNode constraintNode = null;
	if (isFirstNode) {
	  isFirstNode = false; constraintNode = (ConstraintNavNode) renderInitialNode( entity);
	  if (didRenderInitialNodeChange) { isLayoutNeeded = true; }
	} else {
	  PwConstraint constraint = (PwConstraint) entity;
	  constraintNode = addConstraintNavNode( constraint);
	  if (! constraintNode.areNeighborsShown()) {
	    if (constraintNode.addConstraintObjects( constraintNode)) {
	      isLayoutNeeded = true;
	    }
	    constraintNode.setAreNeighborsShown( true);
	  }
	}
	nodeList.add( constraintNode);
      } else if ((entity = partialPlan.getRuleInstance( entityKey)) != null) {
	RuleInstanceNavNode ruleInstanceNode = null;
	if (isFirstNode) {
	  isFirstNode = false;
	  ruleInstanceNode = (RuleInstanceNavNode) renderInitialNode( entity);
	  if (didRenderInitialNodeChange) { isLayoutNeeded = true; }
	} else {
	  PwRuleInstance ruleInstance = (PwRuleInstance) entity;
	  ruleInstanceNode = addRuleInstanceNavNode( ruleInstance);
	  if (! ruleInstanceNode.areNeighborsShown()) {
	    if (ruleInstanceNode.addRuleInstanceObjects( ruleInstanceNode)) {
	      isLayoutNeeded = true;
	    }
	    ruleInstanceNode.setAreNeighborsShown( true);
	  }
	}
	nodeList.add( ruleInstanceNode);
      } else if ((entity = partialPlan.getSlot( entityKey)) != null) {
	SlotNavNode slotNode = null;
	if (isFirstNode) {
	  isFirstNode = false; slotNode = (SlotNavNode) renderInitialNode( entity);
	  if (didRenderInitialNodeChange) { isLayoutNeeded = true; }
	} else {
	  PwSlot slot = (PwSlot) entity;
	  slotNode = addSlotNavNode( slot);
	  if (! slotNode.areNeighborsShown()) {
	    if (slotNode.addSlotObjects( slotNode)) {
	      isLayoutNeeded = true;
	    }
	    slotNode.setAreNeighborsShown( true);
	  }
	}
	nodeList.add( slotNode);
      } else if ((entity = partialPlan.getTimeline( entityKey)) != null) {
	TimelineNavNode timelineNode = null;
	if (isFirstNode) {
	  isFirstNode = false; timelineNode = (TimelineNavNode) renderInitialNode( entity);
	  if (didRenderInitialNodeChange) { isLayoutNeeded = true; }
	} else {
	  PwTimeline timeline = (PwTimeline) entity;
	  timelineNode = addTimelineNavNode( timeline);
	  if (! timelineNode.areNeighborsShown()) {
	    if (timelineNode.addTimelineObjects( timelineNode)) {
	      isLayoutNeeded = true;
	    }
	    timelineNode.setAreNeighborsShown( true);
	  }
	}
	nodeList.add( timelineNode);
      } else if ((entity = partialPlan.getResource( entityKey)) != null) {
	ResourceNavNode resourceNode = null;
	if (isFirstNode) {
	  isFirstNode = false; resourceNode = (ResourceNavNode) renderInitialNode( entity);
	  if (didRenderInitialNodeChange) { isLayoutNeeded = true; }
	} else {
	  PwResource resource = (PwResource) entity;
	  resourceNode = addResourceNavNode( resource);
	  if (! resourceNode.areNeighborsShown()) {
	    if (resourceNode.addResourceObjects( resourceNode)) {
	      isLayoutNeeded = true;
	    }
	    resourceNode.setAreNeighborsShown( true);
	  }
	}
	nodeList.add(resourceNode);
      } else if ((entity = partialPlan.getObject( entityKey)) != null) {
	// PwObject must be last
	ModelClassNavNode objectNode = null;
	if (isFirstNode) {
	  isFirstNode = false; objectNode = (ModelClassNavNode) renderInitialNode( entity);
	  if (didRenderInitialNodeChange) { isLayoutNeeded = true; }
	} else {
	  PwObject object = (PwObject) entity;
	  objectNode = addModelClassNavNode( object);
	  if (! objectNode.areNeighborsShown()) {
	    if (objectNode.addObjects( objectNode)) {
	      isLayoutNeeded = true;
	    }
	    objectNode.setAreNeighborsShown( true);
	  }
	}
	nodeList.add( objectNode);
      } else {
	System.err.println( "NavigatorView.renderEntityPathNodes: entityKey " +
			    entityKey + " not handled");
      }
      if (nodeList.size() >= 2) {
        BasicNodeLink link = NodeGenerics.getLinkFromNodes
          ( (IncrementalNode) nodeList.get( nodeList.size() - 2),
            (IncrementalNode) nodeList.get( nodeList.size() - 1), navLinkMap);
        if (link != null) {
          linkList.add( link);
        }
      }
    }
    if (isLayoutNeeded) {
      // System.err.println( "NavigatorView.renderEntityPathNodes: setLayoutNeeded");
      setLayoutNeeded();
    }
    setFocusNode( null);
    highlightPathNodesList = nodeList;
    highlightPathLinksList = linkList;
    redraw();
    return nodeList;
  } // end renderEntityPathNodes

  private void createHighlightPathItem( final JMenuItem highlightPathItem,
					final List nodeList, final List linksList) {
    highlightPathItem.addActionListener( new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	  NodeGenerics.highlightPathNodes( nodeList, jGoView);
	  NodeGenerics.highlightPathLinks( linksList, NavigatorView.this, jGoView);
	  FindEntityPath.outputEntityPathNodes( nodeList, NavigatorView.this);
	}
      });
  } // end createHighlightPathItem


  public List getDefaultLinkTypes() {
    return ViewConstants.ALL_LINK_TYPES;
  }

} // end class NavigatorView
