package gov.nasa.arc.europa.utils.test

import gov.nasa.arc.europa.utils.Error._

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class TestException(s: String) extends Exception(s)

class ErrorTest extends FunSuite with ShouldMatchers { 
  test("exception test") { 
    gov.nasa.arc.europa.utils.Error.doNotDisplayErrors
    gov.nasa.arc.europa.utils.Error.doThrowExceptions
    val thrown = evaluating { checkError(ALWAYS_FAIL, "Test")} should produce [Exception]
    thrown.getMessage should equal ("ErrorTest.scala:14: Test")
    val nextThrown = evaluating { checkError[TestException](ALWAYS_FAIL, "Test") } should produce [TestException]
    nextThrown.getMessage should equal ("ErrorTest.scala:16: Test")
  }
}
