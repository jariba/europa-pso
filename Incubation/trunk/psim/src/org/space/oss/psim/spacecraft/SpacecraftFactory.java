package org.space.oss.psim.spacecraft;

import java.util.List;

import org.space.oss.psim.PSim;
import org.space.oss.psim.Spacecraft;

public interface SpacecraftFactory 
{
	List<Spacecraft> makeSpacecraft(PSim psim);
}
