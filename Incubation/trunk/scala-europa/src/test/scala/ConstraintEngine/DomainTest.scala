package gov.nasa.arc.europa.constraintengine.test
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.constraintengine.component.BoolDomain
import gov.nasa.arc.europa.constraintengine.component.FloatDT
import gov.nasa.arc.europa.constraintengine.component.IntervalDomain
import gov.nasa.arc.europa.constraintengine.component.IntervalDomain._
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain._
import gov.nasa.arc.europa.utils.Infinity
import gov.nasa.arc.europa.utils.Number._

import scalaz._
import Scalaz._

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
  implicit def BarEqual: Equal[Bar] = equalBy(_.getBounds)
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

class IntervalDomainTest extends FunSuite with ShouldMatchers { 
  test("allocation") { 
    val infTest = new IntervalDomain()
    infTest.isEmpty should equal (false)
    
    val realDomain = new IntervalDomain(10.2, 20.4)
    realDomain.isEmpty should equal (false)
    realDomain.isFinite should equal (false)
    realDomain.size should equal (None)

    val intDomain = new IntervalIntDomain(10, 20)
    intDomain.isFinite should equal (true)

    val (lb, ub) = intDomain.getBounds
    val d1 = new IntervalIntDomain
    d1.empty
    d1.isEmpty should equal (true)

    val d2: Domain = intDomain
    d2.isEmpty should equal (false)

    val d3 = new IntervalIntDomain(intDomain)
    val d4 = new IntervalIntDomain

    (d3 ≟ d4) should equal (false)
    d3 relax d4
    (d3 ≟ d4) should equal (true)
    
    (d2 ≟ d4) should equal (false)
    d2 relax d4
    (d2 ≟ d4) should equal (true)
  }
  test("relaxation") { 
    val listener = new ChangeListener
    val dom0 = new IntervalIntDomain()
    dom0.isEmpty should equal (false)
    val dom1 = new IntervalIntDomain(-100, 100)
    dom1.setListener(listener)
    dom1.relax(dom0)
    var (res, change) = listener.checkAndClearChange
    res should equal (true)
    change should equal (DomainListener.RELAXED)
    dom1 isSubsetOf dom0 should equal (true)
    dom0 isSubsetOf dom1 should equal (true)
    (dom0 ≟ dom1) should equal (true)

    val dom2 = new IntervalIntDomain(-300, 100)
    dom1 intersect dom2
    val (res1, change1) = listener.checkAndClearChange
    res1 should equal (true)
    (dom1 ≟ dom2) should equal (true)
    dom1 relax dom2
    val (res2, change2) = listener.checkAndClearChange
    res2 should equal (false)
  }
  test("precision") { 
    val EPSILON = FloatDT.INSTANCE.minDelta
    val dom0 = new IntervalDomain(-EPSILON, 0)
    dom0 isMember -EPSILON should equal (true)
    dom0 isMember (-EPSILON -EPSILON/10) should equal (true)
    dom0 isMember (-EPSILON -EPSILON) should equal (false)
    
    val dom1 = new IntervalDomain(-EPSILON, EPSILON/10)
    (dom1 ≟ dom0) should equal (true)
    
    val dom2 = new IntervalDomain(-EPSILON, -EPSILON/10)
    dom2 intersects dom0 should equal (true)
  }
  test("intersection") { 
    val l_listener = new ChangeListener

    val dom0 = new IntervalIntDomain; // Will have very large default range
    dom0.setListener(l_listener);

    // Execute intersection and verify results
    val dom1 = new IntervalIntDomain(-100, 100);
    dom0.intersect(dom1);
    val(res0, _) = l_listener.checkAndClearChange
    res0 should equal (true)
    (dom0 ≟ dom1) should equal (true)

    // verify no change triggered if none should take place.
    dom0.intersect(dom1);
    val (res1, _) = l_listener.checkAndClearChange
    res1 should equal (false)

    // Verify only the upper bound changes
    val dom2 = new IntervalIntDomain(-200, 50);
    dom0.intersect(dom2);
    val (res2, _) = l_listener.checkAndClearChange
    res2 should equal (true)
    dom0.lowerBound should equal (dom1.lowerBound)
    dom0.upperBound should equal (dom2.upperBound)

    // Make an intersection that leads to an empty domain
    val dom3 = new IntervalIntDomain(500, 1000);
    dom0.intersect(dom3);
    val(res3, _) = l_listener.checkAndClearChange
    res3 should equal (true)
    dom0.isEmpty should equal (true)

    val dom4 = new IntervalDomain(0.98, 101.23);
    val dom5 = new IntervalDomain(80, 120.44);
    val dom6 = new IntervalDomain(80, 101.23);
    dom4 equate dom5
    (dom4 ≟ dom6) should equal (true)
    (dom5 ≟ dom6) should equal (true)

    val domEq1 = new IntervalIntDomain(7)
    val domEq2 = new IntervalIntDomain(0, 10)
    domEq1 equate domEq2
    (domEq1 ≟ domEq2) should equal (true)
    domEq1 should have ('lowerBound (7), 'upperBound (7))
    domEq2 should have ('lowerBound (7), 'upperBound (7))


    val dom7 = new IntervalDomain(-1, 0);
    dom6.intersect(dom7);
    dom6.isEmpty should equal (true)

    val dom8 = new IntervalDomain;
    val dom9 = new IntervalDomain;
    dom8.intersect(IntervalDomain(0.1, 0.10));
    dom9.intersect(IntervalDomain(0.10, 0.10));
    dom8.intersects(dom9) should equal (true)

    // Case added to recreate failure case for GNATS 3045
    val dom8a = new IntervalDomain;
    val dom9a = new IntervalDomain;
    dom8a.intersect(IntervalDomain(0.1, 0.1));
    dom9a.intersect(IntervalDomain(0.1, 0.1));
    dom8a.intersects(dom9a) should equal (true)
    dom8a.upperBound should equal (0.1)
    dom8a.lowerBound should equal (0.1);
    dom9a.upperBound should equal (0.1);
    dom9a.lowerBound should equal (0.1);

    // Test at the limit of precision
    val dom10 = new IntervalDomain(0.0001);
    val dom11 = new IntervalDomain(0.0001);
    dom10.intersects(dom11) should equal (true)

    // Test at the limit of precision
    val dom12 = new IntervalDomain(-0.0001);
    val dom13 = new IntervalDomain(-0.0001);
    dom12.intersects(dom13) should equal (true)

    val EPSILON = FloatDT.INSTANCE.minDelta
    // Test beyond the limits of precission
    val dom14 = new IntervalDomain(-0.1 - EPSILON/10);
    val dom15 = new IntervalDomain(-0.1);
    dom14.intersects(dom15) should equal (true)
    dom14.intersect(dom15) should equal (false)

    val intBase = new IntervalIntDomain(-3, 3);
    val dom16 = new IntervalIntDomain(intBase);
    val dom17 = new IntervalDomain(-2.9, 3);
    val dom18 = new IntervalDomain(-3, 2.9);
    val dom19 = new IntervalDomain(-2.9, 2.9);
    val dom20 = new IntervalDomain(-0.9, 3);
    val dom21 = new IntervalDomain(-3, 0.9);
    val dom22 = new IntervalDomain(0.3, 0.4);

    dom16.intersect(dom17);
    dom16 should have ('lowerBound (-2.0), 'upperBound (3.0))
    dom16.relax(intBase);

    dom16.intersect(dom18);
    dom16 should have ('lowerBound (-3.0), 'upperBound (2.0))
    dom16.relax(intBase);

    dom16.intersect(dom19);
    dom16 should have ('lowerBound (-2.0), 'upperBound (2.0))
    dom16.relax(intBase);

    dom16.intersect(dom20);
    dom16 should have ('lowerBound (0.0), 'upperBound (3.0))
    dom16.relax(intBase);

    dom16.intersect(dom21);
    dom16 should have ('lowerBound (-3.0), 'upperBound (0.0))
    dom16.relax(intBase);

    dom16.intersect(dom22);
    dom16.isEmpty should equal (true)
    dom16.relax(intBase);
  }
  test("subset") { 
    val dom0 = new IntervalIntDomain(10, 35);
    val dom1 = new IntervalDomain(0, 101);
    dom0.isSubsetOf(dom1) should equal (true)
    dom1.isSubsetOf(dom0) should equal (false)

    // Handle cases where domains are equal
    val dom2 = new IntervalIntDomain(dom0);
    (dom2 ≟ dom0) should equal (true)
    dom0.isSubsetOf(dom2) should equal (true)
    dom2.isSubsetOf(dom0) should equal (true)

    // Handle case with no intersection
    val dom3 = new IntervalIntDomain(0, 9);
    dom3.isSubsetOf(dom0) should equal (false)
    dom0.isSubsetOf(dom3) should equal (false)

    // Handle case with partial intersection
    val dom4 = new IntervalIntDomain(0, 20);
    dom4.isSubsetOf(dom0) should equal (false)
    dom0.isSubsetOf(dom4) should equal (false)

    // Handle intersection with infinites
    val dom5 = new IntervalDomain;
    val dom6 = new IntervalDomain(0, 100);
    dom6.isSubsetOf(dom5) should equal (true)
  }
  test("printing") { 
    val d1 = new IntervalIntDomain(1, 100)
    d1.toString should equal("int:CLOSED[1 100]")

    //intervalInt domain
    val intervalInt = new IntervalIntDomain(1,100);
    intervalInt.set(1)
    intervalInt.dataType.toString(intervalInt.getSingletonValue.get) should equal ("1")

    //intervalReal domain
    val intervalReal = new IntervalDomain(1.5, 100.6)
    intervalReal.set(1.5);
    intervalReal.dataType.toString(intervalReal.getSingletonValue.get) should equal ("1.5")

    // boolean domain
    val boolDomainTrue = new BoolDomain(true)
    boolDomainTrue.set(true);
    boolDomainTrue.toString(boolDomainTrue.getSingletonValue.get) should equal("true")

    val boolDomainFalse = new BoolDomain(false)
    boolDomainFalse.set(false);
    boolDomainFalse.toString(boolDomainFalse.getSingletonValue.get) should equal("false")

    // numeric domain
    // val numericDom = NumericDomain(1.117)
    // numericDom.set(1.117);
    // numericDom.getDataType.toString(numericDom getSingletonValue get) should equal ("1.117")

    //   // string domain
    // LabelStr theString("AString");
    //   StringDomain stringDom(theString);
    //   stringDom.set(theString);
    //   CPPUNIT_ASSERT(stringDom.isSingleton());
    //   std::string d6DisplayValueStr = stringDom.getDataType()->toString(stringDom.getSingletonValue());
    //   std::string expectedD6DisplayValue("AString");
    //   CPPUNIT_ASSERT(d6DisplayValueStr == expectedD6DisplayValue);

    //   // symbol domain
    //   LabelStr element("ASymbol");
    //   SymbolDomain symbolDom(element);
    //   symbolDom.set(element);
    //   std::string d7DisplayValueStr = symbolDom.getDataType()->toString(symbolDom.getSingletonValue());
    //   std::string expectedD7DisplayValue("ASymbol");
    //   CPPUNIT_ASSERT(d7DisplayValueStr == expectedD7DisplayValue);

  }
  test("bool domain") { 
    val dom0 = new BoolDomain(true);
    dom0 should have ('size (Some(1)), 'upperBound (1.0), 'lowerBound (1.0))
    // dom0.getSize should equal (1)
    // dom0.getUpperBound should equal (1.0)
    // dom0.getLowerBound should equal (true)
    
    val dom1 = new BoolDomain;
    dom1 should have ('size (Some(2)), 'upperBound (1.0), 'lowerBound (0.0))
    // dom1.getSize should equal (2)
    // dom1.getUpperBound should equal (1.0);
    // dom1.getLowerBound should equal (0.0);
    
    dom1.intersect(dom0);
    (dom1 ≟ dom0) should equal (true)
    // CPPUNIT_ASSERT(dom1 == dom0);

  }
  test ("difference") { 
    val dom0 = new IntervalDomain(1, 10);
    val dom1 = new IntervalDomain(11, 20);
    dom0.difference(dom1) should equal (false)    
    dom1.difference(dom0) should equal (false)

    val dom2 = new IntervalDomain(dom0);
    dom2.difference(dom0) should equal (true)
    dom2.isEmpty should equal (true)

    val dom3 = new IntervalIntDomain(5, 100);
    dom3.difference(dom0) should equal (true)
    dom3 should have ('lowerBound (11))
    dom3.difference(dom1) should equal (true)
    dom3 should have ('lowerBound (21))

    val dom4 = new IntervalDomain(0, 20);
    dom4.difference(dom1) should equal (true)
    dom4.upperBound should equal (dom1.getLowerBound - dom4.minDelta)

    // val dom5 = new NumericDomain(3.14159265);
    // CPPUNIT_ASSERT(dom5.getSize() == 1);

    // std::list<edouble> vals;
    // vals.push_back(dom5.getSingletonValue());
    // vals.push_back(1.2);
    // vals.push_back(2.1);
    // vals.push_back(PLUS_INFINITY);
    // vals.push_back(MINUS_INFINITY);
    // vals.push_back(EPSILON);
    // val dom6 = new NumericDomain(vals);

    // CPPUNIT_ASSERT(dom6.getSize() == 6);
    // CPPUNIT_ASSERT(std::abs(dom5.minDelta() - dom6.minDelta()) < EPSILON); // Should be ==, but allow some leeway.
    // CPPUNIT_ASSERT(dom6.intersects(dom5));

    // dom6.difference(dom5);
    // CPPUNIT_ASSERT(!(dom6.intersects(dom5)));
    // CPPUNIT_ASSERT(dom6.getSize() == 5);

    // dom6.difference(dom5);
    // CPPUNIT_ASSERT(!(dom6.intersects(dom5)));
    // CPPUNIT_ASSERT(dom6.getSize() == 5);

  }
  test("operator equals") { 
    val dom0 = new IntervalDomain(1, 28);
    val dom1 = new IntervalDomain(50, 100);
    dom0 := dom1 
    (dom0 ≟ dom1) should equal (true)
  }
  test("InfinitesAndInts") {
    val dom0 = new IntervalDomain;
      // dom0.translateNumber(MINUS_INFINITY) should equal (MINUS_INFINITY)
      // dom0.translateNumber(MINUS_INFINITY - 1) should equal (MINUS_INFINITY)
      // dom0.translateNumber(MINUS_INFINITY + 1) should equal (MINUS_INFINITY + 1)
      // dom0.translateNumber(PLUS_INFINITY + 1) should equal (PLUS_INFINITY)
      // dom0.translateNumber(PLUS_INFINITY - 1) should equal (PLUS_INFINITY - 1)
      dom0.translateNumber(MINUS_INFINITY) should equal (MINUS_INFINITY)
      dom0.translateNumber(Infinity.minus(MINUS_INFINITY, 1, MINUS_INFINITY)) should equal (MINUS_INFINITY)
      dom0.translateNumber(Infinity.plus(MINUS_INFINITY, 1, MINUS_INFINITY)) should equal (Infinity.plus(MINUS_INFINITY, 1, MINUS_INFINITY))
      dom0.translateNumber(Infinity.plus(PLUS_INFINITY, 1, PLUS_INFINITY)) should equal (PLUS_INFINITY)
      dom0.translateNumber(Infinity.minus(PLUS_INFINITY, 1, PLUS_INFINITY)) should equal (Infinity.minus(PLUS_INFINITY, 1, PLUS_INFINITY))
      dom0.translateNumber(2.8) should equal (2.8)

      val dom1 = new IntervalIntDomain;
      dom1.translateNumber(2.8, false) should equal (2)
      dom1.translateNumber(2.8, true) should equal (3)
      dom1.translateNumber(Infinity.minus(PLUS_INFINITY, 0.2, PLUS_INFINITY), false) should equal (PLUS_INFINITY)
      dom1.translateNumber(Infinity.minus(PLUS_INFINITY, 0.2, PLUS_INFINITY), true) should equal (PLUS_INFINITY)
      dom1.translateNumber(Infinity.minus(PLUS_INFINITY, 0.2, PLUS_INFINITY), false) should equal ((Infinity.minus(PLUS_INFINITY, 1, PLUS_INFINITY)))
      dom1.translateNumber(Infinity.minus(PLUS_INFINITY, 0.2, PLUS_INFINITY), false) should equal (Infinity.minus(PLUS_INFINITY, 1, PLUS_INFINITY))

  }
  // test("EnumSet") { assert(false) }
  // test("InsertAndRemove") { assert(false) }
  // test("ValidComparisonWithEmpty_gnats2403") { assert(false) }
  test("IntervalSingletonValues") { 
    for(v <- -2.0 to 1.5 by 0.1) {
      val id = new IntervalDomain(v, v);
      val values = id.getValues
      values should have length (1)
      values should contain (v)
    }
    for(v <- 2.0 to 1.5 by -0.1) {
      val id = new IntervalDomain(v, v);
      val values = id.getValues
      values should have length (1)
      values should contain (v)
    }
    val id = new IntervalDomain(0, 0);
    val values = id.getValues
    values should have length (1)
    values should contain (0.0)
  }
  test("IntervalIntValues") { 
    val i0 = new IntervalIntDomain(10, 20)
    val values = i0.getValues
    values should have length (11)
    values should equal((10 to 20).toList)

    val i1 = new IntervalIntDomain(-4, 3)
    val values1 = i1.getValues
    values1 should have length (8)
    values1 should equal ((-4 to 3).toList)

    val i2 = new IntervalIntDomain(-10, 10)
    val i3: IntervalDomain = i2
    i2.minDelta should equal(i3.minDelta)
  }

}

class EnumeratedDomainTest extends FunSuite with ShouldMatchers { 
  ignore("Strings") {}
  ignore("EnumerationOnly") {}
  ignore("BasicLabelOperations") {}
  ignore("LabelSetAllocations") {}
  ignore("Equate") {}
  ignore("ValueRetrieval") {}
  ignore("Intersection") {}
  ignore("Difference") {}
  ignore("OperatorEquals") {}
  ignore("EmptyOnClosure") {}
  ignore("OpenEnumerations") {}

}

class MixedTypeTest extends FunSuite with ShouldMatchers { 
  ignore("OpenAndClosed") {}
  ignore("InfinityBounds") {}
  ignore("Equality") {}
  ignore("Intersection") {}
  ignore("Subset") {}
  ignore("IntDomain") {}
  ignore("DomainComparatorConfiguration") {}
  ignore("Copying") {}
  ignore("SymbolicVsNumeric") {}
}



// class DomainTest extends FunSuite with ShouldMatchers { 
// }
