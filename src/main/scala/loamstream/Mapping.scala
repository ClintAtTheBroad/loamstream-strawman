package loamstream

import java.nio.file.Paths

import scala.reflect.ClassTag

import cats.{ Id, ~> }
import loamstream.vcf.VcfParser
import java.nio.file.Files
import java.io.File
import com.typesafe.config.Config
import java.nio.file.Path

/**
 * @author clint
 * date: Mar 11, 2016
 */
trait Mapping extends (PipelineOp ~> Id) {
  
}

object Mapping {
  import PipelineOp._
  
  final case class FromConfig(config: Config) extends Mapping {
     
    override def apply[A](op: PipelineOp[A]): Id[A] = op match {
      case FsPath(p)                    => Paths.get(p)
      case FileFromClasspath(name)      => Paths.get(getClass.getClassLoader.getResource(name).getFile)
      case GetSamplesFromFile(path)     => Pile.Set.from(VcfParser(path).samples.toSet)
      case CombineFiles(lhs, rhs) => {
        val (dest, _) = withTempFile(dest => run(combineFilesCommand(lhs, rhs, dest)))
        
        dest
      }
      case Compress(input) => {
        val (dest, _) = withTempFile(dest => run(compressCommand(input, dest)))
        
        dest
      }
      case Burden(input) => {
        val (_, output) = withTempFile(PipelineOp.Products.BurdenOutput(_))
        
        output
      }
    }

    private def run(command: String): Int = {
      import sys.process._
      
      command.!
    }
    
    private def withTempFile[A](f: Path => A): (Path, A) = {
      val file = tempFile
      
      (file, f(file))
    }
    
    private def tempFile: Path = File.createTempFile("loamstream", "combine-vcfs").toPath
    
    private def combineFilesCommand(lhs: Path, rhs: Path, dest: Path): String = {
      val gatkJarPath = Paths.get(config.getString("loamstream.commands.combine.gatkJarPath"))
      
      val fastaFile = Paths.get(config.getString("loamstream.commands.combine.fastaFilePath"))
      
      val variantFilesChunk = s"--variant $lhs --variant $rhs"
      
      s"java -jar $gatkJarPath -T CombineVariants -R $fastaFile $variantFilesChunk -o $dest -genotypeMergeOptions UNIQUIFY"
    }
    
    private def compressCommand(input: Path, output: Path): String = {
      //TODO
      
      s"foghorn -l error -f DOS -s 0 -t vnc -i $input -o $output"
    }
  }
  
  object Default extends Mapping {
    override def apply[A](op: PipelineOp[A]): Id[A] = op match {
      case FsPath(p)                    => Paths.get(p)
      case FileFromClasspath(name)      => Paths.get(getClass.getClassLoader.getResource(name).getFile)
      case GetSamplesFromFile(path)     => Pile.Set.from(VcfParser(path).samples.toSet)
      case CombineFiles(lhs, rhs) => {
        val destFile = File.createTempFile("loamstream", "combine-vcfs")
        
        destFile.toPath
      }
      case Compress(path) => {
        val destFile = File.createTempFile("loamstream", "combine-vcfs")
        
        destFile.toPath
      }
      case Burden(input) => {
        val resultsFile = File.createTempFile("loamstream", "combine-vcfs")
        
        PipelineOp.Products.BurdenOutput(resultsFile.toPath)
      }
    }
  }

  def emptyMap[K, V](kt: ClassTag[K], vt: ClassTag[V]): Map[K, V] = Map.empty
}