package org.space.oss.psim;

import java.util.Properties;

public interface PSim 
{
	public void init(Config cfg);
    public void shutdown();
    public Properties getVersionInfo();
	public void save(String dir);
	public void load(String dir);
    
    public CommandService getCommandService();
    public TelemetryService getTelemetryService();
    public SpacecraftService getSpacecraftService();
    public TimeService getTimeService();
}
