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
import loamstream.commands.CommandType

/**
 * @author clint
 * date: Mar 11, 2016
 */
trait Mapping extends (PipelineOp ~> Id)

object Mapping {
  import PipelineOp._

  def fromConfig(config: Config): Try[Mapping] = LoamConfig.fromConfig(config).map(FromConfig(_))

  def fromLoamConfig(loamConfig: LoamConfig): FromConfig = FromConfig(loamConfig)

  final case class FromConfig(config: LoamConfig) extends Mapping {
    //TODO: Fail loudly here, or at another time?
    override def apply[A](op: PipelineOp[A]): Id[A] = op match {
      case Literal(a)               => a()
      case FsPath(p)                => Paths.get(p)
      case FileFromClasspath(name)  => Paths.get(getClass.getClassLoader.getResource(name).getFile)
      case GetSamplesFromFile(path) => Pile.Set.from(VcfParser(path).samples.toSet)
      case BuildCommand(invocation) => resolve(invocation).processBuilder
      case RunCommand(invocation)   => {
        val resolved = resolve(invocation)
        
        val exitStatus = run(resolved.processBuilder)
        
        CommandResult(resolved.output, exitStatus)
      }
    }
    
    private final case class ResolvedInvocation(processBuilder: ProcessBuilder, output: Option[Path])
    
    private def file(name: String) = new java.io.File(name)
    
    private def resolve(invocation: Invocation): ResolvedInvocation = {
      //TODO: Fail loudly here, or at another time?
      val desc = config.commands(invocation.name)

      val commandLine = desc.commandString(invocation.params: _*)

      import scala.sys.process._
      
      //TODO: Fix, super-fragile
      val builder = desc.commandType match {
        case CommandType.Simple => {
          commandLine #> desc.produces.get.toFile
        }
        case CommandType.Transform => {
          //TODO: XXX: HACK
          val isSingleQuote = (c: Char) => c == '\'' 

          val munged = commandLine.drop("perl -e".size).trim.dropWhile(isSingleQuote).reverse.dropWhile(isSingleQuote).reverse
          
          Seq("perl", "-e", munged) #< file(invocation.params.head.toString) #> desc.produces.get.toFile
        }
      }
      
      ResolvedInvocation(builder, desc.produces)
    }
    
    private def run(builder: ProcessBuilder): Int = builder.!
  }
}