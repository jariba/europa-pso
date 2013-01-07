package org.space.oss.psim;

public interface TelemetrySource 
{
	public String getID();
	
	public void addObserver(TelemetryObserver to);
	public void removeObserver(TelemetryObserver to);
}
