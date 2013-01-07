package org.space.oss.psim;

import java.util.List;

public interface TelemetryService extends PSimService
{
	public void addTelemetrySource(TelemetrySource ts);
	public void removeTelemetrySource(TelemetrySource ts);
	
	public List<TelemetryRecord> getAllTelemetry();
	
	void addObserver(TelemetryObserver to);
	void removeObserver(TelemetryObserver to);
}
