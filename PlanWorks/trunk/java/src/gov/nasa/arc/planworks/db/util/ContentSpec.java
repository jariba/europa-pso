package gov.nasa.arc.planworks.db.util;

import java.util.List;

public interface ContentSpec {

  public static final String CONTENT_SPEC_TITLE = "ContentSpec";

  public List getValidIds();
  public List getCurrentSpec();
  public void resetSpec();
  public void applySpec(List spec);
}
