package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr

abstract class PostPropagationCallback(ce: ConstraintEngine) { 
  ce.addCallback(this)
  def apply: Boolean
}

trait ConstraintEngineListener { 
  def notifyPropagationCommenced(): Unit = {}

  def notifyPropagationCompleted(): Unit = {}

  def notifyPropagationPreempted(): Unit = {}

  def notifyAdded(c: Constraint): Unit = {}

  def notifyActivated(c: Constraint): Unit = {}

  def notifyDeactivated(v: ConstrainedVariable): Unit = {}

  def notifyActivated(v: ConstrainedVariable): Unit = {}

  def notifyDeactivated(c: Constraint): Unit = {}

  def notifyRemoved(c: Constraint): Unit = {}

  def notifyExecuted(c: Constraint): Unit = {}

  def notifyAdded(v: ConstrainedVariable): Unit = {}

  def notifyRemoved(v: ConstrainedVariable): Unit = {}

  def notifyChanged(v: ConstrainedVariable, change: DomainListener.ChangeType): Unit = {}

  def notifyViolationAdded(c: Constraint): Unit = {}

  def notifyViolationRemoved(c: Constraint): Unit = {}

  def setConstraintEngine(ce: ConstraintEngine): Unit = { 
    engine = ce
    ce.add(this)
  }

  var engine: ConstraintEngine;
}

trait ViolationManager { 
  def getMaxViolationsAllowed: Int
  def setMaxViolationsAllowed(i: Int): Unit

  def getViolation: Double 
  def getViolationExpl: List[String] 
  def getAllViolations: List[Constraint] 
  
  def handleEmpty(v: ConstrainedVariable): Boolean 
  def handleRelax(v: ConstrainedVariable): Boolean 
  def canContinuePropagation: Boolean 

  def isViolated(c: Constraint): Boolean
  def addViolatedConstraint(c: Constraint): Unit
  def removeViolatedConstraint(c: Constraint): Unit

  def isEmpty(v: ConstrainedVariable): Boolean
  def addEmptyVariable(v: ConstrainedVariable): Unit
  def getEmptyVariables: Set[ConstrainedVariable]
  def clearEmptyVariables: Unit
  def relaxEmptyVariables: Unit

}

class DefaultViolationManager(var maxViolationsAllowed: Int,
                              val ce: ConstraintEngine) extends ViolationManager { 
  def getMaxViolationsAllowed: Int = maxViolationsAllowed
  def setMaxViolationsAllowed(i: Int): Unit = maxViolationsAllowed = i

  def getViolation: Double = violatedConstraints.map(_.getViolation).reduceLeft(_ + _)
  def getViolationExpl: List[String] = violatedConstraints.map(_.getViolationExpl).toList
  def getAllViolations: List[Constraint] = violatedConstraints.toList
  
  def handleEmpty(v: ConstrainedVariable): Boolean = { 
    if(violatedConstraints.size >= maxViolationsAllowed) return false
    if(v.propagatingConstraint != null) v.propagatingConstraint.notifyViolated
    return true
  }
  def handleRelax(v: ConstrainedVariable): Boolean = { 
    if(violatedConstraints.size == 0) return true;
    v.constraints.foreach((c) => if(isViolated(c._1)) c._1.notifyNoLongerViolated)
    return true
  }
  def canContinuePropagation: Boolean = violatedConstraints.size < maxViolationsAllowed

  def isViolated(c: Constraint): Boolean = violatedConstraints.contains(c)
  def addViolatedConstraint(c: Constraint): Unit = { 
    violatedConstraints = violatedConstraints + c
    c.deactivate
    ce.notifyViolationAdded(c)
  }
  def removeViolatedConstraint(c: Constraint): Unit = { 
    checkError(() => isViolated(c), "Tried to remove a non-violated constraint")
    c.undoDeactivation
    violatedConstraints = violatedConstraints - c
    ce.notifyViolationRemoved(c)
  }

  def isEmpty(v: ConstrainedVariable): Boolean = emptyVariables.contains(v)
  def addEmptyVariable(v: ConstrainedVariable): Unit = emptyVariables = emptyVariables + v
  def getEmptyVariables: Set[ConstrainedVariable] = emptyVariables
  def clearEmptyVariables: Unit = emptyVariables = Set()
  def relaxEmptyVariables: Unit = {
    while(!emptyVariables.isEmpty) { 
//      if(!emptyVariables.head.isDiscarded) emptyVariables.head.relax
      emptyVariables.head.relax
      emptyVariables = emptyVariables.tail
    }
  }


  var violatedConstraints: Set[Constraint] = Set()
  var emptyVariables: Set[ConstrainedVariable] = Set()
  var relaxing: Boolean = false
  
}

object ConstraintEngine { 
  object State extends Enumeration { 
    type State = Value
    val PROVEN_INCONSISTENT, CONSTRAINT_CONSISTENT, PENDING = Value
  }
  object Event extends Enumeration { 
    type Event = Value
    val UPPER_BOUND_DECREASED, /**< If the upper bound of an interval domain is reduced. */
    LOWER_BOUND_INCREASED, /**< If the lower bound of an interval domain is increased. */
    BOUNDS_RESTRICTED, /**< Both upper and lower are decreased and increased respectively. */
    VALUE_REMOVED, /**< A restriction to an enumerated domain. */
    RESTRICT_TO_SINGLETON, /**< A restriction of the domain to a singleton value through inference. */
    SET_TO_SINGLETON, /**< Special case restriction when the domain is set to a singleton i.e. instantiated. */
    RESET, /**< Special case of an external relaxation. */
    RELAXED, /**< Inferred relaxtion to the domain is indicated by this type of change. */
    CLOSED, /**< If a dynamic domain is closed this event will be generated. */
    OPENED, /**< If a closed domain is re-opened this event will be generated. */
    EMPTIED, /**< If a domain is emptied, indicating an inconsistency, then this event is generated. */
    PROPAGATION_COMMENCED, /**< Starting propagation. */
    PROPAGATION_COMPLETED, /**< Completed propagation. */
    PROPAGATION_PREEMPTED, /**< Propagation pre-empted and incomplete. */
    CONSTRAINT_ADDED, /**< A constraint was created. */
    CONSTRAINT_REMOVED, /**< A constraint was removed. */
    CONSTRAINT_EXECUTED, /**< A constraint was used during propagation. */
    VARIABLE_ADDED, /**< A variable was created. */
    VARIABLE_REMOVED, /**< A variable was removed. */
    LAST_EVENT /**< Use only for EVENT_COUNT. @see EVENT_COUNT */
    = Value
  }
}

class ConstraintEngine(schema: CESchema) { 
  def purge: Unit = { 
    purged = true
    checkError(() => Entity.isPurging || constraints.isEmpty, "")
    while(!constraints.isEmpty) constraints.head.discard
    checkError(() => Entity.isPurging || variables.isEmpty, "")
    while(!variables.isEmpty) variables.head.discard
    while(!propagators.isEmpty) propagators.head.discard
  }
  def provenInconsistent: Boolean = hasEmptyVariables
  def constraintConsistent: Boolean = if(provenInconsistent) false else getNextPropagator.isEmpty
  def pending: Boolean = if(!dirty) false else relaxed.map(_.hasActiveConstraint).reduceLeft(_ || _) || (!provenInconsistent && !constraintConsistent)
  def isPropagating: Boolean = propInProgress
  
  def propagate: Boolean = { 
    var result = true
    var done = false
    while(!done) { 
      result = doPropagate
      if(!result && canContinuePropagation) { 
        relaxingViolation = true
        violationMgr.relaxEmptyVariables
      }
      else { 
        done = true
      }
    }
    return result
  }
  def canContinuePropagation: Boolean = violationMgr.canContinuePropagation
  
  // def getVariable(index: Int): Option[ConstrainedVariable] = ;
  // def getIndex(v: ConstrainedVariable): Int = 0
  
  // def getConstraint(index: Int): Constraint = null
  // def getIndex(c: Constraint): Int = 0
  
  def getPropagatorByName(name: LabelStr): Option[Propagator] = propagatorsByName.get(name)
  def setAutoPropagation(b: Boolean): Unit = { 
    if(b != autoPropagate) { 
      autoPropagate = b
      if(shouldAutoPropagate) propagate
    }
  }
  def getAutoPropagation: Boolean = autoPropagate
  def shouldAutoPropagate: Boolean = autoPropagate && !propInProgress

  def setAllowViolations(b: Boolean): Unit = {
    violationMgr.setMaxViolationsAllowed(if(b) java.lang.Integer.MAX_VALUE else 0)
  }
  def getAllowViolations: Boolean = violationMgr.getMaxViolationsAllowed > 0
  def getViolation: Double = violationMgr.getViolation
  def getViolationExpl: List[String] = violationMgr.getViolationExpl
  def getAllViolations: List[Constraint] = violationMgr.getAllViolations
  def isViolated(c: Constraint): Boolean = violationMgr.isViolated(c)
  def isRelaxed: Boolean = !relaxed.isEmpty

  def getSchema: CESchema = schema
  
  def createVariableByType(typeName: LabelStr, internal: Boolean = false, 
                           canBeSpecified: Boolean = true,
                           name: LabelStr = ConstrainedVariable.NO_NAME,
                           parent: Option[Entity] = None,
                           index: Int = ConstrainedVariable.NO_INDEX): ConstrainedVariable = { 
    val dt = schema.getDataType(typeName)
    checkError(() => !dt.isEmpty, "Unknown data type: ", typeName)
    return createVariableWithDomain(typeName, dt.get.baseDomain, internal, canBeSpecified, name,
                                    parent, index)
  }

  def createVariableWithDomain(typeName: LabelStr, baseDomain: Domain, internal: Boolean = false,
                               canBeSpecified: Boolean = true, 
                               name: LabelStr = ConstrainedVariable.NO_NAME,
                               parent: Option[Entity] = None, 
                               index: Int = ConstrainedVariable.NO_INDEX): ConstrainedVariable = { 
    val dt = schema.getDataType(typeName)
    checkError(() => !dt.isEmpty, "Unknown data type: ", typeName)
    return dt.get.createVariable(this, baseDomain, internal, canBeSpecified, name, parent, index)
  }
  def createConstraint(name: LabelStr, scope: Vector[ConstrainedVariable], 
                       violationExpl: String = ""): Constraint = { 
    val factory = schema.getConstraintType(name)
    checkError(() => !factory.isEmpty, "Unknown constraint type: ", name)
    val retval = factory.get.createConstraint(this, scope, violationExpl)
    if(shouldAutoPropagate) propagate
    return retval
  }
  def deleteConstraint(c: Constraint): Unit = { 
    c.discard
    if(shouldAutoPropagate) propagate
  }
  
  def createValue(typeName: LabelStr, value: String): Option[Double] = { 
    schema.getDataType(typeName).getOrElse(DataType.NOTHING).createValue(value)
  }
  def addCallback(callback: PostPropagationCallback): Unit = callbacks = callback :: callbacks
  def removeCallback(callback: PostPropagationCallback): Unit = callbacks = callbacks.filterNot(_ == callback)
  
  def allocateVariableListener(v: ConstrainedVariable): DomainListener = new VariableChangeListener(v, this)

  def add(c: Constraint, propagatorName: LabelStr): Unit = { 
    checkError(() => !constraints.contains(c), "Attempted to add ", c.name, " twice")
    val possProp: Option[Propagator] = propagatorsByName.get(propagatorName)
    possProp match { 
      case Some(p) => p.addConstraint(c); c.setPropagator(p)
      case None => checkError(ALWAYS_FAIL, "No propagator named ", propagatorName)
    }
    constraints = constraints + c
    if(c.isRedundant) redundantConstraints = redundantConstraints + c
    listeners.foreach(_.notifyAdded(c))
  }
  def remove(c: Constraint): Unit = { 
    checkError(() => constraints.contains(c), "Attempted to remove ", c.name, " without adding it.")
    checkError(() => propInProgress, "Can't remove a constraint during propagation")
    constraints = constraints - c
    redundantConstraints = redundantConstraints - c
    if(!Entity.isPurging) { 
      c.propagator.get.removeConstraint(c)
      if(c.isActive) c.getModifiedVariables.filter((v: ConstrainedVariable) => (v.isDiscarded && v.lastRelaxed < cycleCount)).foreach(_.relax)
    }
  }
  
  private def publish(message: (ConstraintEngineListener) => Unit): Unit = { 
    dirty = true
    listeners.foreach(message(_))
  }

  def notify(source: ConstrainedVariable, change: DomainListener.ChangeType): Unit = { 
    checkError(() => !Entity.isPurging, "Tried to handle a notification while purging")
    dirty = true
    if(source.isActive) { 
      change match { 
        case DomainListener.EMPTIED => handleEmpty(source)
        case DomainListener.RELAXED | DomainListener.OPENED => handleRelax(source)
        case _ => handleRestrict(source)
      }
    }
    if(change != DomainListener.EMPTIED)
      source.constraints.filter((c: (Constraint, Int)) => (c._1.isActive && !(c._1.isDiscarded) && !(c._1.canIgnore(source, c._2, change))))
    .foreach((c: (Constraint, Int)) => c._1.propagator.get.handleNotification(source, c._2, c._1, change))
    publish(_.notifyChanged(source, change))
  }

  def handleEmpty(v: ConstrainedVariable): Unit = { 
    checkError(v.getCurrentDomain.isEmpty, "Variable ", v.toLongString, " isn't empty")
    violationMgr.addEmptyVariable(v)
    violationMgr.handleEmpty(v)
  }
  def handleRelax(v: ConstrainedVariable): Unit = { 
    def relaxLoop(v: ConstrainedVariable): Unit = { 
      for(c: (Constraint, Int) <- v.constraints) { 
        for(variable <- c._1.getModifiedVariables(v)) { 
          if(variable.lastRelaxed < cycleCount) { 
            variable.updateLastRelaxed(cycleCount)
            variable.relax
            if(!relaxingViolation) violationMgr.handleRelax(variable)
          }
        }
      }
    }

    checkError(!propInProgress, "Can't relax variables during propagation")
    if(!relaxing) { 
      if(!relaxingViolation) violationMgr.handleRelax(v)
      if(relaxed.isEmpty)
        incrementCycle
      v.updateLastRelaxed(cycleCount)
      relaxed = relaxed + v
      if(hasEmptyVariables && !(getViolationManager.isEmpty(v)))
        getViolationManager.relaxEmptyVariables
      relaxing = true
      relaxLoop(v)
      relaxing = false
    }
  }
  def handleRestrict(v: ConstrainedVariable): Unit = { 
    relaxed = relaxed - v
  }

  def add(p: Propagator): Unit = { 
    propagatorsByName.get(p.name) match { 
      case Some(o) => {propagators = propagators.filterNot(_ == p)
                       o.discard
                       propagatorsByName = propagatorsByName - p.name}
      case None => 
    }
    propagators = p :: propagators
    propagatorsByName = propagatorsByName + ((p.name, p))
  }
  
  def notifyDeactivated(c: Constraint): Unit = {
    checkError(!Entity.isPurging, "Purging!")
    checkError(!c.isActive, "Handling deactivation notification on active constraint")
    c.propagator.get.handleConstraintDeactivated(c)
    publish(_.notifyDeactivated(c))
  }

  def notifyActivated(c: Constraint): Unit = { 
    checkError(!Entity.isPurging, "Purging!")
    checkError(c.isActive, "Handling activation notification on inactive constraint")
    c.propagator.get.handleConstraintActivated(c)
    publish(_.notifyActivated(c))
    
  }

  def notifyRedundant(c: Constraint): Unit = { 
    if(c.isActive) { 
      redundantConstraints = redundantConstraints + c
      notifyActivated(c)
    }
  }

  def notifyDeactivated(v: ConstrainedVariable): Unit = { 
    checkError(!Entity.isPurging, "Purging!")
    checkError(!v.isActive, "Received deactivation notice on an active variable")
    
    propagators.foreach(_.handleVariableDeactivated(v))
    publish(_.notifyDeactivated(v))
  }

  def notifyActivated(v: ConstrainedVariable): Unit = { 
    checkError(!Entity.isPurging, "Purging!")
    checkError(v.isActive, "Received activation notice on an inactive variable")
    
    propagators.foreach(_.handleVariableActivated(v))
    publish(_.notifyActivated(v))
  }

  def execute(c: Constraint): Unit = { 
    def allActiveVariables(vars: Seq[ConstrainedVariable]): Boolean = vars.map(_.isActive).reduceLeft(_&&_)
    checkError(!provenInconsistent, "Can't execute a constraint while proven inconsistent")
    checkError(c.isActive, "Can't execute an inactive constraint")
    checkError(c.isValid, "Can't execute an invalid constraint")
    checkError(propInProgress, "Have to be propagating")
    checkError(allActiveVariables(c.getScope), "All variables must be active")
    publish(_.notifyExecuted(c))
    c.execute
  }

  def execute(c: Constraint, v: ConstrainedVariable, argIndex: Int, 
              change: DomainListener.ChangeType): Unit = { 
    def allActiveVariables(vars: Seq[ConstrainedVariable]): Boolean = vars.map(_.isActive).reduceLeft(_&&_)
    checkError(!provenInconsistent, "Can't execute a constraint while proven inconsistent")
    checkError(c.isActive, "Can't execute an inactive constraint")
    checkError(c.isValid, "Can't execute an invalid constraint")
    checkError(propInProgress, "Have to be propagating")
    checkError(allActiveVariables(c.getScope), "All variables must be active")
    publish(_.notifyExecuted(c))
    c.execute(v, argIndex, change)
  }

  def add(v: ConstrainedVariable): Unit = { 
    checkError(!variables.contains(v), "Tried to add ", v, " twice.")
    variables = variables + v
    publish(_.notifyAdded(v))
  }

  def remove(v: ConstrainedVariable): Unit = {
    checkError(!propInProgress, "Can't remove variables during propagation")
    checkError(variables.contains(v), "Can't remove a variable that hasn't been added")
    variables = variables - v
    relaxed = relaxed - v
    if(!Entity.isPurging) { 
      if(getViolationManager.isEmpty(v)) clearEmptyVariables
      publish(_.notifyRemoved(v))
    }
  }

  def add(l: ConstraintEngineListener): Unit = { 
    checkError(!Entity.isPurging, "Purging!")
    checkError(listeners.find(_ == l).isEmpty, "Tried to add a listener twice")
    listeners = l :: listeners
  }

  def remove(l: ConstraintEngineListener): Unit = { 
    checkError(!listeners.find(_ == l).isEmpty, "Tried to remove a listener twice")
    if(!deleted)
      listeners = listeners.filter(_ == l)
  }

  def getNextPropagator: Option[Propagator] = propagators.find((p: Propagator) => p.isEnabled && p.updateRequired)
  def incrementCycle: Unit = { 
    cycleCount += 1
    if(!relaxed.isEmpty) { 
      checkError(!propInProgress, "Not sure how this happened")
      mostRecentRepropagation = cycleCount
    }
  }

  def hasEmptyVariables: Boolean = !getViolationManager.getEmptyVariables.isEmpty
  def clearEmptyVariables: Unit = getViolationManager.clearEmptyVariables
  def doPropagate: Boolean = { 
    checkError(!Entity.isPurging, "Purging!")
    checkError(!propInProgress, "Recursive call to propagation")
    if(dirty) { 
      if(hasEmptyVariables) violationMgr.relaxEmptyVariables
      if(hasEmptyVariables) return false
      relaxed = Set()
      var started = false
      var continueProp = true
      while(continueProp) { 
        incrementCycle
        propInProgress = true
        var activePropagator = getNextPropagator
        while(!activePropagator.isEmpty) { 
          if(!started) { 
            started = true
            publish(_.notifyPropagationCommenced)
          }
          activePropagator.get.execute
          activePropagator = getNextPropagator
        }
        continueProp = false
        propInProgress = false
        incrementCycle
        val oldProp = autoPropagate
        autoPropagate = false
        continueProp = callbacks.map(_.apply).reduceLeft(_||_)
        autoPropagate = oldProp
      }
      if(constraintConsistent && started) { 
        publish(_.notifyPropagationCompleted)
        dirty = false
        processRedundantConstraints
      }
      else if(!constraintConsistent && started) { 
        publish(_.notifyPropagationPreempted)
      }
      relaxed = Set()
    }
    return true
  }
  
  def processRedundantConstraints: Unit = { 
    checkError(constraintConsistent, "Can only process when fully propagated")
    for(c <- redundantConstraints) { 
      checkError(c.isRedundant, "Non-redundant constraint")
      c.deactivate
    }
    redundantConstraints = Set()
  }
  
  def notifyViolationAdded(c: Constraint): Unit = publish(_.notifyViolationAdded(c))
  def notifyViolationRemoved(c: Constraint): Unit = publish(_.notifyViolationRemoved(c))

  def dumpPropagatorState(propagators: List[Propagator]): String = "" //needs impl
  

  def getViolationManager: ViolationManager = violationMgr


  var cycleCount: Int = 0
  var variables: Set[ConstrainedVariable] = Set()
  var constraints: Set[Constraint] = Set()
  var propagators: List[Propagator] = List()
  var propagatorsByName: Map[LabelStr, Propagator] = Map()
  var relaxing: Boolean = false
  var relaxingViolation: Boolean = false
  var relaxed: Set[ConstrainedVariable] = Set()
  var propInProgress: Boolean = false
  var deleted: Boolean = false
  var purged: Boolean = false
  var dirty: Boolean = false
  var mostRecentRepropagation: Int = 0
  var listeners: List[ConstraintEngineListener] = List()
  var redundantConstraints: Set[Constraint] = Set()
  var violationMgr: ViolationManager = null
  var autoPropagate: Boolean = true
  var callbacks: List[PostPropagationCallback] = List()
}
