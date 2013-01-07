package org.space.oss.psim;

public interface TelemetryService extends PSimService
{
	public void addTelemetrySource(TelemetrySource ts);
	public void removeTelemetrySource(TelemetrySource ts);
}
