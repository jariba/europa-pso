package gov.nasa.arc.europa.constraintengine.component
import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.ConstraintType
import gov.nasa.arc.europa.constraintengine.DataType
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.Infinity
import gov.nasa.arc.europa.utils.LabelStr
import gov.nasa.arc.europa.utils.Number._

import scala.math._

class AbsoluteValue(name: LabelStr, pName: LabelStr, ce: ConstraintEngine, vars: Seq[ConstrainedVariable]) extends Constraint(name, pName, ce, vars) { 
  val x: IntervalDomain = vars(0).getCurrentDomain.asInstanceOf[IntervalDomain]
  val y: IntervalDomain = vars(1).getCurrentDomain.asInstanceOf[IntervalDomain]
  override def handleExecute: Unit = { 
    if(y.lowerBound >= 0) x.intersect(y)
    else x.intersect(if(y.upperBound >= 0) 0.0 else min(abs(y.lowerBound), abs(y.upperBound)),
                     max(abs(y.lowerBound), y.upperBound))

    if(x.lowerBound == 0 && y.isMember(-x.upperBound) && y.isMember(x.upperBound))
      y.intersect(-x.upperBound, x.upperBound)
    if((y.isMember(x.lowerBound) || y.isMember(x.upperBound)) &&
       (y.isMember(-x.lowerBound) || y.isMember(-x.upperBound)))
      return;
    if(y.isMember(x.lowerBound) || y.isMember(x.upperBound))
      y.intersect(x)

    if(y.isMember(-x.lowerBound) || y.isMember(-x.upperBound))
      y.intersect(-x.upperBound, -x.lowerBound)
  }
}

class AbsoluteValueType(name: LabelStr, propagatorName: LabelStr, systemDefined: Boolean) 
extends ConstraintType(name, propagatorName, systemDefined) with TwoArgs with AllNumeric with FirstAssignable  { 
  override def createConstraint(engine: ConstraintEngine, scope: Seq[ConstrainedVariable], 
                                violationExpl: String): Constraint = { 
    ConstraintType.make[AbsoluteValue](name, propagatorName, engine, scope, violationExpl);
  }
}

class AddEqual(name: LabelStr, pName: LabelStr, ce: ConstraintEngine, vars: Seq[ConstrainedVariable]) extends Constraint(name, pName, ce, vars) { 
  val x: Domain = vars(0).getCurrentDomain
  val y: Domain = vars(1).getCurrentDomain
  val z: Domain = vars(2).getCurrentDomain

  override def handleExecute(): Unit = { 
     // Test preconditions for continued execution.
    if (x.isOpen ||
        y.isOpen ||
        z.isOpen)
      return

    var (xMin: Double, xMax: Double) = x.getBounds
    var (yMin: Double, yMax: Double) = y.getBounds
    var (zMin: Double, zMax: Double) = z.getBounds

    // Process Z
    val xMax_plus_yMax = Infinity.plus(xMax, yMax, zMax);
    if (zMax > xMax_plus_yMax)
      zMax = z.translateNumber(xMax_plus_yMax, false);

    val xMin_plus_yMin = Infinity.plus(xMin, yMin, zMin);
    if (zMin < xMin_plus_yMin)
      zMin = z.translateNumber(xMin_plus_yMin, true);

    if (z.intersect(zMin, zMax) && z.isEmpty)
      return;

    // Process X
    val zMax_minus_yMin = Infinity.minus(zMax, yMin, xMax);
    if (xMax > zMax_minus_yMin)
      xMax = x.translateNumber(zMax_minus_yMin, false);

    val zMin_minus_yMax = Infinity.minus(zMin, yMax, xMin);
    if (xMin < zMin_minus_yMax)
      xMin = x.translateNumber(zMin_minus_yMax, true);

    if (x.intersect(xMin, xMax) && x.isEmpty)
      return;

    // Process Y
    val yMaxCandidate = Infinity.minus(zMax, xMin, yMax);
    if (yMax > yMaxCandidate)
      yMax = y.translateNumber(yMaxCandidate, false);

    val yMinCandidate = Infinity.minus(zMin, xMax, yMin);
    if (yMin < yMinCandidate)
      yMin = y.translateNumber(yMinCandidate, true);

    if (y.intersect(yMin,yMax) && y.isEmpty)
      return;


    /* Now, rounding issues from mixed numeric types can lead to the
     * following inconsistency, not yet caught.  We handle it here
     * however, by emptying the domain if the invariant post-condition
     * is not satisfied. The motivating case for this: A:Int[-10,10] +
     * B:Int[-10,10] == C:Real[0.01, 0.99].
     */
    if (z.isInterval &&
	(!z.isMember(Infinity.plus(yMax, xMin, zMin)) ||
	 !z.isMember(Infinity.plus(yMin, xMax, zMin))))
      z.empty;
 }
}

class AddEqualType(name: LabelStr, propagatorName: LabelStr, systemDefined: Boolean) 
extends ConstraintType(name, propagatorName, systemDefined) with ThreeArgs with AllNumeric with LastAssignable { 
  override def createConstraint(engine: ConstraintEngine, scope: Seq[ConstrainedVariable], 
                                violationExpl: String): Constraint = { 
    ConstraintType.make[AddEqual](name, propagatorName, engine, scope, violationExpl);
  }
}

class EqualConstraint(name: LabelStr, pName: LabelStr, ce: ConstraintEngine, vars: Seq[ConstrainedVariable]) 
extends Constraint(name, pName, ce, vars) {
  override def handleExecute: Unit = {

    var changed = false

    for(i <- 1 until vars.length) {
      val v1 = vars(i - 1)
      val v2 = vars(i)
      val (tempChanged, isEmpty) = equate(v1, v2)
      changed = changed || tempChanged;
      if(isEmpty) return
    }

    //if the previous process changed the domains, we need to make sure that
    //the change occurs everywhere.  fortunately, since the n-1st variable
    //is now equal to the intersection of all of the variables,
    //we can just equate backwards and they should all be equal
    if(changed && vars.length > 2) {
      for(i <- (vars.length - 2) to 1 by -1) { 
        val v1 = vars(i)
	val v2 = vars(i-1)

	val(tempChanged, isEmpty) = equate(v1, v2)
	if(isEmpty) return;
      }
    }
  }
 
  def equate(v1: ConstrainedVariable, v2: ConstrainedVariable): (Boolean, Boolean) = {
    val d1 = v1.getCurrentDomain
    val d2 = v2.getCurrentDomain

    var changed = false
    var isEmpty = false
    if((d1.isClosed && d2.isClosed) || (d1.isEnumerated && d2.isEnumerated)){
      debugMsg("EqualConstraint:equate","before equate " , v1.toString , " --- " , v2.toString);
      changed = d1.equate(d2);
      if(changed && (d1.isEmpty || d2.isEmpty)) {
	debugMsg("EqualConstraint:equate","emptied variable " , v1.toString , " --- " , v2.toString);
	isEmpty = true;
      }
    }
    else {
      checkError(!d1.isInterval && !d2.isInterval,
		 v1.toString , " should not be equated with " , v2.toString);

      var d1_values = d1.getValues
      val d2_base = v2.baseDomain
      while(!isEmpty && !d1_values.isEmpty){
	// if it not a member of d2 BUT a member of the base domain of v2, then we should exclude from d1.
        val value = d1_values.head;
	if(!d2.isMember(value) && (d2_base.isMember(value) || d2.isClosed)){
	  d1.remove(value);
	  changed = true;
	  isEmpty = d1.isEmpty;
	}
	d1_values = d1_values.tail;
      }

      if(!isEmpty){
	var d2_values = d2.getValues
	val d1_base = v1.baseDomain
	while(!isEmpty && !d2_values.isEmpty){
	  // if it not a member of d2 BUT a member of the base domain of v2, then we should exclude from d1.
	  val value = d2_values.head;
	  if(!d1.isMember(value) && (d1_base.isMember(value) || d1.isClosed)){
	    d2.remove(value);
	    changed = true;
	    isEmpty = d2.isEmpty;
	  }
	  d2_values = d2_values.tail
	}
      }
    }
    return (changed, isEmpty);
  }
}

class EqualConstraintType(name: LabelStr, propagatorName: LabelStr, systemDefined: Boolean) 
extends ConstraintType(name, propagatorName, systemDefined) with SameArgs { 
  override def createConstraint(engine: ConstraintEngine, scope: Seq[ConstrainedVariable], 
                                violationExpl: String): Constraint = { 
    ConstraintType.make[EqualConstraint](name, propagatorName, engine, scope, violationExpl);
  }
}

class LessThanEqualConstraint(name: LabelStr, pName: LabelStr, ce: ConstraintEngine, vars: Seq[ConstrainedVariable]) 
extends Constraint(name, pName, ce, vars) {
  val x = vars(0)
  val y = vars(1)

  override def handleExecute: Unit = {
    LessThanEqualConstraint.propagate(x.getCurrentDomain.asInstanceOf[IntervalDomain],
                                      y.getCurrentDomain.asInstanceOf[IntervalDomain])
  }

  override def canIgnore(variable: ConstrainedVariable, argIndex: Int, 
                         changeType: DomainListener.ChangeType): Boolean = { 
    if(changeType==DomainListener.RESET || changeType == DomainListener.RELAXED)
      return false;

    val domain = variable.lastDomain;

    if(domain.isSingleton ||
       (domain.isInterval && domain.isFinite && domain.size.get <= 2 )) // Since this transition is key for propagation
      return false;

    return true;
  }

  override def testIsRedundant(variable: Option[ConstrainedVariable]): Boolean = { 
    super.testIsRedundant(variable) ||
    getScope(0).baseDomain.getUpperBound <= getScope(1).baseDomain.getLowerBound
  }
}

object LessThanEqualConstraint { 
  def propagate(domx: IntervalDomain, domy: IntervalDomain): Unit = { 
    checkError(domx.getDataType.canBeCompared(domy.getDataType),
               "Cannot compare " , domx.toString , " and " , domy.toString , ".");

    // Discontinue if either domain is open.
    if (domx.isOpen || domy.isOpen)
      return;

    debugMsg("LessThanConstraint:handleExecute", "Computing " , domx.toString , " < " , domy.toString , " x.minDelta = " ,
	     domx.minDelta , " y.minDelta = " , domy.minDelta);
    if(domx.getUpperBound >= domy.getUpperBound &&
       domy.getUpperBound < PLUS_INFINITY &&
       domx.intersect(domx.getLowerBound, domy.getUpperBound - domx.minDelta) &&
       domx.isEmpty)
      return;

    if(domy.getLowerBound <= domx.getLowerBound &&
       domx.getLowerBound > MINUS_INFINITY &&
       domy.intersect(domx.getLowerBound + domy.minDelta, domy.getUpperBound) &&
       domy.isEmpty)
      return;

    // Special handling for singletons, which could be infinite
    if(domx.isSingleton && domy.isSingleton && domx.getSingletonValue.get >= domy.getSingletonValue.get){
      domx.empty;
      return;
    }
  }
}

class LessThanEqualConstraintType(name: LabelStr, propagatorName: LabelStr, systemDefined: Boolean) 
extends ConstraintType(name, propagatorName, systemDefined) with SameArgs with AllNumeric with TwoArgs { 
  override def createConstraint(engine: ConstraintEngine, scope: Seq[ConstrainedVariable], 
                                violationExpl: String): Constraint = { 
    ConstraintType.make[LessThanEqualConstraint](name, propagatorName, engine, scope, violationExpl);
  }
}


class NotEqualConstraint(name: LabelStr, pName: LabelStr, ce: ConstraintEngine, vars: Seq[ConstrainedVariable]) 
extends Constraint(name, pName, ce, vars) {
  override def handleExecute: Unit = { 
    val domx = vars(0).getCurrentDomain
    val domy = vars(1).getCurrentDomain

    // Discontinue if either domain is open.
    //if (domx.isOpen() || domy.isOpen())
    //  return;
    checkError(!domx.isEmpty && !domy.isEmpty, "");

    if (!NotEqualConstraint.checkAndRemove(domx, domy))
      NotEqualConstraint.checkAndRemove(domy, domx);
  }
  override def canIgnore(variable: ConstrainedVariable, argIndex: Int, 
                         changeType: DomainListener.ChangeType): Boolean = { 
    if(changeType==DomainListener.RESET || changeType == DomainListener.RELAXED)
      return false;

    val domain = variable.lastDomain;

    if(domain.isSingleton ||
       (domain.isInterval && domain.isFinite && domain.size.get <=2 )) // Since this transition is key for propagation
       return false;

    return true;

  }
}

object NotEqualConstraint { 
  def checkAndRemove(domx: Domain, domy: Domain): Boolean = { 
    if (!domx.isSingleton)
      return(false);
    val value = domx.getSingletonValue.get;
    // Not present, so nothing to remove.
    if (!domy.isMember(value))
      return(false);
    // If enumerated, remove it and be done with it.
    if (domy.isEnumerated) {
      domy.remove(value);
      return(true);
    }
    // Since it is an interval, and it does contain the value, empty it if a singleton.
    if (domy.isSingleton) {
	domy.empty;
	return(true);
    }
    // If it is a Boolean domain then set it to be the alternate
    if (domy.isBool) {
      domy.set(if(value == 0) 1.0 else 0.0);
      return(true);
    }

    if (domx.compareEqual(domx.getSingletonValue.get, domy.getLowerBound)) {
      val low = domx.getSingletonValue.get + domx.minDelta;
      domy.intersect(IntervalDomain(low, domy.getUpperBound));
      return(true);
    }
    if (domx.compareEqual(domx.getSingletonValue.get, domy.getUpperBound)) {
      val hi = domx.getSingletonValue.get - domx.minDelta;
      domy.intersect(IntervalDomain(domy.getLowerBound, hi));
      return(true);
    }
    /** COULD SPECIAL CASE INTERVAL INT DOMAIN, BUT NOT WORTH IT PROBABLY **/
    // Otherwise, we would have to split the interval, so do not propagate it
    return(false);

  }
}

class NotEqualConstraintType(name: LabelStr, propagatorName: LabelStr, systemDefined: Boolean) 
extends ConstraintType(name, propagatorName, systemDefined) with SameArgs with TwoArgs { 
  override def createConstraint(engine: ConstraintEngine, scope: Seq[ConstrainedVariable], 
                                violationExpl: String): Constraint = { 
    ConstraintType.make[NotEqualConstraint](name, propagatorName, engine, scope, violationExpl);
  }
}
