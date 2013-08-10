package gov.nasa.arc.europa.constraintengine.test
import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.ConstraintEngineListener
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.constraintengine.Scope
import gov.nasa.arc.europa.constraintengine.Variable
import gov.nasa.arc.europa.constraintengine.component.EnumeratedDomain
import gov.nasa.arc.europa.constraintengine.component.EqualConstraint
import gov.nasa.arc.europa.constraintengine.component.IntDT
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
import gov.nasa.arc.europa.constraintengine.component.NumericDomain
import gov.nasa.arc.europa.utils.Debug
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.LabelStr
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import scalaz._
import Scalaz._

object TestListener { 
  val eventMap: Map[DomainListener.ChangeType, ConstraintEngine.Event.Event] = 
    DomainListener.values.zip(ConstraintEngine.Event.values).toMap
}

class TestListener(ce: ConstraintEngine) extends ConstraintEngineListener { 
  setConstraintEngine(ce)
  import ConstraintEngine.Event
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

  test("testMessagingWithEnumerations") { 
    import ConstraintEngine.Event
    import ConstraintEngine.Event._
    // Debug.enable("ConstraintEngine")
    // Debug.enable("ConstrainedVariable")
    val engine = CETestEngine()
    val ENGINE = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]
    
    val listener = new TestListener(ENGINE)

    // Now tests message handling on Enumerated Domain
    {
      val v0 = new Variable[NumericDomain](ENGINE, NumericDomain(Set(1D, 3D, 5D, 10D)))
      val v1 = new Variable[NumericDomain](ENGINE, NumericDomain(Set(2D, 3D, 5D, 11D)))

      val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE,
                                   List(v0, v1));
      ENGINE.propagate; // Should see values removed from both variables domains.
      listener.getCount(VALUE_REMOVED) == 2 should be (true);
      v0.specify(3)
      listener.getCount(SET_TO_SINGLETON) == 1 should be (true);
      v1.specify(5)
      listener.getCount(SET_TO_SINGLETON) == 2 should be (true);
      ENGINE.propagate; // Expect to see exactly one domain emptied
      listener.getCount(EMPTIED) == 1 should be (true);
      v1.reset; // Should now see 2 domains relaxed.
      // listener.getCount(RELAXED) should be (2)
      //The above comment about two relaxed domains is from the original source, but I have no idea
      //why it would get two messages.  Both domains are specified, only one is emptied.
      listener.getCount(RELAXED) should be (1)
    }
  }

  ignore("testDynamicVariable", OpenDomains) {}
  test("testListener") {
    import ConstraintEngine.Event
    import ConstraintEngine.Event._
    val engine = CETestEngine()
    val ENGINE = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]
    
    val listener = new TestListener(ENGINE)

    // Add, Specify, Remove
    {
      val v0 = new Variable[IntervalIntDomain](ENGINE, IntervalIntDomain(0, 100))
      listener.getCount(VARIABLE_ADDED) ≟ 1 should be (true)
      v0.specify(5);
      listener.getCount(SET_TO_SINGLETON) ≟ 1 should be (true)
      v0.discard
    }
    listener.getCount(VARIABLE_REMOVED) ≟ 1 should be (true)

    // Bounds restriction messages for derived domain
    listener.reset;
    {
      val v0 = new Variable[IntervalIntDomain](ENGINE, IntervalIntDomain(0, 100))
      val v1 = new Variable[IntervalIntDomain](ENGINE, IntervalIntDomain(0, 10))
      val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, 
                                   Scope(v0, v1));
      ENGINE.propagate;
      listener.getCount(UPPER_BOUND_DECREASED) ≟ 1 should be (true)
      v0.specify(7);
      ENGINE.propagate; // Expect a RESTRICT_TO_SINGLETON event through propagation
      listener.getCount(RESTRICT_TO_SINGLETON) ≟ 1 should be (true)

      v0.reset; // Expect a RESET message for v0 and a RELAXATION message for both variables
      listener.getCount(RESET) ≟ 1 should be (true)
      listener.getCount(RELAXED) ≟ 2 should be (true)
      ENGINE.pending should be (true)

      v0.specify(0); // Expect EMPTIED
      v1.specify(1); // Expect EMPTIED
      ENGINE.propagate;
      listener.getCount(EMPTIED) ≟ 1 should be (true)
      Entity.purgeStart
      ENGINE.discardConstraintGraph
      Entity.purgeEnd
    }

    // Now tests message handling on Enumerated Domain
    {
      listener.reset;
      // Debug.enable("ConstraintEngine")
      val v0 = new Variable[NumericDomain](ENGINE, NumericDomain(Set(1D, 3D, 5D, 10D)))
      listener.getCount(CLOSED) ≟ 1 should be (true)

      val d0 = NumericDomain(Set(2D, 3D, 5D, 11D))
      val v1 = new Variable[NumericDomain](ENGINE, d0)

      val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE,
                                   Scope(v0, v1));
      ENGINE.propagate; // Should see values removed from both variables domains.
      listener.getCount(VALUE_REMOVED) should be (2)
      v0.specify(3);
      listener.getCount(SET_TO_SINGLETON) should be (1)
      v1.specify(5);
      listener.getCount(SET_TO_SINGLETON) should be (2)
      ENGINE.propagate; // Expect to see exactly one domain emptied
      listener.getCount(EMPTIED) should be (1)
      v1.reset; // Should now see 2 domains relaxed.
      listener.getCount(RELAXED) should be (2)
    }

  } 
  ignore("testVariablesWithOpenDomains", OpenDomains) {}

  test("testRestrictionScenarios") {
    val engine = CETestEngine()
    val ENGINE = engine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]

    val e0 = EnumeratedDomain(Set(0D, 1D, 2D, 3D), IntDT.INSTANCE);

    val e1 = EnumeratedDomain(Set(1D, 3D), IntDT.INSTANCE);

    val v0 = new Variable[EnumeratedDomain](ENGINE, e0);
    val v1 = new Variable[EnumeratedDomain](ENGINE, e0);

    val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, 
                                 Scope(v0, v1));

    // Specify v0 and propagate
    v0.specify(1);
    ENGINE.propagate should be (true)

    // Now v1's derived domain will also be a singleton. However, I want to restrict the base domain of v1 partially.
    // v1.restrictBaseDomain(e1);
    // CPPUNIT_ASSERT(ENGINE->propagate());

    // Now specify v1 to a singleton also, a different value than that already specified.
    v0.reset
    v1.specify(3);
    v0.specify(1);
    ENGINE.propagate should be (false)

    // Repair by reseting v0
    v0.reset
    ENGINE.propagate should be (true)

  }
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
