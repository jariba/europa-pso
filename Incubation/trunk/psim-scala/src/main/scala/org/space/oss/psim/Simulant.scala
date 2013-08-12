package org.space.oss.psim

import scala.actors.Actor
  
trait Simulant extends Actor {
      val eventMgr: PSimEventManager
      def handleSimMessage(msg: Any)
      def simStarting() { }
      def act() {
        loop {
          react {
            case Ping(time) =>
              if (time == 1) simStarting()
              eventMgr ! Pong(time, this)
            case msg => handleSimMessage(msg)
          }
        }
      }
      start()
}