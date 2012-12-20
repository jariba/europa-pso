package org.space.oss.psim.command;

import org.apache.log4j.Logger;
import org.space.oss.psim.CommandService;
import org.space.oss.psim.Config;


public class CommandServiceImpl implements CommandService 
{
	private static Logger LOG = Logger.getLogger(CommandServiceImpl.class);

	@Override
	public void init(Config cfg) 
	{
		LOG.info("Initialized CommandService");
	}

	@Override
	public void shutdown() 
	{
		LOG.info("Shut down CommandService");
	}
}
