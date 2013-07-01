package org.space.oss.psim;

import java.util.List;
import java.util.Map;

import org.space.oss.psim.spacecraft.Subsystem;

public interface Spacecraft 
	extends MessageReceiver, PSimObservable
{
	// Events
	public static final int COMMAND_EXECUTED=0;
	public static final int ACTIVITY_GENERATED=1;
	
	public void setProperty(String name, Object value);
	public Object getProperty(String name);
	public Map<String,Object> getProperties();
	
	public String getID();
	public PSim getPSim();	
	public void init();

	public Subsystem getSubsystem(String name);
	public CommChannel getCommChannel(String dest);
	public List<Object> getCommandTrace();
	public void notifyEvent(int type, Object arg);
	
	// Generators for events relevant to discrete simulation
	public List<PSimEventGenerator> getEventGenerators();
}
