package loamstream

import java.io.File
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.nio.file.Path
import java.nio.file.Paths

object Sources {
  def path(pathName: String): Source[Path] = Source.of(Paths.get(pathName))
}