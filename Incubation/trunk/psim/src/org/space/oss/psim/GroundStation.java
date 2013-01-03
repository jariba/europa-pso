package org.space.oss.psim;

public interface GroundStation 
	extends MessageReceiver
{
	public String getID();

	public boolean sendCommand(Command c);
	
	// Methods to manage command queue
	public void queueCommand(Command c);
	public void removeCommand(Integer commandID);
	public boolean sendQueuedCommand(int retries,boolean discardOnFail);
	public void sendAllQueuedCommands(int retries,boolean discardOnFail);
	public int getCommandQueueSize();
	public void clearCommandQueue();
}
