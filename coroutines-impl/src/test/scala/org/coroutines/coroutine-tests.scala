package org.coroutines

import org.scalatest._
import scala.util.Failure

class CoroutineTest extends funsuite.AnyFunSuite {
  test("should not yield") {
    val getOk = coroutine[Nothing].of { () => "ok" }
    val c = getOk.inst()
    assert(c.isLive)
    intercept[RuntimeException](c.result)
    intercept[RuntimeException](c.value)
    assert(!c.resume)
    intercept[RuntimeException](c.value)
    assert(c.result == "ok")
    assert(!c.isLive)
  }

  test("should throw when not alive") {
    val gimmeFive = coroutine[Nothing].of { () => 5 }
    val c = gimmeFive.inst()
    assert(c.isLive)
    assert(!c.resume)
    assert(c.result == 5)
    assert(c.getResult == Some(5))
    assert(!c.isLive)
    intercept[CoroutineStoppedException](c.resume)
    assert(c.getValue == None)
    c.tryValue match {
      case Failure(t: RuntimeException) =>
      case _ => assert(false)
    }
  }

  test("should yield once") {
    val plusMinus = coroutine[Int].of { (x: Int) =>
      yieldval(x)
      -x
    }
    val c = plusMinus.inst(5)
    assert(c.isLive)
    assert(c.resume)
    assert(c.value == 5)
    assert(c.isLive)
    assert(!c.resume)
    assert(c.result == -5)
    assert(!c.isLive)
  }

  test("should yield several times") {
    val sumAndDiffs = coroutine[Int].of { (x: Int, y: Int) =>
      val sum = x + y
      yieldval(sum)
      val diff1 = x - y
      yieldval(diff1)
      val diff2 = y - x
      diff2
    }
    val c = sumAndDiffs.inst(1, 2)
    assert(c.isLive)
    assert(c.resume)
    assert(c.value == 3)
    assert(c.isLive)
    assert(c.resume)
    assert(c.value == -1)
    assert(c.isLive)
    assert(!c.resume)
    assert(c.result == 1)
    assert(!c.isLive)
  }

  test("should lub yieldvals") {
    val lists = coroutine[List[Any]].of { (x: Int) =>
      yieldval(List(x))
      yieldval(List(x.toString))
    }
    val anotherLists: Coroutine._1[Int, List[Any], Unit] = lists
    val c = lists.inst(5)
    assert(c.resume)
    assert(c.value == List(5))
    assert(c.resume)
    assert(c.value == List("5"))
    assert(!c.resume)
    c.result
    assert(!c.hasException)
  }

  test("should lub yieldtos and returns") {
    val wrapString = coroutine[Nothing].of { (x: String) =>
      List(x.toString)
    }
    val f: Coroutine.Instance[Nothing, List[String]] = wrapString.inst("ok")
    val wrapInt = coroutine[Nothing].of { (x: Int) =>
      yieldto(f)
      Vector(x)
    }
    val c = wrapInt.inst(7)
    assert(c.resume)
    assert(c.getResult == None)
    assert(c.getValue == None)
    assert(!c.resume)
    assert(c.result == Vector(7))
  }

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
  }

  test("should declare body with a coroutine call") {
    val doubleInt = coroutine[Nothing].of { (x: Int) => 2 * x }
    val callOther = coroutine[Nothing].of { (x: Int) =>
      val y = doubleInt(x)
      y
    }
    val c = callOther.inst(5)
    assert(!c.resume)
    assert(c.result == 10)
    assert(!c.isLive)
  }

  test("should declare a value in a nested scope") {
    val someValues = coroutine[Int].of { (x: Int, y: Int) =>
      if (x > 0) {
        val z = -x
        yieldval(z)
        yieldval(-z)
      } else {
        yieldval(y)
      }
      x
    }
    val c1 = someValues.inst(5, 7)
    assert(c1.resume)
    assert(c1.value == -5)
    assert(c1.resume)
    assert(c1.value == 5)
    assert(!c1.resume)
    assert(c1.result == 5)
    val c2 = someValues.inst(-5, 7)
    assert(c2.resume)
    assert(c2.value == 7)
    assert(!c2.resume)
    assert(c2.result == -5)
  }

  test("should declare a variable in a nested scope") {
    val someValues = coroutine[Int].of { (x: Int, y: Int) =>
      if (x > 0) {
        var z = -x
        yieldval(z)
        z = -z
        yieldval(z)
      } else {
        yieldval(x)
      }
      y
    }
    val c1 = someValues.inst(6, 11)
    assert(c1.resume)
    assert(c1.value == -6)
    assert(c1.resume)
    assert(c1.value == 6)
    assert(!c1.resume)
    assert(c1.result == 11)
    val c2 = someValues.inst(-6, 11)
    assert(c2.resume)
    assert(c2.value == -6)
    assert(!c2.resume)
    assert(c2.result == 11)
  }

  test("coroutine should be called") {
    val emitTwice = coroutine[Int].of { (x: Int) =>
      yieldval(x)
      x
    }
    val c = emitTwice.inst(7)
    assert(c.resume)
    assert(c.value == 7)
    assert(!c.resume)
    assert(c.result == 7)
  }

  test("coroutine should contain an if statement and no yields") {
    val abs = coroutine[Nothing].of { (x: Int) =>
      if (x > 0) x
      else -x
    }
    val c1 = abs.inst(-5)
    assert(!c1.resume)
    assert(c1.result == 5)
    val c2 = abs.inst(5)
    assert(!c2.resume)
    assert(c2.result == 5)
  }

  test("coroutine should contain two applications at the end of two branches") {
    val ident = coroutine[Nothing].of { (x: Int) => x }
    val branch = coroutine[Nothing].of { (x: Int) =>
      if (x > 0) {
        val y = ident(x)
      } else {
        val z = ident(-x)
      }
      x
    }
    val c1 = branch.inst(5)
    assert(!c1.resume)
    assert(c1.result == 5)
    assert(c1.isCompleted)
    val c2 = branch.inst(-27)
    assert(!c2.resume)
    assert(c2.result == -27)
    assert(c2.isCompleted)
  }

  test("coroutine should contain two assignments at the end of two branches") {
    val double = coroutine[Nothing].of { (n: Int) => 2 * n }
    val branch = coroutine[Nothing].of { (x: Int) =>
      var y = 0
      if (x > 0) {
        val z = double(x)
        y = z
      } else {
        val z = double(-x)
        y = z
      }
      y
    }
    val c1 = branch.inst(5)
    assert(!c1.resume)
    assert(c1.result == 10)
    val c2 = branch.inst(-10)
    assert(!c2.resume)
    assert(c2.result == 20)
  }

  test("coroutine should have an integer argument and a string local variable") {
    val stringify = coroutine[Nothing].of { (x: Int) =>
      val s = x.toString
      s
    }
    val c = stringify.inst(11)
    assert(!c.resume)
    assert(c.result == "11")
  }

  test("coroutine should assign") {
    val assign = coroutine[Nothing].of { (x: Int) =>
      var y = 0
      y = x + 1
      y
    }
    val c = assign.inst(5)
    assert(!c.resume)
    assert(c.result == 6)
  }

  test("coroutine should contain a while loop") {
    val number = coroutine[Nothing].of { () =>
      var i = 0
      while (i < 10) {
        i += 1
      }
      i
    }
    val c = number.inst()
    assert(!c.resume)
    assert(c.result == 10)
  }

  test("coroutine should contains a while loop with a yieldval") {
    val numbers = coroutine[Int].of { () =>
      var i = 0
      while (i < 10) {
        yieldval(i)
        i += 1
      }
      i
    }
    val c = numbers.inst()
    for (i <- 0 until 10) {
      assert(c.resume)
      assert(c.value == i)
    }
    assert(!c.resume)
    assert(c.result == 10)
    assert(c.isCompleted)
  }

  test("coroutine should correctly skip the while loop") {
    val earlyFinish = coroutine[Int].of { (x: Int) =>
      var i = 1
      while (i < x) {
        yieldval(i) 
        i += 1
      }
      i
    }
    val c = earlyFinish.inst(0)
    assert(!c.resume)
    assert(c.result == 1)
    assert(c.isCompleted)
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
    assert(!c.resume)
    c.result
    assert(!c.hasException)
  }

  test("an anonymous coroutine should be applied") {
    coroutine[Nothing].of { (x: Int) => x }
  }

  test("if statement should be properly regenerated") {
    val addOne = coroutine[Nothing].of { (x: Int) =>
      if (x > 0) {
        x
      } else {
        -x
      }
      x + 1
    }
    val c1 = addOne.inst(1)
    assert(!c1.resume)
    assert(c1.result == 2)
    val c2 = addOne.inst(-1)
    assert(!c2.resume)
    assert(c2.result == 0)
  }

  test("if statement with unit last statement should be properly generated") {
    val addOne = coroutine[Nothing].of { () =>
      var x = 5
      if (0 < x) {
        x = 2
        ()
      } else {
        x = 1
        ()
      }
      x
    }
    val c = addOne.inst()
    assert(!c.resume)
    assert(c.result == 2)
  }

  test("coroutine should yield every second element") {
    val rube = coroutine[Int].of { (x: Int) =>
      var i = 0
      while (i < x) {
        if (i % 2 == 0) yieldval(i)
        i += 1
      }
      i
    }
    val c = rube.inst(5)
    for (i <- 0 until 5; if i % 2 == 0) {
      assert(c.resume)
      assert(c.value == i)
    }
    assert(!c.resume)
    assert(c.result == 5)
  }

  test("coroutine should yield x, 117, and -x") {
    val rube = coroutine[Int].of { (x: Int) =>
      var z = x
      yieldval(z)
      yieldval(117)
      -z
    }
    val c = rube.inst(7)
    assert(c.resume)
    assert(c.value == 7)
    assert(c.resume)
    assert(c.value == 117)
    assert(!c.resume)
    assert(c.result == -7)
    assert(c.isCompleted)
  }

  test("coroutine should yield 117, an even number and 17, or a negative odd number") {
    var rube = coroutine[Int].of { () =>
      var i = 0
      while (i < 4) {
        var z = i
        if (i % 2 == 0) {
          yieldval(117)
          yieldval(z)
          yieldval(17)
        } else {
          yieldval(-z)
        }
        i += 1
      }
      i
    }
    val c = rube.inst()
    assert(c.resume)
    assert(c.value == 117)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 17)
    assert(c.resume)
    assert(c.value == -1)
    assert(c.resume)
    assert(c.value == 117)
    assert(c.resume)
    assert(c.value == 2)
    assert(c.resume)
    assert(c.value == 17)
    assert(c.resume)
    assert(c.value == -3)
    assert(!c.resume)
    assert(c.result == 4)
    assert(c.isCompleted)
  }

  test("coroutine should yield first variable, then second variable, then first") {
    val rube = coroutine[Int].of { () =>
      val x = 1
      yieldval(x)
      val y = 2
      yieldval(y)
      x
    }

    val c = rube.inst()
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 2)
    assert(!c.resume)
    assert(c.result == 1)
    assert(c.isCompleted)
  }

  test("coroutine should yield x, then y or z, then x again") {
    val rube = coroutine[Int].of { (v: Int) =>
      val x = 1
      yieldval(x)
      if (v < 0) {
        val y = x * 3
        yieldval(y)
        yieldval(-y)
      } else {
        val z = x * 2
        yieldval(z)
        yieldval(-z)
      }
      x
    }

    val c0 = rube.inst(5)
    assert(c0.resume)
    assert(c0.value == 1)
    assert(c0.resume)
    assert(c0.value == 2)
    assert(c0.resume)
    assert(c0.value == -2)
    assert(!c0.resume)
    assert(c0.result == 1)
    assert(c0.isCompleted)

    val c1 = rube.inst(-2)
    assert(c1.resume)
    assert(c1.value == 1)
    assert(c1.resume)
    assert(c1.value == 3)
    assert(c1.resume)
    assert(c1.value == -3)
    assert(!c1.resume)
    assert(c1.result == 1)
    assert(c1.isCompleted)
  }

  test("coroutine should yield x, z, x, 117, or just x, 117") {
    val rube = coroutine[Int].of { (v: Int) =>
      val x = v
      yieldval(x)
      if (x > 0) {
        val z = 1
        yieldval(z)
        yieldval(x)
      }
      117
    }

    val c0 = rube.inst(5)
    assert(c0.resume)
    assert(c0.value == 5)
    assert(c0.resume)
    assert(c0.value == 1)
    assert(c0.resume)
    assert(c0.value == 5)
    assert(!c0.resume)
    assert(c0.result == 117)
    assert(c0.isCompleted)

    val c1 = rube.inst(-7)
    assert(c1.resume)
    assert(c1.value == -7)
    assert(!c1.resume)
    assert(c1.result == 117)
    assert(c1.isCompleted)
  }

//  // FIXME fails to compile under 2.13
//  test("should lub nested coroutine calls and returns") {
//    // [Nothing, List[Int]]
//    val id = coroutine[Int].of { (xs: List[Int]) => xs }
//    val attach = coroutine[Int].of { (xs: List[Int]) =>
//      val ys = id(xs)
//      "ok" :: ys
//    }
//
//    val c = attach.inst(1 :: Nil)
//    assert(!c.resume)
//    assert(c.result == "ok" :: 1 :: Nil)
//    assert(c.isCompleted)
//  }
//
//  // FIXME fails to compile under 2.13
//  test("nested coroutine definitions should not affect type of outer coroutine") {
//    val rube: Coroutine._1[List[Int], Nothing, List[Int]] = coroutine[Int].of {
//      (xs: List[Int]) =>
//      val nested = coroutine[Int].of { (x: Int) =>
//        yieldval(-x)
//        x
//      }
//      2 :: xs
//    }
//
//    val c = rube.inst(1 :: Nil)
//    assert(!c.resume)
//    assert(c.result == 2 :: 1 :: Nil)
//    assert(c.isCompleted)
//  }

  test("two nested loops should yield correct values") {
    val rube = coroutine[Int].of { (xs: List[Int]) =>
      var left = xs
      while (left != Nil) {
        var i = 0
        while (i < left.head) {
          yieldval(i)
          i += 1
        }
        left = left.tail
      }
      0
    }

    val c = rube.inst(List(1, 2, 3))
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 0)
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 2)
    assert(!c.resume)
    assert(c.result == 0)
    assert(c.isCompleted)
  }

  test("pull should always yield a value") {
    val goldberg = coroutine[Unit].of { () =>
      yieldval(())
    }
    val c0 = goldberg.inst()
    val rube = coroutine[Int].of { (xs: List[Int]) =>
      yieldto(c0)
      var ys = xs
      while (ys != Nil) {
        yieldval(ys.head)
        ys = ys.tail
      }
    }

    val c1 = rube.inst(1 :: 2 :: Nil)
    assert(c1.pull)
    assert(c1.value == 1)
    assert(c1.pull)
    assert(c1.value == 2)
    assert(!c1.pull)
  }
}


class WideValueTypesTest extends funsuite.AnyFunSuite {
  test("should use a long stack variable") {
    val rube = coroutine[Long].of { (x: Long) =>
      var y = x
      y = x * 3
      yieldval(-x)
      yieldval(y)
      x * 2
    }

    val c = rube.inst(15L)
    assert(c.resume)
    assert(c.value == -15L)
    assert(c.resume)
    assert(c.value == 45L)
    assert(!c.resume)
    assert(c.result == 30L)
  }

  test("should use a double stack variable") {
    val rube = coroutine[Double].of { (x: Double) =>
      var y = x
      y = x * 4
      yieldval(-x)
      yieldval(y)
      x * 2
    }

    val c = rube.inst(2.0)
    assert(c.resume)
    assert(c.value == -2.0)
    assert(c.resume)
    assert(c.value == 8.0)
    assert(!c.resume)
    assert(c.result == 4.0)
  }

  test("should call a coroutine that returns a double value") {
    val twice = coroutine[Nothing].of { (x: Double) =>
      x * 2
    }
    val rube = coroutine[Double].of { (x: Double) =>
      yieldval(x)
      yieldval(twice(x))
      x * 4
    }

    val c = rube.inst(2.0)
    assert(c.resume)
    assert(c.value == 2.0)
    assert(c.resume)
    assert(c.value == 4.0)
    assert(!c.resume)
    assert(c.result == 8.0)
    assert(c.isCompleted)
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

  test("should be able to access thrown exceptions via `getException`") {
    val failure = coroutine[Nothing].of { () =>
      sys.error("rats!")
    }
    val instance = failure.inst()
    assert(!instance.resume)
    assert(instance.hasException)
    instance.getException match {
      case Some(exception) => assert(exception.getMessage() == "rats!")
      case None => assert(false)
    }
  }

  test("`getException` should return `None` when there is no exception") {
    val simple = coroutine[Int].of { () =>
      yieldval(1)
      2
    }
    val instance = simple.inst()
    assert(instance.getException == None)
    assert(instance.resume)
    assert(instance.value == 1)
    assert(instance.getException == None)
    assert(!instance.resume)
    assert(instance.result == 2)
    assert(instance.getException == None)
  }
}
