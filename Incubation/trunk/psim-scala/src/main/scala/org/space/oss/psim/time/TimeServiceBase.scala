package org.space.oss.psim.time

import org.space.oss.psim.TimeService

class TimeServiceBase extends TimeService {
  var currentTime=0L
  var timeListeners = scala.collection.mutable.ListBuffer[(Long)=>Unit]()
  
  override def getCurrentTime = currentTime
  
  override def setCurrentTime(t:Long) {
    println("Advancing to time "+t)
    currentTime = t
    for (listenTo <- timeListeners)
    	listenTo(currentTime)    
  }
  
  override def addObserver(o:(Long)=>Unit) { timeListeners += o }
}