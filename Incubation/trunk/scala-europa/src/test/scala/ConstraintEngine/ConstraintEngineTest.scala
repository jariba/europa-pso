package gov.nasa.arc.europa.constraintengine.test

import gov.nasa.arc.europa.constraintengine._
import gov.nasa.arc.europa.constraintengine.component._
import gov.nasa.arc.europa.utils.Debug
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.LabelStr

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class TwicePropagator(ce: ConstraintEngine) extends PostPropagationCallback(ce) {
  var m_counter = 0;

  override def apply: Boolean = { 
    m_counter = m_counter + 1
    return m_counter < 2;
  }
}

class PropagationCounter(ce: ConstraintEngine) extends ConstraintEngineListener {
  var m_counter = 0
  setConstraintEngine(ce)

  override def notifyPropagationCompleted: Unit = { m_counter = m_counter + 1}
  def counter: Int = m_counter
}

class ConstraintEngineTest extends FunSuite with ShouldMatchers {
  test("PostPropagation") {
    val engine = CETestEngine()
    val ce = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]
    
    var postProp: Int = 0
    val counter = new PropagationCounter(ce)
    val callback = new TwicePropagator(ce)
    ce.addCallback(callback)
    
    // Set up a base domain
    val intBaseDomain = IntervalIntDomain(1, 5)

    for(i <- 0 until 100){
      val v0 = new Variable[IntervalIntDomain](ce, intBaseDomain)
      val v1 = new Variable[IntervalIntDomain](ce, intBaseDomain)
      new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ce, List(v0, v1));
    }

    ce.propagate should be (true)
    counter.counter should equal(1)
  }

  test("DeallocationWithPurging"){
    val engine = CETestEngine()
    val ce = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]

    // Set up a base domain
    val intBaseDomain = IntervalIntDomain(1, 5)

    for(i <- 0 until 100){
      val v0 = new Variable[IntervalIntDomain](ce, intBaseDomain)
      val v1 = new Variable[IntervalIntDomain](ce, intBaseDomain)
      new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ce, List(v0, v1));
    }

    ce.propagate should be (true)
    Entity.purgeStart
    ce.purge
    Entity.purgeEnd
  }

  test("InconsistentInitialVariableDomain"){
    val engine = CETestEngine()
    val ce = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]
    val emptyDomain = IntervalIntDomain()
    emptyDomain.empty
    val v0 = new Variable[IntervalIntDomain](ce, emptyDomain);
    ce.provenInconsistent should be (true)
    v0.discard
    ce.propagate should be (true)
  }

  //may never implement this, don't know
  ignore("testVariableLookupByIndex"){ }
  //  testVariableLookupByIndex(){
  //   std::vector<ConstrainedVariableId> vars;

  //   for(unsigned int i=0;i<10;i++){
  //     ConstrainedVariableId var = (new Variable<IntervalIntDomain> (ENGINE, IntervalIntDomain()))->getId();
  //     CPPUNIT_ASSERT(var == ENGINE->getVariable(i));
  //     CPPUNIT_ASSERT(ENGINE->getIndex(var) == i);
  //     vars.push_back(var);
  //   }

  //   cleanup(vars);
  //   return true;
  // }

  /**
   * A single relaxation may not be enough to empty the variable. Want to ensure that
   * we correctly manage this case.
   */
  test("GNATS_3133") {
    val engine = CETestEngine()
    val ce = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]

    val v0 = new Variable[IntervalIntDomain](ce, IntervalIntDomain(0, 10))
    val v1 = new Variable[IntervalIntDomain](ce, IntervalIntDomain(0, 10))
    val v2 = new Variable[IntervalIntDomain](ce, IntervalIntDomain(0, 10))
    val v3 = new Variable[IntervalIntDomain](ce, IntervalIntDomain(0, 10))

    val c0 = new EqualConstraint(LabelStr("EqualConstraint"),
        			 LabelStr("Default"), ce, List(v0, v1))

    val c1 = new EqualConstraint(LabelStr("EqualConstraint"),
        				 LabelStr("Default"), ce, List(v2, v3))

    val c2 = new EqualConstraint(LabelStr("EqualConstraint"),
        			 LabelStr("Default"), ce, List(v2, v3))

    v0.specify(1)
    v1.specify(2)
    v2.specify(3)
    v3.specify(3)
    ce.propagate should be (false)

    v2.reset
    //DEPRECATED (GNATS 3140):CPPUNIT_ASSERT(ce.provenInconsistent)
    ce.propagate should be (false)
    v3.reset
    ce.propagate should be (false)
    ce.provenInconsistent should be (true)
    v0.reset
    v1.reset
    ce.propagate should be (true)
    ce.provenInconsistent should be (false)

    v0.specify(1)
    v1.specify(2)
    v2.specify(3)
    v3.specify(3)

    // Now delete constraints in the order that relaxes the empty variable last
    c1.discard
    ce.propagate should be (false)
    c2.discard
    ce.propagate should be (false)
    c0.discard
    ce.propagate should be (true)
  }
}
