package org

import scala.annotation.implicitNotFound
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

  // FIXME T: NotNothing causes lots of test failures
  def yieldval[T](x: T): Unit = {
    sys.error("YieldVal allowed only inside coroutines.")
  }

  /** From within a coroutine, the call `yieldto(f)` will evaluate the coroutine instance `f`
   *  until `f` releases control. Then, the calling coroutine will resume executing.
   *  After this happens, `f.hasValue` will be false; yielded values won't
   *  propagate upwards because of calls to `yieldto`.
   */
  def yieldto[T](f: Coroutine.Instance[T, _]): Unit = {
    sys.error("YieldTo allowed only inside coroutines.")
  }

  //def call[Y, R](f: Coroutine._0[Y, R]): Coroutine.Instance[Y, R] = macro Coroutine.call[Coroutine._0[Y, R]]
  //def call[A0, Y, R](f: Coroutine._1[A0, Y, R]): Coroutine.Instance[Y, R] = macro Coroutine.call[Coroutine._1[A0, Y, R]]
  //def call[A0, A1, Y, R](f: Coroutine._2[A0, A1, Y, R]): Coroutine.Instance[Y, R] = macro Coroutine.call[Coroutine._2[A0, A1, Y, R]]
  //def call[A0, A1, A2, Y, R](f: Coroutine._3[A0, A1, A2, Y, R]): Coroutine.Instance[Y, R] = macro Coroutine.call[Coroutine._3[A0, A1, A2, Y, R]]

  // Y = Yield Type
  // R = Return Type
  //def coroutine[Y, R](f: Any): Any = macro Coroutine.synthesize

  //Function

  @implicitNotFound("Nothing was inferred")
  sealed trait NotNothing[-T]

  object NotNothing {
    implicit object notNothing extends NotNothing[Any]
    //We do not want Nothing to be inferred, so make an ambigous implicit
    implicit object `\n The error is because the type parameter was resolved to Nothing` extends NotNothing[Nothing]
    //For classtags, RuntimeClass can also be inferred, so making that ambigous too
    //implicit object `\n The error is because the type parameter was resolved to RuntimeClass` extends NotNothing[RuntimeClass]
  }

  //trait Yields[Y]
  //implicit def yields[T: NotNothing] = new Yields[T] {}

  trait CoroutineBuilder[Y] {
    def of[R](f: () => R): Coroutine._0[Y, R] = macro CoroutineMacros.synthesize[R]
    def of[T1, R](f: T1 => R): Coroutine._1[T1, Y, R] = macro CoroutineMacros.synthesize[R]
    def of[T1, T2, R](f: (T1, T2) => R): Coroutine._2[T1, T2, Y, R] = macro CoroutineMacros.synthesize[R]
    def of[T1, T2, T3, R](f: (T1, T2, T3) => R): Coroutine._3[T1, T2, T3, Y, R] = macro CoroutineMacros.synthesize[R]
  }

  def coroutine[Y]: CoroutineBuilder[Y] = new CoroutineBuilder[Y] {}

  // Coroutine._0[Nothing, R] => never yields, is this a case we care about? would need to statically evaluate

  // Coroutines that yield unit values only, ie nothing useful
  // Coroutine._0[Unit, R] => may yield, is this a case we care about?

  // of suspendable
  def ofS[R](f: () => R): Coroutine._0[Unit, R] = macro CoroutineMacros.synthesize[R]
  def ofS[T1, R](f: T1 => R): Coroutine._1[T1, Unit, R] = macro CoroutineMacros.synthesize[R]
  def ofS[T1, T2, R](f: (T1, T2) => R): Coroutine._2[T1, T2, Unit, R] = macro CoroutineMacros.synthesize[R]
  def ofS[T1, T2, T3, R](f: (T1, T2, T3) => R): Coroutine._3[T1, T2, T3, Unit, R] = macro CoroutineMacros.synthesize[R]

  // of function
  def ofF[R](f: () => R): Coroutine._0[Nothing, R] = macro CoroutineMacros.synthesize[R]
  def ofF[T1, R](f: T1 => R): Coroutine._1[T1, Nothing, R] = macro CoroutineMacros.synthesize[R]
  def ofF[T1, T2, R](f: (T1, T2) => R): Coroutine._2[T1, T2, Nothing, R] = macro CoroutineMacros.synthesize[R]
  def ofF[T1, T2, T3, R](f: (T1, T2, T3) => R): Coroutine._3[T1, T2, T3, Nothing, R] = macro CoroutineMacros.synthesize[R]

  // Y A R
  // auto convert FunctionN to tupled version A => R
  //def coroutineYR[Y, R](f: CR0[Y,R]): Coroutine._0[Y, R] = macro Coroutine.synthesize[R]

  // Replaced with cr.inst(..args)
  //def call[R](f: R): Any = macro CoroutineMacros.call[R]

  // these are the optimized versions
  def coroutine[Y, R](f: () => R): Coroutine._0[_, R] = macro CoroutineLegacyMacros.synthesize[R]
  def coroutine[T1, Y, R](f: T1 => R): Coroutine._1[T1, _, R] = macro CoroutineLegacyMacros.synthesize[R]
  def coroutine[T1, T2, Y, R](f: (T1, T2) => R): Coroutine._2[T1, T2, _, R] = macro CoroutineLegacyMacros.synthesize[R]
  def coroutine[T1, T2, T3, Y, R](f: (T1, T2, T3) => R): Coroutine._3[T1, T2, T3, _, R] = macro CoroutineLegacyMacros.synthesize[R]

  /* syntax sugar */

  type <~>[Y, R] = Coroutine.Instance[Y, R]
  type @@[Y,R] = Tuple2[Y,R]


  implicit def yrcoroutine0[Y, R](b: Coroutine._0[Y, R]): Unit -> (Y @@ R) = {
    val adapterYR = new ArityAdapter[Unit, @@[Y,R]] {
      //def apply(): R = b.apply()
      override def inst[YI, RI](a: Unit)(implicit yr: @@[YI,RI] =:= @@[Y,R]): _root_.org.coroutines.Coroutine.Instance[YI, RI] =
        b.inst().asInstanceOf[Coroutine.Instance[YI,RI]]
      //def $call(a:Unit): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.$call()
      //def $push(c: _root_.org.coroutines.Coroutine.Instance[Y, R])(a:Unit): Unit = b.$push(c)
    }

    ->[Unit, @@[Y,R]](adapterYR)
  }

  implicit def yrcoroutine1[A0, Y, R](b: Coroutine._1[A0, Y, R]): A0 -> (Y @@ R) = {

    val adapterYR = new ArityAdapter[A0, @@[Y,R]] {
      //def apply(): R = b.apply()
      override def inst[YI, RI](a: A0)(implicit yr: YI @@ RI =:= @@[Y,R]): _root_.org.coroutines.Coroutine.Instance[YI, RI] =
        b.inst(a).asInstanceOf[Coroutine.Instance[YI,RI]]

      //def $call(a0:A0): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.$call(a0)
      //def $push(c: _root_.org.coroutines.Coroutine.Instance[Y, R])(a0:A0): Unit = b.$push(c,a0)
    }

    ->[A0, @@[Y,R]](adapterYR)
  }

  implicit def yrcoroutine2[A0, A1, Y, R](b: Coroutine._2[A0, A1, Y, R]): (A0, A1) -> (Y @@ R) = {

    type YR = @@[Y,R]

    val adapterYR = new ArityAdapter[(A0, A1), @@[Y,R]] {
      //def apply(): R = b.apply()
      override def inst[YI, RI](a: (A0,A1))(implicit yr: YI @@ RI =:= YR): _root_.org.coroutines.Coroutine.Instance[YI, RI] =
        b.inst(a._1,a._2).asInstanceOf[Coroutine.Instance[YI,RI]]

      //def $call(a:(A0,A1)): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.$call(a._1,a._2)
      //def $push(c: _root_.org.coroutines.Coroutine.Instance[Y, R])(a:(A0,A1)): Unit = b.$push(c,a._1,a._2)
    }

    ->[(A0,A1), @@[Y,R]](adapterYR)
  }

  implicit def yrcoroutine3[A0, A1, A2, Y, R](b: Coroutine._3[A0, A1, A2, Y, R]): (A0, A1, A2) -> (Y @@ R) = {

    type YR = @@[Y,R]

    val adapterYR = new ArityAdapter[(A0, A1, A2), @@[Y,R]] {
      //def apply(): R = b.apply()
      override def inst[YI, RI](a: (A0,A1,A2))(implicit yr: YI @@ RI =:= YR): _root_.org.coroutines.Coroutine.Instance[YI, RI] =
        b.inst(a._1,a._2,a._3).asInstanceOf[Coroutine.Instance[YI,RI]]
      //def $call(a:(A0,A1,A2)): _root_.org.coroutines.Coroutine.Instance[Y, R] = b.$call(a._1,a._2,a._3)
      //def $push(c: _root_.org.coroutines.Coroutine.Instance[Y, R])(a:(A0,A1,A2)): Unit = b.$push(c,a._1,a._2,a._3)
    }

    ->[(A0,A1,A2), @@[Y,R]](adapterYR)
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
