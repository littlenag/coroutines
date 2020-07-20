package org.coroutines

import org.scalatest._
import scala.collection._
import scala.util.Failure


class YieldToTest extends funsuite.AnyFunSuite {
  test("after resuming to another coroutine, there should be no value") {
    val another = coroutine[String].of { () =>
      yieldval("Yohaha")
    }
    val anotherInstance = another.inst()

    val rube = coroutine[String].of { () =>
      yieldval("started")
      yieldto(anotherInstance)
    }

    val c = rube.inst()
    assert(c.resume)
    assert(c.hasValue)
    assert(c.value == "started")
    assert(c.resume)
    assert(!c.hasValue)
    assert(!c.resume)
    assert(!c.hasValue)
    assert(c.isCompleted)
    assert(anotherInstance.hasValue)
    assert(anotherInstance.value == "Yohaha")
  }

  test("yielding to a completed coroutine raises an error") {
    val another = coroutine[Nothing].of { () => "in and out" }
    val anotherInstance = another.inst()
    assert(!anotherInstance.resume)

    val rube = coroutine[String].of { () =>
      yieldto(anotherInstance)
      yieldval("some more")
    }
    val c = rube.inst()
    assert(!c.resume)
    c.tryResult match {
      case Failure(e: CoroutineStoppedException) =>
      case _ => assert(false, "Should have thrown an exception.")
    }
  }

  test("should be able to yield to a differently typed coroutine") {
    val another: ~~~>[String, Unit] = coroutine[String].of { () =>
      yieldval("hohoho")
    }
    val anotherInstance = another.inst()

    val rube: Int ~~> (Int, Int) = coroutine[Int].of { (x: Int) =>
      yieldval(-x)
      yieldto(anotherInstance)
      x
    }
    val c = rube.inst(5)

    assert(c.resume)
    assert(c.value == -5)
    assert(c.resume)
    assert(!c.hasValue)
    assert(!c.resume)
    assert(c.result == 5)
    assert(anotherInstance.hasValue)
    assert(anotherInstance.value == "hohoho")
  }

  test("should drain the coroutine instance that yields to another coroutine") {
    val another: ~~~>[String, Unit] = coroutine[String].of { () =>
      yieldval("uh-la-la")
    }
    val anotherInstance = another.inst()

    val rube: (Int, Int) ~> (Int, Unit) = coroutine[Int].of { (x: Int, y: Int) =>
      yieldval(x)
      yieldval(y)
      yieldto(anotherInstance)
      yieldval(x * y)
    }
    val c = rube.inst(5, 4)

    val b = mutable.Buffer[Int]()
    while (c.resume) if (c.hasValue) b += c.value

    assert(b == Seq(5, 4, 20))
  }
}
