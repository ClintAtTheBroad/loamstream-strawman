package loamstream

import java.nio.file.Path
import scala.reflect.ClassTag

/**
 * @author clint
 * date: Mar 14, 2016
 */
object Pipelines {
  import PipelineStep._
  
  def getSampleIdsFrom(pathPipeline: Pipeline[Path]): Pipeline[Pile.Set[String]] = {
    for {
      path <- pathPipeline
      samples <- getSamplesFromFile(path)
    } yield samples
  }
  
  def getSamplesIdsFromFile(fileName: String): Pipeline[Pile.Set[String]] = getSampleIdsFrom(fsPath(fileName))
  
  def getSamplesIdsFromClasspathFile(fileName: String): Pipeline[Pile.Set[String]] = getSampleIdsFrom(fileFromClasspath(fileName))
}