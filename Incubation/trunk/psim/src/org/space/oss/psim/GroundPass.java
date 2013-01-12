package org.space.oss.psim;

import java.util.Collection;

public interface GroundPass 
{
	public long getTime();
	public Collection<Command> getCommands();
	public void addCommand(Command c);
	public void removeCommand(Command c);
}
