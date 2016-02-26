package loamstream

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

trait Upstream[A] extends Subscription {
  private[this] var _downstream: Set[Downstream[A]] = Set.empty
  
  private[this] val lock = new AnyRef
  
  private[loamstream] def downstream: Set[Downstream[A]] = lock.synchronized(_downstream)

  override def cancel(): Unit = updateDownstream(Set.empty)
  
  def noLongerObservedBy(o: Downstream[A]): Unit = updateDownstream(downstream - o)
  
  def observedBy(o: Downstream[A]): Subscription = {
    updateDownstream(downstream + o)
    
    Subscription(() => noLongerObservedBy(o))
  }
  
  def foreach(f: A => Any)(implicit scheduler: Scheduler): Subscription = this.observedBy(f)
  
  def publish(a: A)(implicit scheduler: Scheduler): Unit = {
    downstream.foreach { observer =>
      scheduler.run { () =>
        observer.receive(a)
      }
    }
  }
  
  final def connectTo(sink: Sink[A])(implicit scheduler: Scheduler): Unit = this.foreach(sink.accept)
  
  final def >>(sink: Sink[A])(implicit scheduler: Scheduler): Unit = connectTo(sink)
  
  private def updateDownstream(newObservers: Set[Downstream[A]]): Unit = {
    lock.synchronized {
      _downstream = newObservers
    }
  }
}

object Upstream {
  implicit def futuresAreUpstream[A](f: Future[A])(implicit executor: ExecutionContext, scheduler: Scheduler): Upstream[A] = {
    Variable.fromFuture(f)
  }
  
  implicit final class FutureOps[A](val f: Future[A]) extends AnyVal {
    def toObservable(implicit executor: ExecutionContext, scheduler: Scheduler): Upstream[A] = {
      Variable.fromFuture(f)
    }
  }
  
  def multiplex(observables: Upstream[_]*)(implicit emitter: Scheduler): Upstream[_] = {
    val result = Var[Any]()
    
    for {
      observable <- observables
      ev <- observable
    } {
      result.accept(ev)
    }
    
    result
  }
}