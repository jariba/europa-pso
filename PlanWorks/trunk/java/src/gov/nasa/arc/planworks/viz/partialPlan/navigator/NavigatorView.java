// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NavigatorView.java,v 1.9 2004-02-13 02:37:07 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 06jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
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
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
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
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.SlotNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineNode;
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

  private JGoArea initialNode;
  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private MDIInternalFrame navigatorFrame;
  private NavigatorJGoView jGoView;
  private JGoDocument jGoDocument;
  private boolean isLayoutNeeded;
  private ExtendedBasicNode focusNode;
  private boolean isDebugPrint;
  protected Map timelineColorMap;
  protected Map objectNavNodeMap;
  protected Map timelineNavNodeMap;
  protected Map slotNavNodeMap;
  protected Map tokenNavNodeMap;
  protected Map variableNavNodeMap;
  protected Map constraintNavNodeMap;
  protected Map navLinkMap;

  /**
   * <code>NavigatorView</code> - constructor 
   *
   * @param timelineNode - <code>TimelineNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( TimelineNode timelineNode, ViewableObject partialPlan,
                        ViewSet viewSet, MDIInternalFrame navigatorFrame) {
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
   * @param slotNode - <code>SlotNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param navigatorFrame - <code>MDIInternalFrame</code> - 
   */
  public NavigatorView( SlotNode slotNode, ViewableObject partialPlan,
                        ViewSet viewSet, MDIInternalFrame navigatorFrame) {
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
  public NavigatorView( TokenNode tokenNode, ViewableObject partialPlan,
                        ViewSet viewSet, MDIInternalFrame navigatorFrame) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.initialNode = tokenNode;
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
  public NavigatorView( TemporalNode temporalNode, ViewableObject partialPlan,
                        ViewSet viewSet, MDIInternalFrame navigatorFrame) {
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
  public NavigatorView( VariableNode variableNode, ViewableObject partialPlan,
                        ViewSet viewSet, MDIInternalFrame navigatorFrame) {
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
  public NavigatorView( ConstraintNode constraintNode, ViewableObject partialPlan,
                        ViewSet viewSet, MDIInternalFrame navigatorFrame) {
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
    objectNavNodeMap = new HashMap();
    timelineNavNodeMap = new HashMap();
    slotNavNodeMap = new HashMap();
    tokenNavNodeMap = new HashMap();
    variableNavNodeMap = new HashMap();
    constraintNavNodeMap = new HashMap();

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new NavigatorJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);

    SwingUtilities.invokeLater( runInit);
  } // end commonConstructor

  Runnable runInit = new Runnable() {
      public void run() {
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
  public void init() {
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for NavigatorView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "navigatorView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);

    jGoDocument = jGoView.getDocument();

    renderInitialNodes();

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
  public void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
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
  public JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>setStartTimeMSecs</code>
   *
   * @param msecs - <code>long</code> - 
   */
  protected void setStartTimeMSecs( long msecs) {
    startTimeMSecs = msecs;
  }

  /**
   * <code>setLayoutNeeded</code>
   *
   */
  public void setLayoutNeeded() {
    isLayoutNeeded = true;
  }

  /**
   * <code>isLayoutNeeded</code>> - needed for PlanWorksTest
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isLayoutNeeded() {
    return isLayoutNeeded ;
  }

  /**
   * <code>setFocusNode</code>
   *
   * @param node - <code>ExtendedBasicNode</code> - 
   */
  public void setFocusNode( ExtendedBasicNode node) {
    this.focusNode = node;
  }

  private void renderInitialNodes() {
    if (initialNode instanceof TimelineNode) {
      renderTimelineNode();
    } else if (initialNode instanceof SlotNode) {
      renderSlotNode();
    } else if ((initialNode instanceof TokenNode) ||
               (initialNode instanceof TemporalNode)) {
      renderTokenNode();
    } else if (initialNode instanceof VariableNode) {
      renderVariableNode();
    } else if (initialNode instanceof ConstraintNode) {
      renderConstraintNode();
    } else {
      System.err.println( " NavigatorView.renderInitialNodes: " + initialNode +
                      " not handled");
      System.exit( 1);
    }
  } // end renderInitialNodes

  private void renderTimelineNode() {
    // TimelineView.TimelineNode
    PwTimeline timeline = ((TimelineNode) initialNode).getTimeline();
    PwObject object = partialPlan.getObject( timeline.getParentId());
    boolean isDraggable = true;
    ModelClassNavNode objectNavNode = null;
    if(object != null) {
      objectNavNode = 
        new ModelClassNavNode( object, new Point( ViewConstants.TIMELINE_VIEW_X_INIT,
                                                  ViewConstants.TIMELINE_VIEW_Y_INIT),
                               ColorMap.getColor( ViewConstants.OBJECT_BG_COLOR),
                               isDraggable, this);
      objectNavNode.setInLayout( true);
      objectNavNodeMap.put( object.getId(), objectNavNode);
      jGoDocument.addObjectAtTail( objectNavNode);
    }
    TimelineNavNode timelineNavNode =
      new TimelineNavNode( timeline, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                                ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                           getTimelineColor( timeline.getId()),
                           isDraggable, this);
    timelineNavNode.setInLayout( true);
    timelineNavNodeMap.put( timeline.getId(), timelineNavNode);
    jGoDocument.addObjectAtTail( timelineNavNode);

    if(objectNavNode != null) {
      addNavigatorLink( objectNavNode, timelineNavNode, timelineNavNode);
    }

    addSlotNavNodes( timelineNavNode);
    addTimelineToSlotNavLinks( timelineNavNode);

    timelineNavNode.setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    timelineNavNode.setAreNeighborsShown( true);
  } // end renderTimelineNode

  private void renderSlotNode() {
    // TimelineView.SlotNode
    boolean isDraggable = true;
    PwSlot slot = ((SlotNode) initialNode).getSlot();
    PwTimeline timeline = partialPlan.getTimeline( slot.getTimelineId());
    TimelineNavNode timelineNavNode =
      new TimelineNavNode( timeline, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                                ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                           getTimelineColor( timeline.getId()),
                           isDraggable, this);
    timelineNavNode.setInLayout( true);
    timelineNavNodeMap.put( timeline.getId(), timelineNavNode);
    jGoDocument.addObjectAtTail( timelineNavNode);

    SlotNavNode slotNavNode =
      new SlotNavNode( slot, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                        ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                       getTimelineColor( timeline.getId()),
                       isDraggable, this);
    slotNavNode.setInLayout( true);
    slotNavNodeMap.put( slot.getId(), slotNavNode);
    jGoDocument.addObjectAtTail( slotNavNode);

    addNavigatorLink( timelineNavNode, slotNavNode, slotNavNode);

    slotNavNode.addTokenNavNodes();
    slotNavNode.addSlotToTokenNavLinks();

    slotNavNode.setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    slotNavNode.setAreNeighborsShown( true);
  } // end renderSlotNode

  private void renderTokenNode() {
    // TokenNetwork.TokenNode  TemporalExtent.TemporalNode
    boolean isDraggable = true;
    PwToken token = null;
    if (initialNode instanceof TokenNode) {
      token = ((TokenNode) initialNode).getToken();
    } else if (initialNode instanceof TemporalNode) {
      token = ((TemporalNode) initialNode).getToken();
    } else {
      System.err.println( "NavigatorView.renderTokenNode " + initialNode +
                          " not handled");
      System.exit( -1);
    }
    SlotNavNode slotNavNode = null;
    Color nodeColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
    if (! token.isFreeToken()) {
      PwSlot slot = (PwSlot) partialPlan.getSlot( token.getSlotId());
      PwTimeline timeline = partialPlan.getTimeline( slot.getTimelineId());
      nodeColor = getTimelineColor( timeline.getId());
      slotNavNode = new SlotNavNode( slot, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                                      ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                                     nodeColor, isDraggable, this);
      slotNavNode.setInLayout( true);
      slotNavNodeMap.put( slot.getId(), slotNavNode);
      jGoDocument.addObjectAtTail( slotNavNode);
    }

    TokenNavNode tokenNavNode =
      new TokenNavNode( token, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                          ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                        nodeColor, isDraggable, this);
    tokenNavNode.setInLayout( true);
    tokenNavNodeMap.put( token.getId(), tokenNavNode);
    jGoDocument.addObjectAtTail( tokenNavNode);

    if (! token.isFreeToken()) {
      addNavigatorLink( slotNavNode, tokenNavNode, tokenNavNode);
    }

    tokenNavNode.addVariableNavNodes();
    tokenNavNode.addTokenToVariableNavLinks();

    tokenNavNode.addMasterNavNodes();
    tokenNavNode.addMasterToTokenNavLinks();
    tokenNavNode.addSlaveNavNodes();
    tokenNavNode.addTokenToSlaveNavLinks();

    tokenNavNode.setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    tokenNavNode.setAreNeighborsShown( true);
  } // end renderTokenNode

  private  void renderVariableNode() {
    // ConstraintNetwork.VariableNode
    boolean isDraggable = true;
    PwVariable variable = ((VariableNode) initialNode).getVariable();
    //THIS NEEDS TO CHANGE
    PwToken token = (PwToken) variable.getParent();
    Color nodeColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
    if (! token.isFreeToken()) {
      PwTimeline timeline = partialPlan.getTimeline( token.getTimelineId());
      nodeColor = getTimelineColor( timeline.getId());
    }
    TokenNavNode tokenNavNode =
      new TokenNavNode( token, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                          ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                       nodeColor, isDraggable, this);
    tokenNavNode.setInLayout( true);
    tokenNavNodeMap.put( token.getId(), tokenNavNode);
    jGoDocument.addObjectAtTail( tokenNavNode);

    VariableNavNode variableNavNode =
      new VariableNavNode( variable, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                                ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                       nodeColor, isDraggable, this);
    variableNavNode.setInLayout( true);
    variableNavNodeMap.put( variable.getId(), variableNavNode);
    jGoDocument.addObjectAtTail( variableNavNode);

    addNavigatorLink( tokenNavNode, variableNavNode, variableNavNode);

    variableNavNode.addConstraintNavNodes();
    variableNavNode.addVariableToConstraintNavLinks();

    variableNavNode.setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    variableNavNode.setAreNeighborsShown( true);
  } // end renderVariableNode

  private  void renderConstraintNode() {
    // ConstraintNetwork.ConstraintNode
    boolean isDraggable = true;
    PwConstraint constraint = ((ConstraintNode) initialNode).getConstraint();
    PwVariable variable = (PwVariable) constraint.getVariablesList().get( 0);
    //THIS NEEDS TO CHANGE
    PwToken token = (PwToken) variable.getParent();
    Color nodeColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
    if (! token.isFreeToken()) {
      PwTimeline timeline = partialPlan.getTimeline( token.getTimelineId());
      nodeColor = getTimelineColor( timeline.getId());
    }
    ConstraintNavNode constraintNavNode =
      new ConstraintNavNode( constraint, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                                    ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                             nodeColor, isDraggable, this);
    constraintNavNode.setInLayout( true);
    constraintNavNodeMap.put( constraint.getId(), constraintNavNode);
    jGoDocument.addObjectAtTail( constraintNavNode);

    constraintNavNode.addVariableNavNodes();
    constraintNavNode.addVariableToConstraintNavLinks();

    constraintNavNode.setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    constraintNavNode.setAreNeighborsShown( true);
  } // end renderConstraintNode

  // ********************************************************** timelineNavNodes

  /**
   * <code>addTimelineNodes</code>
   *
   * @param objectNavNode - <code>ModelClassNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addTimelineNavNodes( ModelClassNavNode objectNavNode) {
    boolean areNodesChanged = false;
    List timelineList = objectNavNode.getObject().getComponentList();
    boolean isDraggable = true;
    Iterator timelineIterator = timelineList.iterator();
    while (timelineIterator.hasNext()) {
      PwTimeline timeline = (PwTimeline) timelineIterator.next();
      TimelineNavNode timelineNavNode =
        (TimelineNavNode) timelineNavNodeMap.get( timeline.getId());
      if (timelineNavNode == null) {
        timelineNavNode =
          new TimelineNavNode( timeline, 
                               new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                          ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                               getTimelineColor( timeline.getId()),
                               isDraggable, this);
        timelineNavNodeMap.put( timeline.getId(), timelineNavNode);
        jGoDocument.addObjectAtTail( timelineNavNode);
      }
      addTimelineNavNode( timelineNavNode);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addTimelineNavNodes

  /**
   * <code>addTimelineNavNode</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   */
  protected void addTimelineNavNode( TimelineNavNode timelineNavNode) {
    if (isDebugPrint) {
      System.err.println( "add timelineNavNode " +
                          timelineNavNode.getTimeline().getId());
    }
    if (! timelineNavNode.inLayout()) {
      timelineNavNode.setInLayout( true);
    }
  } // end addTimelineNavNode

  /**
   * <code>removeTimelineNavNodes</code>
   *
   * @param objectNavNode - <code>ModelClassNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeTimelineNavNodes( ModelClassNavNode objectNavNode) {
    boolean areNodesChanged = false;
    List timelineList = objectNavNode.getObject().getComponentList();
    Iterator timelineIterator = timelineList.iterator();
    while (timelineIterator.hasNext()) {
      PwTimeline timeline = (PwTimeline) timelineIterator.next();
      TimelineNavNode timelineNavNode =
        (TimelineNavNode) timelineNavNodeMap.get( timeline.getId());
      if ((timelineNavNode != null) && timelineNavNode.inLayout() &&
          (timelineNavNode.getObjectLinkCount() == 0) &&
          (timelineNavNode.getSlotLinkCount() == 0)) {
        removeTimelineNavNode( timelineNavNode);
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeTimelineNavNodes

  /**
   * <code>removeTimelineNavNode</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   */
  protected void removeTimelineNavNode( TimelineNavNode timelineNavNode) {
    if (isDebugPrint) {
      System.err.println( "remove timelineNavNode " +
                          timelineNavNode.getTimeline().getId());
    }
    timelineNavNode.setInLayout( false);
    timelineNavNode.resetNode( isDebugPrint);
  } // end removeTimelineNavNode


  /**
   * <code>addNavigatorLink</code>
   *
   * @param fromNode - <code>ExtendedBasicNode</code> - 
   * @param toNode - <code>ExtendedBasicNode</code> - 
   * @param sourceNode - <code>ExtendedBasicNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean addNavigatorLink( ExtendedBasicNode fromNode, ExtendedBasicNode toNode,
                                      ExtendedBasicNode sourceNode) {
    BasicNodeLink link = null;
    boolean areLinksChanged = false;
    String linkType = "";
    if (fromNode instanceof ModelClassNavNode) {
      link = addObjectToTimelineNavLink( (ModelClassNavNode) fromNode,
                                         (TimelineNavNode) toNode, sourceNode);
      linkType = "OtoTi";

    } else if (fromNode instanceof TimelineNavNode) {
      link = addTimelineToSlotNavLink( (TimelineNavNode) fromNode,
                                       (SlotNavNode) toNode, sourceNode);
      linkType = "TitoS";

    } else if (fromNode instanceof SlotNavNode) {
      link = ((SlotNavNode) fromNode).addSlotToTokenNavLink( (TokenNavNode) toNode,
                                                             sourceNode);
      linkType = "StoTo";
    } else if ((fromNode instanceof TokenNavNode) &&
               (toNode instanceof TokenNavNode)) {
      link = ((TokenNavNode) fromNode).addTokenToTokenNavLink( (TokenNavNode) fromNode,
                                                               (TokenNavNode) toNode,
                                                               sourceNode);
      linkType = "TtoT";
    } else if (fromNode instanceof TokenNavNode) {
      link = ((TokenNavNode) fromNode).addTokenToVariableNavLink( (VariableNavNode) toNode,
                                                                  sourceNode);
      linkType = "TtoV";
    } else if (fromNode instanceof VariableNavNode) {
      link = ((VariableNavNode) fromNode).addVariableToConstraintNavLink
        ( (ConstraintNavNode) toNode, sourceNode);
      linkType = "VtoC";
    }
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
  } // end addNavigatorLink

  /**
   * <code>addObjectToTimelineNavLinks</code>
   *
   * @param objectNavNode - <code>ModelClassNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addObjectToTimelineNavLinks( ModelClassNavNode objectNavNode) {
    boolean areLinksChanged = false;
    List timelineList = objectNavNode.getObject().getComponentList();
    Iterator timelineIterator = timelineList.iterator();
    while (timelineIterator.hasNext()) {
      PwTimeline timeline = (PwTimeline) timelineIterator.next();
      TimelineNavNode timelineNavNode =
        (TimelineNavNode) timelineNavNodeMap.get( timeline.getId());
      if ((timelineNavNode != null) && timelineNavNode.inLayout()) {
        if (addNavigatorLink( objectNavNode, timelineNavNode, objectNavNode)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end addObjectToTimelineNavLinks

  private BasicNodeLink addObjectToTimelineNavLink( ModelClassNavNode objectNavNode,
                                                    TimelineNavNode timelineNavNode,
                                                    ExtendedBasicNode sourceNode) {
    BasicNodeLink returnLink = null;
    String linkName = objectNavNode.getObject().getId().toString() + "->" +
      timelineNavNode.getTimeline().getId().toString();
    BasicNodeLink link = (BasicNodeLink) navLinkMap.get( linkName);
    if (link == null) {
      link = new BasicNodeLink( objectNavNode, timelineNavNode, linkName);
      link.setArrowHeads(false, true);
      objectNavNode.incrTimelineLinkCount();
      timelineNavNode.incrObjectLinkCount();
      returnLink = link;
      navLinkMap.put( linkName, link);
      if (isDebugPrint) {
        System.err.println( "add object=>timeline link " + linkName);
      }
    } else {
      if (! link.inLayout()) {
        link.setInLayout( true);
      }
      link.incrLinkCount();
      objectNavNode.incrTimelineLinkCount();
      timelineNavNode.incrObjectLinkCount();
      if (isDebugPrint) {
        System.err.println( "OtoTi1 incr link: " + link.toString() + " to " +
                            link.getLinkCount());
      }
    }
    return returnLink;
  } // end addObjectToTimelineLink


  /**
   * <code>removeObjectToTimelineNavLinks</code>
   *
   * @param objectNavNode - <code>ModelClassNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeObjectToTimelineNavLinks( ModelClassNavNode objectNavNode) {
    boolean areLinksChanged = false;
    List timelineList = objectNavNode.getObject().getComponentList();
    Iterator timelineIterator = timelineList.iterator();
    while (timelineIterator.hasNext()) {
      PwTimeline timeline = (PwTimeline) timelineIterator.next();
      TimelineNavNode timelineNavNode =
        (TimelineNavNode) timelineNavNodeMap.get( timeline.getId());
      if ((timelineNavNode != null) && timelineNavNode.inLayout()) {
        String linkName = objectNavNode.getObject().getId().toString() + "->" +
          timelineNavNode.getTimeline().getId().toString();
        BasicNodeLink link = (BasicNodeLink) navLinkMap.get( linkName);
        if ((link != null) && link.inLayout() &&
            removeObjectToTimelineNavLink( link, objectNavNode, timelineNavNode)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end removeObjectToTimelineLinks

  private boolean removeObjectToTimelineNavLink( BasicNodeLink link,
                                                 ModelClassNavNode objectNavNode,
                                                 TimelineNavNode timelineNavNode) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    objectNavNode.decTimelineLinkCount();
    timelineNavNode.decObjectLinkCount();
    if (isDebugPrint) {
      System.err.println( "OtoTi dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebugPrint) {
        System.err.println( "removeObjectToTimelineNavLink: " + link.toString());
      }
      link.setInLayout( false);
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeObjectToTimelineNavLink

  // *********************************************** objectNavNodes

  /**
   * <code>addObjectNavNodes</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addObjectNavNodes( TimelineNavNode timelineNavNode) {
    boolean areNodesChanged = false;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      if (object instanceof PwTimeline) {
        PwTimeline timeline = (PwTimeline) object;
        if (timeline.getId().equals( timelineNavNode.getTimeline().getId())) {
          // System.err.println( "parent " + timeline.getParent());
          // System.err.println( "children " + timeline.getComponentList());
          PwObject parentObject = timeline.getParent();
          if (parentObject != null) {
            addObjectNavNode( parentObject);
            areNodesChanged = true;
          }
          Iterator childObjectItr = timeline.getComponentList().iterator();
          while (childObjectItr.hasNext()) {
            PwObject childObject = (PwObject) childObjectItr.next();
            addObjectNavNode( childObject);
            areNodesChanged = true;
          }
        }
      }
    }
    return areNodesChanged;
  } // end addObjectNavNodes

  private void addObjectNavNode( PwObject object) {
    boolean isDraggable = true;
    ModelClassNavNode objectNavNode =
      (ModelClassNavNode) objectNavNodeMap.get( object.getId());
    if (objectNavNode == null) {
      objectNavNode =
        new ModelClassNavNode( object, 
                               new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                          ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                               ColorMap.getColor( ViewConstants.OBJECT_BG_COLOR),
                               isDraggable, this);
      objectNavNodeMap.put( object.getId(), objectNavNode);
      jGoDocument.addObjectAtTail( objectNavNode);
    }
    if (isDebugPrint) {
      System.err.println( "add objectNavNode " +
                          objectNavNode.getObject().getId());
    }
    if (! objectNavNode.inLayout()) {
      objectNavNode.setInLayout( true);
    }
  } // end addObjectNavNode


  /**
   * <code>removeObjectNavNodes</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeObjectNavNodes( TimelineNavNode timelineNavNode) {
    boolean areNodesChanged = false;
    ModelClassNavNode parentNavNode =
      (ModelClassNavNode) objectNavNodeMap.get( timelineNavNode.getTimeline().getParentId());
    if ((parentNavNode != null) && removeObjectNavNode( parentNavNode)) {
      areNodesChanged = true;
    }
    Iterator childObjectItr = timelineNavNode.getTimeline().getComponentList().iterator();
    while (childObjectItr.hasNext()) {
      PwObject childObject = (PwObject) childObjectItr.next();
      ModelClassNavNode childNavNode =
        (ModelClassNavNode) objectNavNodeMap.get( childObject.getId());
      if (removeObjectNavNode( childNavNode)) {
         areNodesChanged = true;
      }
    }
    return areNodesChanged;
  }

  private boolean removeObjectNavNode( ModelClassNavNode objectNavNode) {
    boolean areNodesChanged = false;
    if ((objectNavNode != null) && objectNavNode.inLayout() &&
        (objectNavNode.getTimelineLinkCount() == 0)) {
      areNodesChanged = true;
      if (isDebugPrint) {
        System.err.println( "remove objectNavNode " + objectNavNode.getObject().getId());
      }
      objectNavNode.setInLayout( false);
      objectNavNode.resetNode( isDebugPrint);
    }
    return areNodesChanged;
  } // end removeObjectNavNode

  /**
   * <code>addObjectToTimelineNavLinks</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addObjectToTimelineNavLinks( TimelineNavNode timelineNavNode) {
    boolean areLinksChanged = false;
    ModelClassNavNode objectNavNode =
      (ModelClassNavNode) objectNavNodeMap.get( timelineNavNode.getTimeline().getParentId());
    if ((objectNavNode != null) && objectNavNode.inLayout()) {
      if (addNavigatorLink( objectNavNode, timelineNavNode, timelineNavNode)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end addObjectToTimelineNavLinks


  /**
   * <code>removeObjectToTimelineNavLinks</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeObjectToTimelineNavLinks( TimelineNavNode timelineNavNode) {
    boolean areLinksChanged = false;
    ModelClassNavNode objectNavNode =
      (ModelClassNavNode) objectNavNodeMap.get( timelineNavNode.getTimeline().getParentId());
    if ((objectNavNode != null) && objectNavNode.inLayout()) {
      String linkName = objectNavNode.getObject().getId().toString() + "->" +
        timelineNavNode.getTimeline().getId().toString();
      BasicNodeLink link = (BasicNodeLink) navLinkMap.get( linkName);
      if ((link != null) && link.inLayout() &&
          removeObjectToTimelineNavLink( link, objectNavNode, timelineNavNode)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end removeObjectToTimelineNavLinks


  // ***********************************************slotNavNode

  /**
   * <code>addSlotNavNodes</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addSlotNavNodes( TimelineNavNode timelineNavNode) {
    boolean areNodesChanged = false, isDraggable = true;
    PwTimeline timeline = timelineNavNode.getTimeline();
    Iterator slotIterator = timeline.getSlotList().iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      SlotNavNode slotNavNode =
        (SlotNavNode) slotNavNodeMap.get( slot.getId());
      if (slotNavNode == null) {
        slotNavNode =
          new SlotNavNode( slot, 
                               new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                          ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                               getTimelineColor( timeline.getId()),
                               isDraggable, this);
        slotNavNodeMap.put( slot.getId(), slotNavNode);
        jGoDocument.addObjectAtTail( slotNavNode);
      }
      addSlotNavNode( slotNavNode);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addSlotNavNodes

  /**
   * <code>addSlotNavNode</code>
   *
   * @param slotNavNode - <code>SlotNavNode</code> - 
   */
  protected void addSlotNavNode( SlotNavNode slotNavNode) {
    if (isDebugPrint) {
      System.err.println( "add slotNavNode " +
                          slotNavNode.getSlot().getId());
    }
    if (! slotNavNode.inLayout()) {
      slotNavNode.setInLayout( true);
    }
  } // end addSlotNavNode

  /**
   * <code>removeSlotNavNodes</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeSlotNavNodes( TimelineNavNode timelineNavNode) {
    boolean areNodesChanged = false;
    PwTimeline timeline = timelineNavNode.getTimeline();
    Iterator slotIterator = timeline.getSlotList().iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      SlotNavNode slotNavNode =
        (SlotNavNode) slotNavNodeMap.get( slot.getId());
      if ((slotNavNode != null) && slotNavNode.inLayout() &&
          (slotNavNode.getTimelineLinkCount() == 0) &&
          (slotNavNode.getTokenLinkCount() == 0)) {
        removeSlotNavNode( slotNavNode);
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeSlotNavNodes

  /**
   * <code>removeSlotNavNode</code>
   *
   * @param slotNavNode - <code>SlotNavNode</code> - 
   */
  protected void removeSlotNavNode( SlotNavNode slotNavNode) {
    if (isDebugPrint) {
      System.err.println( "remove slotNavNode " +
                          slotNavNode.getSlot().getId());
    }
    slotNavNode.setInLayout( false);
    slotNavNode.resetNode( isDebugPrint);
  } // end removeSlotNavNode


  /**
   * <code>addTimelineToSlotNavLinks</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addTimelineToSlotNavLinks( TimelineNavNode timelineNavNode) {
    boolean areLinksChanged = false;
    PwTimeline timeline = timelineNavNode.getTimeline();
    Iterator slotIterator = timeline.getSlotList().iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      SlotNavNode slotNavNode =
        (SlotNavNode) slotNavNodeMap.get( slot.getId());
      if ((slotNavNode != null) && slotNavNode.inLayout()) {
        if (addNavigatorLink( timelineNavNode, slotNavNode, timelineNavNode)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end addTimelineToSlotNavLinks

  /**
   * <code>addTimelineToSlotNavLink</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @param slotNavNode - <code>SlotNavNode</code> - 
   * @param sourceNode - <code>ExtendedBasicNode</code> - 
   * @return - <code>BasicNodeLink</code> - 
   */
  protected BasicNodeLink addTimelineToSlotNavLink( TimelineNavNode timelineNavNode,
                                                    SlotNavNode slotNavNode,
                                                    ExtendedBasicNode sourceNode) {
    BasicNodeLink returnLink = null;
    String linkName = timelineNavNode.getTimeline().getId().toString() + "->" +
      slotNavNode.getSlot().getId().toString();
    BasicNodeLink link = (BasicNodeLink) navLinkMap.get( linkName);
    if (link == null) {
      link = new BasicNodeLink( timelineNavNode, slotNavNode, linkName);
      link.setArrowHeads(false, true);
      timelineNavNode.incrSlotLinkCount();
      slotNavNode.incrTimelineLinkCount();
      returnLink = link;
      navLinkMap.put( linkName, link);
      if (isDebugPrint) {
        System.err.println( "add timeline=>slot link " + linkName);
      }
    } else {
      if (! link.inLayout()) {
        link.setInLayout( true);
      }
      link.incrLinkCount();
      timelineNavNode.incrSlotLinkCount();
      slotNavNode.incrTimelineLinkCount();
      if (isDebugPrint) {
        System.err.println( "TitoS1 incr link: " + link.toString() + " to " +
                            link.getLinkCount());
      }
    }
    return returnLink;
  } // end addTimelineToSlotNavLink

  /**
   * <code>removeTimelineToSlotNavLinks</code>
   *
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeTimelineToSlotNavLinks( TimelineNavNode timelineNavNode) {
    boolean areLinksChanged = false;
    PwTimeline timeline = timelineNavNode.getTimeline();
    Iterator slotIterator = timeline.getSlotList().iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      SlotNavNode slotNavNode =
        (SlotNavNode) slotNavNodeMap.get( slot.getId());
      if ((slotNavNode != null) && slotNavNode.inLayout()) {
        String linkName = timelineNavNode.getTimeline().getId().toString() + "->" +
          slotNavNode.getSlot().getId().toString();
        BasicNodeLink link = (BasicNodeLink) navLinkMap.get( linkName);
        if ((link != null) && link.inLayout() &&
            removeTimelineToSlotNavLink( link, timelineNavNode, slotNavNode)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end removeTimelineToSlotNavLinks

  /**
   * <code>removeTimelineToSlotNavLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   * @param timelineNavNode - <code>TimelineNavNode</code> - 
   * @param slotNavNode - <code>SlotNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean removeTimelineToSlotNavLink( BasicNodeLink link,
                                                 TimelineNavNode timelineNavNode,
                                                 SlotNavNode slotNavNode) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    timelineNavNode.decSlotLinkCount();
    slotNavNode.decTimelineLinkCount();
    if (isDebugPrint) {
      System.err.println( "TitoS dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebugPrint) {
        System.err.println( "removeTimelineToSlotNavLink: " + link.toString());
      }
      link.setInLayout( false);
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeTimelineToSlotNavLink


  // ***********************************************


  private void setNodesLinksVisible() {
    List objectNodeKeyList = new ArrayList( objectNavNodeMap.keySet());
    Iterator objectNodeKeyItr = objectNodeKeyList.iterator();
    while (objectNodeKeyItr.hasNext()) {
      ModelClassNavNode objectNavNode =
        (ModelClassNavNode) objectNavNodeMap.get( (Integer) objectNodeKeyItr.next());
      if (objectNavNode.inLayout()) {
        objectNavNode.setVisible( true);
      } else {
        objectNavNode.setVisible( false);
      }
    }
    List timelineNodeKeyList = new ArrayList( timelineNavNodeMap.keySet());
    Iterator timelineNodeKeyItr = timelineNodeKeyList.iterator();
    while (timelineNodeKeyItr.hasNext()) {
      TimelineNavNode timelineNavNode =
        (TimelineNavNode) timelineNavNodeMap.get( (Integer) timelineNodeKeyItr.next());
      if (timelineNavNode.inLayout()) {
        timelineNavNode.setVisible( true);
      } else {
        timelineNavNode.setVisible( false);
      }
    }
    List slotNodeKeyList = new ArrayList( slotNavNodeMap.keySet());
    Iterator slotNodeKeyItr = slotNodeKeyList.iterator();
    while (slotNodeKeyItr.hasNext()) {
      SlotNavNode slotNavNode =
        (SlotNavNode) slotNavNodeMap.get( (Integer) slotNodeKeyItr.next());
      if (slotNavNode.inLayout()) {
        slotNavNode.setVisible( true);
      } else {
        slotNavNode.setVisible( false);
      }
    }
    List tokenNodeKeyList = new ArrayList( tokenNavNodeMap.keySet());
    Iterator tokenNodeKeyItr = tokenNodeKeyList.iterator();
    while (tokenNodeKeyItr.hasNext()) {
      TokenNavNode tokenNavNode =
        (TokenNavNode) tokenNavNodeMap.get( (Integer) tokenNodeKeyItr.next());
      if (tokenNavNode.inLayout()) {
        tokenNavNode.setVisible( true);
      } else {
        tokenNavNode.setVisible( false);
      }
    }
    List variableNodeKeyList = new ArrayList( variableNavNodeMap.keySet());
    Iterator variableNodeKeyItr = variableNodeKeyList.iterator();
    while (variableNodeKeyItr.hasNext()) {
      VariableNavNode variableNavNode =
        (VariableNavNode) variableNavNodeMap.get( (Integer) variableNodeKeyItr.next());
      if (variableNavNode.inLayout()) {
        variableNavNode.setVisible( true);
      } else {
        variableNavNode.setVisible( false);
      }
    }
    List constraintNodeKeyList = new ArrayList( constraintNavNodeMap.keySet());
    Iterator constraintNodeKeyItr = constraintNodeKeyList.iterator();
    while (constraintNodeKeyItr.hasNext()) {
      ConstraintNavNode constraintNavNode =
        (ConstraintNavNode) constraintNavNodeMap.get( (Integer) constraintNodeKeyItr.next());
      if (constraintNavNode.inLayout()) {
        constraintNavNode.setVisible( true);
      } else {
        constraintNavNode.setVisible( false);
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
  class NavigatorJGoView extends JGoView {

    /**
     * <code>NavigatorJGoView</code> - constructor 
     *
     */
    public NavigatorJGoView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick

  } // end class NavigatorJGoView

  private void mouseRightPopupMenu( Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         PlanWorks.NAVIGATOR_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                         final NavigatorView navigatorView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public void actionPerformed( ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( PlanWorks.NAVIGATOR_VIEW, partialPlan,
                                            navigatorView, viewSet, jGoView, viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem


} // end class NavigatorView
