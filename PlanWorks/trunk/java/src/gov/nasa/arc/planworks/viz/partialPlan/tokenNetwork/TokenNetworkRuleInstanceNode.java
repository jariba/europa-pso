// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNetworkRuleInstanceNode.java,v 1.3 2004-08-25 18:41:03 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 26feb04
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.nodes.IncrementalNode;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>TokenNetworkRuleInstanceNode</code> - JGo widget to render a plan ruleInstance
 *                       and its neighbors for the token network view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNetworkRuleInstanceNode extends RuleInstanceNode
  implements IncrementalNode, OverviewToolTip {

  private PwRuleInstance ruleInstance;
  private TokenNetworkView tokenNetworkView;

  private int linkCount;
  private boolean inLayout;
  private boolean isDebugPrint;

  /**
   * <code>TokenNetworkRuleInstanceNode</code> - constructor 
   *
   * @param ruleInstance - <code>PwRuleInstance</code> - 
   * @param ruleInstanceLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public TokenNetworkRuleInstanceNode( final PwRuleInstance ruleInstance,
                              final Point ruleInstanceLocation, 
                              final Color backgroundColor, final boolean isDraggable, 
                              final PartialPlanView partialPlanView) { 
    super( ruleInstance, null, null, ruleInstanceLocation, backgroundColor,
          isDraggable, partialPlanView);
    this.ruleInstance = ruleInstance;
    tokenNetworkView = (TokenNetworkView) partialPlanView;
    isDebugPrint = false;
    // isDebugPrint = true;

    inLayout = false;
    setAreNeighborsShown( false);
    linkCount = 0;
  } // end constructor

  /**
   * <code>getId</code> - implements IncrementalNode
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getId() {
    return ruleInstance.getId();
  }

  /**
   * <code>getTypeName</code> - implements IncrementalNode
   *
   * @return - <code>String</code> - 
   */
  public final String getTypeName() {
    return "ruleInstance";
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
    setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
  }

  /**
   * <code>resetNode</code> - implements IncrementalNode
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public final void resetNode( final boolean isDebug) {
    setAreNeighborsShown( false);
    if (isDebug && (linkCount != 0)) {
      System.err.println( "reset ruleInstance node: " + ruleInstance.getId() +
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
    if (ruleInstance.getMasterId() != null) {
      returnList.add( partialPlanView.getPartialPlan().getToken( ruleInstance.getMasterId()));
    }
    return returnList;
  }

  /**
   * <code>getComponentEntityList</code> - implements IncrementalNode
   *
   * @return - <code>List</code> - of PwEntity 
   */
  public final List getComponentEntityList() {
    List returnList = new ArrayList();
    PwPartialPlan partialPlan = partialPlanView.getPartialPlan();
    Iterator slaveIdItr = ruleInstance.getSlaveIdsList().iterator();
    while (slaveIdItr.hasNext()) {
      returnList.add( partialPlan.getToken( (Integer) slaveIdItr.next()));
    }
    return returnList;
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
    // StringBuffer tip = new StringBuffer( "<html>ruleInstance<br>");
    StringBuffer tip = new StringBuffer( "<html>");
    if (partialPlanView.getZoomFactor() > 1) {
      tip.append( "rule ");
      tip.append( ruleInstance.getRuleId().toString());
      tip.append( "<br>key=");
      tip.append( ruleInstance.getId().toString());
      tip.append( "<br>");
    }
    if (isDebugPrint) {
      tip.append( " linkCnt ").append( String.valueOf( linkCount));
      tip.append( "<br>");
    }
    tip.append( "Mouse-L: ").append( operation);
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview ruleInstance node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public final String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>");
    tip.append( "rule ");
    tip.append( ruleInstance.getRuleId().toString());
    tip.append( "<br>key=");
    tip.append( ruleInstance.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText


  /**
   * <code>doMouseClick</code> - For Model Network View, Mouse-left opens/closes
   *            constraintNode to show variableNodes 
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public final boolean doMouseClick( final int modifiers, final Point docCoords,
                                     final Point viewCoords,
                                     final JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    RuleInstanceNode ruleInstanceNode = (RuleInstanceNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      TokenNetworkView tokenNetworkView = (TokenNetworkView) partialPlanView;
      tokenNetworkView.setStartTimeMSecs( System.currentTimeMillis());
      boolean areObjectsChanged = false;
      if (! areNeighborsShown()) {
        areObjectsChanged = addRuleInstanceObjects( this);
        setAreNeighborsShown( true);
      } else {
        areObjectsChanged = removeRuleInstanceObjects( this);
        setAreNeighborsShown( false);
      }
      if (areObjectsChanged) {
        tokenNetworkView.setLayoutNeeded();
        tokenNetworkView.setFocusNode( this);
        tokenNetworkView.redraw( true);
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( ruleInstanceNode, viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

  /**
   * <code>addRuleInstanceObjects</code>
   *
   * @param ruleInstanceNode - <code>TokenNetworkRuleInstanceNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean addRuleInstanceObjects( final TokenNetworkRuleInstanceNode ruleInstanceNode) {
    boolean areNodesChanged =
      TokenNetworkGenerics.addEntityTokNetNodes( ruleInstanceNode, tokenNetworkView,
                                                 isDebugPrint);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      TokenNetworkGenerics.addParentToEntityTokNetLinks( ruleInstanceNode, tokenNetworkView,
                                                      isDebugPrint);
     boolean areChildLinksChanged =
       TokenNetworkGenerics.addEntityToChildTokNetLinks( ruleInstanceNode, tokenNetworkView,
                                                      isDebugPrint);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addRuleInstanceObjects

  private boolean removeRuleInstanceObjects( final TokenNetworkRuleInstanceNode ruleInstanceNode) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      TokenNetworkGenerics.removeParentToEntityTokNetLinks( ruleInstanceNode, tokenNetworkView,
                                                    isDebugPrint);
    boolean areChildLinksChanged =
      TokenNetworkGenerics.removeEntityToChildTokNetLinks( ruleInstanceNode, tokenNetworkView,
                                                   isDebugPrint);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged =
      TokenNetworkGenerics.removeEntityTokNetNodes( ruleInstanceNode, tokenNetworkView,
                                                    isDebugPrint);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeRuleInstanceObjects



} // end class TokenNetworkRuleInstanceNode
