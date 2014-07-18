package org.space.oss.psim

trait SpacecraftService 
{
	def init(cfg: Config, numSpacecraft: Int)
	
	def getSpacecraftByID(id: String): Option[Spacecraft]
	
	def getAllSpacecraft() : List[Spacecraft]
}
