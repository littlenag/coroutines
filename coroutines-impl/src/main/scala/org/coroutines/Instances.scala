package org.coroutines

case class CoroutineResumeException() extends Exception
case class CoroutineStoppedException() extends Exception


class ~~>[T, YR] private[coroutines](val blueprint: Coroutine._1[T, _, _]) extends Coroutine.FactoryDefMarker[YR] {

  def apply[Y, R](t: T)(implicit e: (Y, R) =:= YR): R = {
    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
  }

  def inst[@specialized Y, R](t: T)(implicit e: (Y, R) =:= YR): Coroutine.Instance[Y, R] = {
    blueprint.asInstanceOf[Coroutine._1[T, Y, R]].$call(t)
  }

  def $call[@specialized Y, R](t: T)(implicit e: (Y, R) =:= YR): Coroutine.Instance[Y, R] = {
    blueprint.asInstanceOf[Coroutine._1[T, Y, R]].$call(t)
  }

  def $push[@specialized Y, R](co: Coroutine.Instance[Y, R], t: T)(implicit e: (Y, R) =:= YR): Unit = {
    blueprint.asInstanceOf[Coroutine._1[T, Y, R]].$push(co, t)
  }
}

trait ArityAdapter[A, YR] {
  def inst[Y,R](a:A)(implicit yr: @@[Y, R] =:= YR): Coroutine.Instance[Y, R]
  //def $call[Y,R](a:A)(implicit yr: @@[Y, R] =:= YR): Coroutine.Instance[Y, R]

  def $push[Y, R](c: Coroutine.Instance[Y, R])(
    implicit yr: (Y, R) =:= YR
  ): Unit = sys.error(COROUTINE_PUSH_ARITY_ERROR_MESSAGE)
  def $push[T1, Y, R](c: Coroutine.Instance[Y, R], t1: T1)(
    implicit ps: A =:= T1, yr: (Y, R) =:= YR
  ): Unit = sys.error(COROUTINE_PUSH_ARITY_ERROR_MESSAGE)
  def $push[T1, T2, Y, R](c: Coroutine.Instance[Y, R], t1: T1, t2: T2)(
    implicit ps: A =:= Tuple2[T1, T2], yr: (Y, R) =:= YR
  ): Unit = sys.error(COROUTINE_PUSH_ARITY_ERROR_MESSAGE)
  def $push[T1, T2, T3, Y, R](c: Coroutine.Instance[Y, R], t1: T1, t2: T2, t3: T3)(
    implicit ps: A =:= Tuple3[T1, T2, T3], yr: (Y, R) =:= YR
  ): Unit = sys.error(COROUTINE_PUSH_ARITY_ERROR_MESSAGE)

}


/**
 * Coroutine factory, i.e. pre-invocation - can be invoked to create an instance of a coroutine.
 *
 * Int ~> (Unit @@ Unit)
 *
 * Pre-invocation type for coroutine which will be invoked with a single params
 *
 * This class is safe because its only created as part of an implicit conversion from an _1.
 *
 * @tparam A   Argument types, tupled if arity > 1
 * @tparam YR  Pair (YieldType ResultType)
 */
case class ~>[A, YR] private[coroutines](adapter: ArityAdapter[A,YR]) extends Coroutine.FactoryDefMarker[YR] {

  // Meant to be used INSIDE coroutine scope!
  def apply[Y,R](a:A)(implicit yr: @@[Y, R] =:= YR): R =
    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)

  // May be be used INSIDE or OUTSIDE coroutine scope!
  def inst[Y,R](a:A)(implicit yr: @@[Y, R] =:= YR): Coroutine.Instance[Y, R] =
    adapter.inst[Y,R](a)


  def $push[Y, R](c: Coroutine.Instance[Y, R])(
    implicit yr: (Y, R) =:= YR
  ): Unit = {
    adapter.$push(c)
  }

  def $push[T1, Y, R](c: Coroutine.Instance[Y, R], t1: T1)(
    implicit ps: A =:= T1, yr: (Y, R) =:= YR
  ): Unit = {
    adapter.$push(c, t1)
  }

  def $push[T1, T2, Y, R](c: Coroutine.Instance[Y, R], t1: T1, t2: T2)(
    implicit ps: A =:= Tuple2[T1, T2], yr: (Y, R) =:= YR
  ): Unit = {
    adapter.$push(c, t1, t2)
  }

  def $push[T1, T2, T3, Y, R](c: Coroutine.Instance[Y, R], t1: T1, t2: T2, t3: T3)(
      implicit ps: A =:= Tuple3[T1, T2, T3], yr: (Y, R) =:= YR
  ): Unit = {
    adapter.$push(c, t1, t2, t3)
  }
}


//class ~>[PS, YR] private[coroutines] (val blueprint: Coroutine.FactoryDefMarker[YR]) extends Coroutine.FactoryDefMarker[YR] {
//
//  def apply[T1, T2, S, R](t1: T1, t2: T2)(
//    implicit ps: PS =:= Tuple2[T1, T2], yr: (S, R) =:= YR
//  ): R = {
//    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
//  }
//  def inst[T1, T2, @specialized S, R](t1: T1, t2: T2)(
//    implicit ps: PS =:= Tuple2[T1, T2], yr: (S, R) =:= YR
//  ): Coroutine.Instance[S, R] = {
//    blueprint.asInstanceOf[Coroutine._2[T1, T2, S, R]].$call(t1, t2)
//  }
//  def $call[T1, T2, @specialized S, R](t1: T1, t2: T2)(
//    implicit ps: PS =:= Tuple2[T1, T2], yr: (S, R) =:= YR
//  ): Coroutine.Instance[S, R] = {
//    blueprint.asInstanceOf[Coroutine._2[T1, T2, S, R]].$call(t1, t2)
//  }
//
//  def apply[T1, T2, T3, Y, R](t1: T1, t2: T2, t3: T3)(
//    implicit ps: PS =:= Tuple3[T1, T2, T3], yr: (Y, R) =:= YR
//  ): R = {
//    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
//  }
//  def inst[T1, T2, T3, @specialized Y, R](t1: T1, t2: T2, t3: T3)(
//    implicit ps: PS =:= Tuple3[T1, T2, T3], yr: (Y, R) =:= YR
//  ): Coroutine.Instance[Y, R] = {
//    blueprint.asInstanceOf[Coroutine._3[T1, T2, T3, Y, R]].$call(t1, t2, t3)
//  }
//  def $call[T1, T2, T3, @specialized Y, R](t1: T1, t2: T2, t3: T3)(
//    implicit ps: PS =:= Tuple3[T1, T2, T3], yr: (Y, R) =:= YR
//  ): Coroutine.Instance[Y, R] = {
//    blueprint.asInstanceOf[Coroutine._3[T1, T2, T3, Y, R]].$call(t1, t2, t3)
//  }
//}