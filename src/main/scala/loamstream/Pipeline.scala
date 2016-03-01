package loamstream

import scala.reflect.ClassTag
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.concurrent.Await

final case class Pipeline[A](upstream: Source[A]) extends Source[A] {
  override def toString = s"Pipeline($upstream)"
  
  override def value: Future[A] = upstream.value

  def run(maxWaitTime: Duration = Duration.Inf): A = Await.result(value, maxWaitTime)
  
  override def map[B](f: A ~> B)(implicit executor: ExecutionContext): Pipeline[B] = Pipeline {
    for {
      a <- value
    } yield f(a)
  }
  
  override def flatMap[B](f: A ~> Pipeline[B])(implicit executor: ExecutionContext): Pipeline[B] = Pipeline {
    for {
      a <- value
      p = f(a)
      b <- p.value
    } yield b
  }
}

object Pipeline {
  def from[A](source: Source[A]): Pipeline[A] = Pipeline(source)
  
  def apply[A](f: Future[A]): Pipeline[A] = Pipeline(Source(f))
}
