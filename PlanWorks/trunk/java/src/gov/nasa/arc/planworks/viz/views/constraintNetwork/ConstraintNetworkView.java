// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintNetworkView.java,v 1.5 2003-08-06 17:11:28 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 28July03
//

package gov.nasa.arc.planworks.viz.views.constraintNetwork;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
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
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ConstraintNode;
import gov.nasa.arc.planworks.viz.nodes.TimelineBasicNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.nodes.VariableNode;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.views.VizView;


/**
 * <code>ConstraintNetworkView</code> - render a partial plan's network of
 *                                      constraints
 *                JPanel->VizView->ConstraintNetworkView
 *                JComponent->JGoView
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNetworkView extends VizView {

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private String viewName;
  private JGoView jGoView;
  private JGoDocument jGoDocument;
  // private JGoLayer hiddenLayer;
  private Font font;
  private FontMetrics fontMetrics;
  // tokenNodeList & tmpTokenNodeList used by JFCUnit test case
  private List tokenNodeList; // element TokenNode
  private List tmpTokenNodeList; // element TokenNode
  private List timelineNodeList; // element TimelineNode
  private List variableNodeList; // element VariableNode
  private List constraintNodeList; // element ConstraintNode
  private List tokenLinkList; // element BasicNodeLink
  private List constraintLinkList; // element BasicNodeLink
  private List variableLinkList; // element BasicNodeLink
  private List overloadedTokensIdList; // element Integer
  private int maxViewWidth;
  private int maxViewHeight;

  /**
   * <code>ConstraintNetworkView</code> - constructor -
   *                                      called by ViewSet.openConstraintNetworkView.
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>PwPartialPlan</code> -
   * @param startTimeMSecs - <code>long</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public ConstraintNetworkView( PwPartialPlan partialPlan, long startTimeMSecs,
                           ViewSet viewSet) {
    super( partialPlan);
    this.partialPlan = partialPlan;
    this.startTimeMSecs = startTimeMSecs;
    this.viewSet = viewSet;
    viewName = "constraintNetworkView";
    tokenNodeList = null;
    tmpTokenNodeList = new ArrayList();
    timelineNodeList = new ArrayList();
    variableNodeList = new ArrayList();
    constraintNodeList = new ArrayList();
    tokenLinkList = new ArrayList();
    constraintLinkList = new ArrayList();
    variableLinkList = new ArrayList();
    overloadedTokensIdList = new ArrayList();
    maxViewWidth = PlanWorks.INTERNAL_FRAME_WIDTH;
    maxViewHeight = PlanWorks.INTERNAL_FRAME_HEIGHT;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new JGoView();
    jGoView.setBackground( ColorMap.getColor( "lightGray"));
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    jGoView.setFont( font);
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
    // wait for TimelineView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "constraintNetworkView displayable " + this.isDisplayable());
    }
    Graphics graphics = ((JPanel) this).getGraphics();
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();

    jGoDocument = jGoView.getDocument();
    // hiddenLayer = jGoDocument.addLayerBefore( jGoDocument.getDefaultLayer());

    // create all nodes
    createTokenNodes();
    // setVisible( true | false) depending on ContentSpec
    setNodesLinksVisible();
    ConstraintNetworkLayout layout =
      new ConstraintNetworkLayout( jGoDocument, startTimeMSecs);
    layout.performLayout();
    computeExpandedViewFrame();
    expandViewFrame( viewSet, viewName, maxViewWidth, maxViewHeight);

    long stopTimeMSecs = (new Date()).getTime();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *
   */
  public void redraw() {
    setCursor( new Cursor( Cursor.WAIT_CURSOR));
    long startTimeMSecs = (new Date()).getTime();
    System.err.println( "Redrawing Constraint Network View ...");
    // setVisible(true | false) depending on keys
    setNodesLinksVisible();
    ConstraintNetworkLayout layout =
      new ConstraintNetworkLayout( jGoDocument, startTimeMSecs);
    layout.performLayout();
    computeExpandedViewFrame();
    expandViewFrame( viewSet, viewName, maxViewWidth, maxViewHeight);
    setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end redraw



  private void computeExpandedViewFrame() {
    Iterator tokenNodeIterator = tokenNodeList.iterator();
    while (tokenNodeIterator.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeIterator.next();
      int maxWidth = (int) tokenNode.getLocation().getX() +
        (int) tokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_X_INIT;
      maxViewWidth = Math.max( maxWidth, maxViewWidth);
      int maxHeight = (int) tokenNode.getLocation().getY() +
        ViewConstants.TIMELINE_VIEW_Y_INIT;
      maxViewHeight = Math.max( maxHeight, maxViewHeight);
    }
  } // end computeExpandedViewFrame

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>getFontMetrics</code>
   *
   * @return - <code>FontMetrics</code> - 
   */
  public FontMetrics getFontMetrics()  {
    return fontMetrics;
  }

  private void createTokenNodes() {
    boolean isDraggable = true;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT * 2;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    int objectCnt = 0;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      Iterator timelineIterator = object.getTimelineList().iterator();
      while (timelineIterator.hasNext()) {
        int x = ViewConstants.TIMELINE_VIEW_X_INIT;
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        String timelineName = timeline.getName();
        String timelineNodeName = objectName + " : " + timelineName;
        TimelineBasicNode timelineNode = null;
//         TimelineBasicNode timelineNode =
//           new TimelineBasicNode( timelineNodeName, timeline, new Point( x, y),
//                             objectCnt, isDraggable, this);
//         timelineNodeList.add( timelineNode);
//         jGoDocument.addObjectAtTail( timelineNode);
//         x += timelineNode.getSize().getWidth();

        createTokenNodesOfTimeline( timeline, timelineNode, x, y, objectCnt);

        y += ViewConstants.TIMELINE_VIEW_Y_DELTA;
      }
      objectCnt += 1;
    }
    // free tokens
    List freeTokenList = partialPlan.getFreeTokenList();
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    // System.err.println( "token network view freeTokenList " + freeTokenList);
    Iterator freeTokenItr = freeTokenList.iterator();
    boolean isFreeToken = true; objectCnt = -1;
    while (freeTokenItr.hasNext()) {
      PwToken token = (PwToken) freeTokenItr.next();
      TokenNode freeTokenNode = new TokenNode( token, new Point( x, y), objectCnt,
                                               isFreeToken, isDraggable, viewName, this);
      if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
        x += freeTokenNode.getSize().getWidth() * 0.5;
        freeTokenNode.setLocation( x, y);
      }
      tmpTokenNodeList.add( freeTokenNode);
      jGoDocument.addObjectAtTail( freeTokenNode);

      x += freeTokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
    }
    tokenNodeList = tmpTokenNodeList;
  } // end createTokenNodes

  private void createTokenNodesOfTimeline( PwTimeline timeline,
                                           TimelineBasicNode timelineNode,
                                           int x, int y, int objectCnt) {
    boolean isFreeToken = false, isDraggable = true;
    Iterator slotIterator = timeline.getSlotList().iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      Iterator tokenIterator = slot.getTokenList().iterator();
      int tokenCnt = 0;
      while (tokenIterator.hasNext()) {
        PwToken token = (PwToken) tokenIterator.next();
        if (tokenCnt == 0) { // only create node for base token
          TokenNode tokenNode =
            new TokenNode( token, new Point( x, y), objectCnt, isFreeToken,
                           isDraggable, viewName, this);
          if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
            x += tokenNode.getSize().getWidth() * 0.5;
            tokenNode.setLocation( x, y);
          }
          tmpTokenNodeList.add( tokenNode);
          jGoDocument.addObjectAtTail( tokenNode);
          // link tokenNode to its timelineNode
//           String linkName = timelineNode.getTimelineName() + "->" +
//             tokenNode.getPredicateName();
//           BasicNodeLink link = new BasicNodeLink( timelineNode, tokenNode, linkName);
//           jGoDocument.addObjectAtTail( link);
//           tokenLinkList.add( link);
          x += tokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
        } else {
          overloadedTokensIdList.add( token.getKey());
        }
        tokenCnt++;
      }
    }
  } // end createTokenNodesOfTimeline

  /**
   * <code>createVariableAndConstraintNodes</code>
   *
   *                    called by TokenNode doMouseClick
   *
   * @param tokenNode - <code>TokenNode</code> - 
   */
  public void createVariableAndConstraintNodes( TokenNode tokenNode) {
    PwToken token = tokenNode.getToken();
    int objectCnt = tokenNode.getObjectCnt();
    int xVar = (int) tokenNode.getLocation().getX();
    int yVar = (int) tokenNode.getLocation().getY() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
    boolean isDraggable = true;
    Iterator variableItr = token.getVariablesList().iterator();
    while (variableItr.hasNext()) {
      PwVariable variable = (PwVariable) variableItr.next();
      if (variable != null) {
        VariableNode variableNode = getVariableNode( variable.getKey());
        if (variableNode == null) {
          variableNode = new VariableNode( variable, tokenNode, new Point( xVar, yVar),
                                           objectCnt, isDraggable, this);
          // System.err.println( "add variableNode " + variableNode.getVariable().getKey());
          variableNodeList.add( variableNode);
          jGoDocument.addObjectAtTail( variableNode);
        } else {
          variableNode.addTokenNode( tokenNode);
        }
        tokenNode.addVariableNode( variableNode);

        int xCon = xVar, yCon = yVar + ViewConstants.TIMELINE_VIEW_Y_DELTA;
        // System.err.println( "variable " + variable.getKey());
        // System.err.println( "  constraintList " + variable.getConstraintList());
        Iterator constraintItr = variable.getConstraintList().iterator();
        while (constraintItr.hasNext()) {
          PwConstraint constraint = (PwConstraint) constraintItr.next();
          if (constraint != null) {
            ConstraintNode constraintNode = getConstraintNode( constraint.getKey());
            if (constraintNode == null) {
              constraintNode =
                new ConstraintNode( constraint, variableNode, new Point( xCon, yCon),
                                    objectCnt, isDraggable, this);
              // System.err.println( "add constraintNode " +
              //                     constraintNode.getConstraint().getKey());
              constraintNodeList.add( constraintNode);
              jGoDocument.addObjectAtTail( constraintNode);
            } else {
              constraintNode.addVariableNode( variableNode);
            }
            variableNode.addConstraintNode( constraintNode);
            xCon += constraintNode.getSize().getWidth() * 1.5;
            constraintNode.setLocation( xCon, yCon);
          }
        }
        xVar += variableNode.getSize().getWidth() * 1.5;
        variableNode.setLocation( xVar, yVar);
      }
    }
  } // end createVariableAndConstraintNodes

  /**
   * <code>removeVariableAndConstraintNodes</code>
   *
   * @param tokenNode - <code>TokenNode</code> - 
   */
  public void removeVariableAndConstraintNodes( TokenNode tokenNode) {
    Iterator variableNodeItr = tokenNode.getVariableNodeList().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      List varTokenNodeList = variableNode.getTokenNodeList();
      varTokenNodeList.remove( varTokenNodeList.indexOf( tokenNode));
      if (varTokenNodeList.size() == 0) {
        variableNodeList.remove( variableNodeList.indexOf( variableNode));
        // System.err.println( "remove variableNode " +
        //                     variableNode.getVariable().getKey());
        jGoDocument.removeObject( variableNode);
      }
 
      Iterator constraintNodeItr = variableNode.getConstraintNodeList().iterator();
      while (constraintNodeItr.hasNext()) {
        ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
        List conVarTokenList = constraintNode.getVariableNodeList();
        conVarTokenList.remove( conVarTokenList.indexOf( variableNode));
        if (conVarTokenList.size() == 0) {
          constraintNodeList.remove( constraintNodeList.indexOf( constraintNode));
          // System.err.println( "remove constraintNode " +
          //                     constraintNode.getConstraint().getKey());
          jGoDocument.removeObject( constraintNode);
        }
      }
    }
    tokenNode.setVariableNodeList( new ArrayList());
  } // end removeVariableAndConstraintNodes

  /**
   * <code>createTokenVariableConstraintLinks</code>
   *
   *                    called by TokenNode doMouseClick
   * @param tokenNode - <code>TokenNode</code> - 
   */
  public void createTokenVariableConstraintLinks( TokenNode tokenNode) {
    // System.err.println( "tokenNode " + tokenNode.getToken().getKey());
    Iterator variableItr = tokenNode.getVariableNodeList().iterator();
    while (variableItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableItr.next();
      // System.err.println( "  variableNode " + variableNode.getVariable().getKey());
      createConstraintLink( variableNode, tokenNode);
      Iterator constraintNodeItr = variableNode.getConstraintNodeList().iterator();
      while (constraintNodeItr.hasNext()) {
        ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
        // System.err.println( "    constraintNode " + constraintNode.getConstraint().getKey());
        createConstraintLink( constraintNode, variableNode);
      }
    }
  } // end createTokenVariableConstraintLinks

  /**
   * <code>removeTokenVariableConstraintLinks</code>
   *
   *                    called by TokenNode doMouseClick
   * @param tokenNode - <code>TokenNode</code> - 
   */
  public void removeTokenVariableConstraintLinks( TokenNode tokenNode) {
    // System.err.println( "tokenNode " + tokenNode.getToken().getKey());
    Iterator variableItr = tokenNode.getVariableNodeList().iterator();
    while (variableItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableItr.next();
      Iterator varTokLinkItr = variableNode.getVariableTokenLinkList().iterator();
      while (varTokLinkItr.hasNext()) {
        BasicNodeLink link = (BasicNodeLink) varTokLinkItr.next();
        if (((TokenNode) link.getToNode()).equals( tokenNode)) {
          int index = variableLinkList.indexOf( link);
          if (index != -1) {
            // System.err.println( "removeVariableTokenLink: " + link.toString());
            jGoDocument.removeObject
              ( (BasicNodeLink) variableLinkList.remove( index));
          }
        }
      }
      Iterator constraintNodeItr = variableNode.getConstraintNodeList().iterator();
      while (constraintNodeItr.hasNext()) {
        ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
        Iterator conVarLinkItr = constraintNode.getConstraintVariableLinkList().iterator();
        while (conVarLinkItr.hasNext()) {
          BasicNodeLink link = (BasicNodeLink) conVarLinkItr.next();
          VariableNode varNode = (VariableNode) link.getToNode();
          if ((varNode.getTokenNodeList().size() == 1) &&
              (((TokenNode) varNode.getTokenNodeList().get( 0)).equals( tokenNode))) {
            int index = constraintLinkList.indexOf( link);
            if (index != -1) {
              // System.err.println( "removeConstraintVariableLink: " + link.toString());
              jGoDocument.removeObject
                ( (BasicNodeLink) constraintLinkList.remove( index));
            }
          }
        }
      }
    }
  } // end removeTokenVariableConstraintLinks

  private TokenNode getTokenNode( Integer nodeId) {
    TokenNode tokenNode = null;
    Iterator tokenNodeItr = tokenNodeList.iterator();
    while (tokenNodeItr.hasNext()) {
      tokenNode = (TokenNode) tokenNodeItr.next();
      if (nodeId.equals( tokenNode.getToken().getKey())) {
        return tokenNode;
      }
    }
    return null;
  } // end getTokenNode

  private VariableNode getVariableNode( Integer nodeId) {
    VariableNode variableNode = null;
    Iterator variableNodeItr = variableNodeList.iterator();
    while (variableNodeItr.hasNext()) {
      variableNode = (VariableNode) variableNodeItr.next();
      if (nodeId.equals( variableNode.getVariable().getKey())) {
        return variableNode;
      }
    }
    return null;
  } // end getVariableNode

  private ConstraintNode getConstraintNode( Integer nodeId) {
    ConstraintNode constraintNode = null;
    Iterator constraintNodeItr = constraintNodeList.iterator();
    while (constraintNodeItr.hasNext()) {
      constraintNode = (ConstraintNode) constraintNodeItr.next();
      if (nodeId.equals( constraintNode.getConstraint().getKey())) {
        return constraintNode;
      }
    }
    return null;
  } // end getConstraintNode
 
  private void createConstraintLink( BasicNode fromNode, BasicNode toNode) {
    String linkName = null, linkTypeName = null;
    BasicNodeLink link = null;
    if (fromNode instanceof ConstraintNode) {
      linkName =
        ((ConstraintNode) fromNode).getConstraint().getKey().toString() + "->" +
        ((VariableNode) toNode).getVariable().getKey().toString();
      Iterator linkItr = constraintLinkList.iterator();
      while (linkItr.hasNext()) {
        if (linkName.equals( ((BasicNodeLink) linkItr.next()).getLinkName())) {
          // System.err.println( "discard constraint=>variable link " + linkName);
          return;
        }
      }
      linkTypeName = "constraint=>variable";
      link = new BasicNodeLink( fromNode, toNode, linkName);
      constraintLinkList.add( link);
      ((ConstraintNode) fromNode).addLink( link);
    }
    if (fromNode instanceof VariableNode) {
      linkName =
        ((VariableNode) fromNode).getVariable().getKey().toString() + "->" +
        ((TokenNode) toNode).getToken().getKey().toString();
      Iterator linkItr = variableLinkList.iterator();
      while (linkItr.hasNext()) {
        if (linkName.equals( ((BasicNodeLink) linkItr.next()).getLinkName())) {
          // System.err.println( "discard variable=>token link " + linkName);
          return;
        }
      }
      linkTypeName = "variable=>token";
      link = new BasicNodeLink( fromNode, toNode, linkName);
      variableLinkList.add( link);
      ((VariableNode) fromNode).addLink( link);
    }
    // System.err.println( "create " +  linkTypeName + " link " + linkName);
    jGoDocument.addObjectAtTail( link);
  } // end createConstraintLink

  private void setNodesLinksVisible() {
    // System.err.println( "Constraint Network View - contentSpec");
    // viewSet.printSpec();
    validTokenIds = viewSet.getValidTokenIds();
    displayedTokenIds = new ArrayList();
    Iterator overloadedTokensItr = overloadedTokensIdList.iterator();
    while (overloadedTokensItr.hasNext()) {
      Integer key = (Integer) overloadedTokensItr.next();
      if (validTokenIds.indexOf( key) >= 0) {
        displayedTokenIds.add( key);
      }
    }
    Iterator tokenNodeIterator = tokenNodeList.iterator();
    while (tokenNodeIterator.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeIterator.next();
      if (isTokenInContentSpec( tokenNode.getToken())) {
        tokenNode.setVisible( true);
      } else {
        tokenNode.setVisible( false);
      }
      Iterator variablesItr = tokenNode.getVariableNodeList().iterator();
      while (variablesItr.hasNext()) {
        VariableNode variableNode = (VariableNode) variablesItr.next();
        if (isVariableNodeInContentSpec( variableNode)) {
          variableNode.setVisible( true);
        } else {
          variableNode.setVisible( false);
        }
        Iterator varConItr = variableNode.getConstraintNodeList().iterator();
        while (varConItr.hasNext()) {
          ConstraintNode constraintNode = (ConstraintNode) varConItr.next();
          if (isConstraintNodeInContentSpec( constraintNode)) {
            constraintNode.setVisible( true);
          } else {
            constraintNode.setVisible( false);
          }
        }
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
      TokenNode toNode = (TokenNode) link.getToNode();
      if (isTokenInContentSpec( toNode.getToken())) {
        link.setVisible( true);
      } else {
        link.setVisible( false);
      }
    }
    Iterator constraintLinkItr = constraintLinkList.iterator();
    while (constraintLinkItr.hasNext()) {
      BasicNodeLink link = (BasicNodeLink) constraintLinkItr.next();
      VariableNode toNode = (VariableNode) link.getToNode();
      Iterator varTokenItr = toNode.getTokenNodeList().iterator();
      while (varTokenItr.hasNext()) {
        TokenNode varTokenNode = (TokenNode) varTokenItr.next();
        if (isTokenInContentSpec( varTokenNode.getToken())) {
          link.setVisible( true);
          break;
        } else {
          link.setVisible( false);
        }
      }
    }
    Iterator tokenLinkItr = tokenLinkList.iterator();
    while (tokenLinkItr.hasNext()) {
      BasicNodeLink link = (BasicNodeLink) tokenLinkItr.next();
      TokenNode toNode = (TokenNode) link.getToNode();
      if (isTokenInContentSpec( toNode.getToken())) {
        link.setVisible( true);
      } else {
        link.setVisible( false);
      }
    }
  } // end setLinksVisible




} // end class ConstraintNetworkView











