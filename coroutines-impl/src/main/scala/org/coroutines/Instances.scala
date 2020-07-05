package org.coroutines

case class CoroutineResumeException() extends Exception
case class CoroutineStoppedException() extends Exception

/**
 * Coroutine factory, i.e. pre-invocation - can be invoked to create an instance of a coroutine.
 *
 * Pre-invocation type for zero arg coroutine
 *
 * @param blueprint Generalized coroutine factory
 * @tparam Y Yield Type
 * @tparam R Result Type
 */
class ~~~>[@specialized Y, R] private[coroutines](val blueprint: Coroutine[Y, R]) extends Coroutine.FactoryDefMarker[(Y, R)] {
  def apply(): R =
    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
  def $call(): Coroutine.Instance[Y, R] =
    blueprint.asInstanceOf[Coroutine._0[Y, R]].$call()
  def $push(co: Coroutine.Instance[Y, R]): Unit =
    blueprint.asInstanceOf[Coroutine._0[Y, R]].$push(co)
}

/**
 * Coroutine factory, i.e. pre-invocation - can be invoked to create an instance of a coroutine.
 *
 * Pre-invocation type for coroutine which will be invoked with a single params
 *
 * @param blueprint Generalized coroutine factory
 * @tparam T   1st param type, untupled
 * @tparam YR  Equals pair (S,R) => (YieldType ResultType)
 */
class ~~>[T, YR] private[coroutines] (val blueprint: Coroutine.FactoryDefMarker[YR]) extends Coroutine.FactoryDefMarker[YR] {

  def apply[@specialized Y, R](t: T)(implicit e: (Y, R) =:= YR): R = {
    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
  }

  def $call[@specialized Y, R](t: T)(implicit e: (Y, R) =:= YR): Coroutine.Instance[Y, R] = {
    blueprint.asInstanceOf[Coroutine._1[T, Y, R]].$call(t)
  }

  def $push[@specialized Y, R](co: Coroutine.Instance[Y, R], t: T)(implicit e: (Y, R) =:= YR): Unit = {
    blueprint.asInstanceOf[Coroutine._1[T, Y, R]].$push(co, t)
  }
}

/**
 * Coroutine factory, i.e. pre-invocation - can be invoked to create an instance of a coroutine.
 *
 * Pre-invocation type for coroutine which will be invoked with tuple of params
 *
 * @param blueprint Generalized coroutine factory
 * @tparam PS Tuple of the params (P1, P2, ...)
 * @tparam YR Equals pair (S,R) => (YieldType ResultType)
 */
class ~>[PS, YR] private[coroutines] (val blueprint: Coroutine.FactoryDefMarker[YR]) extends Coroutine.FactoryDefMarker[YR] {

  def apply[T1, T2, @specialized S, R](t1: T1, t2: T2)(
    implicit ps: PS =:= Tuple2[T1, T2], yr: (S, R) =:= YR
  ): R = {
    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
  }
  def $call[T1, T2, @specialized S, R](t1: T1, t2: T2)(
    implicit ps: PS =:= Tuple2[T1, T2], yr: (S, R) =:= YR
  ): Coroutine.Instance[S, R] = {
    blueprint.asInstanceOf[Coroutine._2[T1, T2, S, R]].$call(t1, t2)
  }
  def $push[T1, T2, @specialized S, R](co: Coroutine.Instance[S, R], t1: T1, t2: T2)(
    implicit ps: PS =:= Tuple2[T1, T2], yr: (S, R) =:= YR
  ): Unit = {
    blueprint.asInstanceOf[Coroutine._2[T1, T2, S, R]].$push(co, t1, t2)
  }

  def apply[T1, T2, T3, @specialized S, R](t1: T1, t2: T2, t3: T3)(
    implicit ps: PS =:= Tuple3[T1, T2, T3], yr: (S, R) =:= YR
  ): R = {
    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
  }
  def $call[T1, T2, T3, @specialized S, R](t1: T1, t2: T2, t3: T3)(
    implicit ps: PS =:= Tuple3[T1, T2, T3], yr: (S, R) =:= YR
  ): Coroutine.Instance[S, R] = {
    blueprint.asInstanceOf[Coroutine._3[T1, T2, T3, S, R]].$call(t1, t2, t3)
  }
  def $push[T1, T2, T3, @specialized S, R](co: Coroutine.Instance[S, R], t1: T1, t2: T2, t3: T3)(
    implicit ps: PS =:= Tuple3[T1, T2, T3], yr: (S, R) =:= YR
  ): Unit = {
    blueprint.asInstanceOf[Coroutine._3[T1, T2, T3, S, R]].$push(co, t1, t2, t3)
  }
}