// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: RuleInstanceNavNode.java,v 1.5 2004-08-14 01:39:16 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 26feb04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.nodes.IncrementalNode;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>RuleInstanceNavNode</code> - JGo widget to render a plan ruleInstance and its neighbors
 *                                   for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class RuleInstanceNavNode extends RuleInstanceNode
  implements IncrementalNode, OverviewToolTip {

  private PwRuleInstance ruleInstance;
  private NavigatorView navigatorView;

  private int linkCount;
  private boolean inLayout;
  private boolean isDebugPrint;

  /**
   * <code>RuleInstanceNavNode</code> - constructor 
   *
   * @param ruleInstance - <code>PwRuleInstance</code> - 
   * @param ruleInstanceLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public RuleInstanceNavNode( final PwRuleInstance ruleInstance,
                              final Point ruleInstanceLocation, 
                              final Color backgroundColor, final boolean isDraggable, 
                              final PartialPlanView partialPlanView) { 
    super( ruleInstance, null, null, ruleInstanceLocation, backgroundColor,
          isDraggable, partialPlanView);
    this.ruleInstance = ruleInstance;
    navigatorView = (NavigatorView) partialPlanView;
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
    returnList.addAll( ((PwVariableContainer) ruleInstance).getVariables());
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
    RuleInstanceNavNode ruleInstanceNode = (RuleInstanceNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      NavigatorView navigatorView = (NavigatorView) partialPlanView;
      navigatorView.setStartTimeMSecs( System.currentTimeMillis());
      boolean areObjectsChanged = false;
      if (! areNeighborsShown()) {
        areObjectsChanged = addRuleInstanceObjects( this);
        setAreNeighborsShown( true);
      } else {
        areObjectsChanged = removeRuleInstanceObjects( this);
        setAreNeighborsShown( false);
      }
      if (areObjectsChanged) {
        navigatorView.setLayoutNeeded();
        navigatorView.setFocusNode( this);
        navigatorView.redraw();
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      JPopupMenu menu = new JPopupMenu();
      ViewListener viewListener = null;
      menu.add( ViewGenerics.createRuleInstanceViewItem
                ( (RuleInstanceNode) ruleInstanceNode, partialPlanView, viewListener));
      ViewGenerics.showPopupMenu( menu, partialPlanView, viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

  /**
   * <code>addRuleInstanceObjects</code>
   *
   * @param ruleInstanceNavNode - <code>RuleInstanceNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean addRuleInstanceObjects( final RuleInstanceNavNode ruleInstanceNavNode) {
    boolean areNodesChanged =
      NavNodeGenerics.addEntityNavNodes( ruleInstanceNavNode, navigatorView, isDebugPrint);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.addParentToEntityNavLinks( ruleInstanceNavNode, navigatorView, isDebugPrint);
     boolean areChildLinksChanged =
       NavNodeGenerics.addEntityToChildNavLinks( ruleInstanceNavNode, navigatorView, isDebugPrint);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addRuleInstanceObjects

  private boolean removeRuleInstanceObjects( final RuleInstanceNavNode ruleInstanceNavNode) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.removeParentToEntityNavLinks( ruleInstanceNavNode, navigatorView,
                                                    isDebugPrint);
    boolean areChildLinksChanged =
      NavNodeGenerics.removeEntityToChildNavLinks( ruleInstanceNavNode, navigatorView,
                                                   isDebugPrint);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged =
      NavNodeGenerics.removeEntityNavNodes( ruleInstanceNavNode, navigatorView, isDebugPrint);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeRuleInstanceObjects


} // end class RuleInstanceNavNode
