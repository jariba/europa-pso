package org.space.oss.psim;

import java.util.Collection;

public interface CommandService extends PSimService
{
	public Collection<GroundStation> getGroundStations();
	
	public GroundStation getGroundStationByID(String gsID);
	
	public Command makeCommand(String type,String args);
	
	public Collection<CommandDescriptor> getCommandDictionary();
	
	public void setCommandDictionary(Collection<CommandDescriptor> cd);	
}
