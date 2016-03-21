package loamstream

import loamstream.config.LoamConfig
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import cats.~>
import cats.free.Free
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.nio.file.Paths

/**
 * @author clint
 * date: Mar 18, 2016
 */
object Pipeline extends App {
  //TODO: CBF Magic?
  //NB: Dumb, proof-of-concept version; maybe should return Pipeline[Seq[A]] ?
  def parallelize[A](pas: Seq[Pipeline[A]], mapping: Mapping)(implicit executor: ExecutionContext): Pipeline[Future[Seq[A]]] = {
    PipelineOp.literal {
      val futures = pas.map { pipeline =>
        Future(pipeline.runWith(mapping))
      }
      
      Future.sequence(futures)
    }
  }

  //TODO: CBF Magic?
  //Assemble a sequence of pipelines into one long pipeline that executes each step in order
  def sequence[A](pas: Seq[Pipeline[A]]): Pipeline[Seq[A]] = {
    //Use a vector for faster appending
    val z: Pipeline[Seq[A]] = PipelineOp.literal(Vector.empty)

    pas.foldLeft(z) { (acc, pa) =>
      for {
        as <- acc
        a <- pa
      } yield as :+ a
    }
  }
}