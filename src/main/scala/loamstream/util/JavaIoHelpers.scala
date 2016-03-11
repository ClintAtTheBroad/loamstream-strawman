package loamstream.util

import java.io.BufferedReader

object JavaIoHelpers {
  final implicit class BufferedReadersAreIterable(val buf: BufferedReader) extends AnyVal {
    def toIterable: Iterable[String] = new Iterable[String] {
      import scala.collection.JavaConverters._
      
      override def iterator: Iterator[String] = buf.lines.iterator.asScala
    }
  }
}