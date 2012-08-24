package gov.nasa.arc.europa.utils;

/** <p>
 *   The <code>OneOf</code> type represents a value of one of three possible
 *   types (a disjoint union). The data constructors <code>Left</code>,
 *   <code>Right</code>, and <code>Middle</code> represent the three possible values.
 *  </p>
 *
 *  @author <a href="mailto:Michael.J.Iatauro@nasa.gov">Michael Iatauro</a>
 *  @version 1.0
 */
//sealed
abstract class OneOf[+A, +B, +C] {
  /**
   * Projects this <code>OneOf</code> as a <code>Left</code>.
   */
  def left = OneOf.LeftProjection(this)

  /**
   * Projects this <code>OneOf</code> as a <code>Right</code>.
   */
  def right = OneOf.RightProjection(this)

  /**
   * Projects this <code>OneOf</code> as a <code>Middle</code>.
   */
  def middle = OneOf.MiddleProjection(this)


  /**
   * Deconstruction of the <code>OneOf</code> type (in contrast to pattern matching).
   */
  def fold[X](fl: A => X, fm: B => X, fr: C => X) = this match {
    case Left(a) => fl(a)
    case Middle(b) => fm(b)
    case Right(c) => fr(c)
  }

  /**
   * If this is a <code>Left</code>, then return the left value in <code>Right</code> or vice versa.
   */
  def swap = this match {
    case Left(a) => Right(a)
    case Middle(b) => Middle(b)
    case Right(c) => Left(c)
  }
  
  //TODO: figure out the joins
  /**
   * Joins an <code>OneOf</code> through <code>Right</code>.
   */
  // def joinRight[A1 >: A, B1 >: B, C1 >: C, D](implicit ev: B1 <:< OneOf[A1, B1, C]): OneOf[A1, B1, C] = this match {
  //   case Left(a)  => Left(a)
  //   case Middle(b) => Middle(b)
  //   case Right(c) => c
  // }
  
  /**
   * Joins an <code>OneOf</code> through <code>Left</code>.
   */
  // def joinLeft[A1 >: A, B1 >: B, , C1 >: C, D](implicit ev: A1 <:< OneOf[C, B1]): OneOf[C, B1] = this match {
  //   case Left(a)  => a
  //   case Right(b) => Right(b)
  // }

  /**
   * Returns <code>true</code> if this is a <code>Left</code>, <code>false</code> otherwise.
   */
  def isLeft: Boolean
  
  /**
   * Returns <code>true</code> if this is a <code>Right</code>, <code>false</code> otherwise.
   */
  def isRight: Boolean
  
  def isMiddle: Boolean
}

/**
 * The left side of the disjoint union, as opposed to the <code>Right</code> side.
 *
 * @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
 * @version 1.0, 11/10/2008
 */
final case class Left[+A, +B, +C](a: A) extends OneOf[A, B, C] { 
  def isLeft = true
  def isRight = false
  def isMiddle = false
}

final case class Middle[+A, +B, +C](b: B) extends OneOf[A, B, C] { 
  def isLeft = false
  def isRight = false
  def isMiddle = true
}

/**
 * The right side of the disjoint union, as opposed to the <code>Left</code> side.
 *
 * @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
 * @version 1.0, 11/10/2008 
 */ 
final case class Right[+A, +B, +C](c: C) extends OneOf[A, B, C] {
  def isLeft = false
  def isRight = true
  def isMiddle = false;
}

object OneOf {
  class MergeableOneOf[A](x: OneOf[A, A, A]) {
    def merge: A = x match {
      case Left(a)  => a
      case Right(a) => a
      case Middle(a) => a
    }
  }
  
  implicit def oneof2mergeable[A](x: OneOf[A, A, A]): MergeableOneOf[A] = new MergeableOneOf(x)

  /**
   * Projects an <code>OneOf</code> into a <code>Left</code>.
   *
   * @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
   * @version 1.0, 11/10/2008
   */
  final case class LeftProjection[+A, +B, +C](e: OneOf[A, B, C]) {
    /**
     * Returns the value from this <code>Left</code> or throws <code>Predef.NoSuchElementException</code>
     * if this is a <code>Right</code>.
     *
     * @throws Predef.NoSuchElementException if the option is empty.
     */
    def get = e match {
      case Left(a) => a
      case Right(_) =>  throw new NoSuchElementException("OneOf.left.value on Right")
      case Middle(_) =>  throw new NoSuchElementException("OneOf.left.value on Middle")
    }

    /**
     * Executes the given side-effect if this is a <code>Left</code>.
     *
     * @param e The side-effect to execute.
     */
    def foreach[U](f: A => U) = e match {
      case Left(a) => f(a)
      case Right(_) => {}
      case Middle(_) => {}
    }

    /**
     * Returns the value from this <code>Left</code> or the given argument if this is a
     * <code>Right</code>.
     */
    def getOrElse[AA >: A](or: => AA) = e match {
      case Left(a) => a
      case Right(_) => or
      case Middle(_) => or
    }

    /**
     * Returns <code>true</code> if <code>Right</code> or returns the result of the application of
     * the given function to the <code>Left</code> value.
     */
    def forall(f: A => Boolean) = e match {
      case Left(a) => f(a)
      case Right(_) => true
      case Middle(_) => true
    }

    /**
     * Returns <code>false</code> if <code>Right</code> or returns the result of the application of
     * the given function to the <code>Left</code> value.
     */
    def exists(f: A => Boolean) = e match {
      case Left(a) => f(a)
      case Right(_) => false
      case Middle(_) => false
    }

    /**
     * Binds the given function across <code>Left</code>.
     *
     * @param The function to bind across <code>Left</code>.
     */
    // def flatMap[BB >: B, X](f: A => OneOf[X, BB]) = e match {
    //   case Left(a) => f(a)
    //   case Right(b) => Right(b)
    // }

    /**
     * Maps the function argument through <code>Left</code>.
     */
    def map[X](f: A => X) = e match {
      case Left(a) => Left(f(a))
      case Right(b) => Right(b)
      case Middle(c) => Middle(c)
    }

    /**
     * Returns <code>None</code> if this is a <code>Right</code> or if the given predicate
     * <code>p</code> does not hold for the left value, otherwise, returns a <code>Left</code>.
     */
    def filter[Y, Z](p: A => Boolean): Option[OneOf[A, Y, Z]] = e match {
      case Left(a) => if(p(a)) Some(Left(a)) else None
      case Right(b) => None
      case Middle(c) => None
    }

    /**
     * Returns a <code>Seq</code> containing the <code>Left</code> value if it exists or an empty
     * <code>Seq</code> if this is a <code>Right</code>.
     */
    def toSeq = e match {
      case Left(a) => Seq(a)
      case Right(_) => Seq.empty
      case Middle(_) => Seq.empty
    }

    /**
     * Returns a <code>Some</code> containing the <code>Left</code> value if it exists or a
     * <code>None</code> if this is a <code>Right</code>.
     */
    def toOption = e match {
      case Left(a) => Some(a)
      case Right(_) => None
      case Middle(_) => None
    }
  }

  /**
   * Projects an <code>OneOf</code> into a <code>Right</code>.
   *
   * @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
   * @version 1.0, 11/10/2008
   */
  final case class MiddleProjection[+A, +B, +C](e: OneOf[A, B, C]) {
    /**
     * Returns the value from this <code>Right</code> or throws 
     * <code>Predef.NoSuchElementException</code> if this is a <code>Left</code>.
     *
     * @throws Predef.NoSuchElementException if the projection is <code>Left</code>.
     */
    def get = e match {
      case Left(_) =>  throw new NoSuchElementException("OneOf.right.value on Left")
      case Middle(a) => a
      case Right(_) =>  throw new NoSuchElementException("OneOf.right.value on Right")
    }

    /**
     * Executes the given side-effect if this is a <code>Right</code>.
     *
     * @param e The side-effect to execute.
     */
    def foreach[U](f: B => U) = e match {
      case Left(_) => {}
      case Middle(b) => f(b)
      case Right(_) => {}
    }

    /**
     * Returns the value from this <code>Right</code> or the given argument if this is a
     * <code>Left</code>.
     */
    def getOrElse[BB >: B](or: => BB) = e match {
      case Left(_) => or
      case Middle(b) => b
      case Right(_) => or
    }

    /**
     * Returns <code>true</code> if <code>Left</code> or returns the result of the application of
     * the given function to the <code>Right</code> value.
     */
    def forall(f: B => Boolean) = e match {
      case Left(_) => true
      case Middle(b) => f(b)
      case Right(_) => true
    }

    /**
     * Returns <code>false</code> if <code>Left</code> or returns the result of the application of
     * the given function to the <code>Right</code> value.
     */
    def exists(f: B => Boolean) = e match {
      case Left(_) => false
      case Middle(b) => f(b)
      case Right(_) => false
    }

    /**
     * Binds the given function across <code>Right</code>.
     *
     * @param The function to bind across <code>Right</code>.
     */
    // def flatMap[AA >: A, Y](f: B => OneOf[AA, Y]) = e match {
    //   case Left(a) => Left(a)
    //   case Right(b) => f(b)
    // }

    /**
     * Maps the function argument through <code>Right</code>.
     */
    def map[Y](f: B => Y) = e match {
      case Left(a) => Left(a)
      case Middle(b) => Middle(f(b))
      case Right(c) => Right(c)
    }

    /** Returns <code>None</code> if this is a <code>Left</code> or if the
     *  given predicate <code>p</code> does not hold for the right value,
     *  otherwise, returns a <code>Right</code>.
     */
    def filter[X, Y](p: B => Boolean): Option[OneOf[X, B, Y]] = e match {
      case Left(_) => None
      case Middle(b) => if(p(b)) Some(Middle(b)) else None
      case Right(_) => None
    }

    /** Returns a <code>Seq</code> containing the <code>Right</code> value if
     *  it exists or an empty <code>Seq</code> if this is a <code>Left</code>.
     */
    def toSeq = e match {
      case Left(_) => Seq.empty
      case Middle(b) => Seq(b)
      case Right(_) => Seq.empty
    }

    /** Returns a <code>Some</code> containing the <code>Right</code> value
     *  if it exists or a <code>None</code> if this is a <code>Left</code>.
     */
    def toOption = e match {
      case Left(_) => None
      case Middle(b) => Some(b)
      case Right(_) => None
    }
  }

  /**
   * Projects an <code>OneOf</code> into a <code>Right</code>.
   *
   * @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
   * @version 1.0, 11/10/2008
   */
  final case class RightProjection[+A, +B, +C](e: OneOf[A, B, C]) {
    /**
     * Returns the value from this <code>Right</code> or throws 
     * <code>Predef.NoSuchElementException</code> if this is a <code>Left</code>.
     *
     * @throws Predef.NoSuchElementException if the projection is <code>Left</code>.
     */
    def get = e match {
      case Left(_) =>  throw new NoSuchElementException("OneOf.right.value on Left")
      case Middle(_) =>  throw new NoSuchElementException("OneOf.right.value on Middle")
      case Right(a) => a
    }

    /**
     * Executes the given side-effect if this is a <code>Right</code>.
     *
     * @param e The side-effect to execute.
     */
    def foreach[U](f: C => U) = e match {
      case Left(_) => {}
      case Middle(_) => {}
      case Right(b) => f(b)
    }

    /**
     * Returns the value from this <code>Right</code> or the given argument if this is a
     * <code>Left</code>.
     */
    def getOrElse[CC >: C](or: => CC) = e match {
      case Left(_) => or
      case Middle(_) => or
      case Right(b) => b
    }

    /**
     * Returns <code>true</code> if <code>Left</code> or returns the result of the application of
     * the given function to the <code>Right</code> value.
     */
    def forall(f: C => Boolean) = e match {
      case Left(_) => true
      case Middle(_) => true
      case Right(b) => f(b)
    }

    /**
     * Returns <code>false</code> if <code>Left</code> or returns the result of the application of
     * the given function to the <code>Right</code> value.
     */
    def exists(f: C => Boolean) = e match {
      case Left(_) => false
      case Middle(_) => false
      case Right(b) => f(b)
    }

    /**
     * Binds the given function across <code>Right</code>.
     *
     * @param The function to bind across <code>Right</code>.
     */
    // def flatMap[AA >: A, Y](f: B => OneOf[AA, Y]) = e match {
    //   case Left(a) => Left(a)
    //   case Right(b) => f(b)
    // }

    /**
     * Maps the function argument through <code>Right</code>.
     */
    def map[Y](f: C => Y) = e match {
      case Left(a) => Left(a)
      case Middle(b) => Middle(b)
      case Right(c) => Right(f(c))
    }

    /** Returns <code>None</code> if this is a <code>Left</code> or if the
     *  given predicate <code>p</code> does not hold for the right value,
     *  otherwise, returns a <code>Right</code>.
     */
    def filter[X, Y](p: C => Boolean): Option[OneOf[X, Y, C]] = e match {
      case Left(_) => None
      case Middle(_) => None
      case Right(b) => if(p(b)) Some(Right(b)) else None
    }

    /** Returns a <code>Seq</code> containing the <code>Right</code> value if
     *  it exists or an empty <code>Seq</code> if this is a <code>Left</code>.
     */
    def toSeq = e match {
      case Left(_) => Seq.empty
      case Middle(_) => Seq.empty
      case Right(b) => Seq(b)
    }

    /** Returns a <code>Some</code> containing the <code>Right</code> value
     *  if it exists or a <code>None</code> if this is a <code>Left</code>.
     */
    def toOption = e match {
      case Left(_) => None
      case Middle(_) => None
      case Right(b) => Some(b)
    }
  }

  // @deprecated("use `x.joinLeft'")
  // def joinLeft[A, B](es: OneOf[OneOf[A, B], B]) =
  //   es.left.flatMap(x => x)

  // @deprecated("use `x.joinRight'")
  // def joinRight[A, B](es: OneOf[A, OneOf[A, B]]) =
  //   es.right.flatMap(x => x)
    
  /**
   * Takes an <code>OneOf</code> to its contained value within <code>Left</code> or 
   * <code>Right</code>.
   */
  // @deprecated("use `x.merge'")
  // def merge[T](e: OneOf[T, T]) = e match {
  //   case Left(t) => t
  //   case Right(t) => t
  // }
   
  /** If the condition satisfies, return the given A in <code>Left</code>,
   *  otherwise, return the given B in <code>Right</code>.
   */
  // def cond[A, B, C](test: Boolean, right: => C, left: => A, middle => B): OneOf[A, B] = 
  //   if (test) Right(right) else Left(left)
}
