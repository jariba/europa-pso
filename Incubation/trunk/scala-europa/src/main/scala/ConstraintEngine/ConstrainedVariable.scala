package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr
import scala.collection.immutable.Set

trait ConstrainedVariableListener { 
  def notifyDiscard: Unit
  def notifyConstraintAdded(constr: Constraint, argIndex: Int): Unit
  def notifyConstraintRemoved(constr: Constraint, argIndex: Int): Unit
}

abstract class ConstrainedVariable(val engine: ConstraintEngine, val internal: Boolean, 
                                   val canBeSpecified: Boolean, override val name: LabelStr,
                                   val parent: Option[Entity],
                                   val index: Int) extends Entity {

  def getEntityType: String = "ConstrainedVariable"
  def getDataType: DataType = baseDomain.getDataType

  def isValid: Boolean = lastDomain.getListener == listener &&
                         (lastDomain.isEmpty || lastDomain.isSubsetOf(baseDomain)) &&
                         constraints.map(_._1.isVariableOf(this)).reduceLeftOption(_ && _).getOrElse(true) && validate
  def constrs: Set[Constraint] = constraints.map(_._1).toSet
  def constraintCount: Int = constraints.size
  def getFirstConstraint: Option[Constraint] = constraints.headOption.map(_._1)
  def hasActiveConstraint: Boolean = constraints.map(_._1.isActive).reduceLeft(_ && _)
  def provenInconsistent: Boolean = engine.provenInconsistent
  def pending: Boolean = engine.pending
  def update: Unit = engine.propagate
  def deactivate: Unit = { 
    checkError(() => !Entity.isPurging, "Called deactivate while purging")
    deactivationRefCount += 1
    constraints.map(_._1.deactivate)
    if(deactivationRefCount == 1) engine.notifyDeactivated(this)
  }
  def undoDeactivation: Unit = { 
    checkError(() => !Entity.isPurging, "Called undoDeactivation while purging")
    deactivationRefCount -= 1
    constraints.map(_._1.undoDeactivation)
    if(deactivationRefCount == 0) engine.notifyActivated(this)
  }
  def isActive: Boolean = deactivationRefCount == 0
  def refCount: Int = deactivationRefCount
  
  def isSpecified: Boolean = specifiedFlag
  override def toString: String = { 
    val builder = new StringBuilder
    builder append super.toString
    if(isSingleton) builder.append(getSingletonValue)
    else if(baseDomain.isInterval) builder.append("[").append(lastDomain.getLowerBound).append(" ").append(lastDomain.getUpperBound).append("]")
    else if(baseDomain.isEnumerated) builder.append("{").append(lastDomain.getValues.map(_.toString)).append("}")
    else builder.append("Error!")
    return builder.toString
  }
  def toLongString: String = { 
    val builder = new StringBuilder
    builder.append(super.toString).append(if(isSpecified) "(S)" else "").append(" DERIVED=")
    .append(lastDomain.toString)
    return builder.toString
  }
  def getIndex: Option[Int] = Some(index)

  def lastDomain: Domain
  def derivedDomain: Domain
  def baseDomain: Domain

  def reset: Unit = reset(internal_baseDomain)
  def reset(d: Domain): Unit = { 
    checkError(() => d isSubsetOf internal_baseDomain, d.toString, " not in ",
               internal_baseDomain.toString)
    specifiedFlag = false
    getCurrentDomain.reset(d)
  }
  //TODO: eliminate specified flag in favor of just the option on specifiedValue
  def relax: Unit = { 
    if(specifiedFlag) getCurrentDomain.relax(specifiedValue.get)
    else getCurrentDomain.relax(baseDomain)
  }
  def isClosed: Unit = baseDomain.isClosed

  def insert(value: Double): Unit = { 
    checkError(() => internal_baseDomain.isOpen,
               "Can't insert a member into a variable with a cloased base domain.")
    internal_baseDomain.insert(value)
    if(!specifiedFlag && getCurrentDomain.isOpen)
      getCurrentDomain.insert(value)
  }

  def remove(value: Double): Unit = { 
    internal_baseDomain.remove(value)
    if(getCurrentDomain.isMember(value))
      getCurrentDomain.remove(value)
  }

  def specify(value: Double): Unit = { 
    debugMsg("ConstrainedVariable:specify", "specifying value: ", toString)
    checkError(() => canBeSpecified, "Tried to specify a variable that can't be specified")
    internalSpecify(value)
    debugMsg("ConstrainedVariable:specify", "specified value: ", toString)
  }

  def close: Unit = { 
    checkError(() => internal_baseDomain.isOpen, 
               "Attempted to close a variable but the base domain is already closed")
    internal_baseDomain.close
    if(getCurrentDomain.isOpen) getCurrentDomain.close
  }
  def open: Unit = { 
    checkError(() => internal_baseDomain.isClosed,
               "Attempted to open a variable but the base domain is already open.")
    internal_baseDomain.open
    if(getCurrentDomain.isClosed) getCurrentDomain.open
  }

  def touch: Unit = getCurrentDomain.touch

  def handleBase(d: Domain): Unit = { }
  def handleSpecified(value: Double): Unit = { }
  def handleReset: Unit = { }
  
  def notifyAdded(listener: ConstrainedVariableListener): Unit = { 
    checkError(() => !listeners.contains(listener), "Attempted to add a listener twice")
    listeners += listener
  }
  def notifyRemoved(listener: ConstrainedVariableListener): Unit = { 
    checkError(() => listeners.contains(listener), "Attempted to remove a listener twice")
    listeners -= listener
  }

  def toString(value: Double): String = getDataType.toString(value)

  def getViolation: Double = constraints.map(_._1.getViolation).reduceLeft(_+_)
  def getViolationExpl: String = new StringBuilder().append(constraints.map(_._1.getViolationExpl)).toString

  def setCurrentPropagatingConstraint(c: Constraint) = propagatingConstraint = c
  
  override def handleDiscard: Unit = { 
    if(!Entity.isPurging) { 
      constraints.map(_._1.discard)
    }
    engine.remove(this)
    listeners.map(_.notifyDiscard) //does this work?
  }
  def validate: Boolean = true
  def internal_baseDomain: Domain
  def internalSpecify(singletonValue: Double): Unit = { 
    if(!specifiedFlag || specifiedValue.get == singletonValue) { 
      debugMsg("ConstrainedVariable:internalSpecify", "specifying value: ",  toString)
      checkError(baseDomain.isMember(singletonValue), singletonValue, " not in ", baseDomain.toString)
      checkError(isActive, toString)
      if(!getCurrentDomain.isSingleton && engine.getAllowViolations) reset
      specifiedFlag = true
      specifiedValue = Some(singletonValue)
      if(!getCurrentDomain.isMember(singletonValue)) getCurrentDomain.empty
      else getCurrentDomain.set(singletonValue)
      debugMsg("ConstrainedVariable:internalSpecify", "specified value: ", toString)
      checkError(isValid, "Invalid!")
    }
  }
  def getCurrentDomain: Domain

  def addConstraint(constraint: Constraint, argIndex: Int): Unit = { 
    checkError(!Entity.isPurging, "Can't add constraints during purge")
    debugMsg("ConstrainedVariable:addConstraint", "Adding ", constraint, " to ", this)
    constraints = (constraint, index) :: constraints
    handleConstraintAdded(constraint)
    listeners.map(_.notifyConstraintAdded(constraint, argIndex))
    checkError(isConstrainedBy(constraint), "Weird state")
  }
  def removeConstraint(constraint: Constraint, argIndex: Int): Unit = { 
    checkError(() => !Entity.isPurging, "Purging!")
    checkError(() => isConstrainedBy(constraint), "Tried to remove a constraint from a variable it isn't on")
    constraints = constraints.filterNot(_ == (constraint, argIndex))
    handleConstraintRemoved(constraint)
    listeners.map(_.notifyConstraintRemoved(constraint, argIndex))
  }

  def handleConstraintAdded(constraint: Constraint): Unit = { }
  def handleConstraintRemoved(constraint: Constraint): Unit = { }
  
  def isConstrainedBy(constr: Constraint): Boolean = !constraints.find(_._1 == constr).isEmpty
  private[constraintengine] def updateLastRelaxed(cycleCount: Int): Unit = { 
    checkError(() => !Entity.isPurging, "Purging!")
    checkError(() => lastRelaxed < cycleCount, "Tried to back up the cycle count")
    lastRelaxed = cycleCount
    debugMsg("ConstrainedVariable:updateLastRelaxed", name, " updated to ", lastRelaxed)
  }
  
  def isSingleton: Boolean = isSpecified || lastDomain.isSingleton
  def getSingletonValue: Option[Double] = if(isSpecified) specifiedValue else lastDomain.getSingletonValue

  var deactivationRefCount: Int = 0
  val listener: DomainListener = engine.allocateVariableListener(this)
  var propagatingConstraint: Constraint = null
  var lastRelaxed: Int = -1
  var specifiedFlag: Boolean = false
  var specifiedValue: Option[Double] = None
  var listeners: Set[ConstrainedVariableListener] = Set()
  var constraints: List[(Constraint, Int)] = List()
}

object ConstrainedVariable { 
  val NO_NAME: LabelStr = LabelStr("NO_NAME")
  val NO_INDEX: Int = -1
}
