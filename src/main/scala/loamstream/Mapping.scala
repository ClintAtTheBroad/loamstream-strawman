package loamstream

import cats.~>
import cats.Id
import java.nio.file.Paths
import loamstream.vcf.VcfParser
import loamstream.vcf.VcfParser
import scala.concurrent.Future

/**
 * @author clint
 * date: Mar 11, 2016
 */
trait Mapping extends (PipelineOp ~> Id)

import scala.concurrent.ExecutionContext.Implicits.global

object Mapping {
  object Default extends Mapping {
    import PipelineOp._

    override def apply[A](op: PipelineOp[A]): A = op match {
      case FsPath(p)                => Future.successful(Paths.get(p))
      case FileFromClasspath(name)  => Future.successful(Paths.get(getClass.getClassLoader.getResource(name).getFile))
      case GetSamplesFromFile(path) => path.map(VcfParser(_).samples.toSet)
    }
  }
}