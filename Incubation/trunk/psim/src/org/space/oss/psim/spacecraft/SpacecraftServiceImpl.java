package org.space.oss.psim.spacecraft;

import org.apache.log4j.Logger;
import org.space.oss.psim.Config;
import org.space.oss.psim.SpacecraftService;


public class SpacecraftServiceImpl implements SpacecraftService 
{
	private static Logger LOG = Logger.getLogger(SpacecraftServiceImpl.class);

	@Override
	public void init(Config cfg) 
	{
		LOG.info("Initialized SpacecraftService");
	}

	@Override
	public void shutdown() 
	{
		LOG.info("Shut down SpacecraftService");
	}
}
