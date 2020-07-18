package org.coroutines

import scala.reflect.macros.blackbox.Context

object CoroutineMacros {
  def synthesize[R: c.WeakTypeTag](c: Context)(f: c.Tree): c.Tree = {
    new Synthesizer[c.type](c).synthesize(f)
  }

  def call[T: c.WeakTypeTag](c: Context)(f: c.Tree): c.Tree = {
    new Synthesizer[c.type](c).call(f)
  }
}