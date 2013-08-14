package org.space.oss.psim.spacecraft

import org.space.oss.psim.{Config,Spacecraft,SpacecraftService}

class SpacecraftServiceImpl extends SpacecraftService 
{
	var spacecraft: List[Spacecraft] = Nil
	
	override def init(cfg: Config) {
		spacecraft = (for (i <- 1 to 8) yield new SpacecraftBase("SC-"+i)) toList
	}
	
	override def toString(): String = spacecraft.toString()	
	 
	override def getSpacecraftByID(id: String): Option[Spacecraft] = { spacecraft.find(sc => id==sc.getID)}
	
	override def getAllSpacecraft() : List[Spacecraft] = { spacecraft }	 
}