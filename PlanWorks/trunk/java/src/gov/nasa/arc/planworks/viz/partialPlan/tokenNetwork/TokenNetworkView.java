// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TokenNetworkView.java,v 1.6 2003-10-25 00:58:19 taylor Exp $
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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwTokenRelation;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.AskTokenByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>TokenNetworkView</code> - render a partial plan's tokens, their
 *                                 parents and children
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNetworkView extends PartialPlanView {

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private TokenNetworkJGoView jGoView;
  private JGoDocument jGoDocument;
  // private JGoLayer hiddenLayer;
  // nodeList & tmpNodeList used by JFCUnit test case
  private List nodeList; // element TokenNode
  private List tmpNodeList; // element TokenNode
  private List linkList; // element TokenLink
  private Map relationships; // master, slave, self relationships
  private List linkNameList; // element String

  /**
   * <code>TokenNetworkView</code> - constructor - 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> -
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TokenNetworkView( ViewableObject partialPlan,  ViewSet viewSet) {
    super( (PwPartialPlan)partialPlan, (PartialPlanViewSet) viewSet);
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (PartialPlanViewSet) viewSet;
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    jGoView = new TokenNetworkJGoView();
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

    expandViewFrame( this.getClass().getName(),
                     (int) jGoView.getDocumentSize().getWidth(),
                     (int) jGoView.getDocumentSize().getHeight());
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
      boolean isRedraw = true;
      renderTokenNetwork( isRedraw);
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
    nodeList = null;
    tmpNodeList = new ArrayList();
    linkList = new ArrayList();
    linkNameList = new ArrayList();

    jGoDocument = jGoView.getDocument();

    buildTokenParentChildRelationships();
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
  } // end redrawView

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>getNodeList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getNodeList() {
    return nodeList;
  }

  /**
   * <code>getLinkList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getLinkList() {
    return linkList;
  }

  private void buildTokenParentChildRelationships() {
    relationships = new HashMap();
    initRelationships();
    buildRelationships();
//     System.err.println( "Token Network View: count: " + relationships.keySet().size());
//     Iterator keyIterator = relationships.keySet().iterator();
//     while (keyIterator.hasNext()) {
//       System.err.println( relationships.get( (Integer) keyIterator.next()));
//     }
  } // end buildTokenParentChildRelationships 


  private void initRelationships() {
    Iterator objectIterator = partialPlan.getObjectList().iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      Iterator timelineIterator = object.getTimelineList().iterator();
      while (timelineIterator.hasNext()) {
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        if (isTimelineInContentSpec( timeline)) {
          Iterator slotIterator = timeline.getSlotList().iterator();
          while (slotIterator.hasNext()) {
            PwSlot slot = (PwSlot) slotIterator.next();
            if (isSlotInContentSpec( slot)) {
              Iterator tokenIterator = slot.getTokenList().iterator();
              while (tokenIterator.hasNext()) {
                PwToken token = (PwToken) tokenIterator.next();
                if (isTokenInContentSpec( token)) {
                  if (token != null) { // empty slot
                    relationships.put( token.getId(), new TokenRelations( token));
                  }
                }
              }
            }
          }
        }
      }
    }
  } // end initRelationships


  class TokenRelations {

    private PwToken token;
    private List masterTokenIds; // element Integer
    private List slaveTokenIds; // element Integer

    public TokenRelations( PwToken token) {
      this.token = token;
      masterTokenIds = new ArrayList();
      slaveTokenIds = new ArrayList();
    } // end constructor

    public List getMasterTokenIds () {
      return masterTokenIds;
    }

    public void addMasterTokenId( Integer id) {
      masterTokenIds.add( id);
    }

    public List getSlaveTokenIds () {
      return slaveTokenIds;
    }

    public void addSlaveTokenId( Integer id) {
      slaveTokenIds.add( id);
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer( "tokenId: " +
                                              String.valueOf( token.getId()));
      buffer.append( "\n  masterTokenIds: " + masterTokenIds);
      buffer.append( "\n  slaveTokenIds: " + slaveTokenIds);
      return buffer.toString();
    }

  } // end class TokenRelations


  private void buildRelationships() {
    // process each token relation only once
    // List tokenRelationIds = new ArrayList();
    Iterator objectIterator = partialPlan.getObjectList().iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      Iterator timelineIterator = object.getTimelineList().iterator();
      while (timelineIterator.hasNext()) {
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        if (isTimelineInContentSpec( timeline)) {
          Iterator slotIterator = timeline.getSlotList().iterator();
          while (slotIterator.hasNext()) {
            PwSlot slot = (PwSlot) slotIterator.next();
            if (isSlotInContentSpec( slot)) {
              Iterator tokenIterator = slot.getTokenList().iterator();
              while (tokenIterator.hasNext()) {
                PwToken token = (PwToken) tokenIterator.next();
                if (isTokenInContentSpec( token)) {
                  if (token != null) {
                    if (token.getTokenRelationIdsList().size() == 0) {
                      continue;
                    }
                    Integer tokenId = token.getId();
                    TokenRelations tokenRelations =
                      (TokenRelations) relationships.get( tokenId);
                    Iterator tokenRelationIdIterator = token.getTokenRelationIdsList().iterator();
                    while (tokenRelationIdIterator.hasNext()) {
                      PwTokenRelation tokenRelation =
                        partialPlan.getTokenRelation( (Integer) tokenRelationIdIterator.next());
                      if (tokenRelation == null) {
                        continue;
                      }
                      // buildTokenParentChildRelationships printout is complete with
                      // this commented out -- same links are drawn
                      //                 Integer id = tokenRelation.getId();
                      //                 if (tokenRelationIds.indexOf( id) == -1) {
                      //                   tokenRelationIds.add( id);
                      Integer masterTokenId = tokenRelation.getTokenAId();
                      Integer slaveTokenId = tokenRelation.getTokenBId();
                      if (masterTokenId.equals( tokenId)) {
                        tokenRelations.addSlaveTokenId( slaveTokenId);
                      }
                      if (slaveTokenId.equals( tokenId)) {
                        tokenRelations.addMasterTokenId( masterTokenId);
                      }
                      //                 }
                    }
                    relationships.put( tokenId, tokenRelations);
                  }
                }
              }
            }
          }
        }
      }
    }
  } // end buildRelationships


  private void createTokenNodes() {
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT * 2;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    int timelineCnt = 0;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      Iterator timelineIterator = object.getTimelineList().iterator();
      while (timelineIterator.hasNext()) {
        int x = ViewConstants.TIMELINE_VIEW_X_INIT;
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        Color timelineColor =
          ((PartialPlanViewSet) viewSet).getColorStream().getColor( timelineCnt);
        createTokenNodesOfTimeline( timeline, x, y, timelineColor);
        y += 2 * ViewConstants.TIMELINE_VIEW_Y_DELTA;
        timelineCnt++;
      }
    }
    // free tokens
    List freeTokenList = partialPlan.getFreeTokenList();
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    // System.err.println( "token network view freeTokenList " + freeTokenList);
    Iterator freeTokenItr = freeTokenList.iterator();
    boolean isFreeToken = true, isDraggable = false;
    Color backgroundColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
    while (freeTokenItr.hasNext()) {
      PwToken freeToken = (PwToken) freeTokenItr.next();
      if (isTokenInContentSpec( freeToken)) {
        TokenNode freeTokenNode = new TokenNode( freeToken, new Point( x, y),
                                                 backgroundColor, isFreeToken,
                                                 isDraggable, this);
        if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
          x += freeTokenNode.getSize().getWidth() * 0.5;
          freeTokenNode.setLocation( x, y);
        }
        if (!tmpNodeList.contains(freeTokenNode)) {
          tmpNodeList.add( freeTokenNode);
          jGoDocument.addObjectAtTail( freeTokenNode);
          x += freeTokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
        }
      }
    }
    createTokenParentChildRelationships();
    nodeList = tmpNodeList;
  } // end createTokenNodes

  private void createTokenNodesOfTimeline( PwTimeline timeline, int x, int y,
                                           Color backgroundColor) {
    boolean isFreeToken = false, isDraggable = false;
    Iterator slotIterator = timeline.getSlotList().iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      Iterator tokenIterator = slot.getTokenList().iterator();
      while (tokenIterator.hasNext()) {
        PwToken token = (PwToken) tokenIterator.next();
        if (isTokenInContentSpec( token)) {
          TokenNode tokenNode =
            new TokenNode( token, new Point( x, y), backgroundColor, isFreeToken,
                           isDraggable, this);
          if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
            x += tokenNode.getSize().getWidth() * 0.5;
            tokenNode.setLocation( x, y);
          }
          //MIKE
          if (!tmpNodeList.contains(tokenNode)) {
            tmpNodeList.add( tokenNode);
            jGoDocument.addObjectAtTail( tokenNode);
            x += tokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
          }
        }
      }
    }
  } // end createTokenNodes

  private void createTokenParentChildRelationships() {
    Iterator tokenNodeIterator = tmpNodeList.iterator();
    while (tokenNodeIterator.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeIterator.next();
      Integer tokenId = tokenNode.getToken().getId();
      TokenRelations tokenRelations =
        (TokenRelations) relationships.get( tokenId);
      if (tokenRelations != null) {
        Iterator masterTokenItr = tokenRelations.getMasterTokenIds().iterator();
        while (masterTokenItr.hasNext()) {
          Integer masterTokenId = (Integer) masterTokenItr.next();
          if (! masterTokenId.equals( tokenId)) {
            createTokenLink( getTokenNode( masterTokenId), tokenNode, "master");
          }
        }
        Iterator slaveTokenItr = tokenRelations.getSlaveTokenIds().iterator();
        while (slaveTokenItr.hasNext()) {
          Integer slaveTokenId = (Integer) slaveTokenItr.next();
          if (! slaveTokenId.equals( tokenId)) {
            createTokenLink( tokenNode, getTokenNode( slaveTokenId), "slave");
          }
        }
      }
    }
  } // end createTokenParentChildRelationships

  private TokenNode getTokenNode( Integer nodeId) {
    Iterator tokenNodeItr = tmpNodeList.iterator();
    while (tokenNodeItr.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeItr.next();
      if (nodeId.equals( tokenNode.getToken().getId())) {
        return tokenNode;
      }
    }
    return null;
  } // end getTokenNode

 
  private void createTokenLink( TokenNode fromTokenNode, TokenNode toTokenNode,
                                String type) {
    if ((fromTokenNode == null) || (toTokenNode == null)) {
      return;
    }
    String linkName = fromTokenNode.getToken().getId().toString() + "->" +
      toTokenNode.getToken().getId().toString();
    Iterator linkItr = linkNameList.iterator();
    while (linkItr.hasNext()) {
      if (linkName.equals( (String) linkItr.next())) {
        // System.err.println( "discard " + linkName + " type " + type);
        return;
      }
    }
    TokenLink link = new TokenLink( fromTokenNode, toTokenNode);
    linkList.add( link);
    // master/slave & meets/met-by are on the same link ???
//     if (type.equals( "master")) {
//       link.setMidLabel( new JGoText( "meets"));
//     } else if (type.equals( "slave")) {
//       link.setMidLabel( new JGoText( "met-by"));
//     } else {
//       System.err.println( "createTokenLink: type " + type + " not handled");
//     }
//     ((JGoText) link.getMidLabel()).setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);

//     System.err.println( fromTokenNode.getPredicateName() + " " +
//                         fromTokenNode.getToken().getId().toString() + " => " +
//                         toTokenNode.getPredicateName() + " " +
//                         toTokenNode.getToken().getId().toString() + " " + type);
    jGoDocument.addObjectAtTail( link);
    linkNameList.add( linkName);
  } // end createTokenLink


//   private void setNodesVisible() {
//     // print content spec
//     // System.err.println( "Token Network View - contentSpec");
//     // viewSet.printSpec();
//     validTokenIds = viewSet.getValidIds();
//     displayedTokenIds = new ArrayList();
//     Iterator tokenNodeIterator = nodeList.iterator();
//     while (tokenNodeIterator.hasNext()) {
//       TokenNode tokenNode = (TokenNode) tokenNodeIterator.next();
//       if (isTokenInContentSpec( tokenNode.getToken())) {
//         tokenNode.setVisible( true);
//       } else {
//         tokenNode.setVisible( false);
//       }
//     }
//     Iterator tokenLinkIterator = linkList.iterator();
//     while (tokenLinkIterator.hasNext()) {
//       TokenLink tokenLink = (TokenLink) tokenLinkIterator.next();
//       if (isTokenInContentSpec( tokenLink.getFromToken()) &&
//           isTokenInContentSpec( tokenLink.getToToken())) {
//         tokenLink.setVisible( true);
//       } else {
//         tokenLink.setVisible( false);
//       }
//     }
//     boolean showDialog = true;
//     isContentSpecRendered( PlanWorks.TOKEN_NETWORK_VIEW, showDialog);
//   } // end setNodesVisible


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
    JPopupMenu mouseRightPopup = new JPopupMenu();
    JMenuItem tokenByKeyItem = new JMenuItem( "Find Token by Key");
    createTokenByKeyItem( tokenByKeyItem);
    mouseRightPopup.add( tokenByKeyItem);
    JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
    createActiveTokenItem( activeTokenItem);
    mouseRightPopup.add( activeTokenItem);

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

  private void createTokenByKeyItem( JMenuItem tokenByKeyItem) {
    tokenByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskTokenByKey tokenByKeyDialog =
            new AskTokenByKey( partialPlan, "Find Token by Key", "key (int)");
          Integer tokenKey = tokenByKeyDialog.getTokenKey();
          if (tokenKey != null) {
            // System.err.println( "createTokenByKeyItem: tokenKey " + tokenKey.toString());
            PwToken tokenToFind = partialPlan.getToken( tokenKey);
            boolean isByKey = true;
            findAndSelectToken( tokenToFind, isByKey);
          }
        }
      });
  } // end createTokenByKeyItem

  private void findAndSelectToken( PwToken tokenToFind, boolean isByKey) {
    boolean isTokenFound = false;
    Iterator tokenNodeListItr = nodeList.iterator();
    while (tokenNodeListItr.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeListItr.next();
      if ((tokenNode.getToken() != null) &&
          (tokenNode.getToken().getId().equals( tokenToFind.getId()))) {
        System.err.println( "TokenNetworkView found token: " +
                            tokenToFind.getPredicate().getName() +
                            " (key=" + tokenToFind.getId().toString() + ")");
        NodeGenerics.focusViewOnNode( tokenNode, jGoView);
        isTokenFound = true;
        break;
      }
    }
    if (isTokenFound && (! isByKey)) {
      NodeGenerics.selectSecondaryNodes
        ( NodeGenerics.mapTokensToTokenNodes
          (((PartialPlanViewSet) TokenNetworkView.this.getViewSet()).
           getSecondaryTokens(), nodeList),
          jGoView);
    }
    if (! isTokenFound) {
      String message = "Token " + tokenToFind.getPredicate().getName() +
        " (key=" + tokenToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                     "Token Not Found in TokenNetworkView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
      System.exit( 1);
    }
  } // end findAndSelectToken


} // end class TokenNetworkView











