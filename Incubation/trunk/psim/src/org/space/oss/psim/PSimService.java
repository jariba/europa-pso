package org.space.oss.psim;

public interface PSimService 
{
	public void init(PSim psim, Config cfg);
    public void shutdown();
	public PSim getPSim();
	public void save(String dir);
	public void load(String dir);
}
