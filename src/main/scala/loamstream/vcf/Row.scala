package loamstream.vcf

import scala.util.Try
import loamstream.util.Tries
import scala.util.Success

/**
 * @author clint
 * date: Feb 4, 2016
 */
//NB: Hard-coded and opaque, for now
//#CHROM POS     ID        REF ALT    QUAL FILTER INFO                              FORMAT      NA00001        NA00002        NA00003
//20     14370   rs6054257 G      A       29   PASS   NS=3;DP=14;AF=0.5;DB;H2           GT:GQ:DP:HQ 0|0:48:1:51,51 1|0:48:8:51,51 1/1:43:5:.,.
final case class Row(
  chromosome: Option[Int],
  pos: Option[Int],
  id: Option[Id],
  ref: Option[String],
  alt: Option[String],
  qual: Option[String],
  filter: Option[Id],
  info: Option[Id],
  format: Option[Id],
  samples: Seq[String])
  
object Row {
  def fromString(line: String): Try[Row] = {
    val parts = line.trim.split("\\s+")
    
    parts match {
      case Array(chrom, pos, id, ref, alt, qual, filter, info, rest @ _*) => {
        def isPass(s: String): Boolean = s == "PASS"
        def isDot(s: String): Boolean = s == "."
        def isPassOrDot(s: String): Boolean = isPass(s) || isDot(s)
        
        def toOption(s: String): Option[String] = Option(s).filterNot(isPassOrDot)
        def toId(s: String): Option[Id] = toOption(s).map(Id(_))
        def toInt(s: String): Option[Int] = Try(s.toInt).toOption
        
        val format = rest.headOption.flatMap(toId)
        
        val samples = rest.drop(1)
        
        Success(Row(
            toInt(chrom), 
            toInt(pos), 
            toId(id), 
            toOption(ref), 
            toOption(alt), 
            toOption(qual), 
            toId(filter), 
            toId(info),
            format,
            samples))
      }
      case _ => Tries.failure(s"Couldn't parse VCF line '$line'")
    }
  }
}