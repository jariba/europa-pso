package org.space.oss.psim;


public class CommandDescriptor 
{
    protected String name;
	protected String category;
    protected CommandArg args[];
	
	public CommandDescriptor(String category, String name, CommandArg args[])
	{
		this.category = category;
		this.name = name;
		this.args = args;
	}
	
	public String getCategory() { return category; }
	public String getName() { return name; }
	public CommandArg[] getArgs() { return args; }
	
	public String toString() { return getName(); }
}
