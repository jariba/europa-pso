package org.space.oss.psim.command;

import org.apache.log4j.Logger;
import org.space.oss.psim.CommandService;
import org.space.oss.psim.Config;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.PSim;


public class CommandServiceImpl implements CommandService 
{
	private static Logger LOG = Logger.getLogger(CommandServiceImpl.class);

	PSim psim_;
	
	@Override
	public void init(PSim psim, Config cfg) 
	{
		psim_ = psim;
		LOG.info("Initialized CommandService");
	}

	@Override
	public void shutdown() 
	{
		LOG.info("Shut down CommandService");
	}

	@Override
	public GroundStation getGroundStationByID(String gsID) {
		// TODO Auto-generated method stub
		return null;
	}
}
