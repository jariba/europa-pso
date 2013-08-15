package org.space.oss.psim

import scala.actors.Actor

case class Ping(time: Int)
case class Pong(time: Int, from: Actor)
case class AfterDelay(delay: Int, source: Actor, target: Actor, msg: Any)
case class WorkItem(time: Int, source: Actor, target: Actor, msg: Any)
trait PSimEvent
case object Start
case object Stop

class PSimEventManager(psim: PSim) extends Actor 
{
	private var running = false
	private var maxTime_ = Int.MaxValue
    private var currentTime = 0
    private var agenda: List[WorkItem] = List()
    private var allSimulants: List[Actor] = List()
    private var busySimulants: Set[Actor] = Set.empty
    
    def maxTime:Int = maxTime_
    def maxTime_=(newMax:Int) = {
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
    		  
    	  reactToOneMessage()
    	}
    }	
    
    def advance() {
    	if (agenda.isEmpty && currentTime > 0) {
    		println("** Agenda empty.  Clock exiting at time "+currentTime+".")
    		this ! Stop
    		return
    	}
    	
    	if (agenda.head.time > maxTime) {
    		println("** Next event in agenda exceeds maxTime.  Clock exiting at time "+currentTime+".")
    		this ! Stop
    		return    	  
    	}

    	currentTime = agenda.head.time
    			println("Advancing to time "+currentTime)

    	processCurrentEvents()
    	for (sim <- allSimulants)
    		sim ! Ping(currentTime)

    	busySimulants = Set.empty ++ allSimulants
    }    
    
    private def processCurrentEvents() {
    	val todoNow = agenda.takeWhile(_.time <= currentTime)

    	agenda = agenda.drop(todoNow.length)

    	for (WorkItem(time, source, target, msg) <- todoNow) {
    		assert(time == currentTime)
    		target ! msg
    	}
    }    
    
    def reactToOneMessage() {
    	react {
    		case AfterDelay(delay, source, target, msg) =>
    			val item = WorkItem(currentTime + delay, source, target, msg)
    			agenda = insert(agenda, item)

    		case item: WorkItem =>
    			agenda = insert(agenda, item)
    			//println("New event from "+item.source+", t="+item.time)

    		case Pong(time, sim) =>
    			assert(time == currentTime)
    			assert(busySimulants contains sim)
    			busySimulants -= sim
    			println("Got Pong from "+sim+" busySimulants="+busySimulants)

    		case Start =>
    			assert(!running)
    			agenda = List.empty[WorkItem]
    			running = true
    			for (sim <- allSimulants)
    				sim ! Ping(currentTime)
    			busySimulants = Set.empty ++ allSimulants
    			println("Simulation started at time:"+this.currentTime)

    		case Stop =>
    			assert(running)
    		    running = false
    			for (sim <- allSimulants)
    				sim ! Stop
    			println("Simulation stopped at time:"+this.currentTime)
    			//exit()
    	}
    }   
    
    // TODO: use a sorted collection instead?
    private def insert(ag: List[WorkItem], item: WorkItem): List[WorkItem] = {
        if (ag.isEmpty || item.time < ag.head.time) item :: ag
        else ag.head :: insert(ag.tail, item)
    }
}