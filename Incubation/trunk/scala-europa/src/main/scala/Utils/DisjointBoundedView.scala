package gov.nasa.arc.europa.utils;

object DisjointBoundedView {
  type or[A, B] = Either[A, B]
  implicit def l[T](t: T) = Left(t);
  implicit def r[T](t: T) = Right(t);
  implicit def ll[T](t: T) = l(l(t));
  implicit def lr[T](t: T) = l(r(t));
  implicit def rl[T](t: T) = r(l(t));
  implicit def rr[T](t: T) = r(r(t));

  implicit def lll[T](t: T) = l(ll(t));
  implicit def llr[T](t: T) = l(lr(t));
  implicit def lrl[T](t: T) = l(rl(t));
  implicit def lrr[T](t: T) = l(rr(t));

  implicit def rll[T](t: T) = r(ll(t));
  implicit def rlr[T](t: T) = r(lr(t));
  implicit def rrl[T](t: T) = r(rl(t));
  implicit def rrr[T](t: T) = r(rr(t));

  implicit def llll[T](t: T) = l(lll(t));
  implicit def lllr[T](t: T) = l(llr(t));
  implicit def llrl[T](t: T) = l(lrl(t));
  implicit def llrr[T](t: T) = l(lrr(t));
  implicit def lrll[T](t: T) = l(rll(t));
  implicit def lrlr[T](t: T) = l(rlr(t));
  implicit def lrrl[T](t: T) = l(rrl(t));
  implicit def lrrr[T](t: T) = l(rrr(t));

  implicit def rlll[T](t: T) = r(lll(t));
  implicit def rllr[T](t: T) = r(llr(t));
  implicit def rlrl[T](t: T) = r(lrl(t));
  implicit def rlrr[T](t: T) = r(lrr(t));
  implicit def rrll[T](t: T) = r(rll(t));
  implicit def rrlr[T](t: T) = r(rlr(t));
  implicit def rrrl[T](t: T) = r(rrl(t));
  implicit def rrrr[T](t: T) = r(rrr(t));

  //add more as necessary
}

/*
 *Note:  in the event of performance problems with two-item disjoint sets, you can do this:
 * type not[A] = A => Nothing
 * type notnot[A] = not[not[A]]
 * type OR[T, U] = not[not[T] with not[U]]
 * type or[T, U] = {type l[X] = notnot[X] <:< (T OR U)}
 *
 * which prevents boxing
 */
