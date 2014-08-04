package org.space.oss.psim

trait PSimObservable {
	var observers:Set[PSimObserver] = Set.empty[PSimObserver]
	
	def addObserver(o:PSimObserver) { observers = observers + o }
	def removeObserver(o:PSimObserver) { observers = observers - o}
	def notifyEvent(eventType: Int, event:Any) { 
		for (o <- observers) o.handleEvent(eventType,event) 
	}
}

trait PSimObserver {
	def handleEvent(eventType: Int,event: Any)
}