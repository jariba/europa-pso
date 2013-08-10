package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.Number._

import scala.math.max
import scala.math.min

class DefaultTemporalAdvisor(val ce: ConstraintEngine) extends TemporalAdvisor { 
  import plandb._

  override def canPrecede(first: Token, second: Token): Boolean =
    canPrecede(first.end, second.start)

  override def canPrecede(first: TimeVar, second: TimeVar): Boolean =
    first.derivedDomain.lowerBound <= second.derivedDomain.upperBound

  override def canFitBetween(token: Token, predecessor: Token, successor: Token): Boolean = { 
    checkError(token != predecessor, "")
    checkError(token != successor, "")
    checkError(predecessor != successor, "")

    val minDuration = successor.start.derivedDomain.upperBound - 
                      predecessor.end.derivedDomain.lowerBound
    return minDuration >= token.duration.derivedDomain.lowerBound
  }

  override def canBeConcurrent(first: Token, second: Token): Boolean = true //?


  override def getTemporalDistanceDomain(first: TimeVar, second: TimeVar, 
                                         exact: Boolean): IntervalIntDomain = { 
    if(first.getExternalEntity == null || second.getExternalEntity == null) { 
      val (f_lb, f_ub) = first.derivedDomain.getBounds
      val (s_lb, s_ub) = second.derivedDomain.getBounds
      val min_distance = 
        if(s_lb > MINUS_INFINITY && f_ub < PLUS_INFINITY) 
          max(MINUS_INFINITY, s_lb - f_ub)
        else
          MINUS_INFINITY
      val max_distance = 
        if(f_lb > MINUS_INFINITY && s_ub < PLUS_INFINITY)
          min(PLUS_INFINITY, s_ub - f_lb)
        else
          PLUS_INFINITY
      return IntervalIntDomain(min_distance.toInt, max_distance.toInt)
    }
    else { 
      return IntervalIntDomain();
    }
  }

  override def getTemporalDistanceDomains(first: TimeVar,
                                          seconds: Vector[TimeVar]): Vector[IntervalIntDomain] = seconds.map(getTemporalDistanceDomain(first, _, true))

  override def getTemporalDistanceSigns(first: TimeVar, 
                                        seconds: Vector[TimeVar]): (Vector[Int], Vector[Int]) = { 
    getTemporalDistanceDomains(first, seconds).map(_.getBounds).map((x) => (x._1.toInt, x._2.toInt)).unzip

  }

  override def mostRecentRepropagation: Int = ce.mostRecentRepropagation

}
