// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintNetworkView.java,v 1.64 2004-05-29 00:31:38 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 28July03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>ConstraintNetworkView</code> - render a partial plan's network of
 *                constraints.  Create tokenNode, variableNode, & constraintNode
 *                node and link structure, and only layout the tokens.
 *                In response to mouse clicks on tokenNodes variableNodes and constraintNodes,
 *                add or remove nearest neighbor links, and nearest neighbor
 *                variable/constraint nodes, if they have no other layout links.
 *
 *                add/delete nodes/links from document and network works fine for
 *                layout, but not for add/delete/add -- links and nodes become
 *                unconnected.  To work around this, do not delete nodes/links
 *                from document, and make invisible deleted nodes/links.
 *
 *                this scheme assumes that variableNodes are not shared between
 *                tokenNodes
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNetworkView extends PartialPlanView {

  private static final int SET_VISIBLE = 1;
  private static final int VIEW_HEIGHT = 275;

  public static final double HORIZONTAL_CONSTRAINT_BAND_Y = 50.;
  public static final double HORIZONTAL_VARIABLE_BAND_Y = 150.;
  public static final double HORIZONTAL_TOKEN_BAND_Y = 250.;
  
  public static double VERTICAL_CONSTRAINT_BAND_X = 450.;
  public static double VERTICAL_VARIABLE_BAND_X = 250.;
  public static double VERTICAL_TOKEN_BAND_X= 50.;

  private static final double VERTICAL_BAND_DISTANCE = 200;
  public static final double NODE_SPACING = 10.;

  private long startTimeMSecs;
  private ViewSet viewSet;
  private ConstraintJGoView jGoView;
  private JGoDocument document;
  //private Map tokenNodeMap;
  private Map containerNodeMap;
  private Map variableNodeMap;
  private Map constraintNodeMap;
  private Map constraintLinkMap;
  private Map variableLinkMap;
  private boolean isDebugPrint;
  private boolean isDebugTraverse;
  private boolean isLayoutNeeded;
  private PartialPlanViewState s;
  private JGoArea focusNode; // ConstraintNetworkTokenNode/ConstraintNode/VariableNode
  private NewConstraintNetworkLayout newLayout;
  private boolean isStepButtonView;

  /**
   * <code>ConstraintNetworkView</code> - constructor -
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> -
   * @param viewSet - <code>ViewSet</code> - 
   */
  public ConstraintNetworkView( ViewableObject partialPlan, ViewSet viewSet) {
    super( (PwPartialPlan)partialPlan, (PartialPlanViewSet) viewSet);
    constraintNetworkViewInit(viewSet);
    s = null;
    isStepButtonView = false;
    SwingUtilities.invokeLater( runInit);
  } // end constructor

  /**
   * <code>ConstraintNetworkView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param s - <code>PartialPlanViewState</code> - 
   */
  public ConstraintNetworkView(ViewableObject partialPlan, ViewSet viewSet, 
                               PartialPlanViewState s) {
    super( (PwPartialPlan)partialPlan, (PartialPlanViewSet) viewSet);
    constraintNetworkViewInit(viewSet);
    isStepButtonView = true;
    //setState(s);
    this.s = s;
    SwingUtilities.invokeLater( runInit);
  }

  /**
   * <code>ConstraintNetworkView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public ConstraintNetworkView( ViewableObject partialPlan, ViewSet viewSet,
                                ViewListener viewListener) {
    super( (PwPartialPlan)partialPlan, (PartialPlanViewSet) viewSet);
    constraintNetworkViewInit(viewSet);
    s = null;
    isStepButtonView = false;
    if (viewListener != null) {
      addViewListener( viewListener);
    }
    SwingUtilities.invokeLater( runInit);
  } // end constructor

  private void constraintNetworkViewInit(ViewSet viewSet) {
    this.viewSet = (PartialPlanViewSet) viewSet;
    variableNodeMap = new HashMap();
    constraintNodeMap = new HashMap();
    constraintLinkMap = new HashMap();
    variableLinkMap = new HashMap();
    containerNodeMap = new HashMap();
    ViewListener viewListener = null;
    viewFrame = viewSet.openView( this.getClass().getName(), viewListener);
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());
    //isDebugPrint = true;
    isDebugPrint = false;

    //isDebugTraverse = true;
    isDebugTraverse = false;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new ConstraintJGoView( this);
    jGoView.addViewListener(createViewListener());
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);
  }

  public PartialPlanViewState getState() {
    return new ConstraintNetworkViewState(this);
  }
  public void setState(PartialPlanViewState s) {
    super.setState(s);
    if(s == null) {
      return;
    }
    zoomFactor = s.getCurrentZoomFactor();
    boolean isSetState = true;
    zoomView( jGoView, isSetState, this);
    int penWidth = getOpenJGoPenWidth( zoomFactor);

    ConstraintNetworkViewState state = (ConstraintNetworkViewState) s;
    ListIterator idIterator = state.getModContainers().listIterator();
    while(idIterator.hasNext()) {
      VariableContainerNode node = 
        (VariableContainerNode) containerNodeMap.get((Integer)idIterator.next());
      if(node != null) {
        addVariableNodes(node);
        addVariableToContainerLinks(node);
        node.setAreNeighborsShown(true);
        node.setPen( new JGoPen( JGoPen.SOLID, penWidth,  ColorMap.getColor( "black")));
      }
    }
    idIterator = state.getModVariables().listIterator();
    while(idIterator.hasNext()) {
      VariableNode node = (VariableNode) variableNodeMap.get((Integer)idIterator.next());
      if(node != null) {
        addConstraintNodes(node);
        addContainerAndConstraintToVariableLinks(node);
        node.setAreNeighborsShown(true);
        node.setPen( new JGoPen( JGoPen.SOLID, penWidth,  ColorMap.getColor( "black")));
      }
    }
    idIterator = state.getModConstraints().listIterator();
    while(idIterator.hasNext()) {
      ConstraintNode node = (ConstraintNode) constraintNodeMap.get((Integer)idIterator.next());
      if(node != null) {
        addVariableNodes(node);
        addConstraintToVariableLinks(node);
        node.setAreNeighborsShown(true);
        node.setPen( new JGoPen( JGoPen.SOLID, penWidth,  ColorMap.getColor( "black")));
      }
    }
    if(state.layoutHorizontal()) {
      newLayout.setLayoutHorizontal();
    }
    else {
      newLayout.setLayoutVertical();
    }
  }

  Runnable runInit = new Runnable() {
      public void run() {
        init();
      }
    };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use runInit in constructor
   */
  public void init() {
    handleEvent(ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for ConstraintNetworkView instance to become displayable
		if(!displayableWait()) {
			return;
		}

    this.computeFontMetrics( this);

    document = jGoView.getDocument();
    document.addDocumentListener(createDocListener());
    createVerticalScrollBarMaintainer();

    long t1 = System.currentTimeMillis();
    createContainerNodes();
    newLayout = new NewConstraintNetworkLayout(getContainerNodeList(), this);
    setState(s);
    s = null;
    System.err.println("createTokenNodes took " + (System.currentTimeMillis() - t1) + "ms");
    // setVisible( true | false) depending on ContentSpec
    setNodesLinksVisible();

    double maxTokenWidth = 0.;
    Iterator tokenIterator = containerNodeMap.values().iterator();
    while(tokenIterator.hasNext()) {
      VariableContainerNode node = (VariableContainerNode) tokenIterator.next();
      node.discoverLinkage();
      node.connectNodes(containerNodeMap);
      if(node.getSize().getWidth() > maxTokenWidth) {
        maxTokenWidth = node.getSize().getWidth();
      }
    }

    VERTICAL_TOKEN_BAND_X = (maxTokenWidth / 2) + NODE_SPACING;
    VERTICAL_VARIABLE_BAND_X = VERTICAL_TOKEN_BAND_X + VERTICAL_BAND_DISTANCE;
    VERTICAL_CONSTRAINT_BAND_X = VERTICAL_VARIABLE_BAND_X + VERTICAL_BAND_DISTANCE;

    newLayout.performLayout();

    // this constricts, unduly, the width of the frame, for small numbers of tokens
//     Rectangle documentBounds = jGoView.getDocument().computeBounds();
//     jGoView.getDocument().setDocumentSize( (int) documentBounds.getWidth() +
//                                            (ViewConstants.TIMELINE_VIEW_X_INIT * 2),
//                                            (int) documentBounds.getHeight() +
//                                            (ViewConstants.TIMELINE_VIEW_Y_INIT * 2));
    if (! isStepButtonView) {
      expandViewFrame( viewFrame, (int) jGoView.getDocumentSize().getWidth(), VIEW_HEIGHT);
    }
    addStepButtons( jGoView);
    if (! isStepButtonView) {
      expandViewFrameForStepButtons( viewFrame, jGoView);
    }
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... " + ViewConstants.CONSTRAINT_NETWORK_VIEW + " elapsed time: " +
                        (stopTimeMSecs -
                         PlanWorks.getPlanWorks().getViewRenderingStartTime
                         ( ViewConstants.CONSTRAINT_NETWORK_VIEW)) + " msecs.");
    startTimeMSecs = 0L;
    isLayoutNeeded = false;
    focusNode = null;

    handleEvent(ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
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
      try {
        ViewGenerics.setRedrawCursor( viewFrame);
        redrawView();
      } finally {
        ViewGenerics.resetRedrawCursor( viewFrame);
      }
    } //end run

  } // end class RedrawViewThread

  private void redrawView() {
    handleEvent(ViewListener.EVT_REDRAW_BEGUN_DRAWING);
    // prevent user from seeing intermediate layouts
    this.setVisible( false);
    System.err.println( "Redrawing Constraint Network View ...");
    if (startTimeMSecs == 0L) {
      startTimeMSecs = System.currentTimeMillis();
    }
    // setVisible(true | false) depending on keys
    setNodesLinksVisible();

    // content spec apply/reset do not change layout, only ConstraintNetworkTokenNode/
    // variableNode/constraintNode opening/closing
    if (isLayoutNeeded) {
      if (isDebugPrint) {
        //network.validateConstraintNetwork();
      }
      newLayout.performLayout();
      // do not highlight node, if it has been removed
      boolean isHighlightNode = ((focusNode instanceof VariableContainerNode) ||
                                 ((focusNode instanceof VariableNode) &&
                                  (((VariableNode) focusNode).inLayout())) ||
                                 ((focusNode instanceof ConstraintNode) &&
                                  (((ConstraintNode) focusNode).inLayout())));
      NodeGenerics.focusViewOnNode( focusNode, isHighlightNode, jGoView);
      isLayoutNeeded = false;
    }
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... " + ViewConstants.CONSTRAINT_NETWORK_VIEW + " elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    startTimeMSecs = 0L;
    this.setVisible( true);
    handleEvent(ViewListener.EVT_REDRAW_ENDED_DRAWING);
  } // end redrawView


  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.document;
  }

  /**
   * <code>getJGoView</code> - 
   *
   * @return - <code>JGoView</code> - 
   */
  public JGoView getJGoView()  {
    return jGoView;
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
   * <code>getContainerNodeList</code> - used by PlanWorksTest
   *
   * @return - <code>List</code> - 
   */
  public List getContainerNodeList() {
    return new ArrayList(containerNodeMap.values());
  }

  /**
   * <code>getConstraintNodeList</code> - used by PlanWorksTest
   *
   * @return - <code>List</code> - 
   */
  public List getConstraintNodeList() {
    //return constraintNodeList;
    return new ArrayList(constraintNodeMap.values());
  }

  /**
   * <code>getVariableNodeList</code> - used by PlanWorksTest
   *
   * @return - <code>List</code> - 
   */
  public List getVariableNodeList() {
    //return variableNodeList;
    return new ArrayList(variableNodeMap.values());
  }

  /**
   * <code>setFocusNode</code>
   *
   * @param node - <code>JGoArea</code> - 
   */
  public void setFocusNode( JGoArea node) {
    this.focusNode = node;
  }

  /**
   * <code>getNewLayout</code>
   *
   * @return - <code>NewConstraintNetworkLayout</code> - 
   */
  protected NewConstraintNetworkLayout getNewLayout() {
    return newLayout;
  }

  /**
   * <code>setStartTimeMSecs</code>
   *
   * @param msecs - <code>long</code> - 
   */
  protected void setStartTimeMSecs( long msecs) {
    startTimeMSecs = msecs;
  }

  private void createVerticalScrollBarMaintainer() {
    // jGoView initially consists of a row of token nodes at a large y value,
    // empty space above it -- if the window is resized to cover up the token
    // nodes, the vertical scroll bar goes away, and the tokens are lost
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT;
    JGoStroke hiddenLine = new JGoStroke();
    hiddenLine.addPoint( x, y);
    hiddenLine.addPoint( x, y + (int) HORIZONTAL_TOKEN_BAND_Y);
    // make mark invisible
    hiddenLine.setPen( new JGoPen( JGoPen.SOLID, 1, ViewConstants.VIEW_BACKGROUND_COLOR));
    hiddenLine.setDraggable(false);
    hiddenLine.setResizable(false);
    hiddenLine.setSelectable(false);
    
    document.addObjectAtTail( hiddenLine);
  } // end createVerticalScrollBarMaintainer

  private void createContainerNodes() {
    boolean isDraggable = true;
    //Map timelineIndexMap = createTimelineColorMap();
    int x = 0;
    int y = 0;
    long tokenTime = 0L;
    long varTime = 0L;
    long constrTime = 0L;
    
    ListIterator tokenIterator = partialPlan.getTokenList().listIterator();
    while(tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      Color backgroundColor = null;
      if(token.isFree()) {
        backgroundColor = ViewConstants.FREE_TOKEN_BG_COLOR;
      }
      else {
        backgroundColor = getTimelineColor( token.getParentId());
      }
      long t1 = System.currentTimeMillis();
      ConstraintNetworkTokenNode tokenNode =
        new ConstraintNetworkTokenNode( token, partialPlan.getSlot(token.getSlotId()), 
                                        new Point( x, y), backgroundColor, token.isFree(), 
                                        isDraggable, this);
      tokenTime += System.currentTimeMillis() - t1;
      containerNodeMap.put(token.getId(), tokenNode);
      document.addObjectAtTail(tokenNode);
    }
    ListIterator objectIterator = partialPlan.getObjectList().listIterator();
    while(objectIterator.hasNext()) {
      PwObject obj = (PwObject) objectIterator.next();
      if(!obj.getVariables().isEmpty()) {
        Color backgroundColor = getTimelineColor(obj.getId());
        VariableContainerNode node = null;
        if(obj instanceof PwTimeline) {
          node = new ConstraintNetworkTimelineNode((PwTimeline) obj, new Point(x, y),
                                                   backgroundColor, isDraggable, this);
        }
        else if(obj instanceof PwResource) {
          node = new ConstraintNetworkResourceNode((PwResource) obj, new Point(x, y),
                                                   backgroundColor, isDraggable, this);
        }
        else {
          node = new ConstraintNetworkObjectNode(obj, new Point(x, y), isDraggable, this);
        }
        containerNodeMap.put(obj.getId(), node);
        document.addObjectAtTail((JGoObject)node);
      }
    }
  }

  private void createVarsAndLinksForContainer(VariableContainerNode parentNode) {
    boolean isDraggable = true;
    int x = 0, y = 0;
    PwVariableContainer parent = parentNode.getContainer();
    ListIterator varIterator = parent.getVariables().listIterator();
    while(varIterator.hasNext()) {
      PwVariable var = (PwVariable) varIterator.next();
      if(var != null) {
        VariableNode varNode;
        if((varNode = getVariableNode(var.getId())) == null) {
          varNode = new VariableNode(var, parentNode, new Point(x, y), parentNode.getColor(),
                                     isDraggable, this);
          variableNodeMap.put(var.getId(), varNode);
          document.addObjectAtTail(varNode);
        }
        varNode.addContainerNode(parentNode);
        parentNode.addVariableNode(varNode);
        //document.removeObject(varNode);
        
        String linkName = var.getId() + "->" + parent.getId();
        BasicNodeLink link;
        if((link = getVariableLink(linkName)) == null) {
          link = new BasicNodeLink(varNode, (BasicNode) parentNode, linkName);
          variableLinkMap.put(link.getLinkName(), link);
          document.addObjectAtHead(link);
          //document.insertObjectBefore(document.findObject(tokenNode), link);
        }
        varNode.addLink(link);
        //document.removeObject(link);
      }
    }
  }


  private void createConstrsAndLinksForVar(VariableNode varNode) {
    boolean isDraggable = true;
    int x = 0, y = 0;
    PwVariable var = varNode.getVariable();
    ListIterator constrIterator = var.getConstraintList().listIterator();
    while(constrIterator.hasNext()) {
      PwConstraint constr = (PwConstraint) constrIterator.next();
      if(constr != null) {
        ConstraintNode constrNode;
        if((constrNode = getConstraintNode(constr.getId())) == null) {
          constrNode = new ConstraintNode(constr, varNode, new Point(x, y), varNode.getColor(),
                                          true, isDraggable, this);
          constraintNodeMap.put(constr.getId(), constrNode);
          document.addObjectAtTail(constrNode);
        }
        constrNode.addVariableNode(varNode);
        varNode.addConstraintNode(constrNode);
        //document.removeObject(constrNode);
        
        BasicNodeLink link;
        String linkName = constr.getId() + "->" + var.getId();
        if((link = getConstraintLink(linkName)) == null) {
          link = new BasicNodeLink(constrNode, varNode, linkName);
          document.addObjectAtHead(link);
          //document.insertObjectBefore(document.findObject(varNode), link);
        }
        constraintLinkMap.put(linkName, link);
        constrNode.addLink(link);
        //document.removeObject(link);
      }
    }
  }

  private void createVarNodesAndLinksForConstr(ConstraintNode constrNode) {
    boolean isDraggable = true;
    int x = 0, y = 0;
    PwConstraint constr = constrNode.getConstraint();
    ListIterator varIterator = constr.getVariablesList().listIterator();
    while(varIterator.hasNext()) {
      PwVariable var = (PwVariable) varIterator.next();
      if(!(var.getParent() instanceof PwVariableContainer)) {
        continue;
      }
      PwVariableContainer parent = (PwVariableContainer) var.getParent();
      VariableContainerNode parentNode = getContainerNode(parent.getId());
      if(var != null) {
        VariableNode varNode;
        if((varNode = getVariableNode(var.getId())) == null) {
          varNode = new VariableNode(var, parentNode, new Point(x, y), parentNode.getColor(),
                                     isDraggable, this);
          variableNodeMap.put(var.getId(), varNode);
          document.addObjectAtTail(varNode);
        }
        parentNode.addVariableNode(varNode);
        constrNode.addVariableNode(varNode);
        varNode.addConstraintNode(constrNode);
        varNode.addContainerNode(parentNode);
        //document.removeObject(varNode);
        
        String link1Name = constr.getId() + "->" + var.getId();
        String link2Name = var.getId() + "->" + parent.getId();
        BasicNodeLink link1, link2;
        if((link1 = getConstraintLink(link1Name)) == null) {
          link1 = new BasicNodeLink(constrNode, varNode, link1Name);
          constraintLinkMap.put(link1Name, link1);
          //document.insertObjectBefore(document.findObject(tokenNode), link1);
          document.addObjectAtHead(link1);
        }
        if((link2 = getVariableLink(link2Name)) == null) {
          link2 = new BasicNodeLink(varNode, (BasicNode) parentNode, link2Name);
          variableLinkMap.put(link2Name, link2);
          document.addObjectAtHead(link2);
          //document.insertObjectBefore(document.findObject(tokenNode), link2);
        }
        constrNode.addLink(link1);
        varNode.addLink(link2);
        //document.removeObject(link1);
        //document.removeObject(link2);
      }
    }
  }

  private VariableContainerNode getContainerNode(Integer nodeId) {
    return (VariableContainerNode) containerNodeMap.get(nodeId);
  }

  private VariableNode getVariableNode( Integer nodeId) {
    return (VariableNode) variableNodeMap.get(nodeId);
  } // end getVariableNode

  private ConstraintNode getConstraintNode( Integer nodeId) {
    return (ConstraintNode) constraintNodeMap.get(nodeId);
  } // end getConstraintNode
 
  private BasicNodeLink getConstraintLink( String linkName) {
    return (BasicNodeLink) constraintLinkMap.get(linkName);
  } // end getConstraintLink

  private BasicNodeLink getVariableLink( String linkName) {
    return (BasicNodeLink) variableLinkMap.get(linkName);
  } // end getVariableLink

  /**
   * <code>addVariableNodes</code>
   *
   * @param tokenNode - <code>TokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addVariableNodes( VariableContainerNode parentNode) { 
    boolean areNodesChanged = false;
    if(parentNode.getVariableNodes().isEmpty() || 
       parentNode.getContainer().getVariables().size() != 
       parentNode.getVariableNodes().size()) {
      createVarsAndLinksForContainer(parentNode);
    }
    Iterator variableNodeItr = parentNode.getVariableNodes().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (! variableNode.inLayout()) {
        if (isDebugPrint) {
          System.err.println( "add variableNode " + variableNode.getVariable().getId());
        }
        variableNode.setInLayout( true);
        // nodes are always in front of any links
        //document.removeObject(variableNode);
        document.addObjectAtTail( variableNode);
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end addVariableNodes( TokenNode tokenNode)

  /**
   * <code>addConstraintNodes</code>
   *
   * @param variableNode - <code>VariableNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addConstraintNodes( VariableNode variableNode) {
    boolean areNodesChanged = false;
    if(variableNode.getConstraintNodeList().isEmpty() ||
       variableNode.getVariable().getConstraintList().size() != 
       variableNode.getConstraintNodeList().size()) {
      createConstrsAndLinksForVar(variableNode);
    }
    Iterator constraintNodeItr = variableNode.getConstraintNodeList().iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      if (! constraintNode.inLayout()) {
        if (isDebugPrint) {
          System.err.println( "add constraintNode " +
                              constraintNode.getConstraint().getId());
        }
        constraintNode.setInLayout( true);
        // nodes are always in front of any links
        //document.removeObject(constraintNode);
        document.addObjectAtTail( constraintNode);
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end addConstraintNodes

  /**
   * <code>addVariableNodes</code>
   *
   * @param constraintNode - <code>ConstraintNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addVariableNodes( ConstraintNode constraintNode) {
    boolean areNodesChanged = false;
    createVarNodesAndLinksForConstr(constraintNode);
    Iterator variableNodeItr = constraintNode.getVariableNodes().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (! variableNode.inLayout()) {
        if (isDebugPrint) {
          System.err.println( "add variableNode " + variableNode.getVariable().getId());
        }
        variableNode.setInLayout( true);
        // nodes are always in front of any links
        //document.removeObject(variableNode);
        document.addObjectAtTail( variableNode);
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end addVariableNodes( ConstraintNode constraintNode)

  /**
   * <code>addTokenVariableLinks</code>
   *
   * @param tokenNode - <code>TokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addVariableToContainerLinks( VariableContainerNode parentNode) {
    boolean areLinksChanged = false;
    Iterator variableNodeItr = parentNode.getVariableNodes().iterator();
    // System.err.println( "addVariableToTokenLinks: tokenNode " +
    //                     tokenNode.getToken().getId());
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      //System.err.println( "  variableNode " + variableNode.getVariable().getId());
      if (addConstraintLink( variableNode, (BasicNode) parentNode, (BasicNode) parentNode)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end addVariableToTokenLinks

  /**
   * <code>addTokenAndConstraintToVariableLinks</code>
   *
   * @param variableNode - <code>VariableNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addContainerAndConstraintToVariableLinks( VariableNode variableNode) {
    boolean areLinksChanged = false;
    Iterator constraintNodeItr = variableNode.getConstraintNodeList().iterator();
    boolean isFirstLink = true;
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      // System.err.println( "    constraintNode " + constraintNode.getConstraint().getId());
      if (addConstraintLink( constraintNode, variableNode, variableNode)) {
        areLinksChanged = true;
      }
      if (isFirstLink) {
        incrVariableToContainerLink( variableNode);
      }
      isFirstLink = false;
    }
    Iterator parentNodeItr = variableNode.getContainerNodeList().iterator();
    while (parentNodeItr.hasNext()) {
      VariableContainerNode parentNode = (VariableContainerNode) parentNodeItr.next();
      // System.err.println( "  tokenNode " + tokenNode.getToken().getId());
      if (addConstraintLink( variableNode, (BasicNode) parentNode, variableNode)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end addTokenAndConstraintToVariableLinks

  /**
   * <code>addConstraintToVariableLinks</code>
   *
   * this does not necessarily work for constraint nodes with > 2 variables
   * no examples available at this time 20aug03
   *
   * @param constraintNode - <code>ConstraintNode</code> -
   * @return - <code>boolean</code> - 
   */
  public boolean addConstraintToVariableLinks( ConstraintNode constraintNode) {
    boolean areLinksChanged = false;
    Iterator variableNodeItr = constraintNode.getVariableNodes().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      // System.err.println( "  variableNode " + variableNode.getVariable().getId());
      if (addConstraintLink( constraintNode, variableNode, constraintNode)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end addConstraintToVariableLinks

  private boolean addConstraintLink( BasicNode fromNode, BasicNode toNode,
                                     BasicNode sourceNode) {
    BasicNodeLink link = null;
    boolean areLinksChanged = false;
    String linkType = "";
    if (fromNode instanceof ConstraintNode) {

      link = addConstraintToVariableLink( (ConstraintNode) fromNode,
                                          (VariableNode) toNode, sourceNode);
      linkType = "CtoV";

    } else if (fromNode instanceof VariableNode) {

      link = addVariableToContainerLink( (VariableNode) fromNode,
                                     (VariableContainerNode) toNode,
                                     sourceNode);

      linkType = "VtoT";
    }
    if (link != null) {
      // links are always behind any nodes
      //document.removeObject(link);
      document.addObjectAtHead( link);
      //network.addConstraintLink( link, fromNode, toNode);
      link.setInLayout( true);
      link.incrLinkCount();
      if (isDebugPrint) {
        System.err.println( linkType + " incr link: " + link.toString() + " to " +
                            link.getLinkCount());
      }
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end addConstraintLink

  private BasicNodeLink addConstraintToVariableLink( ConstraintNode constraintNode,
                                                     VariableNode variableNode,
                                                     BasicNode sourceNode) {
    String linkName = null; BasicNodeLink link = null, returnLink = null;
    linkName = constraintNode.getConstraint().getId().toString() + "->" +
      variableNode.getVariable().getId().toString();
    // Iterator conVarLinkItr = constraintLinkList.iterator();
    Iterator conVarLinkItr = constraintNode.getConstraintVariableLinkList().iterator();
    while (conVarLinkItr.hasNext()) {
      link = (BasicNodeLink) conVarLinkItr.next();
      if ((linkName.equals( link.getLinkName())) && (! link.inLayout())) {
        if (isDebugPrint) {
          System.err.println( "add constraint=>variable link " + linkName);
        }
        returnLink = link;
        constraintNode.incrVariableLinkCount();
        variableNode.incrConstraintLinkCount();
        break;
      } else if (link.inLayout() &&
                 ((VariableNode) link.getToNode()).equals( variableNode)) {
        link.incrLinkCount();
        if (isDebugPrint) {
          System.err.println( "CtoV1 incr link: " + link.toString() + " to " +
                              link.getLinkCount());
        }
      }
      
    }
    return returnLink;
  } // end addConstraintToVariableLink

  private void incrVariableToContainerLink( VariableNode variableNode) {
    Iterator varTokLinkItr = variableNode.getVariableContainerLinkList().iterator();
    while (varTokLinkItr.hasNext()) {
      BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
      if (link.inLayout() &&
          (((VariableNode) link.getFromNode()).equals( variableNode))) {
        link.incrLinkCount();
        if (isDebugPrint) {
          System.err.println( "VtoT1 incr link: " + link.toString() + " to " +
                              link.getLinkCount());
        }
      }
    }
  } // end incrConstraintToVariableLink

  private BasicNodeLink addVariableToContainerLink( VariableNode variableNode,
                                                    VariableContainerNode parentNode,
                                                    BasicNode sourceNode) {
    String linkName = null; BasicNodeLink link = null, returnLink = null;
    linkName = variableNode.getVariable().getId().toString() + "->" +
      parentNode.getContainer().getId().toString();
    // Iterator linkItr = variableLinkList.iterator();
    Iterator linkItr = variableNode.getVariableContainerLinkList().iterator();
    while (linkItr.hasNext()) {
      link = (BasicNodeLink) linkItr.next();
      if ((linkName.equals( link.getLinkName())) && (! link.inLayout())) {
        if (isDebugPrint) {
          System.err.println( "add variable=>token link " + linkName);
        }
        returnLink = link;
        variableNode.incrContainerLinkCount();
        parentNode.incrVariableLinkCount();
        break;
      } else if (link.inLayout() &&
                 ((sourceNode instanceof VariableContainerNode) ||
                  ((sourceNode instanceof VariableNode) &&
                   (! ((VariableContainerNode) link.getToNode()).equals( parentNode))))) {
        link.incrLinkCount();
        if (isDebugPrint) {
          System.err.println( "VtoT2 incr link: " + link.toString() + " to " +
                              link.getLinkCount());
        }
      }
    }
    return returnLink;
  } // end addVariableToTokenLink

  private void removeVariableNode( VariableNode variableNode) {
    if (isDebugPrint) {
      System.err.println( "remove variableNode " +
                          variableNode.getVariable().getId());
    }
    // document.removeObject( variableNode);
    //network.removeConstraintNode( variableNode);
    variableNode.setInLayout( false);
    variableNode.resetNode( isDebugPrint);
  } // end removeVariableNode

  private void removeConstraintNode( ConstraintNode constraintNode) {
    if (isDebugPrint) {
      System.err.println( "remove constraintNode " +
                          constraintNode.getConstraint().getId());
    }
    // document.removeObject( constraintNode);
    //network.removeConstraintNode( constraintNode);
    constraintNode.setInLayout( false);
    constraintNode.resetNode( isDebugPrint);
  } // end removeConstraintNode

  /**
   * <code>removeVariableNodes</code> - remove all subtrees of variable/constraint
   *                              nodes which do not have token leaves.
   *
   * @param tokenNode - <code>TokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeVariableNodes( VariableContainerNode parentNode) {
    boolean areNodesChanged = false;
    Iterator variableNodeItr = parentNode.getVariableNodes().iterator();
    //variableNodeItr = tokenNode.getVariableNodes().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (variableNode.inLayout() && (variableNode.getLinkCount() == 0)) {
        removeVariableNode( variableNode);
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeVariableNodes( TokenNode tokenNode)

  /**
   * <code>removeConstraintNodes</code> - do not remove if there are constraint to
   *                   other variable links.
   *
   * @param variableNode - <code>VariableNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeConstraintNodes( VariableNode variableNode) {
    boolean areNodesChanged = false;
    Iterator constraintNodeItr = variableNode.getConstraintNodeList().iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      if (constraintNode.inLayout() && (constraintNode.getLinkCount() == 0)) {
        removeConstraintNode( constraintNode);
        areNodesChanged = true;
      }
    }
    if (variableNode.getLinkCount() == 0) {
      removeVariableNode( variableNode);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end removeConstraintNodes

  /**
   * <code>removeVariableNodes</code> - 
   *
   * @param constraintNode - <code>ConstraintNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeVariableNodes( ConstraintNode constraintNode) {
    boolean areNodesChanged = false;
    Iterator variableNodeItr = constraintNode.getVariableNodes().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (variableNode.inLayout() && (variableNode.getLinkCount() == 0)) {
        removeVariableNode( variableNode);
        areNodesChanged = true;
      }
    }
    if (constraintNode.getVariableLinkCount() == 0) {
      removeConstraintNode( constraintNode);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end removeVariableNodes( ConstraintNode constraintNode)

  private boolean removeVariableToContainerLink( BasicNodeLink link,
                                                 VariableNode variableNode,
                                                 VariableContainerNode parentNode) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    if (isDebugPrint) {
      System.err.println( "VtoT dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebugPrint) {
        System.err.println( "removeVariableToTokenLink: " + link.toString());
      }
      //network.removeConstraintLink( link);
      // document.removeObject( link);
      link.setInLayout( false);
      variableNode.decContainerLinkCount();
      parentNode.decVariableLinkCount();
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeVariableToTokenLink

  private boolean removeConstraintToVariableLink( BasicNodeLink link,
                                                  ConstraintNode constraintNode,
                                                  VariableNode variableNode) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    if (isDebugPrint) {
      System.err.println( "CtoV dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebugPrint) {
        System.err.println( "removeConstraintToVariableLink: " + link.toString());
      }
      //network.removeConstraintLink( link);
      // document.removeObject( link);
      link.setInLayout( false);
      constraintNode.decVariableLinkCount();
      variableNode.decConstraintLinkCount();
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeConstraintToVariableLink

  /**
   * <code>removeVariableToTokenLinks</code> - unless variableNode has links to
   *                         constraintNodes
   *
   *                    called by TokenNode doMouseClick
   * @param tokenNode - <code>TokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeVariableToContainerLinks( VariableContainerNode parentNode) {
    // System.err.println( "tokenNode " + tokenNode.getToken().getId());
    boolean areLinksChanged = false;
    Iterator variableItr = parentNode.getVariableNodes().iterator();
    while (variableItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableItr.next();
      Iterator varTokLinkItr = variableNode.getVariableContainerLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
        if (link.inLayout() &&
            ((VariableContainerNode) link.getToNode()).equals(parentNode)) {
          if (removeVariableToContainerLink( link, variableNode, parentNode)) {
            areLinksChanged = true;
          }
        }
      }
    }
    return areLinksChanged;
  } // end removeVariableToTokenLinks

  /**
   * <code>removeTokenToVariableLinks</code>
   *
   *                    called by VariableNode doMouseClick
   * @param variableNode - <code>VariableNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeContainerToVariableLinks( VariableNode variableNode) {
    boolean areLinksChanged = false;
    if (removeConstraintToVariableLinks( variableNode)) {
      areLinksChanged = true;
    }
    Iterator parentNodeItr = variableNode.getContainerNodeList().iterator();
    while (parentNodeItr.hasNext()) {
      VariableContainerNode parentNode = (VariableContainerNode) parentNodeItr.next();
      Iterator varTokLinkItr = variableNode.getVariableContainerLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
        if (((VariableContainerNode) link.getToNode()).equals(parentNode) &&
            link.inLayout()) {
          if (removeVariableToContainerLink( link, variableNode, parentNode)) {
            areLinksChanged = true;
          }
        }
      }
    }
    return areLinksChanged;
  } // end removeTokenToVariableLinks

  private boolean removeConstraintToVariableLinks( VariableNode variableNode) {
    boolean areLinksChanged = false;
    Iterator constraintNodeItr = variableNode.getConstraintNodeList().iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      Iterator conVarLinkItr = constraintNode.getConstraintVariableLinkList().iterator();
      while (conVarLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) conVarLinkItr.next();
        if ((((VariableNode) link.getToNode()).equals( variableNode)) &&
            link.inLayout()) {
          if (removeConstraintToVariableLink( link, (ConstraintNode) link.getFromNode(),
                                              variableNode)) {
            areLinksChanged = true;
          }
        }
      }
    }
    return areLinksChanged;
  } // end removeConstraintToVariableLinks( VariableNode variableNode)

  /**
   * <code>removeConstraintToVariableLinks</code>
   *
   *                    called by VariableNode doMouseClick
   * @param constraintNode - <code>ConstraintNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean removeConstraintToVariableLinks( ConstraintNode constraintNode) {
    boolean areLinksChanged = false;
    Iterator variableNodeItr = constraintNode.getVariableNodes().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      Iterator conVarLinkItr = constraintNode.getConstraintVariableLinkList().iterator();
      while (conVarLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) conVarLinkItr.next();
        if (link.inLayout() &&
            ((VariableNode) link.getToNode()).equals( variableNode)) {
          if (removeConstraintToVariableLink( link, constraintNode, variableNode)) {
            areLinksChanged = true;
          }
        }
      }
      
    } 
    return areLinksChanged;
  }

  private boolean traverseVariableNode( VariableNode variableNode, VariableContainerNode parentNode,
                                        int action) {
    boolean isVariableLinkedToToken = false;
    //System.err.println(variableNode + " : " + variableNode.inLayout());
    if (variableNode.inLayout()) {
      if (isDebugTraverse) {
        System.err.println( "traverse to variable " + variableNode.getVariable().getId() +
                            " from token1 " + parentNode.getContainer().getId());
      }
      Iterator varTokLinkItr = variableNode.getVariableContainerLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
        //         System.err.println( "  linkName " + link.getLinkName());
        if (link.inLayout() &&
            ((VariableNode) link.getFromNode()).equals( variableNode) &&
            ((VariableContainerNode) link.getToNode()).equals( parentNode)) {
          isVariableLinkedToToken = true;
          break;
        }
      }
      if (isVariableLinkedToToken && (action == SET_VISIBLE)) {
        variableNode.setVisible( true);
      }
    }
    return isVariableLinkedToToken;
  } // end traverseVariableNode

  private void traverseContainerSubtree( VariableNode variableNode, BasicNode fromNode, 
                                         int action) {
    ConstraintNode fromConstraintNode = null;
    VariableContainerNode fromContainerNode = null;
    if (isDebugTraverse) {
      System.err.print( "traverse to variable " + variableNode.getVariable().getId());
    }
    if (fromNode instanceof ConstraintNode) {
      fromConstraintNode = (ConstraintNode) fromNode;
      if (isDebugTraverse) {
        System.err.println( " from constraint " +
                            fromConstraintNode.getConstraint().getId());
      }
    } else if (fromNode instanceof VariableContainerNode) {
      fromContainerNode = (VariableContainerNode) fromNode;
      if (isDebugTraverse) {
        System.err.println( " from token2 " +
                            fromContainerNode.getContainer().getId());
      }
    }
    Iterator varConItr = variableNode.getConstraintNodeList().iterator();
    while (varConItr.hasNext()) {
      ConstraintNode varConstraintNode = (ConstraintNode) varConItr.next();
      if ((! varConstraintNode.hasBeenVisited()) &&
          varConstraintNode.inLayout() &&
          ((fromContainerNode != null) ||
           ((fromConstraintNode != null) &&
            (! varConstraintNode.equals( fromConstraintNode))))) {
        varConstraintNode.setHasBeenVisited( true);
        if (action == SET_VISIBLE) {
          varConstraintNode.setVisible( true);
        }
        traverseContainerSubtree( varConstraintNode, variableNode, action);
      }
    }
  } // end traverseContainerSubtree

  private void traverseContainerSubtree( ConstraintNode constraintNode,
                                     VariableNode fromVariableNode, int action) {
    if (isDebugTraverse) {
      System.err.println( "traverse to constraint " +
                          constraintNode.getConstraint().getId() +
                          " from variable " + fromVariableNode.getVariable().getId());
    }
    Iterator conVarItr = constraintNode.getVariableNodes().iterator();
    while (conVarItr.hasNext()) {
      VariableNode conVariableNode = (VariableNode) conVarItr.next();
      if ((! conVariableNode.hasBeenVisited()) &&
          conVariableNode.inLayout() &&
          (! conVariableNode.equals( fromVariableNode))) {
        conVariableNode.setHasBeenVisited( true);
        if (action == SET_VISIBLE) {
          conVariableNode.setVisible( true);
        }

        traverseContainerSubtree( conVariableNode, constraintNode, action);
      }
    }
  } // end traverseContainerSubtree

  private void setNodesLinksVisible() {
    // System.err.println( "Constraint Network View - contentSpec");
    // viewSet.printSpec();
    List validContainerIds = viewSet.getValidIds();
    validTokenIds = validContainerIds;
    displayedTokenIds = new ArrayList();
    Iterator constraintNodeItr = constraintNodeMap.values().iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      constraintNode.setVisible( false);
      constraintNode.setHasBeenVisited( false);
    }
    Iterator variableNodeItr = variableNodeMap.values().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      variableNode.setVisible( false);
      variableNode.setHasBeenVisited( false);
    }

    Iterator containerNodeIterator = containerNodeMap.values().iterator();
    while (containerNodeIterator.hasNext()) {
      VariableContainerNode parentNode = (VariableContainerNode) containerNodeIterator.next();
//       System.err.println( "setNodesLinksVisible parentNode.getContainer() " +
//                           parentNode.getContainer().getClass().getName());
      if (parentNode instanceof ConstraintNetworkObjectNode ||
          parentNode instanceof ConstraintNetworkTimelineNode ||
          parentNode instanceof ConstraintNetworkResourceNode ||
          isTokenInContentSpec( (PwToken) parentNode.getContainer())) {
        parentNode.setVisible( true);
        Iterator variablesItr = parentNode.getVariableNodes().iterator();
        while (variablesItr.hasNext()) {
          VariableNode variableNode = (VariableNode) variablesItr.next();
          boolean isVariableLinkedToContainer =
            traverseVariableNode( variableNode, parentNode, SET_VISIBLE);
          if (isVariableLinkedToContainer) {
            traverseContainerSubtree( variableNode, (BasicNode) parentNode, SET_VISIBLE);
          }
        }
      } else {
        parentNode.setVisible( false);
      }
    }
    setLinksVisible();
    boolean showDialog = true;
    isContentSpecRendered( ViewConstants.CONSTRAINT_NETWORK_VIEW, showDialog);
  } // end setNodesLinksVisible

  private void setLinksVisible() {
    Iterator variableLinkItr = variableLinkMap.values().iterator();
    while (variableLinkItr.hasNext()) {
      BasicNodeLink link = (BasicNodeLink) variableLinkItr.next();
      VariableContainerNode parentNode = (VariableContainerNode) link.getToNode();
      VariableNode variableNode = (VariableNode) link.getFromNode();
      if (link.inLayout() &&
          variableNode.inLayout() && variableNode.isVisible() &&
          parentNode.isVisible()) {
        link.setVisible( true);
        if (isDebugPrint && (link.getMidLabel() != null)) {
          link.getMidLabel().setVisible( true);
        }
      } else {
        link.setVisible( false);
        if (isDebugPrint && (link.getMidLabel() != null)) {
          link.getMidLabel().setVisible( false);
        }
      }
    }
    Iterator constraintLinkItr = constraintLinkMap.values().iterator();
    while (constraintLinkItr.hasNext()) {
      BasicNodeLink link = (BasicNodeLink) constraintLinkItr.next();
      ConstraintNode fromNode = (ConstraintNode) link.getFromNode();
      VariableNode toNode = (VariableNode) link.getToNode();
      if (link.inLayout() &&
          toNode.inLayout() && toNode.isVisible() &&
          fromNode.inLayout() && fromNode.isVisible()) {
        link.setVisible( true);
        if (isDebugPrint && (link.getMidLabel() != null)) {
          link.getMidLabel().setVisible( true);
        }
      } else {
        link.setVisible( false);
        if (isDebugPrint && (link.getMidLabel() != null)) {
          link.getMidLabel().setVisible( false);
        }
      }
    }
  } // end setLinksVisible
  /**
     * <code>ConstraintJGoView</code> - subclass JGoView to add doBackgroundClick and
     *                           handle Mouse-Right functionality
     *
     * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
     *                NASA Ames Research Center - Code IC
     * @version 0.0
     */
  public class ConstraintJGoView extends JGoView {

    private ConstraintNetworkView constraintNetworkView;
    private PwPartialPlan partialPlan;


    /**
     * <code>ConstraintJGoView</code> - constructor 
     *
     * @param constraintNetworkView - <code>ConstraintNetworkView</code> - 
     */
    public ConstraintJGoView( ConstraintNetworkView constraintNetworkView) {
      super();
      this.constraintNetworkView = constraintNetworkView;
      this.partialPlan = constraintNetworkView.getPartialPlan();
    }

    /**
     * <code>resetOpenNodes</code> - reset the nodes bounding rectangles highlight width
     *                               to the current zoom factor
     *
     */
    public void resetOpenNodes() {
      int penWidth =
        constraintNetworkView.getOpenJGoPenWidth( constraintNetworkView.getZoomFactor());
      Iterator constraintNodeItr = constraintNodeMap.values().iterator();
      while (constraintNodeItr.hasNext()) {
        ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
        if (constraintNode.areNeighborsShown()) {
          constraintNode.setPen( new JGoPen( JGoPen.SOLID, penWidth,
                                             ColorMap.getColor( "black")));
        }
      }
      Iterator variableNodeItr = variableNodeMap.values().iterator();
      while (variableNodeItr.hasNext()) {
        VariableNode variableNode = (VariableNode) variableNodeItr.next();
        if (variableNode.areNeighborsShown()) {
          variableNode.setPen( new JGoPen( JGoPen.SOLID, penWidth,
                                           ColorMap.getColor( "black")));
        }
      }
      Iterator containerNodeIterator = containerNodeMap.values().iterator();
      while (containerNodeIterator.hasNext()) {
        VariableContainerNode parentNode =
          (VariableContainerNode) containerNodeIterator.next();
        if (parentNode.areNeighborsShown()) {
          parentNode.setPen( new JGoPen( JGoPen.SOLID, penWidth,
                                         ColorMap.getColor( "black")));
        }
      }
    } // end resetOpenNodes

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

    private void mouseRightPopupMenu( Point viewCoords) {
      String partialPlanName = partialPlan.getPartialPlanName();
      PwPlanningSequence planSequence =
        PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
      JPopupMenu mouseRightPopup = new JPopupMenu();

      JMenuItem tokenByKeyItem = new JMenuItem( "Find by Key");
      createNodeByKeyItem( tokenByKeyItem);
      mouseRightPopup.add( tokenByKeyItem);

      constraintNetworkView.createOpenViewItems( partialPlan, partialPlanName, planSequence,
                                                 mouseRightPopup,
                                                 ViewConstants.CONSTRAINT_NETWORK_VIEW);
    
      JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
      createOverviewWindowItem( overviewWindowItem, constraintNetworkView, viewCoords);
      mouseRightPopup.add( overviewWindowItem);

      JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
      constraintNetworkView.createRaiseContentSpecItem( raiseContentSpecItem);
      mouseRightPopup.add( raiseContentSpecItem);
    
      if (((PartialPlanViewSet) ConstraintNetworkView.this.getViewSet()).
          getActiveToken() != null) {
        JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
        createActiveTokenItem( activeTokenItem);
        mouseRightPopup.add( activeTokenItem);
      }

      JMenuItem changeLayoutItem = null;
      if(constraintNetworkView.getNewLayout().layoutHorizontal()) {
        changeLayoutItem = new JMenuItem("Vertical Layout");
      }
      else {
        changeLayoutItem = new JMenuItem("Horizontal Layout");
      }
      createChangeLayoutItem(changeLayoutItem);
      mouseRightPopup.add(changeLayoutItem);

      constraintNetworkView.createZoomItem( jGoView, zoomFactor, mouseRightPopup,
                                            ConstraintNetworkView.this);

      if (viewSet.doesViewFrameExist( ViewConstants.NAVIGATOR_VIEW)) {
        mouseRightPopup.addSeparator();
        JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
        createCloseNavigatorWindowsItem( closeWindowsItem);
        mouseRightPopup.add( closeWindowsItem);
      }
      constraintNetworkView.createAllViewItems( partialPlan, partialPlanName,
                                                planSequence, mouseRightPopup);

      ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
    } // end mouseRightPopupMenu

    private void createActiveTokenItem( JMenuItem activeTokenItem) {
      activeTokenItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            PwToken activeToken =
              ((PartialPlanViewSet) constraintNetworkView.getViewSet()).getActiveToken();
            if (activeToken != null) {
              boolean isByKey = false;
              findAndSelectContainer( activeToken, isByKey);
            }
          }
        });
    } // end createActiveTokenItem

    private void createNodeByKeyItem( JMenuItem tokenByKeyItem) {
      tokenByKeyItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            AskNodeByKey nodeByKeyDialog =
              new AskNodeByKey( "Find by Key", "key (int)", constraintNetworkView);
            Integer nodeKey = nodeByKeyDialog.getNodeKey();
            if (nodeKey != null) {
              // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());
              findAndSelectNodeKey( nodeKey);
            }
          }
        });
    } // end createNodeByKeyItem


    /**
     * <code>findAndSelectNodeKey</code> - called from CreatePartialPlanViewThread
     *
     * @param nodeKey - <code>Integer</code> - 
     */
    public void findAndSelectNodeKey( Integer nodeKey) {
      PwToken tokenToFind = partialPlan.getToken( nodeKey);
      if (tokenToFind != null) {
        boolean isByKey = true;
        findAndSelectContainer( tokenToFind, isByKey);
      } 
      else {
        PwVariable variableToFind = partialPlan.getVariable( nodeKey);
        if (variableToFind != null) {
          findAndSelectVariable( variableToFind);
        } 
        else {
          PwConstraint constraintToFind = partialPlan.getConstraint( nodeKey);
          if (constraintToFind != null) {
            findAndSelectConstraint( constraintToFind);
          }
          else {
            PwObject objToFind = partialPlan.getObject(nodeKey);
            if(objToFind != null) {
              findAndSelectContainer(objToFind, true);
            } else {
              System.err.println( "ConstaintNetworkView.findAndSelectNodeKey: nodeKey " +
                                  nodeKey.toString() + " not found");
            }
          }
        }
      }
    } // end findAndSelectNodeKey

    private void createChangeLayoutItem(JMenuItem changeLayoutItem) {
      changeLayoutItem.addActionListener( new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            NewConstraintNetworkLayout newLayout =
              constraintNetworkView.getNewLayout();
            if(newLayout.layoutHorizontal()) {
              newLayout.setLayoutVertical();
            }
            else {
              newLayout.setLayoutHorizontal();
            }
            newLayout.performLayout();
            constraintNetworkView.redraw();
          }
        });
    }

    private void findAndSelectContainer( PwVariableContainer contToFind, boolean isByKey) {
      boolean isTokenFound = false;
      boolean isHighlightNode = true;
      Iterator contNodeListItr = constraintNetworkView.getContainerNodeList().iterator();
      while (contNodeListItr.hasNext()) {
        VariableContainerNode contNode = (VariableContainerNode) contNodeListItr.next();
        if ((contNode.getContainer() != null) &&
            (contNode.getContainer().getId().equals( contToFind.getId()))) {
          System.err.println( "ConstraintNetworkView found container: " +
                              contToFind.getName() +
                              " (key=" + contToFind.getId().toString() + ")");
          NodeGenerics.focusViewOnNode( (JGoArea) contNode, isHighlightNode, this);
          isTokenFound = true;
          break;
        }
      }
      if (isTokenFound && (! isByKey)) {
        NodeGenerics.selectSecondaryNodes
          ( NodeGenerics.mapTokensToTokenNodes
            (((PartialPlanViewSet) constraintNetworkView.getViewSet()).
             getSecondaryTokens(), constraintNetworkView.getContainerNodeList()), this);
      }
      if (! isTokenFound) {
        // Content Spec filtering may cause this to happen
        String message = "Token " + contToFind.getName() +
          " (key=" + contToFind.getId().toString() + ") not found.";
        JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                       "Token Not Found in ConstraintNetworkView",
                                       JOptionPane.ERROR_MESSAGE);
        System.err.println( message);
      }
    } // end findAndSelectToken

    private void findAndSelectVariable( PwVariable variableToFind) {
      boolean isVariableFound = false;
      boolean isHighlightNode = true;
      Iterator variableNodeListItr = constraintNetworkView.getVariableNodeList().iterator();
      while (variableNodeListItr.hasNext()) {
        VariableNode variableNode = (VariableNode) variableNodeListItr.next();
        if (variableNode.getVariable().getId().equals( variableToFind.getId())) {
          System.err.println( "ConstraintNetworkView found variable: " +
                              variableToFind.getDomain().toString() + " (key=" +
                              variableToFind.getId().toString() + ")");
          if (! variableNode.inLayout()) {
            VariableContainerNode parentNode = 
              (VariableContainerNode) variableNode.getContainerNodeList().get( 0);
            System.err.println( "ConstraintNetworkView found container: " +
                                parentNode.getContainer().getName() +
                                " (key=" + parentNode.getContainer().getId() + ")");
            // open connecting token to display it
            parentNode.addContainerNodeVariables( parentNode, constraintNetworkView);
            parentNode.setAreNeighborsShown( true);
          }
          constraintNetworkView.setFocusNode( variableNode);
          NodeGenerics.focusViewOnNode( variableNode, isHighlightNode, this);
          isVariableFound = true;
          return;
        }
      }
      if (! isVariableFound) {
        // Content Spec filtering may cause this to happen
        if(variableToFind.getParent() instanceof PwObject) {
          String message = "Variable " + variableToFind.getDomain().toString() +
            " (key=" + variableToFind.getId().toString() + ") not found.";
          JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                         "Variable Not Found in ConstraintNetworkView",
                                         JOptionPane.ERROR_MESSAGE);
          System.err.println( message);
          return;
        }
        Integer parentId = variableToFind.getParent().getId();
        Iterator parentIterator = constraintNetworkView.getContainerNodeList().iterator();
        while(parentIterator.hasNext()) {
          VariableContainerNode parent = (VariableContainerNode) parentIterator.next();
          if(parent.getContainer().getId().equals(parentId)) {
            parent.addContainerNodeVariables(parent, constraintNetworkView);
            parent.setAreNeighborsShown(true);
            Iterator varIterator = parent.getVariableNodes().iterator();
            while(varIterator.hasNext()) {
              VariableNode variableNode = (VariableNode) varIterator.next();
              if(variableNode.getVariable().getId().equals(variableToFind.getId())) {
                constraintNetworkView.setFocusNode(variableNode);
                NodeGenerics.focusViewOnNode(variableNode, isHighlightNode, this);
                return;
              }
            }
          }
        }
      }
      String message = "Variable " + variableToFind.getDomain().toString() +
        " (key=" + variableToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Variable Not Found in ConstraintNetworkView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    } // end findAndSelectVariable

    private void findAndSelectConstraint( PwConstraint constraintToFind) {
      boolean isConstraintFound = false;
      boolean isHighlightNode = true;
      Iterator constraintNodeListItr = constraintNetworkView.getConstraintNodeList().iterator();
      while (constraintNodeListItr.hasNext()) {
        ConstraintNode constraintNode = (ConstraintNode) constraintNodeListItr.next();
        if (constraintNode.getConstraint().getId().equals( constraintToFind.getId())) {
          System.err.println( "ConstraintNetworkView found constraint: " +
                              constraintToFind.getName() + " (key=" +
                              constraintToFind.getId().toString() + ")");
          if (! constraintNode.inLayout()) {
            VariableNode variableNode = null;
            // look for open connected variableNode
            variableNode = getVariableNodeInLayout( constraintNode);
            if (variableNode == null) {
              variableNode = 
                (VariableNode) constraintNode.getVariableNodes().get( 0);
            }
            System.err.println( "ConstraintNetworkView found variable: " +
                                variableNode.getVariable().getDomain().toString() +
                                " (key=" + variableNode.getVariable().getId().toString() + ")");
            if (! variableNode.inLayout()) {
              VariableContainerNode parentNode = null;
              // look for open connected tokenNode
              parentNode = getOpenContainerNode( variableNode);
              if (parentNode == null) {
                parentNode = 
                  (VariableContainerNode) variableNode.getContainerNodeList().get( 0);
                // open connecting token to display variable node
                parentNode.addContainerNodeVariables( parentNode, constraintNetworkView);
                parentNode.setAreNeighborsShown( true);
              }
              System.err.println( "ConstraintNetworkView found token: " +
                                  parentNode.getContainer().getName() +
                                  " (key=" + parentNode.getContainer().getId() + ")");
            }
            // open connecting variableNode to display it
            variableNode.addVariableNodeContainersAndConstraints( variableNode,
                                                                  constraintNetworkView);
            variableNode.setAreNeighborsShown( true);
          }
          constraintNetworkView.setFocusNode( constraintNode);
          NodeGenerics.focusViewOnNode( constraintNode, isHighlightNode, this);
          isConstraintFound = true;
          break;
        }
      }
      if (! isConstraintFound) {
        // Content Spec filtering may cause this to happen
        String message = "Constraint " + constraintToFind.getName() +
          " (key=" + constraintToFind.getId().toString() + ") not found.";
        JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                       "Constraint Not Found in ConstraintNetworkView",
                                       JOptionPane.ERROR_MESSAGE);
        System.err.println( message);
      }
    } // end findAndSelectConstraint

    private VariableContainerNode getOpenContainerNode( VariableNode variableNode) {
      Iterator contNodeItr = variableNode.getContainerNodeList().iterator();
      while (contNodeItr.hasNext()) {
        VariableContainerNode parentNode = (VariableContainerNode) contNodeItr.next();
        if (parentNode.areNeighborsShown()) {
          return parentNode;
        }
      }
      return null;
    } // end getOpenTokenNode

    private VariableNode getVariableNodeInLayout( ConstraintNode constraintNode) {
      Iterator variableNodeItr = constraintNode.getVariableNodes().iterator();
      while (variableNodeItr.hasNext()) {
        VariableNode variableNode = (VariableNode) variableNodeItr.next();
        if (variableNode.inLayout()) {
          return variableNode;
        }
      }
      return null;
    } // end getVariableNodeInLayout

    private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                           final ConstraintNetworkView constraintNetworkView,
                                           final Point viewCoords) {
      overviewWindowItem.addActionListener( new ActionListener() { 
          public void actionPerformed( ActionEvent evt) {
            VizViewOverview currentOverview =
              ViewGenerics.openOverviewFrame( ViewConstants.CONSTRAINT_NETWORK_VIEW, partialPlan,
                                              constraintNetworkView,
                                              constraintNetworkView.getViewSet(),
                                              ConstraintJGoView.this, viewCoords);
            if (currentOverview != null) {
              constraintNetworkView.setOverview( currentOverview);
            }
          }
        });
    } // end createOverviewWindowItem


  } // end class ConstraintJGoView
} // end class ConstraintNetworkView








