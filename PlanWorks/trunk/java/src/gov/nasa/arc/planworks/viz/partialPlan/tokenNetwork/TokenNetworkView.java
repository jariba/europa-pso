// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TokenNetworkView.java,v 1.38 2004-03-24 02:31:05 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 19June03
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
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

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwTokenRelation;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
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
  private MDIInternalFrame viewFrame;
  private TokenNetworkJGoView jGoView;
  private JGoDocument jGoDocument;
  private Map tokenNodeMap; // key = tokenId, element TokenNode
  private Map tokenLinkMap; // key = linkName, element TokenLink
  private boolean isStepButtonView;

  /**
   * <code>TokenNetworkView</code> - constructor - 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> -
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TokenNetworkView( ViewableObject partialPlan,  ViewSet viewSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    tokenNetworkViewInit(viewSet);
    isStepButtonView = false;
    // print content spec
    // viewSet.printSpec();

    SwingUtilities.invokeLater( runInit);
  } // end constructor

  public TokenNetworkView(ViewableObject partialPlan, ViewSet viewSet, PartialPlanViewState s) {
    super((PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    tokenNetworkViewInit(viewSet);
    isStepButtonView = true;
    setState(s);
    SwingUtilities.invokeLater(runInit);
  }

  private void tokenNetworkViewInit(ViewSet viewSet) {
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (PartialPlanViewSet) viewSet;
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    jGoView = new TokenNetworkJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);
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
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for TimelineView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "tokenNetworkView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);

    boolean isRedraw = false;
    renderTokenNetwork( isRedraw);

    viewFrame = viewSet.openView( this.getClass().getName());
    Rectangle documentBounds = jGoView.getDocument().computeBounds();
    jGoView.getDocument().setDocumentSize( (int) documentBounds.getWidth() +
                                           (ViewConstants.TIMELINE_VIEW_X_INIT * 4),
                                           (int) documentBounds.getHeight() +
                                           (ViewConstants.TIMELINE_VIEW_Y_INIT * 2));
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
    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end init


  public void setState( PartialPlanViewState s) {
    super.setState( s);
    if(s == null) {
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
  public void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      boolean isRedraw = true;
      renderTokenNetwork( isRedraw);
      addStepButtons( jGoView);
      // causes bottom view edge to creep off screen
//       if (! isStepButtonView) {
//         expandViewFrameForStepButtons( viewFrame, jGoView);
//       }
    } //end run

  } // end class RedrawViewThread

  private void renderTokenNetwork( boolean isRedraw) {
    if (isRedraw) {
      jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
      System.err.println( "Redrawing Token Network View ...");
      startTimeMSecs = System.currentTimeMillis();
      this.setVisible( false);
    }
    jGoView.getDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    tokenNodeMap = new HashMap();
    tokenLinkMap = new HashMap();

    jGoDocument = jGoView.getDocument();

   // create all nodes
    createTokenNodes();

    boolean showDialog = true;
    isContentSpecRendered( PlanWorks.TOKEN_NETWORK_VIEW, showDialog);

    TokenNetworkLayout layout = new TokenNetworkLayout( jGoDocument, startTimeMSecs);
    layout.performLayout();
    /*long t1 = System.currentTimeMillis();
    TreeRingLayout layout = new TreeRingLayout(linkList, getWidth(), getHeight());
    //layout.ensureAllPositive();
    //layout.position(getWidth(), getHeight());
    System.err.println("Ring layout took " + (System.currentTimeMillis() - t1));*/

    if (isRedraw) {
      this.setVisible( true);
      jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
    }
  } // end renderTokenNetwork

  /**
   * <code>getJGoView</code> - 
   *
   * @return - <code>JGoView</code> - 
   */
  public JGoView getJGoView()  {
    return jGoView;
  }

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoDocument;
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
        backgroundColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
      }
      if (isTokenInContentSpec( token)) {
        TokenNode tokenNode =
          new TokenNode( token, slot, new Point( x, y), backgroundColor, isFreeToken,
                         isDraggable, this);
        if (tokenNodeMap.get( token.getId()) == null) {
          tokenNodeMap.put( token.getId(), tokenNode);
          jGoDocument.addObjectAtTail( tokenNode);
          x += tokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
        }
      }
    }

    createTokenParentChildRelationships();
   } // end createTokenNodes

  private void createTokenParentChildRelationships() {
    List tokenNodeKeyList = new ArrayList( tokenNodeMap.keySet());
    Iterator tokenNodeKeyItr = tokenNodeKeyList.iterator();
    while (tokenNodeKeyItr.hasNext()) {
      TokenNode tokenNode =
        (TokenNode) tokenNodeMap.get( (Integer) tokenNodeKeyItr.next());
      Integer tokenId = tokenNode.getToken().getId();
      Integer masterTokenId = partialPlan.getMasterTokenId( tokenId);
      if (masterTokenId != null) {
        TokenNode masterToken = (TokenNode) tokenNodeMap.get( masterTokenId);
        if ((masterToken != null) && (! masterTokenId.equals( tokenId))) {
          createTokenLink( masterToken, tokenNode, "master");
        }
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
  } // end createTokenParentChildRelationships

  private void createTokenLink( TokenNode fromTokenNode, TokenNode toTokenNode,
                                String type) {
    String linkName = fromTokenNode.getToken().getId().toString() + "->" +
      toTokenNode.getToken().getId().toString();
//     if (tokenLinkMap.get( linkName) != null) {
//       System.err.println( "createTokenLink discard " + linkName);
//       return;
//     }
    // getOpenJGoPenWidth( getZoomFactor()) = 2, with zoomFactor = 1, but
    // this view is not being redrawn when zoomFactor changes, so we need
    // the extra width for the higher zoomFactors
    TokenLink link = new TokenLink( fromTokenNode, toTokenNode,
                                    getOpenJGoPenWidth( getZoomFactor()));
    tokenLinkMap.put( linkName, link);
    jGoDocument.addObjectAtTail( link);
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
    public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick

  } // end class TokenNetworkJGoView


  private void mouseRightPopupMenu( Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
    createNodeByKeyItem( nodeByKeyItem);
    mouseRightPopup.add( nodeByKeyItem);

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         PlanWorks.TOKEN_NETWORK_VIEW);

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

    if (doesViewFrameExist( PlanWorks.NAVIGATOR_VIEW)) {
      mouseRightPopup.addSeparator();
      JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
      createCloseNavigatorWindowsItem( closeWindowsItem);
      mouseRightPopup.add( closeWindowsItem);
    }
    createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createActiveTokenItem( JMenuItem activeTokenItem) {
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) TokenNetworkView.this.getViewSet()).getActiveToken();
          if (activeToken != null) {
            boolean isByKey = false;
            findAndSelectToken( activeToken, isByKey);
          }
        }
      });
  } // end createActiveTokenItem

  private void createNodeByKeyItem( JMenuItem nodeByKeyItem) {
    nodeByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskNodeByKey nodeByKeyDialog =
            new AskNodeByKey( "Find by Key", "key (int)", TokenNetworkView.this);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());
            PwToken tokenToFind = partialPlan.getToken( nodeKey);
            boolean isByKey = true;
            findAndSelectToken( tokenToFind, isByKey);
          }
        }
      });
  } // end createNodeByKeyItem

  private void findAndSelectToken( PwToken tokenToFind, boolean isByKey) {
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
        NodeGenerics.focusViewOnNode( tokenNode, isHighlightNode, jGoView);
        isTokenFound = true;
        break;
      }
    }
    if (isTokenFound && (! isByKey)) {
      NodeGenerics.selectSecondaryNodes
        ( NodeGenerics.mapTokensToTokenNodes
          (((PartialPlanViewSet) TokenNetworkView.this.getViewSet()).getSecondaryTokens(),
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

  private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                         final TokenNetworkView tokenNetworkView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public void actionPerformed( ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( PlanWorks.TOKEN_NETWORK_VIEW, partialPlan,
                                            tokenNetworkView, viewSet, jGoView, viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem


} // end class TokenNetworkView











