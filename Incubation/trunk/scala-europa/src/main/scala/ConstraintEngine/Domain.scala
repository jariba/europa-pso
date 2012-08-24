package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.utils.Entity

import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr
import gov.nasa.arc.europa.utils.Number._
import scala.math.abs

trait DomainListener { 
  def notifyChange(change: DomainListener.ChangeType): Unit
}
object DomainListener extends Enumeration { 
  type ChangeType = Value
  val UPPER_BOUND_DECREASED, LOWER_BOUND_INCREASED, BOUNDS_RESTRICTED, VALUE_REMOVED, RESTRICT_TO_SINGLETON,
  SET_TO_SINGLETON, RESET, RELAXED, CLOSED, OPENED, EMPTIED  = Value
  def isRestriction(change: ChangeType): Boolean = change <= RESTRICT_TO_SINGLETON
}


abstract class Domain(var dataType: DataType = null, var closed: Boolean = true, var listener: DomainListener = null) { 
  def isEnumerated: Boolean
  def isInterval: Boolean
  def isClosed: Boolean = closed
  def isOpen: Boolean = !isClosed
  def isEmpty: Boolean
  def isFinite: Boolean
  def isInfinite: Boolean = !isFinite
  def areBoundsFinite: Boolean = (!dataType.isNumeric || 
                                  (!isEmpty && isClosed && upperBound < PLUS_INFINITY && 
                                   lowerBound > MINUS_INFINITY))
  def isSingleton: Boolean

  def size: Option[Int]

  def lowerBound: Double
  def upperBound: Double
  def getUpperBound: Double = lowerBound
  def getLowerBound: Double = upperBound
  def getBounds: (Double, Double) = (getLowerBound, getUpperBound)
  def getValues: List[Double]
  def getSingletonValue: Option[Double]

  def close: Unit = {
    if(!closed) { 
      closed = true
      notifyChange(DomainListener.CLOSED)
      if(isEmpty) empty
    }
  }
  def open: Unit = { 
    if(closed) { 
      closed = false
      notifyChange(DomainListener.OPENED)
    }
  }
  def touch: Unit = notifyChange(DomainListener.BOUNDS_RESTRICTED)
  def empty: Unit
  def set(value: Double): Unit
  def reset(other: Domain): Unit
  def relax(other: Domain): Unit
  def relax(other: Double): Unit
  def insert(other: Double): Unit
  def remove(other: Double): Unit
  def intersect(other: Domain): Boolean
  def intersect(lb: Double, ub: Double): Boolean
  def difference(other: Domain): Boolean
  def equate(other: Domain): Boolean

  def isMember(value: Double): Boolean
  def isSubsetOf(other: Domain): Boolean
  def intersects(other: Domain): Boolean

  def ===(other: Domain): Boolean = closed == other.closed && isFinite == other.isFinite
  def !==(other: Domain): Boolean = !(this === other)

  def setListener(l: DomainListener): Unit = { 
    checkError(() => listener == null, "Tried to set listener twice")
    listener = l
    if(isClosed) { 
      notifyChange(DomainListener.CLOSED)
      if(isEmpty) { 
        notifyChange(DomainListener.EMPTIED)
      }
    }
  }

  def getListener: DomainListener = listener

  def copy: Domain

  override def toString: String = { 
    val retval = new StringBuilder
    retval.append(dataType.name).append(if(isClosed) ":CLOSED" else ":OPEN")
    return retval.toString
  }

  def setDataType(dt: DataType): Unit = dataType = dt
  def getDataType: DataType = dataType
  def minDelta: Double = dataType.minDelta
  def isSymbolic: Boolean = dataType.isSymbolic
  def isEntity: Boolean = dataType.isEntity
  def isNumeric: Boolean = dataType.isNumeric
  def isBool: Boolean = dataType.isBool
  def isString: Boolean = dataType.isString

  def translateNumber(number: Double, asMin: Boolean = true): Double = number
  def convertToMemberValue(value: String): Option[Double]
  def compareEqual(a: Double, b: Double): Boolean = {return abs(a - b) < minDelta}
  def lt(a: Double, b: Double): Boolean = a != PLUS_INFINITY && b != MINUS_INFINITY && (a + minDelta <= b)
  def eq(a: Double, b: Double) = compareEqual(a, b)
  def leq(a: Double, b: Double): Boolean = (a == b) || (a - minDelta) < b

  protected def notifyChange(changeType: DomainListener.ChangeType): Unit = { 
    if(listener != null) { 
      if(DomainListener.isRestriction(changeType) && isSingleton)
        listener.notifyChange(DomainListener.RESTRICT_TO_SINGLETON)
      else
        listener.notifyChange(changeType)
    }
  }

  protected def check_value(value: Double): Boolean = { 
    testPrecision(value); 
    return !dataType.isNumeric || (value >= MINUS_INFINITY && value <= PLUS_INFINITY)
  }
  protected def testPrecision(value: Double): Unit

}

object Domain { 
  def canBeCompared(domx: Domain, domy: Domain) = false;
  def assertSafeComparison(doma: Domain, domb: Domain): Unit = { }

  val NO_DOMAIN: Domain = new Domain(DataType.NOTHING, true, null) { 
    def isEmpty = true
    def isFinite = true
    def isSingleton = false
    def isEnumerated = false
    def isInterval = false
    def size = Some(0)
    def lowerBound = throw new Exception("Tried to get the lower bound of the Nothing domain")
    def upperBound = throw new Exception("Tried to get the upper bound of the Nothing domain")
    def getValues = throw new Exception("Tried to get the values of the Nothing domain")
    def getSingletonValue = throw new Exception("Tried to get the singelton value of the Nothing domain")
    def empty = throw new Exception("Tried to empty the Nothing domain")
    def set(value: Double) = throw new Exception("Tried to set the value of the Nothing domain")
    def reset(other: Domain) = throw new Exception("Tried to reset the Nothing domain")
    def relax(other: Domain) = throw new Exception("Tried to relax the Nothing domain")
    def relax(value: Double) = throw new Exception("Tried to relax the Nothing domain")
    def insert(value: Double) = throw new Exception("Tried to insert a value into the Nothing domain")
    def remove(value: Double) = throw new Exception("Tried to remove a value from the Nothing domain")
    def intersect(lb: Double, ub: Double) = throw new Exception("Tried to perform an intersection on the Nothing domain")
    def intersect(other: Domain) = throw new Exception("Tried to perform an intersection on the Nothing domain")
    def difference(other: Domain) = throw new Exception("Tried to compute a difference with the Nothing domain")
    def equate(other: Domain) = throw new Exception("Tried to equate the Nothing domain with another")
    def isMember(value: Double) = false
    def isSubsetOf(other: Domain) = false
    def intersects(ohter: Domain) = false
    override def ===(other: Domain) = false
    override def setListener(l: DomainListener): Unit = throw new Exception("Tried to add a listener to the Nothing domain")
    override def getListener  = throw new Exception("Tried to get a listener from the Nothing domain")
    override def copy = throw new Exception("Tried to copy the Nothing domain")
    override def toString = "{Nothing}"
    override def translateNumber(number: Double, asMin: Boolean = true): Double = throw new Exception("Tried to translate a number to the Nothing domain")
    override def convertToMemberValue(s: String) = throw new Exception("Tried to translate a string to the Nothing domain")
    def testPrecision(value: Double) = throw new Exception("Fail")
    
  }
}
