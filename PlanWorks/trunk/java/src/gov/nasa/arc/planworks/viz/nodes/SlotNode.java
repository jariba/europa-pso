// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGo3DRect;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.VizView;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;


/**
 * <code>SlotNode</code> - 
*             Object->JGoObject->JGoArea->TextNode->SlotNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SlotNode extends TextNode implements ViewConstants {

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
  private Point slotLocation;
  private PwToken previousToken;
  private boolean isLastSlot;
  private FontMetrics fontMetrics;
  private TimelineView view;

  private PwDomain startTimeIntervalDomain;
  private PwDomain endTimeIntervalDomain;
  private PwPredicate predicate;

  /**
   * <code>SlotNode</code> - constructor 
   *
   * @param predicateName - <code>String</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param view - <code>VizView</code> - 
   */
  public SlotNode( String predicateName, PwSlot slot, Point slotLocation,
                   PwToken previousToken, boolean isLastSlot, TimelineView view) {
    super( predicateName);
    this.predicateName = predicateName;
    this.slot = slot;
    this.slotLocation = slotLocation;
    this.previousToken = previousToken;
    this.isLastSlot = isLastSlot;
    this.view = view;
    // System.err.println( "SlotNode: predicateName " + predicateName);
    configure();
  } // end constructor


  private final void configure() {
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( "gray60")));  
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
   * <code>getSlotLocation</code>
   *
   * @return - <code>Point</code> - 
   */
  public Point getSlotLocation() {
    return slotLocation;
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
    PwToken baseToken = (PwToken) slot.getTokenList().get( 0);
    boolean isLocationAbsolute = false;
    if (baseToken == null) {
      intervalVariable = previousToken.getEndVariable();
    } else if (isLastSlot == true) {
      intervalVariable = baseToken.getStartVariable();
      lastIntervalVariable = baseToken.getEndVariable();
    } else {
      intervalVariable = baseToken.getStartVariable();      
    }

    startTimeIntervalDomain = intervalVariable.getDomain();
    // System.err.println( "startTimeIntervalDomain " + startTimeIntervalDomain.toString());
    Point startLoc = new Point( (int) this.getLocation().getX() - this.getXOffset(),
                                (int) this.getLocation().getY() +
                                (int) this.getSize().getHeight());
    renderIntervalText( startTimeIntervalDomain.toString(), startLoc);

    if (lastIntervalVariable != null) {
      endTimeIntervalDomain = lastIntervalVariable.getDomain();
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
    textObject.setEditOnSingleClick( false);
    textObject.setBkColor( ColorMap.getColor( "lightGray"));
    view.getJGoView().getDocument().addObjectAtTail( textObject);
    return textObject;
  } // end renderText


    // offset time interval labels, so they overlap previous & current slot nodes
  private int getXOffset() {
    if (previousToken == null) {
      return 0;
    } else {
      return SwingUtilities.computeStringWidth( view.getFontMetrics(),
                                                startTimeIntervalDomain.toString()) / 2;
    }
  } // end getXOffset


} // end class SlotNode

