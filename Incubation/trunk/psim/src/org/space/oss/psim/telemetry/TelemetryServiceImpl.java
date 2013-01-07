package org.space.oss.psim.telemetry;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.space.oss.psim.Config;
import org.space.oss.psim.PSim;
import org.space.oss.psim.TelemetryObserver;
import org.space.oss.psim.TelemetryService;
import org.space.oss.psim.TelemetrySource;


public class TelemetryServiceImpl 
	implements TelemetryService, TelemetryObserver
{
	private static Logger LOG = Logger.getLogger(TelemetryServiceImpl.class);
	
	protected List<TelemetrySource> sources_;

	@Override
	public void init(PSim psim, Config cfg) 
	{
		sources_ = new ArrayList<TelemetrySource>();
		LOG.info("Initialized TelemetryService");
	}

	@Override
	public void shutdown() 
	{
		LOG.info("Shut down TelemetryService");
	}

	@Override
	public void addTelemetrySource(TelemetrySource ts) 
	{
		ts.addObserver(this);
		sources_.add(ts);
	}

	@Override
	public void removeTelemetrySource(TelemetrySource ts) 
	{
		ts.removeObserver(this);
		sources_.remove(ts);
	}

	@Override
	public void handleNewTelemetry(TelemetrySource source, long time,
			Object data) 
	{
		// TODO: store telemetry somewhere
		LOG.debug("Received: {src="+source.getID()+" , time="+time+" , data="+data.toString());
	}
}
