// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: SlotNavNode.java,v 1.1 2004-01-12 19:46:32 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 09jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>SlotNavNode</code> - JGo widget to render a plan object (class model)
 *                                   with a label for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SlotNavNode extends ExtendedBasicNode {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private PwSlot slot;
  private ExtendedBasicNode parentNode;
  private PartialPlanView partialPlanView;
  private String nodeLabel;
  private boolean isDebug;
  private boolean areNeighborsShown;
  private int timelineLinkCount;
  private int tokenLinkCount;
  private boolean inLayout;
  private boolean isEmptySlot;

  public SlotNavNode( PwSlot slot, ExtendedBasicNode parentNode, Point slotLocation,
                      Color backgroundColor, boolean isDraggable,
                      PartialPlanView partialPlanView) { 
    super( ViewConstants.HEXAGON);
    this.slot = slot;
    this.parentNode = parentNode;
    this.partialPlanView = partialPlanView;

    // isDebug = false;
    isDebug = true;
    PwToken token = slot.getBaseToken();
    StringBuffer labelBuf = null;
    if (token == null) { // empty slot
      labelBuf = new StringBuffer( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
      isEmptySlot = true;
    } else {
      labelBuf = new StringBuffer( token.getPredicate().getName());
      labelBuf.append( " (").append( String.valueOf( slot.getTokenList().size()));
      labelBuf.append( ")");
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
   * <code>getTimeline</code>
   *
   * @return - <code>PwTimeline</code> - 
   */
  public PwSlot getSlot() {
    return slot;
  }

  /**
   * <code>getPartialPlanView</code>
   *
   * @return - <code>PartialPlanView</code> - 
   */
  public PartialPlanView getPartialPlanView() {
    return partialPlanView;
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
    tip.append( nodeLabel);
    if (isDebug) {
      tip.append( " linkCntTimeline ").append( String.valueOf( timelineLinkCount));
      tip.append( " linkCntToken ").append( String.valueOf( tokenLinkCount));
    }
    if (! isEmptySlot) {
      tip.append( "<br> Mouse-L: ").append( operation);
      tip.append(" nearest");
      if ((operation.equals( "open") && (timelineLinkCount == 0)) ||
          (operation.equals( "close") && (timelineLinkCount > 0))) {
        tip.append( " timeline");
      }
      if ((operation.equals( "open") && (timelineLinkCount == 0) && (tokenLinkCount == 0)) ||
          (operation.equals( "close") && (timelineLinkCount > 0) && (tokenLinkCount > 0))) {
        tip.append( " &");
      }
      if ((operation.equals( "open") && (tokenLinkCount == 0)) ||
          (operation.equals( "close") && (tokenLinkCount > 0))) {
        tip.append( " tokens");
      }
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
    tip.append( nodeLabel);
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
        if (! areNeighborsShown) {
//           System.err.println( "doMouseClick: Mouse-L show timeline & token nodes of timeline id " +
//                               timelineNode.getTimeline().getId());
          addSlotTimelines( this, (NavigatorView) partialPlanView);
          addSlotTokens( this, (NavigatorView) partialPlanView);
          areNeighborsShown = true;
        } else {
//           System.err.println( "doMouseClick: Mouse-L hide timeline & token nodes of timeline id " +
//                               timelineNode.getTimeline().getId());
          removeSlotTimelines( this, (NavigatorView) partialPlanView);
          removeSlotTokens( this, (NavigatorView) partialPlanView);
          areNeighborsShown = false;
        }
        return true;
      }
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private void addSlotTimelines( SlotNavNode slotNavNode,
                                 NavigatorView navigatorView) {
    navigatorView.setStartTimeMSecs( System.currentTimeMillis());
//     boolean areNodesChanged = navigatorView.addTimelineNavNodes( slotNavNode);
//     boolean areLinksChanged =
//       navigatorView.addTimelineToSlotNavLinks( slotNavNode);
//     if (areNodesChanged || areLinksChanged) {
//       navigatorView.setLayoutNeeded();
//       navigatorView.setFocusNode( slotNavNode);
//       navigatorView.redraw();
//     }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  } // end addSlotTimelines

  private void removeSlotTimelines( SlotNavNode slotNavNode,
                                    NavigatorView navigatorView) {
    navigatorView.setStartTimeMSecs( System.currentTimeMillis());
//     boolean areLinksChanged =
//       navigatorView.removeTimelineToSlotNavLinks( slotNavNode);
//     boolean areNodesChanged = navigatorView.removeTimelineNavNodes( slotNavNode);
//     if (areNodesChanged || areLinksChanged) {
//       navigatorView.setLayoutNeeded();
//       navigatorView.setFocusNode( slotNavNode);
//       navigatorView.redraw();
//     }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end removeSlotTimelines

  private void addSlotTokens( SlotNavNode slotNavNode,
                                 NavigatorView navigatorView) {
    navigatorView.setStartTimeMSecs( System.currentTimeMillis());
//     boolean areNodesChanged = navigatorView.addTokenNavNodes( slotNavNode);
//     boolean areLinksChanged =
//       navigatorView.addTokenToSlotNavLinks( slotNavNode);
//     if (areNodesChanged || areLinksChanged) {
//       navigatorView.setLayoutNeeded();
//       navigatorView.setFocusNode( slotNavNode);
//       navigatorView.redraw();
//     }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  } // end addSlotTokens

  private void removeSlotTokens( SlotNavNode slotNavNode,
                                    NavigatorView navigatorView) {
    navigatorView.setStartTimeMSecs( System.currentTimeMillis());
//     boolean areLinksChanged =
//       navigatorView.removeTokenToSlotNavLinks( slotNavNode);
//     boolean areNodesChanged = navigatorView.removeTokenNavNodes( slotNavNode);
//     if (areNodesChanged || areLinksChanged) {
//       navigatorView.setLayoutNeeded();
//       navigatorView.setFocusNode( slotNavNode);
//       navigatorView.redraw();
//     }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end removeSlotTokens


} // end class SlotNavNode
