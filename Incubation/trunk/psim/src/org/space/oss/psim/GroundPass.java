package org.space.oss.psim;

import java.util.List;

public interface GroundPass 
{
	long getTime();
	List<Command> getCommands();
    void addCommand(Command c);
	void removeCommand(Command c);
	void setDiscardOnfailure(boolean b);
	void setRetries(int r);
	void execute();
	GroundStation getGroundStation();
}
