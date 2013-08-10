package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
/**
 * @brief An abstract interface for inquiring about possible
 * temporal relations among tokens. It is a vital source of look-ahead information when formulating
 * merging and ordering choices.
 * @ingroup PlanDatabase
 */

trait TemporalAdvisor { 
  import plandb._
  /**
   * @brief Test if the first token can precede the second token.
   * @param first Candidate to be predecessor
   * @param second Candidate to be successor
   * @return true if temporal distance first.end to second.start can be >= 0
   */
  def canPrecede(first: Token, second: Token): Boolean

  def canPrecede(first: TimeVar, second: TimeVar): Boolean

  /**
   * @brief test if the given token can fit between the predecessor and successor.
   * @param token The token to be tested if it can fit in the middle
   * @param predecessor The token to be placed before 'token'
   * @param successor The token to be placed after 'token'
   * @return True if sumultaneously the temporal distance between predecessor.end and successor.start >= minimum token.duration and
   * canPrecede(predecessor, token) and canPrecede(token, successor)
   */
  def canFitBetween(token: Token, predecessor: Token, successor: Token): Boolean

  /**
   * @brief test of the given tokens can have a zero temporal distance between their respective timepoints. Particularly
   * useful as a look-ahead when evaluating merge candidates.
   * @param first A token to consider
   * @param second A token to consider
   * @return true if distance beteen start times includes 0 and distance between end times includes 0.
   */
  def canBeConcurrent(first: Token, second: Token): Boolean


  /**
   * @brief General utility for obtaining the min and max temporal distance between two timepoints.
   * @param first The first time point
   * @param second The second time point
   * @param exact if true, it will enforce most rigourous test and give tightest bounds. If false, it can use
   * previously calcuated results but may be quite wrong.
   */
  def getTemporalDistanceDomain(first: TimeVar, second: TimeVar, 
                                exact: Boolean): IntervalIntDomain

  /**
   * @brief Obtains exact min/max temporal distance between one and several timepoints.
   * @param first The first time point
   * @param seconds The other time points
   * @param domains The returned calculated domains.
   */
  def getTemporalDistanceDomains(first: TimeVar,
                                 seconds: Vector[TimeVar]): Vector[IntervalIntDomain]

  /**
   * @brief Obtains min/max temporal distance signs between one and several timepoints.  Only the signs (-,+,0)
   * are guaranteed accurate; the values may be arbitrary.  Utility for determining precedence relations.
   * @param first The first time point
   * @param seconds The other time points
   * @param lbs The returned lower-bound signs as numbers with correct signs but arbitrary values.
   * @param ubs The returned upper-bound signs as numbers with correct signs but arbitrary values.
   */
  //it's posible that these numbers don't need to be eints
  def getTemporalDistanceSigns(first: TimeVar, 
                               seconds: Vector[TimeVar]): (Vector[Int], Vector[Int])

  /**
   * @brief Obtains the most recent repropagation of relevant information w.r.t. time
   */
  def mostRecentRepropagation: Int
  
}
