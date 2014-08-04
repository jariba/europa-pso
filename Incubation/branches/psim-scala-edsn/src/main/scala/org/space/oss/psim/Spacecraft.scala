package org.space.oss.psim

trait Spacecraft 
	extends Simulant 
	with PSimObservable {
  
	def getID: String
	def getCommandTrace: IndexedSeq[AnyRef]
}

object Spacecraft {
  val EXECUTED_COMMAND=1
}