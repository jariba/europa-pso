package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.util.List;

public interface ContentSpecElement
{
  public List getValue() throws NullPointerException;
  public void reset();
}
