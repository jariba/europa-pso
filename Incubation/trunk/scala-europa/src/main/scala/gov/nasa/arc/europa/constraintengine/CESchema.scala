package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.EngineComponent
import gov.nasa.arc.europa.utils.LabelStr
import gov.nasa.arc.europa.utils.LabelStr._
import scala.collection.immutable.Map

trait CFunction { 
  def name: String
}

class CESchema extends EngineComponent { 
  //refactor this to generically update maps from named things to their names?
  def registerDataType(dt: DataType): Unit = { 
    condDebugMsg(() => isDataType(dt.name), "CESchema:registerDataType", "Overwriting ", dt.name)
    dataTypes = dataTypes.updated(dt.name, dt)
    debugMsg("CESchema:registerDataType", "Registered ", dt.name)
  }
  def getDataType(name: String): Option[DataType] = dataTypes.get(name)
  def isDataType(name: String): Boolean = dataTypes.isDefinedAt(name)
  def baseDomain(name: String): Option[Domain] = dataTypes.get(name).map(_.baseDomain)
  def getDataTypes: Map[LabelStr, DataType] = dataTypes
  def purgeDataTypes: Unit = dataTypes = Map()

  def registerConstraintType(ct: ConstraintType): Unit = { 
    condDebugMsg(() => isConstraintType(ct.name), "CESchema:registerConstraintType", "Overwriting ", ct.name)
    constraintTypes = constraintTypes.updated(ct.name, ct)
    debugMsg("CESchema:registerConstraintType", "Registered ", ct.name)
  }
  def getConstraintType(name: LabelStr): Option[ConstraintType] = constraintTypes.get(name)
  def isConstraintType(name: LabelStr): Boolean = constraintTypes.isDefinedAt(name)
  def purgeConstraintTypes: Unit = constraintTypes = Map()

  def registerCFunction(cf: CFunction): Unit = { 
    condDebugMsg(() => cfunctions.isDefinedAt(cf.name), "CESchema:registerCFunction", "Overwriting ", cf.name)
    cfunctions = cfunctions.updated(cf.name, cf)
    debugMsg("CESchema:registerConstraintType", "Registered ", cf.name)
  }
  def getCFunction(name: LabelStr): Option[CFunction] = cfunctions.get(name)
  def purgeCFunctions: Unit = cfunctions = Map()
  
  def purgeAll: Unit = { purgeDataTypes; purgeConstraintTypes; purgeCFunctions}
  def delete: Unit = { }

  private var dataTypes: Map[LabelStr, DataType] = Map()
  private var constraintTypes: Map[LabelStr, ConstraintType] = Map()
  private var cfunctions: Map[LabelStr, CFunction] = Map()

}
