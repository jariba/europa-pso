package gov.nasa.arc.planworks.viz.viewMgr;

import java.util.List;

import gov.nasa.arc.planworks.db.util.ContentSpec;

public interface ViewableObject {
  public void setContentSpec(List spec);
  public List getContentSpec();
  public String getName();
}
