package loamstream

import java.nio.file.Path
import loamstream.vcf.VcfParser
import cats.free.Free

/**
 * @author clint
 * date: Mar 11, 2016
 */
sealed trait PipelineOp[+A]

object PipelineOp {
  def fsPath(p: String): Pipeline[Path] = Free.liftF(FsPath(p))
  def fileFromClasspath(name: String): Pipeline[Path] = Free.liftF(FileFromClasspath(name))
  def parseVcf(path: Path): Pipeline[VcfParser] = Free.liftF(ParseVcf(path))
  def getSamples(vcf: VcfParser): Pipeline[Set[String]] = Free.liftF(GetSamples(vcf))

  case class FsPath(path: String) extends PipelineOp[Path]
  case class FileFromClasspath(resourceName: String) extends PipelineOp[Path]
  case class ParseVcf(path: Path) extends PipelineOp[VcfParser]
  case class GetSamples(vcf: VcfParser) extends PipelineOp[Set[String]]
}
