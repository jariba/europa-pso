package org.space.oss.psim.spacecraft;

import java.util.Collection;
import java.util.List;

import org.space.oss.psim.PSimEventGenerator;
import org.space.oss.psim.Spacecraft;

public interface Subsystem 
{
	public String getName();

	public Spacecraft getSpacecraft();

	public List<PSimEventGenerator> getEventGenerators();
	
	// Subsystems can be arranged hierarchically
	public Collection<Subsystem> getSubsystems();
	public Subsystem getSubsystem(String name);
}
