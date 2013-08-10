package gov.nasa.arc.europa.constraintengine.test
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.ModuleConstraintEngine
import gov.nasa.arc.europa.constraintengine.ModuleConstraintLibrary
import gov.nasa.arc.europa.utils.EngineBase

case class CETestEngine() extends EngineBase { 
  createModules
  doStart
  

  def createModules = { 
    addModule(new ModuleConstraintEngine)
    addModule(new ModuleConstraintLibrary)
  }
  def getConstraintEngine: ConstraintEngine = getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]

  import gov.nasa.arc.europa.constraintengine.component.SymbolicDomain._
  import gov.nasa.arc.europa.constraintengine.component.SymbolicDomain
  getConstraintEngine.getSchema.registerDataType(new gov.nasa.arc.europa.constraintengine.component.RestrictedDT("Locations", gov.nasa.arc.europa.constraintengine.component.SymbolDT.INSTANCE, SymbolicDomain(Set("Hill", "Rock", "Lander"))))
}
