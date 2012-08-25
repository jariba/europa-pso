package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.LabelStr

class Variable[D <: Domain](override val engine: ConstraintEngine, val baseDomain: D, 
                            override val internal: Boolean, override val canBeSpecified: Boolean, 
                            override val name: LabelStr, override val parent: Option[Entity],
                            index: Int) extends ConstrainedVariable(engine, internal, canBeSpecified, name, parent, index) { 
  val m_derivedDomain: D = baseDomain.copy.asInstanceOf[D]
  override val listener = engine.allocateVariableListener(this)
  override def lastDomain: Domain = m_derivedDomain
  override def derivedDomain: Domain = { 
    if(!engine.isPropagating && pending)
      update

    if(engine.provenInconsistent)
      return m_derivedDomain
    else
      return baseDomain.dataType.emptyDomain
  }
  override def internal_baseDomain = baseDomain
  override def getCurrentDomain = lastDomain
}










