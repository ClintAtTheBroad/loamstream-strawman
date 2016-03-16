package loamstream

import java.nio.file.Path
import loamstream.vcf.VcfParser
import cats.free.Free
import scala.reflect.ClassTag
import loamstream.commandline.CommandTemplate
import java.nio.file.Paths

/**
 * @author clint
 * date: Mar 11, 2016
 */
sealed trait PipelineOp[+A]

object PipelineOp {
  def fsPath(p: String): Pipeline[Path] = Free.liftF(FsPath(p))

  def fileFromClasspath(name: String): Pipeline[Path] = Free.liftF(FileFromClasspath(name))
  
  def getSamplesFromFile(path: Path): Pipeline[Pile.Set[String]] = Free.liftF(GetSamplesFromFile(path))
  
  def combineFiles(lhs: Path, rhs: Path): Pipeline[Path] = Free.liftF(CombineFiles(lhs, rhs))
  
  def compress(input: Path): Pipeline[Path] = Free.liftF(Compress(input))
  
  def runBurden(input: Path): Pipeline[Products.BurdenOutput] = Free.liftF(Burden(input))

  final case class FsPath(path: String) extends PipelineOp[Path]
  final case class FileFromClasspath(resourceName: String) extends PipelineOp[Path]
  final case class GetSamplesFromFile(path: Path) extends PipelineOp[Pile.Set[String]]
  final case class CombineFiles(lhs: Path, rhs: Path) extends PipelineOp[Path]
  final case class Compress(input: Path) extends PipelineOp[Path]
  final case class Burden(input: Path) extends PipelineOp[Products.BurdenOutput]
  
  object Products {
    final case class BurdenOutput(results: Path)
  }
}
