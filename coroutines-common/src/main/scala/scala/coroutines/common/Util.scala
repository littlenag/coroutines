package scala.coroutines.common

/**
 *
 */
import scala.reflect.macros.blackbox
import scala.reflect.runtime.universe._
import scala.language.experimental.macros

object Util {

  def _desugar[T](c : blackbox.Context)(expr : c.Expr[T]): c.Expr[T] = {
    import c.universe._
    println("DESUGAR vvvvvvv")
    println(show(expr.tree))
    println("DESUGAR ^^^^^^^")
    expr
  }

  def desugar[T](expr : T): T = macro _desugar[T]

}
