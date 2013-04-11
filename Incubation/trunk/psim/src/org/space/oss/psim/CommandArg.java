package org.space.oss.psim;

public class CommandArg 
{
    public String name;
    public String type;
    public String arraySize;
    public String description;
    public String defaultValue;
    
    public CommandArg(String n,String d,String dv)
    {
    	name=n;
    	description=d;
    	defaultValue=dv;
    }
}
