package loamstream

import scala.reflect.ClassTag
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

final case class Pipeline[A](upstream: Source[A]) extends Source[A] {
  override def value(implicit executor: ExecutionContext): Future[A] = upstream.value

  override def map[B](f: A ~> B)(implicit executor: ExecutionContext): Pipeline[B] = Pipeline {
    Source { () =>
      for {
        a <- value
        b <- f(a)
      } yield b
    }
  }
  
  override def flatMap[B](f: A ~> Pipeline[B])(implicit executor: ExecutionContext): Pipeline[B] = Pipeline {
    Source { () =>
      for {
        a <- value
        src <- f(a)
        b <- src.value
      } yield b
    }
  }
}

object Pipeline {
  def from[A](source: Source[A]): Pipeline[A] = Pipeline(source)
}

/*final class Pipeline[P] extends PileStream[P] {

  def run()(implicit scheduler: Scheduler): Unit = ???
  
  override type Self[X] = Pipeline[X]
  
  def makeEmpty[E]: Self[E] = Pipeline.empty

  override protected def asSelf: Self[P] = this
  
  override def accept(a: P)(implicit scheduler: Scheduler): Unit = publish(a)
  
  override def map[Q](f: P => Q)(implicit scheduler: Scheduler): Self[Q] = {
    produce { result =>
      this.foreach { a =>
        result.accept(f(a))
      }
    }
  }

  override def flatMap[Q](f: P => PileStream[Q])(implicit scheduler: Scheduler): Self[Q] = {
    produce { result =>
      for {
        a <- this
        b <- f(a)
      } {
        result.accept(b)
      }
    }
  }

  override def filter(p: P => Boolean)(implicit scheduler: Scheduler): Self[P] = {
    produce { result =>
      foreach { a =>
        if(p(a)) {
          result.accept(a)
        }
      }
    }
  }

  override def collect[Q](f: PartialFunction[P, Q])(implicit emitter: Scheduler): Self[Q] = {
    produce { result =>
      for {
        event <- this
        if f.isDefinedAt(event)
      } {
        result.accept(f(event))
      }
    }
  }

  override def collectMulti[Q](f: PartialFunction[P, PileStream[Q]])(implicit scheduler: Scheduler): Self[Q] = {
    produce { result =>
      for {
        event <- this
        if f.isDefinedAt(event)
        b <- f(event)
      } {
        result.accept(b)
      }
    }
  }

  override def ++(other: PileStream[P])(implicit scheduler: Scheduler): Self[P] = {
    produce { result =>
      this >> result
      other >> result
    }
  }

  private def produce[E](body: Self[E] => Any): Self[E] = {
    val result = makeEmpty[E]

    body(result)

    result
  }
}

object Pipeline extends HasEmpty[Pipeline] {
  def empty[P]: Pipeline[P] = ???
  
  def fromSource[K, V](source: Source[K, V]): Pipeline[Pile[K, V]] = ???
}*/