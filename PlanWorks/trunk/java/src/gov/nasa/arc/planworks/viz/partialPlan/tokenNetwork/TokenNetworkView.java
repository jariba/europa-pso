// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TokenNetworkView.java,v 1.59 2004-07-08 21:33:26 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 19June03
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>TokenNetworkView</code> - render a partial plan's tokens, their masters
 *                                 and slaves
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNetworkView extends PartialPlanView {

  private long startTimeMSecs;
  private ViewSet viewSet;
  private TokenNetworkJGoView jGoView;
  private JGoDocument jGoDocument;
  private Map tokenNodeMap; // key = tokenId, element TokenNode
  private Map tokenLinkMap; // key = linkName, element TokenLink
  private Map ruleInstanceNodeMap; // key = ruleInstanceId, element RuleInstanceNode
  private boolean isStepButtonView;
  private List rootNodes;
  private Integer focusNodeId;

  /**
   * <code>TokenNetworkView</code> - constructor - 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> -
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TokenNetworkView( final ViewableObject partialPlan,  final ViewSet viewSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    tokenNetworkViewInit( viewSet);
    isStepButtonView = false;
    // print content spec
    // viewSet.printSpec();

    SwingUtilities.invokeLater( runInit);
  } // end constructor

  /**
   * <code>TokenNetworkView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param s - <code>PartialPlanViewState</code> - 
   */
  public TokenNetworkView( final ViewableObject partialPlan, final ViewSet viewSet,
                           final PartialPlanViewState s) {
    super((PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    tokenNetworkViewInit( viewSet);
    isStepButtonView = true;
    setState( s);
    SwingUtilities.invokeLater( runInit);
  }

  /**
   * <code>TokenNetworkView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public TokenNetworkView( final ViewableObject partialPlan,  final ViewSet viewSet,
                           final ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    tokenNetworkViewInit( viewSet);
    isStepButtonView = false;
    // print content spec
    // viewSet.printSpec();
    if (viewListener != null) {
      addViewListener( viewListener);
    }

    SwingUtilities.invokeLater( runInit);
  } // end constructor

  private void tokenNetworkViewInit( final ViewSet viewSet) {
    this.viewSet = (PartialPlanViewSet) viewSet;
    focusNodeId = null;
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    jGoView = new TokenNetworkJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);
    ViewListener viewListener = null;
    viewFrame = viewSet.openView( this.getClass().getName(), viewListener);
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());
    viewName = ViewConstants.TOKEN_NETWORK_VIEW;
  }

  private Runnable runInit = new Runnable() {
      public final void run() {
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
  public final void init() {
    handleEvent( ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for TimelineView instance to become displayable
    if (! ViewGenerics.displayableWait( TokenNetworkView.this)) {
      return;
    }
    this.computeFontMetrics( this);

    boolean isRedraw = false;
    renderTokenNetwork( isRedraw);

//     Rectangle documentBounds = jGoView.getDocument().computeBounds();
//     jGoView.getDocument().setDocumentSize( (int) documentBounds.getWidth() +
//                                            (ViewConstants.TIMELINE_VIEW_X_INIT * 4),
//                                            (int) documentBounds.getHeight() +
//                                            (ViewConstants.TIMELINE_VIEW_Y_INIT * 2));
    if (! isStepButtonView) {
      expandViewFrame( viewFrame, (int) jGoView.getDocumentSize().getWidth(),
                       (int) jGoView.getDocumentSize().getHeight());
    }
    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();
    
    addStepButtons( jGoView);
    if (! isStepButtonView) {
      expandViewFrameForStepButtons( viewFrame, jGoView);
    }
    handleEvent( ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init


  /**
   * <code>setState</code>
   *
   * @param s - <code>PartialPlanViewState</code> - 
   */
  public final void setState( final PartialPlanViewState s) {
    super.setState( s);
    if (s == null) {
      return;
    }
    zoomFactor = s.getCurrentZoomFactor();
    boolean isSetState = true;
    zoomView( jGoView, isSetState, this);
  } // end setState

  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
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
      handleEvent( ViewListener.EVT_REDRAW_BEGUN_DRAWING);
      try {
        ViewGenerics.setRedrawCursor( viewFrame);
        boolean isRedraw = true;
        renderTokenNetwork( isRedraw);
        addStepButtons( jGoView);
        // causes bottom view edge to creep off screen
        //       if (! isStepButtonView) {
        //         expandViewFrameForStepButtons( viewFrame, jGoView);
        //       }
      } finally {
        ViewGenerics.resetRedrawCursor( viewFrame);
      }
      handleEvent(ViewListener.EVT_REDRAW_ENDED_DRAWING);
    } // end run

  } // end class RedrawViewThread

  private void renderTokenNetwork( final boolean isRedraw) {
    if (isRedraw) {
      System.err.println( "Redrawing Token Network View ...");
      startTimeMSecs = System.currentTimeMillis();
      this.setVisible( false);
    } else {
      startTimeMSecs =
        PlanWorks.getPlanWorks().getViewRenderingStartTime( ViewConstants.TOKEN_NETWORK_VIEW);
    }
    jGoView.getDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    tokenNodeMap = new HashMap();
    tokenLinkMap = new HashMap();
    ruleInstanceNodeMap = new HashMap();

    jGoDocument = jGoView.getDocument();

    // create all nodes
    createTokenNodes();

    boolean showDialog = true;
    isContentSpecRendered( ViewConstants.TOKEN_NETWORK_VIEW, showDialog);

    TokenNetworkLayout layout = new TokenNetworkLayout( jGoDocument, startTimeMSecs);
    layout.performLayout();

//     TokenNetworkLayout layout =
//       new TokenNetworkLayout( jGoDocument, rootNodes, startTimeMSecs);
//     layout.performLayout();

    /*long t1 = System.currentTimeMillis();
    TreeRingLayout layout = new TreeRingLayout(linkList, getWidth(), getHeight());
    //layout.ensureAllPositive();
    //layout.position(getWidth(), getHeight());
    System.err.println("Ring layout took " + (System.currentTimeMillis() - t1));*/

    if (isRedraw) {
      this.setVisible( true);
    }
  } // end renderTokenNetwork

  /**
   * <code>getJGoView</code> - 
   *
   * @return - <code>JGoView</code> - 
   */
  public final JGoView getJGoView()  {
    return jGoView;
  }

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public final JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>getTokenNodeKeyList</code>
   *
   * @return - <code>List</code> - 
   */
  public final List getTokenNodeKeyList() {
    return new ArrayList( tokenNodeMap.keySet());
  }

  /**
   * <code>getRuleInstanceNodeKeyList</code>
   *
   * @return - <code>List</code> - 
   */
  public final List getRuleInstanceNodeKeyList() {
    return new ArrayList( ruleInstanceNodeMap.keySet());
  }

  /**
   * <code>getTokenNode</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>TokenNode</code> - 
   */
  public final TokenNode getTokenNode( final Integer id) {
    return (TokenNode) tokenNodeMap.get( id);
  }

  /**
   * <code>getRuleInstanceNode</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>RuleInstanceNode</code> - 
   */
  public final RuleInstanceNode getRuleInstanceNode( final Integer id) {
    return (RuleInstanceNode) ruleInstanceNodeMap.get( id);
  }

  /**
   * <code>getFocusNodeId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getFocusNodeId() {
    return focusNodeId;
  }

  private void createTokenNodes() {
    boolean isDraggable = false;
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT * 2;
    List tokenList = partialPlan.getTokenList();
    Iterator tokenIterator = tokenList.iterator();
    Color backgroundColor = null;
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
//       if (token.isSlotted() && (! token.isBaseToken())) {
//         // non-slotted, non-base tokens - not displayed, put in displayedTokenIds
//         isTokenInContentSpec( token);
//         continue;
//       }
      boolean isFreeToken = false;
      PwSlot slot = null;
      if (! token.isFree()) { // slotted base tokens, resourceTransactions, other tokens
        if (token.getSlotId() != null) {
          slot = partialPlan.getSlot( token.getSlotId());
        }
        backgroundColor = getTimelineColor( token.getParentId());
      } else { // free tokens
        isFreeToken = true;
        backgroundColor = ViewConstants.FREE_TOKEN_BG_COLOR;
      }
      if (isTokenInContentSpec( token)) {
        TokenNode tokenNode =
          new TokenNode( token, slot, new Point( x, y), backgroundColor, isFreeToken,
                         isDraggable, this);
        if (tokenNodeMap.get( token.getId()) == null) {
          tokenNodeMap.put( token.getId(), tokenNode);
          jGoDocument.addObjectAtTail( tokenNode);
          // let JGo layout position nodes -- this will result in correct view bounds
//           x += tokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
        }
      }
    }

    createTokenParentChildRelationships();
   } // end createTokenNodes

  private void createTokenParentChildRelationships() {
    rootNodes = new ArrayList();
    List tokenNodeKeyList = new ArrayList( tokenNodeMap.keySet());
    Iterator tokenNodeKeyItr = tokenNodeKeyList.iterator();
    int x = 2, y = 2;
    boolean isDraggable = false;
    while (tokenNodeKeyItr.hasNext()) {
      TokenNode tokenNode =
        (TokenNode) tokenNodeMap.get( (Integer) tokenNodeKeyItr.next());
      Integer tokenId = tokenNode.getToken().getId();
      Integer masterTokenId = partialPlan.getMasterTokenId( tokenId);
      if (masterTokenId != null) {
        TokenNode masterTokenNode = (TokenNode) tokenNodeMap.get( masterTokenId);
        if ((masterTokenNode != null) && (! masterTokenId.equals( tokenId))) {
          Integer ruleInstanceId = partialPlan.getToken( tokenId).getRuleInstanceId();
          if (ruleInstanceId != null) {
            RuleInstanceNode ruleInstanceNode =
              (RuleInstanceNode) ruleInstanceNodeMap.get( ruleInstanceId);
            if ( ruleInstanceNode == null) {
              List toTokenNodeList = new ArrayList();
              toTokenNodeList.add( tokenNode);
              ruleInstanceNode =
                new RuleInstanceNode( partialPlan.getRuleInstance( ruleInstanceId),
                                      masterTokenNode, toTokenNodeList, new Point( x, y),
                                      ViewConstants.RULE_INSTANCE_BG_COLOR, isDraggable, this);
              ruleInstanceNodeMap.put( ruleInstanceId, ruleInstanceNode);
              jGoDocument.addObjectAtTail( ruleInstanceNode);
            } else {
              ruleInstanceNode.addToTokenNodeList( tokenNode);
            }
            createTokenLink( masterTokenNode, ruleInstanceNode);
            createTokenLink( ruleInstanceNode, tokenNode);
//             System.err.println( "ruleInstance " + ruleInstanceId + " from " +
//                                 masterTokenNode.getToken().getId() + " to " +
//                                 tokenNode.getToken().getId());
          } else {
            createTokenLink( masterTokenNode, tokenNode);
            System.err.println( "no rule instance id=" + tokenId);
          }
        }
      } else {
        rootNodes.add( tokenNode);
      }
      // redundant
//       Iterator slaveTokenIdItr = partialPlan.getSlaveTokenIds( tokenId).iterator();
//       while (slaveTokenIdItr.hasNext()) {
//         Integer slaveTokenId = (Integer) slaveTokenIdItr.next();
//         TokenNode slaveToken = (TokenNode) tokenNodeMap.get( slaveTokenId);
//         if ((slaveToken != null) && (! slaveTokenId.equals( tokenId))) {
//           createTokenLink( tokenNode, slaveToken, "slave");
//         }
//       }
    }
//     Iterator rootNodesItr = rootNodes.iterator();
//     while (rootNodesItr.hasNext()) {
//       System.err.println( "root id " + ((TokenNode)rootNodesItr.next()).getToken().getId());
//     }
  } // end createTokenParentChildRelationships

  private void createTokenLink( final BasicNode fromNode, final BasicNode toNode) {
    String linkName = "";
    if (fromNode instanceof TokenNode) {
      if (toNode instanceof TokenNode) {
        linkName = ((TokenNode) fromNode).getToken().getId().toString() + "->" +
          ((TokenNode) toNode).getToken().getId().toString();
      } else {
        linkName = ((TokenNode) fromNode).getToken().getId().toString() + "->" +
          ((RuleInstanceNode) toNode).getRuleInstance().getId().toString();
      }
    } else if (fromNode instanceof RuleInstanceNode) {
      linkName = ((RuleInstanceNode) fromNode).getRuleInstance().getId().toString() + "->" +
        ((TokenNode) toNode).getToken().getId().toString();
    }
    if (tokenLinkMap.get( linkName) != null) {
      // System.err.println( "createTokenLink discard " + linkName);
      return;
    }
    // getOpenJGoPenWidth( getZoomFactor()) = 2, with zoomFactor = 1, but
    // this view is not being redrawn when zoomFactor changes, so we need
    // the extra width for the higher zoomFactors
    TokenLink link = new TokenLink( fromNode, toNode,
                                    getOpenJGoPenWidth( getZoomFactor()), this);
    tokenLinkMap.put( linkName, link);
    // jGoDocument.addObjectAtTail( link);
    // put links behind nodes
    jGoDocument.addObjectAtHead( link);
  } // end createTokenLink


  /**
   * <code>TokenNetworkJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  class TokenNetworkJGoView extends JGoView {

    /**
     * <code>TokenNetworkJGoView</code> - constructor 
     *
     */
    public TokenNetworkJGoView() {
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
    public final void doBackgroundClick( final int modifiers, final Point docCoords,
                                         final Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick

  } // end class TokenNetworkJGoView


  private void mouseRightPopupMenu( final Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
    createNodeByKeyItem( nodeByKeyItem);
    mouseRightPopup.add( nodeByKeyItem);

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         viewListenerList, ViewConstants.TOKEN_NETWORK_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
    createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);
    
    if (((PartialPlanViewSet) this.getViewSet()).getActiveToken() != null) {
      JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
      createActiveTokenItem( activeTokenItem);
      mouseRightPopup.add( activeTokenItem);
    }

    this.createZoomItem( jGoView, zoomFactor, mouseRightPopup, this);

    if ((viewSet.doesViewFrameExist( ViewConstants.NAVIGATOR_VIEW)) ||
        (viewSet.doesViewFrameExist( ViewConstants.RULE_INSTANCE_VIEW))) {
      mouseRightPopup.addSeparator();
    }
    if (viewSet.doesViewFrameExist( ViewConstants.NAVIGATOR_VIEW)) {
      JMenuItem closeNavWindowsItem = new JMenuItem( "Close Navigator Views");
      createCloseNavigatorWindowsItem( closeNavWindowsItem);
      mouseRightPopup.add( closeNavWindowsItem);
    }
    if (viewSet.doesViewFrameExist( ViewConstants.RULE_INSTANCE_VIEW)) {
      JMenuItem closeRuleWindowsItem = new JMenuItem( "Close Rule Instance Views");
      createCloseRuleWindowsItem( closeRuleWindowsItem);
      mouseRightPopup.add( closeRuleWindowsItem);
    }

    createAllViewItems( partialPlan, partialPlanName, planSequence, viewListenerList,
                        mouseRightPopup);

    ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createActiveTokenItem( final JMenuItem activeTokenItem) {
    activeTokenItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) TokenNetworkView.this.getViewSet()).getActiveToken();
          if (activeToken != null) {
            boolean isByKey = false;
            findAndSelectToken( activeToken, isByKey);
          }
        }
      });
  } // end createActiveTokenItem

  private void createNodeByKeyItem( final JMenuItem nodeByKeyItem) {
    nodeByKeyItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          AskNodeByKey nodeByKeyDialog =
            new AskNodeByKey( "Find by Key", "key (int)", TokenNetworkView.this);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            boolean isByKey = true;
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());
            PwToken tokenToFind = partialPlan.getToken( nodeKey);
            if (tokenToFind != null) {
              findAndSelectToken( tokenToFind, isByKey);
            } else {
              PwRuleInstance ruleInstanceToFind =  partialPlan.getRuleInstance( nodeKey);
              if (ruleInstanceToFind != null) {
                findAndSelectRuleInstance( ruleInstanceToFind);
              }
            }
          }
        }
      });
  } // end createNodeByKeyItem

  /**
   * <code>findAndSelectToken</code>
   *
   * @param tokenToFind - <code>PwToken</code> - 
   * @param isByKey - <code>boolean</code> - 
   */
  public final void findAndSelectToken( final PwToken tokenToFind, final boolean isByKey) {
    boolean isTokenFound = false;
    boolean isHighlightNode = true;
    List tokenNodeList = new ArrayList( tokenNodeMap.values());
    Iterator tokenNodeListItr = tokenNodeList.iterator();
    while (tokenNodeListItr.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeListItr.next();
      if ((tokenNode.getToken() != null) &&
          (tokenNode.getToken().getId().equals( tokenToFind.getId()))) {
        System.err.println( "TokenNetworkView found token: " +
                            tokenToFind.getPredicateName() +
                            " (key=" + tokenToFind.getId().toString() + ")");
        focusNodeId = tokenNode.getToken().getId();
        NodeGenerics.focusViewOnNode( tokenNode, isHighlightNode, jGoView);
        isTokenFound = true;
        break;
      }
    }
    if (isTokenFound && (! isByKey)) {
      NodeGenerics.selectSecondaryNodes
        ( NodeGenerics.mapTokensToTokenNodes
          ( ((PartialPlanViewSet) TokenNetworkView.this.getViewSet()).getSecondaryTokens(),
           tokenNodeList),
          jGoView);
    }
    if (! isTokenFound) {
      // Content Spec filtering may cause this to happen
      String message = "Token " + tokenToFind.getPredicateName() +
        " (key=" + tokenToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Token Not Found in TokenNetworkView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectToken

  public final void findAndSelectRuleInstance( final PwRuleInstance ruleInstanceToFind) {
    boolean isRuleInstanceFound = false;
    boolean isHighlightNode = true;
    List ruleInstanceNodeList = new ArrayList( ruleInstanceNodeMap.values());
    Iterator ruleInstanceNodeListItr = ruleInstanceNodeList.iterator();
    while (ruleInstanceNodeListItr.hasNext()) {
      RuleInstanceNode ruleInstanceNode = (RuleInstanceNode) ruleInstanceNodeListItr.next();
      if ((ruleInstanceNode.getRuleInstance() != null) &&
          (ruleInstanceNode.getRuleInstance().getId().equals( ruleInstanceToFind.getId()))) {
        System.err.println( "TokenNetworkView found ruleInstance: rule " +
                            ruleInstanceToFind.getRuleId() +
                            " (key=" + ruleInstanceToFind.getId().toString() + ")");
        focusNodeId = ruleInstanceNode.getRuleInstance().getId();
        NodeGenerics.focusViewOnNode( ruleInstanceNode, isHighlightNode, jGoView);
        isRuleInstanceFound = true;
        break;
      }
    }
    if (! isRuleInstanceFound) {
      // Content Spec filtering may cause this to happen
      String message = "RuleInstance 'rule " + ruleInstanceToFind.getRuleId() +
        "' (key=" + ruleInstanceToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "RuleInstance Not Found in TokenNetworkView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectRuleInstance

  private void createOverviewWindowItem( final JMenuItem overviewWindowItem,
                                         final TokenNetworkView tokenNetworkView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public final void actionPerformed( final ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( ViewConstants.TOKEN_NETWORK_VIEW, partialPlan,
                                            tokenNetworkView, viewSet, jGoView, viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem


} // end class TokenNetworkView











