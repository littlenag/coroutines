package org.coroutines

import scala.coroutines.common.Util._
import org.scalatest._

class Playground extends funsuite.AnyFunSuite {
//  test("desugar coroutine") {
//    desugar {
//      coroutine { (x: AnyRef) =>
//        x match {
//          case s: String => s.length
//          case xs: List[_] => xs.size
//        }
//      }
//    }
//  }

//  test("desugar coroutine call") {
//    desugar {
//      val rube = coroutine { (x: AnyRef) =>
//        val v = 0xDEADBEEF
//        yieldval(v)
//        x
//      }
//
//      val c = call(rube("asdfsdf"))
//    }
//  }
}

object foo {
  {

    // desugaring of
    //      val rube = coroutine { (x: AnyRef) =>
    //        val v = 0xDEADBEEF
    //        yieldval(v)
    //        x
    //      }

    // canonicalize of
    // ((x: AnyRef) => {
    //   val v: Int = -559038737;
    //   val xa$macro$1: Unit = coroutines.this.`package`.yieldval[Int](v);
    //   ()
    // })

    val rube: org.coroutines.Coroutine._1[AnyRef,Int,AnyRef] with org.coroutines._1$spec$L[AnyRef,Int,AnyRef]{def $pop($c: org.coroutines.Coroutine.Instance[Int,AnyRef]): Unit} = {

      final class Anon extends org.coroutines.Coroutine._1[AnyRef,Int,AnyRef] with org.coroutines._1$spec$L[AnyRef,Int,AnyRef] {

        def $call(x: AnyRef): org.coroutines.Coroutine.Instance[Int,AnyRef] = {
          val c$macro$3: org.coroutines.Coroutine.Instance[Int,AnyRef] = new org.coroutines.Coroutine.Instance[Int,AnyRef]();
          this.$push(c$macro$3, x);
          c$macro$3
        };

        def apply(x: AnyRef): AnyRef = scala.sys.error(org.coroutines.COROUTINE_DIRECT_APPLY_ERROR_MESSAGE);

        def $push($c: org.coroutines.Coroutine.Instance[Int,AnyRef], x: AnyRef): Unit = {
          {
            if ($c.$costack.==(null))
              $c.$costack_=(new Array[org.coroutines.Coroutine[Int,AnyRef]](-1))
            else
              ();
            if ($c.$costackptr.>=($c.$costack.length))
            {
              val nstack: Array[org.coroutines.Coroutine[Int,AnyRef]] = new Array[org.coroutines.Coroutine[Int,AnyRef]]($c.$costack.length.*(2));
              java.lang.System.arraycopy($c.$costack, 0, nstack, 0, $c.$costack.length);
              $c.$costack_=(nstack)
            }
            else
              ();
            $c.$costack.update($c.$costackptr, this);
            $c.$costackptr_=($c.$costackptr.+(1))
          };
          {
            if ($c.$pcstack.==(null))
              $c.$pcstack_=(new Array[Short](-1))
            else
              ();
            if ($c.$pcstackptr.>=($c.$pcstack.length))
            {
              val nstack: Array[Short] = new Array[Short]($c.$pcstack.length.*(2));
              java.lang.System.arraycopy($c.$pcstack, 0, nstack, 0, $c.$pcstack.length);
              $c.$pcstack_=(nstack)
            }
            else
              ();
            $c.$pcstack.update($c.$pcstackptr, 0.toShort);
            $c.$pcstackptr_=($c.$pcstackptr.+(1))
          };
          {
            if ($c.$refstack.==(null))
              $c.$refstack_=(new Array[AnyRef](4))
            else
              ();
            $c.$refstackptr_=($c.$refstackptr.+(1));
            while ($c.$refstackptr.>=($c.$refstack.length)) {
              val nstack: Array[AnyRef] = new Array[AnyRef]($c.$refstack.length.*(2));
              java.lang.System.arraycopy($c.$refstack, 0, nstack, 0, $c.$refstack.length);
              $c.$refstack_=(nstack)
            }
          };
          $c.$refstack.update($c.$refstackptr.-(1).-(0), x)
        };

        def $pop($c: org.coroutines.Coroutine.Instance[Int,AnyRef]): Unit = {
          {
            $c.$pcstackptr_=($c.$pcstackptr.-(1));
            val fresh$macro$6: Short = $c.$pcstack.apply($c.$pcstackptr);
            $c.$pcstack.update($c.$pcstackptr, null.asInstanceOf[Short]);
            fresh$macro$6
          };
          {
            $c.$costackptr_=($c.$costackptr.-(1));
            val fresh$macro$7: org.coroutines.Coroutine[Int,AnyRef] = $c.$costack.apply($c.$costackptr);
            $c.$costack.update($c.$costackptr, null.asInstanceOf[org.coroutines.Coroutine[Int,AnyRef]]);
            fresh$macro$7
          };
          {
            {
              $c.$refstackptr = $c.$refstackptr - 1
              val fresh$macro$8: AnyRef = $c.$refstack.apply($c.$refstackptr);
              $c.$refstack.update($c.$refstackptr, null.asInstanceOf[AnyRef]);
              fresh$macro$8
            };
            ()
          }
        };

        def $enter(c: org.coroutines.Coroutine.Instance[Int,AnyRef]): Unit = {

          val pc: Short = c.$pcstack.apply(c.$pcstackptr.-(1));
          if (pc.==(0))
            this.$ep0(c)
          else
            this.$ep1(c)
        };

        override def $ep0(fresh$macro$2: org.coroutines.Coroutine.Instance[Int,AnyRef]): Unit =
          try {
            {
              val v: Int = -559038737;
              ();
              {
                {
                  val fresh$macro$9: Short = fresh$macro$2.$pcstack.apply(fresh$macro$2.$pcstackptr.-(1));
                  fresh$macro$2.$pcstack.update(fresh$macro$2.$pcstackptr.-(1), 1L.toShort);
                  fresh$macro$9
                };
                this.$assignyield(fresh$macro$2, v);
                return ()
              }
            }
          } catch {
            case (t @ (_: Throwable)) => {
              fresh$macro$2.$exception_=(t);
              this.$pop(fresh$macro$2);
              if (fresh$macro$2.$costackptr.<=(0).unary_!)
                fresh$macro$2.$target_=(fresh$macro$2)
              else
                ()
            }
          };

        override def $ep1(fresh$macro$2: org.coroutines.Coroutine.Instance[Int,AnyRef]): Unit = try {
          {
            val x: AnyRef = fresh$macro$2.$refstack.apply(fresh$macro$2.$refstackptr.-(1).-(0)).asInstanceOf[AnyRef];
            {
              ();
              {
                this.$pop(fresh$macro$2);
                if (fresh$macro$2.$costackptr.<=(0))
                  this.$assignresult(fresh$macro$2, x)
                else
                {
                  fresh$macro$2.$target_=(fresh$macro$2);
                  fresh$macro$2.$costack.apply(fresh$macro$2.$costackptr.-(1)).$returnvalue$L(fresh$macro$2, x)
                };
                return ()
              }
            }
          }
        } catch {
          case (t @ (_: Throwable)) => {
            fresh$macro$2.$exception_=(t);
            this.$pop(fresh$macro$2);
            if (fresh$macro$2.$costackptr.<=(0).unary_!)
              fresh$macro$2.$target_=(fresh$macro$2)
            else
              ()
          }
        };
        def $returnvalue$Z(c: org.coroutines.Coroutine.Instance[Int,AnyRef], v: Boolean): Unit = ();
        def $returnvalue$B(c: org.coroutines.Coroutine.Instance[Int,AnyRef], v: Byte): Unit = ();
        def $returnvalue$S(c: org.coroutines.Coroutine.Instance[Int,AnyRef], v: Short): Unit = ();
        def $returnvalue$C(c: org.coroutines.Coroutine.Instance[Int,AnyRef], v: Char): Unit = ();
        def $returnvalue$I(c: org.coroutines.Coroutine.Instance[Int,AnyRef], v: Int): Unit = ();
        def $returnvalue$F(c: org.coroutines.Coroutine.Instance[Int,AnyRef], v: Float): Unit = ();
        def $returnvalue$J(c: org.coroutines.Coroutine.Instance[Int,AnyRef], v: Long): Unit = ();
        def $returnvalue$D(c: org.coroutines.Coroutine.Instance[Int,AnyRef], v: Double): Unit = ();
        def $returnvalue$L(c: org.coroutines.Coroutine.Instance[Int,AnyRef], v: Any): Unit = ()
      };

      new Anon()
    }

    val c: org.coroutines.Coroutine.Instance[Int,AnyRef] = rube.$call("asdfsdf");
    ()
  }
}