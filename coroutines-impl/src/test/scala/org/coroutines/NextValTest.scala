package org.coroutines

import org.scalatest._
import scala.coroutines.common.Util._


/**
 *
 */
class NextValTest extends funsuite.AnyFunSuite {

//  test("next-val statement") {
//    val echo : String ~~~> Unit = desugar {
//      coroutine { () =>
//        val e = next[String]()
//        val f = yieldval("")
//        val g = f
//        ()
//      }
//    }
//  }

  test("next in cell coroutine") {
    var cell: Option[String] = None

    val echo : String ~~~> Unit =
      coroutine { () =>
        val c = cell.get
        yieldval(c)
        ()
      }

    cell = Some("5")
    val e: String <~> Unit = call(echo())
    assert(e.resume)
    assert(e.value == "5")
    assert(!e.resume)
    assert(e.isCompleted)
  }

}
