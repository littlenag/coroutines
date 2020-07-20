package org.coroutines

import org.scalatest._

class ASTCanonicalizationTest extends funsuite.AnyFunSuite {
  test("if statements with applications") {
    val rube = coroutine[Nothing].of { () =>
      if (0 < { math.abs(-1); math.max(1, 2) }) 2 else 1
    }
    val c = rube.inst()
    assert(!c.resume)
    assert(c.result == 2)
  }

  test("if statements with applications and yield") {
    val rube = coroutine[Int].of { () =>
      val x = if (0 < { math.abs(-1); math.max(1, 2) }) 2 else 1
      yieldval(x)
      -x
    }
    val c = rube.inst()
    assert(c.resume)
    assert(c.value == 2)
    assert(!c.resume)
    assert(c.result == -2)
    assert(c.isCompleted)
  }

  test("if statements with selections") {
    val rube = coroutine[Nothing].of { () =>
      if (0 < { math.abs(math.Pi) }) 2 else 1
    }
    val c = rube.inst()
    assert(!c.resume)
    assert(c.result == 2)
  }

  test("if statements with selections and yield") {
    val rube = coroutine[Int].of { () =>
      val x = if (0 < { math.abs(math.Pi) }) 2 else 1
      yieldval(x)
      -x
    }
    val c = rube.inst()
    assert(c.resume)
    assert(c.value == 2)
    assert(!c.resume)
    assert(c.result == -2)
    assert(c.isCompleted)
  }

  test("if statements with updates") {
    val rube = coroutine[Nothing].of { () =>
      val xs = new Array[Int](2)
      if (0 < { xs(0) = 1; xs(0) }) 2 else 1
    }
    val c = rube.inst()
    assert(!c.resume)
    assert(c.result == 2)
  }

  test("if statements with block in tuple") {
    val rube = coroutine[Nothing].of { () =>
      if (0 < ({ math.abs(1); math.abs(3) + 2 }, 2)._1) 2 else 1
    }
    val c = rube.inst()
    assert(!c.resume)
    assert(c.result == 2)
  }

  test("if statement with another if statement in condition") {
    val rube = coroutine[Nothing].of { () =>
      if (0 < (if (math.abs(-1) > 5) 1 else 2)) 2 else 1
    }
    val c = rube.inst()
    assert(!c.resume)
    assert(c.result == 2)
  }

  test("value declaration should be the last statement") {
    val unit = coroutine[Nothing].of { () =>
     val t = (2, 3)
     val (y, z) = t
    }

    val c = unit.inst()
    assert(!c.resume)
    assert(!c.isLive)
    c.result
    assert(!c.hasException)
  }

  test("coroutine should be callable outside value declaration") {
    var y = 0
    val setY = coroutine[Nothing].of { (x: Int) => y = x }
    val setTo5 = coroutine[Nothing].of { () =>
      setY(5)
    }
    val c = setTo5.inst()
    assert(!c.resume)
    assert(y == 5)
  }

  test("coroutine should be callable outside value declaration and yield") {
    var y = 0
    val setY = coroutine[Nothing].of { (x: Int) => y = x }
    val setTo5 = coroutine[Unit].of { () =>
      yieldval(setY(5))
      setY(-5)
    }
    val c = setTo5.inst()
    assert(c.resume)
    assert(y == 5)
    assert(!c.resume)
    assert(y == -5)
  }

  test("coroutine should yield in while loop with complex condition") {
    val rube = coroutine[Int].of { (x: Int) =>
      var i = 0
      while (i < x && x < math.abs(-15)) {
        yieldval(i)
        i += 1
      }
      i
    }
    val c1 = rube.inst(10)
    for (i <- 0 until 10) {
      assert(c1.resume)
      assert(c1.value == i)
    }
    assert(!c1.resume)
    assert(c1.result == 10)
    assert(c1.isCompleted)
    val c2 = rube.inst(20)
    assert(!c2.resume)
    assert(c2.result == 0)
    assert(c2.isCompleted)
  }

  test("coroutine should yield every second element or just zero") {
    val rube = coroutine[Int].of { (x: Int) =>
      var i = 0
      while (i < x && x < math.abs(-15)) {
        if (i % 2 == 0) yieldval(i)
        i += 1
      }
      i
    }

    val c1 = rube.inst(10)
    for (i <- 0 until 10; if i % 2 == 0) {
      assert(c1.resume)
      assert(c1.value == i)
    }
    assert(!c1.resume)
    assert(c1.result == 10)
    assert(c1.isCompleted)
    val c2 = rube.inst(20)
    assert(!c2.resume)
    assert(c2.result == 0)
    assert(c2.isCompleted)
  }

  test("coroutine should yield 1 or yield 10 elements, and then 117") {
    val rube = coroutine[Int].of { (x: Int) =>
      var i = 1
      if (x > math.abs(0)) {
        while (i < x) {
          yieldval(i)
          i += 1
        }
      } else {
        yieldval(i)
      }
      117
    }

    val c1 = rube.inst(10)
    for (i <- 1 until 10) {
      assert(c1.resume)
      assert(c1.value == i)
    }
    assert(!c1.resume)
    assert(c1.result == 117)
    assert(c1.isCompleted)
    val c2 = rube.inst(-10)
    assert(c2.resume)
    assert(c2.value == 1)
    assert(!c2.resume)
    assert(c2.result == 117)
    assert(c2.isCompleted)
  }

  test("yield absolute and original value") {
    val rube = coroutine[Int].of { (x: Int) =>
      yieldval(math.abs(x))
      x
    }

    val c = rube.inst(-5)
    assert(c.resume)
    assert(c.value == 5)
    assert(!c.resume)
    assert(c.result == -5)
    assert(c.isCompleted)
  }

  // FIXME fails to pass for 2.13
  test("short-circuiting should work for and") {
    var state = "untouched"
    val rube = ofF { (x: Int) =>
      if (x < 0 && { state = "touched"; true }) x
      else -x
    }
    
    val c0 = rube.inst(5)
    assert(!c0.resume)
    assert(c0.result == -5)
    assert(c0.isCompleted)
    assert(state == "untouched")

    val c1 = rube.inst(-5)
    assert(!c1.resume)
    assert(c1.result == -5)
    assert(c1.isCompleted)
    assert(state == "touched")
  }

  // FIXME fails to pass for 2.13
  test("short-circuiting should work for or") {
    var state = "untouched"
    val rube = ofF { (x: Int) =>
      if (x > 0 || { state = "touched"; false }) x
      else -x
    }
    
    val c0 = rube.inst(5)
    assert(!c0.resume)
    assert(c0.result == 5)
    assert(c0.isCompleted)
    assert(state == "untouched")

    val c1 = rube.inst(-5)
    assert(!c1.resume)
    assert(c1.result == 5)
    assert(c1.isCompleted)
    assert(state == "touched")
  }

  test("do-while should be simplified into a while loop") {
    val rube = coroutine[Int].of { (x: Int) =>
      var i = 0
      do {
        yieldval(i)

        i += 1
      } while (i < x)
      i
    }

    val c0 = rube.inst(5)
    assert(c0.resume)
    assert(c0.value == 0)
    assert(c0.resume)
    assert(c0.value == 1)
    assert(c0.resume)
    assert(c0.value == 2)
    assert(c0.resume)
    assert(c0.value == 3)
    assert(c0.resume)
    assert(c0.value == 4)
    assert(!c0.resume)
    assert(c0.result == 5)
    assert(c0.isCompleted)

    val c1 = rube.inst(0)
    assert(c1.resume)
    assert(c1.value == 0)
    assert(!c1.resume)
    assert(c1.result == 1)
    assert(c1.isCompleted)
  }

  test("should be able to define uncalled function inside coroutine") {
    val oy = coroutine[Nothing].of { () =>
      def foo(): String = "bar"
      val bar = "bar"
      1
    }
    val c = oy.inst()
    assert(!c.resume)
    assert(c.hasResult)
    assert(c.result == 1)
    assert(c.isCompleted)
  }

  test("should be able to define called function inside coroutine") {
    val oy = coroutine[Nothing].of { () =>
      def foo(): String = "bar"
      val bar = foo()
      1
    }
    val c = oy.inst()
    assert(!c.resume)
    assert(c.hasResult)
    assert(c.result == 1)
    assert(c.isCompleted)
  }
}
