// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DbConstants.java,v 1.37 2005-11-10 01:22:07 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09June03
//

package gov.nasa.arc.planworks.db;


/**
 * interface <code>DbConstants</code> - constants for use by .viz.* packages
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface DbConstants {

  /**
   * constant <code>PLUS_INFINITY</code> - String
   *
   */
  public static final String PLUS_INFINITY = "Infinity";

  /**
   * constant <code>MINUS_INFINITY</code> - String
   *
   */
  public static final String MINUS_INFINITY = "-Infinity";

  /**
   * constant <code>PLUS_INFINITY_INT</code>
   *
   */
  public static final int PLUS_INFINITY_INT = Integer.MAX_VALUE;

  /**
   * constant <code>MINUS_INFINITY_INT</code>
   *
   */
  public static final int MINUS_INFINITY_INT = Integer.MIN_VALUE;

  public static final String PLUS_INFINITY_UNIC = "\u221E";
  
  public static final String MINUS_INFINITY_UNIC = "-\u221E";

  /**
   * constant <code>PP_PARTIAL_PLAN_EXT</code>
   *
   */
  public static final String PP_PARTIAL_PLAN_EXT = "partialPlan";

  /**
   * constant <code>PP_OBJECTS_EXT</code>
   *
   */
  public static final String PP_OBJECTS_EXT = "objects";

  /**
   * constant <code>PP_TOKENS_EXT</code>
   *
   */
  public static final String PP_TOKENS_EXT = "tokens";

  /**
   * constant <code>PP_VARIABLES_EXT</code>
   *
   */
  public static final String PP_VARIABLES_EXT = "variables";

  /**
   * constant <code>PP_CONSTRAINTS_EXT</code>
   *
   */
  public static final String PP_CONSTRAINTS_EXT = "constraints";

  /**
   * constant <code>PP_RULE_INSTANCE_EXT</code>
   *
   */
  public static final String PP_RULE_INSTANCE_EXT = "ruleInstances";

  /**
   * constant <code>PP_RULE_INSTANCE_SLAVE_MAP_EXT</code>
   *
   */
  public static final String PP_RULE_INSTANCE_SLAVE_MAP_EXT = "ruleInstanceSlaveMap";

  /**
   * constant <code>PP_CONSTRAINT_VAR_MAP_EXT</code>
   *
   */
  public static final String PP_CONSTRAINT_VAR_MAP_EXT = "constraintVarMap";

  public static final String PP_RESOURCE_INSTANTS_EXT = "instants";

	public static final String PP_DECISIONS_EXT = "decisions";
	
  public static final String SEQ_COL_SEP_HEX = "0x1e";

  public static final String SEQ_COL_SEP = "1E";
  // covert to string - (char) Integer.parseInt(DbConstants.SEQ_COL_SEP, 16)
 
  public static final String SEQ_LINE_SEP_HEX = "0x1f";

  public static final String SEQ_LINE_SEP = "1F";
  // covert to string - (char) Integer.parseInt(DbConstants.SEQ_LINE_SEP, 16)

  public static final String SEQ_PP_STATS = "partialPlanStats";

  public static final String SEQ_FILE = "sequence";
  
    //public static final String SEQ_TRANSACTIONS = "transactions";

  public static final String SEQ_RULES = "rules";

  public static final String SEQ_MODEL_PATH = "modelUrl";

  /**
   * constant <code>PARTIAL_PLAN_FILE_EXTS</code> - array of Strings
   *
   */
  public static final String [] PARTIAL_PLAN_FILE_EXTS =
    new String []
      { PP_PARTIAL_PLAN_EXT, PP_OBJECTS_EXT, PP_TOKENS_EXT, PP_VARIABLES_EXT, PP_CONSTRAINTS_EXT, 
        PP_RULE_INSTANCE_EXT, PP_RULE_INSTANCE_SLAVE_MAP_EXT, PP_CONSTRAINT_VAR_MAP_EXT, 
        PP_RESOURCE_INSTANTS_EXT, PP_DECISIONS_EXT};

  /**
   * constant <code>NUMBER_OF_PP_FILES</code>
   *
   */
  public static final int NUMBER_OF_PP_FILES = PARTIAL_PLAN_FILE_EXTS.length;


  public static final String [] SEQUENCE_FILES =  new String [] { SEQ_FILE, SEQ_PP_STATS,  
                                                                  /*SEQ_TRANSACTIONS,*/ SEQ_RULES};

  public static final int NUMBER_OF_SEQ_FILES = SEQUENCE_FILES.length;
    
  /**
   * constant <code>SOURCE_USER</code> - String - transaction source type
   *
   */
  public static final String SOURCE_USER = "USER";

  /**
   * constant <code>SOURCE_SYSTEM</code> - String - transaction source type
   *
   */
  public static final String SOURCE_SYSTEM = "SYSTEM";

  /**
   * constant <code>SOURCE_UNKNOWN</code> - String - transaction source type
   *
   */
  public static final String SOURCE_UNKNOWN = "UNKNOWN";

  public static final String ASSIGN_NEXT_DECISION_STARTED = "ASSIGN_NEXT_DECISION_STARTED";

  public static final String ASSIGN_NEXT_DECISION_SUCCEEDED = "ASSIGN_NEXT_DECISION_SUCCEEDED";


  /**
   * constant <code>OBJECT_CREATED</code> - String - transaction type
   *
   */
  public static final String OBJECT_CREATED = "OBJECT_CREATED";

  /**
   * constant <code>TOKEN_CREATED</code> - String - transaction type
   *
   */
  public static final String TOKEN_CREATED = "TOKEN_CREATED";

  /**
   * constant <code>TOKEN_DELETED</code> - String - transaction type
   *
   */
  public static final String TOKEN_DELETED = "TOKEN_DELETED";

  /**
   * constant <code>TOKEN_FREED</code> - String - transaction type
   *
   */
  public static final String TOKEN_FREED = "TOKEN_FREED";

  /**
   * constant <code>TOKEN_INSERTED</code> - String - transaction type
   *
   */
  public static final String TOKEN_INSERTED = "TOKEN_INSERTED";

  /**
   * constant <code>TOKEN_ADDED_TO_OBJECT</code> - String - transaction type
   *
   */
  public static final String TOKEN_ADDED_TO_OBJECT = "TOKEN_ADDED_TO_OBJECT";

  /**
   * constant <code>TOKEN_REMOVED</code> - String - transaction type
   *
   */
  public static final String TOKEN_REMOVED = "TOKEN_REMOVED";

  /**
   * constant <code>TOKEN_CLOSED</code> - String - transaction type
   *
   */
  public static final String TOKEN_CLOSED = "TOKEN_CLOSED";

  /**
   * constant <code>TOKEN_ALL_TYPES</code> - String - transaction type
   *
   */
  public static final String TOKEN_ALL_TYPES = "TOKEN_%";

  /**
   * constant <code>VARIABLE_CREATED</code> - String - transaction type
   *
   */
  public static final String VARIABLE_CREATED = "VARIABLE_CREATED";

  /**
   * constant <code>VARIABLE_DELETED</code> - String - transaction type
   *
   */
  public static final String VARIABLE_DELETED = "VARIABLE_DELETED";

  /**
   * constant <code>VARIABLE_DOMAIN_EMPTIED</code> - String - transaction type
   *
   */
  public static final String VARIABLE_DOMAIN_EMPTIED = "VARIABLE_DOMAIN_EMPTIED";

  /**
   * constant <code>VARIABLE_DOMAIN_RELAXED</code> - String - transaction type
   *
   */
  public static final String VARIABLE_DOMAIN_RELAXED = "VARIABLE_DOMAIN_RELAXED";

  /**
   * constant <code>VARIABLE_DOMAIN_RESET</code> - String - transaction type
   *
   */
  public static final String VARIABLE_DOMAIN_RESET = "VARIABLE_DOMAIN_RESET";

  /**
   * constant <code>VARIABLE_DOMAIN_RESTRICTED</code> - String - transaction type
   *
   */
  public static final String VARIABLE_DOMAIN_RESTRICTED = "VARIABLE_DOMAIN_RESTRICTED";

  /**
   * constant <code>VARIABLE_DOMAIN_SPECIFIED</code> - String - transaction type
   *
   */
  public static final String VARIABLE_DOMAIN_SPECIFIED = "VARIABLE_DOMAIN_SPECIFIED";
                                                          
  /**
   * constant <code>VARIABLE_DOMAIN_CLOSED</code> - String - transaction type
   *
   */
  public static final String VARIABLE_DOMAIN_CLOSED = "VARIABLE_DOMAIN_CLOSED";
      
  public static final String VARIABLE_DOMAIN_RESTRICT_TO_SINGLETON =
    "VARIABLE_DOMAIN_RESTRICT_TO_SINGLETON";
  public static final String VARIABLE_DOMAIN_LOWER_BOUND_INCREASED =
    "VARIABLE_DOMAIN_LOWER_BOUND_INCREASED";
  public static final String VARIABLE_DOMAIN_LOWER_BOUND_DECREASED =
    "VARIABLE_DOMAIN_LOWER_BOUND_DECREASED";
  public static final String VARIABLE_DOMAIN_UPPER_BOUND_INCREASED =
    "VARIABLE_DOMAIN_UPPER_BOUND_INCREASED";
  public static final String VARIABLE_DOMAIN_UPPER_BOUND_DECREASED =
    "VARIABLE_DOMAIN_UPPER_BOUND_DECREASED";
  public static final String VARIABLE_DOMAIN_BOUNDS_RESTRICTED =
    "VARIABLE_DOMAIN_BOUNDS_RESTRICTED";
  public static final String VARIABLE_DOMAIN_VALUE_REMOVED =
    "VARIABLE_DOMAIN_VALUE_REMOVED";
  public static final String VARIABLE_DOMAIN_SET_TO_SINGLETON =
    "VARIABLE_DOMAIN_SET_TO_SINGLETON";
   public static final String VARIABLE_DOMAIN_SET = "VARIABLE_DOMAIN_SET";
                          
  /**
   * constant <code>VARIABLE_ALL_TYPES</code> - String - transaction type
   *
   */
  public static final String VARIABLE_ALL_TYPES = "VARIABLE_%";

  /**
   * constant <code>CONSTRAINT_CREATED</code> - String - transaction type
   *
   */
  public static final String CONSTRAINT_CREATED = "CONSTRAINT_CREATED";

  /**
   * constant <code>CONSTRAINT_DELETED</code> - String - transaction type
   *
   */
  public static final String CONSTRAINT_DELETED = "CONSTRAINT_DELETED";

  /**
   * constant <code>CONSTRAINT_EXECUTED</code> - String - transaction type
   *
   */
  public static final String CONSTRAINT_EXECUTED = "CONSTRAINT_EXECUTED";

  /**
   * constant <code>CONSTRAINT_ALL_TYPES</code> - String - transaction type
   *
   */
  public static final String CONSTRAINT_ALL_TYPES = "CONSTRAINT_%";

  /**
   * constant <code>PROPOGATION_BEGUN</code> - String - transaction type
   *
   */
  public static final String PROPOGATION_BEGUN = "PROPOGATION_BEGUN";

  /**
   * constant <code>PROPOGATION_ENDED</code> - String - transaction type
   *
   */
  public static final String PROPOGATION_ENDED = "PROPOGATION_ENDED";

  public static final String RULE_EXECUTED = "RULE_EXECUTED";


  public static final String ATEMPORAL_CONSTRAINT_TYPE = "ATEMPORAL";

  public static final String TEMPORAL_CONSTRAINT_TYPE = "TEMPORAL";

  public static final String TOKEN_RELATION_TYPE = "CAUSAL";

  public static final String INTEGER_INTERVAL_DOMAIN_TYPE = "INTEGER_SORT";

  public static final String REAL_INTERVAL_DOMAIN_TYPE = "REAL_SORT";

  public static final String ENUMERATED_DOMAIN = "EnumeratedDomain";

  public static final String INTERVAL_DOMAIN = "IntervalDomain";

  // DB Transaction Types
  public static final String TT_CREATION = "CREATION";
  public static final String TT_DELETION = "DELETION";
  public static final String TT_ADDITION = "ADDITION";
  public static final String TT_REMOVAL = "REMOVAL";
  public static final String TT_CLOSURE = "CLOSURE";
  public static final String TT_RESTRICTION = "RESTRICTION";
  public static final String TT_RELAXATION = "RELAXATION";
  public static final String TT_EXECUTION = "EXECUTION";
  public static final String TT_SPECIFICATION = "SPECIFICATION";
  public static final String TT_UNDO = "UNDO";
  public static final String TT_ASSIGNMENT = "ASSIGNMENT";

  public static final String [] TT_CREATION_NAMES = new String [] {
    TOKEN_CREATED, VARIABLE_CREATED, CONSTRAINT_CREATED, OBJECT_CREATED };
  public static final String [] TT_DELETION_NAMES = new String [] {
    TOKEN_DELETED, VARIABLE_DELETED, CONSTRAINT_DELETED };
  public static final String [] TT_ADDITION_NAMES = new String [] {
    TOKEN_ADDED_TO_OBJECT};
  public static final String [] TT_REMOVAL_NAMES = new String [] {
    TOKEN_REMOVED};
  public static final String [] TT_CLOSURE_NAMES = new String [] {
    VARIABLE_DOMAIN_CLOSED, TOKEN_CLOSED};
  public static final String [] TT_RESTRICTION_NAMES = new String [] {
    VARIABLE_DOMAIN_RESTRICT_TO_SINGLETON, VARIABLE_DOMAIN_LOWER_BOUND_INCREASED,
    VARIABLE_DOMAIN_LOWER_BOUND_DECREASED, VARIABLE_DOMAIN_UPPER_BOUND_INCREASED,
    VARIABLE_DOMAIN_UPPER_BOUND_DECREASED, VARIABLE_DOMAIN_BOUNDS_RESTRICTED,
    VARIABLE_DOMAIN_VALUE_REMOVED };
  public static final String [] TT_RELAXATION_NAMES = new String [] {
    };
  public static final String [] TT_EXECUTION_NAMES = new String [] {
    RULE_EXECUTED, CONSTRAINT_EXECUTED };
  public static final String [] TT_SPECIFICATION_NAMES = new String [] {
    VARIABLE_DOMAIN_SET_TO_SINGLETON, VARIABLE_DOMAIN_SET};
  public static final String [] TT_UNDO_NAMES = new String [] {
    };
  public static final String [] TT_ASSIGNMENT_NAMES = new String [] {
    ASSIGN_NEXT_DECISION_STARTED, ASSIGN_NEXT_DECISION_SUCCEEDED};
  
  public static final String TBL_CONSTRAINT = "VConstraint";
  public static final String TBL_CONSTVARMAP = "ConstraintVarMap";
  public static final String TBL_OBJECT = "Object";
  public static final String TBL_PARTIALPLAN = "PartialPlan";
  public static final String TBL_PROJECT = "Project";
  public static final String TBL_SEQUENCE = "Sequence";
  public static final String TBL_TOKEN = "Token";
  public static final String TBL_RULE_INSTANCE = "RuleInstance";
  public static final String TBL_RULE_INSTANCE_SLAVE_MAP = "RuleInstanceSlaveMap";
  public static final String TBL_VARIABLE = "Variable";
    //public static final String TBL_TRANSACTION = "Transaction";
  public static final String TBL_INSTANTS = "ResourceInstants";
  public static final String TBL_RULES = "Rules";
  public static final String TBL_PP_STATS = "PartialPlanStats";
  public static final String TBL_DECISION = "Decision";

  public static final String [] PW_DB_TABLES = new String [] {
    TBL_PARTIALPLAN, TBL_OBJECT, TBL_TOKEN, TBL_VARIABLE, TBL_CONSTRAINT, TBL_RULE_INSTANCE, 
    TBL_RULE_INSTANCE_SLAVE_MAP,
    TBL_CONSTVARMAP, TBL_INSTANTS, TBL_DECISION, TBL_PROJECT, TBL_SEQUENCE, TBL_PP_STATS, 
    /*TBL_TRANSACTION,*/ TBL_RULES};

  // number of numeric characters in a type long value
  public static final int LONG_INT_LENGTH = 13;

  public static final String RULE_MEETS = "meets";
  public static final String RULE_MET_BY = "met-by";
  public static final String RULE_CONTAINS = "contains";
  public static final String RULE_CONTAINED_BY = "contained-by";

  public static final String START_VAR = "START_VAR";
  public static final String END_VAR = "END_VAR";
  public static final String DURATION_VAR = "DURATION_VAR";
  public static final String OBJECT_VAR = "OBJECT_VAR";
  public static final String PARAMETER_VAR = "PARAMETER_VAR";
  public static final String STATE_VAR = "STATE_VAR";
  public static final String GLOBAL_VAR = "GLOBAL_VAR";
  public static final String MEMBER_VAR = "MEMBER_VAR";
  public static final String RULE_VAR = "RULE_VAR";

  public static final String [] DB_VARIABLE_TYPES = new String [] {
    START_VAR, END_VAR, DURATION_VAR, OBJECT_VAR, PARAMETER_VAR, STATE_VAR, GLOBAL_VAR,
    MEMBER_VAR, RULE_VAR};

  //object types
  public static final int O_OBJECT = 0;
  public static final int O_TIMELINE = 1;
  public static final int O_RESOURCE = 2;

  public static final int T_INTERVAL = 0;
  public static final int T_TRANSACTION = 1;

  public static final int D_OBJECT = 0;
  public static final int D_TOKEN = 1;
  public static final int D_VARIABLE = 2;
  public static final int C_OBJECT = 0;
  public static final int C_VALUE = 1;
  public static final int C_DOMAIN = 2;
  public static final int C_CLOSE = 3;

  public static final Integer NO_ID = new Integer( -1);

} // end interface DbConstants

    
