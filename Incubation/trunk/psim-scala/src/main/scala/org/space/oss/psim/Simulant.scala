package org.space.oss.psim

import scala.actors.Actor
  
trait Simulant extends Actor {
      var eventMgr: PSimEventManager
      
      def setManager(mgr: PSimEventManager) { eventMgr = mgr }
      
      def handleSimMessage(msg: Any)
      def handleNewTime(time: Int)
      def nextEvents(time: Int): List[PSimEvent] 
      def act() {
        loop {
          react {
            case Ping(time) => {
              handleNewTime(time)
              eventMgr ! Pong(time, this)
            }
            case Stop =>
              println(this+" stopped")
            case msg => handleSimMessage(msg)
          }
        }
      }
      start()
}