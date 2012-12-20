package org.space.oss.psim;

import java.util.Map;

public interface Command 
{
	public Integer getID();
	public String getType();
	public Map<String,Object> getArgs();
	public void setArgs(Map<String,Object> args);
}
