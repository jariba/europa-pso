package gov.nasa.arc.europa.utils

import scalaz.Equal

object EqualImplicits {
  implicit def intEq = Equal.equalA[Int]
  implicit def doubleEq = Equal.equalA[Double]
  implicit def stringEq = Equal.equalA[String]
  implicit def optionEq[A](implicit E: Equal[A]): Equal[Option[A]] = Equal.equal {
    case(Some(a1), Some(a2)) => E.equal(a1, a2)
    case(a1, a2) => a1.isDefined == a2.isDefined
  }
  implicit def tupleEq[A, B](implicit E: Equal[A], F: Equal[B]): Equal[(A, B)] = Equal.equal {
    case(a1, a2) => E.equal(a1._1, a2._1) && F.equal(a1._2, a2._2)
  }
}
