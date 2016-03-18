package loamstream

import java.nio.file.Path
import loamstream.vcf.VcfParser
import cats.free.Free
import scala.reflect.ClassTag
import java.nio.file.Paths
import loamstream.commands.CommandDescription
import loamstream.config.LoamConfig
import scala.sys.process.ProcessBuilder

/**
 * @author clint
 * date: Mar 11, 2016
 */
sealed trait PipelineOp[+A]

object PipelineOp {
  def locate(path: String): Pipeline[Path] = {
    val classpathPrefix = "classpath:"
    
    if(path.startsWith(classpathPrefix)) {
      val classpathFile = path.drop(classpathPrefix.size)
      
      fileFromClasspath(classpathFile) 
    }
    else { fsPath(path) }
  }
  
  def fsPath(p: String): Pipeline[Path] = Free.liftF(FsPath(p))

  def fileFromClasspath(name: String): Pipeline[Path] = Free.liftF(FileFromClasspath(name))
  
  def getSamplesFromFile(path: Path): Pipeline[Pile.Set[String]] = Free.liftF(GetSamplesFromFile(path))

  //TODO: Handle missing commands
  //TODO: Fail now on missing commands, or later, when pipeline is run??
  def buildCommand(name: String)(params: Any*): Pipeline[ProcessBuilder] = Free.liftF(BuildCommand(Invocation(name, params.toSeq)))
  
  def buildCommand(invocation: Invocation): Pipeline[ProcessBuilder] = Free.liftF(BuildCommand(invocation))

  def runCommand(name: String)(params: Any*): Pipeline[Int] = Free.liftF(RunCommand(Invocation(name, params.toSeq)))
  
  def runCommand[A](name: String, expectation: Expectation[A])(params: Any*): Pipeline[A] = {
    for {
      result <- expectation.toPipeline(Invocation(name, params.toSeq))
    } yield result
  }
  
  final case class FsPath(path: String) extends PipelineOp[Path]
  final case class FileFromClasspath(resourceName: String) extends PipelineOp[Path]
  final case class GetSamplesFromFile(path: Path) extends PipelineOp[Pile.Set[String]]
  
  final case class Invocation(name: String, params: Seq[Any])
  
  final case class RunCommand(i: Invocation) extends PipelineOp[Int]
  
  final case class BuildCommand(i: Invocation) extends PipelineOp[ProcessBuilder]
  
  object Products {
    final case class BurdenOutput(results: Path)
  }
}
