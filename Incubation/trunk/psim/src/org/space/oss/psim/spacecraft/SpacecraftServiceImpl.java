package org.space.oss.psim.spacecraft;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.space.oss.psim.Config;
import org.space.oss.psim.PSim;
import org.space.oss.psim.Spacecraft;
import org.space.oss.psim.SpacecraftService;
import org.springframework.beans.factory.annotation.Autowired;


public class SpacecraftServiceImpl implements SpacecraftService 
{
	private static Logger LOG = Logger.getLogger(SpacecraftServiceImpl.class);
	protected Map<String,Spacecraft> spacecraft_;
	
	protected PSim psim_;
	
	@Autowired
	protected SpacecraftFactory spacecraftFactory;
	
	@Override
	public void init(PSim psim, Config cfg) 
	{
		psim_ = psim;
		spacecraft_ = new TreeMap<String,Spacecraft>();
		// TODO: make number of SC configurable
		addSpacecraft("SC-1");
		LOG.info("Initialized SpacecraftService");
	}
	
	protected void addSpacecraft(String id)
	{
		Spacecraft s = spacecraftFactory.makeSpacecraft(id, psim_);
		spacecraft_.put(s.getID(), s);
	}

	@Override
	public void shutdown() 
	{
		LOG.info("Shut down SpacecraftService");
	}

	@Override
	public Spacecraft getSpacecraftByID(String spacecraftID) 
	{
		return spacecraft_.get(spacecraftID);
	}
}
