package org.space.oss.psim.spacecraft;

import org.apache.log4j.Logger;
import org.space.oss.psim.Message;
import org.space.oss.psim.Spacecraft;

public class SpacecraftImpl implements Spacecraft 
{
	private static Logger LOG = Logger.getLogger(SpacecraftImpl.class);
	
	protected String id_;
	
	public SpacecraftImpl(String id)
	{
		id_ = id;
	}
	
	@Override
	public String getID() { return id_; }

	@Override
	public void receiveMessage(Message message) 
	{
		LOG.debug(getID()+ " received: "+message);
	}
}
