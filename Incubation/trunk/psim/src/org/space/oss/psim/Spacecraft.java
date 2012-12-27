package org.space.oss.psim;

public interface Spacecraft 
	extends MessageReceiver
{
	public String getID();
	public void init();
	
	public CommChannel getCommChannel(String dest);	
}
