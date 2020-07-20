package org.examples



import org.coroutines._



object Identity {
  val id = coroutine[Nothing].of { (x: Int) => x }

  def main(args: Array[String]) {
    val c = id.inst(7)
    assert(!c.resume)
    assert(c.result == 7)
  }
}
