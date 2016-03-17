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
    
    //TODO
    def tempFile = File.createTempFile("foo", "bar").toPath
    
    //TODO
    implicit val config = LoamConfig.fromConfig(ConfigFactory.parseString("")).get 
    
    import PipelineOp.Products.BurdenOutput
    
    val pipeline: Pipeline[BurdenOutput] = for {
      vcf1 <- fsPath("burden-input1.vcf") //TODO
      vcf2 <- fsPath("burden-input2.vcf") //TODO
      combined <- command("combine", () => tempFile)(vcf1, vcf2)
      compressed <- command("compress", () => tempFile)(combined)
      output <- command("burden", () => BurdenOutput(tempFile))(compressed)
    } yield output
    
    val burdenOutput = pipeline.runWith(Mapping.Default)
    
    assert(Files.exists(burdenOutput.results))
    
    //TODO: More
  }
}