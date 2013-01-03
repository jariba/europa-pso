package org.space.oss.psim;


public class CommandDescriptor 
{
	protected String category_;
    protected String name_;
    protected CommandArg args_[];
	
	public CommandDescriptor(String category, String name, CommandArg args[])
	{
		category_ = category;
		name_ = name;
		args_ = args;
	}
	
	public String getCategory() { return category_; }
	public String getName() { return name_; }
	public CommandArg[] getArgs() { return args_; }
	
	public String toString() { return getName(); }
}
