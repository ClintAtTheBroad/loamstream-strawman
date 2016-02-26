package loamstream

import scala.util.Try

trait Subscription {
  def cancel(): Unit
}

object Subscription {
  private final class HelperSubscription(c: () => Unit) extends Subscription {
    override def cancel(): Unit = c()
  }

  def apply(cancel: () => Unit): Subscription = new HelperSubscription(cancel)
  
  def apply(subs: Subscription*): Subscription = new HelperSubscription( () =>
    subs.foreach(s => Try(s.cancel()))
  )
}