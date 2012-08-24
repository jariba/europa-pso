package gov.nasa.arc.europa.constraintengine.component
import gov.nasa.arc.europa.constraintengine.DataType
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.utils.Number._

class VoidDT extends DataType(VoidDT.NAME) { 
  override def isNumeric = false
  override def isBool = false
  override def isString = false
  override def createValue(v: String): Option[Double] = throw new Exception("Can't create an instance of the void type.")
  
}
object VoidDT { 
  val NAME = "void"
  val INSTANCE = new VoidDT
}

class FloatDT extends DataType(FloatDT.NAME, 0.00001, new IntervalDomain()) { 
  override def isNumeric = true
  override def isBool = false
  override def isString = false
  override def createValue(v: String): Option[Double] = { 
    if(v == "-inf" || v == "-inff") return Some(MINUS_INFINITY)
    else if(v == "inf" || v == "+inf" || v == "inff" || v == "+inff") return Some(PLUS_INFINITY)
    else return try { Some(java.lang.Double.parseDouble(v))} catch {case _ => None}
  }
  
}

object FloatDT { 
  val NAME = "float"
  val INSTANCE = new FloatDT
}

class IntDT extends DataType(IntDT.NAME, 1, new IntervalIntDomain()) { 
  override def isNumeric = true
  override def isBool = false
  override def isString = false
  override def createValue(v: String): Option[Double] = { 
    if(v == "-inf") return Some(MINUS_INFINITY)
    else if(v == "inf" || v == "+inf") return Some(PLUS_INFINITY)
    else return try { Some(java.lang.Double.parseDouble(v))} catch {case _ => None}
  }
}

object IntDT { 
  val NAME = "int"
  val INSTANCE = new IntDT
}

class BoolDT extends DataType(BoolDT.NAME, 1, new BoolDomain()) { 
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
}

object BoolDT { 
  val NAME = "bool"
  val INSTANCE = new BoolDT
}
