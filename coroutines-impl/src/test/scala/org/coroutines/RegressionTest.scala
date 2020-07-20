package org.coroutines



import org.scalatest._
import scala.util.Failure



class RegressionTest extends funsuite.AnyFunSuite {
  test("should declare body with if statement") {
    val xOrY = coroutine[Int].of { (x: Int, y: Int) =>
      if (x > 0) {
        yieldval(x)
      } else {
        yieldval(y)
      }
    }
    val c1 = xOrY.inst(5, 2)
    assert(c1.resume)
    assert(c1.value == 5)
    assert(!c1.resume)
    assert(c1.isCompleted)
    c1.result
    assert(!c1.hasException)
    val c2 = xOrY.inst(-2, 7)
    assert(c2.resume)
    assert(c2.value == 7)
    assert(!c2.resume)
    assert(c2.isCompleted)
    c2.result
    assert(!c2.hasException)
  }

  test("coroutine should have a nested if statement") {
    val numbers = coroutine[Int].of { () =>
      var z = 1
      var i = 1
      while (i < 5) {
        if (z > 0) {
          yieldval(z * i)
          z = -1
        } else {
          yieldval(z * i)
          z = 1
          i += 1
        }
      }
    }
    val c = numbers.inst()
    for (i <- 1 until 5) {
      assert(c.resume)
      assert(c.value == i)
      assert(c.resume)
      assert(c.value == -i)
    }
  }

  test("coroutine should call a coroutine with a different return type") {
    val stringer = coroutine[Nothing].of { (x: Int) => x.toString }
    val caller = coroutine[String].of { (x: Int) =>
      val s = stringer(2 * x)
      yieldval(s)
      x * 3
    }

    val c = caller.inst(5)
    assert(c.resume)
    assert(c.value == "10")
    assert(!c.resume)
    assert(c.result == 15)
  }

  test("issue #14 -- simple case") {
    object Test {
      val foo: Coroutine._1[Int, Int, Unit] = coroutine[Int].of { (i: Int) =>
        yieldval(i)
        if (i > 0) {
          foo(i - 1)
          foo(i - 1)
        }
      }
    }

    val c = Test.foo.inst(2)
    assert(c.resume)
    assert(c.value == 2)
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 0)
    assert(!c.resume)
  }

  test("issue #14 -- complex case") {
    object Test {
      val foo: Coroutine._1[Int, Int, Unit] = coroutine[Int].of { (i: Int) =>
        yieldval(i)
        if (i > 0) {
          foo(i - 1)
          foo(i - 1)
        }
      }
    }

    val bar = coroutine[Int].of { () =>
      Test.foo(2)
      Test.foo(2)
    }

    val c = bar.inst()
    assert(c.resume)
    assert(c.value == 2)
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 2)
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 0)
    assert(!c.resume)
  }

  test("issue #15 -- hygiene") {
    val scala, Any, String, TypeTag, Unit = ()
    trait scala; trait Any; trait String; trait TypeTag; trait Unit

    val id = coroutine[Nothing].of { (x: Int) =>
      x
    }
  }

  test("issue #15 -- more hygiene") {
    val org, coroutines, Coroutine = ()
    trait org; trait coroutines; trait Coroutine
    
    val id = coroutine[Nothing].of { () => }
  }

  test("should use c as an argument name") {
    val nuthin = coroutine[Nothing].of { () => }
    val resumer = coroutine[Nothing].of { (c: Nothing <~> Unit) =>
      c.resume
    }
    val c = nuthin.inst()
    val r = resumer.inst(c)
    assert(!r.resume)
    assert(!r.hasException)
    assert(r.hasResult)
  }

  test("issue #21") {
    val test = coroutine[Nothing].of { () => {} }
    val foo = coroutine[Nothing].of { () => {
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test()
        test() // Lines after this did not previously compile.
        test()
        test()
      }
    }
  }

  test("must catch exception passed from a direct call") {
    val buggy = coroutine[Nothing].of { () =>
      throw new Exception
    }
    val catchy = coroutine[Nothing].of { () =>
      var result = "initial value"
      try {
        buggy()
        "not ok..."
      } catch {
        case e: Exception =>
          result = "caught!"
      }
      result
    }

    val c = catchy.inst()
    assert(!c.resume)
    assert(c.result == "caught!")
  }
}
