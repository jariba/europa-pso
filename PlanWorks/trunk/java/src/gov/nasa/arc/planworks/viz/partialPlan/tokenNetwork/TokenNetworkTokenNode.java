// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNetworkTokenNode.java,v 1.4 2004-08-26 20:51:27 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 12jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

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
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.IncrementalNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>TokenNetworkTokenNode</code> - JGo widget to render a plan token and its neighbors
 *                                   for the token network view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNetworkTokenNode extends ExtendedBasicNode
  implements IncrementalNode, OverviewToolTip {

  private PwToken token;
  private PartialPlanView partialPlanView;
  private PwPartialPlan partialPlan;
  private TokenNetworkView tokenNetworkView;
  private String nodeLabel;
  private boolean isDebug;
  private int linkCount;
  private boolean inLayout;
  private boolean hasZeroRuleChildren;

  /**
   * <code>TokenNetworkTokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public TokenNetworkTokenNode( final PwToken token, final Point tokenLocation,
                       final Color backgroundColor, final boolean isDraggable,
                       final PartialPlanView partialPlanView) { 
    super( ViewConstants.RECTANGLE);
    this.token = token;
    this.partialPlanView = partialPlanView;
    partialPlan = partialPlanView.getPartialPlan();

    tokenNetworkView = (TokenNetworkView) partialPlanView;

    hasZeroRuleChildren = false;
    int numValidRulInstances = 0;
    Iterator slaveIdItr = partialPlan.getSlaveTokenIds( token.getId()).iterator();
    while (slaveIdItr.hasNext()) {
      Integer ruleInstanceId =
        partialPlan.getToken( (Integer) slaveIdItr.next()).getRuleInstanceId();
      if ((ruleInstanceId != null) && (ruleInstanceId.intValue() > 0)) {
        numValidRulInstances++;
      }
    }
    if (numValidRulInstances == 0) {
      hasZeroRuleChildren = true;
    }
    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( token.getPredicateName());
    labelBuf.append( "\nkey=").append( token.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "TokenNetworkTokenNode: " + nodeLabel);

    inLayout = false;
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
    setAreNeighborsShown( false);
    if (hasZeroRuleChildren) {
      setAreNeighborsShown( true);
      int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
      setPen( new JGoPen( JGoPen.SOLID, penWidth,  ColorMap.getColor( "black")));
    }
  } // end configure

  /**
   * <code>getId</code> - implements IncrementalNode
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getId() {
    return token.getId();
  }

  /**
   * <code>getTypeName</code> - implements IncrementalNode
   *
   * @return - <code>String</code> - 
   */
  public final String getTypeName() {
    return "token";
  }

  /**
   * <code>incrLinkCount</code> - implements IncrementalNode
   *
   */
  public final void incrLinkCount() {
    linkCount++;
  }

  /**
   * <code>decLinkCount</code> - implements IncrementalNode
   *
   */
  public final void decLinkCount() {
    linkCount--;
  }

  /**
   * <code>getLinkCount</code> - implements IncrementalNode
   *
   * @return - <code>int</code> - 
   */
  public final int getLinkCount() {
    return linkCount;
  }

  /**
   * <code>setLinkCount</code>
   *
   * @param cnt - <code>int</code> - 
   */
  public final void setLinkCount( int cnt) {
    linkCount = cnt;
  }

  /**
   * <code>inLayout</code> - implements IncrementalNode
   *
   * @return - <code>boolean</code> - 
   */
  public final boolean inLayout() {
    return inLayout;
  }

  /**
   * <code>setInLayout</code> - implements IncrementalNode
   *
   * @param value - <code>boolean</code> - 
   */
  public final void setInLayout( final boolean value) {
    int width = 1;
    inLayout = value;
    if (value == false) {
      setAreNeighborsShown( false);
    }
    if (hasZeroRuleChildren) {
      setAreNeighborsShown( true);
      width = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    }
    setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
  }

  /**
   * <code>resetNode</code> - - implements IncrementalNode
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public final void resetNode( final boolean isDbg) {
    setAreNeighborsShown( false);
    if (isDbg && (linkCount != 0)) {
      System.err.println( "reset token node: " + token.getId() +
                          "; linkCount != 0: " + linkCount);
    }
    linkCount = 0;
  } // end resetNode

  /**
   * <code>getParentEntityList</code> - implements IncrementalNode
   *
   * @return - <code>List</code> - of PwEntity
   */
  public final List getParentEntityList() {
    List returnList = new ArrayList();
    Integer ruleInstanceId = token.getRuleInstanceId();
    if ((ruleInstanceId != null) && (ruleInstanceId.intValue() > 0)) {
//       System.err.println( "getParentEntityList ruleInstanceId " + ruleInstanceId +
//                           " ruleInstance " + partialPlan.getRuleInstance( ruleInstanceId));
      returnList.add( partialPlan.getRuleInstance( ruleInstanceId));
    }
    return returnList;
  }

  /**
   * <code>getComponentEntityList</code> - implements IncrementalNode
   *
   * @return - <code>List</code> - of PwEntity
   */
  public final List getComponentEntityList() {
    List returnList = new UniqueSet();
    Iterator slaveIdItr = partialPlan.getSlaveTokenIds( token.getId()).iterator();
    while (slaveIdItr.hasNext()) {
      Integer ruleInstanceId =
        partialPlan.getToken( (Integer) slaveIdItr.next()).getRuleInstanceId();
      if ((ruleInstanceId != null) && (ruleInstanceId.intValue() > 0)) {
         returnList.add( partialPlan.getRuleInstance( ruleInstanceId));
//          System.err.println( "getComponentEntityList ruleInstanceId " + ruleInstanceId);
      }
    }
//     System.err.println( "getComponentEntityList returnList len " + returnList.size());
    return new ArrayList( returnList);
  }

  /**
   * <code>equals</code>
   *
   * @param node - <code>TokenNetworkTokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public final boolean equals( final TokenNetworkTokenNode node) {
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
    if (areNeighborsShown()) {
      operation = "close";
    } else {
      operation = "open";
    }
    StringBuffer tip = new StringBuffer( "<html>");
//     if (token instanceof PwResourceTransaction) {
//       tip.append( "resourceTransaction");
//     } else {
//       tip.append( "token");
//     }
//     tip.append( "<br>");
    tip.append( token.toString());
    if (partialPlanView.getZoomFactor() > 1) {
      tip.append( "<br>key=");
      tip.append( token.getId().toString());
    }
    if (isDebug) {
      tip.append( " linkCnt ").append( String.valueOf( linkCount));
    }
    if (! hasZeroRuleChildren) {
      tip.append( "<br> Mouse-L: ").append( operation);
    }
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *                               implements OverviewToolTip
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
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public final boolean doMouseClick( final int modifiers, final Point docCoords,
                                     final Point viewCoords, final JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    TokenNetworkTokenNode tokenNetworkTokenNode =
      (TokenNetworkTokenNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (! hasZeroRuleChildren) {
        tokenNetworkView.setStartTimeMSecs( System.currentTimeMillis());
        boolean areObjectsChanged = false;
        if (! areNeighborsShown()) {
          areObjectsChanged = addTokenObjects( this);
          setAreNeighborsShown( true);
        } else {
          areObjectsChanged = removeTokenObjects( this);
          setAreNeighborsShown( false);
        }
        if (areObjectsChanged) {
          tokenNetworkView.setLayoutNeeded();
          tokenNetworkView.setFocusNode( this);
          tokenNetworkView.redraw( true);
        }
        return true;
      }
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( tokenNetworkTokenNode, viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

  /**
   * <code>addTokenObjects</code>
   *
   * @param tokenNetworkTokenNode - <code>TokenNetworkTokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean addTokenObjects( final TokenNetworkTokenNode tokenNetworkTokenNode) {
    boolean areNodesChanged =
      TokenNetworkGenerics.addEntityTokNetNodes( tokenNetworkTokenNode, tokenNetworkView,
                                         isDebug);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      TokenNetworkGenerics.addParentToEntityTokNetLinks( tokenNetworkTokenNode,
                                                 tokenNetworkView, isDebug);
     boolean areChildLinksChanged =
       TokenNetworkGenerics.addEntityToChildTokNetLinks( tokenNetworkTokenNode,
                                                 tokenNetworkView, isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addTokenObjects

  /**
   * <code>removeTokenObjects</code>
   *
   * @param tokenNetworkTokenNode - <code>TokenNetworkTokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean removeTokenObjects( final TokenNetworkTokenNode tokenNetworkTokenNode) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      TokenNetworkGenerics.removeParentToEntityTokNetLinks( tokenNetworkTokenNode,
                                                    tokenNetworkView, isDebug);
    boolean areChildLinksChanged =
      TokenNetworkGenerics.removeEntityToChildTokNetLinks( tokenNetworkTokenNode,
                                                   tokenNetworkView, isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged =
      TokenNetworkGenerics.removeEntityTokNetNodes( tokenNetworkTokenNode, tokenNetworkView,
                                                    isDebug);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeTokenObjects

  public void mouseRightPopupMenu( final TokenNetworkTokenNode tokenNetworkTokenNode,
                                   final Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
    navigatorItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          String viewSetKey = partialPlanView.getNavigatorViewSetKey();
          MDIInternalFrame navigatorFrame = partialPlanView.openNavigatorViewFrame( viewSetKey);
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = partialPlanView.getPartialPlan();
          contentPane.add( new NavigatorView( tokenNetworkTokenNode.getToken(),
                                              partialPlan, partialPlanView.getViewSet(),
                                              viewSetKey, navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    JMenuItem activeTokenItem = new JMenuItem( "Set Active Token");
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken = TokenNetworkTokenNode.this.getToken();
          ((PartialPlanViewSet) partialPlanView.getViewSet()).setActiveToken( activeToken);
          ((PartialPlanViewSet) partialPlanView.getViewSet()).setSecondaryTokens( null);
          System.err.println( "TokenNode setActiveToken: " +
                              activeToken.getPredicateName() +
                              " (key=" + activeToken.getId().toString() + ")");
        }
      });
    mouseRightPopup.add( activeTokenItem);

    ViewGenerics.showPopupMenu( mouseRightPopup, partialPlanView, viewCoords);
  } // end mouseRightPopupMenu

} // end class TokenNetworkTokenNode

