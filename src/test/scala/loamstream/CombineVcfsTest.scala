package loamstream

import org.scalatest.FunSuite
import java.nio.file.Path
import java.nio.file.Files

/**
 * @author clint
 * date: Mar 14, 2016
 */
final class CombineVcfsTest extends FunSuite {
  test("Should be able to combine VCFs with GATK") {
    import PipelineOp._
    
    val pipeline: Pipeline[Path] = for {
      p1 <- fileFromClasspath("mini.vcf")
      p2 <- fileFromClasspath("mini.vcf")
      dest <- fsPath("/tmp/out.vcf")
      combined <- combineFiles(p1, p2)
    } yield combined
    
    val combined = Runner.Default.run(pipeline)
    
    assert(Files.exists(combined))
    
    //TODO: More
  }
}