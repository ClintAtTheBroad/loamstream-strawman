package loamstream

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait Source[A] {
  def value: Future[A]
  
  def toPipeline: Pipeline[A] = Pipeline.from(this)
  
  def map[B](f: A ~> B)(implicit executor: ExecutionContext): Pipeline[B] = toPipeline.map(f)
  
  def flatMap[B](f: A ~> Pipeline[B])(implicit executor: ExecutionContext): Pipeline[B] = toPipeline.flatMap(f)
}

object Source {

  def of[A](a: A): Source[A] = Literal(a)
  
  def apply[A](f: () => Future[A]): Source[A] = new Source[A] {
    override def value: Future[A] = f()
  }

  final case class Literal[A](a: A) extends Source[A] {
    override def value: Future[A] = Future.successful(a)
  }

  final case class Composite[A, B](as: Source[A], bs: Source[B])(implicit executor: ExecutionContext) extends Source[(A, B)] {
    override def value: Future[(A, B)] = {
      val (futureA, futureB) = (as.value, bs.value)
      
      for {
        a <- futureA
        b <- futureB
      } yield (a, b)
    }
  }
  
  import scala.language.implicitConversions
  
  implicit def sourcesArePipelines[A](s: Source[A]): Pipeline[A] = s.toPipeline
}