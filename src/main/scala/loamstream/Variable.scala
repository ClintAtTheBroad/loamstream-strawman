package loamstream

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait Variable[A] extends Upstream[A] with Sink[A] {
  def value: A
  
  def apply(): A = value
  
  def valueOption: Option[A]
  
  def set(a: A)(implicit scheduler: Scheduler): Unit
  
  def :=(a: A)(implicit scheduler: Scheduler): Unit = set(a)
  
  def modify(f: A => A)(implicit emitter: Scheduler): Unit = valueOption.foreach(v => set(f(v)))

  def modifyOrElse(default: => A)(f: A => A)(implicit emitter: Scheduler): Unit
  
  def toPipeline(implicit scheduler: Scheduler): Pipeline[A]
}

object Variable {
  def fromFuture[A](f: Future[A])(implicit executor: ExecutionContext, scheduler: Scheduler): Variable[A] = {
    val result: Var[A] = Var()
    
    f.foreach(result.accept)
    
    result
  }
}