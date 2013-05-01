package org.space.oss.psim;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class PSimImpl implements PSim 
{
	private static Logger LOG = Logger.getLogger(PSimImpl.class);

	@Autowired
	protected TimeService timeService;
	@Autowired
	protected CommandService commandService;
	@Autowired
	protected SpacecraftService spacecraftService;
	@Autowired
	protected TelemetryService telemetryService;

	protected Properties versionInfo;
	List<PSimService> services;
	
	public PSimImpl()
	{	
		services = new ArrayList<PSimService>();
	}
	
	@Override
	public void init(Config cfg) 
	{
		services.add(timeService);
		services.add(telemetryService);
		services.add(commandService);
		services.add(spacecraftService);
		
		for (PSimService s : services)
			s.init(this, cfg);
		
		LOG.info("Initialized PSim");
	}

	@Override
	public void shutdown() 
	{
		for (PSimService s : services)
			s.shutdown();
		
		LOG.info("PSim shutdown completed succesfully");
	}

	@Override
	public TimeService getTimeService() { return timeService; }
	
	@Override
	public CommandService getCommandService() { return commandService; }

	@Override
	public TelemetryService getTelemetryService() { return telemetryService; }

	@Override
	public SpacecraftService getSpacecraftService() { return spacecraftService; }

	@Override
	public Properties getVersionInfo() { return versionInfo; }
	public void setVersionInfo(Properties vi) { versionInfo = vi; }

	@Override
	public void save(String dir) 
	{
		for (PSimService s : this.services)
			s.save(dir);
	}

	@Override
	public void load(String dir) 
	{
		for (PSimService s : this.services)
			s.load(dir);
	}
}
