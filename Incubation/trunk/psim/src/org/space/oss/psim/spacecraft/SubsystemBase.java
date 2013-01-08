package org.space.oss.psim.spacecraft;

import org.space.oss.psim.Spacecraft;

public class SubsystemBase implements Subsystem 
{
	protected String name_;
	protected Spacecraft spacecraft_;

	public SubsystemBase(Spacecraft s, String n)
	{
		name_ = n;
		spacecraft_ = s;
	}
	
	@Override
	public String getName() { return name_; }
	
	@Override
	public Spacecraft getSpacecraft() { return spacecraft_; }
}
