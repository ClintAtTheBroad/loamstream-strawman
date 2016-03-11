package loamstream

import cats.~>
import cats.Id
import java.nio.file.Paths
import loamstream.vcf.VcfParser

/**
 * @author clint
 * date: Mar 11, 2016
 */
trait Mapping extends (PipelineOp ~> Id)

object Mapping {
  object Default extends Mapping {
    import PipelineOp._

    override def apply[A](op: PipelineOp[A]): Id[A] = op match {
      case FsPath(p)               => Paths.get(p)
      case FileFromClasspath(name) => Paths.get(getClass.getClassLoader.getResource(name).getFile)
      case ParseVcf(path)          => VcfParser(path)
      case GetSamples(vcf)         => vcf.samples.toSet
    }
  }
}