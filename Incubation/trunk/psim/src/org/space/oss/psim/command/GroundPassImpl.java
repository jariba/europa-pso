package org.space.oss.psim.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.space.oss.psim.Command;
import org.space.oss.psim.GroundPass;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.GroundStation.GSEvent;
import org.space.oss.psim.TimeUtil;

public class GroundPassImpl 
	implements GroundPass 
{
	private static Logger LOG = Logger.getLogger(GroundPassImpl.class);
	
	protected long time_;
	protected List<Command> commands_;
	protected GroundStationImpl groundStation_;
	private boolean discardOnFailure_;
	private int retries_;

	public GroundPassImpl(GroundStationImpl gs, long t) 
	{
		groundStation_ = gs;
		time_ = t;
		commands_ = new ArrayList<Command>();
		discardOnFailure_ = false;
		retries_ = 10;
	}
	
	@Override
	public long getTime() { return time_; }

	@Override
	public List<Command> getCommands() { return commands_; }
	
	@Override
	public void addCommand(Command c) 
	{
		commands_.add(c);
		groundStation_.notifyEvent(GSEvent.GP_COMMAND_QUEUED, new Object[]{this,c});
	}

	@Override
	public void removeCommand(Command c) 
	{
		commands_.remove(c);
		groundStation_.notifyEvent(GSEvent.GP_COMMAND_REMOVED, new Object[]{this,c});
	}
	
	@Override
	public void setRetries(int r) { retries_ = r; }
	
	@Override
	public void setDiscardOnfailure(boolean b) { discardOnFailure_ = b; }
	
	@Override
	public void execute()
	{
		while (commands_.size() > 0) {
			Command c = commands_.get(0);
			boolean ok = groundStation_.sendCommand(c,retries_);
			if (ok || discardOnFailure_) {
				removeCommand(c);
				LOG.info("Discarded Command:"+c);
			}
			else
				break;
		}
	}
	
	public String toString()
	{
		return TimeUtil.toGMTPlusLongString(getTime());
	}

	@Override
	public GroundStation getGroundStation() 
	{
		return groundStation_;
	}
}
