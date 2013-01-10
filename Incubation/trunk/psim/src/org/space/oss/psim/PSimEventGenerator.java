package org.space.oss.psim;

import java.util.List;

public interface PSimEventGenerator 
{

	public void setManager(PSimEventManager m);

	public List<PSimEvent> getNextEvents(long time);

	public void disable();
}
