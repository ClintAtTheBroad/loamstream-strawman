package loamstream

import java.nio.file.Path
import scala.concurrent.Future

/**
 * @author clint
 * date: Mar 14, 2016
 */
object Pipelines {
  import PipelineOp._
  
  def samplesFrom(pathPipeline: Pipeline[Path]): Pipeline[Set[String]] = {
    for {
      path <- pathPipeline
      samples <- getSamplesFromFile(path)
    } yield samples
  }
  
  def samplesFromFile(fileName: String): Pipeline[Set[String]] = samplesFrom(fsPath(fileName))
  
  def samplesFromClasspathFile(fileName: String): Pipeline[Set[String]] = samplesFrom(fileFromClasspath(fileName))
}