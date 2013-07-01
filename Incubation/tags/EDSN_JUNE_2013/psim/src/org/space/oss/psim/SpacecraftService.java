package org.space.oss.psim;

import java.util.Collection;

public interface SpacecraftService extends PSimService
{
	public Spacecraft getSpacecraftByID(String spacecraftID);

	public Collection<Spacecraft> getAllSpacecraft();
}
