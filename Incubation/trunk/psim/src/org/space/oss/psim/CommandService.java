package org.space.oss.psim;

public interface CommandService extends PSimService
{
	// Methods to manage event queue
	public void queueCommand(Command c);
	public void removeCommand(Integer commandID);
	public boolean sendQueuedCommand(int retries,boolean discardOnFail);
	public void sendAllQueuedCommands(int retries,boolean discardOnFail);
	public int getCommandQueueSize();
	public void clearCommandQueue();
}
