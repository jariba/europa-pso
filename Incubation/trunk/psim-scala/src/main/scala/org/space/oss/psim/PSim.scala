package org.space.oss.psim

trait PSim 
{
	def init(cfg: Config)
	def getSpacecraftService: SpacecraftService
	def getTimeService: TimeService
}