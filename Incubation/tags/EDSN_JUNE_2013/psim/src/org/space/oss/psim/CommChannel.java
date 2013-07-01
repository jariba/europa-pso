package org.space.oss.psim;

public interface CommChannel 
{
	public String getOrigin();
	public String getDestination();
	
	public boolean sendMessage(Message message);
}
