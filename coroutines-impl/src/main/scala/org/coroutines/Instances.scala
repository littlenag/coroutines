package org.coroutines

case class CoroutineStoppedException() extends Exception

/**
 * Pre-invocation type
 * @param blueprint
 * @tparam S
 * @tparam R
 */
class ~~~>[@specialized S, R] private[coroutines] (
                                                    val blueprint: Coroutine[S, R]
                                                  ) extends Coroutine.DefMarker[(S, R)] {
  def apply(): R =
    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
  def $call(): Coroutine.Instance[S, R] =
    blueprint.asInstanceOf[Coroutine._0[S, R]].$call()
  def $push(co: Coroutine.Instance[S, R]): Unit =
    blueprint.asInstanceOf[Coroutine._0[S, R]].$push(co)
}

/**
 * Pre-invocation type
 * @param blueprint
 * @tparam T
 * @tparam YR
 */
class ~~>[T, YR] private[coroutines] (
                                       val blueprint: Coroutine.DefMarker[YR]
                                     ) extends Coroutine.DefMarker[YR] {
  def apply[@specialized S, R](t: T)(implicit e: (S, R) =:= YR): R =
    sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
  def $call[@specialized S, R](t: T)(
    implicit e: (S, R) =:= YR
  ): Coroutine.Instance[S, R] = {
    blueprint.asInstanceOf[Coroutine._1[T, S, R]].$call(t)
  }
  def $push[@specialized S, R](co: Coroutine.Instance[S, R], t: T)(
    implicit e: (S, R) =:= YR
  ): Unit = {
    blueprint.asInstanceOf[Coroutine._1[T, S, R]].$push(co, t)
  }
}

/**
 * Pre-invocation type
 * @param blueprint
 * @tparam PS
 * @tparam YR
 */
class ~>[PS, YR] private[coroutines] (
                                       val blueprint: Coroutine.DefMarker[YR]
                                     ) extends Coroutine.DefMarker[YR] {
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
  def $push[T1, T2, T3, @specialized S, R](
                                            co: Coroutine.Instance[S, R], t1: T1, t2: T2, t3: T3
                                          )(
                                            implicit ps: PS =:= Tuple3[T1, T2, T3], yr: (S, R) =:= YR
                                          ): Unit = {
    blueprint.asInstanceOf[Coroutine._3[T1, T2, T3, S, R]].$push(co, t1, t2, t3)
  }
}