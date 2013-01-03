package org.space.oss.psim;

import java.util.Map;

public interface Command 
{
	public Integer getID();
	public String getType();
	public String getDestination();
	
	public Map<String,Object> getArgs();
	public void setArgs(Map<String,Object> args);
	
	/*
	 * args must follow the format:
	 * arg1=value1,arg2=value2,...
	 */
	public void setArgs(String args);
}
