package org.space.oss.psim

trait TimeService {
	def getCurrentTime: Long
	def setCurrentTime(t:Long): Unit
	def addObserver(o:(Long)=>Unit)
}