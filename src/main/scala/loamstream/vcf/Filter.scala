package loamstream.vcf

import scala.util.Try
import loamstream.util.Tries
import scala.util.Success

//##FILTER=<ID=ID,Description=”description”>
final case class Filter(id: Id, description: String)

object Filter {
  //FILTER=<ID=q10,Description="Quality below 10">
  private val filterRegex = {
    "FILTER\\=<ID\\=(.+?),Description=\"(.+?)\">".r
  }
  
  def fromString(line: String): Try[Filter] = {
    val munged = line.trim.dropWhile(_ == '#')
    
    def failure = Tries.failure(s"Could't parse line as a Filter: '$line'")
    
    munged match {
      case filterRegex(id, desc) => Success(Filter(Id(id), desc))
      case _ => failure
    }
  }
}