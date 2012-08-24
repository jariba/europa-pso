package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.LabelStr

abstract class Variable[D <: Domain](override val engine: ConstraintEngine, val baseDomain: D, 
                            override val internal: Boolean, override val canBeSpecified: Boolean, 
                            override val name: LabelStr, override val parent: Option[Entity],
                            index: Int) extends ConstrainedVariable(engine, internal, canBeSpecified, name, parent, index) { 
}
