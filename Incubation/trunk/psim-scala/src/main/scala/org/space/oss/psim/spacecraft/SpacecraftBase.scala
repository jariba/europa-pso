package org.space.oss.psim.spacecraft

import org.space.oss.psim.Spacecraft
import org.space.oss.psim.PSimEvent
import org.space.oss.psim.PSimEventManager
import org.space.oss.psim.WorkItem
import org.space.oss.psim.Stop
import org.space.oss.psim.PSimObservable
import java.text.SimpleDateFormat

case class DoXLink(time: Long, source: Spacecraft, target: Spacecraft) extends PSimEvent
case class XLinkResponse(time: Long, source: Spacecraft, target: Spacecraft) extends PSimEvent
case class ExecutedCommand(c:Any)

class SpacecraftBase(id:String) extends Spacecraft
{
	 override def getID = id
	 override def toString(): String = id
	 override def getCommandTrace:IndexedSeq[AnyRef] = commandTrace
	 
	 var eventMgr: PSimEventManager = null
	 var nextXlinkTime = -1L
	 var commandTrace:Vector[AnyRef] = Vector.empty
	 
	 def handleSimMessage(msg: Any) {
	   msg match {
	     case DoXLink(time, source,target) =>
	       val cmd = "Time:"+asTimeStr(time)+" "+this.toString +" received xlink request from "+source
	       logCommand(cmd)
	       val responseDelay=asTime(2)
	       val responseTime=time+responseDelay
	       eventMgr ! WorkItem(responseTime,this,source,XLinkResponse(responseTime,this,source))
	       
	     case XLinkResponse(time, source,target) =>
	       val cmd = "Time:"+asTimeStr(time)+" "+this.toString +" received xlink response from "+source
	       logCommand(cmd)
	   }
	 } 
	 
	 def handleNewTime(time: Long) {
		 nextEvents(time) map {
		   event =>
		     event match {
		       case DoXLink(xlinkTime,source,target) =>
		       		eventMgr ! WorkItem(xlinkTime,source,target,event)
		     }
		 }		 
	 }
	 
	 def nextEvents(time: Long): List[PSimEvent] = {
	   if (this.getID!="SC-1" && time>=nextXlinkTime) {
		   var target = eventMgr.getPSim.getSpacecraftService.getSpacecraftByID("SC-1").getOrElse(null)
		   val offset = asTime(1)+(Math.random()*asTime(10)).toLong
		   nextXlinkTime = time+offset
		   List(DoXLink(nextXlinkTime,this,target))
	   }
	   else {
	     List.empty[PSimEvent]
	   }
	 }
	 
	 def asTime(t:Long) = t*1000
	 
	 val formatter:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
	 def asTimeStr(t:Long) = formatter.format(t)
	 
	 def logCommand(c:AnyRef) {
	   commandTrace = commandTrace :+ c
	   println(c)
	   this.notifyEvent(ExecutedCommand(c))
	 }
	 
	 override def handleStop() {
	   super.handleStop()
	   nextXlinkTime = -1
	 }
}