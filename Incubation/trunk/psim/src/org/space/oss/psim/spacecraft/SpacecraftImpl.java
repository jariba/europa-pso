package org.space.oss.psim.spacecraft;

import org.apache.log4j.Logger;
import org.space.oss.psim.CommChannel;
import org.space.oss.psim.Message;
import org.space.oss.psim.PSim;
import org.space.oss.psim.Spacecraft;
import org.space.oss.psim.comms.CommChannelImpl;
import org.space.oss.psim.comms.MessageImpl;

public class SpacecraftImpl implements Spacecraft 
{
	private static Logger LOG = Logger.getLogger(SpacecraftImpl.class);
	
	protected PSim psim_;
	protected String id_;
	
	public SpacecraftImpl(String id, PSim psim)
	{
		id_ = id;
		psim_ = psim;
	}
	
	@Override
	public String getID() { return id_; }

	@Override
	public void receiveMessage(Message message) 
	{
		LOG.debug(getID()+ " received: "+message);
		String dest = message.getSender();
		CommChannel cc = getCommChannel(dest);
		cc.sendMessage(new MessageImpl(getID(),dest,"OK- "+getID()+" processed "+message));
	}
	
	protected CommChannel getCommChannel(String dest)
	{
		// TODO: implement through factory
		return new CommChannelImpl(psim_,getID(),dest);
	}
}
