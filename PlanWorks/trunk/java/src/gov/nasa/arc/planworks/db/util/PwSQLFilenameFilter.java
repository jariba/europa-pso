package gov.nasa.arc.planworks.db.util;

import java.io.File;
import java.io.FilenameFilter;

import gov.nasa.arc.planworks.db.DbConstants;

public class PwSQLFilenameFilter implements FilenameFilter {
  public PwSQLFilenameFilter() {
  }
  public boolean accept(File dir, String name) {
    return (name.endsWith(DbConstants.PP_PARTIAL_PLAN_EXT) || 
            name.endsWith(DbConstants.PP_OBJECTS_EXT) || 
            name.endsWith(DbConstants.PP_TIMELINES_EXT) || 
            name.endsWith(DbConstants.PP_SLOTS_EXT) ||
            name.endsWith(DbConstants.PP_TOKENS_EXT) || 
            name.endsWith(DbConstants.PP_VARIABLES_EXT) ||
            name.endsWith(DbConstants.PP_PREDICATES_EXT) || 
            name.endsWith(DbConstants.PP_PARAMETERS_EXT) ||
            name.endsWith(DbConstants.PP_ENUMERATED_DOMAINS_EXT) || 
            name.endsWith(DbConstants.PP_INTERVAL_DOMAINS_EXT) ||
            name.endsWith(DbConstants.PP_CONSTRAINTS_EXT) ||
            name.endsWith(DbConstants.PP_TOKEN_RELATIONS_EXT) || 
            name.endsWith(DbConstants.PP_PARAM_VAR_TOKEN_MAP_EXT) ||
            name.endsWith(DbConstants.PP_CONSTRAINT_VAR_MAP_EXT));
  }
}
