package org.space.oss.psim.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.space.oss.psim.CommChannel;
import org.space.oss.psim.Command;
import org.space.oss.psim.GroundPass;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.GroundStationObserver;
import org.space.oss.psim.Message;
import org.space.oss.psim.PSim;
import org.space.oss.psim.PSimEvent;
import org.space.oss.psim.PSimEventGenerator;
import org.space.oss.psim.PSimEventGeneratorBase;
import org.space.oss.psim.TelemetryObserver;
import org.space.oss.psim.TimeServiceObserver;
import org.space.oss.psim.comms.CommChannelImpl;
import org.space.oss.psim.comms.MessageImpl;

public class GroundStationImpl 
	implements GroundStation, TimeServiceObserver
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
		groundPasses_ = new TreeMap<Long,GroundPass>();
		psim_.getTimeService().addObserver(this);
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
		boolean result =  sendCommand(c,retries);
		
		if (!result && discardOnFail) {
			commands_.remove(c);
			LOG.debug("Discarded Command:"+c);
		}
		
		return result;
	}
	
	@Override
	public boolean sendCommand(Command c, int retries)
	{
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
		
		LOG.debug(c + " failed to be sent after "+retries+ " attempts");
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

	protected void notifyEvent(GSEvent type,Object o)
	{
		for (GroundStationObserver gso : observers_)
			gso.handleEvent(type, o);
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
	public GroundPass addGroundPass(long time) 
	{
		if (groundPasses_.containsKey(time))
			throw new RuntimeException(getID()+" already has a GroundPass at time "+time);

		GroundPass gp = new GroundPassImpl(this,time);
		groundPasses_.put(time,gp);
		notifyEvent(GSEvent.GROUND_PASS_ADDED, gp);
		return gp;
	}

	@Override
	public void removeGroundPass(long time) 
	{
		if (!groundPasses_.containsKey(time))
			throw new RuntimeException(getID()+" doesn't have a GroundPass at time "+time);
		
		GroundPass gp = groundPasses_.remove(time);
		notifyEvent(GSEvent.GROUND_PASS_REMOVED, gp);
	}

	@Override
	public Collection<GroundPass> getGroundPasses() 
	{
		return groundPasses_.values();
	}
	
	@Override 
	public GroundPass getGroundPass(long time)
	{
		return groundPasses_.get(time);
	}

	@Override
	public List<PSimEventGenerator> getEventGenerators() 
	{
		List<PSimEventGenerator> retval = new ArrayList<PSimEventGenerator>();
		retval.add(new GSEventGenerator());
		return retval;
	}
	
	protected class GSEventGenerator extends PSimEventGeneratorBase 
		implements GroundStationObserver
	{
		
		public GSEventGenerator()
		{
			super(getID());
			addObserver(this);
		}

		@Override
		public List<PSimEvent> getNextEvents(long time) 
		{
			List<PSimEvent> retval = new ArrayList<PSimEvent>();
			
			for (GroundPass gp : groundPasses_.values()) {
				if (gp.getTime() > time)
					retval.add(new PSimEvent(gp.getTime(), this, getID()+" GroundPass"));
			}
			
			return retval;
		}

		@Override
		public void disable() {
			// TODO Auto-generated method stub			
		}

		@Override
		public void handleEvent(GSEvent type, Object o) 
		{
			if (type.equals(GSEvent.GROUND_PASS_ADDED) ||
					type.equals(GSEvent.GROUND_PASS_REMOVED)) {
				manager_.setNextEvents(this, getNextEvents(manager_.getPSim().getTimeService().getCurrentTime()));
			}
			
		}
		
	}

	@Override
	public void handleCurrentTime(long t) 
	{
		List<GroundPass> toExecute = new ArrayList<GroundPass>();
		for (GroundPass gp : this.groundPasses_.values()) {
			if (gp.getTime() == t)
				toExecute.add(gp);
		}
		
		for (GroundPass gp : toExecute)
			gp.execute();
	}
}
