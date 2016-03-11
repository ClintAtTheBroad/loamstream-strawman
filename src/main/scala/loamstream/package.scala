
import cats.free.Free

/**
 * @author clint
 * date: Mar 11, 2016
 */
package object loamstream {
  type Pipeline[A] = Free[PipelineOp, A]
}