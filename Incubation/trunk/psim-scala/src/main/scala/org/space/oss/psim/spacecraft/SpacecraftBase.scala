package org.space.oss.psim.spacecraft

import org.space.oss.psim.Spacecraft
import org.space.oss.psim.PSimEvent
import org.space.oss.psim.PSimEventManager
import org.space.oss.psim.WorkItem
import org.space.oss.psim.Stop
import org.space.oss.psim.PSimObservable

case class DoXLink(time: Int, source: Spacecraft, target: Spacecraft) extends PSimEvent
case class XLinkResponse(time: Int, source: Spacecraft, target: Spacecraft) extends PSimEvent
case class ExecutedCommand(c:Any)

class SpacecraftBase(id:String) extends Spacecraft
{
	 override def getID = id
	 override def toString(): String = id
	 override def getCommandTrace:IndexedSeq[AnyRef] = commandTrace
	 
	 var eventMgr: PSimEventManager = null
	 var nextXlinkTime = -1
	 var commandTrace:Vector[AnyRef] = Vector.empty
	 
	 def handleSimMessage(msg: Any) {
	   msg match {
	     case DoXLink(time, source,target) =>
	       val cmd = "Time:"+time+" "+this.toString +" received xlink request from "+source
	       logCommand(cmd)
	       println(msg)
	       val responseDelay=5
	       val responseTime=time+responseDelay
	       eventMgr ! WorkItem(responseTime,this,source,XLinkResponse(responseTime,this,source))
	     case XLinkResponse(time, source,target) =>
	       val cmd = "Time:"+time+" "+this.toString +" received xlink response from "+source
	       logCommand(cmd)
	       println(msg)	       
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
	 }
	 
	 def nextEvents(time: Int): List[PSimEvent] = {
	   if (this.getID!="SC-1" && time>=nextXlinkTime) {
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
	 
	 def logCommand(c:AnyRef) {
	   commandTrace = commandTrace :+ c
	   this.notifyEvent(ExecutedCommand(c))
	 }
	 
	 override def handleStop() {
	   nextXlinkTime = -1
	 }
}