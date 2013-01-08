package org.space.oss.psim.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.space.oss.psim.Command;
import org.space.oss.psim.CommandDescriptor;
import org.space.oss.psim.CommandService;
import org.space.oss.psim.Config;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.PSim;
import org.space.oss.psim.PSimServiceBase;


public class CommandServiceImpl extends PSimServiceBase implements CommandService 
{
	private static Logger LOG = Logger.getLogger(CommandServiceImpl.class);
	
	protected Map<String,GroundStation> groundStations_;
	protected List<CommandDescriptor> commandDictionary_;
	
	@Override
	public void init(PSim psim, Config cfg) 
	{
		super.init(psim,cfg);
		groundStations_ = new TreeMap<String,GroundStation>();
		// TODO: make number of GS configurable
		addGroundStation("GS-1");
		addGroundStation("GS-2");
		
		commandDictionary_ = new ArrayList<CommandDescriptor>();
		
		LOG.info("Initialized CommandService");
	}

	protected void addGroundStation(String id)
	{
		GroundStation s = new GroundStationImpl(id,psim_);
		groundStations_.put(s.getID(), s);
		psim_.getTelemetryService().addTelemetrySource(s);
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

	@Override
	public Collection<CommandDescriptor> getCommandDictionary() 
	{
		return commandDictionary_;
	}

	@Override
	public void setCommandDictionary(Collection<CommandDescriptor> cd) 
	{
		commandDictionary_.clear();
		commandDictionary_.addAll(cd);
	}

	@Override
	public Collection<GroundStation> getGroundStations() 
	{
		return groundStations_.values();
	}
}
