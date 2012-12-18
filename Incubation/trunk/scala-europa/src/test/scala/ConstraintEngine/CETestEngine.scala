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
}
