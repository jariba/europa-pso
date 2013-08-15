package org.space.oss.psim

trait Spacecraft 
	extends Simulant 
	with PSimObservable {
  
	def getID: String
	def getCommandTrace: IndexedSeq[AnyRef]
}