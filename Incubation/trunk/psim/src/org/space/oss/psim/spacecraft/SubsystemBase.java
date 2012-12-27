package org.space.oss.psim.spacecraft;

public class SubsystemBase implements Subsystem 
{
	protected String name_;

	public SubsystemBase(String n)
	{
		name_ = n;
	}
	
	@Override
	public String getName() { return name_; }
}
