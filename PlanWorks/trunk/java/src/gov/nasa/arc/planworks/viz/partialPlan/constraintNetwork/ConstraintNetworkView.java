// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintNetworkView.java,v 1.1 2003-09-25 23:52:45 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 28July03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;


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

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private PartialPlanViewSet viewSet;
  private String viewName;
  private ConstraintJGoView jGoView;
  private JGoDocument document;
  private ConstraintNetwork network;
  private Font font;
  private FontMetrics fontMetrics;
  // tokenNodeList & tmpTokenNodeList used by JFCUnit test case
  private List tokenNodeList; // element TokenNode
  private List tmpTokenNodeList; // element TokenNode
  private List variableNodeList; // element VariableNode
  private List constraintNodeList; // element ConstraintNode
  private List constraintLinkList; // element BasicNodeLink
  private List variableLinkList; // element BasicNodeLink
  private boolean isDebugPrint;
  private boolean isDebugTraverse;
  private boolean isLayoutNeeded;
  private JGoArea focusNode; // TokenNode/ConstraintNode/VariableNode


  /**
   * <code>ConstraintNetworkView</code> - constructor -
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>PwPartialPlan</code> -
   * @param startTimeMSecs - <code>long</code> - 
   * @param viewSet - <code>PartialPlanViewSet</code> - 
   */
  public ConstraintNetworkView( PwPartialPlan partialPlan, long startTimeMSecs,
                           PartialPlanViewSet viewSet) {
    super( partialPlan, viewSet);
    this.partialPlan = partialPlan;
    this.startTimeMSecs = startTimeMSecs;
    this.viewSet = viewSet;
    viewName = "constraintNetworkView";
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

    jGoView = new ConstraintJGoView();
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
    Graphics graphics = ((JPanel) this).getGraphics();
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    // does nothing
    // jGoView.setFont( font);
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();

    document = jGoView.getDocument();
    network = new ConstraintNetwork();

    createTokenNodes();
    // setVisible( true | false) depending on ContentSpec
    setNodesLinksVisible();

    ConstraintNetworkLayout layout =
      new ConstraintNetworkLayout( document, network, startTimeMSecs);
    layout.performLayout();
    expandViewFrame( viewName,
                     (int) jGoView.getDocumentSize().getWidth(), VIEW_HEIGHT);

    isLayoutNeeded = false;
    focusNode = null;

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
    new RedrawViewThread().start();
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
    long startTimeMSecs = (new Date()).getTime();
    // setVisible(true | false) depending on keys
    setNodesLinksVisible();

    // content spec apply/reset do not change layout, only tokenNode/
    // variableNode/constraintNode opening/closing
    if (isLayoutNeeded) {
      System.err.println( "Redrawing Constraint Network View ...");
      if (isDebugPrint) {
        network.validateConstraintNetwork();
      }
      ConstraintNetworkLayout layout =
        new ConstraintNetworkLayout( document, network, startTimeMSecs);
      layout.performLayout();
      if (focusNode != null) {
        NodeGenerics.focusViewOnNode( focusNode, jGoView);
      }
      isLayoutNeeded = false;
    }
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
   * <code>getFontMetrics</code>
   *
   * @return - <code>FontMetrics</code> - 
   */
  public FontMetrics getFontMetrics()  {
    return fontMetrics;
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
        Color timelineColor = viewSet.getColorStream().getColor( timelineCnt);

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
    while (freeTokenItr.hasNext()) {
      PwToken token = (PwToken) freeTokenItr.next();
      TokenNode freeTokenNode = new TokenNode( token, new Point( x, y), backgroundColor,
                                               isFreeToken, isDraggable, this);
      if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
        x += freeTokenNode.getSize().getWidth() * 0.5;
        freeTokenNode.setLocation( x, y);
      }
      tmpTokenNodeList.add( freeTokenNode);
      // nodes are always in front of any links
      document.addObjectAtTail( freeTokenNode);
      network.addConstraintNode( freeTokenNode);

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
        TokenNode tokenNode =
          new TokenNode( token, new Point( x, y), backgroundColor, isFreeToken,
                         isDraggable, this);
        if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
          x += tokenNode.getSize().getWidth() * 0.5;
          tokenNode.setLocation( x, y);
        }
        tmpTokenNodeList.add( tokenNode);
        // nodes are always in front of any links
        document.addObjectAtTail( tokenNode);
        network.addConstraintNode( tokenNode);
          
        createVariableAndConstraintNodes( tokenNode, backgroundColor, isFreeToken);
        createTokenVariableConstraintLinks( tokenNode);

        x += tokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
        tokenCnt++;
      }
    }
  } // end createTokenNodesOfTimeline

  private void createVariableAndConstraintNodes( TokenNode tokenNode, Color backgroundColor,
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

  private void createTokenVariableConstraintLinks( TokenNode tokenNode) {
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
  public boolean addVariableNodes( TokenNode tokenNode) { 
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
        network.addConstraintNode( variableNode);
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
        network.addConstraintNode( constraintNode);
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
        network.addConstraintNode( variableNode);
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
  public boolean addVariableToTokenLinks( TokenNode tokenNode) {
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

      link = addVariableToTokenLink( (VariableNode) fromNode, (TokenNode) toNode,
                                     sourceNode);
      linkType = "VtoT";
    }
    if (link != null) {
      // links are always behind any nodes
      document.addObjectAtHead( link);
      network.addConstraintLink( link, fromNode, toNode);
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
                                                TokenNode tokenNode,
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
    network.removeConstraintNode( variableNode);
    variableNode.setInLayout( false);
    variableNode.resetNode( isDebugPrint);
  } // end removeVariableNode

  private void removeConstraintNode( ConstraintNode constraintNode) {
    if (isDebugPrint) {
      System.err.println( "remove constraintNode " +
                          constraintNode.getConstraint().getId());
    }
    // document.removeObject( constraintNode);
    network.removeConstraintNode( constraintNode);
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
  public boolean removeVariableNodes( TokenNode tokenNode) {
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
                                             TokenNode tokenNode) {
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
      network.removeConstraintLink( link);
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
      network.removeConstraintLink( link);
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
  public boolean removeVariableToTokenLinks( TokenNode tokenNode) {
    // System.err.println( "tokenNode " + tokenNode.getToken().getId());
    boolean areLinksChanged = false;
    Iterator variableItr = tokenNode.getVariableNodeList().iterator();
    while (variableItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableItr.next();
      Iterator varTokLinkItr = variableNode.getVariableTokenLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
        if (link.inLayout() && ((TokenNode) link.getToNode()).equals( tokenNode)) {
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
      TokenNode tokenNode = (TokenNode) tokenNodeItr.next();
      Iterator varTokLinkItr = variableNode.getVariableTokenLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
        if (((TokenNode) link.getToNode()).equals( tokenNode) &&
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
      Iterator varTokLinkItr = variableNode.getVariableTokenLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
        if (link.inLayout() &&
            ((VariableNode) link.getFromNode()).equals( variableNode)) {
          if (removeVariableToTokenLink( link, variableNode,
                                         (TokenNode) link.getToNode())) {
            areLinksChanged = true;
          }
        }
      }
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
      TokenNode tokenNode = (TokenNode) tokenNodeIterator.next();
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
    isContentSpecRendered( "Constraint Network View", showDialog);
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
   * <code>ConstraintJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  class ConstraintJGoView extends JGoView {

    /**
     * <code>ConstraintJGoView</code> - constructor 
     *
     */
    public ConstraintJGoView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *                                 1) snap to active token
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

  } // end class ConstraintJGoView


  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();
    JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
    createActiveTokenItem( activeTokenItem);
    mouseRightPopup.add( activeTokenItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createActiveTokenItem( JMenuItem activeTokenItem) {
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) ConstraintNetworkView.this.getViewSet()).getActiveToken();
          boolean isTokenFound = false;
          if (activeToken != null) {
            Iterator tokenNodeListItr = tokenNodeList.iterator();
            while (tokenNodeListItr.hasNext()) {
              TokenNode tokenNode = (TokenNode) tokenNodeListItr.next();
              if ((tokenNode.getToken() != null) &&
                  (tokenNode.getToken().getId().equals( activeToken.getId()))) {
                System.err.println( "ConstraintNetworkView snapToActiveToken: " +
                                    activeToken.getPredicate().getName());
                NodeGenerics.focusViewOnNode( tokenNode, jGoView);
                isTokenFound = true;
                break;
              }
            }
            if (isTokenFound) {
              NodeGenerics.selectSecondaryNodes
                ( NodeGenerics.mapTokensToTokenNodes
                  (((PartialPlanViewSet) ConstraintNetworkView.this.getViewSet()).
                   getSecondaryTokens(), tokenNodeList),
                  jGoView);
            } else {
              String message = "active token '" + activeToken.getPredicate().getName() +
                "' not found in ConstraintNetworkView";
              JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                             "Active Token Not Found",
                                             JOptionPane.ERROR_MESSAGE);
              System.err.println( message);
              System.exit( 1);
            }
          }
        }
      });
  } // end createActiveTokenItem


} // end class ConstraintNetworkView











