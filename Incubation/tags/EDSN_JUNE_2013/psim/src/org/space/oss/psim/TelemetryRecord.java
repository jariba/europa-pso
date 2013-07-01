package org.space.oss.psim;

public interface TelemetryRecord 
{
	public long getTime();
	public TelemetrySource getSource();
	public Object getData();
}
