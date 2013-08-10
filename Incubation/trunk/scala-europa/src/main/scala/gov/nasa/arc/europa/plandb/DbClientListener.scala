package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.utils.LabelStr

trait DbClientListener { 
  val client: DbClient
  client.notifyAdded(this)

  def notifyClosed: Unit = { }
  def notifyClosed(objectType: LabelStr): Unit = { }

  def notifyVariableCreated(v: ConstrainedVariable): Unit = { }
  def notifyVariableDeleted(v: ConstrainedVariable): Unit = { }
  def notifyVariableSpecified(v: ConstrainedVariable): Unit = { }
  def notifyVariableReset(v: ConstrainedVariable): Unit = { }

  def notifyObjectCreated(o: gov.nasa.arc.europa.plandb.Object): Unit = { }
  def notifyObjectCreated(o: gov.nasa.arc.europa.plandb.Object, args: Vector[Domain]): Unit = { }
  def notifyObjectDeleted(o: gov.nasa.arc.europa.plandb.Object): Unit = { }

  def notifyTokenCreated(t: Token): Unit = { }
  def notifyTokenDeleted(t: Token, name: LabelStr): Unit = { }
  def notifyConstrained(o: gov.nasa.arc.europa.plandb.Object, predecessor: Token,
                        successor: Token): Unit = { }
  def notifyFreed(o: gov.nasa.arc.europa.plandb.Object, predecessor: Token, 
                  successor: Token): Unit = { }
  def notifyActivated(t: Token): Unit = { }
  def notifyMerged(t: Token, a: Token): Unit = { }
  def notifyMerged(t: Token): Unit = { }
  def notifyRejected(t: Token): Unit = { }
  def notifyCancelled(t: Token): Unit = { }
  def notifyConstraintCreated(c: Constraint): Unit = { }
  def notifyConstraintDeleted(c: Constraint): Unit = { }
}
