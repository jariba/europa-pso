package gov.nasa.arc.europa.constraintengine.test
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.Domain._
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.constraintengine.component.BoolDomain
import gov.nasa.arc.europa.constraintengine.component.BoolDT
import gov.nasa.arc.europa.constraintengine.component.EnumeratedDomain
import gov.nasa.arc.europa.constraintengine.component.FloatDT
import gov.nasa.arc.europa.constraintengine.component.IntervalDomain
import gov.nasa.arc.europa.constraintengine.component.IntervalDomain._
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain._
import gov.nasa.arc.europa.constraintengine.component.IntDT
import gov.nasa.arc.europa.constraintengine.component.NumericDomain
import gov.nasa.arc.europa.constraintengine.component.SymbolicDomain
import gov.nasa.arc.europa.constraintengine.component.SymbolDT
import gov.nasa.arc.europa.utils.EqualImplicits._
import gov.nasa.arc.europa.utils.Infinity
import gov.nasa.arc.europa.utils.LabelStr
import gov.nasa.arc.europa.utils.LabelStr._
import gov.nasa.arc.europa.utils.Number._

import scalaz.Equal
import scalaz.syntax.equal._

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

trait Foo

class Bar extends Foo { 
  var lb: Double = 0.0
  var ub: Double = 100.0
  def this(lb: Double, ub: Double) = {this(); this.lb = lb; this.ub = ub}
  def getBounds = (lb, ub)
}
object Bar { 
  implicit def BarEqual: Equal[Bar] = Equal.equalBy(_.getBounds)
}

class Baz extends Bar { 
  def this(lb: Int, ub: Int) = { 
    this()
    this.lb = lb; this.ub = ub;
  }
}

class WierdTest extends FunSuite with ShouldMatchers { 

  test("stuff") { 
    val v1 = new Bar()
    val v2 = new Bar(10.0, 20.0)
    val v3 = new Bar(10.0, 20.0)

    (v1 ≟ v1) should equal (true)
    (v2 ≟ v2) should equal (true)
    (v3 ≟ v3) should equal (true)
    (v1 ≟ v2) should equal (false)
    (v2 ≟ v1) should equal (false)
    (v2 ≟ v3) should equal (true)
    (v3 ≟ v2) should equal (true)
  }
}

class ChangeListener extends DomainListener { 
  var changed = false
  var change = DomainListener.RESET
  
  def notifyChange(change: DomainListener.ChangeType): Unit = { 
    changed = true
    this.change = change
  }
  def checkAndClearChange: (Boolean, DomainListener.ChangeType) = { 
    val retval = (changed, change)
    changed = false
    return retval
  }
}

class EnumeratedDomainTest extends FunSuite with ShouldMatchers { 
  ignore("Strings", OpenDomains) {}
  ignore("OpenEnumerations", OpenDomains) {}

  test("EnumerationOnly") {
    val values: Set[Double] = Set(-98.67, -0.01, 1, 2, 10, 11)
    val d0 = NumericDomain(values)
    val d1 = NumericDomain(values);

    d0 ≟ d1 should be (true)

    d0.convertToMemberValue("-0.01").getOrElse(0.0) should be (-0.01)
    d0.convertToMemberValue("88.46").getOrElse(0.0) should be (0.0)

    d0.isSubsetOf(d1) should be (true)
    d1.isSubsetOf(d0) should be (true)
    d0.isMember(-98.67) should be (true)

    d0.remove(-0.01);
    d0.isMember(-0.01) should be (false)
    d0.isSubsetOf(d1) should be (true)
    d1.isSubsetOf(d0) should be (false)
  }

  test("BasicLabelOperations") {
    val initialCount = LabelStr.size
    val dt_l1 = LabelStr("DT_L1");
    val dt_l2 = LabelStr("DT_L2");
    val dt_l3 = LabelStr("DT_L3");
    dt_l1 should be < (dt_l2)
    dt_l2 should be < (dt_l3)

    val la = LabelStr("L");
    val l4 = LabelStr("L30");
    val lb = LabelStr("L");

    la should equal (lb)
    la should be < (l4)

    val copy1 = LabelStr(dt_l1);
    dt_l1 should equal (copy1)
    dt_l2 should not equal (copy1)

    LabelStr.size should equal (initialCount + 5)

    dt_l1.toString() should equal ("DT_L1")
    LabelStr.isString(dt_l1.key) should be (true)
    LabelStr.isString((PLUS_INFINITY+1).toInt) should be (false)

  }

  test("LabelSetAllocations") {
    val values = Set[Double](LabelStr("DT_L1"), LabelStr("L4"), LabelStr("DT_L2"), LabelStr("L5"),
                             LabelStr("DT_L3"))
    val listener = new ChangeListener
    val ls0 = new SymbolicDomain(values)
    ls0.setListener(listener)
    ls0.isOpen should be (false)

    val dt_l2 = LabelStr("DT_L2")
    ls0.isMember(dt_l2) should be (true)
    
    ls0.remove(dt_l2)
    listener.checkAndClearChange should equal (true, DomainListener.VALUE_REMOVED)
    ls0.isMember(dt_l2) should be (false)
    
    val dt_l3 = LabelStr("DT_L3")
    ls0.set(dt_l3)
    ls0.isMember(dt_l3)
    ls0.size.get == 1
    
    val ls1 = new SymbolicDomain(values)
    ls0.relax(ls1)
    listener.checkAndClearChange should equal (true, DomainListener.RELAXED)
    ls0 ≟ ls1 should be (true)
  }
  test("Equate") {
    import SymbolicDomain._
    val baseValues: Set[Double] = Set[String]("A", "B", "C", "D", "E", "F", "G", "H")
    val listener = new ChangeListener
    val ls0 = SymbolicDomain(baseValues)
    val ls1 = SymbolicDomain(baseValues)
    ls0.setListener(listener)
    ls1.setListener(listener)
    
    ls0 ≟ ls1 should be (true)
    ls0.size should be (Some(8))
    ls0.equate(ls1) should be (false)

    val lC = LabelStr("C")
    ls0.remove(lC)
    ls0.isMember(lC) should be (false)
    ls1.isMember(lC) should be (true)
    
    ls0.equate(ls1) should be (true)
    ls1.isMember(lC) should be (false)
    
    val ls2 = SymbolicDomain(baseValues)
    ls2.setListener(listener)
    ls2.remove(LabelStr("A"))
    ls2.remove(LabelStr("B"))
    ls2.remove(LabelStr("C"))
    ls2.remove(LabelStr("D"))
    ls2.remove(LabelStr("E"))

    val ls3 = SymbolicDomain(baseValues)
    ls3.setListener(listener)
    val lA = LabelStr("A")
    val lB = LabelStr("B")
    ls3.remove(lA)
    ls3.remove(lB)
    ls3.remove(lC)
    ls2.equate(ls3) should be (true)
    ls2 ≟ ls3 should be (true)

    val ls4 = SymbolicDomain(baseValues)
    ls4.setListener(listener)
    ls4.remove(LabelStr("A"))
    ls4.remove(LabelStr("B"))
    ls4.remove(LabelStr("C"))
    ls4.remove(LabelStr("D"))
    ls4.remove(LabelStr("E"))
    
    val ls5 = SymbolicDomain(baseValues)
    ls5.setListener(listener)
    ls5.remove(LabelStr("F"))
    ls5.remove(LabelStr("G"))
    ls5.remove(LabelStr("H"))

    ls4.equate(ls5)
    listener.checkAndClearChange should be (true, DomainListener.EMPTIED)
    ls4.isEmpty || ls5.isEmpty should be (true)
    !(ls4.isEmpty && ls5.isEmpty) should be (true)

    val enumVals = Set(1.0, 2.5,-0.25,3.375,-1.75)
    val ed1 = NumericDomain(enumVals)
    val ed3 = NumericDomain(enumVals)

    val enumVals2 = Set(3.375, 2.5)
    val ed2 = NumericDomain(enumVals2)
    val ed4 = NumericDomain(enumVals2)

    ed1.equate(ed2);
    ed1 ≟ ed2 should be (true);

    ed1.equate(ed3);
    ed1 ≟ ed3 should be (true);

    var ed0 = NumericDomain(Set(0D))

    ed1.equate(ed0);

    // This is actually false because equate only empties
    // one of the domains when the intersection is empty.
    // ed0 qeq ed1 should be (true);

    !(ed0 ≟ ed1) should be (true);
    ed1.isEmpty != ed0.isEmpty should be (true);

    ed0 = NumericDomain(Set(0D));
    !ed0.isEmpty should be (true);

    ed0.equate(ed2);
    ed2 != ed0 && ed2.isEmpty != ed0.isEmpty should be (true);

    ed0 = NumericDomain(Set(0.0, 20.0));
    !ed0.isEmpty && !ed0.isSingleton should be (true);

    val id0 = IntervalDomain(-10.0, 10.0);

    id0.equate(ed0);
    //ed0.isSingleton && ed0.getSingletonValue == 0.0, ed0.toString should be (true);
    //id0.isSingleton && id0.getSingletonValue == 0.0, id0.toString should be (true);

    ed0 = NumericDomain(Set(0.0, 20.0)); // Now 0.0 and 20.0
    !ed0.isEmpty && !ed0.isSingleton should be (true);

    val id1 = IntervalDomain(0.0, 5.0);

    ed0.equate(id1);
    ed0.isSingleton && ed0.getSingletonValue.get ≟ 0.0 should be (true);
    id1.isSingleton && id1.getSingletonValue.get ≟ 0.0 should be (true);

    val ed5 = NumericDomain(Set(3.375, 2.5, 1.5))
    val id2 = IntervalDomain(2.5, 3.0);

    ed5.equate(id2);
    ed5.isSingleton && ed5.getSingletonValue.get ≟ 2.5 should be (true);
    id2.isSingleton && id2.getSingletonValue.get ≟ 2.5 should be (true);
    
    val ed6 = NumericDomain(Set(3.375, 2.5, 1.5, -2.0))
    val id3 = IntervalDomain(-1.0, 3.0);

    id3.equate(ed6);
    ed6.size.get ≟ 2 should be (true);
    id3 ≟ IntervalDomain(1.5, 2.5) should be (true);

    val id4 = IntervalDomain(1.0, 1.25);

    ed6.equate(id4);
    ed6.isEmpty != id4.isEmpty should be (true);

    
    val ed7 = NumericDomain(Set(1.0))
    val id5 = IntervalDomain(1.125, PLUS_INFINITY);

    id5.equate(ed7);
    ed7.isEmpty != id5.isEmpty should be (true);
  }
  test("ValueRetrieval") {
    import SymbolicDomain._
    val values: Set[Double] = Set[LabelStr]("A", "B", "C", "D", "E")
    val dt_l1 = SymbolicDomain(values);

    val results = dt_l1.getValues
    results should equal (List(LabelStr("A").key, LabelStr("B").key, LabelStr("C").key, LabelStr("D").key, LabelStr("E").key).sorted)

    val dt_l2 = SymbolicDomain(results.toSet);

    dt_l1 ≟ dt_l2 should be (true)
    val lbl = LabelStr("C");
    dt_l1.set(lbl);
    dt_l1.getSingletonValue.get should equal (lbl.key)
  }
  test("Intersection") {
    import SymbolicDomain._
    import LabelStr._
    val values: Set[Double] = Set[String]("A", "B", "C", "D", "E", "F", "G", "H", "I")
    val ls1 = SymbolicDomain(values);

    val value = ls1.convertToMemberValue("H")
    value should be ('defined)
    value.get ≟ LabelStr("H") should be (true)
    ls1.convertToMemberValue("LMN") should not be ('defined)

    val ls2 = SymbolicDomain(values);
    ls2.remove(LabelStr("A"));
    ls2.remove(LabelStr("C"));
    ls2.remove(LabelStr("E"));
    ls2.isSubsetOf(ls1) should be (true)
    !ls1.isSubsetOf(ls2) should be (true)

    val ls3 = SymbolicDomain(ls1);
    ls1.intersect(ls2);
    ls1 ≟ ls2 should be (true)
    ls2.isSubsetOf(ls1) should be (true)

    ls1.relax(ls3);
    ls2.isSubsetOf(ls1) should be (true)
    ls1 ≟ ls3 should be (true)

    val ls4 = SymbolicDomain(values);
    ls4.remove(LabelStr("A"));
    ls4.remove(LabelStr("B"));
    ls4.remove(LabelStr("C"));
    ls4.remove(LabelStr("D"));
    ls4.remove(LabelStr("E"));
    ls4.remove(LabelStr("F"));
    ls4.remove(LabelStr("G"));

    ls3.remove(LabelStr("H"));
    ls3.remove(LabelStr("I"));
    ls4.intersect(ls3);
    ls4.isEmpty should be (true)

    val d0 = NumericDomain(0 to 3)

    val d1 = NumericDomain(Set(-1D, 2D, 4D, 5D))

    d0.intersect(d1);
    d0.size ≟ Some(1) should be (true)

    // Also test bounds intersection
    d1.intersect(0, 4.6);
    d1.size ≟ Some(2) should be (true)

  }
  test("Difference") {
    val dom0 = NumericDomain(Set(1D,3D,2D,8D,10D,6D))

    val dom1 = IntervalIntDomain(11, 100);
    var res = dom0.difference(dom1);
    !res should be (true)

    val dom2 = IntervalIntDomain(5, 100);
    res = dom0.difference(dom2);
    res should be (true)
    dom0.getUpperBound should be (3)

    val dom3 = IntervalIntDomain(0, 100);
    res = dom0.difference(dom3);
    res should be (true)
    dom0.isEmpty should be (true)
  }

  test("OperatorEquals") {
    val dom0 = NumericDomain(Set(1D,3D,2D,8D,10D,6D))

    val dom1 = NumericDomain(Set(1D,3D,2D))

    val dom2 = NumericDomain(dom0);

    dom0 ≟ dom1 should be (false)
    dom0 := dom1
    dom0 ≟ dom1 should be (true)

    dom1 := dom2;
    dom1 ≟ dom2 should be (true)
  }
  ignore("EmptyOnClosure", OpenDomains) {}
}

class MixedTypeTest extends FunSuite with ShouldMatchers { 
  ignore("OpenAndClosed", OpenDomains) {}
  test("InfinityBounds") {
    val dom0 = IntervalDomain()

    dom0.areBoundsFinite should be (false)
    val dom1 = IntervalDomain(0, PLUS_INFINITY)
    dom1.areBoundsFinite should be (false)
    //have to change this test, because the definition of infinity has changed
    //IntervalDomain dom2(0, PLUS_INFINITY-1);
    val dom2 = IntervalDomain(0, MAX_INT);
    dom2.areBoundsFinite should be (true)
    val dom3 = NumericDomain()
    dom3.areBoundsFinite should be (false)
    val dom4 = SymbolicDomain()
    dom4.areBoundsFinite should be (true)
    val dom5 = NumericDomain(Set(0D, 1D))
      // dom5.insert(0);
      // dom5.insert(1);
      // dom5.areBoundsFinite should be (false)
      // dom5.close;
    dom5.areBoundsFinite should be (true)
    val dom6 = NumericDomain(PLUS_INFINITY);
    dom6.areBoundsFinite should be (false)
  }
  test("Equality") {
    val dom = NumericDomain(1D, 2D)
    
    val dom0 = NumericDomain(dom);
    dom0.set(1.0)

    val dom1 = IntervalDomain(1.0);
    dom1.asInstanceOf[Domain] ≟ dom0.asInstanceOf[Domain] should be (true);
    dom0.asInstanceOf[Domain] ≟ dom1.asInstanceOf[Domain] should be (true);

    val dom2 = IntervalIntDomain(1);
    dom1 ≟ dom2 should be (true);

    dom0.reset(dom);
    val dom3 = IntervalIntDomain(1, 2);
    dom0.asInstanceOf[Domain] ≟ dom3.asInstanceOf[Domain] should be (true);

  }
  test("Intersection") {
    val dom0 = NumericDomain(0.0,0.98,1.0,1.89,2.98,10.0);

    dom0.size ≟ Some(6) should be (true)
    val dom1 = IntervalIntDomain(1, 8);
    val dom2 = NumericDomain(dom0)

    dom0.intersect(dom1);
    dom0.size should be (Some(1))
    dom0.isMember(1.0) should be (true)

    val dom3 = IntervalDomain(1, 8);
    dom2.intersect(dom3);
    dom2.size should be (Some(3))
  }
  test("Subset") {
    val dom0 = NumericDomain(0.0,0.98,1.0,1.89,2.98,10.0);
    val dom1 = IntervalDomain(0, 10);
    dom0.isSubsetOf(dom1) should be (true)

    val dom2 = IntervalIntDomain(0, 10);
    dom0.isSubsetOf(dom2) should be (false)

    dom0.remove(0.98);
    dom0.remove(1.89);
    dom0.remove(2.98);
    dom0.isSubsetOf(dom2) should be (true)

    dom2.isSubsetOf(dom1) should be (true)
    dom1.isSubsetOf(dom2) should be (false)
  }
  test("IntDomain") {
    val dom0 = NumericDomain(10.0,12.0)
    val dom1 = NumericDomain(9.98,9.037)

    val dom2 = NumericDomain(10)
    dom2.isOpen should be (false)
    dom2.isSingleton should be (true)

    Domain.canBeCompared(dom0, dom2) should be (true)
    dom0 ≟ dom2 should be (false)

    dom0.isSubsetOf(dom2) should be (false)
    dom0.isSubsetOf(dom0) should be (true)
    dom2.isSubsetOf(dom0) should be (true)
    dom2.isSubsetOf(dom2) should be (true)
  }
  ignore("DomainComparatorConfiguration", Nope) {  }
  test("Copying") {
    val boolDom = BoolDomain()
    var copy: Domain = boolDom.copy
    copy should not be (null)
    copy ≟ boolDom should be (true)
    copy should not be (boolDom)

    boolDom.set(false);
    copy ≟ boolDom should be (false)

    copy = boolDom.copy
    copy should not be (null)
    copy ≟ boolDom should be (true)
    copy should not be (boolDom)
    boolDom.empty
    copy ≟ boolDom should be (false)

    copy = boolDom.copy
    copy should not be (null)
    copy ≟ boolDom should be (true)
    copy should not be (boolDom)
    boolDom.relax(BoolDomain(true))
    copy ≟ boolDom should be (false)

    copy = boolDom.copy
    copy should not be (null)
    copy ≟ boolDom should be (true)
    copy should not be (boolDom)
    boolDom.remove(true)
    copy ≟ boolDom should be (false)

    var iiDom = IntervalIntDomain(-2, PLUS_INFINITY.toInt);
    copy = iiDom.copy
    copy should not be (null)
    copy ≟ iiDom should be (true)
    copy should not be (iiDom)

      //have to change the definition of this test because PLUS_INFINITY has changed
//       iiDom = IntervalIntDomain(-2, PLUS_INFINITY-1);
    iiDom = IntervalIntDomain(-2, MAX_INT.toInt);
    copy ≟ iiDom should be (false)

    val iDom = IntervalDomain(MINUS_INFINITY);
    copy = iDom.copy
    copy should not be (null)
    copy ≟ iDom should be (true)
    copy should not be (iDom)
    iDom.empty
    copy ≟ iDom should be (false)
    copy.empty
    copy ≟ iDom should be (true)

    val eDom = NumericDomain(2.7, PLUS_INFINITY)
    copy = eDom.copy
    copy should not be (null)
    copy ≟ eDom should be (true)
    copy should not be (eDom)

    eDom.remove(PLUS_INFINITY);
    copy.remove(PLUS_INFINITY)
    copy ≟ eDom should be (true)
    copy should not be (eDom)
  }
  test("CopyingBoolDomains") { 
    var copyPtr: Domain = null
    val falseDom = BoolDomain(false);
    val trueDom = BoolDomain(true);
    val both = BoolDomain()
    val customDom = BoolDomain(true);

    copyPtr = falseDom.copy
    copyPtr.isBool should be (true);
    (copyPtr.asInstanceOf[BoolDomain]).isFalse should be (true);
    (copyPtr.asInstanceOf[BoolDomain]).isTrue should be (false);


    copyPtr = trueDom.copy;
    copyPtr.isBool should be (true);
    (copyPtr.asInstanceOf[BoolDomain]).isTrue should be (true);
    (copyPtr.asInstanceOf[BoolDomain]).isFalse should be (false);

    copyPtr = both.copy;
    copyPtr.isBool should be (true);
    (copyPtr.asInstanceOf[BoolDomain]).isFalse should be (false);
    (copyPtr.asInstanceOf[BoolDomain]).isTrue should be (false);

    copyPtr = customDom.copy;
    copyPtr.isBool should be (true);
    copyPtr.dataType.name.toString == BoolDT.NAME should be (true);
    (copyPtr.asInstanceOf[BoolDomain]).isTrue should be (true);
    (copyPtr.asInstanceOf[BoolDomain]).isFalse should be (false);

  }
  test("CopyingEnumeratedDomains") { 
    var copyPtr: Domain = null

    val values = Set(0.0, 1.1, 2.7, 3.1, 4.2)
    val fourDom = NumericDomain(0.0, 1.1, 2.7, 3.1);
    val fiveDom = NumericDomain(values); // Closed
    val oneDom = NumericDomain(2.7); // Singletn


    copyPtr = fourDom.copy;
    copyPtr.dataType.name should be (LabelStr("float"))
    copyPtr.isOpen should be (false);
    copyPtr.isEnumerated should be (true);
    copyPtr.size should be (Some(4))
    copyPtr.isSubsetOf(fiveDom) should be (true);

    copyPtr = fiveDom.copy;
    copyPtr.dataType.name should be (LabelStr("float"))
    copyPtr.isOpen should be (false);
    copyPtr.isEnumerated should be (true);
    copyPtr.size should be (Some(5))
    fourDom.isSubsetOf(copyPtr) should be (true);

    copyPtr = oneDom.copy;
    copyPtr.dataType.name should be (LabelStr("float"))
    copyPtr.isOpen should be (false);
    copyPtr.isEnumerated should be (true);
    copyPtr.isSingleton should be (true);

    // Can't call this with a dynamic domain, so close it first.
    copyPtr.isSubsetOf(fourDom) should be (true);

  }
  test("CopyingIntervalDomains") { 
    var copyPtr : Domain = null
    val one2ten = IntervalDomain(1.0, 10.9);
    val four = IntervalIntDomain(4,4);
    val empty = IntervalDomain()
    empty.empty;

      // Domains containing infinities should also be tested.

    copyPtr = empty.copy;
    copyPtr.dataType.name ≟ LabelStr("float") should be (true)
    copyPtr.isOpen should be (false)
    copyPtr.isNumeric should be (true)
    copyPtr.isEnumerated should be (false)
    copyPtr.isFinite should be (true)
    copyPtr.isMember(0.0) should be (false)
    copyPtr.isSingleton should be (false)
    copyPtr.isEmpty should be (true)
    copyPtr.size ≟ Some(0) should be (true)
    copyPtr ≟ empty should be (true)
    (copyPtr ≟ one2ten) should be (false)
    copyPtr.relax(IntervalDomain(-3.1, 11.0));
    copyPtr.isMember(0.0) should be (true)
    copyPtr.isSingleton should be (false)
    copyPtr.isEmpty should be (false)
    empty.isEmpty should be (true)
    (copyPtr ≟ empty) should be (false)
    empty.isSubsetOf(copyPtr) should be (true)

    copyPtr = one2ten.copy;
    copyPtr.dataType.name ≟ LabelStr("float") should be (true)
    copyPtr.isOpen should be (false)
    copyPtr.isNumeric should be (true)
    copyPtr.isEnumerated should be (false)
    copyPtr.isFinite should be (false)
    copyPtr.isMember(0.0) should be (false)
    copyPtr.isSingleton should be (false)
    copyPtr.isEmpty should be (false)
    (copyPtr ≟ empty) should be (false)
    copyPtr ≟ one2ten should be (true)
    copyPtr.relax(IntervalDomain(-3.1, 11.0));
    copyPtr.isMember(0.0) should be (true)
    copyPtr.isSingleton should be (false)
    copyPtr.isEmpty should be (false)
    (copyPtr ≟ one2ten) should be (false)
    one2ten.isSubsetOf(copyPtr) should be (true)

    copyPtr = four.copy;
    copyPtr.dataType.name ≟ IntDT.NAME should be (true)
    copyPtr.isOpen should be (false)
    copyPtr.isNumeric should be (true)
    copyPtr.isEnumerated should be (false)
    copyPtr.isFinite should be (true)
    copyPtr.isMember(0.0) should be (false)
    copyPtr.isSingleton should be (true)
    copyPtr.isEmpty should be (false)
    copyPtr.size ≟ Some(1) should be (true)
    (copyPtr ≟ empty) should be (false)
    copyPtr ≟ four should be (true)
    (copyPtr ≟ one2ten) should be (false)
    copyPtr.relax(IntervalIntDomain(-3, 11));
    copyPtr.isMember(0.0) should be (true)
    copyPtr.isSingleton should be (false)
    copyPtr.isEmpty should be (false)
    (copyPtr ≟ empty) should be (false)
    (copyPtr ≟ four) should be (false)
    four.isSubsetOf(copyPtr) should be (true)
  }
  test("CopyingIntervalIntDomains") { 
    var copyPtr: Domain = null
    val one2ten = IntervalIntDomain(1, 10);
    val four = IntervalIntDomain(4,4);
    val empty = IntervalIntDomain()
    empty.empty;
    // domains containing infinities should also be tested

    copyPtr = empty.copy;
    copyPtr.dataType.name ≟ LabelStr("int") should be (true);
    copyPtr.isOpen should be (false);
    copyPtr.isNumeric should be (true);
    copyPtr.isEnumerated should be (false);
    copyPtr.isFinite should be (true);
    copyPtr.isMember(0) should be (false);
    copyPtr.isSingleton should be (false);
    copyPtr.isEmpty should be (true);
    copyPtr.size ≟ Some(0) should be (true);
    copyPtr ≟ empty should be (true);
    (copyPtr ≟ one2ten) should be (false);
    copyPtr.relax(IntervalIntDomain(-3, 11));
    copyPtr.isMember(0) should be (true);
    copyPtr.isSingleton should be (false);
    copyPtr.isEmpty should be (false);
    empty.isEmpty should be (true);
    !(copyPtr ≟ empty) should be (true);
    empty.isSubsetOf(copyPtr) should be (true);

    copyPtr = one2ten.copy;
    copyPtr.dataType.name ≟ LabelStr("int") should be (true);
    copyPtr.isOpen should be (false);
    copyPtr.isNumeric should be (true);
    copyPtr.isEnumerated should be (false);
    copyPtr.isFinite should be (true);
    copyPtr.isMember(0) should be (false);
    copyPtr.isSingleton should be (false);
    copyPtr.isEmpty should be (false);
    copyPtr.size ≟ Some(10) should be (true);
    !(copyPtr ≟ empty) should be (true);
    copyPtr ≟ one2ten should be (true);
    copyPtr.relax(IntervalIntDomain(-3, 11));
    copyPtr.size ≟ Some(15) should be (true);
    copyPtr.isMember(0) should be (true);
    copyPtr.isSingleton should be (false);
    copyPtr.isEmpty should be (false);
    !(copyPtr ≟ one2ten) should be (true);
    one2ten.isSubsetOf(copyPtr) should be (true);

    copyPtr = four.copy;
    copyPtr.dataType.name.toString ≟ IntDT.NAME should be (true);
    copyPtr.isOpen should be (false);
    copyPtr.isNumeric should be (true);
    copyPtr.isEnumerated should be (false);
    copyPtr.isFinite should be (true);
    copyPtr.isMember(0) should be (false);
    copyPtr.isSingleton should be (true);
    copyPtr.isEmpty should be (false);
    copyPtr.size ≟ Some(1) should be (true);
    !(copyPtr ≟ empty) should be (true);
    copyPtr ≟ four should be (true);
    !(copyPtr ≟ one2ten) should be (true);
    copyPtr.relax(IntervalIntDomain(-3, 11));
    copyPtr.size ≟ Some(15) should be (true);
    copyPtr.isMember(0) should be (true);
    copyPtr.isSingleton should be (false);
    copyPtr.isEmpty should be (false);
    !(copyPtr ≟ empty) should be (true);
    !(copyPtr ≟ four) should be (true);
    four.isSubsetOf(copyPtr) should be (true);

  }
  test("SymbolicVsNumeric") {
    val bDom = BoolDomain(false);
    val iiDom = IntervalIntDomain(-2, PLUS_INFINITY.toInt);
    val iDom = IntervalDomain(MINUS_INFINITY);
    val nDom = NumericDomain(2.7);
    val eDom = EnumeratedDomain(SymbolDT.INSTANCE); // non numeric enum
    val enDom = EnumeratedDomain(FloatDT.INSTANCE); // numeric enum
    val sDom = SymbolicDomain()
    //StringDomain stDom;

    // change for gnats 3242
    bDom.isNumeric should be (true)
    iiDom.isNumeric should be (true)
    iDom.isNumeric should be (true)
    nDom.isNumeric should be (true)
    eDom.isSymbolic should be (true)
    enDom.isNumeric should be (true)
    sDom.isSymbolic should be (true)
    // stDom.isSymbolic should be (true)
  }
}



// class DomainTest extends FunSuite with ShouldMatchers { 
// }
