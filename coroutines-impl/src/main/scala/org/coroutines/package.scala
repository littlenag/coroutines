package org

import scala.annotation.implicitNotFound
import scala.language.experimental.macros

package object coroutines {

  val COROUTINE_DIRECT_APPLY_ERROR_MESSAGE =
    "Coroutines can only be invoked directly from within other coroutines. " +
    "Use `call(<coroutine>(<arg0>, ..., <argN>))` instead if you want to " +
    "start a new coroutine."

  val COROUTINE_PUSH_ARITY_ERROR_MESSAGE =
    "Calling $push is done internally. The correct arity function will be overriden and " +
    "and an implementation provided. Do not call directly."

  // Suspends the coroutine, should only be called directly in suspended functions
  def suspend(): Unit = {
    sys.error("suspend allowed only inside coroutines.")
  }

  // FIXME T: NotNothing causes lots of test failures
  // Suspends the coroutine, yielding the value provided, should only be called directly in suspended functions
  def yieldval[T](x: T): Unit = {
    sys.error("YieldVal allowed only inside coroutines.")
  }

  // Suspends the coroutine, awaiting a resume value, should only be called directly in suspended functions
  def next[T](): T = {
    sys.error("Next allowed only inside coroutines.")
  }

  // Internal placeholder method
  private[coroutines] def awaitCellValue(): Unit = {
    sys.error("pullcell allowed only inside coroutines.")
  }

  // Internal placeholder method
  private[coroutines] def pullcell[T](): T = {
    sys.error("pullcell allowed only inside coroutines.")
  }

  /** From within a coroutine, the call `yieldto(f)` will evaluate the coroutine instance `f`
   *  until `f` releases control. Then, the calling coroutine will resume executing.
   *  After this happens, `f.hasValue` will be false; yielded values won't
   *  propagate upwards because of calls to `yieldto`.
   */
  def yieldto[T](f: Coroutine.Instance[T, _]): Unit = {
    sys.error("YieldTo allowed only inside coroutines.")
  }

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

  // functions that may suspend an indefinite number of times, yielding no value (though .value will return null)
  def task[R](f: () => R): Coroutine._0[Nothing, R] = macro CoroutineMacros.synthesize[R]
  def task[T1, R](f: T1 => R): Coroutine._1[T1, Nothing, R] = macro CoroutineMacros.synthesize[R]
  def task[T1, T2, R](f: (T1, T2) => R): Coroutine._2[T1, T2, Nothing, R] = macro CoroutineMacros.synthesize[R]
  def task[T1, T2, T3, R](f: (T1, T2, T3) => R): Coroutine._3[T1, T2, T3, Nothing, R] = macro CoroutineMacros.synthesize[R]

  // Y A R
  // auto convert FunctionN to tupled version A => R
  //def coroutineYR[Y, R](f: CR0[Y,R]): Coroutine._0[Y, R] = macro Coroutine.synthesize[R]

  // Replaced with cr.inst(..args)
  //def call[R](f: R): Any = macro CoroutineMacros.call[R]

  // these allow whitebxing to infer the yield type
  def coroutine[Y, R](f: () => R): Coroutine._0[_, R] = macro CoroutineLegacyMacros.synthesize[R]
  def coroutine[T1, Y, R](f: T1 => R): Coroutine._1[T1, _, R] = macro CoroutineLegacyMacros.synthesize[R]
  def coroutine[T1, T2, Y, R](f: (T1, T2) => R): Coroutine._2[T1, T2, _, R] = macro CoroutineLegacyMacros.synthesize[R]
  def coroutine[T1, T2, T3, Y, R](f: (T1, T2, T3) => R): Coroutine._3[T1, T2, T3, _, R] = macro CoroutineLegacyMacros.synthesize[R]

  /* syntax sugar */

  type <~>[Y, R] = Coroutine.Instance[Y, R]
  type @@[Y,R] = Tuple2[Y,R]

  implicit def coroutine0[Y, R](b: Coroutine._0[Y, R]): Unit ~> (Y @@ R) = {
    val adapterYR = new ArityAdapter[Unit, @@[Y,R]] {
      override def inst[YI, RI](a: Unit)(implicit yr: @@[YI,RI] =:= @@[Y,R]): _root_.org.coroutines.Coroutine.Instance[YI, RI] =
        b.inst().asInstanceOf[Coroutine.Instance[YI,RI]]
      override def $push[YI, RI](c: _root_.org.coroutines.Coroutine.Instance[YI, RI])(implicit yr: @@[YI,RI] =:= @@[Y,R]): Unit =
        b.$push(c.asInstanceOf[Coroutine.Instance[Y,R]])
    }

    ~>[Unit, @@[Y,R]](adapterYR)
  }

  implicit def coroutine1[A0, Y, R](b: Coroutine._1[A0, Y, R]): A0 ~> (Y @@ R) = {

    val adapterYR = new ArityAdapter[A0, @@[Y,R]] {
      override def inst[YI, RI](a: A0)(implicit yr: YI @@ RI =:= @@[Y,R]): _root_.org.coroutines.Coroutine.Instance[YI, RI] =
        b.inst(a).asInstanceOf[Coroutine.Instance[YI,RI]]
      override def $push[T1, YI, RI](c: _root_.org.coroutines.Coroutine.Instance[YI, RI], t1: T1)(implicit ps: A0 =:= T1, yr: @@[YI,RI] =:= @@[Y,R]): Unit =
        b.$push(c.asInstanceOf[Coroutine.Instance[Y,R]],t1.asInstanceOf[A0])
    }

    ~>[A0, @@[Y,R]](adapterYR)
  }

  implicit def coroutine2[A0, A1, Y, R](b: Coroutine._2[A0, A1, Y, R]): (A0, A1) ~> (Y @@ R) = {
    val adapterYR = new ArityAdapter[(A0, A1), @@[Y,R]] {
      override def inst[YI, RI](a: (A0,A1))(implicit yr: YI @@ RI =:= @@[Y,R]): _root_.org.coroutines.Coroutine.Instance[YI, RI] =
        b.inst(a._1,a._2).asInstanceOf[Coroutine.Instance[YI,RI]]
      override def $push[T1, T2, YI, RI](c: _root_.org.coroutines.Coroutine.Instance[YI, RI], t1: T1, t2: T2)(implicit ps: (A0,A1) =:= Tuple2[T1,T2], yr: @@[YI,RI] =:= @@[Y,R]): Unit =
        b.$push(c.asInstanceOf[Coroutine.Instance[Y,R]],t1.asInstanceOf[A0],t2.asInstanceOf[A1])
    }

    ~>[(A0,A1), @@[Y,R]](adapterYR)
  }

  implicit def coroutine3[A0, A1, A2, Y, R](b: Coroutine._3[A0, A1, A2, Y, R]): (A0, A1, A2) ~> (Y @@ R) = {
    val adapterYR = new ArityAdapter[(A0, A1, A2), @@[Y,R]] {
      override def inst[YI, RI](a: (A0,A1,A2))(implicit yr: YI @@ RI =:= @@[Y,R]): _root_.org.coroutines.Coroutine.Instance[YI, RI] =
        b.inst(a._1,a._2,a._3).asInstanceOf[Coroutine.Instance[YI,RI]]
      override def $push[T1, T2, T3, YI, RI](c: _root_.org.coroutines.Coroutine.Instance[YI, RI], t1: T1, t2: T2, t3:T3)(implicit ps: (A0,A1,A2) =:= Tuple3[T1,T2,T3], yr: @@[YI,RI] =:= @@[Y,R]): Unit =
        b.$push(c.asInstanceOf[Coroutine.Instance[Y,R]],t1.asInstanceOf[A0],t2.asInstanceOf[A1],t3.asInstanceOf[A2])
    }

    ~>[(A0,A1,A2), @@[Y,R]](adapterYR)
  }
}