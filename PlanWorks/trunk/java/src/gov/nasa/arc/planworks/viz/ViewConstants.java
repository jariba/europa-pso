// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz;

import java.awt.Font;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoText;



public interface ViewConstants {

  // views.timeline.TimelineView

  /**
   * constant <code>TIMELINE_VIEW_X_INIT</code>
   *
   */
  public static final int TIMELINE_VIEW_X_INIT = 10;

  /**
   * constant <code>TIMELINE_VIEW_Y_INIT</code>
   *
   */
  public static final int TIMELINE_VIEW_Y_INIT = 10;

  /**
   * constant <code>TIMELINE_VIEW_Y_DELTA</code>
   *
   */
  public static final int TIMELINE_VIEW_Y_DELTA = 50;

  /**
   * constant <code>TIMELINE_VIEW_FONT_SIZE</code>
   *
   */
  public static final int TIMELINE_VIEW_FONT_SIZE = 12;

  /**
   * constant <code>TIMELINE_VIEW_FONT_NAME</code>
   *
   */
  public static final String TIMELINE_VIEW_FONT_NAME = "Serif";

  /**
   * constant <code>TIMELINE_VIEW_FONT_STYLE</code>
   *
   */
  public static final int TIMELINE_VIEW_FONT_STYLE = Font.PLAIN;

  /**
   * constant <code>TIMELINE_VIEW_EMPTY_NODE_LABEL</code>
   *
   */
  public static final String TIMELINE_VIEW_EMPTY_NODE_LABEL = "<empty>";

  /**
   * constant <code>TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN</code>
   *
   */
  public static final int TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN = 7;

  // nodes.TimelineNode
  // nodes.SlotNode

  /**
   * constant <code>TIMELINE_VIEW_INSET_SIZE</code>
   *
   */
  public static final int TIMELINE_VIEW_INSET_SIZE = 10;

  /**
   * constant <code>TIMELINE_VIEW_INSET_SIZE_HALF</code>
   *
   */
  public static final int TIMELINE_VIEW_INSET_SIZE_HALF = TIMELINE_VIEW_INSET_SIZE / 2;

  /**
   * constant <code>TIMELINE_VIEW_IS_FONT_BOLD</code>
   *
   */
  public static final boolean TIMELINE_VIEW_IS_FONT_BOLD = false;

  /**
   * constant <code>TIMELINE_VIEW_IS_FONT_UNDERLINED</code>
   *
   */
  public static final boolean TIMELINE_VIEW_IS_FONT_UNDERLINED = false;

  /**
   * constant <code>TIMELINE_VIEW_IS_FONT_ITALIC</code>
   *
   */
  public static final boolean TIMELINE_VIEW_IS_FONT_ITALIC = false;

  /**
   * constant <code>TIMELINE_VIEW_TEXT_ALIGNMENT</code>
   *
   */
  public static final int TIMELINE_VIEW_TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;

  /**
   * constant <code>TIMELINE_VIEW_IS_TEXT_MULTILINE</code>
   *
   */
  public static final boolean TIMELINE_VIEW_IS_TEXT_MULTILINE = false;

  /**
   * constant <code>TIMELINE_VIEW_IS_TEXT_EDITABLE</code>
   *
   */
  public static final boolean TIMELINE_VIEW_IS_TEXT_EDITABLE = false;


} // end ViewConstants
