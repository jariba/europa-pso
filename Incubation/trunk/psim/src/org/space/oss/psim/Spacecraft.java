package org.space.oss.psim;

import org.space.oss.psim.spacecraft.Subsystem;

public interface Spacecraft 
	extends MessageReceiver
{
	public String getID();
	public PSim getPSim();	
	public void init();

	public Subsystem getSubsystem(String name);
	public CommChannel getCommChannel(String dest);
}
