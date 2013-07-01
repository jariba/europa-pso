package org.space.oss.psim;

import java.util.Map;
import java.util.TreeMap;

public class ConfigByMap 
    implements Config 
{
	protected Map<String,String> values_;
	protected String[] args_;
	
	public ConfigByMap(String args[])
	{
	    values_ = new TreeMap<String,String>();
	    args_ = args;
	}
	
	public void addValue(String name,String value)
	{
	    values_.put(name, value);	
	}
	
	public String getValue(String name) 
	{
		return values_.get(name);
	}	
}
