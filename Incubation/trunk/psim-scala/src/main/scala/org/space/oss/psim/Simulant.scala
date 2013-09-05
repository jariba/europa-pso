package org.space.oss.psim

import scala.actors.Actor
  
trait Simulant extends Actor {
      var eventMgr: PSimEventManager
      
      def setManager(mgr: PSimEventManager) { eventMgr = mgr }
      
      def handleSimMessage(msg: Any)
      def handleNewTime(time: Long)
      def nextEvents(time: Long): List[PSimEvent] 
      def act() {
        loop {
          react {
            case Ping(time) => {
              handleNewTime(time)
              eventMgr ! Pong(time, this)
            }
            case Stop =>
              handleStop()
            case msg => 
              handleSimMessage(msg)
          }
        }
      }
      
      def handleStop() {
        println(this+" stopped")
      }
      
      start()
}