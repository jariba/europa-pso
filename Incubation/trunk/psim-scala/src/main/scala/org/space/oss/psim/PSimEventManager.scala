package org.space.oss.psim

import scala.actors.Actor

case class Ping(time: Long)
case class Pong(time: Long, from: Actor)
case class AfterDelay(delay: Long, source: Actor, target: Actor, msg: Any)
case class WorkItem(time: Long, source: Actor, target: Actor, msg: Any)
trait PSimEvent
case object Start
case object Stop

class PSimEventManager(sim: PSim) extends Actor 
{
    val psim = sim
    var startTime=0L;
	private var running = false
	private var maxTime_ = Long.MaxValue
    private var agenda: List[WorkItem] = List()
    private var allSimulants: List[Actor] = List()
    private var busySimulants: Set[Actor] = Set.empty
    
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
		for (sc <- psim.getSpacecraftService.getAllSpacecraft) 
		  add(sc)

		psim.getTimeService.addObserver(handleNewTime)  
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
    	if (agenda.isEmpty) {
    	  if (getCurrentTime > startTime) {
    		println("** Agenda empty.  Clock exiting at time "+getCurrentTime+".")
    		this ! Stop
    	  }
    	  return
    	}
    	
    	if (agenda.head.time > maxTime) {
    		println("** Next event in agenda exceeds maxTime.  Clock exiting at time "+getCurrentTime+".")
    		this ! Stop
    		return    	  
    	}

    	
    	setCurrentTime(agenda.head.time) // eventually triggers a call to handleNewTime below
    }   
    
    private def handleNewTime(t:Long) {
      if (!running)
        return
        
      processCurrentEvents(t)
      for (sim <- allSimulants)
    	  sim ! Ping(t)

   	  busySimulants = Set.empty ++ allSimulants      
    }
    
    private def processCurrentEvents(t:Long) {
      val todoNow = agenda.takeWhile(_.time <= t)

      agenda = agenda.drop(todoNow.length)

      for (item <- todoNow) {
        assert(item.time == t)
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
    			println("New event from "+item.source+", t="+item.time)

    		case item: WorkItem =>
    			agenda = insert(agenda, item)
    			println("New event from "+item.source+", t="+item.time)

    		case Pong(time, sim) =>
    			assert(time == getCurrentTime)
    			assert(busySimulants contains sim)
    			busySimulants -= sim
    			println("Got Pong from "+sim+" busySimulants="+busySimulants)

    		case Start =>
    			assert(!running)
    			agenda = List.empty[WorkItem]
    			running = true
    			for (sim <- allSimulants)
    				sim ! Ping(getCurrentTime)
    			busySimulants = Set.empty ++ allSimulants
    			startTime = getCurrentTime
    			println("Simulation started at time:"+getCurrentTime)

    		case Stop =>
    			assert(running)
    		    running = false
    			for (sim <- allSimulants)
    				sim ! Stop
    			println("Simulation stopped at time:"+getCurrentTime)
    			//exit()
    	}
    }   
    
    // TODO: use a sorted collection instead?
    private def insert(ag: List[WorkItem], item: WorkItem): List[WorkItem] = {
        if (ag.isEmpty || item.time < ag.head.time) item :: ag
        else ag.head :: insert(ag.tail, item)
    }
    
    def getCurrentTime = psim.getTimeService.getCurrentTime
    
    def setCurrentTime(t:Long) { psim.getTimeService.setCurrentTime(t) }
}
