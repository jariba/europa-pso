// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DbConstants.java,v 1.14 2003-11-06 21:52:20 miatauro Exp $
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
   * constant <code>PP_TIMELINES_EXT</code>
   *
   */
  public static final String PP_TIMELINES_EXT = "timelines";

  /**
   * constant <code>PP_SLOTS_EXT</code>
   *
   */
  public static final String PP_SLOTS_EXT = "slots";

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
   * constant <code>PP_PREDICATES_EXT</code>
   *
   */
  public static final String PP_PREDICATES_EXT = "predicates";

  /**
   * constant <code>PP_PARAMETERS_EXT</code>
   *
   */
  public static final String PP_PARAMETERS_EXT = "parameters";

  /**
   * constant <code>PP_ENUMERATED_DOMAINS_EXT</code>
   *
   */
  public static final String PP_ENUMERATED_DOMAINS_EXT = "enumeratedDomains";

  /**
   * constant <code>PP_INTERVAL_DOMAINS_EXT</code>
   *
   */
  public static final String PP_INTERVAL_DOMAINS_EXT = "intervalDomains";

  /**
   * constant <code>PP_CONSTRAINTS_EXT</code>
   *
   */
  public static final String PP_CONSTRAINTS_EXT = "constraints";

  /**
   * constant <code>PP_TOKEN_RELATIONS_EXT</code>
   *
   */
  public static final String PP_TOKEN_RELATIONS_EXT = "tokenRelations";

  /**
   * constant <code>PP_PARAM_VAR_TOKEN_MAP_EXT</code>
   *
   */
  public static final String PP_PARAM_VAR_TOKEN_MAP_EXT = "paramVarTokenMap";

  /**
   * constant <code>PP_CONSTRAINT_VAR_MAP_EXT</code>
   *
   */
  public static final String PP_CONSTRAINT_VAR_MAP_EXT = "constraintVarMap";

  public static final String SEQ_PP_STATS = "partialPlanStats";

  public static final String SEQ_FILE = "sequence";
  
  public static final String SEQ_TRANSACTIONS = "transactions";

  //public static final String PP_TRANSACTIONS_EXT = "transactions";

  /**
   * constant <code>PARTIAL_PLAN_FILE_EXTS</code> - array of Strings
   *
   */
  public static final String [] PARTIAL_PLAN_FILE_EXTS =
    new String []
      { PP_PARTIAL_PLAN_EXT, PP_OBJECTS_EXT,
        PP_TIMELINES_EXT, PP_SLOTS_EXT,
        PP_TOKENS_EXT, PP_VARIABLES_EXT,
        PP_PREDICATES_EXT, PP_PARAMETERS_EXT,
        PP_ENUMERATED_DOMAINS_EXT, PP_INTERVAL_DOMAINS_EXT,
        PP_CONSTRAINTS_EXT, PP_TOKEN_RELATIONS_EXT,
        PP_PARAM_VAR_TOKEN_MAP_EXT, PP_CONSTRAINT_VAR_MAP_EXT/*, PP_TRANSACTIONS_EXT */};

  /**
   * constant <code>NUMBER_OF_PP_FILES</code>
   *
   */
  public static final int NUMBER_OF_PP_FILES = PARTIAL_PLAN_FILE_EXTS.length;


  public static final String [] SEQUENCE_FILES =  new String [] { SEQ_PP_STATS, SEQ_FILE, 
                                                                  SEQ_TRANSACTIONS};

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


  public static final String TBL_CONSTRAINT = "VConstraint";
  public static final String TBL_CONSTVARMAP = "ConstraintVarMap";
  public static final String TBL_ENUMDOMAIN = "EnumeratedDomain";
  public static final String TBL_INTDOMAIN = "IntervalDomain";
  public static final String TBL_OBJECT = "Object";
  public static final String TBL_PARAMVARTOKMAP = "ParamVarTokenMap";
  public static final String TBL_PARAMETER = "Parameter";
  public static final String TBL_PARTIALPLAN = "PartialPlan";
  public static final String TBL_PREDICATE = "Predicate";
  public static final String TBL_PROJECT = "Project";
  public static final String TBL_SEQUENCE = "Sequence";
  public static final String TBL_SLOT = "Slot";
  public static final String TBL_TIMELINE = "Timeline";
  public static final String TBL_TOKEN = "Token";
  public static final String TBL_TOKENREL = "TokenRelation";
  public static final String TBL_VARIABLE = "Variable";
  public static final String TBL_TRANSACTION = "Transaction";

  public static final String [] PW_DB_TABLES = new String [] {
    TBL_PARTIALPLAN, TBL_OBJECT, TBL_TIMELINE, TBL_SLOT, TBL_TOKEN, TBL_VARIABLE, TBL_PREDICATE,
    TBL_PARAMETER, TBL_ENUMDOMAIN, TBL_INTDOMAIN, TBL_CONSTRAINT, TBL_TOKENREL, TBL_PARAMVARTOKMAP,
    TBL_CONSTVARMAP, TBL_TRANSACTION, TBL_SEQUENCE, TBL_PROJECT};

  // number of numeric characters in a type long value
  public static final int LONG_INT_LENGTH = 13;

} // end interface DbConstants
