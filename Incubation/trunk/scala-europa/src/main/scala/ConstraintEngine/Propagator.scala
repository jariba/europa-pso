package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr

abstract class Propagator(override val name: LabelStr, val engine: ConstraintEngine) extends Entity { 
  engine.add(this)

  def isEnabled: Boolean = enabled
  def enable: Unit = enabled = true
  def disable: Unit = enabled = false

  def addConstraint(c: Constraint): Unit = { 
    checkError(c.propagator == null, c, " already has a propagator.")
    constraints = constraints + c
    handleConstraintAdded(c)
  }

  def removeConstraint(c: Constraint): Unit = { 
    checkError(c.propagator == this, c, " isn't on this propagator")
    constraints = constraints - c
    handleConstraintRemoved(c)
  }
  def handleNotification(source: ConstrainedVariable, argIndex: Int, c: Constraint, 
                         change: DomainListener.ChangeType): Unit

  def execute: Unit

  def updateRequired: Boolean

  def handleConstraintAdded(c: Constraint): Unit
  def handleConstraintActivated(c: Constraint): Unit 
  def handleConstraintDeactivated(c: Constraint): Unit
  def handleConstraintRemoved(c: Constraint): Unit

  def handleVariableAdded(v: ConstrainedVariable): Unit
  def handleVariableDeactivated(v: ConstrainedVariable): Unit
  def handleVariableActivated(v: ConstrainedVariable): Unit
  def handleVariableRemoved(v: ConstrainedVariable): Unit

  def execute(c: Constraint): Unit = engine.execute(c)

  def notifyConstraintViolated(c: Constraint): Unit = c.notifyViolated
  def notifyVariableEmptied(v: ConstrainedVariable): Unit = engine.getViolationManager.addEmptyVariable(v)

  var enabled: Boolean = false
  var constraints: Set[Constraint] = Set()
}

object Propagator { 
  def getCurrentDomain(v: ConstrainedVariable): Domain = null
}
