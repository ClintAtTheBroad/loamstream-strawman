package loamstream

import loamstream.vcf.VcfFile
import java.io.File
import java.nio.file.Path
import scala.util.Try
import loamstream.model.SampleId
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import loamstream.util.Tries

object Transforms {
  def toVcfFile: Path ~> VcfFile = Transform("Parse VCF") { path =>
    import Tries._
    
    VcfFile.fromPath(path).toFuture
  }
  
  def getSamples: VcfFile ~> Set[SampleId] = Transform("Extract Sample Ids") { vcfFile =>
    Future.successful(vcfFile.sampleIds.toSet)
  }
  
  def takeLines(howMany: Int)(implicit executor: ExecutionContext): Path ~> Seq[String] = TakeLines(howMany)
  
  final case class TakeLines(howMany: Int)(implicit executor: ExecutionContext) extends (Path ~> Seq[String]) {
    override val id = s"Take $howMany lines"
    
    private def command(p: Path) = s"head -n $p"
    
    override def apply(p: Path): Future[Seq[String]] = {
      Future {
        import sys.process._
        
        val output = command(p).!!
        
        val s = scala.io.Source.fromIterable(output)
        
        try { s.getLines.toIndexedSeq }
        finally { s.close() }
      }
    }
  }
}