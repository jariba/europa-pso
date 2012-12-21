package org.space.oss.psim.telemetry;

import org.apache.log4j.Logger;
import org.space.oss.psim.Config;
import org.space.oss.psim.PSim;
import org.space.oss.psim.TelemetryService;


public class TelemetryServiceImpl implements TelemetryService 
{
	private static Logger LOG = Logger.getLogger(TelemetryServiceImpl.class);

	@Override
	public void init(PSim psim, Config cfg) 
	{
		LOG.info("Initialized TelemetryService");
	}

	@Override
	public void shutdown() 
	{
		LOG.info("Shut down TelemetryService");
	}
}
