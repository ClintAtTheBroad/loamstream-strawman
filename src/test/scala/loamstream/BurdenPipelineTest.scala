package loamstream

/**
 * @author clint
 * date: Mar 15, 2016
 */
import org.scalatest.FunSuite
import java.nio.file.Files
final class BurdenPipelineTest extends FunSuite {
  test("The burden pipeline should run") {
    import PipelineOp._
    
    val pipeline: Pipeline[PipelineOp.Products.BurdenOutput] = for {
      vcf1 <- fsPath("burden-input1.vcf") //TODO
      vcf2 <- fsPath("burden-input2.vcf") //TODO
      combined <- combineFiles(vcf1, vcf2)
      compressed <- compress(combined)
      output <- runBurden(compressed)
    } yield output
    
    val burdenOutput = pipeline.runWith(Mapping.Default)
    
    assert(Files.exists(burdenOutput.results))
    
    //TODO: More
  }
}