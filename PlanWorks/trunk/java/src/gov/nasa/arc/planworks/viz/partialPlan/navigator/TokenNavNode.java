// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNavNode.java,v 1.3 2004-02-13 02:37:07 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 12jan04
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
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>TokenNavNode</code> - JGo widget to render a plan token and its neighbors
 *                                   for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNavNode extends ExtendedBasicNode {

  private PwToken token;
  private PwSlot slot;
  private PwTimeline timeline;
  private PwPartialPlan partialPlan;
  private NavigatorView navigatorView;
  private String nodeLabel;
  private boolean isDebug;
  private boolean areNeighborsShown;
  private int slotLinkCount;
  private int variableLinkCount;
  private int masterLinkCount;
  private int slaveLinkCount;
  private boolean inLayout;

  /**
   * <code>TokenNavNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public TokenNavNode( PwToken token, Point tokenLocation, Color backgroundColor,
                       boolean isDraggable, PartialPlanView partialPlanView) { 
    super( ViewConstants.RECTANGLE);
    this.token = token;
    partialPlan = partialPlanView.getPartialPlan();
    if (! token.isFreeToken()) {
      slot =  partialPlan.getSlot( token.getSlotId());
      timeline = partialPlan.getTimeline( slot.getTimelineId());
    }
    navigatorView = (NavigatorView) partialPlanView;

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( token.getPredicateName());
    labelBuf.append( "\nkey=").append( token.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "TokenNavNode: " + nodeLabel);

    inLayout = false;
    areNeighborsShown = false;
    slotLinkCount = 0;
    variableLinkCount = 0;
    masterLinkCount = 0;
    slaveLinkCount = 0;

    configure( tokenLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( Point tokenLocation, Color backgroundColor,
                                boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( tokenLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
  } // end configure

  /**
   * <code>equals</code>
   *
   * @param node - <code>TokenNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( TokenNavNode node) {
    return (this.getToken().getId().equals( node.getToken().getId()));
  }

  /**
   * <code>getToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken() {
    return token;
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
    return token.getId().toString();
  }

  /**
   * <code>incrSlotLinkCount</code>
   *
   */
  public void incrSlotLinkCount() {
    slotLinkCount++;
  }

  /**
   * <code>decSlotLinkCount</code>
   *
   */
  public void decSlotLinkCount() {
    slotLinkCount--;
  }

  /**
   * <code>getSlotLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getSlotLinkCount() {
    return slotLinkCount;
  }

  /**
   * <code>incrVariableLinkCount</code>
   *
   */
  public void incrVariableLinkCount() {
    variableLinkCount++;
  }

  /**
   * <code>decVariableLinkCount</code>
   *
   */
  public void decVariableLinkCount() {
    variableLinkCount--;
  }

  /**
   * <code>getVariableLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getVariableLinkCount() {
    return variableLinkCount;
  }

  /**
   * <code>incrMasterLinkCount</code>
   *
   */
  public void incrMasterLinkCount() {
    masterLinkCount++;
  }

  /**
   * <code>decMasterLinkCount</code>
   *
   */
  public void decMasterLinkCount() {
    masterLinkCount--;
  }

  /**
   * <code>getMasterLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getMasterLinkCount() {
    return masterLinkCount;
  }

  /**
   * <code>incrSlaveLinkCount</code>
   *
   */
  public void incrSlaveLinkCount() {
    slaveLinkCount++;
  }

  /**
   * <code>decSlaveLinkCount</code>
   *
   */
  public void decSlaveLinkCount() {
    slaveLinkCount--;
  }

  /**
   * <code>getSlaveLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getSlaveLinkCount() {
    return slaveLinkCount;
  }

  /**
   * <code>resetNode</code> - when closed 
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (slotLinkCount != 0)) {
      System.err.println( "reset slot node: " + slot.getId() +
                          "; slotLinkCount != 0: " + slotLinkCount);
    }
    if (isDebug && (variableLinkCount != 0)) {
      System.err.println( "reset slotnode: " + slot.getId() +
                          "; variableLinkCount != 0: " + variableLinkCount);
    }
    if (isDebug && (masterLinkCount != 0)) {
      System.err.println( "reset slotnode: " + slot.getId() +
                          "; masterLinkCount != 0: " + masterLinkCount);
    }
    if (isDebug && (slaveLinkCount != 0)) {
      System.err.println( "reset slotnode: " + slot.getId() +
                          "; slaveLinkCount != 0: " + slaveLinkCount);
    }
    slotLinkCount = 0;
    variableLinkCount = 0;
    masterLinkCount = 0;
    slaveLinkCount = 0;
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
    tip.append( token.toString());
    if (isDebug) {
      tip.append( " linkCntSlot ").append( String.valueOf( slotLinkCount));
      tip.append( " linkCntVariable ").append( String.valueOf( variableLinkCount));
    }
    tip.append( "<br> Mouse-L: ").append( operation);
    tip.append("</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( token.getPredicateName());
    tip.append( "<br>key=");
    tip.append( token.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText


  /**
   * <code>addTokenNavNode</code>
   *
   */
  protected void addTokenNavNode() {
    if (isDebug) {
      System.err.println( "add tokenNavNode " + token.getId());
    }
    if (! inLayout()) {
      inLayout = true;
    }
  } // end addTokenNavNode

  /**
   * <code>removeTokenNavNode</code>
   *
   */
  protected void removeTokenNavNode() {
    if (isDebug) {
      System.err.println( "remove tokenNavNode " + token.getId());
    }
    inLayout = false;
    resetNode( isDebug);
  } // end removeTokenNavNode

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
    TokenNavNode tokenNavNode = (TokenNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      boolean areSlotsChanged = false;
      boolean areVariablesChanged = false;
      boolean isMasterChanged = false;
      boolean areSlavesChanged = false;
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      if (! areNeighborsShown) {
        if (! token.isFreeToken()) {
          areSlotsChanged = addTokenSlots();
        }
        areVariablesChanged = addTokenVariables();
        isMasterChanged = addTokenMaster();
        areSlavesChanged = addTokenSlaves();
        areNeighborsShown = true;
      } else {
        if (! token.isFreeToken()) {
          areSlotsChanged = removeTokenSlots();
        }
        areVariablesChanged = removeTokenVariables();
        isMasterChanged = removeTokenMaster();
        areSlavesChanged = removeTokenSlaves();
        areNeighborsShown = false;
      }
      if (areSlotsChanged || areVariablesChanged || isMasterChanged || areSlavesChanged) {
        navigatorView.setLayoutNeeded();
        navigatorView.setFocusNode( this);
        navigatorView.redraw();
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private boolean addTokenSlots() {
    boolean areNodesChanged = addSlotNavNodes();
    boolean areLinksChanged = addSlotToTokenNavLinks();
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addTokenSlots

  private boolean removeTokenSlots() {
    boolean areLinksChanged = removeSlotToTokenNavLinks();
    boolean areNodesChanged = removeSlotNavNodes();
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeTokenSlots

  private boolean addTokenVariables() {
    boolean areNodesChanged = addVariableNavNodes();
    boolean areLinksChanged = addTokenToVariableNavLinks();
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addTokenVariables

  private boolean removeTokenVariables() {
    boolean areLinksChanged = removeTokenToVariableNavLinks();
    boolean areNodesChanged = removeVariableNavNodes();
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeTokenVariables

  // ****************************

  private boolean addTokenMaster() {
    boolean areNodesChanged = addMasterNavNodes();
    boolean areLinksChanged = addMasterToTokenNavLinks();
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addTokenMaster

  private boolean removeTokenMaster() {
    boolean areLinksChanged = removeMasterToTokenNavLinks();
    boolean areNodesChanged = removeMasterNavNodes();
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeTokenMasters

  private boolean addTokenSlaves() {
    boolean areNodesChanged = addSlaveNavNodes();
    boolean areLinksChanged = addTokenToSlaveNavLinks();
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addTokenSlaves

  private boolean removeTokenSlaves() {
    boolean areLinksChanged = removeTokenToSlaveNavLinks();
    boolean areNodesChanged = removeSlaveNavNodes();
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeTokenSlaves

  // **********************************************************

  /**
   * <code>addSlotNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addSlotNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    SlotNavNode slotNavNode =
      (SlotNavNode) navigatorView.slotNavNodeMap.get( slot.getId());
    if (slotNavNode == null) {
      slotNavNode =
        new SlotNavNode( slot, 
                         new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                    ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                         navigatorView.getTimelineColor( timeline.getId()),
                         isDraggable, navigatorView);
      navigatorView.slotNavNodeMap.put( slot.getId(), slotNavNode);
      navigatorView.getJGoDocument().addObjectAtTail( slotNavNode);
    }
    navigatorView.addSlotNavNode( slotNavNode);
    areNodesChanged = true;
    return areNodesChanged;
  } // end addSlotNavNodes

  private boolean removeSlotNavNodes() {
    boolean areNodesChanged = false;
    SlotNavNode slotNavNode =
      (SlotNavNode) navigatorView.slotNavNodeMap.get( slot.getId());
    if ((slotNavNode != null) && slotNavNode.inLayout() &&
        (slotNavNode.getTimelineLinkCount() == 0) &&
        (slotNavNode.getTokenLinkCount() == 0)) {
      navigatorView.removeSlotNavNode( slotNavNode);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end removeSlotNavNodes


  /**
   * <code>addSlotToTokenNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addSlotToTokenNavLinks() {
    boolean areLinksChanged = false;
    SlotNavNode slotNavNode =
      (SlotNavNode) navigatorView.slotNavNodeMap.get( slot.getId());
    if ((slotNavNode != null) && slotNavNode.inLayout()) {
      if (navigatorView.addNavigatorLink( slotNavNode, this, this)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // addSlotToTokenNavLinks

  private boolean removeSlotToTokenNavLinks() {
    boolean areLinksChanged = false;
    SlotNavNode slotNavNode =
      (SlotNavNode) navigatorView.slotNavNodeMap.get( slot.getId());
    if ((slotNavNode != null) && slotNavNode.inLayout()) {
      String linkName = slotNavNode.getSlot().getId().toString() + "->" +
        token.getId().toString();
      BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
      if ((link != null) && link.inLayout() &&
          slotNavNode.removeSlotToTokenNavLink( link, this)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end removeSlotToTokenNavLinks

  // **********************************************************


  /**
   * <code>addVariableNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addVariableNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    Iterator variableIterator = token.getVariablesList().iterator();
    while (variableIterator.hasNext()) {
      PwVariable variable = (PwVariable) variableIterator.next();
      VariableNavNode variableNavNode =
        (VariableNavNode) navigatorView.variableNavNodeMap.get( variable.getId());
      if (variableNavNode == null) {
        Color nodeColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
        if (! token.isFreeToken()) {
          nodeColor = navigatorView.getTimelineColor( timeline.getId());
        }
        variableNavNode =
          new VariableNavNode( variable, new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                                    ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                               nodeColor, isDraggable, navigatorView);
        navigatorView.variableNavNodeMap.put( variable.getId(), variableNavNode);
        navigatorView.getJGoDocument().addObjectAtTail( variableNavNode);
      }
      variableNavNode.addVariableNavNode();
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addVariableNavNodes

  /**
   * <code>removeVariableNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean removeVariableNavNodes() {
    boolean areNodesChanged = false;
    Iterator variableIterator = token.getVariablesList().iterator();
    while (variableIterator.hasNext()) {
      PwVariable variable = (PwVariable) variableIterator.next();
      VariableNavNode variableNavNode =
        (VariableNavNode) navigatorView.variableNavNodeMap.get( variable.getId());
      if ((variableNavNode != null) && variableNavNode.inLayout() &&
          (variableNavNode.getTokenLinkCount() == 0) &&
          (variableNavNode.getConstraintLinkCount() == 0)) {
        variableNavNode.removeVariableNavNode();
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeVariableNavNodes

  /**
   * <code>addTokenToVariableNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addTokenToVariableNavLinks() {
    boolean areLinksChanged = false;
    Iterator variableIterator = token.getVariablesList().iterator();
    while (variableIterator.hasNext()) {
      PwVariable variable = (PwVariable) variableIterator.next();
      VariableNavNode variableNavNode =
        (VariableNavNode) navigatorView.variableNavNodeMap.get( variable.getId());
      if ((variableNavNode != null) && variableNavNode.inLayout()) {
        if (navigatorView.addNavigatorLink( this, variableNavNode, this)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end addTokenToVariableNavLinks

  /**
   * <code>addTokenToVariableNavLink</code>
   *
   * @param variableNavNode - <code>VariableNavNode</code> - 
   * @param sourceNode - <code>ExtendedBasicNode</code> - 
   * @return - <code>BasicNodeLink</code> - 
   */
  protected BasicNodeLink addTokenToVariableNavLink( VariableNavNode variableNavNode,
                                                     ExtendedBasicNode sourceNode) {
    BasicNodeLink returnLink = null;
    String linkName = token.getId().toString() + "->" +
      variableNavNode.getVariable().getId().toString();
    BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
    if (link == null) {
      link = new BasicNodeLink( this, variableNavNode, linkName);
      link.setArrowHeads( false, true);
      incrVariableLinkCount();
      variableNavNode.incrTokenLinkCount();
      returnLink = link;
      navigatorView.navLinkMap.put( linkName, link);
      if (isDebug) {
        System.err.println( "add token=>variable link " + linkName);
      }
    } else {
      if (! link.inLayout()) {
        link.setInLayout( true);
      }
      link.incrLinkCount();
      incrVariableLinkCount();
      variableNavNode.incrTokenLinkCount();
      if (isDebug) {
        System.err.println( "StoTo1 incr link: " + link.toString() + " to " +
                            link.getLinkCount());
      }
    }
    return returnLink;
  } // end addTokenToVariableNavLink


  /**
   * <code>removeTokenToVariableNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean removeTokenToVariableNavLinks() {
    boolean areLinksChanged = false;
    Iterator variableIterator = token.getVariablesList().iterator();
    while (variableIterator.hasNext()) {
      PwVariable variable = (PwVariable) variableIterator.next();
      VariableNavNode variableNavNode =
        (VariableNavNode) navigatorView.variableNavNodeMap.get( variable.getId());
      if ((variableNavNode != null) && variableNavNode.inLayout()) {
        String linkName = token.getId().toString() + "->" +
          variableNavNode.getVariable().getId().toString();
        BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
        if ((link != null) && link.inLayout() &&
            removeTokenToVariableNavLink( link, variableNavNode)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end removeTokenToVariableNavLinks

  /**
   * <code>removeTokenToVariableNavLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   * @param variableNavNode - <code>VariableNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean removeTokenToVariableNavLink( BasicNodeLink link,
                                                  VariableNavNode variableNavNode) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    decVariableLinkCount();
    variableNavNode.decTokenLinkCount();
    if (isDebug) {
      System.err.println( "TtoV dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebug) {
        System.err.println( "removeTokenToVariableNavLink: " + link.toString());
      }
      link.setInLayout( false);
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeTokenToVariableNavLink

  // ************************************************************

  protected boolean addMasterNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    Integer masterId = partialPlan.getMasterTokenId( token.getId());
    if (masterId != null) {
      PwToken master = partialPlan.getToken( masterId);
      TokenNavNode masterNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( masterId);
      if (masterNavNode == null) {
        PwSlot slot =  partialPlan.getSlot( master.getSlotId());
        Color nodeColor = null;
        if (slot == null) { // free token
          nodeColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
        } else {
          PwTimeline timeline = partialPlan.getTimeline( slot.getTimelineId());
          nodeColor = navigatorView.getTimelineColor( timeline.getId());
        }
        masterNavNode =
          new TokenNavNode( master, 
                            new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                       ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                            nodeColor, isDraggable, navigatorView);
        navigatorView.tokenNavNodeMap.put( masterId, masterNavNode);
        navigatorView.getJGoDocument().addObjectAtTail( masterNavNode);
      }
      if (isDebug) {
        System.err.println( "add tokenMasterNavNode " + masterId);
      }
      if (! masterNavNode.inLayout()) {
        masterNavNode.setInLayout( true);
      }
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addMasterNavNode

  private boolean removeMasterNavNodes() {
    boolean areNodesChanged = false;
    Integer masterId = partialPlan.getMasterTokenId( token.getId());
    if (masterId != null) {
      TokenNavNode masterNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( masterId);
      if ((masterNavNode != null) && masterNavNode.inLayout() &&
          (masterNavNode.getMasterLinkCount() == 0) &&
          (masterNavNode.getSlaveLinkCount() == 0) &&
          (masterNavNode.getSlotLinkCount() == 0) &&
          (masterNavNode.getVariableLinkCount() == 0)) {
        if (isDebug) {
          System.err.println( "remove tokenMasterNavNode " + masterId);
        }
        masterNavNode.setInLayout( false);
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeMasterNavNode

  /**
   * <code>addMasterToTokenNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addMasterToTokenNavLinks() {
    boolean areLinksChanged = false;
    Integer masterId = partialPlan.getMasterTokenId( token.getId());
    if (masterId != null) {
      TokenNavNode masterNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( masterId);
      if ((masterNavNode != null) && masterNavNode.inLayout()) {
        if (navigatorView.addNavigatorLink( masterNavNode, this, this)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // addMasterToTokenNavLinks


  /**
   * <code>addTokenToTokenNavLink</code> - master to slave links
   *
   * @param fromNavNode - <code>TokenNavNode</code> - 
   * @param toNavNode - <code>TokenNavNode</code> - 
   * @return - <code>BasicNodeLink</code> - 
   */
  protected BasicNodeLink addTokenToTokenNavLink( TokenNavNode fromNavNode,
                                                  TokenNavNode toNavNode,
                                                  ExtendedBasicNode sourceNode) {
    BasicNodeLink returnLink = null;
    String linkName = fromNavNode.getToken().getId().toString() + "->" +
      toNavNode.getToken().getId().toString();
    BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
    if (link == null) {
      link = new BasicNodeLink( fromNavNode, toNavNode, linkName);
      link.setArrowHeads( false, true);
      fromNavNode.incrMasterLinkCount();
      toNavNode.incrSlaveLinkCount();
      returnLink = link;
      navigatorView.navLinkMap.put( linkName, link);
      if (isDebug) {
        System.err.println( "add token=>token link " + linkName);
      }
    } else {
      if (! link.inLayout()) {
        link.setInLayout( true);
      }
      link.incrLinkCount();
      fromNavNode.incrMasterLinkCount();
      toNavNode.incrSlaveLinkCount();
      if (isDebug) {
        System.err.println( "TtoT1 incr link: " + link.toString() + " to " +
                            link.getLinkCount());
      }
    }
    return returnLink;
  } // end addTokenToVariableNavLink


  /**
   * <code>removeMasterToTokenNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean removeMasterToTokenNavLinks() {
    boolean areLinksChanged = false;
    Integer masterId = partialPlan.getMasterTokenId( token.getId());
    if (masterId != null) {
      TokenNavNode masterNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( masterId);
      if ((masterNavNode != null) && masterNavNode.inLayout()) {
        String linkName = masterId.toString() + "->" + token.getId().toString();
        BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
        if ((link != null) && link.inLayout() &&
            removeMasterToTokenNavLink( link, masterNavNode)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end removeMasterToTokenNavLinks

  /**
   * <code>removeMasterToTokenNavLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   * @param masterNavNode - <code>TokenNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean removeMasterToTokenNavLink( BasicNodeLink link,
                                                TokenNavNode masterNavNode) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    masterNavNode.decMasterLinkCount();
    decSlaveLinkCount();
    if (isDebug) {
      System.err.println( "TtoT dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebug) {
        System.err.println( "removeTokenToTokenNavLink: " + link.toString());
      }
      link.setInLayout( false);
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeMasterToTokenNavLink

  // ******************************************************************

  /**
   * <code>addSlaveNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addSlaveNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    // System.err.println( "addSlaveNavNodes id " + token.getId() + " " +
    //                     partialPlan.getSlaveTokenIds( token.getId()));
    Iterator slaveIdItr = partialPlan.getSlaveTokenIds( token.getId()).iterator();
    while (slaveIdItr.hasNext()) {
      Integer slaveId = (Integer) slaveIdItr.next();
      PwToken slave = partialPlan.getToken( slaveId);
      TokenNavNode slaveNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( slaveId);
      if (slaveNavNode == null) {
        Color nodeColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
        if (! slave.isFreeToken()) {
          PwTimeline timeline =
            navigatorView.getPartialPlan().getTimeline( slave.getTimelineId());
          nodeColor = navigatorView.getTimelineColor( timeline.getId());
        }
        slaveNavNode =
          new TokenNavNode( slave, 
                            new Point( ViewConstants.TIMELINE_VIEW_X_INIT * 2,
                                       ViewConstants.TIMELINE_VIEW_Y_INIT * 2),
                            nodeColor, isDraggable, navigatorView);
        navigatorView.tokenNavNodeMap.put( slaveId, slaveNavNode);
        navigatorView.getJGoDocument().addObjectAtTail( slaveNavNode);
      }
      if (isDebug) {
        System.err.println( "add tokenSlaveNavNode " + slaveId);
      }
      if (! slaveNavNode.inLayout()) {
        slaveNavNode.setInLayout( true);
      }
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addSlaveNavNodes

  /**
   * <code>removeSlaveNavNodes</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean removeSlaveNavNodes() {
    boolean areNodesChanged = false, isDraggable = true;
    Iterator slaveIdItr = partialPlan.getSlaveTokenIds( token.getId()).iterator();
    while (slaveIdItr.hasNext()) {
      Integer slaveId = (Integer) slaveIdItr.next();
      PwToken slave = partialPlan.getToken( slaveId);
      TokenNavNode slaveNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( slaveId);
      if ((slaveNavNode != null) && slaveNavNode.inLayout() &&
          (slaveNavNode.getMasterLinkCount() == 0) &&
          (slaveNavNode.getSlaveLinkCount() == 0) &&
          (slaveNavNode.getSlotLinkCount() == 0) &&
          (slaveNavNode.getVariableLinkCount() == 0)) {
        if (isDebug) {
          System.err.println( "remove tokenSlaveNavNode " + slaveId);
        }
        slaveNavNode.setInLayout( false);
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // end removeSlaveNavNodes

  /**
   * <code>addTokenToSlaveNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean addTokenToSlaveNavLinks() {
    boolean areLinksChanged = false;
    Iterator slaveIdItr = partialPlan.getSlaveTokenIds( token.getId()).iterator();
    while (slaveIdItr.hasNext()) {
      Integer slaveId = (Integer) slaveIdItr.next();
      PwToken slave = partialPlan.getToken( slaveId);
      TokenNavNode slaveNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( slaveId);
      if ((slaveNavNode != null) && slaveNavNode.inLayout()) {
        if (navigatorView.addNavigatorLink( this, slaveNavNode, this)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end addTokenToSlaveNavLinks

  /**
   * <code>removeTokenToSlaveNavLinks</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean removeTokenToSlaveNavLinks() {
    boolean areLinksChanged = false;
    Iterator slaveIdItr = partialPlan.getSlaveTokenIds( token.getId()).iterator();
    while (slaveIdItr.hasNext()) {
      Integer slaveId = (Integer) slaveIdItr.next();
      PwToken slave = partialPlan.getToken( slaveId);
      TokenNavNode slaveNavNode =
        (TokenNavNode) navigatorView.tokenNavNodeMap.get( slaveId);
      if ((slaveNavNode != null) && slaveNavNode.inLayout()) {
        String linkName = token.getId().toString() + "->" + slaveId.toString();
        BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
        if ((link != null) && link.inLayout() &&
            removeTokenToSlaveNavLink( link, slaveNavNode)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end removeTokenToSlaveNavLinks

  /**
   * <code>removeTokenToSlaveNavLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   * @param slaveNavNode - <code>TokenNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean removeTokenToSlaveNavLink( BasicNodeLink link,
                                               TokenNavNode slaveNavNode) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    decMasterLinkCount();
    slaveNavNode.decSlaveLinkCount();
    if (isDebug) {
      System.err.println( "TtoT dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebug) {
        System.err.println( "removeTokenToTokenNavLink: " + link.toString());
      }
      link.setInLayout( false);
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeTokenToSlaveNavLink


} // end class TokenNavNode

