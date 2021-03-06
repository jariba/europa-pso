package gov.nasa.arc.europa.constraintengine.component
import gov.nasa.arc.europa.constraintengine.DataType
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.utils.Debug._
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
  def apply(v: Double) = new IntervalDomain(v, v, FloatDT.INSTANCE)
  //implicit def IntervalEqual: Equal[IntervalDomain] = equalBy(_.getBounds)
}
class IntervalDomain(dataType: DataType = FloatDT.INSTANCE) extends Domain(dataType, true) { 
 var lowerBound: Double = MINUS_INFINITY
 var upperBound: Double = PLUS_INFINITY

  def this(lowerBound: Double, upperBound: Double, dataType: DataType = FloatDT.INSTANCE) = { 
    this(dataType)
    this.lowerBound = lowerBound
    this.upperBound = upperBound
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
      if(lowerBound != v || upperBound != v) { 
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
    checkError(!(lt(lowerBound, d.lowerBound) && lt(d.getUpperBound, upperBound)),
               "Removing ", d, " from ", this, " would split the domain.")

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
      notifyChange(DomainListener.RELAXED)
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
  def convert(v: Double): Double = v
  override def isMember(v: Double): Boolean = { 
    convert(v) == v && leq(lowerBound, v) && leq(v, upperBound)
  }

  override def isEnumerated = false
  override def isInterval = true
  override def isEmpty: Boolean = lt(upperBound, lowerBound)
  override def testPrecision(v: Double): Unit = { }
  override def convertToMemberValue(s: String): Option[Double] = { 
    var v: Double = 0
    try { v = java.lang.Double.parseDouble(s) }
    catch { case _ : Throwable => return None }
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
    debugMsg("IntervalDomain:equate", "Equating ", this, " with ", d)
    return if(d.isEnumerated) d.equate(this)
           else intersect(d) | ((!isEmpty) && d.intersect(this))
  }

  override def toString: String = (new StringBuilder) append typeString append "[" append lowerBound append " " append upperBound append "]" toString

  override def :=(d: Domain): Domain = {
//    safeComparison(this, d)
    checkError(listener == null, "")
    lowerBound = d.getLowerBound
    upperBound = d.getUpperBound
//    closed = d.isFinite
    this
  }

  override def eq(other: Domain): Boolean = eq(lowerBound, other.lowerBound) && eq(upperBound, other.upperBound)
}

object IntervalIntDomain { 
  def apply() = new IntervalIntDomain(IntDT.INSTANCE)
  def apply(dataType: DataType) = new IntervalIntDomain(dataType)
  def apply(value: Int) = new IntervalIntDomain(value, value, IntDT.INSTANCE)
  def apply(lowerBound: Int, upperBound: Int) = 
    new IntervalIntDomain(lowerBound, upperBound, IntDT.INSTANCE)
  def apply(lowerBound: Int, upperBound: Int, dataType: DataType) = 
    new IntervalIntDomain(lowerBound, upperBound, dataType)

//  implicit def IntervalIntEqual: Equal[IntervalIntDomain] = equalBy(_.getBounds)
}

class IntervalIntDomain(dataType: DataType = IntDT.INSTANCE) extends IntervalDomain(dataType) { 

  def this(lowerBound: Double, upperBound: Double, dataType: DataType = IntDT.INSTANCE) = { 
    this(dataType)
    this.lowerBound = lowerBound
    this.upperBound = upperBound
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
  override def translateNumber(v: Double, asMin: Boolean = true): Double = { //if(asMin) floor(v) else round(v)
    if(v == PLUS_INFINITY || v == MINUS_INFINITY)
      return v
    val result = super.translateNumber(v.toInt, asMin);
    if(abs(result - v) >= FloatDT.INSTANCE.minDelta && asMin && v > 0) result + 1
    else result
  }
  override def copy: Domain = new IntervalIntDomain(this)
  override def toString: String = (new StringBuilder) append super.typeString append "[" append lowerBound.toInt append " " append upperBound.toInt append "]" toString
  override def convert(v: Double): Double = v.toInt.toDouble
}

object BoolDomain { 
  def apply() = new BoolDomain(BoolDT.INSTANCE)
  def apply(dataType: DataType) = new BoolDomain(dataType)
  def apply(value: Boolean) = 
    new BoolDomain(value, BoolDT.INSTANCE)

  // implicit def BoolEqual: Equal[BoolDomain] = equalBy(_.getBounds)
}

class BoolDomain(dataType: DataType = BoolDT.INSTANCE) extends IntervalIntDomain(0, 1, dataType) { 

  def this(lb: Double, ub: Double, dataType: DataType = BoolDT.INSTANCE) = { 
    this(dataType)
    lowerBound = lb
    upperBound = ub
  }
  def this(v: Double, dataType: DataType = BoolDT.INSTANCE) = { 
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
  def this(b: Boolean, dataType: DataType) = { 
    this(dataType)
    lowerBound = translateToNumber(b)
    upperBound = translateToNumber(b)
  }
  def this(b: Boolean) { 
    this(b, BoolDT.INSTANCE)
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
  def translateToNumber(b: Boolean): Double = if(b) 1.0 else 0.0
  def set(b: Boolean): Unit = set(translateToNumber(b))
  def toString(b: Boolean): String = b.toString
  def isMember(b: Boolean): Boolean = isMember(translateToNumber(b))
  def remove(b: Boolean): Unit = if(b) upperBound = 0 else lowerBound = 1
}
//TODO: StringDomain
