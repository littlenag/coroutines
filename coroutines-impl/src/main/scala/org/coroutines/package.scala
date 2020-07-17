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

  // Internal placeholder method
  private[coroutines] def suspend(): Unit = {
    sys.error("suspend allowed only inside coroutines.")
  }

  // Internal placeholder method
  private[coroutines] def pullcell[T](): T = {
    sys.error("pullcell allowed only inside coroutines.")
  }

  def yieldval[T](x: T): Unit = {
    sys.error("Yield allowed only inside coroutines.")
  }

  def yieldto[T](f: Coroutine.Instance[T, _]): Unit = {
    sys.error("Yield allowed only inside coroutines.")
  }

  // Replaced with cr.inst(..args)
  def call[R](f: R): Any = macro Coroutine.call[R]

  //def call[Y, R](f: Coroutine._0[Y, R]): Coroutine.Instance[Y, R] = macro Coroutine.call[Coroutine._0[Y, R]]
  //def call[A0, Y, R](f: Coroutine._1[A0, Y, R]): Coroutine.Instance[Y, R] = macro Coroutine.call[Coroutine._1[A0, Y, R]]
  //def call[A0, A1, Y, R](f: Coroutine._2[A0, A1, Y, R]): Coroutine.Instance[Y, R] = macro Coroutine.call[Coroutine._2[A0, A1, Y, R]]
  //def call[A0, A1, A2, Y, R](f: Coroutine._3[A0, A1, A2, Y, R]): Coroutine.Instance[Y, R] = macro Coroutine.call[Coroutine._3[A0, A1, A2, Y, R]]

  // Y = Yield Type
  // R = Return Type
  //def coroutine[Y, R](f: Any): Any = macro Coroutine.synthesize

  def coroutine[Y, R](f: () => R): Coroutine._0[_, R] = macro Coroutine.synthesize[R]
  def coroutine[T1, Y, R](f: T1 => R): Coroutine._1[T1, _, R] = macro Coroutine.synthesize[R]
  def coroutine[T1, T2, Y, R](f: (T1, T2) => R): Coroutine._2[T1, T2, _, R] = macro Coroutine.synthesize[R]
  def coroutine[T1, T2, T3, Y, R](f: (T1, T2, T3) => R): Coroutine._3[T1, T2, T3, _, R] = macro Coroutine.synthesize[R]

  //def coroutine[Y, R, F](f: F): Coroutine.FactoryDefMarker[(Y, R)] = macro Coroutine.synthesize

  /* syntax sugar */

  type <~>[Y, R] = Coroutine.Instance[Y, R]

  implicit def yrcoroutine0[Y, R](b: Coroutine._0[Y, R]): Unit -> (Y @@ R) = {

    type YY = Y
    type RR = R

    val adapterYR = new ArityAdapter[Unit, Y, R] {
      //def apply(): R = b.apply()
      def inst(a:Unit): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.inst()
      def $call(a:Unit): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.$call()
      def $push(c: _root_.org.coroutines.Coroutine.Instance[Y, R])(a:Unit): Unit = b.$push(c)
    }

    new ->[Unit, @@[Y,R]](new @@[Y,R](b)) { self =>
      type Y = YY
      type R = RR
      val adapter = adapterYR
      val yr = new @@[self.Y,self.R](b)
    }
  }

  implicit def yrcoroutine1[A0, Y, R](b: Coroutine._1[A0, Y, R]): A0 -> (Y @@ R) = {

    type YY = Y
    type RR = R

    val adapterYR = new ArityAdapter[A0, Y, R] {
      //def apply(): R = b.apply()
      def inst(a0:A0): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.inst(a0)
      def $call(a0:A0): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.$call(a0)
      def $push(c: _root_.org.coroutines.Coroutine.Instance[Y, R])(a0:A0): Unit = b.$push(c,a0)
    }

    new ->[A0, @@[Y,R]](new @@[Y,R](b)) { self =>
      type Y = YY
      type R = RR
      val adapter = adapterYR
      val yr = new @@[self.Y,self.R](b)
    }
  }

  implicit def yrcoroutine2[A0, A1, Y, R](b: Coroutine._2[A0, A1, Y, R]): (A0, A1) -> (Y @@ R) = {

    type YY = Y
    type RR = R

    val adapterYR = new ArityAdapter[(A0, A1), Y, R] {
      //def apply(): R = b.apply()
      def inst(a:(A0,A1)): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.inst(a._1, a._2)
      def $call(a:(A0,A1)): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.$call(a._1,a._2)
      def $push(c: _root_.org.coroutines.Coroutine.Instance[Y, R])(a:(A0,A1)): Unit = b.$push(c,a._1,a._2)
    }

    new ->[(A0,A1), @@[Y,R]](new @@[Y,R](b)) { self =>
      type Y = YY
      type R = RR
      val adapter = adapterYR
      val yr = new @@[self.Y,self.R](b)
    }
  }


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
