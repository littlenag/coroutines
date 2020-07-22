package org.coroutines

import scala.coroutines.common.Util._
import org.scalatest._

class FriendlyTypeTest extends funsuite.AnyFunSuite {

  // model using streams and channels? coroutine can only suspend?
  // streams -> can consume values from
  // channels -> can publish values to

  // ends up more like reactor framework

  // how do you have these ops: buffer, next, yield
  // and make them non-blocking?


  // takes Unit, returns Unit, yields R -> Enumerator
  // take Unit, return R, yields R

  test("use raw type for coroutine artity 0") {
    val rube: Coroutine._0[Int, Unit] = coroutine[Int].of { () =>
      val v = 0xDEADBEEF
      yieldval(v)
    }

    val a = rube.inst()

    assert(a.resume)
    assert(a.value === 0xDEADBEEF)
    assert(!a.expectsResumeValue)
    assert(!a.resume)
    assert(a.result === ())
    assert(a.isCompleted)
  }

  test("use raw type for coroutine artity 3") {
    val rube: Coroutine._3[Int, Int, Int, Int, Int] = coroutine[Int].of { (x: Int, y: Int, z:Int) =>
      val v = 0xDEADBEEF
      yieldval(v)
      x * y * z
    }

    val a = rube.inst(2,2,2)

    assert(a.resume)
    assert(a.value === 0xDEADBEEF)
    assert(!a.expectsResumeValue)
    assert(!a.resume)
    assert(a.result === 8)
    assert(a.isCompleted)
  }

  test("use friendly type for coroutine artity 0") {
    val rube: Unit ~> (Int @@ Unit) = coroutine[Int].of { () =>
      val v = 0xDEADBEEF
      yieldval(v)
    }

    val a = rube.inst()

    assert(a.resume)
    assert(a.value === 0xDEADBEEF)
    assert(!a.expectsResumeValue)
    assert(!a.resume)
    assert(a.result === ())
    assert(a.isCompleted)
  }

  test("use friendly type for coroutine artity 1") {
    val rube: Int ~> (Int @@ Int) = coroutine[Int].of { (x: Int) =>
      val v = 0xDEADBEEF
      yieldval(v)
      x
      }

    val a = rube.inst(2)

    assert(a.resume)
    assert(a.value === 0xDEADBEEF)
    assert(!a.expectsResumeValue)
    assert(!a.resume)
    assert(a.result === 2)
    assert(a.isCompleted)
  }


  test("use friendly type for coroutine artity 2") {
    val rube: (Int,Int) ~> (Int @@ Int) = coroutine[Int].of { (x: Int, y: Int) =>
      val v = 0xDEADBEEF
      yieldval(v)
      x * y
    }

    val a = rube.inst(2,4)

    assert(a.resume)
    assert(a.value === 0xDEADBEEF)
    assert(!a.expectsResumeValue)
    assert(!a.resume)
    assert(a.result === 8)
    assert(a.isCompleted)
  }

  test("use friendly type for coroutine artity 3") {
    val rube: (Int,Int,Int) ~> (Int @@ Int) = coroutine[Int].of { (x: Int, y: Int, z:Int) =>
      val v = 0xDEADBEEF
      yieldval(v)
      x * y * z
    }

    val a = rube.inst(2,2,2)

    assert(a.resume)
    assert(a.value === 0xDEADBEEF)
    assert(!a.expectsResumeValue)
    assert(!a.resume)
    assert(a.result === 8)
    assert(a.isCompleted)
  }

}