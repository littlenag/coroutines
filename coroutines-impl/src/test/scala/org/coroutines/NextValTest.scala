package org.coroutines

import org.scalatest._
import scala.coroutines.common.Util._
/**
 *
 */
class NextValTest extends funsuite.AnyFunSuite {
  test("next-val statement") {
    desugar {
      coroutine { () =>
        var a = 1
        val e = next[String]()
        val b = 1
        val y = yieldval("11")
        yieldval("22")
        val c = 1
        yieldval("33")

        ()
      }
    }


//    val echo /*: String ~~~> Unit */ = coroutine { () =>
//      val asdf = yieldval("123")
//
//      ()
//    }
//
//    val c: String <~> Unit = call(echo())
//    assert(!c.resume)
//    assert(!c.resumeWithValue("5"))
//    assert(c.value == "5")
//    assert(c.isCompleted)
  }
}
