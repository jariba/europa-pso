// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: SlotNode.java,v 1.26 2005-06-01 17:16:18 pdaley Exp $
//
// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.partialPlan.timeline;

import java.awt.Color;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;


/**
 * <code>SlotNode</code> - JGo widget to render a timeline slot with a
 *                         label consisting of the slot's predicate name,
 *                         or "-empty-".  Below each slot's border with
 *                         the next one, the start/end time interval is displayed
 *             Object->JGoObject->JGoArea->TextNode->SlotNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SlotNode extends TextNode implements OverviewToolTip {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,  // top
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,       // left
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,  // bottom
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);      // right

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private String predicateName;
  private PwSlot slot;
  private PwTimeline timeline;
  private SlotNode previousSlotNode;
  private boolean isFirstSlot;
  private boolean isLastSlot;
  private int forcedSlotWidth;
  private int scaleFactor;
  private int timelineDisplayMode;
  private TimelineView timelineView;

  private PwDomain startTimeIntervalDomain;
  private PwDomain endTimeIntervalDomain;
  private int earliestStartTime;
  private int latestStartTime;
  private int earliestEndTime;
  private int latestEndTime;
  private int earliestDurationTime;
  private int latestDurationTime;
  private int startDurationTime;
  private int endDurationTime;
  private int startTime;
  private int endTime;
  private int horizonMin;
  private int horizonMax;
  private boolean isEarliestStartMinusInf;
  private boolean isEarliestStartPlusInf;
  private boolean isLatestStartPlusInf;
  private boolean isEarliestEndMinusInf;
  private boolean isEarliestEndPlusInf;   
  private boolean isLatestEndPlusInf;
  private JGoText startTimeIntervalObject;
  private JGoText endTimeIntervalObject;
  private boolean isTimeLabelYLocLevel1;
  private PwToken token;
  private ViewListener viewListener;


  /**
   * <code>SlotNode</code> - constructor 
   *
   * @param nodeLabel - <code>String</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param timeline - <code>PwTimeline</code> - 
   * @param slotLocation - <code>Point</code> - 
   * @param previousSlotNode - <code>SlotNode</code> - 
   * @param isFirstSlot - <code>boolean</code> - 
   * @param isLastSlot - <code>boolean</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param forcedSlotWidth - <code>int</code> - 
   * @param scaleFactor - <code>int</code> - 
   * @param horizonMin - <code>int</code> - 
   * @param horizonMax - <code>int</code> - 
   * @param timelineDisplayMode - <code>int</code> - 
   * @param timelineView - <code>TimelineView</code> - 
   */
  public SlotNode( String nodeLabel, PwSlot slot, PwTimeline timeline, Point slotLocation,
                   SlotNode previousSlotNode, boolean isFirstSlot, boolean isLastSlot,
                   Color backgroundColor, int forcedSlotWidth, int scaleFactor, 
                   int horizonMin, int horizonMax, int timelineDisplayMode,
                   TimelineView timelineView) {
    super( nodeLabel);
    // node label now contains \nkey=nnn
    token = slot.getBaseToken();
    if (token == null) { // empty slot
      this.predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
    } else {
      this.predicateName = token.getPredicateName();
    }
    this.slot = slot;
    this.timeline = timeline;
    this.previousSlotNode = previousSlotNode;
    this.isFirstSlot = isFirstSlot;
    this.isLastSlot = isLastSlot;
    this.timelineView = timelineView;
    this.forcedSlotWidth = forcedSlotWidth;
    this.scaleFactor = scaleFactor;
    this.horizonMin = horizonMin;
    this.horizonMax = horizonMax;
    this.startTimeIntervalObject = null;
    this.endTimeIntervalObject = null;
    this.viewListener = null;
    this.timelineDisplayMode = timelineDisplayMode;
    // System.err.println( "SlotNode: predicateName " + predicateName);

    if (timelineDisplayMode == TimelineView.SHOW_INTERVALS) {
      configure( nodeLabel, slotLocation, backgroundColor);
      renderTimeIntervals();
    } else {
      renderGroundedSlotNode();
      configure( nodeLabel, slotLocation, backgroundColor);
    }
  } // end constructor


  /**
   * <code>SlotNode</code> - constructor - for NodeShapes
   *
   * @param nodeLabel - <code>String</code> - 
   * @param slotLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   */
  public SlotNode( String nodeLabel, Point slotLocation, Color backgroundColor) {
    super( nodeLabel);
    this.slot = null;
    // node label now contains \nkey=nnn
    configure( nodeLabel, slotLocation, backgroundColor);
  } // end constructor


  private final void configure( String nodeLabel, Point slotLocation,
                                Color backgroundColor) {
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    getLabel().setMultiline( true);
    getLabel().setAlignment( JGoText.ALIGN_CENTER);
    setDraggable( false);
    // to override VizView:
    // JGoText.setDefaultFontFaceName( ViewConstants.VIEW_FONT_NAME);
    // JGoText.setDefaultFontSize( ViewConstants.VIEW_FONT_SIZE);

    // getLabel().setFaceName( ViewConstants.VIEW_FONT_NAME);
    // getLabel().setFontSize( ViewConstants.VIEW_FONT_SIZE);

    // do not allow user links
    getTopPort().setVisible( false);
    getLeftPort().setVisible( false);
    getBottomPort().setVisible( false);
    getRightPort().setVisible( false);
    if (timelineDisplayMode == TimelineView.SHOW_INTERVALS) {
      setLocation( (int) slotLocation.getX(), (int) slotLocation.getY());
      setInsets( NODE_INSETS);
    } else {
      // setWidth() screws up the location, so use setInsets() to change 
      // the size of the slot nodes. The label text is always shown even
      // if the node should be smaller than the label. May need to pre-
      // compute the length of the label and truncate it so that the
      // size of the node is not affected.
      int  scaledWidth = forcedSlotWidth * startDurationTime / scaleFactor;
      int newInset = scaledWidth - (int) this.getSize().getWidth() +  
                     ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF;
      newInset /= 2;
      setInsets( new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,   //top
                             newInset,                                      //left
                             ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,   //bottom
                             newInset));                                    //right
      int augmentX = ((startTime - horizonMin) * forcedSlotWidth) / scaleFactor;
     
      setLocation( (int) slotLocation.getX() + augmentX  - 
                    ViewConstants.TIMELINE_VIEW_INSET_SIZE + 2, 
                    (int) (slotLocation.getY() - 3));
//    System.err.println( "SlotNode: configure:  augmented x = " + 
//                        (int) (slotLocation.getX() + augmentX));

    }
    setSelectable( true);
//  System.err.println( "SlotNode: configure: " + nodeLabel + "    width = " + 
//                      this.getSize().getWidth());
//  System.err.println( "SlotNode: configure:  start x = " + slotLocation.getX() + 
//                      "  y = " + slotLocation.getY());
  } // end configure

  /**
   * <code>getPredicateName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getPredicateName() {
    return predicateName;
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
   * <code>getStartTimeIntervalObject</code>
   *
   * @return - <code>JGoText</code> - 
   */
  public JGoText getStartTimeIntervalObject() {
    return startTimeIntervalObject;
  }

  /**
   * <code>getEndTimeIntervalObject</code>
   *
   * @return - <code>JGoText</code> - 
   */
  public JGoText getEndTimeIntervalObject() {
    return endTimeIntervalObject;
  }

  /**
   * <code>getStartTimeIntervalString</code>
   *
   * @return - <code>String</code> - 
   */
  public String getStartTimeIntervalString() {
    return startTimeIntervalDomain.toString();
  }

  /**
   * <code>getEndTimeIntervalString</code>
   *
   * @return - <code>String</code> - 
   */
  public String getEndTimeIntervalString() {
    return endTimeIntervalDomain.toString();
  }

  /**
   * <code>isTimeLabelUpperYLoc</code> - level 1 is less y value
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isTimeLabelYLocLevel1() {
    return isTimeLabelYLocLevel1;
  }

  // always render endInterval
  // render startInterval if isFirstSlot or
  //        previousSlot endInterval != startInterval (adjust spacing so labels
  //              do not overlap)
  private void renderTimeIntervals() {
    PwSlot previousSlot = null;
    if (previousSlotNode != null) {
      previousSlot = previousSlotNode.getSlot();
    }
    startTimeIntervalDomain = slot.getStartTime();
    endTimeIntervalDomain = slot.getEndTime();
    boolean isStartLoc = true;
    boolean hasMatchingTime = true;
    if (previousSlotNode != null) {
      hasMatchingTime = previousSlotNode.getEndTimeIntervalString().equals
        ( startTimeIntervalDomain.toString());
    }
    int yLoc = (int) (this.getLocation().getY() + this.getSize().getHeight());
    if (isFirstSlot || hasMatchingTime) {
      isTimeLabelYLocLevel1 = true;
    } else {
      isTimeLabelYLocLevel1 = (! previousSlotNode.isTimeLabelYLocLevel1());
    }
    if (! isTimeLabelYLocLevel1) {
      yLoc = yLoc + timelineView.getFontMetrics().getMaxAscent() +
        timelineView.getFontMetrics().getMaxDescent();
    }
    if (isFirstSlot || (! hasMatchingTime)) {
      if (! isFirstSlot) {
        this.setLocation( (int) this.getLocation().getX() +
                          ViewConstants.TIMELINE_VIEW_X_DELTA,
                          (int) this.getLocation().getY());
      }
      Point startLoc = new Point( (int) (this.getLocation().getX() -
                                         this.getXOffset( isStartLoc)),
                                  yLoc);
      startTimeIntervalObject =
        renderIntervalText( startTimeIntervalDomain.toString(), startLoc);
    }

    isStartLoc = false;
    Point endLoc = new Point( (int) (this.getLocation().getX() +
                                     this.getSize().getWidth() -
                                     this.getXOffset( isStartLoc)),
                              yLoc);
    endTimeIntervalObject =
      renderIntervalText( endTimeIntervalDomain.toString(), endLoc);
  } // end renderTimeIntervals


  private JGoText renderIntervalText( String text, Point textLoc) {
    JGoText textObject = new JGoText( textLoc, ViewConstants.VIEW_FONT_SIZE,
                                      text, ViewConstants.VIEW_FONT_NAME,
                                      IS_FONT_BOLD, IS_FONT_UNDERLINED,
                                      IS_FONT_ITALIC, TEXT_ALIGNMENT,
                                      IS_TEXT_MULTILINE, IS_TEXT_EDITABLE);
    textObject.setResizable( false);
    textObject.setEditable( false);
    textObject.setDraggable( false);
    textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
    timelineView.getJGoDocument().addObjectAtTail( textObject);
    return textObject;
  } // end renderText


    // offset time interval labels, so they do not overlap previous & current slot nodes
  private int getXOffset( boolean isStartLoc) {
    if (isStartLoc) {
      if (isFirstSlot) {
        return 0;
      } else {
        return SwingUtilities.computeStringWidth( timelineView.getFontMetrics(),
                                                  startTimeIntervalDomain.toString()) / 2;
      }
    } else {
        return SwingUtilities.computeStringWidth( timelineView.getFontMetrics(),
                                                  endTimeIntervalDomain.toString()) / 2;
    }
  } // end getXOffset


  private void renderGroundedSlotNode() {

    int yLoc;
    startTimeIntervalDomain = slot.getStartTime();
    endTimeIntervalDomain = slot.getEndTime();
    boolean isFreeToken = false;

    if ((startTimeIntervalDomain != null) && (endTimeIntervalDomain != null)) {
      String earliestDurationString =
        NodeGenerics.getShortestDuration( isFreeToken, token, startTimeIntervalDomain,
                                          endTimeIntervalDomain);
      String latestDurationString =
        NodeGenerics.getLongestDuration( isFreeToken, token, startTimeIntervalDomain,
                                         endTimeIntervalDomain);
      earliestStartTime = startTimeIntervalDomain.getLowerBoundInt();
      latestStartTime = startTimeIntervalDomain.getUpperBoundInt();
      earliestEndTime = endTimeIntervalDomain.getLowerBoundInt();
      latestEndTime = endTimeIntervalDomain.getUpperBoundInt();
      checkIntervalDomains();
      if (earliestDurationString.equals( DbConstants.MINUS_INFINITY)) {
        earliestDurationTime = DbConstants.MINUS_INFINITY_INT;
      } else if (earliestDurationString.equals( DbConstants.PLUS_INFINITY)) {      
        earliestDurationTime = DbConstants.PLUS_INFINITY_INT;
      } else {
        earliestDurationTime = Integer.parseInt( earliestDurationString);
      }
      if (latestDurationString.equals( DbConstants.PLUS_INFINITY)) {
        latestDurationTime = DbConstants.PLUS_INFINITY_INT;
      } else if (latestDurationString.equals( DbConstants.MINUS_INFINITY)) {
        latestDurationTime = DbConstants.MINUS_INFINITY_INT;
      } else {
        latestDurationTime = Integer.parseInt( latestDurationString);
      }
      startTime = earliestStartTime;
      endTime = latestEndTime;
      startDurationTime = earliestDurationTime;
      endDurationTime = latestDurationTime;

      if (timelineDisplayMode == TimelineView.SHOW_EARLIEST) {
        endTime = earliestEndTime;
        if (isEarliestStartMinusInf || isEarliestStartPlusInf || isEarliestEndMinusInf) {
          //startDurationTime = DbConstants.PLUS_INFINITY_INT;
          startDurationTime = horizonMax - earliestStartTime;
        } else {
          startDurationTime = earliestEndTime - earliestStartTime;
        }
        endDurationTime = startDurationTime;
      } else if (timelineDisplayMode == TimelineView.SHOW_LATEST) {
        startTime = latestStartTime;
        if (isLatestStartPlusInf || isLatestEndPlusInf) {
          //startDurationTime = DbConstants.PLUS_INFINITY_INT;
          startDurationTime = horizonMax - latestStartTime;
        } else {
          startDurationTime = latestEndTime - latestStartTime;
        }
        endDurationTime = startDurationTime;
      }
//    System.err.println( "startTime " + startTime + " endTime " + endTime);
//    System.err.println( "startDurationTime " + startDurationTime + " endDurationTime " +
//                     endDurationTime);

    }

  }

  private void checkIntervalDomains() {
    isEarliestStartMinusInf = false;
    isEarliestStartPlusInf = false;
    if (earliestStartTime == DbConstants.MINUS_INFINITY_INT) {
      isEarliestStartMinusInf = true;
      //earliestStartTime = temporalExtentView.getTimeScaleStart();
    } else if (earliestStartTime == DbConstants.PLUS_INFINITY_INT) {
      isEarliestStartPlusInf = true;
      //earliestStartTime = temporalExtentView.getTimeScaleEnd();
    }
                                                                           
    isLatestStartPlusInf = false;
    if (latestStartTime == DbConstants.PLUS_INFINITY_INT) {
      isLatestStartPlusInf = true;
      //latestStartTime = temporalExtentView.getTimeScaleEnd();
    }
                                                                           
    isEarliestEndMinusInf = false;
    isEarliestEndPlusInf = false;
    if (earliestEndTime == DbConstants.MINUS_INFINITY_INT) {
      isEarliestEndMinusInf = true;
      //earliestEndTime = temporalExtentView.getTimeScaleStart();
    } else if (earliestEndTime == DbConstants.PLUS_INFINITY_INT) {
      isEarliestEndPlusInf = true;
      //earliestEndTime = temporalExtentView.getTimeScaleEnd();
    }
                                                                           
    isLatestEndPlusInf = false;
    if (latestEndTime == DbConstants.PLUS_INFINITY_INT) {
      isLatestEndPlusInf = true;
      //latestEndTime = temporalExtentView.getTimeScaleEnd();
    }
  } // end checkIntervalDomains

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (slot == null) {
      return null;
    }
    int numTokens = slot.getTokenList().size();
    StringBuffer tip = new StringBuffer( "<html> ");
    if (numTokens > 0) {
      NodeGenerics.getSlotNodeToolTipText( slot, tip);
      if (timelineView.getZoomFactor() > 1) {
        tip.append( "<br>slot key=");
        tip.append( slot.getId().toString());
      }
    } else { // empty slot
      if (timelineView.getZoomFactor() > 1) {
        tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
        tip.append( "<br>slot key=");
        tip.append( slot.getId().toString());
      } else {
        return null;
      }
    }
    tip.append( " </html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview slot node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    if (token != null) {
      // tip.append( predicateName);
      NodeGenerics.getSlotNodeToolTipText( slot, tip);
    } else {
      tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
    }
    tip.append( "<br>slot key=");
    tip.append( slot.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>doMouseClick</code>
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    if (slot == null) {
      return false;
    }
    JGoObject obj = view.pickDocObject( docCoords, false);
    SlotNode slotNode = (SlotNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {

    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

  private void mouseRightPopupMenu( Point viewCoords) {
    if (SlotNode.this.getSlot().getBaseToken() != null) {
      JPopupMenu mouseRightPopup = new JPopupMenu();

      JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
      navigatorItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            String viewSetKey = timelineView.getNavigatorViewSetKey();
            MDIInternalFrame navigatorFrame =
              timelineView.openNavigatorViewFrame( viewSetKey);
            Container contentPane = navigatorFrame.getContentPane();
            PwPartialPlan partialPlan = timelineView.getPartialPlan();
            contentPane.add( new NavigatorView( SlotNode.this.getSlot(), partialPlan,
                                                timelineView.getViewSet(),
                                                viewSetKey, navigatorFrame));
          }
        });
      mouseRightPopup.add( navigatorItem);

      JMenuItem activeTokenItem = new JMenuItem( "Set Active Token");
      activeTokenItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            PwToken activeToken = SlotNode.this.getSlot().getBaseToken();
            ((PartialPlanViewSet) timelineView.getViewSet()).setActiveToken( activeToken);
            NodeGenerics.setSecondaryTokensForSlot
              ( activeToken, SlotNode.this.getSlot(),
                (PartialPlanViewSet) timelineView.getViewSet());
            System.err.println( "SlotNode setActiveToken " +
                                activeToken.getPredicateName() +
                                " (key=" + activeToken.getId().toString() + ")");
          }
        });
      mouseRightPopup.add( activeTokenItem);

      ViewGenerics.showPopupMenu( mouseRightPopup, timelineView, viewCoords);
    }
  } // end mouseRightPopupMenu

  /**
   * <code>doUncapturedMouseMove</code> -- handles Auto-Snap of TemporalExtentView
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doUncapturedMouseMove( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    if (slot == null) {
      return false;
    }
    JGoObject obj = view.pickDocObject( docCoords, false);
    SlotNode slotNode = (SlotNode) obj.getTopLevelObject();
    JGoArea currentMouseOverNode = timelineView.getMouseOverNode();
    if ((currentMouseOverNode == null) ||
        ((currentMouseOverNode != null) &&
         ((currentMouseOverNode instanceof TokenNode) ||
          ((currentMouseOverNode instanceof SlotNode) &&
           (! ((SlotNode) currentMouseOverNode).getSlot().getId().equals
            ( slotNode.getSlot().getId())))))) {
      timelineView.setMouseOverNode( slotNode);
      if (! slotNode.getSlot().isEmpty()) { // no empty slots in TemporalExtentView
        String className = PlanWorks.getViewClassName( ViewConstants.TEMPORAL_EXTENT_VIEW);
        if (timelineView.isAutoSnapEnabled() &&
            timelineView.getViewSet().viewExists( className)) {
          ((PartialPlanViewSet) timelineView.getViewSet()).setActiveToken( token);
          NodeGenerics.setSecondaryTokensForSlot
            ( token, slot, (PartialPlanViewSet) timelineView.getViewSet());
          TemporalExtentView temporalExtentView =
            ViewGenerics.getTemporalExtentView( timelineView.getViewSet().
                                                openView( className, viewListener));
          boolean isByKey = false;
          temporalExtentView.findAndSelectToken( token, isByKey);
        } else {
          timelineView.setMouseOverNode( null);
        }
      }
      return true;
    } else {
      return false;
    }
  } // end doUncapturedMouseMove


} // end class SlotNode

