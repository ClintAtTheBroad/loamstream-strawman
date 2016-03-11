package loamstream

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import loamstream.vcf.VcfParser

/**
 * @author clint
 * date: Mar 11, 2016
 */
final class SampleIdExtractionPipelineTest extends FunSuite {
  test("Pipeline successfully extracts sample IDs from VCF") {
    
    import PipelineOp._
    
    val pipeline: Pipeline[Set[String]] = for {
      path <- fileFromClasspath("mini.vcf")
      vcf <- parseVcf(path)
      samples <- getSamples(vcf)
    } yield samples
    
    val sampleIds = Runner.Default.run(pipeline)
    
    assert(sampleIds == Set("Sample1", "Sample2", "Sample3"))
  }
}