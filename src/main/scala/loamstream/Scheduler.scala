package loamstream

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait Scheduler {
  def run(f: () => Any): Unit
}

object Scheduler { outer =>
  
  object Sync extends Scheduler {
    override def run(f: () => Any): Unit = f()
  }
  
  object Implicits {
    implicit val Sync = outer.Sync
  }
}