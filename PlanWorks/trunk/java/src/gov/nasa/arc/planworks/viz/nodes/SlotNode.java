// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: SlotNode.java,v 1.21 2003-08-29 22:08:59 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;


/**
 * <code>SlotNode</code> - JGo widget to render a timeline slot with a
 *                         label consisting of the slot's predicate name,
 *                         or "<empty>".  Below each slot's border with
 *                         the next one, the start/end time interval is displayed
 *             Object->JGoObject->JGoArea->TextNode->SlotNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SlotNode extends TextNode {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private String predicateName;
  private PwSlot slot;
  private PwToken previousToken;
  private boolean isFirstSlot;
  private boolean isLastSlot;
  private int objectCnt;
  private TimelineView view;

  private PwDomain startTimeIntervalDomain;
  private PwDomain endTimeIntervalDomain;
  private PwPredicate predicate;
  private JGoText startTimeIntervalObject;
  private JGoText endTimeIntervalObject;


  /**
   * <code>SlotNode</code> - constructor 
   *
   * @param nodeLabel - <code>String</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param slotLocation - <code>Point</code> - 
   * @param previousToken - <code>PwToken</code> - 
   * @param isFirstSlot - <code>boolean</code> - 
   * @param isLastSlot - <code>boolean</code> - 
   * @param objectCnt - <code>int</code> - 
   * @param view - <code>TimelineView</code> - 
   */
  public SlotNode( String nodeLabel, PwSlot slot, Point slotLocation,
                   PwToken previousToken, boolean isFirstSlot, boolean isLastSlot,
                   int objectCnt, TimelineView view) {
    super( nodeLabel);
    this.predicateName = nodeLabel;
    this.slot = slot;
    this.previousToken = previousToken;
    this.isFirstSlot = isFirstSlot;
    this.isLastSlot = isLastSlot;
    this.objectCnt = objectCnt;
    this.view = view;
    this.startTimeIntervalObject = null;
    this.endTimeIntervalObject = null;
    // System.err.println( "SlotNode: predicateName " + predicateName);
    configure( nodeLabel, slotLocation);
  } // end constructor


  private final void configure( String nodeLabel, Point slotLocation) {
    String backGroundColor = null;
    if (nodeLabel.indexOf( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL) >= 0) {
      backGroundColor = ((objectCnt % 2) == 0) ?
        ViewConstants.EVEN_OBJECT_TIMELINE_BG_COLOR :
        ViewConstants.ODD_OBJECT_TIMELINE_BG_COLOR;
    } else {
      backGroundColor = ((objectCnt % 2) == 0) ?
        ViewConstants.EVEN_OBJECT_SLOT_BG_COLOR :
        ViewConstants.ODD_OBJECT_SLOT_BG_COLOR;
    }
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    // to override VizView:
    // JGoText.setDefaultFontFaceName( ViewConstants.TIMELINE_VIEW_FONT_NAME);
    // JGoText.setDefaultFontSize( ViewConstants.TIMELINE_VIEW_FONT_SIZE);

    // getLabel().setFaceName( ViewConstants.TIMELINE_VIEW_FONT_NAME);
    // getLabel().setFontSize( ViewConstants.TIMELINE_VIEW_FONT_SIZE);

    setDraggable( false);
    // do not allow user links
    getTopPort().setVisible( false);
    getLeftPort().setVisible( false);
    getBottomPort().setVisible( false);
    getRightPort().setVisible( false);
    setLocation( (int) slotLocation.getX(), (int) slotLocation.getY());
    setInsets( NODE_INSETS);
    setSelectable( true);

    renderTimeIntervals();

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
   * <code>getStartEndIntervals</code>
   *
   * use startVariable for every token + the endVariable for the last one
   * if a slot is empty, and has no tokens, use the endVariable from
   * the previous slot
   *
   * @param slot - <code>PwSlot</code> - 
   * @param previousToken - <code>PwToken</code> - 
   * @param isLastSlot - <code>boolean</code> - 
   * @return - <code>PwDomain[]</code> - 
   */
  public static PwDomain[] getStartEndIntervals( PwSlot slot, PwToken previousToken,
                                       boolean isLastSlot, boolean alwaysReturnEnd) {
    PwDomain[] intervalArray = new PwDomain[2];
    PwDomain startIntervalDomain = null;
    PwDomain endIntervalDomain = null;
    PwVariable intervalVariable = null, lastIntervalVariable = null;
    PwToken baseToken = slot.getBaseToken();
    PwDomain intervalDomain = null, lastIntervalDomain = null;
    if (baseToken == null) {
      if (previousToken == null) {
        // first slot is empty
        if (isLastSlot == true) {
          // this is also the last slot
          intervalDomain = PwDomain.ZERO_INTERVAL_DOMAIN;
          lastIntervalDomain = PwDomain.INFINITY_INTERVAL_DOMAIN;
        } else {
           intervalDomain = PwDomain.ZERO_INTERVAL_DOMAIN;
        }
      } else {
        // empty slot between filled slots
        intervalVariable = previousToken.getEndVariable();
      }
    } else if (isLastSlot || alwaysReturnEnd) {
      intervalVariable = baseToken.getStartVariable();
      lastIntervalVariable = baseToken.getEndVariable();
    } else {
      intervalVariable = baseToken.getStartVariable();      
    }

    if (intervalVariable == null) {
      startIntervalDomain = intervalDomain;
    } else {
      startIntervalDomain = intervalVariable.getDomain();
    }
    // System.err.println( "startIntervalDomain " + startIntervalDomain.toString());

    if ((lastIntervalVariable != null) || (lastIntervalDomain != null)) {
      if (lastIntervalVariable == null) {
        endIntervalDomain = lastIntervalDomain;
      } else {
        endIntervalDomain = lastIntervalVariable.getDomain();
      }
      // System.err.println( "endIntervalDomain " + endIntervalDomain.toString());
    }
    if (alwaysReturnEnd && (endIntervalDomain == null)) {
      endIntervalDomain = startIntervalDomain;
    }
    intervalArray[0] = startIntervalDomain;
    intervalArray[1] = endIntervalDomain;
    return intervalArray;
  } // end getStartEndIntervals

  /**
   * <code>getShortestDuration</code>
   *
   * @param slot - <code>PwSlot</code> - 
   * @param startTimeIntervalDomain - <code>PwDomain</code> - 
   * @param endTimeIntervalDomain - <code>PwDomain</code> - 
   * @return - <code>String</code> - 
   */
  public static String getShortestDuration( PwSlot slot,
                                            PwDomain startTimeIntervalDomain,
                                            PwDomain endTimeIntervalDomain) {
    PwVariable durationVariable = null;
    if ((slot == null) ||  // free token
        (slot.getBaseToken() == null)) { // empty slot
      int startUpper = startTimeIntervalDomain.getUpperBoundInt();
      int endUpper = endTimeIntervalDomain.getUpperBoundInt();
      int startLower = startTimeIntervalDomain.getLowerBoundInt();
      int endLower = endTimeIntervalDomain.getLowerBoundInt();
      int valueStart = Math.max( startLower, startUpper);
      if (valueStart == PwDomain.PLUS_INFINITY_INT) {
        valueStart = Math.min( startLower, startUpper);
      }
      if (valueStart == PwDomain.MINUS_INFINITY_INT) {
        valueStart = 0;
      }
      int valueEnd = Math.min( endLower, endUpper);
      if (valueEnd == PwDomain.MINUS_INFINITY_INT) {
        valueEnd = Math.max( endLower, endUpper);
      }
      if (valueEnd == PwDomain.PLUS_INFINITY_INT) {
        valueEnd = 0;
      }
      return String.valueOf( Math.abs( valueEnd - valueStart));
    } else {
      durationVariable = slot.getBaseToken().getDurationVariable();
      if (durationVariable != null) {
        return durationVariable.getDomain().getLowerBound();
      } else {
        return "0";
      }
    }
  } // end getEarliestDuration

  /**
   * <code>getLongestDuration</code>
   *
   * @param slot - <code>PwSlot</code> - 
   * @param startTimeIntervalDomain - <code>PwDomain</code> - 
   * @param endTimeIntervalDomain - <code>PwDomain</code> - 
   * @return - <code>String</code> - 
   */
  public static String getLongestDuration( PwSlot slot,
                                           PwDomain startTimeIntervalDomain,
                                           PwDomain endTimeIntervalDomain) {
    PwVariable durationVariable = null;
    if ((slot == null) ||  // free token
        (slot.getBaseToken() == null)) { // empty slot
      String upperBound = endTimeIntervalDomain.getUpperBound();
      String lowerBound = startTimeIntervalDomain.getLowerBound();
      if (upperBound.equals( PwDomain.PLUS_INFINITY)) {
        return PwDomain.PLUS_INFINITY;
      } else if (lowerBound.equals( PwDomain.MINUS_INFINITY)) {
        return PwDomain.PLUS_INFINITY;
      } else {
        return String.valueOf( endTimeIntervalDomain.getUpperBoundInt() -
                               startTimeIntervalDomain.getLowerBoundInt());      
      }
    } else {
      durationVariable = slot.getBaseToken().getDurationVariable();
      if (durationVariable != null) {
        return durationVariable.getDomain().getUpperBound();
      } else {
        return "0";
      }
    }
  } // end getLatestDuration


  private void renderTimeIntervals() {
    boolean alwaysReturnEnd = false;
    PwDomain[] intervalArray = getStartEndIntervals( slot, previousToken, isLastSlot,
                                                     alwaysReturnEnd);
    startTimeIntervalDomain = intervalArray[0];
    endTimeIntervalDomain = intervalArray[1];

    Point startLoc = new Point( (int) this.getLocation().getX() - this.getXOffset(),
                                (int) this.getLocation().getY() +
                                (int) this.getSize().getHeight());
    startTimeIntervalObject =
      renderIntervalText( startTimeIntervalDomain.toString(), startLoc);

    if (endTimeIntervalDomain != null) {
      Point endLoc = new Point( (int) (this.getLocation().getX() +
                                       this.getSize().getWidth()),
                                (int) startLoc.getY());
      endTimeIntervalObject =
        renderIntervalText( endTimeIntervalDomain.toString(), endLoc);
    }
  } // end renderTimeIntervals


  private JGoText renderIntervalText( String text, Point textLoc) {
    JGoText textObject = new JGoText( textLoc, ViewConstants.TIMELINE_VIEW_FONT_SIZE,
                                      text, ViewConstants.TIMELINE_VIEW_FONT_NAME,
                                      ViewConstants.TIMELINE_VIEW_IS_FONT_BOLD,
                                      ViewConstants.TIMELINE_VIEW_IS_FONT_UNDERLINED,
                                      ViewConstants.TIMELINE_VIEW_IS_FONT_ITALIC,
                                      ViewConstants.TIMELINE_VIEW_TEXT_ALIGNMENT,
                                      ViewConstants.TIMELINE_VIEW_IS_TEXT_MULTILINE,
                                      ViewConstants.TIMELINE_VIEW_IS_TEXT_EDITABLE);
    textObject.setResizable( false);
    textObject.setEditable( false);
    textObject.setDraggable( false);
    textObject.setBkColor( ColorMap.getColor( "lightGray"));
    view.getJGoDocument().addObjectAtTail( textObject);
    return textObject;
  } // end renderText


    // offset time interval labels, so they do not overlap previous & current slot nodes
  private int getXOffset() {
    if (isFirstSlot) {
      return 0;
    } else {
      return SwingUtilities.computeStringWidth( view.getFontMetrics(),
                                                startTimeIntervalDomain.toString()) / 2;
    }
  } // end getXOffset


  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (slot.getTokenList().size() > 0) {
      return ((PwToken) slot.getTokenList().get( 0)).toString();
    } else {
      return "";
    }
  } // end getToolTipText

  /**
   * <code>doMouseClick</code>
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point dc, Point vc, JGoView view) {
    JGoObject obj = view.pickDocObject( dc, false);
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
//       System.err.println( "doMouseClick obj class " +
//                           obj.getTopLevelObject().getClass().getName());
//       SlotNode slotNode = (SlotNode) obj.getTopLevelObject();
//       System.err.println( "doMouseClick: slot predicate " + slotNode.getText());

//       JGoSelection jGoSelection = view.getSelection();
//       jGoSelection.clearSelection();
//       System.err.println( "doMouseClick num: " + jGoSelection.getNumObjects());
//       JGoListPosition position = jGoSelection.getFirstObjectPos();
//       while (position != null) {
//         JGoObject object = jGoSelection.getObjectAtPos( position);
//         System.err.println( "doMouseClick selection obj class " +
//                           object.getClass().getName());
//         jGoSelection.toggleSelection( object);
//         // position = jGoSelection.getNextObjectPosAtTop( position);
//         // position = jGoSelection.getNextObjectPos( position);
//         position = jGoSelection.getFirstObjectPos();
//       }
      return true;

    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      // 
    }
    return false;
  } // end doMouseClick   



} // end class SlotNode

