// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ContentSpec.java,v 1.17 2004-02-25 02:30:14 taylor Exp $
//
// PlanWorks
//

package gov.nasa.arc.planworks.db.util;

import java.util.List;

public interface ContentSpec {

  public static final String CONTENT_SPEC_TITLE = "ContentFilter";
  // the next two must be unique as far as String/indexOf()
  public static final String SEQUENCE_QUERY_TITLE = "SequenceQuery";
  public static final String SEQUENCE_QUERY_RESULTS_TITLE = "QueryResults";

  public List getValidIds();
  public List getCurrentSpec();
  public void resetSpec();
  public void applySpec(final List spec);
}
