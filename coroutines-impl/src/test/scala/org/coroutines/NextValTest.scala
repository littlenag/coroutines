package org.coroutines

import org.scalatest._

class NextValTest extends funsuite.AnyFunSuite {

  test("next-val statement") {
    // streams <- in
    // channels <- out
    // streams become part of the arguments that must be supplied
    // channels that you publish to get returned as streams
    val echo = coroutine[String].of { () =>
      val e = next[String]()
      yieldval(e)
    }

    val e = echo.inst()
    assert(e.resume)
    assert(e.expectsResumeValue)
    assert(e.resumeWithValue("5"))
    assert(e.value == "5")
    assert(!e.expectsResumeValue)
    assert(!e.resume)
    assert(e.isCompleted)
  }
}