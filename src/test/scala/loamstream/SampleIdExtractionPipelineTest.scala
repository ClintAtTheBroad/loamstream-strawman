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
    
    val pipeline: Pipeline[Pile.Set[String]] = for {
      path <- fileFromClasspath("mini.vcf")
      samples <- getSamplesFromFile(path)
    } yield samples
    
    val expected = Set("Sample1", "Sample2", "Sample3")
    
    {
      val sampleIds = Runner.Default.run(pipeline).keySet
    
      assert(sampleIds == expected)
    }
    
    {
      val sampleIds = pipeline.runWith(Mapping.Default).keySet
      
      assert(sampleIds == expected)
    }
  }
}