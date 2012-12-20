package org.space.oss.psim.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.space.oss.psim.CommChannel;
import org.space.oss.psim.Command;
import org.space.oss.psim.CommandService;
import org.space.oss.psim.Config;


public class CommandServiceImpl implements CommandService 
{
	private static Logger LOG = Logger.getLogger(CommandServiceImpl.class);

	protected List<Command> commands_;
	
	@Override
	public void init(Config cfg) 
	{
		commands_ = new ArrayList<Command>();
		LOG.info("Initialized CommandService");
	}

	@Override
	public void shutdown() 
	{
		LOG.info("Shut down CommandService");
	}

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
			boolean ok = comm.sendMessage(c);
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
		return null;
	}
}
