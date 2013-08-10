package gov.nasa.arc.europa.constraintengine.component
import gov.nasa.arc.europa.constraintengine.DataType
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.utils.LabelStr
import gov.nasa.arc.europa.utils.Number._

class VoidDT extends DataType(VoidDT.NAME) { 
  override val baseDomain = Domain.NO_DOMAIN
  override def isNumeric = false
  override def isBool = false
  override def isString = false
  override def createValue(v: String): Option[Double] = throw new Exception("Can't create an instance of the void type.")
  override def emptyDomain = Domain.NO_DOMAIN
}
object VoidDT { 
  val NAME = "void"
  val INSTANCE = new VoidDT
}

class FloatDT extends DataType(FloatDT.NAME, EPSILON, null) { 
  override val baseDomain = new IntervalDomain(this)
  override def isNumeric = true
  override def isBool = false
  override def isString = false
  override def createValue(v: String): Option[Double] = { 
    if(v == "-inf" || v == "-inff") return Some(MINUS_INFINITY)
    else if(v == "inf" || v == "+inf" || v == "inff" || v == "+inff") return Some(PLUS_INFINITY)
    else return try { Some(java.lang.Double.parseDouble(v))} catch {case _ : Throwable => None}
  }
  override def emptyDomain = new IntervalDomain(3, -2)
}

object FloatDT { 
  val NAME = "float"
  val INSTANCE = new FloatDT
}

class IntDT extends DataType(IntDT.NAME, 1, null) { 
  override val baseDomain = new IntervalIntDomain()
  override def isNumeric = true
  override def isBool = false
  override def isString = false
  override def createValue(v: String): Option[Double] = { 
    if(v == "-inf") return Some(MINUS_INFINITY)
    else if(v == "inf" || v == "+inf") return Some(PLUS_INFINITY)
    else return try { Some(java.lang.Double.parseDouble(v))} catch {case _ : Throwable => None}
  }
  override def emptyDomain = new IntervalIntDomain(3, -2)
  override def toString(value: Double): String = value.toInt.toString
}

object IntDT { 
  val NAME = "int"
  val INSTANCE = new IntDT
}

class BoolDT extends DataType(BoolDT.NAME, 1, null) { 
  override val baseDomain = new BoolDomain()
  override def isNumeric = true
  override def isBool = true
  override def isString = false
  override def createValue(v: String): Option[Double] = v match { 
    case "true" => Some(1)
    case "false" => Some(0)
    case _ => None
  }
  override def toString(v: Double): String = { 
    if(v == 0) "false"
    else "true"
  }
  override def emptyDomain = new BoolDomain(1.0, 0.0)
}

object BoolDT { 
  val NAME = "bool"
  val INSTANCE = new BoolDT
}

class SymbolDT(name: LabelStr = SymbolDT.NAME) extends DataType(name, 1, null) { 

  override val baseDomain = new SymbolicDomain(this)
  override def isNumeric = false
  override def isBool = false
  override def isString = false
  override def createValue(value: String): Option[Double] = Some(LabelStr(value).key.toDouble)
  override def emptyDomain = new SymbolicDomain(Set[Double](), this)
  override def toString(value: Double): String = LabelStr(value.toInt).toString
}

object SymbolDT { 
  val NAME = "symbol"
  val INSTANCE = new SymbolDT
}

class RestrictedDT(name: LabelStr, baseType: DataType, override val baseDomain: Domain) extends DataType(name, baseType.minDelta, null) { 
  override def isNumeric = baseType.isNumeric
  override def isString = baseType.isString
  override def isBool = baseType.isBool
  override def createValue(v: String): Option[Double] = { 
    val retval = baseType.createValue(v)
    if(!retval.isDefined || !baseDomain.isMember(retval.get)) None else retval
  }
  override def emptyDomain: Domain = baseType.emptyDomain
}

object RestrictedDT { 
  def apply(name: LabelStr, baseType: DataType, baseDomain: Domain) = new RestrictedDT(name, baseType, baseDomain)
  def apply(name: LabelStr, baseDomain: Domain) = new RestrictedDT(name, baseDomain.getDataType, baseDomain)
}
