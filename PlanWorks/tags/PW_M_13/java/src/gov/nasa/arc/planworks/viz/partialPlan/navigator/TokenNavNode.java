// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNavNode.java,v 1.8 2004-03-02 02:34:17 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 12jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariableContainer;
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
public class TokenNavNode extends ExtendedBasicNode implements NavNode {

  private PwToken token;
  private PwSlot slot;
  private PwObject object;
  private PwPartialPlan partialPlan;
  private NavigatorView navigatorView;
  private String nodeLabel;
  private boolean isDebug;
  private boolean areNeighborsShown;
  private int linkCount;
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
  public TokenNavNode( final PwToken token, final Point tokenLocation,
                       final Color backgroundColor, final boolean isDraggable,
                       final PartialPlanView partialPlanView) { 
    super( ViewConstants.RECTANGLE);
    this.token = token;
    partialPlan = partialPlanView.getPartialPlan();
    slot = null; object = null;
    if (token.getSlotId() != null && !token.getSlotId().equals(DbConstants.noId)) {
      slot = (PwSlot) partialPlan.getSlot( token.getSlotId());
    }
    if (token.getParentId() != null && !token.getParentId().equals(DbConstants.noId)) {
      object = partialPlan.getObject( token.getParentId());
    }
//     if ((token.getSlotId() == null) &&
//         (token.getTimelineId() == null)) {
//       // free token
//     }

    navigatorView = (NavigatorView) partialPlanView;

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( token.getPredicateName());
    labelBuf.append( "\nkey=").append( token.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "TokenNavNode: " + nodeLabel);

    inLayout = false;
    areNeighborsShown = false;
    linkCount = 0;

    configure( tokenLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( final Point tokenLocation, final Color backgroundColor,
                                final boolean isDraggable) {
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
   * <code>getId</code> - implements NavNode
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getId() {
    return token.getId();
  }

  /**
   * <code>getTypeName</code> - implements NavNode
   *
   * @return - <code>String</code> - 
   */
  public final String getTypeName() {
    return "token";
  }

  /**
   * <code>incrLinkCount</code> - implements NavNode
   *
   */
  public final void incrLinkCount() {
    linkCount++;
  }

  /**
   * <code>decLinkCount</code> - implements NavNode
   *
   */
  public final void decLinkCount() {
    linkCount--;
  }

  /**
   * <code>getLinkCount</code> - implements NavNode
   *
   * @return - <code>int</code> - 
   */
  public final int getLinkCount() {
    return linkCount;
  }

  /**
   * <code>inLayout</code> - implements NavNode
   *
   * @return - <code>boolean</code> - 
   */
  public final boolean inLayout() {
    return inLayout;
  }

  /**
   * <code>setInLayout</code> - implements NavNode
   *
   * @param value - <code>boolean</code> - 
   */
  public final void setInLayout( final boolean value) {
    int width = 1;
    inLayout = value;
    if (value == false) {
      setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
      areNeighborsShown = false;
    }
  }

  /**
   * <code>resetNode</code> - - implements NavNode
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public final void resetNode( final boolean isDbg) {
    areNeighborsShown = false;
    if (isDbg && (linkCount != 0)) {
      System.err.println( "reset slot node: " + slot.getId() +
                          "; linkCount != 0: " + linkCount);
    }
    linkCount = 0;
  } // end resetNode

 /**
   * <code>setAreNeighborsShown</code> - implements NavNode
   *
   * @param value - <code>boolean</code> - 
   */
  public final void setAreNeighborsShown( final boolean value) {
    areNeighborsShown = value;
  }

  /**
   * <code>getParentEntityList</code> - implements NavNode
   *
   * @return - <code>List</code> - of PwEntity
   */
  public final List getParentEntityList() {
    List returnList = new ArrayList();
    if (slot != null) {
      returnList.add( slot);
    } else if (object != null) {
      returnList.add( object);
    } else {
      // free token
    }
    Integer masterId = partialPlan.getMasterTokenId( token.getId());
    if (masterId != null) {
      PwToken master = partialPlan.getToken( masterId);
      returnList.add( master);
    }
    return returnList;
  }

  /**
   * <code>getComponentEntityList</code> - implements NavNode
   *
   * @return - <code>List</code> - of PwEntity
   */
  public final List getComponentEntityList() {
    List returnList = new ArrayList();
    // returnList.addAll( token.getVariablesList());
    returnList.addAll( ((PwVariableContainer) token).getVariables());
    Iterator slaveIdItr = partialPlan.getSlaveTokenIds( token.getId()).iterator();
    while (slaveIdItr.hasNext()) {
      returnList.add( partialPlan.getToken( (Integer) slaveIdItr.next()));
    }
    return returnList;
  }

  /**
   * <code>equals</code>
   *
   * @param node - <code>TokenNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public final boolean equals( final TokenNavNode node) {
    return (this.getToken().getId().equals( node.getToken().getId()));
  }

  /**
   * <code>getToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public final PwToken getToken() {
    return token;
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public final String toString() {
    return token.getId().toString();
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getToolTipText() {
    String operation = "";
    if (areNeighborsShown) {
      operation = "close";
    } else {
      operation = "open";
    }
    StringBuffer tip = new StringBuffer( "<html>");
    if (token instanceof PwResourceTransaction) {
      tip.append( "resourceTransaction");
    } else {
      tip.append( "token");
    }
      tip.append( "<br>");
    tip.append( token.toString());
    if (isDebug) {
      tip.append( " linkCnt ").append( String.valueOf( linkCount));
    }
    tip.append( "<br> Mouse-L: ").append( operation);
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public final String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>");
    tip.append( token.getPredicateName());
    tip.append( "<br>key=");
    tip.append( token.getId().toString());
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
  public final boolean doMouseClick( final int modifiers, final Point dc, final Point vc,
                                     final JGoView view) {
    JGoObject obj = view.pickDocObject( dc, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    TokenNavNode tokenNavNode = (TokenNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      boolean areObjectsChanged = false;
      if (! areNeighborsShown) {
        areObjectsChanged = addTokenObjects( this);
        areNeighborsShown = true;
      } else {
        areObjectsChanged = removeTokenObjects( this);
        areNeighborsShown = false;
      }
      if (areObjectsChanged) {
        navigatorView.setLayoutNeeded();
        navigatorView.setFocusNode( this);
        navigatorView.redraw();
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private boolean addTokenObjects( final TokenNavNode tokenNavNode) {
    boolean areNodesChanged =
      NavNodeGenerics.addEntityNavNodes( tokenNavNode, navigatorView, isDebug);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.addParentToEntityNavLinks( tokenNavNode, navigatorView, isDebug);
     boolean areChildLinksChanged =
       NavNodeGenerics.addEntityToChildNavLinks( tokenNavNode, navigatorView, isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addTokenObjects

  private boolean removeTokenObjects( final TokenNavNode tokenNavNode) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.removeParentToEntityNavLinks( tokenNavNode, navigatorView, isDebug);
    boolean areChildLinksChanged =
      NavNodeGenerics.removeEntityToChildNavLinks( tokenNavNode, navigatorView, isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged =
      NavNodeGenerics.removeEntityNavNodes( tokenNavNode, navigatorView, isDebug);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeTokenObjects


} // end class TokenNavNode

