
import cats.free.Free
import scala.concurrent.Future

/**
 * @author clint
 * date: Mar 11, 2016
 */
package object loamstream {
  type Pipeline[A] = Free[PipelineOp, Future[A]]
}