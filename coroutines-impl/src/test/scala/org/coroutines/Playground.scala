package org.coroutines

import scala.coroutines.common.Util._
import org.scalatest._

class Playground extends funsuite.AnyFunSuite {

  // FIXME broken $push for ~> type
  test("nothing yield") {
    //desugar {
      //var fibsugar: Int ~> (Unit, Int) = null
      //var fibsugar: Coroutine._1[Int, Unit, Int] = null
      lazy val fibsugar: Coroutine._1[Int, Unit, Int] = coroutine[Unit].of { (n: Int) =>
        if (n == 0) 0
        else if (n == 1) 1
        else fibsugar(n - 1) + fibsugar(n - 2)
      }

      val fib = fibsugar.inst(26)
      assert(!fib.resume)
      assert(fib.result === 121393)
    //}
  }

//  test("friendly def syntax") {
//    desugar {
//      val rube: Int -> (Int @@ Int) = coroutine[Int].of { (x: Int) =>
//        val v = 0xDEADBEEF
//        yieldval(v)
//        x
//      }
//
//      val a = rube.inst(2)
//
//      assert(a.resume)
//      assert(a.value === 0xDEADBEEF)
//      assert(!a.expectsResumeValue)
//      assert(!a.resume)
//      assert(a.isCompleted)
//    }
//  }
//
//  test("desugar coroutine call") {
//    desugar {
//      val rube: Int -> (Int @@ Int) = coroutine[Int].of { (x: Int) =>
//        val v = 0xDEADBEEF
//        yieldval(v)
//        x
//      }
//
//      val a = rube.inst(2)
//
//      assert(a.resume)
//      assert(a.value === 0xDEADBEEF)
//      assert(!a.expectsResumeValue)
//      assert(!a.resume)
//      assert(a.isCompleted)
//    }
//  }
}