package org.space.oss.psim

import org.space.oss.psim.spacecraft._
import org.space.oss.psim.time._

class PSimImpl extends PSim 
{
	val spacecraftService = new SpacecraftServiceImpl
    val timeService = new TimeServiceBase
    
	override def init(cfg: Config, numCraft: Int) {
	  spacecraftService.init(cfg,numCraft)
	}
	
	override def getSpacecraftService: SpacecraftService = spacecraftService

	def getTimeService: TimeService = timeService
	
	override def toString(): String = {
	  val buf = new StringBuilder
	  buf ++= 
	    "PSim {\n" ++=
	    ("  " + timeService.toString() + "\n") ++=
	    ("  " + spacecraftService.toString() + "\n") ++=
	    "}\n"
	  buf.toString
	}
}
