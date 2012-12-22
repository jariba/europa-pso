package org.space.oss.psim.spacecraft;

import org.space.oss.psim.PSim;
import org.space.oss.psim.Spacecraft;

public class SpacecraftFactoryImpl implements SpacecraftFactory {

	@Override
	public Spacecraft makeSpacecraft(String id, PSim psim) 
	{
		return new SpacecraftImpl(id,psim);
	}

}
