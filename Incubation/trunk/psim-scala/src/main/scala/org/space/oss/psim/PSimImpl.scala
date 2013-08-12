package org.space.oss.psim

import org.space.oss.psim.spacecraft._

class PSimImpl extends PSim 
{
	val spacecraftService = new SpacecraftServiceImpl
  
	override def init(cfg: Config) {
	  spacecraftService.init(cfg)
	}
	
	override def getSpacecraftService: SpacecraftService = spacecraftService

	override def toString(): String = {
	  val buf = new StringBuilder
	  buf ++= 
	    "PSim {\n" ++=
	    ("  " + spacecraftService.toString() + "\n") ++=
	    "}\n"
	  buf.toString
	}
}