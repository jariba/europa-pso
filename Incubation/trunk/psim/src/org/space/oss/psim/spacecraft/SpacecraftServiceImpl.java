package org.space.oss.psim.spacecraft;

import org.apache.log4j.Logger;
import org.space.oss.psim.Config;
import org.space.oss.psim.PSim;
import org.space.oss.psim.Spacecraft;
import org.space.oss.psim.SpacecraftService;


public class SpacecraftServiceImpl implements SpacecraftService 
{
	private static Logger LOG = Logger.getLogger(SpacecraftServiceImpl.class);

	@Override
	public void init(PSim psim, Config cfg) 
	{
		LOG.info("Initialized SpacecraftService");
	}

	@Override
	public void shutdown() 
	{
		LOG.info("Shut down SpacecraftService");
	}

	@Override
	public Spacecraft getSpacecraftByID(String spacecraftID) 
	{
		// TODO Auto-generated method stub
		return null;
	}
}
