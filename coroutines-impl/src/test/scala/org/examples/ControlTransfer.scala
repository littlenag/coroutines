package org.examples



import org.coroutines._
import scala.collection._
import scala.util.Random



object ControlTransfer {
  var error: String = ""
  val check = coroutine[Boolean].of { () =>
    yieldval(true)
    error = "Total failure."
    yieldval(false)
  }
  val checker = check.inst()

  /** From within `random`, the call `yieldto(checker)` will evaluate `checker`
   *  until `checker` releases control. Then, `random` will release control.
   *  After this happens, `random.hasValue` will be false; yielded values won't
   *  propagate upwards because of calls to `yieldto`.
   */
  val random = coroutine[Double].of { () =>
    yieldval(Random.nextDouble())
    yieldto(checker)
    yieldval(Random.nextDouble())
  }

  def main(args: Array[String]) {
    val r0 = random.inst()
    assert(r0.resume)
    assert(r0.hasValue)
    assert(r0.resume)
    assert(!r0.hasValue)
    assert(r0.resume)
    assert(r0.hasValue)
    assert(!r0.resume)
    assert(!r0.hasValue)

    val r1 = random.inst()
    val values = mutable.Buffer[Double]()
    while (r1.resume) if (r1.hasValue) values += r1.value
    assert(values.length == 2)
    assert(error == "Total failure.")
  }
}
