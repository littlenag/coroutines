package org.coroutines


import scala.coroutines.common.Util._
import org.scalatest._

class FriendlyTypeTest extends funsuite.AnyFunSuite {

  // takes Unit, returns Unit, yields R -> Enumerator
  // take Unit, return R, yields R

  test("use friendly type for coroutine artity 0") {
    val rube: Unit -> (Int @@ Unit) = coroutine { () =>
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
    assert(a.result === 2)
    assert(a.isCompleted)
  }


  test("use friendly type for coroutine artity 2") {
    val rube: (Int,Int) -> (Int @@ Int) = coroutine { (x: Int, y: Int) =>
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

}