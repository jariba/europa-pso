package org.space.oss.psim.spacecraft;

import java.util.ArrayList;
import java.util.List;

import org.space.oss.psim.PSim;
import org.space.oss.psim.Spacecraft;

public class SpacecraftFactoryImpl implements SpacecraftFactory 
{
	@Override
	public List<Spacecraft> makeSpacecraft(PSim psim) 
	{
		List<Spacecraft> retval = new ArrayList<Spacecraft>();
		
		for (int i=0;i<1;i++) {
			Spacecraft sc = new SpacecraftImpl("SC-"+i,psim);
			sc.init();
			retval.add(sc);
		}
		
		return retval;
	}
}
