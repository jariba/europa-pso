// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TokenNetworkView.java,v 1.3 2003-09-28 00:19:30 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 19June03
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

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
  private Font font;
  private FontMetrics fontMetrics;
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
    this.nodeList = null;
    this.tmpNodeList = new ArrayList();
    this.linkList = new ArrayList();
    this.relationships = new HashMap();
    this.linkNameList = new ArrayList();
    buildTokenParentChildRelationships();
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
    Graphics graphics = ((JPanel) this).getGraphics();
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    // does nothing
    // jGoView.setFont( font);
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();

    jGoDocument = jGoView.getDocument();
    // hiddenLayer = jGoDocument.addLayerBefore( jGoDocument.getDefaultLayer());

    // create all nodes
    createTokenNodes();
    // setVisible( true | false) depending on ContentSpec
    setNodesVisible();

    TokenNetworkLayout layout = new TokenNetworkLayout( jGoDocument, startTimeMSecs);
    layout.performLayout();
    /*long t1 = System.currentTimeMillis();
    TreeRingLayout layout = new TreeRingLayout(linkList, getWidth(), getHeight());
    //layout.ensureAllPositive();
    //layout.position(getWidth(), getHeight());
    System.err.println("Ring layout took " + (System.currentTimeMillis() - t1));*/
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
      redrawView();
    } //end run

  } // end class RedrawViewThread

  private void redrawView() {
    // setVisible(true | false) depending on ids
    setNodesVisible();
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

  /**
   * <code>getFontMetrics</code>
   *
   * @return - <code>FontMetrics</code> - 
   */
  public FontMetrics getFontMetrics()  {
    return fontMetrics;
  }

  private void buildTokenParentChildRelationships() {
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
        Iterator slotIterator = timeline.getSlotList().iterator();
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          Iterator tokenIterator = slot.getTokenList().iterator();
          while (tokenIterator.hasNext()) {
            PwToken token = (PwToken) tokenIterator.next();
            if (token != null) { // empty slot
              relationships.put( token.getId(), new TokenRelations( token));
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
    List tokenRelationIds = new ArrayList();
    Iterator objectIterator = partialPlan.getObjectList().iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      Iterator timelineIterator = object.getTimelineList().iterator();
      while (timelineIterator.hasNext()) {
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        Iterator slotIterator = timeline.getSlotList().iterator();
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          Iterator tokenIterator = slot.getTokenList().iterator();
          while (tokenIterator.hasNext()) {
            PwToken token = (PwToken) tokenIterator.next();
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
      TokenNode freeTokenNode = new TokenNode( (PwToken) freeTokenItr.next(),
                                               new Point( x, y), backgroundColor,
                                               isFreeToken, isDraggable, this);
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
    createTokenParentChildRelationships();
    nodeList = tmpNodeList;
  } // end createTokenParentChildNodes

  private void createTokenNodesOfTimeline( PwTimeline timeline, int x, int y,
                                           Color backgroundColor) {
    boolean isFreeToken = false, isDraggable = false;
    Iterator slotIterator = timeline.getSlotList().iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      Iterator tokenIterator = slot.getTokenList().iterator();
      while (tokenIterator.hasNext()) {
        PwToken token = (PwToken) tokenIterator.next();
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
          createTokenLink( getTokenNode( masterTokenId), tokenNode, "master");
        }
        Iterator slaveTokenItr = tokenRelations.getSlaveTokenIds().iterator();
        while (slaveTokenItr.hasNext()) {
          Integer slaveTokenId = (Integer) slaveTokenItr.next();
          createTokenLink( tokenNode, getTokenNode( slaveTokenId), "slave");
        }
      }
    }
  } // end createTokenParentChildRelationships

  private TokenNode getTokenNode( Integer nodeId) {
    TokenNode tokenNode = null;
    Iterator tokenNodeItr = tmpNodeList.iterator();
    while (tokenNodeItr.hasNext()) {
      tokenNode = (TokenNode) tokenNodeItr.next();
      if (nodeId.equals( tokenNode.getToken().getId())) {
        return tokenNode;
      }
    }
    return tokenNode;
  } // end getTokenNode

 
  private void createTokenLink( TokenNode fromTokenNode, TokenNode toTokenNode,
                                String type) {
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


  private void setNodesVisible() {
    // print content spec
    // System.err.println( "Token Network View - contentSpec");
    // viewSet.printSpec();
    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    Iterator tokenNodeIterator = nodeList.iterator();
    while (tokenNodeIterator.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeIterator.next();
      if (isTokenInContentSpec( tokenNode.getToken())) {
        tokenNode.setVisible( true);
      } else {
        tokenNode.setVisible( false);
      }
    }
    Iterator tokenLinkIterator = linkList.iterator();
    while (tokenLinkIterator.hasNext()) {
      TokenLink tokenLink = (TokenLink) tokenLinkIterator.next();
      if (isTokenInContentSpec( tokenLink.getFromToken()) &&
          isTokenInContentSpec( tokenLink.getToToken())) {
        tokenLink.setVisible( true);
      } else {
        tokenLink.setVisible( false);
      }
    }
    boolean showDialog = true;
    isContentSpecRendered( PlanWorks.TOKEN_NETWORK_VIEW, showDialog);
  } // end setNodesVisible


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
          boolean isTokenFound = false;
          if (activeToken != null) {
            Iterator tokenNodeListItr = nodeList.iterator();
            while (tokenNodeListItr.hasNext()) {
              TokenNode tokenNode = (TokenNode) tokenNodeListItr.next();
              if ((tokenNode.getToken() != null) &&
                  (tokenNode.getToken().getId().equals( activeToken.getId()))) {
                System.err.println( "TokenNetworkView snapToActiveToken: " +
                                    activeToken.getPredicate().getName());
                NodeGenerics.focusViewOnNode( tokenNode, jGoView);
                isTokenFound = true;
                break;
              }
            }
            if (isTokenFound) {
              NodeGenerics.selectSecondaryNodes
                ( NodeGenerics.mapTokensToTokenNodes
                  (((PartialPlanViewSet) TokenNetworkView.this.getViewSet()).
                   getSecondaryTokens(), nodeList),
                  jGoView);
            } else {
              String message = "active token '" + activeToken.getPredicate().getName() +
                "' not found in TokenNetworkView";
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


} // end class TokenNetworkView











