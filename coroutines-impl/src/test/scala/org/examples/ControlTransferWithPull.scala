package org.examples

import org.coroutines._
import scala.collection._
import scala.util.Random

object ControlTransferWithPull {
  var error: String = ""
  val check = cr.yielding[Boolean].of { () =>
    yieldval(true)
    error = "Total failure."
    yieldval(false)
  }
  val checker = check.inst()

  val random = cr.yielding[Double].of { () =>
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
    while (r1.pull) values += r1.value
    assert(values.length == 2)
    assert(error == "Total failure.")
  }
}
