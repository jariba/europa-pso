// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TimelineNavNode.java,v 1.1 2004-01-12 19:46:33 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 06jan04
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
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;


/**
 * <code>TimelineNavNode</code> - JGo widget to render a plan object (class model)
 *                                   with a label for the navigator view
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineNavNode extends ExtendedBasicNode {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private PwTimeline timeline;
  private ExtendedBasicNode parentNode;
  private PartialPlanView partialPlanView;
  private String nodeLabel;
  private boolean isDebug;
  private boolean areNeighborsShown;
  private int objectLinkCount;
  private int slotLinkCount;
  private boolean inLayout;

  public TimelineNavNode( PwTimeline timeline, ExtendedBasicNode parentNode,
                            Point timelineLocation, Color backgroundColor,
                            boolean isDraggable,
                            PartialPlanView partialPlanView) { 
    super( ViewConstants.RIGHT_TRAPEZOID);
    this.timeline = timeline;
    this.parentNode = parentNode;
    this.partialPlanView = partialPlanView;

    // isDebug = false;
    isDebug = true;
    StringBuffer labelBuf = new StringBuffer( timeline.getName());
    labelBuf.append( "\nkey=").append( timeline.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "TimelineNavNode: " + nodeLabel);

    inLayout = false;
    areNeighborsShown = false;
    objectLinkCount = 0;
    slotLinkCount = 0;

    configure( timelineLocation, backgroundColor, isDraggable);
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
  } // end configure

  /**
   * <code>equals</code>
   *
   * @param node - <code>TimelineNavNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( TimelineNavNode node) {
    return (this.getTimeline().getId().equals( node.getTimeline().getId()));
  }

  /**
   * <code>getTimeline</code>
   *
   * @return - <code>PwTimeline</code> - 
   */
  public PwTimeline getTimeline() {
    return timeline;
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
    return timeline.getId().toString();
  }

  /**
   * <code>incrObjectLinkCount</code>
   *
   */
  public void incrObjectLinkCount() {
    objectLinkCount++;
  }

  /**
   * <code>decObjectLinkCount</code>
   *
   */
  public void decObjectLinkCount() {
    objectLinkCount--;
  }

  /**
   * <code>getObjectLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getObjectLinkCount() {
    return objectLinkCount;
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
   * <code>resetNode</code> - when closed 
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug) {
    areNeighborsShown = false;
    if (isDebug && (objectLinkCount != 0)) {
      System.err.println( "reset timeline node: " + timeline.getId() +
                          "; objectLinkCount != 0: " + objectLinkCount);
    }
    if (isDebug && (slotLinkCount != 0)) {
      System.err.println( "reset timeline node: " + timeline.getId() +
                          "; slotLinkCount != 0: " + slotLinkCount);
    }
    objectLinkCount = 0;
    slotLinkCount = 0;
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
    tip.append( timeline.getName());
    if (isDebug) {
      tip.append( " linkCntObj ").append( String.valueOf( objectLinkCount));
      tip.append( " linkCntSlot ").append( String.valueOf( slotLinkCount));
    }
    tip.append( "<br> Mouse-L: ").append( operation);
    tip.append(" nearest");
    if ((operation.equals( "open") && (objectLinkCount == 0)) ||
        (operation.equals( "close") && (objectLinkCount > 0))) {
      tip.append( " object");
    }
    if ((operation.equals( "open") && (objectLinkCount == 0) && (slotLinkCount == 0)) ||
        (operation.equals( "close") && (objectLinkCount > 0) && (slotLinkCount > 0))) {
        tip.append( " &");
    }
    if ((operation.equals( "open") && (slotLinkCount == 0)) ||
        (operation.equals( "close") && (slotLinkCount > 0))) {
      tip.append( " slots");
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
    tip.append( timeline.getName());
    tip.append( "<br>key=");
    tip.append( timeline.getId().toString());
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
    TimelineNavNode timelineNode = (TimelineNavNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (! areNeighborsShown) {
        //System.err.println( "doMouseClick: Mouse-L show object & slot nodes of timeline id " +
        //                    timelineNode.getTimeline().getId());
        addTimelineObjects( this, (NavigatorView) partialPlanView);
        addTimelineSlots( this, (NavigatorView) partialPlanView);
        areNeighborsShown = true;
      } else {
        //System.err.println( "doMouseClick: Mouse-L hide object & slot nodes of timeline id " +
        //                    timelineNode.getTimeline().getId());
        removeTimelineObjects( this, (NavigatorView) partialPlanView);
        removeTimelineSlots( this, (NavigatorView) partialPlanView);
        areNeighborsShown = false;
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  } // end doMouseClick   

  private void addTimelineObjects( TimelineNavNode timelineNavNode,
                                   NavigatorView navigatorView) {
    navigatorView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areNodesChanged = navigatorView.addObjectNavNodes( timelineNavNode);
    boolean areLinksChanged =
      navigatorView.addObjectToTimelineNavLinks( timelineNavNode);
    if (areNodesChanged || areLinksChanged) {
      navigatorView.setLayoutNeeded();
      navigatorView.setFocusNode( timelineNavNode);
      navigatorView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  } // end addTimelineObjects

  private void removeTimelineObjects( TimelineNavNode timelineNavNode,
                                      NavigatorView navigatorView) {
    navigatorView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areLinksChanged =
      navigatorView.removeObjectToTimelineNavLinks( timelineNavNode);
    boolean areNodesChanged = navigatorView.removeObjectNavNodes( timelineNavNode);
    if (areNodesChanged || areLinksChanged) {
      navigatorView.setLayoutNeeded();
      navigatorView.setFocusNode( timelineNavNode);
      navigatorView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end removeTimelineObjects

  private void addTimelineSlots( TimelineNavNode timelineNavNode,
                                   NavigatorView navigatorView) {
    navigatorView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areNodesChanged = navigatorView.addSlotNavNodes( timelineNavNode);
    boolean areLinksChanged =
      navigatorView.addTimelineToSlotNavLinks( timelineNavNode);
    if (areNodesChanged || areLinksChanged) {
      navigatorView.setLayoutNeeded();
      navigatorView.setFocusNode( timelineNavNode);
      navigatorView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  } // end addTimelineSlots

  private void removeTimelineSlots( TimelineNavNode timelineNavNode,
                                      NavigatorView navigatorView) {
    navigatorView.setStartTimeMSecs( System.currentTimeMillis());
    boolean areLinksChanged =
      navigatorView.removeTimelineToSlotNavLinks( timelineNavNode);
    boolean areNodesChanged = navigatorView.removeSlotNavNodes( timelineNavNode);
    if (areNodesChanged || areLinksChanged) {
      navigatorView.setLayoutNeeded();
      navigatorView.setFocusNode( timelineNavNode);
      navigatorView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end removeTimelineSlots


} // end class TimelineNavNode
