package org.examples



import org.coroutines._



object CompositionCall {
  private val optionElems = coroutine[Int].of { (opt: Option[Int]) =>
    opt match {
      case Some(x) => yieldval(x)
      case None => // do nothing
    }
  }

  private val optionListElems = coroutine[Int].of { (xs: List[Option[Int]]) =>
    var curr = xs
    while (curr != Nil) {
      val c = optionElems.inst(curr.head)
      while (c.resume) yieldval(c.value)
      curr = curr.tail
    }
  }

  def main(args: Array[String]) {
    val xs = Some(1) :: None :: Some(3) :: Nil
    val c = optionListElems.inst(xs)
    assert(c.resume)
    assert(c.value == 1)
    assert(c.resume)
    assert(c.value == 3)
    assert(!c.resume)
  }
}
