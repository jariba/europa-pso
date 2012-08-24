package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr


object Constraint { 
  def getCurrentDomain(v: ConstrainedVariable): Domain = null
}

abstract class Constraint(override val name: LabelStr, val propagatorName: LabelStr,
                          val engine: ConstraintEngine,
                          val variables: Seq[ConstrainedVariable]) extends Entity { 


  def getEntityType: String = "CONSTRAINT"
  def isVariableOf(v: ConstrainedVariable): Boolean = !variables.find(_ == v).isEmpty
  def isActive: Boolean = deactivationRefCount == 0
  def isUnary: Boolean = variables.size == 1 //why is this a flag in the C++?

  def deactivate: Unit = { 
    checkError(() => !Entity.isPurging, "Purging!")
    deactivationRefCount += 1
    if(deactivationRefCount == 1) { 
      engine.notifyDeactivated(this)
      handleDeactivate
    }
  }
  def undoDeactivation: Unit = { 
    checkError(() => !Entity.isPurging, "Purging!")
    if(!redundant) { 
      deactivationRefCount -= 1
      if(deactivationRefCount == 0) { 
        engine.notifyActivated(this)
        handleActivate
      }
    }
  }
  def getScope: Seq[ConstrainedVariable] = variables
  def setSource(sourceConstraint: Constraint): Unit = source = Some(sourceConstraint)
  
  def toLongString: String = { 
    val builder = new StringBuilder
    builder.append(super.toString)
    variables.zipWithIndex.map(v => builder.append(" ARG[").append(v._2).append("]:").append(v._1.toLongString).append("\n"))
    if(violationExpl != LabelStr()) builder.append("{ViolationExpl:").append(violationExpl.toString).append("}")
    return builder.toString
  }
  def notifyBaseDomainRestricted(v: ConstrainedVariable): Unit = { 
    debugMsg("Constraint:notifyBaseDomainRestricted", "Base domain of ", v.toLongString, " restricted in ", toString)
    if(!redundant) { 
      redundant = testIsRedundant(Some(v))
      if(redundant) engine.notifyRedundant(this)
    }
  }
  def isRedundant: Boolean = redundant

  def getViolation: Double = if(engine.isViolated(this)) 1.0 else 0.0
  def getViolationExpl: String = { 
    if(violationExpl.length > 0) return violationExpl.toString
    val builder = new StringBuilder
    builder.append(name).append("(");
    variables.map((v) => if(v.name.matches("$VAR")) builder.append(v.name).append("(").append(v.key).append(")") else builder.append(v.lastDomain.toString))
    builder.append(")")
    return builder.toString
  }
  def setViolationExpl(expl: String): Unit = violationExpl = expl

  def notifyViolated: Unit = engine.getViolationManager.addViolatedConstraint(this)
  def notifyNoLongerViolated: Unit = engine.getViolationManager.removeViolatedConstraint(this)
  
  def testIsRedundant(v: Option[ConstrainedVariable] = None): Boolean = v match { 
    case None => false
    case Some(variable) => if(variable.baseDomain.isSingleton || variable.baseDomain.isOpen) true
                           else variables.map(_.baseDomain.isSingleton).reduceLeft(_ && _)
  }
  
  def execute: Unit = { 
    variables.map(_.setCurrentPropagatingConstraint(this))
    handleExecute
    variables.map(_.setCurrentPropagatingConstraint(null))
  }
  def execute(variable: ConstrainedVariable, argIndex: Int, change: DomainListener.ChangeType): Unit = { 
    variables.map(_.setCurrentPropagatingConstraint(this))
    handleExecute(variable, argIndex, change)
    variables.map(_.setCurrentPropagatingConstraint(null))
  }

  def handleExecute: Unit
  def handleExecute(variable: ConstrainedVariable, argIndex: Int, change: DomainListener.ChangeType): Unit = handleExecute

  def canIgnore(variable: ConstrainedVariable, argIndex: Int, change: DomainListener.ChangeType): Boolean = false

  def getModifiedVariables: Seq[ConstrainedVariable] = getScope
  def getModifiedVariables(variable: ConstrainedVariable): Seq[ConstrainedVariable] = getScope
  
  def handleActivate: Unit = { }
  def handleDeactivate: Unit = { }

  override def handleDiscard: Unit = { 
    if(!Entity.isPurging) { 
      checkError(() => isValid, "Not valid")
      variables.zipWithIndex.map((v) => v._1.removeConstraint(this, v._2))
    }
    engine.remove(this)
    super.handleDiscard
  }

  private[constraintengine] def setPropagator(p: Propagator): Unit = { 
    checkError(() => propagator == None, "Propagator not None")
    checkError(() => p.engine == engine, "Propagator not for the same engine")
    propagator = Some(p)
  }
  private[constraintengine] def isValid: Boolean = !variables.isEmpty && !propagator.isEmpty && engine != null

  var propagator: Option[Propagator] = None
  var source: Option[Constraint] = None
  var deactivationRefCount: Int = 0
  var redundant: Boolean = false
  var violationExpl: LabelStr = LabelStr()

}

object Scope { 
  def apply(scope: ConstrainedVariable*): Vector[ConstrainedVariable] = Vector(scope:_*)
}
