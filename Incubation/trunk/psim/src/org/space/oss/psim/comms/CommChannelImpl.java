package org.space.oss.psim.comms;

import org.space.oss.psim.CommChannel;
import org.space.oss.psim.Message;
import org.space.oss.psim.MessageReceiver;
import org.space.oss.psim.PSim;

public class CommChannelImpl implements CommChannel 
{
	protected PSim psim_;
	protected String origin_;
	protected String destination_;
	protected MessageReceiver destinationObj_;
	protected double reliability_ = 1.0;
	
	public CommChannelImpl(PSim psim, String origin, String dest)
	{	
		psim_ = psim;
		origin_ = origin;
		destination_ = dest;
		
		if (dest.startsWith("GS"))
			destinationObj_ = psim_.getCommandService().getGroundStationByID(dest);
		else
			destinationObj_ = psim_.getSpacecraftService().getSpacecraftByID(dest);
		assert destinationObj_ != null;
	}
	
	@Override
	public String getOrigin() { return origin_; }

	@Override
	public String getDestination() { return destination_; }

	@Override
	public boolean sendMessage(Message message) 
	{
		// TODO: use channel reliability so that this fails with some probability
		destinationObj_.receiveMessage(message);
		return true;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("CommChannel{")
		   .append(origin_).append(",")
		   .append(destination_).append(",")
		   .append(reliability_).append("}");
		
		return buf.toString();
	}

}
