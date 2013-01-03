package org.space.oss.psim.command;

import java.util.HashMap;

public class CommandArgValues 
	extends HashMap<String,String>
{
	private static final long serialVersionUID = 1L;

	public int getInt(String name)
    {
    	return new Integer((String)get(name)).intValue();
    }
    
    public double getDouble(String name)
    {
    	return new Double((String)get(name)).doubleValue();
    }
}
