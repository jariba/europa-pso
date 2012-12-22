package org.space.oss.psim;

public interface CommandService extends PSimService
{
	GroundStation getGroundStationByID(String gsID);
	
	Command makeCommand(String type,String args);
}
