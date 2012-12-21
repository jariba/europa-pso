package org.space.oss.psim.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.space.oss.psim.CommChannel;
import org.space.oss.psim.Command;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.Message;
import org.space.oss.psim.PSim;
import org.space.oss.psim.comms.CommChannelImpl;
import org.space.oss.psim.comms.MessageImpl;

public class GroundStationImpl implements GroundStation 
{
	private static Logger LOG = Logger.getLogger(GroundStationImpl.class);
		
	String id_;
	PSim psim_;
	protected List<Command> commands_;

	public GroundStationImpl(String id,PSim psim)
	{
		id_ = id;
		psim_ = psim;
		commands_ = new ArrayList<Command>();
	}
	
	@Override
	public String getID() { return id_; }
	
	@Override
	public void queueCommand(Command c) 
	{
		commands_.add(0,c);
	}

	@Override
	public void removeCommand(Integer commandID) 
	{
		for (Command c : commands_) {
			if (c.getID().equals(commandID)) {
				commands_.remove(c);
				return;
			}
		}
		
		throw new RuntimeException("Command "+commandID+" not found for removal");
	}

	@Override
	public boolean sendQueuedCommand(int retries, boolean discardOnFail) 
	{
		assert commands_.size()>0;
		
		Command c = commands_.get(0);
		CommChannel comm = getCommChannel(c);
		
		for (int cnt = 0; cnt < retries; cnt++) {
			// TODO: create comm packet?
			boolean ok = comm.sendMessage(new MessageImpl(getID(),(String)c.getArgs().get("Destination"),c));
			if (ok) {
				LOG.debug(c + " sent successfully");
				return true;
			}
		}
		
		LOG.debug(c + " failed to be sent");
		if (discardOnFail) {
			commands_.remove(c);
			LOG.debug(c + "discarded");
		}
		
		return false;
	}

	@Override
	public void sendAllQueuedCommands(int retries, boolean discardOnFail) 
	{
		while (getCommandQueueSize() > 0) {
			boolean ok = sendQueuedCommand(retries,discardOnFail);
			if (!ok && !discardOnFail)
				return;
		}
	}

	@Override
	public int getCommandQueueSize() 
	{
		return commands_.size();
	}

	@Override
	public void clearCommandQueue() 
	{
		commands_.clear();
	}
	
	protected CommChannel getCommChannel(Command c)
	{
		// TODO: implement through factory
		return new CommChannelImpl(psim_,getID(),(String)c.getArgs().get("Destination"));
	}
	
	@Override
	public void receiveMessage(Message message) 
	{
		// TODO Notify Telemetry service
		LOG.debug(getID()+ " received: "+message);
	}	
}
