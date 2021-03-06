package org.coroutines

import org.scalatest._

class SnapshotTest extends funsuite.AnyFunSuite {
  test("coroutine instance should be cloned and resumed as needed") {
    val countdown = coroutine[Int].of { (n: Int) =>
      var i = n
      while (i >= 0) {
        yieldval(i)
        i -= 1
      }
    }

    val c = countdown.inst(10)
    for (i <- 0 until 5) {
      assert(c.resume)
      assert(c.value == (10 - i))
    }
    val c2 = c.snapshot
    for (i <- 5 to 10) {
      assert(c2.resume)
      assert(c2.value == (10 - i))
    }
    for (i <- 5 to 10) {
      assert(c.resume)
      assert(c.value == (10 - i))
    }
  }
}
