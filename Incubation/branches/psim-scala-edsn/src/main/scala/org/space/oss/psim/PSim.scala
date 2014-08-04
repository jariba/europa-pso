package org.space.oss.psim

import java.text.SimpleDateFormat
import java.util.TimeZone

trait PSim 
{
	def init(cfg: Config)
	def getSpacecraftService: SpacecraftService
	def getTimeService: TimeService
}

object PSimUtil {
 	 val formatter:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
 	 formatter.setTimeZone(TimeZone.getTimeZone("GMT"))
 	 
 	 def setTimeZone(tz:TimeZone) = formatter.setTimeZone(tz)
	 def formatTime(t:Long) = formatter.format(t) 
	 def parseTime(timeStr: String) = formatter.parse(timeStr)
}