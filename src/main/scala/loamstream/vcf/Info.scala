package loamstream.vcf

import scala.util.Try
import scala.util.Failure
import loamstream.util.Tries

//##INFO=<ID=ID,Number=number,Type=type,Description=”description”>
final case class Info(id: Id, number: Option[Int], tpe: Type, description: String)

object Info {
  val allowedTypes: Set[Type] = Set(Type.Integer, Type.Float, Type.Flag, Type.Character, Type.String)
  
  private val infoRegex = {
    "INFO\\=<ID\\=(.+?),Number=(.+?),Type=(.+?),Description=\"(.+?)\">".r
  }
  
  def fromString(line: String): Try[Info] = {
    val munged = line.trim.dropWhile(_ == '#')
    
    def failure = Tries.failure(s"Could't parse line as an Info: '$line'")
    
    //TODO: Fail on mismatched type and number, say non-zero for 'Flag'?
    munged match {
      case infoRegex(id, n, t, desc) => {
        for {
          tpe <- Type.fromString(t).filter(allowedTypes.contains)
          num = Try(n.toInt).toOption
        } yield {
          Info(Id(id), num, tpe, desc)
        }
      }
      case _ => failure
    }
  }
}