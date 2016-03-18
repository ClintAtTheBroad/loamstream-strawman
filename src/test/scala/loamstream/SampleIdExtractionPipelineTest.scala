package loamstream

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import loamstream.vcf.VcfParser
import loamstream.config.LoamConfig

/**
 * @author clint
 * date: Mar 11, 2016
 */
final class SampleIdExtractionPipelineTest extends FunSuite {
  test("Pipeline successfully extracts sample IDs from VCF") {
    
    import PipelineOp._
    
    val pipeline: Pipeline[Pile.Set[String]] = for {
      path <- locate("classpath:mini.vcf")
      samples <- getSamplesFromFile(path)
    } yield samples
    
    val expected = Set("Sample1", "Sample2", "Sample3")
    
    val mapping = Mapping.fromLoamConfig(LoamConfig.Empty)
    
    val sampleIds = pipeline.runWith(mapping).keySet
    
    assert(sampleIds == expected)
  }
}