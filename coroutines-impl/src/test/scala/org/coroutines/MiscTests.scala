package org.coroutines

import org.scalatest._

class MiscTests extends funsuite.AnyFunSuite {

  test("recursive coroutine using lazy val") {
    // TODO ensure works 2.11
    // TODO doesn't work with sugar: lazy val fibsugar: Int ~> (Unit, Int) = ...
    lazy val fibsugar: Coroutine._1[Int, Unit, Int] = coroutine[Unit].of { (n: Int) =>
      if (n == 0) 0
      else if (n == 1) 1
      else fibsugar(n - 1) + fibsugar(n - 2)
    }

    val fib = fibsugar.inst(26)
    assert(!fib.resume)
    assert(fib.result === 121393)
  }

  test("suspended function") {
    var x = 0

    val increment = task { (n: Int) =>
      var i = n
      while (i > 0) {
        x += 1
        i -= 1
        suspend()
      }
    }

    val fib = increment.inst(3)
    assert(fib.resume)
    assert(x === 1)
    assert(fib.resume)
    assert(x === 2)
    assert(fib.resume)
    assert(x === 3)
    assert(!fib.resume)
  }

}
