package gov.nasa.arc.europa.constraintengine.test
import gov.nasa.arc.europa.constraintengine.CESchema
import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.component.BoolDomain
import gov.nasa.arc.europa.constraintengine.component.IntervalDomain
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
import gov.nasa.arc.europa.utils.LabelStr

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class TypeFactoryTest extends FunSuite with ShouldMatchers { 
  test("testValueCreation") { 
    val engine = CETestEngine()
    val ce = engine.getConstraintEngine

    val d0 = new IntervalIntDomain(5);
    ce.getSchema.isDataType(d0.dataType.name) should equal (true)
    val v0 = ce.createValue(d0.dataType.name, "5").get.toInt
    d0.compareEqual(d0.getSingletonValue.get, v0) should equal (true)

    val d1 = new IntervalDomain(2.3);
    val v1 = ce.createValue(d1.dataType.name, "2.3").get;
    d1.compareEqual(d1.getSingletonValue.get, v1) should equal (true)

    val d2 = new BoolDomain(true);
    val v2 = ce.createValue(d2.dataType.name, "true").get.toInt
    d2.compareEqual(d2.getSingletonValue.get, v2) should equal (true)
  }
  test("testDomainCreation") { 
    val engine = CETestEngine()
    val tfm = engine.getComponent("CESchema").asInstanceOf[CESchema]



    val bd0: IntervalIntDomain = tfm.baseDomain(IntervalIntDomain().dataType.name).get.asInstanceOf[IntervalIntDomain]
    bd0.isMember(0) should equal (true)
    bd0.isBool should equal (false)

    val bd1: IntervalDomain = tfm.baseDomain(IntervalDomain().dataType.name).get.asInstanceOf[IntervalDomain]
    bd1.isMember(0.1) should equal (true)
    bd1.isBool should equal (false)

    val bd2: BoolDomain = tfm.baseDomain(BoolDomain().dataType.name).get.asInstanceOf[BoolDomain]
    bd2.isMember(false) should equal (true)
    bd2.isMember(true) should equal (true)
    bd2.isBool should equal (true)


  }
  ignore("testDomainCreationWithEnumerations") { 
    //const Domain& locationsBaseDomain = tfm->getDataType("Locations")->baseDomain();
    // CPPUNIT_ASSERT(locationsBaseDomain.isMember(LabelStr("Hill")));
    // CPPUNIT_ASSERT(locationsBaseDomain.isMember(LabelStr("Rock")));
    // CPPUNIT_ASSERT(locationsBaseDomain.isMember(LabelStr("Lander")));
    // CPPUNIT_ASSERT(!locationsBaseDomain.isMember(LabelStr("true")));
    //!!This (and SymbolDomain) die with complaints of a "bad cast"
    //!!const Locations & loc0 = dynamic_cast<const Locations&>(tfm->baseDomain("Locations"));
    // const EnumeratedDomain & loc0 = dynamic_cast<const EnumeratedDomain &>(tfm->baseDomain("Locations"));
    // CPPUNIT_ASSERT(!loc0.isBool());
    // CPPUNIT_ASSERT(loc0.isMember(LabelStr("Hill")));
    // CPPUNIT_ASSERT(loc0.isMember(LabelStr("Rock")));
    // CPPUNIT_ASSERT(loc0.isMember(LabelStr("Lander")));
    // CPPUNIT_ASSERT(!loc0.isMember(LabelStr("true")));
    //!!The compiler complains about using Locations here when EnumeratedDomain is used above.
    //!!Locations *loc1 = loc0.copy();
    // EnumeratedDomain *loc1 = loc0.copy();
    // loc1->open();
    // loc1->remove(LabelStr("Hill"));
    // CPPUNIT_ASSERT(!loc1->isMember(LabelStr("Hill")));
    // CPPUNIT_ASSERT(loc1->isMember(LabelStr("Rock")));
    // CPPUNIT_ASSERT(loc1->isMember(LabelStr("Lander")));
    // loc1->remove(LabelStr("Rock"));
    // CPPUNIT_ASSERT(!loc1->isMember(LabelStr("Hill")));
    // CPPUNIT_ASSERT(!loc1->isMember(LabelStr("Rock")));
    // CPPUNIT_ASSERT(loc1->isMember(LabelStr("Lander")));
    // loc1->insert(LabelStr("Hill"));
    // CPPUNIT_ASSERT(loc1->isMember(LabelStr("Hill")));
    // CPPUNIT_ASSERT(!loc1->isMember(LabelStr("Rock")));
    // CPPUNIT_ASSERT(loc1->isMember(LabelStr("Lander")));
    // loc1->remove(LabelStr("Lander"));
    // CPPUNIT_ASSERT(loc1->isMember(LabelStr("Hill")));
    // CPPUNIT_ASSERT(!loc1->isMember(LabelStr("Rock")));
    // CPPUNIT_ASSERT(!loc1->isMember(LabelStr("Lander")));
    // loc1->insert(LabelStr("Rock"));
    // CPPUNIT_ASSERT(loc1->isMember(LabelStr("Hill")));
    // CPPUNIT_ASSERT(loc1->isMember(LabelStr("Rock")));
    // CPPUNIT_ASSERT(!loc1->isMember(LabelStr("Lander")));
    // loc1->remove(LabelStr("Hill"));
    // CPPUNIT_ASSERT(!loc1->isMember(LabelStr("Hill")));
    // CPPUNIT_ASSERT(loc1->isMember(LabelStr("Rock")));
    // CPPUNIT_ASSERT(!loc1->isMember(LabelStr("Lander")));
    // delete loc1;

  }
  test("testVariableCreation") { 
    val engine = CETestEngine()
    val ce = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]
    ce should not equal (null)
    val cv0: ConstrainedVariable = ce.createVariableByType(IntervalIntDomain().dataType.name).get
    cv0.baseDomain.dataType.name should equal (IntervalIntDomain().dataType.name)

    val cv1: ConstrainedVariable = ce.createVariableByType(IntervalDomain().dataType.name).get
    cv1.baseDomain.dataType.name should equal (IntervalDomain().dataType.name)

    val cv2: ConstrainedVariable = ce.createVariableByType(BoolDomain().dataType.name).get
    cv2.baseDomain.dataType.name should equal (BoolDomain().dataType.name)
  }

  test("testVariableWithDomainCreation") { 
    val engine = CETestEngine()
    
    val d0 = new IntervalIntDomain(5);
    val d1 = new IntervalDomain(2.3);
    val d2 = new BoolDomain(true);
    
    val ce = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]
 
    val cv0 = ce.createVariableWithDomain(d0.dataType.name, d0).get
    cv0.baseDomain should equal (d0)

    val cv1 = ce.createVariableWithDomain(d1.dataType.name, d1).get
    cv1.baseDomain should equal (d1)

    val cv2 = ce.createVariableWithDomain(d2.dataType.name, d2).get
    cv2.baseDomain should equal (d2)
  }
}
