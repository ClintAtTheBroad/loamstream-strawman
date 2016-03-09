package loamstream

import java.io.File
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.nio.file.Path
import java.nio.file.Paths
import java.io.InputStream
import java.io.Reader
import java.io.InputStreamReader

object Sources {
  def path(pathName: String): Pipeline[Path] = Pipeline(Paths.get(pathName))
  
  def fileFromClasspath(resourceName: String): Pipeline[Path] = Pipeline {
    Paths.get(getClass.getResource(resourceName).getFile)
  }
  
  def fromClasspath(resourceName: String): Pipeline[InputStream] = Pipeline {
    getClass.getResourceAsStream(resourceName)
  }
  
  def fromClasspathAsReader(resourceName: String): Pipeline[Reader] = {
    fromClasspath(resourceName).map(new InputStreamReader(_))
  }
}