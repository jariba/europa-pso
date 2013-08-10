package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr

trait ConstraintTypeChecker { 
  def check(name: String, argTypes: Seq[DataType]): Option[String] = None
}

abstract class ConstraintType(val name: LabelStr, val propagatorName: LabelStr, 
                              val systemDefined: Boolean) { 

  def createConstraint(engine: ConstraintEngine, scope: Seq[ConstrainedVariable], violationExpl: String): Constraint 
  def check(types: Seq[DataType]): Option[String] = check(name.toString, types)
  def check(name: String, argTypes: Seq[DataType]): Option[String]

}

object ConstraintType { 
  def make[C <: Constraint](name: LabelStr, propagatorName: LabelStr, ce: ConstraintEngine, 
              scope: Seq[ConstrainedVariable], violationExpl: String)(implicit m: Manifest[C]): C = { 
    val retval = 
      m.runtimeClass.getConstructor(classOf[LabelStr], classOf[LabelStr],
                                    classOf[ConstraintEngine], 
                                    classOf[Seq[ConstrainedVariable]]).
        newInstance(name, propagatorName, ce, scope).asInstanceOf[C]
    retval.setViolationExpl(violationExpl)
    return retval
  }
}
