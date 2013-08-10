package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr



class CommonAncestorConstraint(name: LabelStr, pName: LabelStr, ce: ConstraintEngine, vars: Seq[ConstrainedVariable]) extends Constraint(name, pName, ce, vars) { 
  val first: ObjectDomain = vars(0).getCurrentDomain.asInstanceOf[ObjectDomain]
  val second: ObjectDomain = vars(1).getCurrentDomain.asInstanceOf[ObjectDomain]
  val restrictions: ObjectDomain = vars(1).getCurrentDomain.asInstanceOf[ObjectDomain]

  override def handleExecute: Unit = { 
    checkError(!first.isEmpty && !second.isEmpty && !restrictions.isEmpty, "")
    if(first.isSingleton) apply(first, second)
    if(second.isSingleton) apply(second, first)
  }
  override def handleExecute(v: ConstrainedVariable, argIndex: Int, changeType: DomainListener.ChangeType): Unit = handleExecute
  override def canIgnore(v: ConstrainedVariable, argIndex: Int, changeType: DomainListener.ChangeType): Boolean = { 
    checkError(argIndex <= 2, "")
    return !first.isSingleton && !second.isSingleton
  }
  def apply(singleton: ObjectDomain, other: ObjectDomain): Unit = { 
    checkError(singleton.isSingleton, "")
    val singletonObject = singleton.getObject(singleton.getSingletonValue.get.toInt)
    val singletonAncestors = singletonObject.get.getAncestors :+ singletonObject.get
    val dt = ce.getSchema.getDataType(singletonObject.get.rootType).get
    val setOfAncestors = ObjectDomain(singletonAncestors, dt)
    setOfAncestors.intersect(restrictions)
    if(setOfAncestors.isEmpty) { 
      other.empty
    }
    else { 
      val candidateValues = other.getValues
      for(obj <- candidateValues) { 
        val candidate = other.getObject(obj.toInt)
        checkError(!candidate.isEmpty, "")
        val candidatesAncestors = candidate.get.getAncestors :+ candidate.get
        val candidateRootDT = ce.getSchema.getDataType(candidate.get.rootType).get
        val od = ObjectDomain(candidatesAncestors, candidateRootDT)
        od.intersect(setOfAncestors)
        if(od.isEmpty) { 
          other.remove(candidate.get)
          if(other.isEmpty)
            return
        }
      }
    }
  }
}
