package org.space.oss.psim.spacecraft;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.space.oss.psim.CommChannel;
import org.space.oss.psim.Command;
import org.space.oss.psim.Message;
import org.space.oss.psim.PSim;
import org.space.oss.psim.Spacecraft;
import org.space.oss.psim.comms.CommChannelImpl;

public class SpacecraftImpl implements Spacecraft 
{
	private static Logger LOG = Logger.getLogger(SpacecraftImpl.class);
	
	protected PSim psim_;
	protected String id_;
	protected Map<String,Subsystem> subsystems_;
	protected Map<String,CommandHandler> commandHandlers_;
	
	public SpacecraftImpl(String id, PSim psim)
	{
		id_ = id;
		psim_ = psim;
	}
	
	@Override
	public PSim getPSim() { return psim_; }
	
	@Override
	public String getID() { return id_; }

	@Override 
	public void init()
	{
	    subsystems_ = new HashMap<String,Subsystem>();
	    setupSubsystems();
		
	    commandHandlers_ = new HashMap<String,CommandHandler>();
	    setupCommandHandlers(); 
	}
	
	protected void setupCommandHandlers()
	{
		commandHandlers_.put("noop",new NoopCommandHandler(this));
	}
	
	protected void setupSubsystems()
	{
		subsystems_.put("fakeComms", new SubsystemBase(this,"fakeComms"));
	}
	protected CommandHandler getCommandHandler(Command c)
	{
		return commandHandlers_.get(c.getType());
	}
	
	@Override
	public void receiveMessage(Message message) 
	{
		LOG.debug(getID()+ " received: "+message);
		
		if (message.getPayload() instanceof Command) {
			try {
				handleCommand(message.getSender(),(Command)message.getPayload());
			}
			catch (Exception e) {
				LOG.error("Failed executing command: "+((Command)message.getPayload()).getType(),e);
			}
		}
	}
	
	protected void handleCommand(String sender, Command c) 
	{
		CommandHandler ch = getCommandHandler(c);
		if (ch == null) 
			throw new RuntimeException("Unable to find command handler for:"+c);
		else
			ch.execute(sender, c);
	}

	@Override
	public CommChannel getCommChannel(String dest)
	{
		// TODO: implement through factory
		return new CommChannelImpl(psim_,getID(),dest);
	}
	
	protected void addSubsystem(Subsystem s)
	{
		subsystems_.put(s.getName(), s);
	}
	
	public Subsystem getSubsystem(String name)
	{
		Subsystem s = subsystems_.get(name);
		
		if (s==null)
			throw new RuntimeException("Subsystem "+name+" doesn't exist in spacecraft "+getID());
		
		return s;
	}
	
	protected Integer asInt(String arg)
	{
		return Integer.valueOf(arg);
	}
	
	protected Long asLong(String arg)
	{
		return Long.valueOf(arg);
	}	
}
