package loamstream

import java.nio.file.Path
import loamstream.vcf.VcfParser
import cats.free.Free
import scala.concurrent.Future

/**
 * @author clint
 * date: Mar 11, 2016
 */
sealed trait PipelineOp[+A]

object PipelineOp {
  def fsPath(p: String): Pipeline[Path] = Free.liftF(FsPath(p))
  def fileFromClasspath(name: String): Pipeline[Path] = Free.liftF(FileFromClasspath(name))
  /*def parseVcf(path: Path): Pipeline[VcfParser] = Free.liftF(ParseVcf(path))
  def getSamples(vcf: VcfParser): Pipeline[Set[String]] = Free.liftF(GetSamples(vcf))*/
  def getSamplesFromFile(path: Future[Path]): Pipeline[Set[String]] = Free.liftF(GetSamplesFromFile(path))

  final case class FsPath(path: String) extends PipelineOp[Future[Path]]
  final case class FileFromClasspath(resourceName: String) extends PipelineOp[Future[Path]]
  /*final case class ParseVcf(path: Path) extends PipelineOp[Future[VcfParser]]
  final case class GetSamples(vcf: VcfParser) extends PipelineOp[Future[Set[String]]]*/
  final case class GetSamplesFromFile(path: Future[Path]) extends PipelineOp[Future[Set[String]]]
}
