package gov.nasa.arc.europa.constraintengine.test

import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.Domain._
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.constraintengine.component.BoolDomain
import gov.nasa.arc.europa.constraintengine.component.FloatDT
import gov.nasa.arc.europa.constraintengine.component.IntervalDomain
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
import gov.nasa.arc.europa.constraintengine.component.NumericDomain
import gov.nasa.arc.europa.constraintengine.component.SymbolicDomain
import gov.nasa.arc.europa.utils.EqualImplicits._
import gov.nasa.arc.europa.utils.Infinity
import gov.nasa.arc.europa.utils.LabelStr
import gov.nasa.arc.europa.utils.Number._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import scala.math.abs

import scalaz.syntax.equal._


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
    val numericDom = NumericDomain(1.117)
    numericDom.set(1.117);
    numericDom.getDataType.toString(numericDom.getSingletonValue.get) should equal ("1.117")

    //   // string domain
    // LabelStr theString("AString");
    //   StringDomain stringDom(theString);
    //   stringDom.set(theString);
    //   CPPUNIT_ASSERT(stringDom.isSingleton());
    //   std::string d6DisplayValueStr = stringDom.getDataType()->toString(stringDom.getSingletonValue());
    //   std::string expectedD6DisplayValue("AString");
    //   CPPUNIT_ASSERT(d6DisplayValueStr == expectedD6DisplayValue);

    // symbol domain
    val element = LabelStr("ASymbol");
    val symbolDom = SymbolicDomain(element);
    symbolDom.set(element);
    val d7DisplayValueStr = symbolDom.dataType.toString(symbolDom.getSingletonValue.get);
    d7DisplayValueStr should be ("ASymbol")
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

    val dom5 = new NumericDomain(Set(3.14159265));
    dom5.size ≟ Some(1) should be (true)

    val vals = Set(dom5.getSingletonValue.get, 1.2, 2.1, PLUS_INFINITY, MINUS_INFINITY, EPSILON)
    val dom6 = new NumericDomain(vals);

    dom6.size should be (Some(6))

    dom5.minDelta should be (dom6.minDelta)

    dom6.intersects(dom5) should be (true)

    dom6.difference(dom5);
    dom6.intersects(dom5) should be (false)
    dom6.size should be (Some(5))
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
  ignore("EnumSet", OpenDomains) { assert(false) } //requires open domains
  test("InsertAndRemove") {
    val enumDom1 = NumericDomain(Set(3.14159265))

    enumDom1.isMember(3.14159265) should be (true)
    enumDom1.isSingleton should be (true)
    enumDom1.isFinite should be (true)
    enumDom1.remove(5.0);
    enumDom1.isMember(3.14159265) should be (true)
    enumDom1.isSingleton should be (true)
    enumDom1.isFinite should be (true)
    
    val minDiff = enumDom1.minDelta;
    minDiff >= EPSILON && EPSILON > 0.0 should be (true)

    val onePlus = 1.0 + 2.0*EPSILON;

    enumDom1.remove(3.14159265 - onePlus*minDiff);
    enumDom1.isMember(3.14159265) should be (true)
    enumDom1.isSingleton should be (true)
    enumDom1.isFinite should be (true)
    enumDom1.remove(3.14159265 + onePlus*minDiff);
    enumDom1.isMember(3.14159265) should be (true)
    enumDom1.isSingleton should be (true)
    enumDom1.isFinite should be (true)
    enumDom1.remove(3.14159265 - minDiff/onePlus);
    enumDom1.isEmpty should be (true)

    // enumDom1.insert(3.14159265);
    // enumDom1.isMember(3.14159265) should be (true)
    // enumDom1.isSingleton should be (true)
    // enumDom1.isFinite should be (true)
    // enumDom1.remove(3.14159265 + minDiff/onePlus);
    // enumDom1.isEmpty should be (true)
    // enumDom1.insert(3.14159265);
    // enumDom1.isMember(3.14159265) should be (true)
    // enumDom1.isSingleton should be (true)
    val pi = 3.14159265
    val vals = Set(pi, 1.2, 2.1, PLUS_INFINITY, MINUS_INFINITY, EPSILON)
    val enumDom2 = NumericDomain(vals);

    !(enumDom2.isOpen) should be (true)
    enumDom2.isNumeric should be (true)
    enumDom2.isFinite should be (true)
    enumDom2.size ≟ Some(6) should be (true)
    
    val minDiff2 = enumDom2.minDelta;

    abs(minDiff - minDiff2) < EPSILON should be (true)

    enumDom2.remove(1.2 - minDiff2/onePlus);
    enumDom2.size ≟ Some(5) should be (true)

    enumDom2.remove(MINUS_INFINITY);
    enumDom2.size ≟ Some(4) should be (true)

    val enumDom3 = NumericDomain(Set(pi))
    // Remove a value near but not "matching" a member and
    //   verify the domain has not changed.
    enumDom2.remove(pi - onePlus*minDiff2);
    enumDom2.intersects(enumDom3) should be (true)
    enumDom2.size ≟ Some(4) should be (true)

      // Remove a value near but not equal a member and
      //   verify the member was removed via intersection.
    enumDom2.remove(pi - minDiff2/onePlus);
    !(enumDom2.intersects(enumDom3)) should be (true)
    enumDom2.size ≟ Some(3) should be (true)

    // Add a value near a value from another domain
    //   verify that the resulting domain intersects the other domain.
    // enumDom2.insert(enumDom3.getSingletonValue.get + minDiff2/onePlus);
    // enumDom2.intersects(enumDom3) should be (true)
    // enumDom2.size ≟ Some(4) should be (true)

    // Add the value in the other domain and
    //   verify the domain is not affected.
    // enumDom2.insert(enumDom3.getSingletonValue.get);
    // enumDom2.intersects(enumDom3) should be (true)
    // enumDom2.size ≟ Some(4) should be (true)

    // Remove a value that should not be a member but is
    //   only slightly too large to "match" the new member.
    // enumDom2.remove(pi + minDiff2/onePlus + onePlus*minDiff2);
    // enumDom2.intersects(enumDom3) should be (true)
    // enumDom2.size ≟ Some(4) should be (true)

    // Remove a value "matching" the added value but larger
    //   and verify the domain no longer intersects the other domain.
    // enumDom2.remove(pi + 2.0*minDiff2/onePlus);
    // enumDom2.size ≟ Some(3) should be (true)
    // !(enumDom2.intersects(enumDom3)) should be (true)
  }

  test("ValidComparisonWithEmpty_gnats2403") { 
    val d0 = IntervalIntDomain()
    val d1 = IntervalIntDomain()
    val d2 = NumericDomain(Set(1D))

    d0 ≟ d1 should be (true)
    d1.asInstanceOf[Domain] ≟ d2.asInstanceOf[Domain] should be (false) 
    d0.empty
    d0 ≟ d1 should be (false)
    d0.asInstanceOf[Domain] ≟ d2.asInstanceOf[Domain] should be (false)
    
    Domain.canBeCompared(d0, d2) should be (true)

    val d3 = NumericDomain(d2)
    d2.empty
    Domain.canBeCompared(d2, d3) should be (true)
    d3 ≟ d2 should be (false)

  }
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
