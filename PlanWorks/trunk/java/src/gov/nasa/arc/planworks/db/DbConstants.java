// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DbConstants.java,v 1.6 2003-09-10 00:23:09 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09June03
//

package gov.nasa.arc.planworks.db;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;


/**
 * interface <code>DbConstants</code> - constants for use by .viz.* packages
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface DbConstants {

  /**
   * constant <code>PP_PARTIAL_PLAN_EXT</code>
   *
   */
  public static final String PP_PARTIAL_PLAN_EXT = "partialplan";

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
  public static final String PP_ENUMERATED_DOMAINS_EXT = "enumerateddomains";

  /**
   * constant <code>PP_INTERVAL_DOMAINS_EXT</code>
   *
   */
  public static final String PP_INTERVAL_DOMAINS_EXT = "intervaldomains";

  /**
   * constant <code>PP_CONSTRAINTS_EXT</code>
   *
   */
  public static final String PP_CONSTRAINTS_EXT = "constraints";

  /**
   * constant <code>PP_TOKEN_RELATIONS_EXT</code>
   *
   */
  public static final String PP_TOKEN_RELATIONS_EXT = "tokenrelations";

  /**
   * constant <code>PP_PARAM_VAR_TOKEN_MAP_EXT</code>
   *
   */
  public static final String PP_PARAM_VAR_TOKEN_MAP_EXT = "paramvartokenmap";

  /**
   * constant <code>PP_CONSTRAINT_VAR_MAP_EXT</code>
   *
   */
  public static final String PP_CONSTRAINT_VAR_MAP_EXT = "constraintvarmap";

  /**
   * constant <code>PARTIAL_PLAN_FILE_EXTS</code> - List of Strings
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
        PP_PARAM_VAR_TOKEN_MAP_EXT, PP_CONSTRAINT_VAR_MAP_EXT };

  /**
   * constant <code>NUMBER_OF_PP_FILES</code>
   *
   */
  public static final int NUMBER_OF_PP_FILES = PARTIAL_PLAN_FILE_EXTS.length;

} // end interface DbConstants
