package org.space.oss.psim.spacecraft;

import java.util.List;

import org.space.oss.psim.PSimEventGenerator;
import org.space.oss.psim.Spacecraft;

public interface Subsystem 
{
	public String getName();

	public Spacecraft getSpacecraft();

	public List<PSimEventGenerator> getEventGenerators();
}
