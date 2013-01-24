package org.space.oss.psim;

import java.util.Collection;
import java.util.List;

public interface GroundStation 
	extends MessageReceiver, TelemetrySource
{
	public enum GSEvent {
		COMMAND_QUEUED, COMMAND_SENT, COMMAND_REMOVED, QUEUE_CLEARED,
		GROUND_PASS_ADDED,GROUND_PASS_REMOVED,GP_COMMAND_QUEUED, GP_COMMAND_REMOVED};
	
	String getID();

	boolean sendCommand(Command c);
	boolean sendCommand(Command c, int retries);	
	
	// Methods to manage command queue
	void queueCommand(Command c);
	void removeCommand(Integer commandID);
	boolean sendQueuedCommand(int retries,boolean discardOnFail);
	void sendAllQueuedCommands(int retries,boolean discardOnFail);
	List<Command> getCommandQueue();
	void clearCommandQueue();
	
	void addObserver(GroundStationObserver o);
	void removeObserver(GroundStationObserver o);

	GroundPass addGroundPass(long time);
	void removeGroundPass(long time);
	Collection<GroundPass> getGroundPasses();
	GroundPass getGroundPass(long time);
}
