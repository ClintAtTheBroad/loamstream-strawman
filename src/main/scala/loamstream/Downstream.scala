package loamstream

import scala.language.implicitConversions

trait Downstream[A] {
  def receive(a: A): Unit
  
  def stopReceiving(o: Upstream[A]): Unit = o.noLongerObservedBy(this)
}

object Downstream {
  def apply[A](f: A => Any)(implicit scheduler: Scheduler): Downstream[A] = function1sAreObservers(f)
  
  implicit def function1sAreObservers[A](f: A => Any)(implicit scheduler: Scheduler): Downstream[A] = new Downstream[A] {
    override def receive(e: A): Unit = scheduler.run(() => f(e))
  }
}