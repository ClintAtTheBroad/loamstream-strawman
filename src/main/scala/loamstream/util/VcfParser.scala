package loamstream.util

import java.io.{PrintWriter, BufferedReader, File, FileInputStream}
import java.util.zip.GZIPInputStream

import scala.io.Source

/**
 * Created on: 1/20/16 
 * @author Kaan Yuksel 
 */
object VcfParser {
  def getSamples(header: String): Array[String] = {
    val ignoreable = "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\t"
    header.stripPrefix(ignoreable).split("\t")
  }

  def gzBufferedReader(gzFile: String) = gzBufferedSource(gzFile).bufferedReader()

  def gzBufferedSource(gzFile: String) = Source.fromInputStream(new GZIPInputStream(new FileInputStream(gzFile)))

  def versionSupported(versionLine: String, versionSupported: String): Boolean = {
    val expectedLine = "##fileformat=VCFv" + versionSupported
    versionLine == expectedLine
  }

  import JavaIoHelpers._
  
  def getHeaderLine(buffer: BufferedReader): String = {
    buffer.toIterable.dropWhile(_.trim.startsWith("##")).headOption.getOrElse("")
  }

  def printToFile(f: File)(op: PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}

object SampleExtractorApp extends App {
  val vcfParser = VcfParser
  val versionSupported = "4.1"
  val file = "/Users/kyuksel/BurdenFiles/v3.clean.1000.vcf.gz"
  val buffer = vcfParser.gzBufferedReader(file)
  if (!vcfParser.versionSupported(buffer.readLine(), versionSupported)) {
    println("VCF versions other than " + versionSupported + " are not supported")
    System.exit(1)
  }

  val headerLine = vcfParser.getHeaderLine(buffer)
  val samples = vcfParser.getSamples(headerLine)
  vcfParser.printToFile(new File("samples.txt")) {
    p => samples.foreach(p.println)
  }
}
