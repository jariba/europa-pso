package org.space.oss.psim;

public interface TelemetryObserver 
{
	public void handleNewTelemetry(TelemetrySource source,long time,Object data);
}
