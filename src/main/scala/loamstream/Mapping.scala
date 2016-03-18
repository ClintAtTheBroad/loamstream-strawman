package loamstream

import java.nio.file.Paths

import scala.reflect.ClassTag

import cats.{ Id, ~> }
import loamstream.vcf.VcfParser
import java.nio.file.Files
import java.io.File
import com.typesafe.config.Config
import java.nio.file.Path
import loamstream.config.LoamConfig
import scala.util.Try
import scala.sys.process.Process
import scala.sys.process.ProcessBuilder

/**
 * @author clint
 * date: Mar 11, 2016
 */
trait Mapping extends (PipelineOp ~> Id) {

}

object Mapping {
  import PipelineOp._

  def fromConfig(config: Config): Try[Mapping] = LoamConfig.fromConfig(config).map(FromConfig(_))

  def fromLoamConfig(loamConfig: LoamConfig): FromConfig = FromConfig(loamConfig)

  final case class FromConfig(config: LoamConfig) extends Mapping {

    override def apply[A](op: PipelineOp[A]): Id[A] = op match {
      case FsPath(p)                => Paths.get(p)
      case FileFromClasspath(name)  => Paths.get(getClass.getClassLoader.getResource(name).getFile)
      case GetSamplesFromFile(path) => Pile.Set.from(VcfParser(path).samples.toSet)
      case BuildCommand(invocation) => builderFrom(invocation)
      case RunCommand(invocation) => builderFrom(invocation).!
    }
    
    private def builderFrom(invocation: Invocation): ProcessBuilder = {
      //TODO: Fail loudly here, or at another time?
      val desc = config.commands(invocation.name)

      val commandLine = desc.commandString(invocation.params: _*)

      Process(commandLine)
    }
  }
}