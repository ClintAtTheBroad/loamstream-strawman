package loamstream

/**
 * @author clint
 * date: Mar 15, 2016
 */
import org.scalatest.FunSuite
import java.nio.file.Files
import java.io.File
import loamstream.config.LoamConfig
import com.typesafe.config.ConfigFactory
import java.nio.file.Path
import loamstream.PipelineOp.Products.BurdenOutput
final class BurdenPipelineTest extends FunSuite {
  test("The burden pipeline should run") {
    import PipelineOp._
    import PipelineOp.Products.BurdenOutput
    import Expectation._
    
    //TODO
    val config = LoamConfig.fromConfig(ConfigFactory.parseString("")).get 
    
    val pipeline: Pipeline[Int] = for {
      vcf1 <- locate("burden-input1.vcf") //TODO
      vcf2 <- locate("burden-input2.vcf") //TODO
      combined <- runCommand("combine", producingFile("combined.vcf"))(vcf1, vcf2)
      compressed <- runCommand("compress", producingFile("compressed.vcf"))(combined)
      burdenResult <- runCommand("burden")(compressed)
    } yield burdenResult
    
    val burdenStatus = pipeline.runWith(Mapping.fromLoamConfig(config))
    
    assert(burdenStatus == 0)
    
    //TODO: More
  }
}