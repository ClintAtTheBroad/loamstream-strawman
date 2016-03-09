package loamstream

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import loamstream.model.SampleId
import loamstream.vcf.VcfParser
final class SampleIdExtractionPipelineTest extends FunSuite with BeforeAndAfter {
  test("Pipeline successfully extracts sample IDs from VCF") {
    
    val pipeline: Pipeline[Set[String]] = for {
      path <- Sources.fileFromClasspath("/mini.vcf")
      vcfParser = VcfParser(path)
      samples = vcfParser.samples
    } yield samples.toSet
    
    val sampleIds = pipeline.run()
    
    assert(sampleIds == Set("Sample1", "Sample2", "Sample3"))
  }
}