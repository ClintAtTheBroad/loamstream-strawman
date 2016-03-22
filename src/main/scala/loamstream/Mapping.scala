package loamstream

import java.nio.file.Path

import java.nio.file.Paths

import scala.sys.process.ProcessBuilder
import scala.util.Try

import com.typesafe.config.Config

import cats.{ Id, ~> }
import loamstream.commands.CommandResult
import loamstream.commands.CommandType
import loamstream.commands.Invocation
import loamstream.config.LoamConfig
import loamstream.vcf.VcfParser

/**
 * @author clint
 * date: Mar 11, 2016
 */
trait Mapping extends (PipelineStep ~> Id)

object Mapping {
  import PipelineStep._

  def fromConfig(config: Config): Try[Mapping] = LoamConfig.fromConfig(config).map(FromConfig(_))

  def fromLoamConfig(loamConfig: LoamConfig): FromConfig = FromConfig(loamConfig)

  final case class FromConfig(config: LoamConfig) extends Mapping {
    //TODO: Fail loudly here, or at another time?
    override def apply[A](op: PipelineStep[A]): Id[A] = op match {
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
      import scala.sys.process._
      //TODO: Fail loudly here, or at another time?
      val desc = config.commands(invocation.name)

      val commandLine = desc.commandString(invocation.params: _*)
      
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