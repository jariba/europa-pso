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
