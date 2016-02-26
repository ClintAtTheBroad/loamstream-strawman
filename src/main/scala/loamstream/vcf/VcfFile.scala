package loamstream.vcf

import java.time.LocalDate
import scala.util.Try
import scala.io.Source
import scala.util.Success
import scala.util.Failure
import scala.util.matching.Regex
import java.time.format.DateTimeFormatter
import loamstream.util.Tries
import java.io.Reader
import java.nio.file.Path
import loamstream.model.SampleId

/**
 * @author clint
 * date: Feb 3, 2016
 */
final case class VcfFile(
    format: String,
    date: LocalDate,
    source: String,
    reference: String,
    phasing: String,
    infos: Seq[Info],
    filters: Seq[Filter],
    formats: Seq[Format],
    columns: Seq[String],
    sampleIds: Seq[SampleId],
    rows: Seq[Row]
)

object VcfFile {
  
  def fromString(data: String): Try[VcfFile] = fromSource(Source.fromString(data))
  
  def fromFile(filename: String): Try[VcfFile] = fromSource(Source.fromFile(filename))
  
  def fromPath(path: Path): Try[VcfFile] = fromSource(Source.fromFile(path.toFile))

  val numFixedColumns = 8  
  
  //NB: Use a by-name param so we have to worry less about whether the passsed Source
  //is exhausted and is reset-able.
  //TODO: properly close the Source
  def fromSource(source: => Source): Try[VcfFile] = {
    def lines = source.getLines.map(_.trim).filterNot(_.isEmpty)
    
    val headers: Seq[String] = lines.takeWhile(isHeader).map(dropLeadingHashes).toVector
    
    val metaLines: Seq[String] = lines.filter(isMetaInfo).map(dropLeadingHashes).toVector
    
    val nonHeaders: Iterator[String] = lines.dropWhile(isHeader)
    
    def singleHeader[T](f: String => Try[T]): Try[T] = {
      Tries.sequenceIgnoringFailures(headers.map(f)).map(_.headOption.get)
    }
    
    def extractSeq[T](expectedPrefix: String, parse: String => Try[T]): Try[Seq[T]] = {
      def isValid(line: String): Boolean = line.startsWith(expectedPrefix)
      
      val attempts = metaLines.filter(isValid).map(parse)
      
      Tries.sequence(attempts)
    }
    
    def extractInfos: Try[Seq[Info]] = extractSeq("INFO", Info.fromString)
    
    def extractFilters: Try[Seq[Filter]] = extractSeq("FILTER", Filter.fromString)
    
    def extractFormats: Try[Seq[Format]] = extractSeq("FORMAT", Format.fromString)
    
    def extractRows: Seq[Row] = {
      //NB: Ignore parsing failures, so we can stream rows simply
      nonHeaders.flatMap(line => Row.fromString(line).toOption.iterator).toStream
    }
    
    for {
      format <- singleHeader(extractFileFormat)
      date <- singleHeader(extractFileDate)
      source <- singleHeader(extractSource)
      reference <- singleHeader(extractReference)
      phasing <- singleHeader(extractPhasing)
      infos <- extractInfos
      filters <- extractFilters
      formats <- extractFormats
    } yield {
      val headerLine = headers.last
      
      val headerParts = headerLine.split("\\s+")
      
      val columns = headerParts.take(numFixedColumns).toVector
      
      val nonColumns = headerParts.drop(numFixedColumns)
      
      val formatColumnPresent = nonColumns.headOption.exists(_ == "FORMAT")
      
      val sampleIds = nonColumns.drop(1).toVector.map(SampleId(_))
      
      val rows = extractRows
      
      VcfFile(format, date, source, reference, phasing, infos, filters, formats, columns, sampleIds, rows)
    }
  }
  
  private[vcf] def isHeader(line: String): Boolean = line.startsWith("#")
  
  private[vcf] def isMetaInfo(line: String): Boolean = line.startsWith("##")
  
  private[vcf] def dropLeadingHashes(line: String): String = line.dropWhile(_ == '#')
  
  /*
   * ##fileformat=VCFv4.0
##fileDate=20090805
##source=myImputationProgramV3.1
##reference=1000GenomesPilot-NCBI36
##phasing=partial
   */
  
  private[vcf] object Regexes {
    val fileFormat = "(?i)fileformat\\=(.+)".r
    val fileDate = "(?i)fileDate\\=(\\d\\d\\d\\d\\d\\d\\d\\d)".r
    val source = "(?i)source\\=(.+)".r
    val reference = "(?i)reference\\=(.+)".r
    val phasing = "(?i)phasing\\=(.+)".r
  }
  
  private def failure(message: String) = Failure(new Exception(message))
  
  private def extract(line: String, regex: Regex, failureMessage: String): Try[String] = {
    line match {
      case regex(s) => Success(s)
      case _ => failure(s"$failureMessage. failing line: '$line'")
    }
  }
  
  private[vcf] def extractFileFormat(line: String): Try[String] = extract(line, Regexes.fileFormat, "Couldn't find fileformat line")
  
  lazy val dateFormatter = DateTimeFormatter.BASIC_ISO_DATE
  
  private def parseLocalDate(digits: String): Try[LocalDate] = {
    Try(LocalDate.parse(digits, dateFormatter))
  }
  
  private[vcf] def extractFileDate(line: String): Try[LocalDate] = { 
    extract(line, Regexes.fileDate, "Couldn't find fileDate line").flatMap(parseLocalDate)
  }
  
  private[vcf] def extractSource(line: String): Try[String] = extract(line, Regexes.source, "Couldn't find source line")
  
  private[vcf] def extractReference(line: String): Try[String] = extract(line, Regexes.reference, "Couldn't find reference line")
  
  private[vcf] def extractPhasing(line: String): Try[String] = extract(line, Regexes.phasing, "Couldn't find phasing line")
}