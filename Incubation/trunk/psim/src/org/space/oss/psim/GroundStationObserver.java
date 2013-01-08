package org.space.oss.psim;

public interface GroundStationObserver 
{
	public void handleEvent(GroundStation.GSEvent type, Command c);
}
