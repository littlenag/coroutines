package org.examples



import org.coroutines._
import scala.util._



object MockSnapshot {
  abstract class TestSuite {
    class Cell {
      var value = false
    }

    val mock: ~~~>[Cell, Boolean] = coroutine { () =>
      val cell = new Cell
      yieldval(cell)
      cell.value
    }

    /** Returns true if either `c.isCompleted && c.hasResult` or if the rest
     *  of the coroutine is satisfied `test` regardless of the veracity of
     *  `c.value`.
     */
    def test[R](c: Cell <~> R): Boolean = {
      if (c.resume) {
        val cell = c.value
        cell.value = true
        val res0 = test(c.snapshot)
        cell.value = false
        val res1 = test(c)
        res0 && res1
      } else c.hasResult
    }
  }

  class MyTestSuite extends TestSuite {
    val myAlgorithm = coroutine { (x: Int) =>
      if (mock()) {
        assert(2 * x == x + x)
      } else {
        assert(x * x / x == x)
      }
    }

    assert(test(call(myAlgorithm(5))))

    // False because there is division by zero.
    assert(!test(call(myAlgorithm(0))))
  }

  def main(args: Array[String]) {
    new MyTestSuite
  }
}
