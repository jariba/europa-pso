package org.space.oss.psim;


public class CommandDescriptor 
{
    protected String name;
    protected String code;
	protected String subsystem;
    protected CommandArg parameters[];
	
	public CommandDescriptor(String category, String name, CommandArg args[])
	{
		this.subsystem = category;
		this.name = name;
		this.parameters = args;
	}
	
	public String getCategory() { return subsystem; }
	public String getName() { return name; }
	public CommandArg[] getArgs() { return parameters; }
	
	public String toString() { return getName(); }
}
