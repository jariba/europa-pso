package org.space.oss.psim;

public interface CommChannel 
{
	public String getOrigin();
	public String getDestination();
	
	public boolean sendMessage(Object message);
	public boolean sendResponse(Object message);
}
