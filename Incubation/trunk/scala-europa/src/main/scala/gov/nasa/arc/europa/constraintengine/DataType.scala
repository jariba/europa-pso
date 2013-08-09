package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr

abstract class DataType(val name: LabelStr, val minDelta: Double = 1, val baseDomain: Domain = Domain.NO_DOMAIN) { 
  // baseDomain.setDataType(this)
  def isNumeric: Boolean
  def isBool: Boolean
  def isString: Boolean 
  def isEntity: Boolean = false
  def isSymbolic: Boolean = !isNumeric

  def canBeCompared(rhs: DataType): Boolean = isNumeric == rhs.isNumeric && isString == rhs.isString && isSymbolic == rhs.isSymbolic

  def isAssignableFrom(rhs: DataType): Boolean = if (isNumeric) rhs.isNumeric && minDelta <= rhs.minDelta else canBeCompared(rhs)

  def createValue(value: String): Option[Double]
  
  def toString(value: Double): String = { 
    if(isNumeric) value.toString
    else LabelStr(value.toInt).toString
  }

  def createVariable[D <: Domain](engine: ConstraintEngine, domainBase: D, internal: Boolean = false,
                     canBeSpecified: Boolean, name: LabelStr = ConstrainedVariable.NO_NAME,
                     parent: Option[Entity] = None, 
                     index: Int = ConstrainedVariable.NO_INDEX): ConstrainedVariable = { 

    checkError(canBeCompared(domainBase.dataType), "Tried to create a ", name.toString, " variable with a different kind of base domain: ", domainBase.dataType.name.toString)
    return new Variable[D](engine, domainBase, internal, canBeSpecified, name, parent, index)
  }

  def emptyDomain: Domain
}

object DataType { 
  val NOTHING: DataType = new DataType(LabelStr("Nothing"), java.lang.Double.MAX_VALUE, Domain.NO_DOMAIN) { 
    def isNumeric = false
    def isBool = false
    def isString = false
    override def canBeCompared(rhs: DataType) = false
    override def isAssignableFrom(rhs: DataType) = false
    def createValue(value: String) = None
    override def toString(value: Double) = "None"
    override def createVariable[D <: Domain](engine: ConstraintEngine, domainBase: D, 
                                internal: Boolean = false, canBeSpecified: Boolean, 
                                name: LabelStr = ConstrainedVariable.NO_NAME,
                                parent: Option[Entity] = None, 
                                index: Int = ConstrainedVariable.NO_INDEX) = { 
      throw new Exception("Tried to create an instance of the Nothing type.")
    }
    override def emptyDomain = Domain.NO_DOMAIN
  }
}
