// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintNetworkView.java,v 1.1 2003-07-30 00:38:41 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 28July03
//

package gov.nasa.arc.planworks.viz.views.constraintNetwork;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoLayer;
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;

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
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ConstraintNode;
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
  // nodeList & tmpNodeList used by JFCUnit test case
  private List nodeList; // elements TokenNode, VariableNode & ConstraintNode
  private List tmpNodeList; // element TokenNode
  private List variableNodeList; // element VariableNode
  private List constraintNodeList; // element ConstraintNode
  private List linkList; // element TokenLink
  private Map relationships; // token, variable, constraint relationships
  private List constraintLinkNameList; // element String
  private List variableLinkNameList; // element String
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
    nodeList = null;
    tmpNodeList = new ArrayList();
    variableNodeList = new ArrayList();
    constraintNodeList = new ArrayList();
    linkList = new ArrayList();
    relationships = new HashMap();
    constraintLinkNameList = new ArrayList();
    variableLinkNameList = new ArrayList();
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
    setNodesVisible();

    LayeredDigraphAutoLayout layout =
      new LayeredDigraphAutoLayout( jGoDocument, startTimeMSecs);
    layout.performLayout();
    computeExpandedViewFrame();
    expandViewFrame( viewSet, viewName, maxViewWidth, maxViewHeight);

    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *
   */
  public void redraw() {
    // setVisible(true | false) depending on keys
    setNodesVisible();
    expandViewFrame( viewSet, viewName, maxViewWidth, maxViewHeight);
  } // end redraw


  private void computeExpandedViewFrame() {
    Iterator tokenNodeIterator = nodeList.iterator();
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

  public List getNodeList() {
    return nodeList;
  }

  public List getLinkList() {
    return linkList;
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
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT * 2;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    int objectCnt = 0;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      Iterator timelineIterator = object.getTimelineList().iterator();
      while (timelineIterator.hasNext()) {
        int x = ViewConstants.TIMELINE_VIEW_X_INIT;
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        createTokenNodesOfTimeline( timeline, x, y, objectCnt);
        y += 2 * ViewConstants.TIMELINE_VIEW_Y_DELTA;
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
                                               isFreeToken, this);
      if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
        x += freeTokenNode.getSize().getWidth() * 0.5;
        freeTokenNode.setLocation( x, y);
      }
      tmpNodeList.add( freeTokenNode);
      jGoDocument.addObjectAtTail( freeTokenNode);

      createVariableAndConstraintNodes( token, freeTokenNode, objectCnt, x, y);
      x += freeTokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
    }

    createTokenVariableConstraintRelationships();
    nodeList = tmpNodeList;
  } // end createTokenNodes

  private void createTokenNodesOfTimeline( PwTimeline timeline, int x, int y,
                                           int objectCnt) {
    boolean isFreeToken = false;
    Iterator slotIterator = timeline.getSlotList().iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      Iterator tokenIterator = slot.getTokenList().iterator();
      while (tokenIterator.hasNext()) {
        PwToken token = (PwToken) tokenIterator.next();
        TokenNode tokenNode =
          new TokenNode( token, new Point( x, y), objectCnt, isFreeToken, this);
        if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
          x += tokenNode.getSize().getWidth() * 0.5;
          tokenNode.setLocation( x, y);
        }
        tmpNodeList.add( tokenNode);
        jGoDocument.addObjectAtTail( tokenNode);

        createVariableAndConstraintNodes( token, tokenNode, objectCnt, x, y);
        x += tokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
      }
    }
  } // end createTokenNodesOfTimeline

  private void createVariableAndConstraintNodes( PwToken token, TokenNode tokenNode,
                                                 int objectCnt, int x, int y) {
    int xVar = x, yVar = y + ViewConstants.TIMELINE_VIEW_Y_DELTA;
    Iterator variableItr = token.getVariablesList().iterator();
    while (variableItr.hasNext()) {
      PwVariable variable = (PwVariable) variableItr.next();
      if (variable != null) {
        VariableNode variableNode = getVariableNode( variable.getKey());
        if (variableNode == null) {
          variableNode = new VariableNode( variable, tokenNode, new Point( xVar, yVar),
                                           objectCnt, this);
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
                                    objectCnt, this);
              constraintNodeList.add( constraintNode);
              jGoDocument.addObjectAtTail( constraintNode);
            }  else {
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

  private void createTokenVariableConstraintRelationships() {
    Iterator tokenNodeIterator = tmpNodeList.iterator();
    while (tokenNodeIterator.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeIterator.next();
      System.err.println( "tokenNode " + tokenNode.getToken().getKey());
      Iterator variableItr = tokenNode.getVariableNodeList().iterator();
      while (variableItr.hasNext()) {
        VariableNode variableNode = (VariableNode) variableItr.next();
        System.err.println( "  variableNode " + variableNode.getVariable().getKey());
        createConstraintLink( variableNode, tokenNode, "variable");
        Iterator constraintNodeItr = variableNode.getConstraintNodeList().iterator();
        while (constraintNodeItr.hasNext()) {
          ConstraintNode constraintNode = (ConstraintNode) constraintNodeItr.next();
          System.err.println( "    constraintNode " + constraintNode.getConstraint().getKey());
          createConstraintLink( constraintNode, variableNode, "constraint");
        }
      }
    }
  } // end createTokenVariableConstraintRelationships

  private TokenNode getTokenNode( Integer nodeId) {
    TokenNode tokenNode = null;
    Iterator tokenNodeItr = tmpNodeList.iterator();
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
 
  private void createConstraintLink( BasicNode fromNode, BasicNode toNode,
                                     String fromNodeType) {
    if (fromNodeType.equals( "constraint")) {
      String linkName =
        ((ConstraintNode) fromNode).getConstraint().getKey().toString() + "->" +
        ((VariableNode) toNode).getVariable().getKey().toString();
      Iterator linkItr = constraintLinkNameList.iterator();
      while (linkItr.hasNext()) {
        if (linkName.equals( (String) linkItr.next())) {
          System.err.println( "discard " + linkName + " fromNodeType " + fromNodeType);
          return;
        }
      }
      constraintLinkNameList.add( linkName);
    }
    if (fromNodeType.equals( "variable")) {
      String linkName =
        ((VariableNode) fromNode).getVariable().getKey().toString() + "->" +
        ((TokenNode) toNode).getToken().getKey().toString();
      Iterator linkItr = variableLinkNameList.iterator();
      while (linkItr.hasNext()) {
        if (linkName.equals( (String) linkItr.next())) {
          System.err.println( "discard " + linkName + " fromNodeType " + fromNodeType);
          return;
        }
      }
      variableLinkNameList.add( linkName);
    }
    BasicNodeLink link = new BasicNodeLink( fromNode, toNode);
    linkList.add( link);
    jGoDocument.addObjectAtTail( link);
  } // end createConstraintLink

  private void setNodesVisible() {
    // System.err.println( "Constraint Network View - contentSpec");
    // viewSet.printSpec();
    validTokenIds = viewSet.getValidTokenIds();
    displayedTokenIds = new ArrayList();
    Iterator tokenNodeIterator = nodeList.iterator();
    while (tokenNodeIterator.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeIterator.next();
      Iterator variablesItr = tokenNode.getVariableNodeList().iterator();
      if (isTokenInContentSpec( tokenNode.getToken())) {
        tokenNode.setVisible( true);
        while (variablesItr.hasNext()) {
          VariableNode variableNode = (VariableNode) variablesItr.next();
          variableNode.setVisible( true);
          Iterator varConItr = variableNode.getConstraintNodeList().iterator();
          while (varConItr.hasNext()) {
            ((ConstraintNode) varConItr.next()).setVisible( true);
          }
        }
      } else {
        tokenNode.setVisible( false);
        while (variablesItr.hasNext()) {
          VariableNode variableNode = (VariableNode) variablesItr.next();
          variableNode.setVisible( false);
          Iterator varConItr = variableNode.getConstraintNodeList().iterator();
          while (varConItr.hasNext()) {
            ((ConstraintNode) varConItr.next()).setVisible( false);
          }
        }
      }
    }

    Iterator tokenLinkIterator = linkList.iterator();
    while (tokenLinkIterator.hasNext()) {
      BasicNodeLink link = (BasicNodeLink) tokenLinkIterator.next();
      BasicNode toNode = link.getToNode();
      if (toNode instanceof TokenNode) {
        if (isTokenInContentSpec( ((TokenNode) toNode).getToken())) {
          link.setVisible( true);
        } else {
          link.setVisible( false);
        }
      } else if (toNode instanceof VariableNode) {
        Iterator varTokenItr = ((VariableNode) toNode).getTokenNodeList().iterator();
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
    }
    boolean showDialog = true;
    isContentSpecRendered( "Constraint Network View", showDialog);
  } // end setNodesVisible



} // end class ConstraintNetworkView











