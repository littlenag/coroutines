package org.coroutines

import scala.coroutines.common.Util._
import org.scalatest._

class Playground extends funsuite.AnyFunSuite {
//  test("desugar coroutine") {
//    desugar {
//      coroutine { (x: AnyRef) =>
//        x match {
//          case s: String => s.length
//          case xs: List[_] => xs.size
//        }
//      }
//    }
//  }

  test("desugar coroutine call") {
    desugar {
      val rube: Int -> (Int @@ Int) = coroutine { (x: Int) =>
        val v = 0xDEADBEEF
        yieldval(v)
        x
      }

      val a = rube.inst(2)

      assert(a.resume)
      assert(a.value === 0xDEADBEEF)
      assert(!a.expectsResumeValue)
      assert(!a.resume)
      assert(a.isCompleted)
    }
  }
}