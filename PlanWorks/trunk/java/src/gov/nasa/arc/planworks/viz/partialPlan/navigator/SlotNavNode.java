// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: SlotNavNode.java,v 1.13 2004-08-14 01:39:17 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 09jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

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
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.IncrementalNode;
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
public class SlotNavNode extends ExtendedBasicNode implements IncrementalNode, OverviewToolTip {

  private PwSlot slot;
  private PartialPlanView partialPlanView;
  private PwTimeline timeline;
  private NavigatorView navigatorView;
  private String nodeLabel;
  private String predicateName;
  private boolean isDebug;
  private int linkCount;
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
  public SlotNavNode( final PwSlot slot, final Point slotLocation, final Color backgroundColor,
                      final boolean isDraggable, final PartialPlanView partialPlanView) { 
    super( ViewConstants.HEXAGON);
    this.slot = slot;
    this.partialPlanView = partialPlanView;
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
    setAreNeighborsShown( false);
    linkCount = 0;

    configure( slotLocation, backgroundColor, isDraggable);
  } // end constructor

  /**
   * <code>SlotNavNode</code> - constructor - for NodeShapes
   *
   * @param name - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @param slotLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   */
  public SlotNavNode( final String name, final Integer id, final Point slotLocation,
                      final Color backgroundColor) { 
    super( ViewConstants.HEXAGON);
    this.slot = null;

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = null;
    labelBuf = new StringBuffer( name);
    isEmptySlot = false;
    labelBuf.append( "\nkey=").append( id.toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "SlotNavNode: " + nodeLabel);
    boolean isDraggable = false;
    inLayout = false;
    setAreNeighborsShown( false);
    linkCount = 0;

    configure( slotLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( final Point timelineLocation, final Color backgroundColor,
                                final boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( timelineLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
    if (isEmptySlot) {
      setAreNeighborsShown( true);
      int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
      setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    }
  } // end configure

  /**
   * <code>equals</code>
   *
   * @param node - <code>SlotNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public final boolean equals( final SlotNavNode node) {
    return (this.getSlot().getId().equals( node.getSlot().getId()));
  }

  /**
   * <code>getSlot</code>
   *
   * @return - <code>PwSlot</code> - 
   */
  public final PwSlot getSlot() {
    return slot;
  }

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getId() {
    return slot.getId();
  }

  /**
   * <code>getTypeName</code> - implements IncrementalNode
   *
   * @return - <code>String</code> - 
   */
  public final String getTypeName() {
    return "slot";
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
    if (isEmptySlot) {
      setAreNeighborsShown( true);
      width = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    }
    setPen( new JGoPen( JGoPen.SOLID, width,  ColorMap.getColor( "black")));
  }

  /**
   * <code>resetNode</code> - implements IncrementalNode
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public final void resetNode( final boolean isDbg) {
    setAreNeighborsShown( false);
    if (isDbg && (linkCount != 0)) {
      System.err.println( "reset slot node: " + slot.getId() +
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
    returnList.add( timeline);
    return returnList;
  }

  /**
   * <code>getComponentEntityList</code> - implements IncrementalNode
   *
   * @return - <code>List</code> - of PwEntity
   */
  public final List getComponentEntityList() {
    return slot.getTokenList();
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public final String toString() {
    return slot.getId().toString();
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getToolTipText() {
    if (slot == null) {
      return null;
    }
    String operation = "";
    if (areNeighborsShown()) {
      operation = "close";
    } else {
      operation = "open";
    }
    // StringBuffer tip = new StringBuffer( "<html>slot<br>");
    StringBuffer tip = new StringBuffer( "<html>");
    if (! isEmptySlot) {
      NodeGenerics.getSlotNodeToolTipText( slot, tip);
      if (partialPlanView.getZoomFactor() > 1) {
        tip.append( "<br>key=");
        tip.append( slot.getId().toString());
      }
    } else {
      if (partialPlanView.getZoomFactor() > 1) {
        tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
        tip.append( "<br>key=");
        tip.append( slot.getId().toString());
      } else {
        return null;
      }
    }
    if (isDebug) {
      tip.append( " linkCnt ").append( String.valueOf( linkCount));
    }
    if (! isEmptySlot) {
      tip.append( "<br> Mouse-L: ").append( operation);
    }
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview timeline node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public final String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>");
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
  public final boolean doMouseClick( final int modifiers, final Point dc, final Point vc,
                                     final JGoView view) {
    if (slot == null) {
      return false;
    }
    JGoObject obj = view.pickDocObject( dc, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    SlotNavNode timelineNode = (SlotNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (! isEmptySlot) {
        navigatorView.setStartTimeMSecs( System.currentTimeMillis());
        boolean areObjectsChanged = false;
        if (! areNeighborsShown()) {
          areObjectsChanged = addSlotObjects( this);
          setAreNeighborsShown( true);
        } else {
          areObjectsChanged = removeSlotObjects( this);
          setAreNeighborsShown( false);
        }
        if (areObjectsChanged) {
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

  /**
   * <code>addSlotObjects</code>
   *
   * @param slotNavNode - <code>SlotNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean addSlotObjects( final SlotNavNode slotNavNode) {
    boolean areNodesChanged =
      NavNodeGenerics.addEntityNavNodes( slotNavNode, navigatorView, isDebug);
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.addParentToEntityNavLinks( slotNavNode, navigatorView, isDebug);
     boolean areChildLinksChanged =
       NavNodeGenerics.addEntityToChildNavLinks( slotNavNode, navigatorView, isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    int penWidth = partialPlanView.getOpenJGoPenWidth( partialPlanView.getZoomFactor());
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end addSlotObjects

  private boolean removeSlotObjects( final SlotNavNode slotNavNode) {
    boolean areLinksChanged = false;
    boolean isParentLinkChanged =
      NavNodeGenerics.removeParentToEntityNavLinks( slotNavNode, navigatorView,
                                                    isDebug);
    boolean areChildLinksChanged =
      NavNodeGenerics.removeEntityToChildNavLinks( slotNavNode, navigatorView,
                                                   isDebug);
     if (isParentLinkChanged || areChildLinksChanged) {
       areLinksChanged = true;
     }
    boolean areNodesChanged =
      NavNodeGenerics.removeEntityNavNodes( slotNavNode, navigatorView, isDebug);
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
    return (areNodesChanged || areLinksChanged);
  } // end removeSlotObjects

} // end class SlotNavNode
