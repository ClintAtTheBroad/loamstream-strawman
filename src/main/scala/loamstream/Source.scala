package loamstream

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait Source[A] {
  def value(implicit executor: ExecutionContext): Future[A]
  
  def map[B](f: A ~> B)(implicit executor: ExecutionContext): Pipeline[B] = Pipeline.from(this).map(f)
  
  def flatMap[B](f: A ~> Pipeline[B])(implicit executor: ExecutionContext): Pipeline[B] = Pipeline.from(this).flatMap(f)
}

object Source {

  def of[A](a: A): Source[A] = Literal(a)
  
  def apply[A](f: () => Future[A]): Source[A] = new Source[A] {
    override def value(implicit executor: ExecutionContext): Future[A] = f()
  }

  final case class Literal[A](a: A) extends Source[A] {
    override def value(implicit executor: ExecutionContext): Future[A] = Future.successful(a)
  }

  final case class Composite[A, B](as: Source[A], bs: Source[B]) extends Source[(A, B)] {
    override def value(implicit executor: ExecutionContext): Future[(A, B)] = {
      for {
        a <- as.value
        b <- bs.value
      } yield (a, b)
    }
  }
}