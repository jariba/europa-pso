package gov.nasa.arc.europa.constraintengine.component
import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.constraintengine.Propagator
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr
import scala.collection.immutable.SortedSet
import scala.collection.immutable.TreeSet

class DefaultPropagator(override val name: LabelStr, override val engine: ConstraintEngine) extends Propagator(name, engine) { 
  override def execute: Unit = { 
    checkError(updateRequired, "Should never be calling this with an empty agenda")
    checkError(activeConstraint == 0, "Recursively called execute")
    
    if(!engine.provenInconsistent){
      val constraint = agenda.head
      agenda = agenda.tail

      if(constraint.isActive){
	activeConstraint = constraint.key
	super.execute(constraint)
      }
    }

    // If we can continue propagation despite the discovered inconsistency,
    // keep agenda for when the ConstraintEngine recovers and decides to resume propagation
    if(engine.provenInconsistent) {
        if(engine.canContinuePropagation) {
	  debugMsg("DefaultPropagator:agenda","CE was proven inconsistent, keeping agenda because propagation can continue later");
	  // TODO: should remove from the agenda any constraints associated with the empty variable, since it'll be relaxed and they'll ba added again
        }
        else {
          agenda = agenda.empty
	  debugMsg("DefaultPropagator:agenda","Cleared agenda because CE was proven inconsistent");
        }
    }
    activeConstraint = 0
  }
  override def updateRequired: Boolean = !agenda.isEmpty
  override def handleConstraintAdded(constraint: Constraint): Unit = { agenda = agenda + constraint}
  override def handleConstraintRemoved(constraint: Constraint): Unit = { agenda = agenda - constraint}
  override def handleConstraintActivated(constraint: Constraint): Unit = { agenda = agenda + constraint}
  override def handleConstraintDeactivated(constraint: Constraint): Unit = { agenda = agenda - constraint}
  override def handleNotification(source: ConstrainedVariable, argIndex: Int, c: Constraint, 
                         change: DomainListener.ChangeType): Unit = { 
    debugMsg("DefaultPropagator:handleNotification",
             "Received ", change, " notification on ", source, " through ", c)
    if(c.key != activeConstraint) agenda = agenda + c
  }

  def handleVariableAdded(v: ConstrainedVariable): Unit = { }
  def handleVariableDeactivated(v: ConstrainedVariable): Unit = { }
  def handleVariableActivated(v: ConstrainedVariable): Unit = { }
  def handleVariableRemoved(v: ConstrainedVariable): Unit = { }


  var agenda = TreeSet[Constraint]()(Ordering.fromLessThan[Constraint](_.key < _.key))
  var activeConstraint = 0;
}
