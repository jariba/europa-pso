package gov.nasa.arc.europa.constraintengine.test
import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.ConstraintEngineListener
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.constraintengine.Variable
import gov.nasa.arc.europa.constraintengine.component.EqualConstraint
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
import gov.nasa.arc.europa.utils.Debug
import gov.nasa.arc.europa.utils.LabelStr
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

object TestListener { 
  val eventMap: Map[DomainListener.ChangeType, ConstraintEngine.Event.Event] = 
    DomainListener.values.zip(ConstraintEngine.Event.values).toMap
}

class TestListener(ce: ConstraintEngine) extends ConstraintEngineListener { 
  setConstraintEngine(ce)
  import ConstraintEngine.Event._
  override def notifyPropagationCommenced: Unit = increment(PROPAGATION_COMMENCED)
  override def notifyPropagationCompleted: Unit = increment(PROPAGATION_COMPLETED)
  override def notifyPropagationPreempted: Unit = increment(PROPAGATION_PREEMPTED)
  override def notifyAdded(c: Constraint): Unit = increment(CONSTRAINT_ADDED)
  override def notifyRemoved(c: Constraint): Unit = increment(CONSTRAINT_REMOVED)
  override def notifyExecuted(c: Constraint): Unit = increment(CONSTRAINT_EXECUTED)
  override def notifyAdded(c: ConstrainedVariable): Unit = increment(VARIABLE_ADDED)
  override def notifyRemoved(c: ConstrainedVariable): Unit = increment(VARIABLE_REMOVED)
  override def notifyChanged(v: ConstrainedVariable, c: DomainListener.ChangeType): Unit = { 
    increment(TestListener.eventMap(c))
  }
  def getCount(e: Event): Int = events.getOrElse(e, 0)
  def reset: Unit = events = Map()

  private def increment(e: Event): Unit = {
    events = events.updated(e, getCount(e) + 1)
  }
  private var events: Map[Event, Int] = Map()
}

class VariableTest extends FunSuite with ShouldMatchers { 
  test("testAllocation") {
    val engine = CETestEngine()
    val ENGINE = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]
    val dom0 = new IntervalIntDomain(0, 1000);
    val v0 = new Variable[IntervalIntDomain](ENGINE, dom0);
    val dom1 = v0.baseDomain
    dom0 should equal (dom1)

    v0.isValid should equal (true)
    v0.canBeSpecified should equal (true)
    
    // Now restrict the base domain
    // val dom2 = new IntervalIntDomain(3, 10);
    // v0.restrictBaseDomain(dom2);
    // v0.getDerivedDomain should equal (dom2);
  
    val v1 = new Variable[IntervalIntDomain](ENGINE, dom1, false, false, LabelStr("TEST VARIABLE"));
    v1.canBeSpecified should equal (false)
    v1.name should equal (LabelStr("TEST VARIABLE"))
    v1.isValid should equal (true)
  }

  test("testMessaging") {
    import ConstraintEngine.Event
    import ConstraintEngine.Event._
    val engine = CETestEngine()
    val ENGINE = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]
    
    val listener = new TestListener(ENGINE)

    // Add, Specify, Remove
    {
      val v0 = new Variable[IntervalIntDomain](ENGINE, IntervalIntDomain(0, 100));
      listener.getCount(VARIABLE_ADDED) should equal (1)
      v0.specify(5)
      listener.getCount(SET_TO_SINGLETON) should equal (1)
      v0.discard
    }
    listener.getCount(VARIABLE_REMOVED) should equal (1);

    // Bounds restriction messages for derived domain
    listener.reset;
    {
      val v0 = new Variable[IntervalIntDomain](ENGINE, IntervalIntDomain(0, 100))
      val v1 = new Variable[IntervalIntDomain](ENGINE, IntervalIntDomain(0, 10))
      val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, List(v0, v1))
      ENGINE.propagate;
      listener.getCount(UPPER_BOUND_DECREASED) should equal (1);
      v0.specify(7);
      ENGINE.propagate; // Expect a RESTRICT_TO_SINGLETON event through propagation
      listener.getCount(RESTRICT_TO_SINGLETON) should equal (1);

      v0.reset; // Expect a RESET message for v0 and a RELAXATION message for both variables
      listener.getCount(RESET) should equal (1);
      listener.getCount(RELAXED) should equal (2);
      ENGINE.pending should equal (true)

      v0.specify(0); // Expect EMPTIED
      v1.specify(1); // Expect EMPTIED
      ENGINE.propagate;
      listener.getCount(EMPTIED) should equal (1);
    }


  }

  ignore("testMessagingWithEnumerations") { 
    // // Now tests message handling on Enumerated Domain
    // listener.reset;
    // {
    //   Variable<NumericDomain> v0(ENGINE, NumericDomain);
    //   v0.insert(1);
    //   v0.insert(3);
    //   v0.insert(5);
    //   v0.insert(10);
    //   // We should expect no relaxation events when inserting into an open domain
    //   CPPUNIT_ASSERT(listener.getCount(RELAXED) == 0);
    //   v0.close;
    //   CPPUNIT_ASSERT(listener.getCount(CLOSED) == 1);

    //   NumericDomain d0;
    //   d0.insert(2);
    //   d0.insert(3);
    //   d0.insert(5);
    //   d0.insert(11);
    //   d0.close;
    //   Variable<NumericDomain> v1(ENGINE, d0);

    //   EqualConstraint c0(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId, v1.getId));q
    //   ENGINE.propagate; // Should see values removed from both variables domains.
    //   CPPUNIT_ASSERT(listener.getCount(VALUE_REMOVED) == 2);
    //   v0.specify(3);
    //   CPPUNIT_ASSERT(listener.getCount(SET_TO_SINGLETON) == 1);
    //   v1.specify(5);
    //   CPPUNIT_ASSERT(listener.getCount(SET_TO_SINGLETON) == 2);
    //   ENGINE.propagate; // Expect to see exactly one domain emptied
    //   CPPUNIT_ASSERT(listener.getCount(EMPTIED) == 1);
    //   v1.reset; // Should now see 2 domains relaxed.
    //   CPPUNIT_ASSERT(listener.getCount(RELAXED) == 2);
    // }
  }

  ignore("testDynamicVariable") {}
  ignore("testListener") {}
  ignore("testVariablesWithOpenDomains") {}

  ignore("testRestrictionScenarios") {}
  test("testSpecification") {
    val engine = CETestEngine()
    val ENGINE = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]

    {
      val v = new Variable[IntervalIntDomain](ENGINE, IntervalIntDomain(0, 0));
      v.isSpecified should equal (false)
      
    }
    val v2 = new Variable[IntervalIntDomain](ENGINE, IntervalIntDomain(0, 10));
    v2.isSpecified should equal (false)
    v2.specify(3)
    v2.isSpecified should equal (true)
    v2.isSingleton should equal (true)
    v2.getSingletonValue should equal (Some(3))

    v2.reset
    v2.isSpecified should equal (false)
    v2.isSingleton should equal (false)
  }

}
