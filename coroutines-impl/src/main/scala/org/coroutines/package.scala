package org

import scala.language.experimental.macros

package object coroutines {

  val COROUTINE_DIRECT_APPLY_ERROR_MESSAGE =
    "Coroutines can only be invoked directly from within other coroutines. " +
    "Use `call(<coroutine>(<arg0>, ..., <argN>))` instead if you want to " +
    "start a new coroutine."

  def next[T](): T = {
    sys.error("Next allowed only inside coroutines.")
  }

  def yieldval[T](x: T): Unit = {
    sys.error("Yield allowed only inside coroutines.")
  }

  def yieldto[T](f: Coroutine.Instance[T, _]): Unit = {
    sys.error("Yield allowed only inside coroutines.")
  }

  def call[R](f: R): Any = macro Coroutine.call[R]

  // Y = Yield Type
  // R = Return Type
  def coroutine[Y, R](f: Any): Any = macro Coroutine.synthesize

  /* syntax sugar */

  type <~>[Y, R] = Coroutine.Instance[Y, R]

  implicit def coroutine0nothing[R](b: Coroutine._0[Nothing, R]) =
    new ~~~>[Nothing, R](b)

  implicit def coroutine0[@specialized S, R](b: Coroutine._0[S, R]) =
    new ~~~>[S, R](b)

  implicit def coroutine1nothing[T, R](b: Coroutine._1[T, Nothing, R]) =
    new ~~>[T, (Nothing, R)](b)

  implicit def coroutine1[T, @specialized S, R](b: Coroutine._1[T, S, R]) =
    new ~~>[T, (S, R)](b)

  implicit def coroutine2nothing[T1, T2, R](
    b: Coroutine._2[T1, T2, Nothing, R]
  ) = {
    new ~>[Tuple2[T1, T2], (Nothing, R)](b)
  }

  implicit def coroutine2[T1, T2, @specialized S, R](b: Coroutine._2[T1, T2, S, R]) =
    new ~>[Tuple2[T1, T2], (S, R)](b)

  implicit def coroutine3nothing[T1, T2, T3, R](
    b: Coroutine._3[T1, T2, T3, Nothing, R]
  ) = {
    new ~>[Tuple3[T1, T2, T3], (Nothing, R)](b)
  }

  implicit def coroutine3[T1, T2, T3, @specialized S, R](
    b: Coroutine._3[T1, T2, T3, S, R]
  ) = {
    new ~>[Tuple3[T1, T2, T3], (S, R)](b)
  }
}
