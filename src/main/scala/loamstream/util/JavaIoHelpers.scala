package loamstream.util

import java.io.BufferedReader

object JavaIoHelpers {
  final implicit class BufferedReadersAreIterable(val buf: BufferedReader) extends AnyVal {
    def toIterable: Iterable[String] = new Iterable[String] {
      override def iterator: Iterator[String] = {
        val delegate = buf.lines.iterator
        
        new Iterator[String] {
          override def hasNext: Boolean = delegate.hasNext
          
          override def next(): String = delegate.next
        }
      }
    }
  }
}