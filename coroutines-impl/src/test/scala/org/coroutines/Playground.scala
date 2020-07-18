package org.coroutines

import scala.coroutines.common.Util._
import org.scalatest._

class Playground extends funsuite.AnyFunSuite {

  test("friendly def syntax") {
    desugar {
      val rube: Int -> (Int @@ Int) = cr.yielding[Int].of { (x: Int) =>
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

  test("desugar coroutine call") {
    desugar {
      val rube: Int -> (Int @@ Int) = cr.yielding[Int].of { (x: Int) =>
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