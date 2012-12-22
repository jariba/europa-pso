package org.space.oss.psim.command;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.space.oss.psim.Command;
import org.space.oss.psim.CommandService;
import org.space.oss.psim.Config;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.PSim;


public class CommandServiceImpl implements CommandService 
{
	private static Logger LOG = Logger.getLogger(CommandServiceImpl.class);
	protected Map<String,GroundStation> groundStations_;

	PSim psim_;
	
	@Override
	public void init(PSim psim, Config cfg) 
	{
		psim_ = psim;
		groundStations_ = new TreeMap<String,GroundStation>();
		// TODO: make number of GS configurable
		addGroundStation("GS-1");		
		LOG.info("Initialized CommandService");
	}

	protected void addGroundStation(String id)
	{
		GroundStation s = new GroundStationImpl(id,psim_);
		groundStations_.put(s.getID(), s);
	}
	
	@Override
	public void shutdown() 
	{
		LOG.info("Shut down CommandService");
	}

	@Override
	public GroundStation getGroundStationByID(String gsID) 
	{
		return groundStations_.get(gsID);
	}

	@Override
	public Command makeCommand(String type, String args) 
	{
		return new CommandImpl(type,args);
	}
}
