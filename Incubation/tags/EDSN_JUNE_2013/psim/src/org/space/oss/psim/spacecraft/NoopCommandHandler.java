package org.space.oss.psim.spacecraft;

import org.space.oss.psim.CommChannel;
import org.space.oss.psim.Command;
import org.space.oss.psim.Spacecraft;
import org.space.oss.psim.comms.MessageImpl;

public class NoopCommandHandler implements CommandHandler 
{
	protected Spacecraft spacecraft_;
	
	public NoopCommandHandler(Spacecraft s)
	{
		spacecraft_ = s;
	}
	
	@Override
	public void execute(String sender, Command c) 
	{
		CommChannel cc = spacecraft_.getCommChannel(sender);
		cc.sendMessage(new MessageImpl(spacecraft_.getID(),sender,"OK- "+spacecraft_.getID()+" processed "+c));		
	}
}
