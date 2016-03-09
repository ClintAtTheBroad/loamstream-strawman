package loamstream

import java.io.File
import java.nio.file.Path
import scala.util.Try
import loamstream.model.SampleId
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import loamstream.util.Tries
import java.nio.file.Paths
import loamstream.vcf.VcfParser
import loamstream.vcf.VcfParser

object Transforms {
  lazy val toVcfParser: Path ~> VcfParser = Transform("Parse VCF") { path =>
    VcfParser(path)
  }

  lazy val getSamples: VcfParser ~> Set[SampleId] = Transform("Extract Sample Ids") { vcfFile =>
    vcfFile.samples.map(SampleId(_)).toSet
  }

  def firstLines(howMany: Int): Path ~> Seq[String] = unixCommand((path: Path) => s"head -n $howMany $path")
  
  def lastLines(howMany: Int): Path ~> Seq[String] = unixCommand((path: Path) => s"tail -n $howMany $path")

  def foghorn: Path ~> Seq[String] = pipedUnixCommand { (inputFile, outputFile) => 
    s"foghorn -l error -f DOS -s 0 -t vnc -i $inputFile -o $outputFile"
  }

  def unixCommand[A](makeCommand: A => String): A ~> Seq[String] = Transform("Unix Command (TODO)") { (a: A) =>
    import sys.process._
    
    makeCommand(a).lineStream
  }

  def pipedUnixCommand[A](makeCommand: (A, Path) => String): A ~> Seq[String] = Transform("Unix Command (TODO)") { (a: A) =>
    import sys.process._

    val pipe = makePipe()

    closeWhenFinished(pipe)(makeCommand(a, pipe.path).lineStream)
  }

  private[loamstream] def closeWhenFinished[A](pipe: Pipe)(stream: Stream[A]): Seq[A] = {
    val delegate = stream.toIterator

    (new Iterator[A] {
      override def hasNext: Boolean = delegate.hasNext
      override def next(): A = {
        val result = delegate.next()

        try { result } finally {
          if (!delegate.hasNext) {
            pipe.close()
          }
        }
      }
    }).toSeq
  }

  private final case class Pipe(path: Path, close: () => Unit)

  private[this] val lock = new AnyRef

  private def makePipe(): Pipe = {
    //TODO
    def makeName(): Path = Paths.get(s"/tmp/loamstream-pipe-${System.currentTimeMillis}${System.nanoTime}")

    def exisits(p: Path): Boolean = p.toFile.exists

    lock.synchronized {
      val path = Iterator.continually(makeName()).filter(exisits).next()

      import sys.process._

      "mkfifo $path".!!

      Pipe(path, () => "rm $path".!!)
    }
  }
}