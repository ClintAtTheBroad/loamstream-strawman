package loamstream

import PipelineOp._
import java.nio.file.Path

/**
 * @author clint
 * date: Mar 17, 2016
 */
sealed trait Expectation[A] {
  def toPipeline(invocation: Invocation): Pipeline[A]
}

object Expectation {
  //def producingLines(): Expectation[Stream[String]] = from(invocation => buildCommand(invocation).map(_.lineStream))
  def producingFile(name: String): Expectation[Path] = fromPipeline(fsPath(name))
  def producingDir(name: String): Expectation[Path] = fromPipeline(fsPath(name))

  private def fromPipeline[A](p: Pipeline[A]): Expectation[A] = new Expectation[A] {
    override def toPipeline(invocation: Invocation) = p
  }

  /*private def from[A](f: Invocation => Pipeline[A]): Expectation[A] = new Expectation[A] {
    override def toPipeline(invocation: Invocation) = f(invocation)
  }*/
}