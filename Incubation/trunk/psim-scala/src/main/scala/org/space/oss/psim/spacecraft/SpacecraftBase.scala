package org.space.oss.psim.spacecraft

import org.space.oss.psim.Spacecraft
import org.space.oss.psim.PSimEvent
import org.space.oss.psim.PSimEventManager
import org.space.oss.psim.WorkItem
import org.space.oss.psim.Stop

case class DoXLink(time: Int, source: Spacecraft, target: Spacecraft) extends PSimEvent

class SpacecraftBase(id:String) extends Spacecraft 
{
	 override def getID = id
	 override def toString(): String = id
	
	 var eventMgr: PSimEventManager = null
	 var nextXlinkTime = -1
	 
	 def handleSimMessage(msg: Any) {
	   msg match {
	     case DoXLink(time, source,target) =>
	       println("Time:"+time+" "+this.toString +" received xlink request from "+source)
	   }
	 } 
	 
	 def handleNewTime(time: Int) {
		 nextEvents(time) map {
		   event =>
		     event match {
		       case DoXLink(xlinkTime,source,target) =>
		       		eventMgr ! WorkItem(xlinkTime,source,target,event)
		     }
		 }
		 
		 if (time > 500)
		   eventMgr ! Stop
	 }
	 
	 def nextEvents(time: Int): List[PSimEvent] = {
	   if (time >= nextXlinkTime) {
		   var target = eventMgr.getPSim.getSpacecraftService.getSpacecraftByID("SC-1").getOrElse(null)
		   val offset = (Math.random()*100).toInt
		   //println("offset:"+offset)
		   nextXlinkTime = time+offset
		   List(DoXLink(nextXlinkTime,this,target))
	   }
	   else {
	     List.empty[PSimEvent]
	   }
	 }	 
}