// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: SlotNode.java,v 1.11 2003-07-03 21:19:47 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
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
  private boolean isLastSlot;
  private int objectCnt;
  private TimelineView view;

  private PwDomain startTimeIntervalDomain;
  private PwDomain endTimeIntervalDomain;
  private PwPredicate predicate;
  private List timeIntervalLabels;

  /**
   * <code>SlotNode</code> - constructor 
   *
   * @param predicateName - <code>String</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param slotLocation - <code>Point</code> - 
   * @param previousToken - <code>PwToken</code> - 
   * @param isLastSlot - <code>boolean</code> - 
   * @param objectCnt - <code>int</code> - 
   * @param view - <code>TimelineView</code> - 
   */
  public SlotNode( String predicateName, PwSlot slot, Point slotLocation,
                   PwToken previousToken, boolean isLastSlot, int objectCnt,
                   TimelineView view) {
    super( predicateName);
    this.predicateName = predicateName;
    this.slot = slot;
    this.previousToken = previousToken;
    this.isLastSlot = isLastSlot;
    this.objectCnt = objectCnt;
    this.view = view;
    this.timeIntervalLabels = new ArrayList();
    // System.err.println( "SlotNode: predicateName " + predicateName);
    configure( slotLocation);
  } // end constructor


  private final void configure( Point slotLocation) {
    String backGroundColor = ((objectCnt % 2) == 0) ?
      ViewConstants.EVEN_OBJECT_SLOT_BG_COLOR :
      ViewConstants.ODD_OBJECT_SLOT_BG_COLOR;
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    setDraggable( false);
    // do not allow links
    getTopPort().setVisible( false);
    getLeftPort().setVisible( false);
    getBottomPort().setVisible( false);
    getRightPort().setVisible( false);
    setLocation( (int) slotLocation.getX(), (int) slotLocation.getY());
    setInsets( NODE_INSETS);

    renderTimeIntervals();

    // retrieveTokenNameAndParams();

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
   * <code>getTimeIntervalLabels</code>
   *
   * @return - <code>List</code> - 
   */
  public List getTimeIntervalLabels() {
    return timeIntervalLabels;
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

  // use startVariable for every token + the endVariable for the last one
  // if a slot is empty, and has no tokens, use the endVariable from
  // the previous slot
  private void renderTimeIntervals() {
    PwVariable intervalVariable = null, lastIntervalVariable = null;
    PwToken baseToken = null;
    PwDomain intervalDomain = null, lastIntervalDomain = null;
    if (slot.getTokenList().size() > 0) {
      baseToken = (PwToken) slot.getTokenList().get( 0);
    }
    boolean isLocationAbsolute = false;
    if (baseToken == null) {
      if (previousToken == null) {
        // first slot is empty
        if (isLastSlot == true) {
          // this is also the last slot
          intervalDomain = DbConstants.ZERO_DOMAIN;
          lastIntervalDomain = DbConstants.PLUS_INFINITY_DOMAIN;
        } else {
           intervalDomain = DbConstants.ZERO_DOMAIN;
        }
      } else {
        // empty slot between filled slots
        intervalVariable = previousToken.getEndVariable();
      }
    } else if (isLastSlot == true) {
      intervalVariable = baseToken.getStartVariable();
      lastIntervalVariable = baseToken.getEndVariable();
    } else {
      intervalVariable = baseToken.getStartVariable();      
    }

    if (intervalVariable == null) {
      startTimeIntervalDomain = intervalDomain;
    } else {
      startTimeIntervalDomain = intervalVariable.getDomain();
    }
    Point startLoc = new Point( (int) this.getLocation().getX() - this.getXOffset(),
                                (int) this.getLocation().getY() +
                                (int) this.getSize().getHeight());
    renderIntervalText( startTimeIntervalDomain.toString(), startLoc);

    if ((lastIntervalVariable != null) || (lastIntervalDomain != null)) {
      if (lastIntervalVariable == null) {
        endTimeIntervalDomain = lastIntervalDomain;
      } else {
        endTimeIntervalDomain = lastIntervalVariable.getDomain();
      }
      // System.err.println( "endTimeIntervalDomain " + endTimeIntervalDomain.toString());
      Point endLoc = new Point( (int) (this.getLocation().getX() +
                                       this.getSize().getWidth()),
                                (int) startLoc.getY());
      renderIntervalText( endTimeIntervalDomain.toString(), endLoc);
    }
  } // end renderTimeIntervals


  private JGoText renderIntervalText( String text, Point textLoc) {
    // make sure that time interval strings do not overlap
    int textLength = text.length() + ViewConstants.TIME_INTERVAL_STRINGS_OVERLAP_OFFSET;
    if (textLength > view.getSlotLabelMinLength()) {
      view.setSlotLabelMinLength( textLength);
    }
    // System.err.println( "renderIntervalText: text '" + text + "' minLen " +
    //                    view.getSlotLabelMinLength());

    // Object->JGoObject->JGoText
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
    timeIntervalLabels.add( textObject);
    view.getJGoDocument().addObjectAtTail( textObject);
    return textObject;
  } // end renderText


    // offset time interval labels, so they do not overlap previous & current slot nodes
  private int getXOffset() {
    if (previousToken == null) {
      return 0;
    } else {
      return SwingUtilities.computeStringWidth( view.getFontMetrics(),
                                                startTimeIntervalDomain.toString()) / 2;
    }
  } // end getXOffset


} // end class SlotNode

