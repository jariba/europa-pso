package gov.nasa.arc.europa.constraintengine.component
import gov.nasa.arc.europa.constraintengine.ConstraintTypeChecker

import scalaz._
import Scalaz._

import gov.nasa.arc.europa.constraintengine.ConstraintType
import gov.nasa.arc.europa.constraintengine.DataType

trait TwoArgs extends ConstraintTypeChecker { 
  override def check(name: String, argTypes: Seq[DataType]): Option[String] = { 
    BaseConstraintTypes.concat(super.check(name, argTypes), 
                               BaseConstraintTypes.requireArgCount(name, argTypes, 2))
  }
}

trait ThreeArgs extends ConstraintTypeChecker { 
  override def check(name: String, argTypes: Seq[DataType]): Option[String] = { 
    BaseConstraintTypes.concat(super.check(name, argTypes), 
                               BaseConstraintTypes.requireArgCount(name, argTypes, 3))
  }
}

trait SameArgs extends ConstraintTypeChecker { 
  override def check(name: String, argTypes: Seq[DataType]): Option[String] = { 
    BaseConstraintTypes.concat(super.check(name, argTypes), 
                               BaseConstraintTypes.requireAllSame(name, argTypes))
  }
}

trait AllNumeric extends ConstraintTypeChecker { 
  override def check(name: String, argTypes: Seq[DataType]): Option[String] = { 
    BaseConstraintTypes.concat(super.check(name, argTypes), 
                               BaseConstraintTypes.requireAll(name, argTypes, BaseConstraintTypes.requireNumeric))

  }
}

trait AllBoolean extends ConstraintTypeChecker { 
  override def check(name: String, argTypes: Seq[DataType]): Option[String] = { 
    BaseConstraintTypes.concat(super.check(name, argTypes), 
                               BaseConstraintTypes.requireAll(name, argTypes, BaseConstraintTypes.requireBoolean))

  }
}

trait FirstAssignable extends ConstraintTypeChecker { 
  override def check(name: String, argTypes: Seq[DataType]): Option[String] = { 
    BaseConstraintTypes.concat(super.check(name, argTypes),
                               BaseConstraintTypes.requireAll(name, argTypes.tail, BaseConstraintTypes.assignable(_:String, argTypes.head, _:DataType)))
  }
}

trait LastAssignable extends ConstraintTypeChecker { 
  override def check(name: String, argTypes: Seq[DataType]): Option[String] = { 
    BaseConstraintTypes.concat(super.check(name, argTypes),
                               BaseConstraintTypes.requireAll(name, argTypes.init, BaseConstraintTypes.assignable(_:String, argTypes.last, _:DataType)))
  }
}

object BaseConstraintTypes { 
  def concat(s1: Option[String], s2: Option[String]): Option[String] = (s1, s2) match { 
    case (Some(x), Some(y)) => Some(x + y)
    case (Some(x), None) => Some(x)
    case (None, Some(y)) => Some(y)
    case (None, None) => None
  }
  def requireArgCount(name: String, argTypes: Seq[DataType], expected: Int): Option[String] = { 
    if(argTypes.size != expected) Some("Constraint '" + name + "' expects " + expected + " arguments, not " + argTypes.size)
    else None
  }
  def minArgCount(name: String, argTypes: Seq[DataType], expected: Int): Option[String] = { 
    if(argTypes.size < expected) Some("Constraint '" + name + "' expects at least " + expected + " arguments, not " + argTypes.size)
    else None
  }
  def assignable(name: String, a: DataType, b: DataType): Option[String] = { 
    if(!a.isAssignableFrom(b)) Some("Constraint '" + name + "' first arg '" + a.name + " must be assignable from " + b.name) 
    else None
  }
  def mutuallyAssignable(name: String, a: DataType, b: DataType): Option[String] = { 
    if(!b.isAssignableFrom(a) || !a.isAssignableFrom(b)) Some("Constraint '" + name + "' args must be assignable.  " + a.name + " and " + b.name + " aren't.")
    else None
  }
  def require(name: String, a: DataType, f: (DataType) => Boolean, s: String): Option[String] = { 
    if(!f(a)) Some("Constraint '" + name + "' arg isn't " + s + ".  " + a.name)
    else None
  }
  def requireNumeric(name: String, a: DataType): Option[String] = 
    require(name, a, (x: DataType) => x.isNumeric, "numeric")
  def requireBoolean(name: String, a: DataType): Option[String] = 
    require(name, a, (x: DataType) => x.isBool, "numeric")
  def requireAll(name: String, argTypes: Seq[DataType], requirement: (String, DataType) => Option[String]): Option[String] = argTypes.map(requirement(name, _)).fold(None)(concat(_,_))
  def requireAllSame(name: String, argTypes: Seq[DataType]): Option[String] = argTypes match { 
    case Seq(x) => None
    case Seq(x, y) => mutuallyAssignable(name, x, y)
    case Seq(x, xs@_*) => { 
      val thisMap = xs.flatMap(mutuallyAssignable(name, x, _))
      val rest = requireAllSame(name, xs)
      var retval = new StringBuilder()
      retval = thisMap.addString(retval)
      retval = retval.append(rest getOrElse "")
      if(retval.isEmpty) return None
      else return Some(retval.toString)
    }
  }
  
}
