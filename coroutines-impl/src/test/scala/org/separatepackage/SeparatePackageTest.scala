package org.separatepackage

import org.coroutines._
import org.scalatest._

class SeparatePackageTest extends funsuite.AnyFunSuite {
  test("should declare and run a coroutine") {
    val rube = coroutine[Int].of { (x: Int) =>
      yieldval(x * 2)
      if (x > 0) yieldval(x)
      else yieldval(-x)
      x + 1
    }

    val c0 = rube.inst(2)
    assert(c0.resume)
    assert(c0.value == 4)
    assert(c0.resume)
    assert(c0.value == 2)
    assert(!c0.resume)
    assert(c0.result == 3)
    assert(c0.isCompleted)

    val c1 = rube.inst(-2)
    assert(c1.resume)
    assert(c1.value == -4)
    assert(c1.resume)
    assert(c1.value == 2)
    assert(!c1.resume)
    assert(c1.result == -1)
    assert(c1.isCompleted)
  }

  test("Another coroutine must be invoked without syntax sugar") {
    val inc = coroutine[Nothing].of { (x: Int) => x + 1 }
    val rube = coroutine[Nothing].of { () =>
      inc(3)
    }

    val c = rube.inst()
    assert(!c.resume)
    assert(c.result == 4)
    assert(c.isCompleted)
  }
}
