package gov.nasa.arc.europa.constraintengine.component

import gov.nasa.arc.europa.constraintengine.DataType
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.Domain._
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr
import scala.collection.SortedSet

object EnumeratedDomain { 
  def apply(v: Set[Double], dataType: DataType) = new EnumeratedDomain(v, dataType)
  def apply(dataType: DataType) = new EnumeratedDomain(dataType)
}

class EnumeratedDomain(dataType: DataType) extends Domain(dataType, true) { 
import scalaz.syntax.equal._

  var values: SortedSet[Double] = SortedSet()

  // def this(v: Seq[Double], dataType: DataType = FloatDT.INSTANCE) = { 
  //   this(dataType)
  //   values = values ++ v
  // }
  def this(v: Set[Double], dataType: DataType = FloatDT.INSTANCE) = { 
    this(dataType)
    values = values ++ v
  }

  //TODO: comment this back in and figure out WTH is going on with the error.
  // def this(v: Double, dataType: DataType = FloatDT.INSTANCE) = { 
  //   this(dataType)
  //   values = values + v
  // }
  def this(d: Domain) = { 
    this(d.getValues.toSet, d.dataType)
  }

  override def lowerBound: Double = values.min
  override def upperBound: Double = values.max

  override def isFinite = true
  override def isSingleton = values.size == 1
  override def getSingletonValue: Option[Double] = if(isSingleton) values.headOption else None
  override def set(v: Double): Unit = { 
    if(isMember(v)) { 
      values = SortedSet(v)
      notifyChange(DomainListener.SET_TO_SINGLETON)
    }
    else
      empty
  }

  override def reset(d: Domain): Unit = { 
    if(!(this.asInstanceOf[Domain] ≟ d)) { 
      relax(d)
      notifyChange(DomainListener.RESET)
    }
  }

  override def intersect(d: Domain): Boolean = { 
    //safeComparison(this, d)
    val initialSize = values.size
    //seriously truncated from original source, for good or ill
    values = values.filter(d.isMember(_))
    if(initialSize == values.size) return false
    if(isEmpty) notifyChange(DomainListener.EMPTIED)
    else if(isSingleton) notifyChange(DomainListener.RESTRICT_TO_SINGLETON)
    else notifyChange(DomainListener.VALUE_REMOVED)
    return true
  }

  override def intersect(lb: Double, ub: Double): Boolean = { 
    if(lt(ub, lb)) { 
      empty
      return true
    }
    return intersect(new IntervalDomain(lb, ub, dataType))
  }
  
  override def difference(d: Domain): Boolean = { 
    val initialSize = values.size
    values  = values.filter(!d.isMember(_))
    if(isEmpty) notifyChange(DomainListener.EMPTIED)
    else if(isSingleton) notifyChange(DomainListener.RESTRICT_TO_SINGLETON) //added from original source
    else if(initialSize < values.size) notifyChange(DomainListener.VALUE_REMOVED)
    return initialSize != values.size
  }

  override def empty: Unit = { 
    values = SortedSet()
    notifyChange(DomainListener.EMPTIED)
  }

  override def relax(d: Domain): Unit = { 
    checkError(d.isEnumerated, "")
    if(isSubsetOf(d)) { 
      val dom = d.asInstanceOf[EnumeratedDomain]
      values = dom.values
      notifyChange(DomainListener.RELAXED)
    }
  }

  override def relax(v: Double): Unit = { 
    checkError(isEmpty || (isSingleton && getSingletonValue.get == v), toString)
    if(isEmpty) { 
      values = values + v
      notifyChange(DomainListener.RELAXED)
    }
  }

  override def insert(v: Double): Unit = { 
    checkError(ALWAYS_FAIL, "Can't insert into any domain.")
  }

  override def remove(v: Double): Unit = { 
    checkError(check_value(v), "Value ", v, " not in ", toString)
    if(isMember(v)) { 
      // values = values - v
      values = values.filterNot((x) => scala.math.abs(v - x) < minDelta)
      if(isEmpty) notifyChange(DomainListener.EMPTIED)
      else notifyChange(DomainListener.VALUE_REMOVED)
    }
  }

  override def isMember(v: Double): Boolean = { 
    return values.filter(compareEqual(_, v)).nonEmpty
  }

  override def isEnumerated = true
  override def isInterval = false
  override def isEmpty: Boolean = !values.nonEmpty
  
  override def convertToMemberValue(s: String): Option[Double] = { 
    var v: Double = 0
    try { v = java.lang.Double.parseDouble(s) }
    catch { case _ : Throwable => return None }
    return if(isMember(v)) Some(v) else None
  }

  override def intersects(d: Domain): Boolean = { 
    //safeComparison(this, dom)
    values.exists(d.isMember(_))
  }

  override def isSubsetOf(d: Domain): Boolean = { 
    !values.exists(!d.isMember(_))
  }

  override def getValues: List[Double] = values.toList

  override def size: Option[Int] = Some(values.size)

  override def equate(d: Domain): Boolean = { 
    //safeComparison(*this, dom)
    val otherChanged = d.intersect(this)
    if(otherChanged && d.isEmpty) return true
    if(intersect(d) && !isEmpty) return d.intersect(this) || otherChanged
    return otherChanged
  }

  override def toString: String = (new StringBuilder) append typeString append "{" append values.mkString(",") append "}" toString

  override def :=(d: Domain): Domain = { 
    //safeComparison(this, d)
    checkError(listener == null, "")
    values = d.asInstanceOf[EnumeratedDomain].values
    this
  }

  override def eq(other: Domain): Boolean = { 
    //safeComparison(this, other)
    if(other.isInterval) return other.isFinite && size == other.size && isSubsetOf(other)
    val dom = other.asInstanceOf[EnumeratedDomain]
    if(this == dom) return true
    values.filter(!dom.isMember(_)).isEmpty && dom.values.filter(!isMember(_)).isEmpty
  }
  override def testPrecision(v: Double): Unit = { }

  override def copy: Domain = new EnumeratedDomain(this)
}

object NumericDomain { 
  def apply() = new NumericDomain(FloatDT.INSTANCE)
  def apply(dataType: DataType) = new NumericDomain(dataType)
  //def apply(v: Double) = new NumericDomain(Set(v))
  def apply(s: Set[Double]) = new NumericDomain(s)
  def apply(r: Range) = new NumericDomain(r.toSet.map((x: Int) => x.toDouble))
  def apply(d: Domain) = new NumericDomain(d)
  def apply(v: Double*) = new NumericDomain(v.toSet)
}
class NumericDomain(dataType: DataType) extends EnumeratedDomain(dataType) { 
    checkError(dataType.isNumeric, "Tried to construct a numeric domain with non-numeric datatype ", dataType.toString())


  def this(v: Set[Double], dataType: DataType = FloatDT.INSTANCE) = { 
    this(dataType)
    values = values ++ v
  }
  // def this(v: Double, dataType: DataType = FloatDT.INSTANCE) = { 
  //   this(dataType)
  //   values = values + v
  // }
  def this(d: Domain) = { 
    this(d.getValues.toSet, d.dataType)
  }
  override def copy: Domain = new NumericDomain(this)
}

object SymbolicDomain { 
  def apply() = new SymbolicDomain(SymbolDT.INSTANCE)
  def apply(dataType: DataType) = new SymbolicDomain(dataType)
  //def apply(v: Double) = new SymbolicDomain(Set(v))
  def apply(s: Set[Double]) = new SymbolicDomain(s)
  def apply(r: Range) = new SymbolicDomain(r.toSet.map((x: Int) => x.toDouble))
  def apply(d: Domain) = new SymbolicDomain(d)
  def apply(v: String*) = new SymbolicDomain(v.map(LabelStr(_).key.toDouble).toSet)
  implicit def strSet2Double(s: Set[String]): Set[Double] = s.map(LabelStr(_).key.toDouble)
  implicit def labelStrSet2Double(s: Set[LabelStr]): Set[Double] = s.map(_.key.toDouble)
}

class SymbolicDomain(dataType: DataType = SymbolDT.INSTANCE) extends EnumeratedDomain(dataType) { 
  checkError(dataType.isSymbolic, "Tried to construct a symbolic domain with non-symbolic datatype ", dataType.toString())

  def this(v: Set[Double], dataType: DataType = SymbolDT.INSTANCE) = { 
    this(dataType)

    values = values ++ v
  }
  // def this(v: Double, dataType: DataType = FloatDT.INSTANCE) = { 
  //   this(dataType)
  //   values = values + v
  // }
  def this(d: Domain) = { 
    this(d.getValues.toSet, d.dataType)
  }
  override def copy: Domain = new SymbolicDomain(this)

  override def convertToMemberValue(s: String): Option[Double] = { 
    val asDouble = LabelStr(s).key.toDouble
    if(values.contains(asDouble)) Some(asDouble) else None
  }
}


