package loamstream.vcf

import scala.util.Try
import loamstream.util.Tries

//##FORMAT=<ID=ID,Number=number,Type=type,Description=”description”>
//TODO: Is the number part optional, as with Infos?
final case class Format(id: Id, number: Option[Int], tpe: Type, description: String)

object Format {
  val allowedTypes: Set[Type] = Set(Type.Integer, Type.Float, Type.Character, Type.String)
  
  //FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
  private val formatRegex = {
    "FORMAT\\=<ID\\=(.+?),Number=(.+?),Type=(.+?),Description=\"(.+?)\">".r
  }
  
  def fromString(line: String): Try[Format] = {
    val munged = line.trim.dropWhile(_ == '#')
    
    def failure = Tries.failure(s"Could't parse line as a Filter: '$line'")
    
    //TODO: Fail on mismatched type and number, say non-zero for 'Flag'?
    munged match {
      case formatRegex(id, n, t, desc) => {
        for {
          tpe <- Type.fromString(t).filter(allowedTypes.contains)
          num = Try(n.toInt).toOption
        } yield {
          Format(Id(id), num, tpe, desc)
        }
      }
      case _ => failure
    }
  }
}
