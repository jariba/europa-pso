package gov.nasa.arc.europa.utils.test

import gov.nasa.arc.europa.utils.Number._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import scala.math.abs

import scalaz._
import Scalaz._

class NumberTest extends FunSuite with ShouldMatchers { 
  test("InfinityTest") { 
    PLUS_INFINITY == PLUS_INFINITY should be (true)
    MINUS_INFINITY == MINUS_INFINITY should be (true)
    PLUS_INFINITY == -(MINUS_INFINITY) should be (true)
    MINUS_INFINITY == -(PLUS_INFINITY) should be (true)
    abs(MINUS_INFINITY) == PLUS_INFINITY should be (true)
    abs(MINUS_INFINITY) >= PLUS_INFINITY should be (true)
    PLUS_INFINITY + MINUS_INFINITY should equal (0.0)
  }
}
