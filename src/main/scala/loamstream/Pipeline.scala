package loamstream

import scala.reflect.ClassTag
import scala.util.Try

final class Pipeline[P] extends PileStream[P] {

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

  /*override def partition(p: P => Boolean)(implicit emitter: Scheduler): (Self[P], Self[P]) = {
    val lhs = makeEmpty[P]
    val rhs = makeEmpty[P]

    this.foreach { event =>
      val dest = if (p(event)) lhs else rhs

      dest.accept(event)
    }

    (lhs, rhs)
  }

  override def drop(howMany: Int)(implicit emitter: Scheduler): Self[P] = {
    if (howMany <= 0) { asSelf }
    else {
      @volatile var leftToSkip: Int = howMany

      val lock = new AnyRef

      produce[P] { result =>
        foreach { event =>
          lock.synchronized {
            if (leftToSkip <= 0) {
              result.accept(event)
            }

            leftToSkip -= 1
          }
        }
      }
    }
  }

  override def take(howMany: Int)(implicit scheduler: Scheduler): Self[P] = {
    if (howMany <= 0) { makeEmpty }
    else {
      @volatile var taken: Int = 0

      val lock = new AnyRef

      produce[P] { result =>
        foreach { event =>
          var done = lock.synchronized {
            if (taken < howMany) {
              result.accept(event)
            }

            taken += 1
            
            taken >= howMany
          }
          
          if(done) {
            result.cancel()
          }
        }
      }
    }
  }*/

  private def produce[E](body: Self[E] => Any): Self[E] = {
    val result = makeEmpty[E]

    body(result)

    result
  }
}

object Pipeline extends HasEmpty[Pipeline] {
  def empty[P]: Pipeline[P] = ???
  
  def fromSource[K, V](source: Source[K, V]): Pipeline[Pile[K, V]] = ???
}