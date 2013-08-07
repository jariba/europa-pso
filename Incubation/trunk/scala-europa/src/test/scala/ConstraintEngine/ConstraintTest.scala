package gov.nasa.arc.europa.constraintengine.test
import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.Domain._
import gov.nasa.arc.europa.constraintengine.Scope
import gov.nasa.arc.europa.constraintengine.Variable
import gov.nasa.arc.europa.constraintengine.component.AddEqual
import gov.nasa.arc.europa.constraintengine.component.EqualConstraint
import gov.nasa.arc.europa.constraintengine.component.IntervalDomain
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
import gov.nasa.arc.europa.utils.Debug
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.LabelStr
import gov.nasa.arc.europa.utils.Number._

import com.codecommit.antixml._

import java.io.BufferedReader
import java.io.FileReader
import java.io.Reader

import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.scalatest.Tag
import org.scalatest.matchers.ShouldMatchers

import scalaz._
import Scalaz._

case class ConstraintTestCase(constraintName: String, fileName: String, caseName: String,
  inputs: Option[List[Domain]], outputs: Option[List[Domain]])

object ConstraintTestCase extends ShouldMatchers {
  def readTestCases(root: Elem, source: String): List[ConstraintTestCase] = {
    return (root \ "Constraint").map(c =>
      ConstraintTestCase(c.attrs.getOrElse("name", ""), source,
        c.attrs.getOrElse("test", ""),
        readDomains((c \ "Inputs").head),
        readDomains((c \ "Outputs").head))).toList
  }
  def readTestCases(reader: Reader, source: String): List[ConstraintTestCase] = {
    return readTestCases(XML.fromReader(reader), source)
  }
  def readTestCases(file: String): List[ConstraintTestCase] = {
    return readTestCases(new BufferedReader(new FileReader(file)), file)
  }

  def readDomains(ds: Elem): Option[List[Domain]] = {
    return (ds \ *).collect(d => d match {
      case e: Elem => e.name match {
        case "BoolDomain" | "NumericDomain" | "SymbolDomain" => None
        case "IntervalDomain" | "IntervalIntDomain" => Some(readInterval(e))
        case _ => None
      }
    }).toList.sequence
  }

  def atoef(a: String): Double = {
    if (a == "-inf") MINUS_INFINITY
    else if (a == "+inf") PLUS_INFINITY
    else if (a == "") 0
    else java.lang.Double.parseDouble(a)
  }
  def readInterval(d: Elem): Domain = {
    val lb = atoef(d.attrs.getOrElse("lb", "3"))
    val ub = atoef(d.attrs.getOrElse("ub", "-2"))
    if (d.name == "IntervalDomain") {
      return new IntervalDomain(lb, ub)
    } else {
      return new IntervalIntDomain(lb, ub)
    }
  }

  def executeTestCases(ce: ConstraintEngine, cases: List[ConstraintTestCase]): Boolean = {
    var problemCount = 0
    var successCount = 0
    var warned: Set[String] = Set()
    // Debug.enable("ConstraintEngine")
    // Debug.enable("ConstrainedVariable")
    // Debug.enable("DefaultPropagator")
    // Debug.enable("EqualConstraint")
    // Debug.enable("IntervalDomain")
    // Debug.enable("LessThanConstraint")

    for (testCase <- cases) {
      if (ce.getSchema.isConstraintType(testCase.constraintName) && !testCase.inputs.isEmpty &&
        !testCase.inputs.get.isEmpty) {
        val inputs = testCase.inputs.get
        val outputs = testCase.outputs.get

        inputs.length should equal(outputs.length)
        val scope = inputs.map {
          (d: Domain) => ce.createVariableWithDomain(d.dataType.name, d).get
        }
        val constr = ce.createConstraint(testCase.constraintName, scope)
        ce.propagate
        var problem = false
        for ((scopeVar, outputDom, i) <- (scope, outputs, 1 to outputs.length).zipped.map((a, b, c) => (a, b, c))) {
          if (outputDom.isEmpty != scopeVar.derivedDomain.isEmpty) {
            if (!problem) {
              Console.println(testCase.fileName + ":" + testCase.caseName +
                ": unexpected result propagating " + testCase.constraintName)
            }
            Console.println(";\n  argument " + i + " is " + scopeVar.derivedDomain + "\n  rather than " + outputDom)
            problem = true
          } else if (!(scopeVar.derivedDomain eq outputDom)) {
            if (!problem) {
              Console.println(testCase.fileName + ":" + testCase.caseName +
                ": unexpected result propagating " + testCase.constraintName)
            }
            Console.println(";\n  argument " + i + " is " + scopeVar.derivedDomain + "\n  rather than " + outputDom)
            problem = true
          }
        }
        if (problem) problemCount = problemCount + 1
        else successCount = successCount + 1
        constr.discard
        scope map (_.discard)
      } else if (!ce.getSchema.isConstraintType(testCase.constraintName)) {
        if (!warned.contains(testCase.constraintName)) {
          Console.println("\n    Warning: " + testCase.fileName + ":" + testCase.caseName +
            ": constraint " + testCase.constraintName + " is unregistered; skipping tests of it.\n")
          warned = warned + testCase.constraintName
        }
      } else {
        Console.println("Skipping case because domains weren't readable: " + testCase)
      }
    }
    Console.println(successCount + " successes")
    
    return problemCount == 0
  }
}

object RunThisOne extends Tag("tags.RunThisOne")

class ConstraintTest extends FunSuite with ShouldMatchers with BeforeAndAfter with BeforeAndAfterEach {
  var testEngine: CETestEngine = _
  var engine: ConstraintEngine = _

  override def beforeEach() { 
    testEngine = CETestEngine()
    engine = testEngine.getComponent("ConstraintEngine").asInstanceOf[ConstraintEngine]
  }
  
  test("constraints") {
    val case1 = ConstraintTestCase("Equal", "ConstraintTest.scala", "1",
      Some(List(IntervalIntDomain(1, 10), IntervalIntDomain(2, 11))),
      Some(List(IntervalIntDomain(2, 10), IntervalIntDomain(2, 10))))
    val clibTestName = "/CLibTestCases.xml"
    val clibTestURL = getClass.getResource(clibTestName)
    val clibTestFile = clibTestURL.getFile

    val rest = ConstraintTestCase.readTestCases(clibTestFile) //????
    ConstraintTestCase.executeTestCases(engine, case1 :: rest) should equal(true)
  }

  ignore("GNATS_3181") {}
  ignore("UnaryConstraint") {}
  test("AddEqualConstraint") {

    // Now test special case of rounding with negative domain bounds.
    try {
      val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(-10, 10));
      val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(-10, 10));
      val v2 = new Variable[IntervalDomain](engine, IntervalDomain(0.01, 0.99));
      val c0 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v0, v1, v2));
      val res = engine.propagate;
      !res should be (true);
    }
    finally { 
      Entity.purgeStart
      engine.discardConstraintGraph
      Entity.purgeEnd
    }

    // Another, similar, case of rounding with negative domain bounds.
    try {
      val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(-10, 10));
      val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(-10, 10));
      val v2 = new Variable[IntervalDomain](engine, IntervalDomain(0.01, 1.99));
      val c0 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v0, v1, v2));
      val res = engine.propagate
      res should be (true);
      // Require correct result to be in v2's domain.
      v2.derivedDomain.isMember(1.0) should be (true);
      // Following is false because implementation of AddEqualConstraint is not smart enough to deduce it.
      //v2.derivedDomain.getSingletonValue == 1.0 should be (true);
    }
    finally { 
      Entity.purgeStart
      engine.discardConstraintGraph
      Entity.purgeEnd
    }


    // Confirm correct result with all singletons.
    try {
      val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(-1, -1));
      val v1 = new Variable[IntervalDomain](engine, IntervalDomain(10.4, 10.4));
      val v2 = new Variable[IntervalDomain](engine, IntervalDomain(9.4, 9.4));
      val c0 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v0, v1, v2));
      val res = engine.propagate
      res should be (true);
    }
    finally { 
      Entity.purgeStart
      engine.discardConstraintGraph
      Entity.purgeEnd
    }


    // Confirm inconsistency detected with all singletons.
    try {
      val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(-1, -1));
      val v1 = new Variable[IntervalDomain](engine, IntervalDomain(10.4, 10.4));
      val v2 = new Variable[IntervalDomain](engine, IntervalDomain(9.39, 9.39));
      val c0 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v0, v1, v2));
      val res = engine.propagate
      !res should be (true);
    }
    finally { 
      Entity.purgeStart
      engine.discardConstraintGraph
      Entity.purgeEnd
    }


    // Obtain factors correct values for fixed result.
    try {
      val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(0, PLUS_INFINITY.toInt));
      val v1 = new Variable[IntervalDomain](engine, IntervalDomain(0, PLUS_INFINITY));
      val v2 = new Variable[IntervalDomain](engine, IntervalDomain(9.390, 9.390));
      val c0 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v0, v1, v2));
      val res = engine.propagate
      res should be (true);
      // TODO v0.derivedDomain eq IntervalIntDomain(0, 9) should be (true);
      // TODO v1.derivedDomain eq IntervalDomain(0.39, 9.39) should be (true);
    }
    finally { 
      Entity.purgeStart
      engine.discardConstraintGraph
      Entity.purgeEnd
    }


    // Test handling with all infinites
    try {
      // Debug.enable("ConstraintEngine")
      // Debug.enable("AddEqual")
      val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(MINUS_INFINITY.toInt, MINUS_INFINITY.toInt));
      val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, PLUS_INFINITY.toInt));
      val v2 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(PLUS_INFINITY.toInt, PLUS_INFINITY.toInt));
      val c0 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v0, v1, v2));
      val res = engine.propagate
      res should be (true);
      v0.derivedDomain eq IntervalIntDomain(MINUS_INFINITY.toInt, MINUS_INFINITY.toInt) should be (true);
      v1.derivedDomain eq IntervalIntDomain(1, PLUS_INFINITY.toInt) should be (true);
      v2.derivedDomain eq IntervalIntDomain(PLUS_INFINITY.toInt, PLUS_INFINITY.toInt) should be (true);
    }
    finally { 
      Entity.purgeStart
      engine.discardConstraintGraph
      Entity.purgeEnd
    }

    // Test handling with infinites and non-infinites
    try {
      val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(10, PLUS_INFINITY.toInt));
      val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, PLUS_INFINITY.toInt));
      val v2 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(MINUS_INFINITY.toInt, 100));
      val c0 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v0, v1, v2));
      val res = engine.propagate
      res should be (true);
      v0.derivedDomain eq IntervalIntDomain(10, 99) should be (true);
      v1.derivedDomain eq IntervalIntDomain(1, 90) should be (true);
      v2.derivedDomain eq IntervalIntDomain(11, 100) should be (true);
    }
    finally { 
      Entity.purgeStart
      engine.discardConstraintGraph
      Entity.purgeEnd
    }


    // Test propagating infinites: start + duration == end.
    try {
      val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(MINUS_INFINITY.toInt, PLUS_INFINITY.toInt));
      val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1));
      val v2 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(MINUS_INFINITY.toInt, PLUS_INFINITY.toInt));
      val c0 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v0, v1, v2));
      val res = engine.propagate
      res should be (true);
      /* TODO, fix characters
      v0.derivedDomain eq IntervalIntDomain(MINUS_INFINITY.toInt, PLUS_INFINITY.toInt) should be (true);
      v1.derivedDomain eq IntervalIntDomain(1) should be (true);
      v2.derivedDomain eq IntervalIntDomain(MINUS_INFINITY.toInt, PLUS_INFINITY.toInt) should be (true);
      */
    }
    finally { 
      Entity.purgeStart
      engine.discardConstraintGraph
      Entity.purgeEnd
    }


    // Test that we can use the constraint on a variable that is present in the constraint more than once
    try {
      val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(10, 14));
      val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(0, 1));
      val c0 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v0, v1, v0));
      val res = engine.propagate
      res should be (true);
      v1.specify(1);
      !engine.propagate should be (true);
      v1.reset
      engine.propagate should be (true);
      v0.specify(11);
      engine.propagate should be (true);
      v1.derivedDomain eq IntervalIntDomain(0) should be (true);
    }
    finally { 
      Entity.purgeStart
      engine.discardConstraintGraph
      Entity.purgeEnd
    }
  }

  ignore("LessThanEqualConstraint") {} //commented out in Europa-C++!
  ignore("LessOrEqThanSumConstraint") {}
  test("BasicPropagation") {

    // v0 == v1
    val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), engine, List(v0, v1));

    // v2 + v3 == v0
    val v2 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 4));
    val v3 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 1));
    val c1 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v2, v3, v0));
    !v0.derivedDomain.isEmpty should be (true);

    // v4 + v5 == v1
    val v4 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v5 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 1000));
    val c2 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v4, v5, v1));

    engine.propagate;
    engine.constraintConsistent should be (true);
    !v4.derivedDomain.isEmpty should be (true);

  }
  test("Deactivation") {
    val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), engine, List(v0, v1));

    engine.propagate;
    engine.constraintConsistent should be (true)

    v0.deactivate;
    !c0.isActive should be (true)
    c0.deactivationCount == 1 should be (true)

    v1.deactivate;
    !c0.isActive should be (true)
    c0.deactivationCount == 2 should be (true)

    c0.deactivate;
    c0.deactivationCount == 3 should be (true)
    v0.undoDeactivation;
    v1.undoDeactivation;
    c0.deactivationCount == 1 should be (true)
    !c0.isActive should be (true)

    v1.deactivate;
    !c0.isActive should be (true)
    c0.deactivationCount == 2 should be (true)
    v1.undoDeactivation;
    c0.deactivationCount == 1 should be (true)
    c0.undoDeactivation;
    c0.isActive should be (true)

    // // Now restrict the base domains to automatically deactivate
    // v0.restrictBaseDomain(IntervalIntDomain(1, 1));
    // c0.isActive should be (true)
    // v1.restrictBaseDomain(IntervalIntDomain(1, 1));
    // c0.isActive should be (true) // Have not propagated yet!
    // engine.propagate;
    // !c0.isActive should be (true) // Now we have propagated, so should be deactivated.

    // // Make sure it stays deactive
    // c0.undoDeactivation;
    // !c0.isActive should be (true)

    // val v2 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    // val v3 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    // val c1 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), engine, List(v2, v3));
    // engine.propagate;
    // v2.restrictBaseDomain(IntervalIntDomain(1, 1));
    // v3.restrictBaseDomain(IntervalIntDomain(2, 2));

    // // Now propagate. The constraint will not be deactivated.
    // engine.propagate;
    // c1.isActive should be (true)

  }
  ignore("DeactivationWithRestrictBaseDomain") { }
  test("ForceInconsistency") {
    // v0 == v1
    val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), engine, List(v0, v1));

    // v2 + v3 == v0
    val v2 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 1));
    val v3 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 1));
    val c1 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v2, v3, v0));

    // v4 + v5 == v1
    val v4 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(2, 2));
    val v5 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(2, 2));
    val c2 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v4, v5, v1));

    engine.propagate;
    engine.provenInconsistent should be (true)
    v1.derivedDomain.isEmpty || v2.derivedDomain.isEmpty should be (true)

    val variables = List(v0, v1, v2, v3, v4, v5)
    val empties = variables.filter(x => x.lastDomain.isEmpty)
    empties should have length 1
  }
  test("Repropagation") {
    // v0 == v1
    val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), engine, List(v0, v1));


    // v2 + v3 == v0
    val v2 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v3 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val c1 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v2, v3, v0));

    // v4 + v5 == v1
    val v4 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v5 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val c2 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine, List(v4, v5, v1));

    engine.propagate;
    /* TODO
    engine.constraintConsistent should be (true);
    v0.specify(IntervalIntDomain(8, 10));
    v1.specify(IntervalIntDomain(2, 7));
    engine.pending should be (true);

    engine.propagate;
    engine.provenInconsistent should be (true);
    */
    v0.reset;
    engine.pending should be (true);
    engine.propagate;
    engine.constraintConsistent should be (true);

    /* Call reset on a constraint consistent network - not sure one would want to do this. */
    v1.reset;
    engine.pending should be (true); /* Strictly speaking we know it is not inconsistent here since all we have done is relax a previously
				  consistent network. However, we have to propagate to find the new derived domains based on relaxed
				  domains. */
    engine.propagate;
    engine.constraintConsistent should be (true);

  }
  test("ConstraintRemoval", RunThisOne) {
    // Debug.enable("ConstraintEngine")
    Entity.isPurging should be (false)
    // v0 == v1
    val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));

    val c0 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), engine,
                                 List(v0, v1))

    // v2 + v3 == v0
    val v2 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v3 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val c1 = new AddEqual(LabelStr("AddEqualConstraint"), LabelStr("Default"), engine,
                                    List(v2, v3, v0))

    engine.propagate;
    engine.constraintConsistent should be (true);

    /* Show that we can simply delete a constraint and confirm that the system is still consistent. */
    Entity.isPurging should be (false)
    val before = engine.constraints.size
    // Debug.enable("ConstraintEngine")
    c1.discard
    before should be > (engine.constraints.size)

    engine.propagate;
    engine.constraintConsistent should be (true);

    val v4 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 1));
    val c2 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), engine,
                                 List(v0, v4))
    engine.propagate;
    engine.constraintConsistent should be (true);
    v1.derivedDomain.getSingletonValue.get == 1 should be (true);

    c2.discard
    engine.propagate;
    engine.constraintConsistent should be (true);
    v1.derivedDomain.getUpperBound == 10 should be (true);

    /* Add a constraint to force an inconsistency and show that consistency can be restored by removing the
     * constraint. */
    val v5 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(0, 0));
    val c3 = new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), engine,
                                 List(v0, v5))
    engine.propagate;
    engine.provenInconsistent should be (true);
    c3.discard
    engine.propagate;
    engine.constraintConsistent should be (true);

    // Clean up remaining constraint
    c0.discard
    engine.propagate;
    engine.constraintConsistent should be (true);

  }

  // object DelegationTestConstraint { 
  //   var s_executionCount = 0
  //   var s_instanceCount = 0
  // }
  // class DelegationTestConstraint(name: LabelStr, pName: LabelStr, ce: ConstraintEngine, vars: Seq[ConstrainedVariable]) extends Constraint(name, pName, ce, vars) with ShouldMatchers { 
  //   import DelegationTestConstraint._
  //   s_instanceCount = s_instanceCount + 1
  //   override def handleDiscard: Unit = { 
  //     s_instanceCount = s_instanceCount - 1
  //     super.handleDiscard
  //   }
  //   override def handleExecute: Unit = { 
  //     s_executionCount = s_executionCount + 1
  //     !getScope.head.derivedDomain.isSingleton should be (true)
  //   }
  // }

  ignore("Delegation") {}
  ignore("NotEqual") {} //commented out in Europa-C++!
  ignore("MultEqualConstraint") {}
  ignore("AddMultEqualConstraint") {}
  ignore("EqualSumConstraint") {}
  ignore("CondAllSameConstraint") {}
  ignore("CondAllDiffConstraint") {}
  ignore("ConstraintDeletion") {
    val v0 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 10));
    val v1 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(1, 100));
    val v2 = new Variable[IntervalIntDomain](engine, IntervalIntDomain(10, 100));
    val v3 = new Variable[IntervalIntDomain](engine, IntervalIntDomain());

    val c0 = new EqualConstraint(LabelStr("eq"), LabelStr("Default"), engine, Scope(v0, v1))
    val c1 = new EqualConstraint(LabelStr("eq"), LabelStr("Default"), engine, Scope(v1, v2))
    val c2 = new EqualConstraint(LabelStr("eq"), LabelStr("Default"), engine, Scope(v2, v3))

    // Force an inconsistency
    v0.specify(1);
    v1.specify(1);
    var res = engine.propagate;
    !res should be (true);

    // Reset, and delete constraint, but it should not matter
    v1.reset;
    c2.discard

    // Confirm still inconsistent
    res = engine.propagate;
    !res should be (true);

    c0.discard
    c1.discard
  }
  ignore("LockConstraint") {}
  ignore("NegateConstraint") {}
  ignore("UnaryQuery") {}
  ignore("TestEqConstraint") {}
  ignore("TestLessThanConstraint") {}
  ignore("TestLEQConstraint") {}
  ignore("GNATS_3075") {}

}

