package org.coroutines

import org.coroutines.common._
import scala.annotation.tailrec
import scala.util.{Try, Success, Failure}

/**
 * The object that generates instances of callable coroutines. Factory.
 *
 * @tparam A tuple of arguments, contravariant to avoid issues in the Instance class.
 *           seems to break things, TODO fix this
 * @tparam Y type of values yielded
 * @tparam R type of value returned
 */
trait Coroutine[@specialized Y, R] extends Coroutine.FactoryDefMarker[(Y, R)] {
  def $enter(c: Coroutine.Instance[Y, R]): Unit
  // sets resume state for suspend()
  def $suspend(c: Coroutine.Instance[Y, R]): Unit = {
    c.$hasYield = false
    c.$yield = null.asInstanceOf[Y]
    c.$expectsResumeValue = false
  }
  // sets resume state for next()
  def $awaitCellValue(c: Coroutine.Instance[Y, R]): Unit = {
    c.$hasYield = false
    c.$yield = null.asInstanceOf[Y]
    c.$expectsResumeValue = true
  }
  // sets resume state for yield()
  def $assignyield(c: Coroutine.Instance[Y, R], v: Y): Unit = {
    c.$hasYield = true
    c.$yield = v
    c.$expectsResumeValue = false
  }

  def $assignresult(c: Coroutine.Instance[Y, R], v: R): Unit = c.$result = v

  def $returnvalue$Z(c: Coroutine.Instance[Y, R], v: Boolean): Unit
  def $returnvalue$B(c: Coroutine.Instance[Y, R], v: Byte): Unit
  def $returnvalue$S(c: Coroutine.Instance[Y, R], v: Short): Unit
  def $returnvalue$C(c: Coroutine.Instance[Y, R], v: Char): Unit
  def $returnvalue$I(c: Coroutine.Instance[Y, R], v: Int): Unit
  def $returnvalue$F(c: Coroutine.Instance[Y, R], v: Float): Unit
  def $returnvalue$J(c: Coroutine.Instance[Y, R], v: Long): Unit
  def $returnvalue$D(c: Coroutine.Instance[Y, R], v: Double): Unit
  def $returnvalue$L(c: Coroutine.Instance[Y, R], v: Any): Unit

  // Pre-defined entry points for a coroutine instance
  // each entry point will be used for each yield separated control flow node
  def $ep0(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep1(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep2(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep3(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep4(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep5(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep6(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep7(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep8(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep9(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep10(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep11(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep12(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep13(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep14(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep15(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep16(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep17(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep18(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep19(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep20(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep21(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep22(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep23(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep24(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep25(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep26(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep27(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep28(c: Coroutine.Instance[Y, R]): Unit = {}
  def $ep29(c: Coroutine.Instance[Y, R]): Unit = {}
}

object Coroutine {
  private[coroutines] val INITIAL_COSTACK_SIZE = 4

  type SomeY

  type SomeR

  @tailrec
  private[coroutines] final def resume[Y, R](
    callsite: Instance[Y, R], actual: Instance[_, _]
  ): Boolean = {
    val cd = Stack.top(actual.$costack).asInstanceOf[Coroutine[SomeY, SomeR]]
    cd.$enter(actual.asInstanceOf[Instance[SomeY, SomeR]])
    if (actual.$target ne null) {
      val newactual = actual.$target
      actual.$target = null
      resume(callsite, newactual)
    } else if (actual.$exception ne null) {
      callsite.isLive
    } else {
      callsite.isLive
    }
  }

  /**
   * Instance of a coroutine that has been instantiated with its arguments.
   * @tparam Y
   * @tparam R
   */
  class Instance[@specialized Y, R] {
    var $costackptr = 0
    var $costack: Array[Coroutine[Y, R]] =
      new Array[Coroutine[Y, R]](INITIAL_COSTACK_SIZE)
    var $pcstackptr = 0
    var $pcstack = new Array[Short](INITIAL_COSTACK_SIZE)
    var $refstackptr = 0
    var $refstack: Array[AnyRef] = _
    var $valstackptr = 0
    var $valstack: Array[Int] = _
    var $target: Instance[Y, _] = null
    var $exception: Throwable = null
    var $hasYield: Boolean = false
    var $yield: Y = null.asInstanceOf[Y]
    var $result: R = null.asInstanceOf[R]
    var $expectsResumeValue: Boolean = false

    // Single cell so that we can inject data on resume
    var $cell: Option[AnyRef] = None

    /** Clones the coroutine that this instance is a part of.
     *
     *  @return A new coroutine instance with exactly the same execution state.
     */
    final def snapshot: Instance[Y, R] = {
      val frame = new Instance[Y, R]
      Stack.copy(this.$costack, frame.$costack)
      Stack.copy(this.$pcstack, frame.$pcstack)
      Stack.copy(this.$refstack, frame.$refstack)
      Stack.copy(this.$valstack, frame.$valstack)
      frame.$exception = this.$exception
      frame.$hasYield = this.$hasYield
      frame.$yield = this.$yield
      frame.$result = this.$result
      frame
    }

    /** Is the coroutine awaiting a value?
     *
     * If so, then resumeWithValue must be called to resume the coroutine. Otherwise
     * no value is expected and resume can be called as normal.
     *
     *  @return `true` if resumeWithValue must be used to resume, `false` otherwise.
     *  @throws CoroutineStoppedException If the coroutine is not live.
     */
    final def expectsResumeValue: Boolean = {
      if (isLive) {
        $expectsResumeValue
      } else throw CoroutineStoppedException()
    }

    /** Supplies a value for the coroutine, and advances to the next yield point.
     *
     *  @return `true` if resume can be called again, `false` otherwise.
     *  @throws CoroutineStoppedException If the coroutine is not live.
     */
    final def resumeWithValue[T](value:T): Boolean = {
      if (!$expectsResumeValue)
        throw CoroutineResumeException()
      if (isLive) {
        $hasYield = false
        $yield = null.asInstanceOf[Y]
        $cell = Some(value).asInstanceOf[Option[AnyRef]]
        Coroutine.resume[Y, R](this, this)
      } else throw CoroutineStoppedException()
    }

    /** Advances the coroutine to the next suspend (yield or suspend or next) point.
     *
     *  @return `true` if resume can be called again, `false` otherwise.
     *  @throws CoroutineStoppedException If the coroutine is not live.
     */
    final def resume: Boolean = {
      if ($expectsResumeValue)
        throw CoroutineResumeException()
      if (isLive) {
        $hasYield = false
        $yield = null.asInstanceOf[Y]
        Coroutine.resume[Y, R](this, this)
      } else throw CoroutineStoppedException()
    }

    /** Calls `resume` until either the coroutine yields a value or returns.
     *
     *  If `pull` returns `true`, then the coroutine has suspended by yielding
     *  a value and there are more elements to traverse.
     *
     *  Usage:
     *
     *  {{{
     *  while (c.pull) c.value
     *  }}}
     *
     *  @return `false` if the coroutine stopped, `true` otherwise.
     *  @throws CoroutineStoppedException If the coroutine is not live.
     */
    @tailrec
    final def pull: Boolean = {
      if (isLive) {
        if (!resume) false
        else if (hasValue) true
        else pull
      } else throw new CoroutineStoppedException
    }

    /** Returns the value yielded by the coroutine.
     *
     *  This method will thrown an exception if the value cannot be accessed.
     *
     *  @return The value yielded by the coroutine, if there is one.
     *  @throws RuntimeException If the coroutine doesn't have a value or if it
     *                           is not live.
     */
    final def value: Y = {
      if (!hasValue)
        sys.error("Coroutine has no value, because it did not yield.")
      if (!isLive)
        sys.error("Coroutine has no value, because it is completed.")
      $yield
    }

    /** Returns whether or not the coroutine yielded a value.
     *
     *  This value can be accessed via `getValue`.
     *
     *  @return `true` if the coroutine yielded a value, `false` otherwise.
     */
    final def hasValue: Boolean = $hasYield

    /** Returns an `Option` instance wrapping the current value of the coroutine, if
     *  any.
     *
     *  @return `Some(value)` if `hasValue`, `None` otherwise.
     */
    final def getValue: Option[Y] = if (hasValue) Some(value) else None

    /** Returns a `Try` instance wrapping this coroutine's value, if any.
     *
     *  The `Try` wraps either the current value of this coroutine or any exceptions
     *  thrown when trying to get the value.
     *
     *  @return `Success(value)` if `value` does not throw an exception, or
     *          a `Failure` instance if it does.
     */
    final def tryValue: Try[Y] =
      try { Success(value) } catch { case t: Throwable => Failure(t) }

    /** The value returned by the coroutine, if the coroutine is completed.
     *
     *  This method will throw an exception if the result cannot be accessed.
     *
     *  '''Note:''' the returned value is not the same as the value yielded
     *  by the coroutine. The coroutine may yield any number of values during its
     *  lifetime, but it returns only a single value after it terminates.
     *
     *  @return The return value of the coroutine, if the coroutine is completed.
     *  @throws RuntimeException If `!isCompleted`.
     *  @throws Exception        If `hasException`.
     */
    final def result: R = {
      if (!isCompleted)
        sys.error("Coroutine has no result, because it is not completed.")
      if ($exception != null) throw $exception
      $result
    }

    /** Returns whether or not the coroutine completed without an exception.
     *
     *  @return `true` if the coroutine completed without an exception, `false`
     *          otherwise.
     */
    final def hasResult: Boolean = isCompleted && $exception == null

    /** Returns an `Option` wrapping this coroutine's non-exception result, if any.
     *
     *  @return `Some(result)` if `hasResult`, `None` otherwise.
     */
    final def getResult: Option[R] = if (hasResult) Some(result) else None

    /** Returns a `Try` object wrapping either the successful result of this
     *  coroutine or the exception that the coroutine threw.
     *
     *  @return A `Failure` instance if the coroutine has an exception,
     *          `Try(result)` otherwise.
     */
    final def tryResult: Try[R] = {
      if ($exception != null) Failure($exception)
      else Try(result)
    }

    /** Returns whether or not the coroutine completed with an exception.
     *
     *  @return `true` iff `isCompleted` and the coroutine has a non-null
     *          exception, `false` otherwise.
     */
    final def hasException: Boolean = isCompleted && $exception != null

    /** Returns an `Option` object wrapping the exception thrown by this coroutine.
     *
     *  @return If `hasException`, a `Some` instance wrapping the exception thrown by
     *          this coroutine. Otherwise, `None`.
     */
    final def getException: Option[Throwable] = {
      if (hasException) Some($exception)
      else None
    }

    /** Returns `false` iff the coroutine instance completed execution.
     *
     *  This is true if there are either more yield statements or if the
     *  coroutine has not yet returned its result.
     *
     *  @return `true` if `resume` can be called without an exception being
     *          thrown, `false` otherwise.
     */
    final def isLive: Boolean = $costackptr > 0

    /** Returns `true` iff the coroutine instance completed execution.
     *
     *  See the documentation for `isLive`.
     *
     *  @return `!isLive`.
     */
    final def isCompleted: Boolean = !isLive

    /** Returns a string representation of the coroutine's state.
     *
     *  Contains less information than `debugString`.
     *
     *  @return A string describing the coroutine state.
     */
    override def toString = s"Coroutine.Instance<depth: ${$costackptr}, live: $isLive>"

    /** Returns a string that describes the internal state of the coroutine.
     *
     *  Contains more information than `toString`.
     *
     *  @return A string containing information about the internal state of the
     *          coroutine.
     */
    final def debugString: String = {
      def toStackLength[T](stack: Array[T]) =
        if (stack != null) s"${stack.length}" else "<uninitialized>"
      def toStackString[T](stack: Array[T]) =
        if (stack != null) stack.mkString("[", ", ", "]") else "<uninitialized>"
      s"Coroutine.Instance <\n" +
      s"  costackptr:  ${$costackptr}\n" +
      s"  costack sz:  ${toStackLength($costack)}\n" +
      s"  pcstackptr:  ${$pcstackptr}\n" +
      s"  pcstack:     ${toStackString($pcstack)}\n" +
      s"  exception:   ${$exception}\n" +
      s"  yield:       ${$yield}\n" +
      s"  result:      ${$result}\n" +
      s"  refstackptr: ${$refstackptr}\n" +
      s"  refstack:    ${toStackString($refstack)}\n" +
      s"  valstackptr: ${$valstackptr}\n" +
      s"  valstack:    ${toStackString($valstack)}\n" +
      s">"
    }
  }

  trait FactoryDefMarker[YR]

  // Requires 0 arguments to create a coroutine instance
  trait _0[@specialized Y, R] extends Coroutine[/*Unit,*/ Y, R] {
    def apply(): R //= sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
    def inst(): Instance[Y, R] = $call()
    def $call(): Instance[Y, R]
    def $push(c: Instance[Y, R]): Unit
    override def toString = s"Coroutine._0@${System.identityHashCode(this)}"
  }

  // Requires 1 argument to create a coroutine instance
  trait _1[A0, @specialized Y, R] extends Coroutine[/*Tuple1[A0],*/ Y, R] {
    def apply(a0: A0): R //= sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
    def inst(a0: A0): Instance[Y, R] = $call(a0)
    def $call(a0: A0): Instance[Y, R]
    def $push(c: Instance[Y, R], a0: A0): Unit
    override def toString = s"Coroutine._1@${System.identityHashCode(this)}"
  }

  // Requires 2 arguments to create a coroutine instance
  trait _2[A0, A1, @specialized Y, R] extends Coroutine[/*(A0, A1),*/ Y, R] {
    def apply(a0: A0, a1: A1): R //= sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
    def inst(a0: A0, a1: A1): Instance[Y, R] = $call(a0, a1)
    def $call(a0: A0, a1: A1): Instance[Y, R]
    def $push(c: Instance[Y, R], a0: A0, a1: A1): Unit
    override def toString = s"Coroutine._2@${System.identityHashCode(this)}"
  }

  // Requires 3 arguments to create a coroutine instance
  trait _3[A0, A1, A2, @specialized Y, R] extends Coroutine[/*(A0, A1, A2),*/ Y, R] {
    def apply(a0: A0, a1: A1, a2: A2): R //= sys.error(COROUTINE_DIRECT_APPLY_ERROR_MESSAGE)
    def inst(a0: A0, a1: A1, a2: A2): Instance[Y, R] = $call(a0, a1, a2)
    def $call(a0: A0, a1: A1, a2: A2): Instance[Y, R]
    def $push(c: Instance[Y, R], a0: A0, a1: A1, a2: A2): Unit
    override def toString = s"Coroutine._3@${System.identityHashCode(this)}"
  }
}
