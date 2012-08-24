package gov.nasa.arc.europa.constraintengine.component
import gov.nasa.arc.europa.constraintengine.DataType
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.Number._

import scala.math._

object IntervalDomain { 
  def apply() = new IntervalDomain(FloatDT.INSTANCE)
  def apply(dataType: DataType) = new IntervalDomain(dataType)
  def apply(lowerBound: Double, upperBound: Double) = 
    new IntervalDomain(lowerBound, upperBound, FloatDT.INSTANCE)
  def apply(lowerBound: Double, upperBound: Double, dataType: DataType) = 
    new IntervalDomain(lowerBound, upperBound, dataType)
}
class IntervalDomain(dataType: DataType = FloatDT.INSTANCE) extends Domain(dataType, true) { 
 var lowerBound: Double = MINUS_INFINITY
 var upperBound: Double = PLUS_INFINITY

  def this(lowerBound: Double, upperBound: Double, dataType: DataType = FloatDT.INSTANCE) = { 
    this(dataType)
  }
  def this(v: Double, dataType: DataType = FloatDT.INSTANCE) = { 
    this(v, v, dataType)
  }
  def this(d: Domain) = { 
    this(d.lowerBound, d.upperBound, d.dataType)
  }

  override def isFinite = (isSingleton && areBoundsFinite) || isEmpty
  override def isSingleton = compareEqual(lowerBound, upperBound)

  override def getSingletonValue: Option[Double] = if(lowerBound == upperBound) Some(upperBound) else None

  override def set(v: Double): Unit = { 
    if(isMember(v)) { 
      if(lowerBound != v && upperBound != v) { 
        lowerBound = v
        upperBound = v
        notifyChange(DomainListener.SET_TO_SINGLETON)
      }
    }
    else { 
      empty
    }
  }
  override def reset(d: Domain): Unit = { 
    //safeComparison(this, d)
    if(this != d) { 
      relax(d)
      notifyChange(DomainListener.RESET)
    }
  }
  override def intersect(d: Domain): Boolean = { 
    //safeComparison(this, d)
    checkError(d.isOpen || !d.isEmpty, d.toString)
    checkError(isOpen || !isEmpty, toString)
    return intersect(d.lowerBound, d.upperBound)
  }
  override def intersect(lb: Double, ub: Double): Boolean = { 
    if(lt(ub, lb) || lt(ub, lowerBound) || lt (upperBound, lb)) { 
      empty
      return true;
    }
    val ub_decreased = if(lt(ub, upperBound)) {upperBound = ub; true} else false
    val lb_increased = if(lt(lowerBound, lb)) {lowerBound = lb; true} else false
    if((lb_increased || ub_decreased) && isSingleton) notifyChange(DomainListener.RESTRICT_TO_SINGLETON)
    else if(lb_increased && ub_decreased) { 
      notifyChange(if(isEmpty) DomainListener.EMPTIED else DomainListener.BOUNDS_RESTRICTED)
    }
    else { 
      if(lb_increased) notifyChange(DomainListener.LOWER_BOUND_INCREASED)
      else if(ub_decreased) notifyChange(DomainListener.UPPER_BOUND_DECREASED)
    }
    return lb_increased || ub_decreased
  }
  override def difference(d: Domain): Boolean = { 
    checkError(d.isOpen || !d.isEmpty, d)
    checkError(isOpen || !isEmpty, this)
    checkError(!(lt(lowerBound, d.lowerBound) && lt(d.getUpperBound, upperBound)), "Intersecting ", this, " with ", 
               d, " would split the domain.")
    if(lt(d.upperBound, lowerBound) || lt(upperBound, d.lowerBound)) return false
    if(leq(d.lowerBound, lowerBound) && leq(upperBound, d.upperBound)) { empty; return true}
    if(leq(d.lowerBound, lowerBound)) { 
      lowerBound = d.upperBound + minDelta
      notifyChange(DomainListener.LOWER_BOUND_INCREASED)
    }
    if(leq(upperBound, d.upperBound)) { 
      upperBound = d.lowerBound - minDelta
      notifyChange(DomainListener.UPPER_BOUND_DECREASED)
    }
    return true
  }
  override def empty: Unit = { 
    upperBound = -2
    lowerBound = 2 + minDelta
    notifyChange(DomainListener.EMPTIED)
  }
  override def relax(d: Domain): Unit = relax(d.lowerBound, d.upperBound)
  override def relax(v: Double): Unit = { 
    val wasEmpty = isEmpty
    lowerBound = v
    upperBound = v
    if(wasEmpty) notifyChange(DomainListener.RELAXED)
  }
  def relax(lb: Double, ub: Double): Boolean = { 
    checkError(leq(lb, ub), "Tried to relax to an empty domain")
    return if(lt(upperBound, ub) || lt(lb, lowerBound)) { 
      lowerBound = lb
      upperBound = ub
      true
    }
    else false
  }
  override def insert(v: Double): Unit = { checkError(ALWAYS_FAIL, "Can't insert into an interval domain")}
  override def remove(v: Double): Unit = { 
    checkError(check_value(v), "Not a valid value for a domain of type ", dataType)
    if(isMember(v)) { 
      if(compareEqual(v, lowerBound)) { 
        lowerBound = lowerBound + minDelta
        notifyChange(if(isEmpty) DomainListener.EMPTIED else DomainListener.LOWER_BOUND_INCREASED)
      }
      else if(compareEqual(v, upperBound)) { 
        upperBound = upperBound - minDelta
        notifyChange(if(isEmpty) DomainListener.EMPTIED else DomainListener.UPPER_BOUND_DECREASED)
      }
      else checkError(ALWAYS_FAIL, "Attempted to remove an element from within the interval.  Would require splitting.")
    }
  }
  override def isMember(v: Double): Boolean = leq(lowerBound, v) && leq(v, upperBound)

  override def isEnumerated = false
  override def isInterval = true
  override def isEmpty: Boolean = lt(upperBound, lowerBound)
  override def testPrecision(v: Double): Unit = { }
  override def convertToMemberValue(s: String): Option[Double] = { 
    var v: Double = 0
    try { v = java.lang.Double.parseDouble(s) }
    catch { case _ => return None }
    return if(isMember(v)) Some(v) else None
  }
  override def intersects(d: Domain): Boolean = { 
    checkError(!isOpen, this)
    checkError(!d.isEmpty, d)
    return !(lt(d.upperBound, lowerBound) || lt(upperBound, d.lowerBound))
  }
  override def copy: Domain = new IntervalDomain(this)
  override def isSubsetOf(d: Domain): Boolean = { 
    checkError(!isOpen, this)
    checkError(!d.isEmpty, d)
    return (isFinite || d.isInfinite) && leq(upperBound, d.upperBound) && leq(d.lowerBound, lowerBound)
  }
  override def getValues: List[Double] = (lowerBound to upperBound by minDelta).toList
  override def size: Option[Int] = { 
    return if(isEmpty) Some(0)
           else if(isSingleton) Some(1)
           else if(isFinite) Some((upperBound - lowerBound).toInt + 1)
           else None
  }
  override def equate(d: Domain): Boolean = { 
    return if(d.isEnumerated) d.equate(this)
           else intersect(d) && !isEmpty && d.intersect(this)
  }
}

class IntervalIntDomain(dataType: DataType = IntDT.INSTANCE) extends IntervalDomain(dataType) { 

  def this(lowerBound: Double, upperBound: Double, dataType: DataType = IntDT.INSTANCE) = { 
    this(dataType)
  }
  def this(v: Double, dataType: DataType = IntDT.INSTANCE) = { 
    this(v, v, dataType)
  }
  def this(d: Domain) = { 
    this(d.lowerBound, d.upperBound, d.dataType)
  }
  override def isSingleton: Boolean = lowerBound == upperBound
  override def isFinite: Boolean = (lowerBound > MINUS_INFINITY && upperBound < PLUS_INFINITY)
   
  override def intersect(lb: Double, ub: Double): Boolean = { 
    return super.intersect(ceil(lb), floor(ub))
  }
  override def insert(v: Double): Unit = { 
    checkError(check_value(v), "Invalid value ", v)
    if(!isMember(v)) { 
      if(isEmpty) { 
        lowerBound = v.toInt; upperBound = v.toInt
        if(!isOpen) notifyChange(DomainListener.RELAXED)
      }
      else if(lowerBound - minDelta <= v && v < lowerBound) { 
        lowerBound = v.toInt
        if(!isOpen) notifyChange(DomainListener.RELAXED)
      }
      else if(upperBound < v && v <= upperBound + minDelta) { 
        upperBound = v.toInt
        if(!isOpen) notifyChange(DomainListener.RELAXED)
      }
    }
    checkError(ALWAYS_FAIL, "Shouldn't get here.")
  }
  override def testPrecision(v: Double): Unit = { 
    checkError(v == v.toInt, v, " must be an integer")
  }
  override def translateNumber(v: Double, asMin: Boolean = true): Double = if(asMin) floor(v) else round(v)
  override def copy: Domain = new IntervalIntDomain(this)
  
}

class BoolDomain(dataType: DataType = BoolDT.INSTANCE) extends IntervalIntDomain(0, 1, dataType) { 

  def this(v: Double, dataType: DataType = IntDT.INSTANCE) = { 
    this(dataType)
    lowerBound = v
    upperBound = v
  }
  def this(d: Domain) = { 
    this(d.dataType)
    checkError(d.isBool, "Tried to construct a boolean from a non-boolean domain")
    lowerBound = d.lowerBound
    upperBound = d.upperBound
  }
  override def isFinite: Boolean = true
  def isTrue: Boolean = lowerBound == 1 && upperBound == 1
  def isFalse: Boolean = lowerBound == 0 && upperBound == 0
   
  override def intersect(lb: Double, ub: Double): Boolean = { 
    return super.intersect(translateNumber(lb), translateNumber(ub))
  }
  override def testPrecision(v: Double): Unit = { 
    checkError(v == v.toInt, v, " must be an integer")
  }
  override def copy: Domain = new BoolDomain(this)
  override def translateNumber(v: Double, asMin: Boolean = true): Double = if(v == 0.0) 0.0 else 1.0
}

