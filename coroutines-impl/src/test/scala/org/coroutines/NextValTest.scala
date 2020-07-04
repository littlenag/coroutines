package org.coroutines

import org.scalatest._
import scala.coroutines.common.Util._
/**
 *
 */
class NextValTest extends funsuite.AnyFunSuite {

  def ff() = 2

  test("next-val statement") {
    val echo : String ~~~> Unit = desugar {
      coroutine { () =>
        val e = next[String]()
        val f = yieldval("")
        val g = f
        ()
      }
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
