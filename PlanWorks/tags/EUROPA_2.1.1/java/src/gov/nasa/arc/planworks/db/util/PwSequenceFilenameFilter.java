//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PwSequenceFilenameFilter.java,v 1.1 2003-11-26 01:24:51 miatauro Exp $
//
package gov.nasa.arc.planworks.db.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import gov.nasa.arc.planworks.db.DbConstants;

/**
 * A class for filtering out files not in the established structure for database import
 */

public class PwSequenceFilenameFilter implements FilenameFilter {
  private static final Pattern stepPattern = Pattern.compile("step\\d+");
  public PwSequenceFilenameFilter() {
  }
  public boolean accept(File dir, String name) {
    for(int i = 0; i < DbConstants.NUMBER_OF_SEQ_FILES; i++) {
      if(name.equals(DbConstants.SEQUENCE_FILES[i])) {
        return true;
      }
    }
    if(stepPattern.matcher(name).matches()) {
      return true;
    }
    return false;
  }
}
