// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: SlotNavNode.java,v 1.3 2004-01-16 19:05:38 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 09jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.Color;
import java.awt.Point;
import java.util.Iterator;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>SlotNavNode</code> - JGo widget to render a plan timeline slot and its neighbors
 *                                   for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SlotNavNode extends ExtendedBasicNode {

  private PwSlot slot;
  private PwTimeline timeline;
  private NavigatorView navigatorView;
  private String nodeLabel;
  private String predicateName;
  private boolean isDebug;
  private boolean areNeighborsShown;
  private int timelineLinkCount;
  private int tokenLinkCount;
  private boolean inLayout;
  private boolean isEmptySlot;

  /**
   * <code>SlotNavNode</code> - constructor 
   *
   * @param slot - <code>PwSlot</code> - 
   * @param slotLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public SlotNavNode( PwSlot slot, Point slotLocation, Color backgroundColor,
                      boolean isDraggable, PartialPlanView partialPlanView) { 
    super( ViewConstants.HEXAGON);
    this.slot = slot;
    timeline = partialPlanView.getPartialPlan().getTimeline( slot.getTimelineId());
    navigatorView = (NavigatorView) partialPlanView;

    isDebug = false;
    // isDebug = true;
    PwToken token = slot.getBaseToken();
    StringBuffer labelBuf = null;
    if (token == null) { // empty slot
      labelBuf = new StringBuffer( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
      predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
      isEmptySlot = true;
    } else {
      labelBuf = new StringBuffer( token.getPredicateName());
      labelBuf.append( " (").append( String.valueOf( slot.getTokenList().size()));
      labelBuf.append( ")");
      predicateName = token.getPredicateName();
      isEmptySlot = false;
    }
    labelBuf.append( "\nkey=").append( slot.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "SlotNavNode: " + nodeLabel);

    inLayout = false;
    areNeighborsShown = false;
    timelineLinkCount = 0;
    tokenLinkCount = 0;

    configure( slotLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( Point timelineLocation, Color backgroundColor,
                                boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( timelineLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
    if (isEmptySlot) {
      setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    }
  } // end configure

  /**
   * <code>equals</code>
   *
   * @param node - <code>SlotNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( SlotNavNode node) {
    return (this.getSlot().getId().equals( node.getSlot().getId()));
  }

  /**
   * <code>getSlot</code>
   *
   * @return - <code>PwSlot</code> - 
   */
  public PwSlot getSlot() {
    return slot;
  }

  /**
   * <code>inLayout</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean inLayout() {
    return inLayout;
  }

  /**
   * <code>setInLayout</code>
   *
   * @param value - <code>boolean</code> - 
   */
  public void setInLayout( boolean value) {
    int width = 1;
    inLayout = value;
    if (value == false) {
      if (isEmptySlot) {
        width = 2;
      }
      setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
      areNeighborsShown = false;
    }
  }

 /**
   * <code>setAreNeighborsShown</code>
   *
   * @param value - <code>boolean</code> - 
   */
  public void setAreNeighborsShown( boolean value) {
    areNeighborsShown = value;
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    return slot.getId().toString();
  }

  /**
   * <code>incrTimelineLinkCount</code>
   *
   */
  public void incrTimelineLinkCount() {
    timelineLinkCount++;
  }

  /**
   * <code>decTimelineLinkCount</code>
   *
   */
  public void decTimelineLinkCount() {
    timelineLinkCount--;
  }

  /**
   * <code>getTimelineLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTimelineLinkCount() {
    return timelineLinkCount;
  }

  /**
   * <code>incrTokenLinkCount</code>
   *
   */
  public void incrTokenLinkCount() {
    tokenLinkCount++;
  }

  /**
   * <code>decTokenLinkCount</code>
   *
   */
  public void decTokenLinkCount() {
    tokenLinkCount--;
  }

  /**
   * <code>getTokenLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTokenLinkCount() {
    return tokenLinkCount;
  }

  /**
   * <code>resetNode</code> - when closed 
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (timelineLinkCount != 0)) {
      System.err.println( "reset slot node: " + slot.getId() +
                          "; timelineLinkCount != 0: " + timelineLinkCount);
    }
    if (isDebug && (tokenLinkCount != 0)) {
      System.err.println( "reset slotnode: " + slot.getId() +
                          "; tokenLinkCount != 0: " + tokenLinkCount);
    }
    timelineLinkCount = 0;
    tokenLinkCount = 0;
  } // end resetNode

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    String operation = "";
    if (areNeighborsShown) {
      operation = "close";
    } else {
      operation = "open";
    }
    StringBuffer tip = new StringBuffer( "<html> ");
    NodeGenerics.getSlotNodeToolTipText( slot, tip);
    if (isDebug) {
      tip.append( " linkCntTimeline ").append( String.valueOf( timelineLinkCount));
      tip.append( " linkCntToken ").append( String.valueOf( tokenLinkCount));
    }
    if (! isEmptySlot) {
      tip.append( "<br> Mouse-L: ").append( operation);
    }
    tip.append("</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview timeline node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( predicateName);
    tip.append( "<br>key=");
    tip.append( slot.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText


  /**
   * <code>doMouseClick</code> - For Model Network View, Mouse-left opens/closes
   *            constarintNode to show variableNodes 
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point dc, Point vc, JGoView view) {
    JGoObject obj = view.pickDocObject( dc, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    SlotNavNode timelineNode = (SlotNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (! isEmptySlot) {
        navigatorView.setStartTimeMSecs( System.currentTimeMillis());
        boolean areTimelinesChanged = false;
        boolean areTokensChanged = false;
        if (! areNeighborsShown) {
          areTimelinesChanged = addSlotTimelines();
          areTokensChanged = addSlotTokens();
          areNeighborsShown = true;
        } else {
          areTimelinesChanged = removeSlotTimelines();
          areTokensChanged = removeSlotTokens();
          areNeighborsShown = false;
        }
        if (areTimelinesChanged || areTokensChanged) {
          navigatorView.setLayoutNeeded();
          navigatorView.setFocusNode( this);
          navigatorView.redraw();
        }
        return true;
      }
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private boolean addSlotTimelines() {
    boolean areNodesChanged = addTimelineNavNodes();
    boolean areLinksChanged = addTimelineToSlotNavLinks();
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addSlotTimelines

  private boolean removeSlotTimelines() {
    boolean areLinksChanged = removeTimelineToSlotNavLinks();
    boolean areNodesChanged = removeTimelineNavNodes();
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeSlotTimelines

  private boolean addSlotTokens() {
    boolean areNodesChanged = addTokenNavNodes();
    boolean areLinksChanged = addSlotToTokenNavLinks();
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addSlotTokens

  private boolean removeSlotTokens() {
    boolean areLinksChanged = removeSlotToTokenNavLinks();
    boolean areNodesChanged = removeTokenNavNodes();
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeSlotTokens

  /**
   * <code>addTimelineNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addTimelineNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    ModelClassNavNode objectNavNode =
      (ModelClassNavNode) navigatorView.objectNavNodeMap.get( timeline.getObjectId());
    TimelineNavNode timelineNavNode =
      (TimelineNavNode) navigatorView.timelineNavNodeMap.get( timeline.getId());
    if (timelineNavNode == null) {
      timelineNavNode =
        new TimelineNavNode( timeline, 
                             new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                        ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                             navigatorView.getTimelineColor( timeline.getId(),
                                                             navigatorView.timelineColorMap),
                             isDraggable, navigatorView);
      navigatorView.timelineNavNodeMap.put( timeline.getId(), timelineNavNode);
      navigatorView.getJGoDocument().addObjectAtTail( timelineNavNode);
    }
    navigatorView.addTimelineNavNode( timelineNavNode);
    areNodesChanged = true;
    return areNodesChanged;
  } // end addTimelineNavNodes

  private boolean removeTimelineNavNodes() {
    boolean areNodesChanged = false;
    TimelineNavNode timelineNavNode =
      (TimelineNavNode) navigatorView.timelineNavNodeMap.get( timeline.getId());
    if ((timelineNavNode != null) && timelineNavNode.inLayout() &&
        (timelineNavNode.getObjectLinkCount() == 0) &&
        (timelineNavNode.getSlotLinkCount() == 0)) {
      navigatorView.removeTimelineNavNode( timelineNavNode);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end removeTimelineNavNodes

  /**
   * <code>addTimelineToSlotNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addTimelineToSlotNavLinks() {
    boolean areLinksChanged = false;
    TimelineNavNode timelineNavNode =
      (TimelineNavNode) navigatorView.timelineNavNodeMap.get( timeline.getId());
    if ((timelineNavNode != null) && timelineNavNode.inLayout()) {
      if (navigatorView.addNavigatorLink( timelineNavNode, this, this)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // addTimelineToSlotNavLinks

  private boolean removeTimelineToSlotNavLinks() {
    boolean areLinksChanged = false;
    TimelineNavNode timelineNavNode =
      (TimelineNavNode) navigatorView.timelineNavNodeMap.get( timeline.getId());
    if ((timelineNavNode != null) && timelineNavNode.inLayout()) {
      String linkName = timelineNavNode.getTimeline().getId().toString() + "->" +
        slot.getId().toString();
      BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
      if ((link != null) && link.inLayout() &&
          navigatorView.removeTimelineToSlotNavLink( link, timelineNavNode, this)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end removeTimelineToSlotNavLinks

  // *************************************************************************

  /**
   * <code>addTokenNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addTokenNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    Iterator tokenIterator = slot.getTokenList().iterator();
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      TokenNavNode tokenNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( token.getId());
      if (tokenNavNode == null) {
        tokenNavNode =
          new TokenNavNode( token, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                              ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                             navigatorView.getTimelineColor( timeline.getId(),
                                                             navigatorView.timelineColorMap),
                             isDraggable, navigatorView);
        navigatorView.tokenNavNodeMap.put( token.getId(), tokenNavNode);
        navigatorView.getJGoDocument().addObjectAtTail( tokenNavNode);
      }
      tokenNavNode.addTokenNavNode();
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addTokenNavNodes

  /**
   * <code>removeTokenNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean removeTokenNavNodes() {
    boolean areNodesChanged = false;
    Iterator tokenIterator = slot.getTokenList().iterator();
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      TokenNavNode tokenNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( token.getId());
      if ((tokenNavNode != null) && tokenNavNode.inLayout() &&
          (tokenNavNode.getSlotLinkCount() == 0) &&
          (tokenNavNode.getVariableLinkCount() == 0)) {
        tokenNavNode.removeTokenNavNode();
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeTokenNavNodes

  /**
   * <code>addSlotToTokenNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addSlotToTokenNavLinks() {
    boolean areLinksChanged = false;
    Iterator tokenIterator = slot.getTokenList().iterator();
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      TokenNavNode tokenNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( token.getId());
      if ((tokenNavNode != null) && tokenNavNode.inLayout()) {
        if (navigatorView.addNavigatorLink( this, tokenNavNode, this)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end addSlotToTokenNavLinks

  /**
   * <code>addSlotToTokenNavLink</code>
   *
   * @param tokenNavNode - <code>TokenNavNode</code> - 
   * @param sourceNode - <code>ExtendedBasicNode</code> - 
   * @return - <code>BasicNodeLink</code> - 
   */
  protected BasicNodeLink addSlotToTokenNavLink( TokenNavNode tokenNavNode,
                                                 ExtendedBasicNode sourceNode) {
    BasicNodeLink returnLink = null;
    String linkName = slot.getId().toString() + "->" +
      tokenNavNode.getToken().getId().toString();
    BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
    if (link == null) {
      link = new BasicNodeLink( this, tokenNavNode, linkName);
      link.setArrowHeads( false, true);
      incrTokenLinkCount();
      tokenNavNode.incrSlotLinkCount();
      returnLink = link;
      navigatorView.navLinkMap.put( linkName, link);
      if (isDebug) {
        System.err.println( "add slot=>token link " + linkName);
      }
    } else {
      if (! link.inLayout()) {
        link.setInLayout( true);
      }
      link.incrLinkCount();
      incrTokenLinkCount();
      tokenNavNode.incrSlotLinkCount();
      if (isDebug) {
        System.err.println( "StoTo1 incr link: " + link.toString() + " to " +
                            link.getLinkCount());
      }
    }
    return returnLink;
  } // end addSlotToTokenNavLink

  /**
   * <code>removeSlotToTokenNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean removeSlotToTokenNavLinks() {
    boolean areLinksChanged = false;
    Iterator tokenIterator = slot.getTokenList().iterator();
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      TokenNavNode tokenNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( token.getId());
      if ((tokenNavNode != null) && tokenNavNode.inLayout()) {
        String linkName = slot.getId().toString() + "->" +
          tokenNavNode.getToken().getId().toString();
        BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
        if ((link != null) && link.inLayout() &&
            removeSlotToTokenNavLink( link, tokenNavNode)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end removeSlotToTokenNavLinks

  /**
   * <code>removeSlotToTokenNavLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   * @param tokenNavNode - <code>TokenNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean removeSlotToTokenNavLink( BasicNodeLink link,
                                              TokenNavNode tokenNavNode) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    decTokenLinkCount();
    tokenNavNode.decSlotLinkCount();
    if (isDebug) {
      System.err.println( "StoTo dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebug) {
        System.err.println( "removeSlotToTokenNavLink: " + link.toString());
      }
      link.setInLayout( false);
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeSlotToTokenNavLink

} // end class SlotNavNode
