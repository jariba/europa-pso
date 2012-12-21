package org.space.oss.psim;

public interface Message 
{
	public String getSender();
	public String getDesination();
	public Object getPayload();
}
