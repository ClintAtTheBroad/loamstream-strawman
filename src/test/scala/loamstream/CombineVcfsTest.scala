package loamstream

import org.scalatest.FunSuite
import java.nio.file.Path
import java.nio.file.Files
import com.typesafe.config.ConfigFactory
import loamstream.config.LoamConfig
import java.io.File

/**
 * @author clint
 * date: Mar 14, 2016
 */
final class CombineVcfsTest extends FunSuite {
  test("Should be able to combine VCFs with GATK") {
    import PipelineOp._
    import Expectation._

    //TODO
    implicit val config = LoamConfig.fromConfig(ConfigFactory.parseString("")).get
    
    val pipeline: Pipeline[Path] = for {
      p1 <- fileFromClasspath("mini.vcf")
      p2 <- fileFromClasspath("mini.vcf")
      combined <- runCommand("combine", producingFile("combined.vcf"))(p1, p2)
    } yield combined
    
    val combined = pipeline.runWith(Mapping.fromLoamConfig(config))
    
    assert(Files.exists(combined))
    
    //TODO: More
  }
}