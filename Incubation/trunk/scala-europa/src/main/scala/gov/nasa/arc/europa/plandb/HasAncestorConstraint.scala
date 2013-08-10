package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr


class HasAncestorConstraint(name: LabelStr, pName: LabelStr, ce: ConstraintEngine, vars: Seq[ConstrainedVariable]) extends Constraint(name, pName, ce, vars) { 
import Constraint._

  override def handleExecute: Unit = apply
  override def handleExecute(variable: ConstrainedVariable, argIndex: Int,
                             changeType: DomainListener.ChangeType): Unit = handleExecute
  override def canIgnore(variable: ConstrainedVariable, argIndex: Int,
                         changeType: DomainListener.ChangeType): Boolean = false

  def apply: Unit = { 
    val firstValues = m_first.getValues

    val allAncestors: Set[Object] = firstValues.map((o: Double) => m_first.getObject(o.toInt)).flatten.map(_.getAncestors.toSet).fold(Set())((a, b) => a | b)

    val dt = engine.getSchema.getDataType(allAncestors.head.getRootType).get

    val setOfAncestors = ObjectDomain(allAncestors, dt)
    setOfAncestors.intersect(m_restrictions)

    for(v <- m_first.getValues) { 
      val candidate = m_first.getObject(v.toInt).get
      val candidatesAncestors = candidate.getAncestors
      if(candidatesAncestors.filterNot(setOfAncestors.isMember(_)).isEmpty) m_first.remove(candidate)
    }
  }

  val m_first: ObjectDomain = getCurrentDomain(vars(0)).asInstanceOf[ObjectDomain]
  val m_restrictions: ObjectDomain = getCurrentDomain(vars(1)).asInstanceOf[ObjectDomain]

}
