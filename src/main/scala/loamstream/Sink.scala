package loamstream

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait Sink[P] {
  def accept(pile: P)(implicit scheduler: Scheduler): Unit
  
  //final def callback(implicit emitter: Scheduler): A => Any = accept(_)
  
  /*final def <<[B <: A](other: Observable[B])(implicit scheduler: Scheduler): Subscription = {
    other.foreach(this.accept)
  }
  
  final def <<[B <: A](v: Variable[B])(implicit scheduler: Scheduler): Subscription = {
     v.valueOption.foreach(accept)
      
     v.foreach(accept)
  }*/
  
  /*final def connectFrom[B <: A](other: Observable[B])(implicit scheduler: Scheduler): Subscription = this << other
  
  final def connectFrom[B <: A](other: Variable[B])(implicit scheduler: Scheduler): Subscription = this << other*/
}

object Sink {
  /*implicit def function1sAreSinks[A, B](f: Pile[A, B] => Any)(implicit scheduler: Scheduler): Sink[A, B] = new Sink[A, B] {
    override def accept(pile: Pile[A, B])(implicit scheduler: Scheduler): Unit = f(pile)
  }*/
}