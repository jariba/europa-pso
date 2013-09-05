package org.space.oss.psim

import scala.actors.Actor

case class Ping(time: Long)
case class Pong(time: Long, from: Actor)
case class AfterDelay(delay: Long, source: Actor, target: Actor, msg: Any)
case class WorkItem(time: Long, source: Actor, target: Actor, msg: Any)
trait PSimEvent
case object Start
case object Stop

class PSimEventManager(psim: PSim) extends Actor 
{
	private var running = false
	private var maxTime_ = Long.MaxValue
    private var currentTime = 0L
    private var agenda: List[WorkItem] = List()
    private var allSimulants: List[Actor] = List()
    private var busySimulants: Set[Actor] = Set.empty
    var timeListeners = scala.collection.mutable.ListBuffer[(Long)=>Unit]()
    
    def maxTime:Long = maxTime_
    def maxTime_=(newMax:Long) = {
    	assert(!running)
    	maxTime_ = newMax
	}
    
    def getPSim: PSim = psim
    def add(sim: Simulant) {
    	allSimulants = sim :: allSimulants
    	sim.setManager(this)
    }
	
	def init() {
		psim.getSpacecraftService.getAllSpacecraft map {
		  sc => add(sc)
		}
	}
	
    def act() {
    	loop {
    	  if (running && busySimulants.isEmpty)
    		  advance()
    		
    	  // react doesn't return so it must be the last thing within our acting loop  
    	  reactToOneMessage()
    	}
    }	
    
    def advance() {
    	if (agenda.isEmpty && getCurrentTime > 0) {
    		println("** Agenda empty.  Clock exiting at time "+getCurrentTime+".")
    		this ! Stop
    		return
    	}
    	
    	if (agenda.head.time > maxTime) {
    		println("** Next event in agenda exceeds maxTime.  Clock exiting at time "+getCurrentTime+".")
    		this ! Stop
    		return    	  
    	}

    	setCurrentTime(agenda.head.time)

    	processCurrentEvents()
    	for (sim <- allSimulants)
    		sim ! Ping(currentTime)

    	busySimulants = Set.empty ++ allSimulants
    }    
    
    private def processCurrentEvents() {
      val todoNow = agenda.takeWhile(_.time <= getCurrentTime)

      agenda = agenda.drop(todoNow.length)

      for (item <- todoNow) {
        assert(item.time == getCurrentTime)
        //println("processing event:"+item)
        item.target ! item.msg
        //println("processed event:"+item)
      }
    }    
    
    def reactToOneMessage() {
    	react {
    		case AfterDelay(delay, source, target, msg) =>
    			val item = WorkItem(getCurrentTime + delay, source, target, msg)
    			agenda = insert(agenda, item)

    		case item: WorkItem =>
    			agenda = insert(agenda, item)
    			//println("New event from "+item.source+", t="+item.time)

    		case Pong(time, sim) =>
    			assert(time == getCurrentTime)
    			assert(busySimulants contains sim)
    			busySimulants -= sim
    			//println("Got Pong from "+sim+" busySimulants="+busySimulants)

    		case Start =>
    			assert(!running)
    			agenda = List.empty[WorkItem]
    			running = true
    			for (sim <- allSimulants)
    				sim ! Ping(currentTime)
    			busySimulants = Set.empty ++ allSimulants
    			//println("Simulation started at time:"+this.currentTime)

    		case Stop =>
    			assert(running)
    		    running = false
    			for (sim <- allSimulants)
    				sim ! Stop
    			//println("Simulation stopped at time:"+this.currentTime)
    			//exit()
    	}
    }   
    
    // TODO: use a sorted collection instead?
    private def insert(ag: List[WorkItem], item: WorkItem): List[WorkItem] = {
        if (ag.isEmpty || item.time < ag.head.time) item :: ag
        else ag.head :: insert(ag.tail, item)
    }
    
    // TODO use PSim Time Service instead
    def getCurrentTime = currentTime
    
    def setCurrentTime(t:Long) {
    	currentTime = t
   		println("Advancing to time "+currentTime)
    	for (l <- timeListeners)
    	  l(currentTime)
    }
}
