package org.space.oss.psim.spacecraft;

import org.space.oss.psim.PSim;
import org.space.oss.psim.Spacecraft;

public interface SpacecraftFactory 
{
	Spacecraft makeSpacecraft(String id, PSim psim);
}
