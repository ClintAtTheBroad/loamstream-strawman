
import cats.free.Free

/**
 * @author clint
 * date: Mar 11, 2016
 */
package object loamstream {
  type Pipeline[A] = Free[PipelineOp, A]
  
  implicit final class PipelineOps[A](val pipeline: Pipeline[A]) extends AnyVal {
    def runWith(mapping: Mapping): A = {
      Runner.fromMapping(mapping).run(pipeline)
    }
    
    def zip[B](other: Pipeline[B]): Pipeline[(A, B)] = {
      for {
        a <- pipeline
        b <- other
      } yield (a, b)
    }
  }
}