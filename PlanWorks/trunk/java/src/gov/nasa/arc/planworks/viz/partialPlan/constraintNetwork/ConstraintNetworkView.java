// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintNetworkView.java,v 1.21 2003-12-30 00:38:47 miatauro Exp $
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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
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
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
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
  private static final int VIEW_HEIGHT = 250;

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
  //private ConstraintNetwork network;
  // tokenNodeList & tmpTokenNodeList used by JFCUnit test case
  private List tokenNodeList; // element ConstraintNetworkTokenNode
  private List tmpTokenNodeList; // element ConstraintNetworkTokenNode
  private List variableNodeList; // element VariableNode
  private List constraintNodeList; // element ConstraintNode
  private List constraintLinkList; // element BasicNodeLink
  private List variableLinkList; // element BasicNodeLink
  private boolean isDebugPrint;
  private boolean isDebugTraverse;
  private boolean isLayoutNeeded;
  private JGoArea focusNode; // ConstraintNetworkTokenNode/ConstraintNode/VariableNode
  private NewConstraintNetworkLayout newLayout;

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
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (PartialPlanViewSet) viewSet;
    tokenNodeList = null;
    tmpTokenNodeList = new ArrayList();
    variableNodeList = new ArrayList();
    constraintNodeList = new ArrayList();
    constraintLinkList = new ArrayList();
    variableLinkList = new ArrayList();

    // isDebugPrint = true;
    isDebugPrint = false;

    // isDebugTraverse = true;
    isDebugTraverse = false;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new ConstraintJGoView( this);
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);

    // print content spec
    // viewSet.printSpec();

    SwingUtilities.invokeLater( runInit);
  } // end constructor

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
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for ConstraintNetworkView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "constraintNetworkView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);

    document = jGoView.getDocument();
    //network = new ConstraintNetwork();
    createVerticalScrollBarMaintainer();
    createTokenNodes();
    // setVisible( true | false) depending on ContentSpec
    setNodesLinksVisible();

    double maxTokenWidth = 0.;
    ListIterator tokenIterator = tokenNodeList.listIterator();
    while(tokenIterator.hasNext()) {
      ConstraintNetworkTokenNode node = (ConstraintNetworkTokenNode) tokenIterator.next();
      node.discoverLinkage();
      if(node.getSize().getWidth() > maxTokenWidth) {
        maxTokenWidth = node.getSize().getWidth();
      }
    }

    VERTICAL_TOKEN_BAND_X = (maxTokenWidth / 2) + NODE_SPACING;
    VERTICAL_VARIABLE_BAND_X = VERTICAL_TOKEN_BAND_X + VERTICAL_BAND_DISTANCE;
    VERTICAL_CONSTRAINT_BAND_X = VERTICAL_VARIABLE_BAND_X + VERTICAL_BAND_DISTANCE;

    //NewConstraintNetworkLayout newLayout = 
    newLayout = new NewConstraintNetworkLayout(tokenNodeList, variableNodeList, constraintNodeList);

    //ConstraintNetworkLayout layout =
    //  new ConstraintNetworkLayout( document, network, startTimeMSecs);
    //layout.performLayout();
    newLayout.performLayout();

    Rectangle documentBounds = jGoView.getDocument().computeBounds();
    jGoView.getDocument().setDocumentSize( (int) documentBounds.getWidth() +
                                           (ViewConstants.TIMELINE_VIEW_X_INIT * 2),
                                           (int) documentBounds.getHeight() +
                                           (ViewConstants.TIMELINE_VIEW_Y_INIT * 2));
    expandViewFrame( viewSet.openView( this.getClass().getName()),
                     (int) jGoView.getDocumentSize().getWidth(), VIEW_HEIGHT);
 
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    startTimeMSecs = 0L;
    isLayoutNeeded = false;
    focusNode = null;

    addStepButtons(jGoView);
    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();
    
    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
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
      redrawView();
    } //end run

  } // end class RedrawViewThread

  private void redrawView() {
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
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
      //ConstraintNetworkLayout layout =
      //  new ConstraintNetworkLayout( document, network, startTimeMSecs);
      //layout.performLayout();
      newLayout.performLayout();

      // do not highlight node, if it has been removed
      boolean isHighlightNode = ((focusNode instanceof ConstraintNetworkTokenNode) ||
                                 ((focusNode instanceof VariableNode) &&
                                  (((VariableNode) focusNode).inLayout())) ||
                                 ((focusNode instanceof ConstraintNode) &&
                                  (((ConstraintNode) focusNode).inLayout())));
      NodeGenerics.focusViewOnNode( focusNode, isHighlightNode, jGoView);
      isLayoutNeeded = false;
    }
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    startTimeMSecs = 0L;
    this.setVisible( true);
    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
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
   * <code>getJGoView</code> - needed for PlanWorksTest
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
   * <code>getTokenNodeList</code> - used by PlanWorksTest
   *
   * @return - <code>List</code> - 
   */
  public List getTokenNodeList() {
    return tokenNodeList;
  }

  /**
   * <code>getConstraintNodeList</code> - used by PlanWorksTest
   *
   * @return - <code>List</code> - 
   */
  public List getConstraintNodeList() {
    return constraintNodeList;
  }

  /**
   * <code>getVariableNodeList</code> - used by PlanWorksTest
   *
   * @return - <code>List</code> - 
   */
  public List getVariableNodeList() {
    return variableNodeList;
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
    document.addObjectAtTail( hiddenLine);
  } // end createVerticalScrollBarMaintainer

  private void createTokenNodes() {
    boolean isDraggable = true;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT * 2;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    int timelineCnt = 0;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      Iterator timelineIterator = object.getTimelineList().iterator();
      while (timelineIterator.hasNext()) {
        int x = ViewConstants.TIMELINE_VIEW_X_INIT;
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        Color timelineColor =
          ((PartialPlanViewSet) viewSet).getColorStream().getColor( timelineCnt);

        createTokenNodesOfTimeline( timeline, x, y, timelineColor);

        y += ViewConstants.TIMELINE_VIEW_Y_DELTA;
        timelineCnt++;
      }
    }
    // free tokens
    List freeTokenList = partialPlan.getFreeTokenList();
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    // System.err.println( "token network view freeTokenList " + freeTokenList);
    Iterator freeTokenItr = freeTokenList.iterator();
    boolean isFreeToken = true;
    Color backgroundColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
    PwSlot slot = null;
    while (freeTokenItr.hasNext()) {
      PwToken token = (PwToken) freeTokenItr.next();
      ConstraintNetworkTokenNode freeTokenNode =
        new ConstraintNetworkTokenNode( token, slot, new Point( x, y), backgroundColor,
                                        isFreeToken, isDraggable, this);
      if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
        x += freeTokenNode.getSize().getWidth() * 0.5;
        freeTokenNode.setLocation( x, y);
      }
      tmpTokenNodeList.add( freeTokenNode);
      // nodes are always in front of any links
      document.addObjectAtTail( freeTokenNode);
      //network.addConstraintNode( freeTokenNode);

      createVariableAndConstraintNodes( freeTokenNode, backgroundColor, isFreeToken);
      createTokenVariableConstraintLinks( freeTokenNode);

      x += freeTokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
    }
    tokenNodeList = tmpTokenNodeList;
  } // end createTokenNodes

  private void createTokenNodesOfTimeline( PwTimeline timeline, int x, int y,
                                           Color backgroundColor) {
    boolean isFreeToken = false, isDraggable = true;
    Iterator slotIterator = timeline.getSlotList().iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      Iterator tokenIterator = slot.getTokenList().iterator();
      int tokenCnt = 0;
      while (tokenIterator.hasNext()) {
        PwToken token = (PwToken) tokenIterator.next();
        ConstraintNetworkTokenNode tokenNode =
          new ConstraintNetworkTokenNode( token, slot, new Point( x, y), backgroundColor,
                                          isFreeToken, isDraggable, this);
        if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
          x += tokenNode.getSize().getWidth() * 0.5;
          tokenNode.setLocation( x, y);
        }
        tmpTokenNodeList.add( tokenNode);
        // nodes are always in front of any links
        document.addObjectAtTail( tokenNode);
        //network.addConstraintNode( tokenNode);
          
        createVariableAndConstraintNodes( tokenNode, backgroundColor, isFreeToken);
        createTokenVariableConstraintLinks( tokenNode);

        x += tokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
        tokenCnt++;
      }
    }
  } // end createTokenNodesOfTimeline

  private void createVariableAndConstraintNodes( ConstraintNetworkTokenNode tokenNode,
                                                 Color backgroundColor,
                                                 boolean isFreeToken) {
    PwToken token = tokenNode.getToken();
    int xVar = (int) tokenNode.getLocation().getX();
    int yVar = (int) tokenNode.getLocation().getY() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
    boolean isDraggable = true;
    Iterator variableItr = token.getVariablesList().iterator();
    // System.err.println( "createVariables: " + token.getVariablesList().size());
    while (variableItr.hasNext()) {
      PwVariable variable = (PwVariable) variableItr.next();
      if (variable != null) {
        VariableNode variableNode = getVariableNode( variable.getId());
        if (variableNode == null) {
          variableNode = new VariableNode( variable, tokenNode, new Point( xVar, yVar),
                                           backgroundColor, isFreeToken, isDraggable, this);
          // System.err.println( "add variableNode " + variableNode.getVariable().getId() +
          //                     " type " + variable.getType());
          variableNodeList.add( variableNode);
        } else {
          variableNode.addTokenNode( tokenNode);
        }
        tokenNode.addVariableNode( variableNode);

        int xCon = xVar, yCon = yVar + ViewConstants.TIMELINE_VIEW_Y_DELTA;
        // System.err.println( "variable " + variable.getId());
        // System.err.println( "  constraintList " + variable.getConstraintList());
        Iterator constraintItr = variable.getConstraintList().iterator();
        while (constraintItr.hasNext()) {
          PwConstraint constraint = (PwConstraint) constraintItr.next();
          if (constraint != null) {
            ConstraintNode constraintNode = getConstraintNode( constraint.getId());
            if (constraintNode == null) {
              constraintNode =
                new ConstraintNode( constraint, variableNode, new Point( xCon, yCon),
                                    backgroundColor, isFreeToken, isDraggable, this);
              // System.err.println( "add constraintNode " +
              //                     constraintNode.getConstraint().getId());
              constraintNodeList.add( constraintNode);
              xCon += constraintNode.getSize().getWidth() * 1.5;
              constraintNode.setLocation( xCon, yCon);
            } else {
              constraintNode.addVariableNode( variableNode);
            }
            variableNode.addConstraintNode( constraintNode);
          }
        }
        xVar += variableNode.getSize().getWidth() * 1.5;
        variableNode.setLocation( xVar, yVar);
      }
    }
  } // end createVariableAndConstraintNodes

  private void createTokenVariableConstraintLinks( ConstraintNetworkTokenNode tokenNode) {
    // System.err.println( "tokenNode " + tokenNode.getToken().getId());
    Iterator variableItr = tokenNode.getVariableNodeList().iterator();
    while (variableItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableItr.next();
      // System.err.println( "  variableNode " + variableNode.getVariable().getId());
      createConstraintLink( variableNode, tokenNode);
      Iterator constraintNodeItr = variableNode.getConstraintNodeList().iterator();
      while (constraintNodeItr.hasNext()) {
        ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
        // System.err.println( "    constraintNode " + constraintNode.getConstraint().getId());
        createConstraintLink( constraintNode, variableNode);
      }
    }
  } // end createTokenVariableConstraintLinks

  private void createConstraintLink( BasicNode fromNode, BasicNode toNode) {
    String linkName = null, linkTypeName = null;
    BasicNodeLink link = null;
    if (fromNode instanceof ConstraintNode) {
      linkName =
        ((ConstraintNode) fromNode).getConstraint().getId().toString() + "->" +
        ((VariableNode) toNode).getVariable().getId().toString();
      if (getConstraintLink( linkName) != null) {
        // System.err.println( "discard duplicate constraint=>variable link " + linkName);
        return;
      }
      linkTypeName = "constraint=>variable";
      link = new BasicNodeLink( fromNode, toNode, linkName);
      constraintLinkList.add( link);
      ((ConstraintNode) fromNode).addLink( link);
    }
    if (fromNode instanceof VariableNode) {
      linkName =
        ((VariableNode) fromNode).getVariable().getId().toString() + "->" +
        ((TokenNode) toNode).getToken().getId().toString();
      if (getConstraintLink( linkName) != null) {
        // System.err.println( "discard duplicate variable=>token link " + linkName);
        return;
      }
      linkTypeName = "variable=>token";
      link = new BasicNodeLink( fromNode, toNode, linkName);
      variableLinkList.add( link);
      ((VariableNode) fromNode).addLink( link);
    }
    // System.err.println( "create " +  linkTypeName + " link " + linkName);
  } // end createConstraintLink

  private VariableNode getVariableNode( Integer nodeId) {
    Iterator variableNodeItr = variableNodeList.iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (nodeId.equals( variableNode.getVariable().getId())) {
        return variableNode;
      }
    }
    return null;
  } // end getVariableNode

  private ConstraintNode getConstraintNode( Integer nodeId) {
    Iterator constraintNodeItr = constraintNodeList.iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      if (nodeId.equals( constraintNode.getConstraint().getId())) {
        return constraintNode;
      }
    }
    return null;
  } // end getConstraintNode
 
  private BasicNodeLink getConstraintLink( String linkName) {
    Iterator linkItr = constraintLinkList.iterator();
    while (linkItr.hasNext()) {
      BasicNodeLink link = (BasicNodeLink) linkItr.next();
      // System.err.println( "getConstraintLink: linkName '" + link.getLinkName() + "'");
      if (linkName.equals( link.getLinkName())) {
        return link;
      }
    }
    return null;
  } // end getConstraintLink

  private BasicNodeLink getVariableLink( String linkName) {
    Iterator linkItr = variableLinkList.iterator();
    while (linkItr.hasNext()) {
      BasicNodeLink link = (BasicNodeLink) linkItr.next();
      // System.err.println( "getVariableLink: linkName '" + link.getLinkName() + "'");
      if (linkName.equals( link.getLinkName())) {
        return link;
      }
    }
    return null;
  } // end getVariableLink

  /**
   * <code>addVariableNodes</code>
   *
   * @param tokenNode - <code>TokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean addVariableNodes( ConstraintNetworkTokenNode tokenNode) { 
    boolean areNodesChanged = false;
    Iterator variableNodeItr = tokenNode.getVariableNodeList().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (! variableNode.inLayout()) {
        if (isDebugPrint) {
          System.err.println( "add variableNode " + variableNode.getVariable().getId());
        }
        variableNode.setInLayout( true);
        // nodes are always in front of any links
        document.addObjectAtTail( variableNode);
        //network.addConstraintNode( variableNode);
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
        document.addObjectAtTail( constraintNode);
        //network.addConstraintNode( constraintNode);
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
    Iterator variableNodeItr = constraintNode.getVariableNodeList().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (! variableNode.inLayout()) {
        if (isDebugPrint) {
          System.err.println( "add variableNode " + variableNode.getVariable().getId());
        }
        variableNode.setInLayout( true);
        // nodes are always in front of any links
        document.addObjectAtTail( variableNode);
        //network.addConstraintNode( variableNode);
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
  public boolean addVariableToTokenLinks( ConstraintNetworkTokenNode tokenNode) {
    boolean areLinksChanged = false;
    Iterator variableNodeItr = tokenNode.getVariableNodeList().iterator();
    // System.err.println( "addVariableToTokenLinks: tokenNode " +
    //                     tokenNode.getToken().getId());
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      // System.err.println( "  variableNode " + variableNode.getVariable().getId());
      if (addConstraintLink( variableNode, tokenNode, tokenNode)) {
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
  public boolean addTokenAndConstraintToVariableLinks( VariableNode variableNode) {
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
        incrVariableToTokenLink( variableNode);
      }
      isFirstLink = false;
    }
    Iterator tokenNodeItr = variableNode.getTokenNodeList().iterator();
    while (tokenNodeItr.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeItr.next();
      // System.err.println( "  tokenNode " + tokenNode.getToken().getId());
      if (addConstraintLink( variableNode, tokenNode, variableNode)) {
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
    Iterator variableNodeItr = constraintNode.getVariableNodeList().iterator();
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

      link = addVariableToTokenLink( (VariableNode) fromNode,
                                     (ConstraintNetworkTokenNode) toNode,
                                     sourceNode);
      linkType = "VtoT";
    }
    if (link != null) {
      // links are always behind any nodes
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

  private void incrVariableToTokenLink( VariableNode variableNode) {
    Iterator varTokLinkItr = variableNode.getVariableTokenLinkList().iterator();
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

  private BasicNodeLink addVariableToTokenLink( VariableNode variableNode,
                                                ConstraintNetworkTokenNode tokenNode,
                                                BasicNode sourceNode) {
    String linkName = null; BasicNodeLink link = null, returnLink = null;
    linkName = variableNode.getVariable().getId().toString() + "->" +
      tokenNode.getToken().getId().toString();
    // Iterator linkItr = variableLinkList.iterator();
    Iterator linkItr = variableNode.getVariableTokenLinkList().iterator();
    while (linkItr.hasNext()) {
      link = (BasicNodeLink) linkItr.next();
      if ((linkName.equals( link.getLinkName())) && (! link.inLayout())) {
        if (isDebugPrint) {
          System.err.println( "add variable=>token link " + linkName);
        }
        returnLink = link;
        variableNode.incrTokenLinkCount();
        tokenNode.incrVariableLinkCount();
        break;
      } else if (link.inLayout() &&
                 ((sourceNode instanceof TokenNode) ||
                  ((sourceNode instanceof VariableNode) &&
                   (! ((TokenNode) link.getToNode()).equals( tokenNode))))) {
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
  public boolean removeVariableNodes( ConstraintNetworkTokenNode tokenNode) {
    boolean areNodesChanged = false;
    Iterator variableNodeItr = tokenNode.getVariableNodeList().iterator();
    variableNodeItr = tokenNode.getVariableNodeList().iterator();
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
    Iterator variableNodeItr = constraintNode.getVariableNodeList().iterator();
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

  private boolean removeVariableToTokenLink( BasicNodeLink link,
                                             VariableNode variableNode,
                                             ConstraintNetworkTokenNode tokenNode) {
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
      variableNode.decTokenLinkCount();
      tokenNode.decVariableLinkCount();
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
  public boolean removeVariableToTokenLinks( ConstraintNetworkTokenNode tokenNode) {
    // System.err.println( "tokenNode " + tokenNode.getToken().getId());
    boolean areLinksChanged = false;
    Iterator variableItr = tokenNode.getVariableNodeList().iterator();
    while (variableItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableItr.next();
      Iterator varTokLinkItr = variableNode.getVariableTokenLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
        if (link.inLayout() &&
            ((ConstraintNetworkTokenNode) link.getToNode()).equals( tokenNode)) {
          if (removeVariableToTokenLink( link, variableNode, tokenNode)) {
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
  public boolean removeTokenToVariableLinks( VariableNode variableNode) {
    boolean areLinksChanged = false;
    if (removeConstraintToVariableLinks( variableNode)) {
      areLinksChanged = true;
    }
    Iterator tokenNodeItr = variableNode.getTokenNodeList().iterator();
    while (tokenNodeItr.hasNext()) {
      ConstraintNetworkTokenNode tokenNode =
        (ConstraintNetworkTokenNode) tokenNodeItr.next();
      Iterator varTokLinkItr = variableNode.getVariableTokenLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
        if (((ConstraintNetworkTokenNode) link.getToNode()).equals( tokenNode) &&
            link.inLayout()) {
          if (removeVariableToTokenLink( link, variableNode, tokenNode)) {
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
    Iterator variableNodeItr = constraintNode.getVariableNodeList().iterator();
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
      // this decrements/removes token->variable links when constraintNode is
      // closed -- do not do it - will 04nov03
//       Iterator varTokLinkItr = variableNode.getVariableTokenLinkList().iterator();
//       while (varTokLinkItr.hasNext()) {
//         BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
//         if (link.inLayout() &&
//             ((VariableNode) link.getFromNode()).equals( variableNode)) {
//           if (removeVariableToTokenLink( link, variableNode,
//                                          (ConstraintNetworkTokenNode) link.getToNode())) {
//             areLinksChanged = true;
//           }
//         }
//       }
    }
    return areLinksChanged;
  } // end removeConstraintToVariableLinks( ConstraintNode constraintNode) {


  private boolean traverseVariableNode( VariableNode variableNode, TokenNode tokenNode,
                                        int action) {
    boolean isVariableLinkedToToken = false;
    if (variableNode.inLayout()) {
      if (isDebugTraverse) {
        System.err.println( "traverse to variable " + variableNode.getVariable().getId() +
                            " from token1 " + tokenNode.getToken().getId());
      }
      Iterator varTokLinkItr = variableNode.getVariableTokenLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
//         System.err.println( "  linkName " + link.getLinkName());
        if (link.inLayout() &&
            ((VariableNode) link.getFromNode()).equals( variableNode) &&
            ((TokenNode) link.getToNode()).equals( tokenNode)) {
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

  private void traverseTokenSubtree( VariableNode variableNode, BasicNode fromNode,
                                     int action) {
    ConstraintNode fromConstraintNode = null;
    TokenNode fromTokenNode = null;
    if (isDebugTraverse) {
      System.err.print( "traverse to variable " + variableNode.getVariable().getId());
    }
    if (fromNode instanceof ConstraintNode) {
      fromConstraintNode = (ConstraintNode) fromNode;
      if (isDebugTraverse) {
        System.err.println( " from constraint " +
                            fromConstraintNode.getConstraint().getId());
      }
    } else if (fromNode instanceof TokenNode) {
      fromTokenNode = (TokenNode) fromNode;
      if (isDebugTraverse) {
        System.err.println( " from token2 " +
                            fromTokenNode.getToken().getId());
      }
    }
    Iterator varConItr = variableNode.getConstraintNodeList().iterator();
    while (varConItr.hasNext()) {
      ConstraintNode varConstraintNode = (ConstraintNode) varConItr.next();
      if ((! varConstraintNode.hasBeenVisited()) &&
          varConstraintNode.inLayout() &&
          ((fromTokenNode != null) ||
           ((fromConstraintNode != null) &&
            (! varConstraintNode.equals( fromConstraintNode))))) {
        varConstraintNode.setHasBeenVisited( true);
        if (action == SET_VISIBLE) {
          varConstraintNode.setVisible( true);
        }
        traverseTokenSubtree( varConstraintNode, variableNode, action);
      }
    }
  } // end traverseTokenSubtree

  private void traverseTokenSubtree( ConstraintNode constraintNode,
                                     VariableNode fromVariableNode, int action) {
    if (isDebugTraverse) {
      System.err.println( "traverse to constraint " +
                          constraintNode.getConstraint().getId() +
                          " from variable " + fromVariableNode.getVariable().getId());
    }
    Iterator conVarItr = constraintNode.getVariableNodeList().iterator();
    while (conVarItr.hasNext()) {
      VariableNode conVariableNode = (VariableNode) conVarItr.next();
      if ((! conVariableNode.hasBeenVisited()) &&
          conVariableNode.inLayout() &&
          (! conVariableNode.equals( fromVariableNode))) {
        conVariableNode.setHasBeenVisited( true);
        if (action == SET_VISIBLE) {
          conVariableNode.setVisible( true);
        }

        traverseTokenSubtree( conVariableNode, constraintNode, action);
      }
    }
  } // end traverseTokenSubtree

  private void setNodesLinksVisible() {
    // System.err.println( "Constraint Network View - contentSpec");
    // viewSet.printSpec();
    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    Iterator constraintNodeItr = constraintNodeList.iterator();
    while (constraintNodeItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
      constraintNode.setVisible( false);
      constraintNode.setHasBeenVisited( false);
    }
    Iterator variableNodeItr = variableNodeList.iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      variableNode.setVisible( false);
      variableNode.setHasBeenVisited( false);
    }

    Iterator tokenNodeIterator = tokenNodeList.iterator();
    while (tokenNodeIterator.hasNext()) {
      ConstraintNetworkTokenNode tokenNode =
        (ConstraintNetworkTokenNode) tokenNodeIterator.next();
      if (isTokenInContentSpec( tokenNode.getToken())) {
        tokenNode.setVisible( true);
        Iterator variablesItr = tokenNode.getVariableNodeList().iterator();
        while (variablesItr.hasNext()) {
          VariableNode variableNode = (VariableNode) variablesItr.next();
          boolean isVariableLinkedToToken =
            traverseVariableNode( variableNode, tokenNode, SET_VISIBLE);
          if (isVariableLinkedToToken) {
            traverseTokenSubtree( variableNode, tokenNode, SET_VISIBLE);
          }
        }
      } else {
        tokenNode.setVisible( false);
      }
    }
    setLinksVisible();
    boolean showDialog = true;
    isContentSpecRendered( PlanWorks.CONSTRAINT_NETWORK_VIEW, showDialog);
  } // end setNodesLinksVisible

  private void setLinksVisible() {
    Iterator variableLinkItr = variableLinkList.iterator();
    while (variableLinkItr.hasNext()) {
      BasicNodeLink link = (BasicNodeLink) variableLinkItr.next();
      TokenNode tokenNode = (TokenNode) link.getToNode();
      VariableNode variableNode = (VariableNode) link.getFromNode();
      if (link.inLayout() &&
          variableNode.inLayout() && variableNode.isVisible() &&
          tokenNode.isVisible()) {
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
    Iterator constraintLinkItr = constraintLinkList.iterator();
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
  class ConstraintJGoView extends JGoView {

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
      PwPlanningSequence planSequence = PlanWorks.planWorks.getPlanSequence( partialPlan);
      JPopupMenu mouseRightPopup = new JPopupMenu();

      createSteppingItems(mouseRightPopup);

      JMenuItem tokenByKeyItem = new JMenuItem( "Find by Key");
      createNodeByKeyItem( tokenByKeyItem);
      mouseRightPopup.add( tokenByKeyItem);

      constraintNetworkView.createOpenViewItems( partialPlan, partialPlanName, planSequence,
                                                 mouseRightPopup,
                                                 PlanWorks.CONSTRAINT_NETWORK_VIEW);
    
      JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
      createOverviewWindowItem( overviewWindowItem, constraintNetworkView, viewCoords);
      mouseRightPopup.add( overviewWindowItem);

      JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
      constraintNetworkView.createRaiseContentSpecItem( raiseContentSpecItem);
      mouseRightPopup.add( raiseContentSpecItem);
    
      JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
      createActiveTokenItem( activeTokenItem);
      mouseRightPopup.add( activeTokenItem);

      JMenuItem changeLayoutItem = null;
      if(constraintNetworkView.getNewLayout().layoutHorizontal()) {
        changeLayoutItem = new JMenuItem("Vertical Layout");
      }
      else {
        changeLayoutItem = new JMenuItem("Horizontal Layout");
      }
      createChangeLayoutItem(changeLayoutItem);
      mouseRightPopup.add(changeLayoutItem);

      constraintNetworkView.createAllViewItems( partialPlan, partialPlanName,
                                                planSequence, mouseRightPopup);

      NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
    } // end mouseRightPopupMenu

    private void createActiveTokenItem( JMenuItem activeTokenItem) {
      activeTokenItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            PwToken activeToken =
              ((PartialPlanViewSet) constraintNetworkView.getViewSet()).getActiveToken();
            if (activeToken != null) {
              boolean isByKey = false;
              findAndSelectToken( activeToken, isByKey);
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

              PwToken tokenToFind = partialPlan.getToken( nodeKey);
              if (tokenToFind != null) {
                boolean isByKey = true;
                findAndSelectToken( tokenToFind, isByKey);
              } else {
                PwVariable variableToFind = partialPlan.getVariable( nodeKey);
                if (variableToFind != null) {
                  findAndSelectVariable( variableToFind);
                } else {
                  PwConstraint constraintToFind = partialPlan.getConstraint( nodeKey);
                  if (constraintToFind != null) {
                    findAndSelectConstraint( constraintToFind);
                  }
                }
              }
            }
          }
        });
    } // end createNodeByKeyItem

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

    private void findAndSelectToken( PwToken tokenToFind, boolean isByKey) {
      boolean isTokenFound = false;
      boolean isHighlightNode = true;
      Iterator tokenNodeListItr = constraintNetworkView.getTokenNodeList().iterator();
      while (tokenNodeListItr.hasNext()) {
        TokenNode tokenNode = (TokenNode) tokenNodeListItr.next();
        if ((tokenNode.getToken() != null) &&
            (tokenNode.getToken().getId().equals( tokenToFind.getId()))) {
          System.err.println( "ConstraintNetworkView found token: " +
                              tokenToFind.getPredicate().getName() +
                              " (key=" + tokenToFind.getId().toString() + ")");
          NodeGenerics.focusViewOnNode( tokenNode, isHighlightNode, this);
          isTokenFound = true;
          break;
        }
      }
      if (isTokenFound && (! isByKey)) {
        NodeGenerics.selectSecondaryNodes
          ( NodeGenerics.mapTokensToTokenNodes
            (((PartialPlanViewSet) constraintNetworkView.getViewSet()).
             getSecondaryTokens(), constraintNetworkView.getTokenNodeList()), this);
      }
      if (! isTokenFound) {
        // Content Spec filtering may cause this to happen
        String message = "Token " + tokenToFind.getPredicate().getName() +
          " (key=" + tokenToFind.getId().toString() + ") not found.";
        JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
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
            ConstraintNetworkTokenNode tokenNode = 
              (ConstraintNetworkTokenNode) variableNode.getTokenNodeList().get( 0);
            System.err.println( "ConstraintNetworkView found token: " +
                                tokenNode.getPredicateName() +
                                " (key=" + tokenNode.getToken().getId().toString() + ")");
            // open connecting token to display it
            tokenNode.addTokenNodeVariables( tokenNode, constraintNetworkView);
            tokenNode.setAreNeighborsShown( true);
          }
          constraintNetworkView.setFocusNode( variableNode);
          NodeGenerics.focusViewOnNode( variableNode, isHighlightNode, this);
          isVariableFound = true;
          break;
        }
      }
      if (! isVariableFound) {
        // Content Spec filtering may cause this to happen
        String message = "Variable " + variableToFind.getDomain().toString() +
          " (key=" + variableToFind.getId().toString() + ") not found.";
        JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                       "Variable Not Found in ConstraintNetworkView",
                                       JOptionPane.ERROR_MESSAGE);
        System.err.println( message);
      }
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
                (VariableNode) constraintNode.getVariableNodeList().get( 0);
            }
            System.err.println( "ConstraintNetworkView found variable: " +
                                variableNode.getVariable().getDomain().toString() +
                                " (key=" + variableNode.getVariable().getId().toString() + ")");
            if (! variableNode.inLayout()) {
              ConstraintNetworkTokenNode tokenNode = null;
              // look for open connected tokenNode
              tokenNode = getOpenTokenNode( variableNode);
              if (tokenNode == null) {
                tokenNode = 
                  (ConstraintNetworkTokenNode) variableNode.getTokenNodeList().get( 0);
                // open connecting token to display variable node
                tokenNode.addTokenNodeVariables( tokenNode, constraintNetworkView);
                tokenNode.setAreNeighborsShown( true);
              }
              System.err.println( "ConstraintNetworkView found token: " +
                                  tokenNode.getPredicateName() +
                                  " (key=" + tokenNode.getToken().getId().toString() + ")");
            }
            // open connecting variableNode to display it
            variableNode.addVariableNodeTokensAndConstraints( variableNode,
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
        JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                       "Constraint Not Found in ConstraintNetworkView",
                                       JOptionPane.ERROR_MESSAGE);
        System.err.println( message);
      }
    } // end findAndSelectConstraint

    private ConstraintNetworkTokenNode getOpenTokenNode( VariableNode variableNode) {
      Iterator tokenNodeItr = variableNode.getTokenNodeList().iterator();
      while (tokenNodeItr.hasNext()) {
        ConstraintNetworkTokenNode tokenNode =
          (ConstraintNetworkTokenNode) tokenNodeItr.next();
        if (tokenNode.areNeighborsShown()) {
          return tokenNode;
        }
      }
      return null;
    } // end getOpenTokenNode

    private VariableNode getVariableNodeInLayout( ConstraintNode constraintNode) {
      Iterator variableNodeItr = constraintNode.getVariableNodeList().iterator();
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
              ViewGenerics.openOverviewFrame( PlanWorks.CONSTRAINT_NETWORK_VIEW, partialPlan,
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








