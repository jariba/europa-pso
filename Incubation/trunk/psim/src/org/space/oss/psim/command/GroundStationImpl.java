package org.space.oss.psim.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.space.oss.psim.CommChannel;
import org.space.oss.psim.Command;
import org.space.oss.psim.GroundPass;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.GroundStationObserver;
import org.space.oss.psim.Message;
import org.space.oss.psim.PSim;
import org.space.oss.psim.TelemetryObserver;
import org.space.oss.psim.comms.CommChannelImpl;
import org.space.oss.psim.comms.MessageImpl;

public class GroundStationImpl 
	implements GroundStation
{
	private static Logger LOG = Logger.getLogger(GroundStationImpl.class);
		
	protected String id_;
	protected PSim psim_;
	protected List<Command> commands_;
	protected List<TelemetryObserver> telemetryObservers_;
	protected List<GroundStationObserver> observers_;
	protected Map<Long,GroundPass> groundPasses_;

	public GroundStationImpl(String id, PSim psim)
	{
		id_ = id;
		psim_ = psim;
		commands_ = new ArrayList<Command>();
		telemetryObservers_ = new ArrayList<TelemetryObserver>();
		observers_ = new ArrayList<GroundStationObserver>();
	}
	
	@Override
	public String getID() { return id_; }
	
	@Override
	public void queueCommand(Command c) 
	{
		commands_.add(c);
		notifyEvent(GSEvent.COMMAND_QUEUED,c);
	}

	@Override
	public void removeCommand(Integer commandID) 
	{
		for (Command c : commands_) {
			if (c.getID().equals(commandID)) {
				commands_.remove(c);
				notifyEvent(GSEvent.COMMAND_REMOVED,c);
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
			boolean ok = comm.sendMessage(new MessageImpl(getID(),c.getDestination(),c));
			if (ok) {
				commands_.remove(c);
				notifyEvent(GSEvent.COMMAND_SENT,c);
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
		while (getCommandQueue().size() > 0) {
			boolean ok = sendQueuedCommand(retries,discardOnFail);
			if (!ok && !discardOnFail)
				return;
		}
	}

	@Override
	public List<Command> getCommandQueue() 
	{
		return commands_;
	}

	@Override
	public void clearCommandQueue() 
	{
		commands_.clear();
		notifyEvent(GSEvent.QUEUE_CLEARED,null);
	}
	
	protected CommChannel getCommChannel(Command c)
	{
		// TODO: implement through factory
		return new CommChannelImpl(psim_,getID(),c.getDestination());
	}
	
	@Override
	public void receiveMessage(Message message) 
	{
		notifyNewTelemetry(psim_.getTimeService().getCurrentTime(), message);
		LOG.debug(getID()+ " received: "+message);
	}	
	
	public String toString()
	{
		return getID();
	}

	@Override
	public boolean sendCommand(Command c) 
	{
		CommChannel comm = getCommChannel(c);
		boolean result = comm.sendMessage(new MessageImpl(getID(),c.getDestination(),c));
		
		if (result)
			notifyEvent(GSEvent.COMMAND_SENT,c);

		return result;
	}

	@Override
	public void addObserver(TelemetryObserver to) 
	{
		telemetryObservers_.add(to);
	}

	@Override
	public void removeObserver(TelemetryObserver to) 
	{
		telemetryObservers_.remove(to);
	}
	
	protected void notifyNewTelemetry(long time,Object data)
	{
		for (TelemetryObserver to : telemetryObservers_)
			to.handleNewTelemetry(this, time, data);
	}

	protected void notifyEvent(GSEvent type,Command c)
	{
		for (GroundStationObserver gso : observers_)
			gso.handleEvent(type, c);
	}

	@Override
	public void addObserver(GroundStationObserver o) 
	{
		observers_.add(o);
	}

	@Override
	public void removeObserver(GroundStationObserver o) 
	{
		observers_.remove(o);
	}

	@Override
	public GroundPass addGroundPass(long time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeGroundPass(long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<Long, GroundPass> getGroundPasses() {
		// TODO Auto-generated method stub
		return null;
	}
}
