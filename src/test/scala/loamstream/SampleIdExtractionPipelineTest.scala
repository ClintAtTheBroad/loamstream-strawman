package loamstream

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import loamstream.vcf.VcfParser
import scala.concurrent.duration.Duration
import scala.concurrent.Await

/**
 * @author clint
 * date: Mar 11, 2016
 */
final class SampleIdExtractionPipelineTest extends FunSuite {
  test("Pipeline successfully extracts sample IDs from VCF") {
    
    import PipelineOp._
    
    val pipeline: Pipeline[Set[String]] = Pipelines.samplesFromClasspathFile("mini.vcf")
    
    val sampleIds = Await.result(Runner.Default.run(pipeline), Duration.Inf)
    
    assert(sampleIds == Set("Sample1", "Sample2", "Sample3"))
  }
}