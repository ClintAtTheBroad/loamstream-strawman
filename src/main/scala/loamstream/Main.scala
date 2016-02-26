package loamstream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.implicitConversions
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object Main extends App {
  val source = Source.Literal(1)

  implicit def function1sToFuturesAreTransforms[A, B](f: A => Future[B]): Transform[A, B] = {
    Transform(f.toString) { f }
  }
  
  implicit def function1sAreTransforms[A, B](f: A => B)(implicit executor: ExecutionContext): Transform[A, B] = {
    Transform(f.toString) { a =>
      Future(f(a))
    }
  }
  
  val f: Int ~> Int = (_: Int) + 1
  
  val g: Int ~> Int = (_: Int) * 3
  
  val h: Int ~> Double = (_: Int).toDouble
  
  val pipeline = source.map(f).map(g).map(h)
  
  /*val pipeline1 = source.map { (_: Int) + 1 }*/
  
  /*val pipeline2 = for {
    i <- source
    plus1 <- f(i)
    times3 <- g(plus1)
    d <- h(times3)
  } yield d*/

  println(Await.result(pipeline.value, Duration.Inf))

  /*val source: Source[Int, String] = ???
  
  final case class Foo(s: String)
  
  val sink: Sink[Double, Foo] = ???
  
  //source >>> sink
  
  val f: (Int, String) => (Int, String) = (i, s) => (i + 1, s)
  
  val g: (Int, String) => (Int, String) = (i, s) => (i * 2, s)
  
  val h: (Int, String) => (Double, Foo) = (i, s) => (i.toDouble, Foo(s))
  
  val p0 = source >>> f
  
  val p1 = p0 >>> g
  
  //p1 >>> sink
  
  val s = source >>> f >>> g >>> h >>> sink*/

  /*import Scheduler.Implicits.Sync
  
  final case class Foo(s: String)
  
  val f: (Int, String) => (Int, String) = (i, s) => (i + 1, s)
  
  val g: (Int, String) => (Int, String) = (i, s) => (i * 2, s)
  
  val h: (Int, String) => (Double, Foo) = (i, s) => (i.toDouble, Foo(s))
  
  val source: Source[Int, String] = ???
  
  val sink: Sink[Pile[Double, Foo]] = ???
  
  val initial: Pipeline[Pile[Int, String]] = Pipeline.fromSource(source)
  
  val pipeline = source >>> f >>> g >>> h >>> sink
  
  pipeline.run()*/
}