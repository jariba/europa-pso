package gov.nasa.arc.europa.utils
import scala.math.abs

object Number { 
  val PLUS_INFINITY: Double = (java.lang.Integer.MAX_VALUE >> 2)
  val MINUS_INFINITY: Double = -PLUS_INFINITY
}

object Infinity { 
  def plus(v1: Double, v2: Double, default: Double): Double = { 
    import Number._
    if(abs(v1) >= PLUS_INFINITY || abs(v2) >= PLUS_INFINITY)
      return default
    val retval = v1 + v2
    if(abs(retval) >= PLUS_INFINITY)
      return default
    return retval
  }
  def minus(v1: Double, v2: Double, default: Double): Double = { 
    import Number._
    if(abs(v1) >= PLUS_INFINITY || abs(v2) >= PLUS_INFINITY)
      return default
    val retval = v1 - v2
    if(abs(retval) >= PLUS_INFINITY)
      return default
    return retval
  }

}
