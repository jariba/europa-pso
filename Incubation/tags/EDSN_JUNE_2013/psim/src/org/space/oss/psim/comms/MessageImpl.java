package org.space.oss.psim.comms;

import org.space.oss.psim.Message;

public class MessageImpl 
	implements Message 
{
	protected String sender_;
	protected String destination_;
	protected Object payload_;
	
	public MessageImpl(String sender,String destination, Object payload)
	{
		sender_ = sender;
		destination_ = destination;
		payload_ = payload;
	}

	@Override
	public String getSender() { return sender_; }

	@Override
	public String getDesination() { return destination_; }

	@Override
	public Object getPayload() { return payload_; }
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		buf.append("Msg{").append(sender_).append(",").append(destination_).append(",").append(payload_).append("}");
		return buf.toString();
	}
}
