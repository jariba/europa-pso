package org.space.oss.psim.spacecraft

import org.space.oss.psim.{Config,Spacecraft,SpacecraftService}

class SpacecraftServiceImpl extends SpacecraftService 
{
	var spacecraft: Seq[Spacecraft] = Nil
	
	override def init(cfg: Config) {
		spacecraft = for (i <- 1 to 8) yield new SpacecraftBase("SC-"+i)
	}
	
	 override def toString(): String = spacecraft.toString()	
}