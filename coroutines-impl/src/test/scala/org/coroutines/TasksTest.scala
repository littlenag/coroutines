package org.coroutines

import org.scalatest._

class TasksTest extends funsuite.AnyFunSuite {

  test("task should suspend and resume as normal") {
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

  test("task instance work as normal") {

    val countdown = task { (n: Int) =>
      var i = n
      while (i >= 0) {
        suspend()
        i -= 1
      }
    }


  }

}
