//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PwSQLFilenameFilter.java,v 1.4 2003-10-02 23:16:56 miatauro Exp $
//
package gov.nasa.arc.planworks.db.util;

import java.io.File;
import java.io.FilenameFilter;

import gov.nasa.arc.planworks.db.DbConstants;

/**
 * A class for filtering out files not in the established structure for database import
 */

public class PwSQLFilenameFilter implements FilenameFilter {
  public PwSQLFilenameFilter() {
  }
  public boolean accept(File dir, String name) {
    for(int i = 0; i < DbConstants.NUMBER_OF_PP_FILES; i++) {
      if(name.endsWith(DbConstants.PARTIAL_PLAN_FILE_EXTS[i])) {
        return true;
      }
    }
    return false;
  }
}
