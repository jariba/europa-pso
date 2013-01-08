package org.space.oss.psim;

import java.util.List;

public interface GroundStation 
	extends MessageReceiver, TelemetrySource
{
	public enum GSEvent {COMMAND_QUEUED, COMMAND_SENT, COMMAND_REMOVED, QUEUE_CLEARED};
	
	public String getID();

	public boolean sendCommand(Command c);
	
	// Methods to manage command queue
	public void queueCommand(Command c);
	public void removeCommand(Integer commandID);
	public boolean sendQueuedCommand(int retries,boolean discardOnFail);
	public void sendAllQueuedCommands(int retries,boolean discardOnFail);
	public List<Command> getCommandQueue();
	public void clearCommandQueue();
	
	public void addObserver(GroundStationObserver o);
	public void removeObserver(GroundStationObserver o);
}
