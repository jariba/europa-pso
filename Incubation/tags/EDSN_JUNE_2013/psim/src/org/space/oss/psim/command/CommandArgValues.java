package org.space.oss.psim.command;

import java.util.HashMap;

public class CommandArgValues 
	extends HashMap<String,Object>
{
	private static final long serialVersionUID = 1L;

	public int getInt(String name)
    {
    	return new Integer(get(name).toString()).intValue();
    }
    
    public double getDouble(String name)
    {
    	return new Double(get(name).toString()).doubleValue();
    }
}
