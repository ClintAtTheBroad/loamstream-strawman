package loamstream.vcf

import java.nio.file.Path

import htsjdk.variant.variantcontext.{Genotype, VariantContext}
import htsjdk.variant.vcf.VCFFileReader

import scala.collection.JavaConverters.{asScalaBufferConverter, asScalaIteratorConverter}

/**
  * Created on: 1/20/16
  *
  * @author Kaan Yuksel
  */
case class VcfParser(path: Path, requireIndex: Boolean = false) {
  private val reader = new VCFFileReader(path.toFile, requireIndex)
  
  val samples: Seq[String] = reader.getFileHeader.getGenotypeSamples.asScala.toSeq

  def rowIter: Iterator[VariantContext] = reader.iterator.asScala

  def genotypesIter: Iterator[Seq[Genotype]] = rowIter.map(_.getGenotypes.asScala.toSeq)

  def genotypeMapIter: Iterator[Map[String, Genotype]] = {
    rowIter.map(row => samples.zip(row.getGenotypes.asScala).toMap)
  }

}
