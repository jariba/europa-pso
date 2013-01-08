package org.space.oss.psim;

public interface PSimService 
{
	public void init(PSim psim, Config cfg);
    public void shutdown();
	PSim getPSim();
}
