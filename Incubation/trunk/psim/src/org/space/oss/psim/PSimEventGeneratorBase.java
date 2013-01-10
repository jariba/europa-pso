package org.space.oss.psim;

public abstract class PSimEventGeneratorBase implements PSimEventGenerator 
{
	protected String name_;
	protected PSimEventManager manager_;
	
	public PSimEventGeneratorBase(String name)
	{
		name_ = name;
	}
	
	@Override
	public String getName() { return name_; }

	@Override
	public void setManager(PSimEventManager m) 
	{
		manager_ = m;
	}
}
