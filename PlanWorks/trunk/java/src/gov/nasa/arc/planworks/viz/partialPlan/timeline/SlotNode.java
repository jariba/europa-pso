// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: SlotNode.java,v 1.1 2003-09-25 23:52:46 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.partialPlan.timeline;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;


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
  private SlotNode previousSlotNode;
  private boolean isFirstSlot;
  private boolean isLastSlot;
  private TimelineView timelineView;

  private PwDomain startTimeIntervalDomain;
  private PwDomain endTimeIntervalDomain;
  private PwPredicate predicate;
  private JGoText startTimeIntervalObject;
  private JGoText endTimeIntervalObject;
  private boolean isTimeLabelYLocLevel1;


  /**
   * <code>SlotNode</code> - constructor 
   *
   * @param nodeLabel - <code>String</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param slotLocation - <code>Point</code> - 
   * @param previousSlotNode - <code>SlotNode</code> - 
   * @param isFirstSlot - <code>boolean</code> - 
   * @param isLastSlot - <code>boolean</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param timelineView - <code>TimelineView</code> - 
   */
  public SlotNode( String nodeLabel, PwSlot slot, Point slotLocation,
                   SlotNode previousSlotNode, boolean isFirstSlot, boolean isLastSlot,
                   Color backgroundColor, TimelineView timelineView) {
    super( nodeLabel);
    // node label now contains \nkey=nnn
    PwToken token = slot.getBaseToken();
    if (token == null) { // empty slot
      this.predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
    } else {
      this.predicateName = token.getPredicate().getName();
    }
    this.slot = slot; 
    this.previousSlotNode = previousSlotNode;
    this.isFirstSlot = isFirstSlot;
    this.isLastSlot = isLastSlot;
    this.timelineView = timelineView;
    this.startTimeIntervalObject = null;
    this.endTimeIntervalObject = null;
    // System.err.println( "SlotNode: predicateName " + predicateName);
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
    // JGoText.setDefaultFontFaceName( ViewConstants.TIMELINE_VIEW_FONT_NAME);
    // JGoText.setDefaultFontSize( ViewConstants.TIMELINE_VIEW_FONT_SIZE);

    // getLabel().setFaceName( ViewConstants.TIMELINE_VIEW_FONT_NAME);
    // getLabel().setFontSize( ViewConstants.TIMELINE_VIEW_FONT_SIZE);

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
    boolean alwaysReturnEnd = true;
    PwSlot previousSlot = null;
    if (previousSlotNode != null) {
      previousSlot = previousSlotNode.getSlot();
    }
    PwDomain[] intervalArray =
      NodeGenerics.getStartEndIntervals( timelineView, slot, previousSlot, isLastSlot,
                                         alwaysReturnEnd);
    startTimeIntervalDomain = intervalArray[0];
    endTimeIntervalDomain = intervalArray[1];
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
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    SlotNode slotNode = (SlotNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
//       System.err.println( "doMouseClick obj class " +
//                           obj.getTopLevelObject().getClass().getName());
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
      mouseRightPopupMenu( viewCoords);
    }
    return false;
  } // end doMouseClick   

  private void mouseRightPopupMenu( Point viewCoords) {
    if (SlotNode.this.getSlot().getBaseToken() != null) {
      JPopupMenu mouseRightPopup = new JPopupMenu();
      JMenuItem activeTokenItem = new JMenuItem( "Set Active Token");
      activeTokenItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            ((PartialPlanViewSet) timelineView.getViewSet()).
              setActiveToken( SlotNode.this.getSlot().getBaseToken());
            List secondaryTokens = SlotNode.this.getSlot().getTokenList();
            secondaryTokens.remove( 0);
            if (secondaryTokens.size() == 0) {
              secondaryTokens = null;
            }
            ((PartialPlanViewSet) timelineView.getViewSet()).
              setSecondaryTokens( secondaryTokens);
            System.err.println( "SlotNode setActiveToken " +
                                SlotNode.this.getSlot().getBaseToken().getPredicate().getName());
          }
        });
      mouseRightPopup.add( activeTokenItem);

      NodeGenerics.showPopupMenu( mouseRightPopup, timelineView, viewCoords);
    }
  } // end mouseRightPopupMenu


} // end class SlotNode

