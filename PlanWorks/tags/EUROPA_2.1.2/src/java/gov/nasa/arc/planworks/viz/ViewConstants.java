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

import java.awt.Color;
import java.awt.SystemColor;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;


// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoText;

import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;


/**
 * Describe interface <code>ViewConstants</code> here.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *        NASA Ames Research Center - Code IC
 * @version 0.0
 */
public abstract class ViewConstants {

  /**
   * constant <code>VIEW_BACKGROUND_COLOR - java.awt.Color</code> 
   *
   */
  public static final Color VIEW_BACKGROUND_COLOR = ColorMap.getColor( "System.background");

  /**
   * constant <code>FREE_TOKEN_BG_COLOR</code>
   *
   */
  public static final Color FREE_TOKEN_BG_COLOR = ColorMap.getColor( "lightGray");

  /**
   * constant <code>RULE_INSTANCE_BG_COLOR</code>
   *
   */
  public static final Color RULE_INSTANCE_BG_COLOR = ColorMap.getColor( "gray60");

  /**
   * constant <code>PRIMARY_SELECTION_COLOR</code> -
   *                   same as jGoView.getDefaultPrimarySelectionColor()
   */
  public static final Color PRIMARY_SELECTION_COLOR = ColorMap.getColor( "green");

  public static final int INTERNAL_FRAME_X_DELTA = 100;

  public static final int INTERNAL_FRAME_X_DELTA_DIV_4 = 25;

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
   * constant <code>TIMELINE_VIEW_X_DELTA</code>
   *
   */
  public static final int TIMELINE_VIEW_X_DELTA = 50;

  /**
   * constant <code>TIMELINE_VIEW_Y_DELTA</code>
   *
   */
  public static final int TIMELINE_VIEW_Y_DELTA = 80; // 60;

  /**
   * constant <code>VIEW_FONT_SIZE</code>
   *
   */
  public static final int VIEW_FONT_SIZE = 12;

  /**
   * constant <code>VIEW_FONT_NAME</code> - fixed width font
   *
   */
  public static final String VIEW_FONT_NAME = "Monospaced";

  /**
   * constant <code>VIEW_FONT_PLAIN_STYLE</code>
   *
   */
  public static final int VIEW_FONT_PLAIN_STYLE = Font.PLAIN;

  /**
   * constant <code>VIEW_FONT_BOLD_STYLE</code>
   *
   */
  public static final int VIEW_FONT_BOLD_STYLE = Font.BOLD;

  /**
   * constant <code>TIME_INTERVAL_STRINGS_OVERLAP_OFFSET</code>
   *
   */
  public static final int TIME_INTERVAL_STRINGS_OVERLAP_OFFSET = 4;

  /**
   * constant <code>TIMELINE_VIEW_EMPTY_NODE_LABEL</code>
   *
   */
  public static final String TIMELINE_VIEW_EMPTY_NODE_LABEL = "-empty-";

  /**
   * constant <code>TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN</code>
   *
   */
  public static final int TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN = 7;

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
   * constant <code>EMPTY_SLOT_WIDTH</code>
   *
   */
  public static final int EMPTY_SLOT_WIDTH = 10;

  /**
   * constant <code>TIMELINE_EXTEND_HORIZON_UNITS</code>
   *
   */
  public static final int TIMELINE_EXTEND_HORIZON_UNITS = 2;

  // Temporal Extent View

  /**
   * constant <code>TEMPORAL_NODE_X_DELTA</code>
   *
   */
  public static final int TEMPORAL_NODE_X_DELTA = 5;

  /**
   * constant <code>TEMPORAL_NODE_Y_DELTA</code>
   *
   */
  public static final int TEMPORAL_NODE_Y_DELTA = 10;

  /**
   * constant <code>TEMPORAL_NODE_CELL_HEIGHT</code>
   *
   */
  public static final int TEMPORAL_NODE_CELL_HEIGHT = 68; // 46;

  /**
   * constant <code>TEMPORAL_NODE_Y_LABEL_OFFSET</code>
   *
   */
  public static final int TEMPORAL_NODE_Y_LABEL_OFFSET = 10;

  /**
   * constant <code>TEMPORAL_NODE_Y_START_OFFSET</code>
   *
   */
  public static final int TEMPORAL_NODE_Y_START_OFFSET = 30; // 22;

  /**
   * constant <code>TEMPORAL_NODE_Y_END_OFFSET</code>
   *
   */
  public static final int TEMPORAL_NODE_Y_END_OFFSET = 42; // 34;

  /**
   * constant <code>TEMPORAL_MIN_END_X_LOC</code>
   *
   */
  public static final int TEMPORAL_MIN_END_X_LOC = 250;

  /**
   * constant <code>TEMPORAL_MIN_MAX_SLOTS</code>
   *
   */
  public static final int TEMPORAL_MIN_MAX_SLOTS = 8;

  /**
   * constant <code>TEMPORAL_TICK_DELTA_X_MIN</code>
   *
   */
  public static final int TEMPORAL_TICK_DELTA_X_MIN = 30;

  /**
   * constant <code>JGO_DOC_BORDER_WIDTH</code>
   *
   */
  public static final int JGO_DOC_BORDER_WIDTH = 5;

  /**
   * constant <code>JGO_SCROLL_BAR_WIDTH</code>
   *
   */
  public static final int JGO_SCROLL_BAR_WIDTH = 17;


  // cannot figure out how to get this from MDIInternalFrame
  /**
   * constant <code>MDI_FRAME_DECORATION_HEIGHT</code>
   *
   */
  public static final int MDI_FRAME_DECORATION_HEIGHT = 70;

  // cannot figure out how to get this from MDIInternalFrame
  /**
   * constant <code>MDI_FRAME_DECORATION_WIDTH</code>
   *
   */
  public static final int MDI_FRAME_DECORATION_WIDTH = 35;

  /**
   * constant <code>FRAME_DECORATION_HEIGHT</code>
   *
   */
  public static final int FRAME_DECORATION_HEIGHT = 75;

  /**
   * constant <code>FRAME_DECORATION_WIDTH</code>
   *
   */
  public static final int FRAME_DECORATION_WIDTH = 8;

  public static final int STEP_VIEW_X_INIT = 10;

  public static final int STEP_VIEW_Y_INIT = 10;

  public static final int STEP_VIEW_STEP_WIDTH = 10;

  public static final int STEP_VIEW_Y_MAX = 100;

    //public static final String DB_TRANSACTION_KEY_HEADER         = "TX_KEY   ";
  // ViewGenerics.computeTransactionNameHeader pads this appropriately with blanks 
  public static final String DB_TRANSACTION_NAME_HEADER        = "TRANSACTION_NAME"; 
  public static final String DB_TRANSACTION_SOURCE_HEADER      = "  SOURCE  ";   
  public static final String DB_TRANSACTION_ENTITY_KEY_HEADER  = "ENTITY_KEY";
  public static final String DB_TRANSACTION_STEP_NUM_HEADER    = " STEP   ";
  public static final String DB_TRANSACTION_ENTITY_NAME_HEADER = "  ENTITY_NAME     ";
  public static final String DB_TRANSACTION_PARENT_HEADER      = "   PARENT_NAME    ";
  public static final String DB_TRANSACTION_PARAMETER_HEADER   = "   PARAMETER_NAME    ";

  public static final String QUERY_CONSTRAINT_KEY_HEADER       = "CSTR_KEY"; 
  public static final String QUERY_CONSTRAINT_TYPE_HEADER      = "CONSTRAINT_TYPE"; 

  public static final String QUERY_TOKEN_KEY_HEADER            = "TOK_KEY"; 
  public static final String QUERY_TOKEN_PREDICATE_HEADER      = "   PREDICATE_NAME   "; 

  public static final String QUERY_VARIABLE_KEY_HEADER         = "VAR_KEY"; 
  public static final String QUERY_VARIABLE_TYPE_HEADER        = "   VARIABLE_TYPE  "; 

  // Extended BasicNode Shapes for viz/nodes/ExtendedBasicNode

  public static final int RECTANGLE = 0;         // PwToken
  public static final int ELLIPSE = 1;           // PwRule
  public static final int DIAMOND = 2;           // PwConstraint
  public static final int LEFT_TRAPEZOID = 3;    // PwObject
  public static final int RIGHT_TRAPEZOID = 4;   // PwTimeline
  public static final int HEXAGON = 5;           // PwSlot
  public static final int PINCHED_RECTANGLE = 6; // PwVariable
  public static final int PINCHED_HEXAGON = 7;   // PwResource

  /**
   * constant <code>RESOURCE_PROFILE_CELL_HEIGHT</code>
   *
   */
  public static final int RESOURCE_PROFILE_CELL_HEIGHT = 120; 

  /**
   * constant <code>RESOURCE_PROFILE_MIN_Y_OFFSET</code>
   *
   */
  public static final int RESOURCE_PROFILE_MIN_Y_OFFSET = 18;

  /**
   * constant <code>RESOURCE_PROFILE_MAX_Y_OFFSET</code>
   *
   */
  public static final int RESOURCE_PROFILE_MAX_Y_OFFSET = 18;

  /**
   * constant <code>RESOURCE_LEVEL_SCALE_WIDTH_OFFSET</code>
   *
   */
  public static final int RESOURCE_LEVEL_SCALE_WIDTH_OFFSET = 5;

  /**
   * constant <code>RESOURCE_TRANSACTION_HEIGHT</code>
   *
   */
  public static final int RESOURCE_TRANSACTION_HEIGHT = 10;


  public static final String CONSTRAINT_NETWORK_VIEW   = "Constraint Network View";
  public static final String TEMPORAL_EXTENT_VIEW      = "Temporal Extent View";
  public static final String TEMPORAL_NETWORK_VIEW     = "Temporal Network View";
  public static final String TIMELINE_VIEW             = "Timeline View";
  public static final String TOKEN_NETWORK_VIEW        = "Token Network View";
  //public static final String DB_TRANSACTION_VIEW       = "DB Transaction View";
  public static final String NAVIGATOR_VIEW            = "Navigator View";
  public static final String RESOURCE_PROFILE_VIEW     = "Resource Profile View";
  public static final String RESOURCE_TRANSACTION_VIEW = "Resource Transaction View";
  public static final String DECISION_VIEW             = "Decision View";
  public static final String RULE_INSTANCE_VIEW        = "Rule Instance View";

  public static final String SEQUENCE_STEPS_VIEW     = "Sequence Steps View";
  public static List SEQUENCE_VIEW_LIST = null;

  public static final String VIEW_TITLE = "View for ";
  public static final String OVERVIEW_TITLE = "Overview for ";

  public static final String CONTENT_SPEC_TITLE = "ContentFilter";
  // the next two must be unique as far as String/indexOf()
  //public static final String SEQUENCE_QUERY_TITLE = "SequenceQuery";
  //public static final String SEQUENCE_QUERY_RESULTS_TITLE = "QueryResults";

  public static final String PLANNER_CONTROLLER_TITLE = "PlannerController";

    public static final String DEBUG_CONSOLE_TITLE = "Debug Console";

  static {

    SEQUENCE_VIEW_LIST = new ArrayList();
    SEQUENCE_VIEW_LIST.add( SEQUENCE_STEPS_VIEW);

  }

  public static final int DECISION_TREE_VIEW_WIDTH = 400;
  public static final int DECISION_TREE_VIEW_HEIGHT = 300;

  public static final String NODE_SHAPES_FRAME = "PlanWorks Node Shapes";
  public static final int NODE_SHAPES_FRAME_WIDTH = 550;
  public static final int NODE_SHAPES_FRAME_HEIGHT = 325;

  public static final int WAIT_NUM_CYCLES = 10; 
  public static final int WAIT_INTERVAL = 50; //in milliseconds

  public static final int MONITOR_MIN_MAX_SCALING = 100;

  public static List CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES = null;
  public static List NAVIGATOR_VIEW_ENTITY_CLASSES = null;
  public static List TOKEN_NETWORK_VIEW_ENTITY_CLASSES = null;

  static {
    CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES = new ArrayList();
    CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES.add( PwConstraint.class);
    CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES.add( PwObject.class);
    CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES.add( PwResource.class);
    CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES.add( PwResourceTransaction.class);
    CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES.add( PwRuleInstance.class);
    CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES.add( PwTimeline.class);
    CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES.add( PwToken.class);
    CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES.add( PwVariable.class);

    NAVIGATOR_VIEW_ENTITY_CLASSES = new ArrayList();
    NAVIGATOR_VIEW_ENTITY_CLASSES.add( PwConstraint.class);
    NAVIGATOR_VIEW_ENTITY_CLASSES.add( PwObject.class);
    NAVIGATOR_VIEW_ENTITY_CLASSES.add( PwResource.class);
    NAVIGATOR_VIEW_ENTITY_CLASSES.add( PwResourceTransaction.class);
    NAVIGATOR_VIEW_ENTITY_CLASSES.add( PwRuleInstance.class);
    NAVIGATOR_VIEW_ENTITY_CLASSES.add( PwSlot.class);
    NAVIGATOR_VIEW_ENTITY_CLASSES.add( PwTimeline.class);
    NAVIGATOR_VIEW_ENTITY_CLASSES.add( PwToken.class);
    NAVIGATOR_VIEW_ENTITY_CLASSES.add( PwVariable.class);
    
    TOKEN_NETWORK_VIEW_ENTITY_CLASSES = new ArrayList();
    TOKEN_NETWORK_VIEW_ENTITY_CLASSES.add( PwRuleInstance.class);
    TOKEN_NETWORK_VIEW_ENTITY_CLASSES.add( PwToken.class);
  }

  public static final String OBJECT_TO_OBJECT_LINK_TYPE = "OtoO";
  public static final String OBJECT_TO_RESOURCE_LINK_TYPE = "OtoR";
  public static final String OBJECT_TO_TIMELINE_LINK_TYPE = "OtoTi";
  public static final String OBJECT_TO_TOKEN_LINK_TYPE = "OtoT";
  public static final String OBJECT_TO_VARIABLE_LINK_TYPE = "OtoV";
  public static final String RESOURCE_TO_TOKEN_LINK_TYPE = "RtoT";
  public static final String RESOURCE_TO_VARIABLE_LINK_TYPE = "RtoV";
  public static final String RULE_INST_TO_TOKEN_LINK_TYPE = "RutoT";
  public static final String RULE_INST_TO_VARIABLE_LINK_TYPE = "RutoV";
  public static final String SLOT_TO_TOKEN_LINK_TYPE = "StoT";
  public static final String TIMELINE_TO_OBJECT_LINK_TYPE = "TitoO";
  public static final String TIMELINE_TO_RESOURCE_LINK_TYPE = "TitoR";
  public static final String TIMELINE_TO_SLOT_LINK_TYPE = "TitoS";
  public static final String TIMELINE_TO_TIMELINE_LINK_TYPE = "TitoTi";
  public static final String TIMELINE_TO_VARIABLE_LINK_TYPE = "TitoV";
  public static final String TOKEN_TO_RULE_INST_LINK_TYPE = "TtoRu";
  public static final String TOKEN_TO_TOKEN_LINK_TYPE = "TtoT";
  public static final String TOKEN_TO_VARIABLE_LINK_TYPE = "TtoV";
  public static final String VARIABLE_TO_CONSTRAINT_LINK_TYPE = "VtoC";
 
  public static List ALL_LINK_TYPES;

  static {
    ALL_LINK_TYPES = new ArrayList(19);
    ALL_LINK_TYPES.add(OBJECT_TO_OBJECT_LINK_TYPE);
    ALL_LINK_TYPES.add(OBJECT_TO_RESOURCE_LINK_TYPE);
    ALL_LINK_TYPES.add(OBJECT_TO_TOKEN_LINK_TYPE);
    ALL_LINK_TYPES.add(OBJECT_TO_TIMELINE_LINK_TYPE);
    ALL_LINK_TYPES.add(OBJECT_TO_VARIABLE_LINK_TYPE);
    ALL_LINK_TYPES.add(RESOURCE_TO_TOKEN_LINK_TYPE);
    ALL_LINK_TYPES.add(RESOURCE_TO_VARIABLE_LINK_TYPE);
    ALL_LINK_TYPES.add(RULE_INST_TO_TOKEN_LINK_TYPE);
    ALL_LINK_TYPES.add(RULE_INST_TO_VARIABLE_LINK_TYPE);
    ALL_LINK_TYPES.add(SLOT_TO_TOKEN_LINK_TYPE);
    ALL_LINK_TYPES.add(TIMELINE_TO_OBJECT_LINK_TYPE);
    ALL_LINK_TYPES.add(TIMELINE_TO_RESOURCE_LINK_TYPE);
    ALL_LINK_TYPES.add(TIMELINE_TO_SLOT_LINK_TYPE);
    ALL_LINK_TYPES.add(TIMELINE_TO_TIMELINE_LINK_TYPE);
    ALL_LINK_TYPES.add(TIMELINE_TO_VARIABLE_LINK_TYPE);
    ALL_LINK_TYPES.add(TOKEN_TO_RULE_INST_LINK_TYPE);
    ALL_LINK_TYPES.add(TOKEN_TO_TOKEN_LINK_TYPE);
    ALL_LINK_TYPES.add(TOKEN_TO_VARIABLE_LINK_TYPE);
    ALL_LINK_TYPES.add(VARIABLE_TO_CONSTRAINT_LINK_TYPE);
  }
} // end ViewConstants
