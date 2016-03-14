package loamstream

/**
 * @author clint
 * date: Mar 11, 2016
 */
import scala.concurrent.Future
trait Runner {
  def run[A](pipeline: Pipeline[A]): Future[A]
}

object Runner {
  object Default extends Runner {
    override def run[A](f: Pipeline[A]): Future[A] = f.foldMap(Mapping.Default)
  }
}