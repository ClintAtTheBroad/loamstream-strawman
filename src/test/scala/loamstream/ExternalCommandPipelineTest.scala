package loamstream

import org.scalatest.FunSuite
import loamstream.config.LoamConfig
import com.typesafe.config.ConfigFactory
import java.nio.file.Path
import java.nio.file.Files

/**
 * @author clint
 * date: Mar 21, 2016
 */
final class ExternalCommandPipelineTest extends FunSuite {
  test("A pipeline calling external tools should run") {
    import PipelineOp._
    
    val config = LoamConfig.load("loamstream-external-commands-test").get 
    
    val pipeline: Pipeline[Path] = for {
      fileA <- locate("src/test/resources/a.txt")
      fileB <- locate("src/test/resources/b.txt")
      combineResult <- runCommand("combine")(fileA, fileB)
      compressResult <- runCommand("compress")(combineResult.path)
      analysisResult <- runCommand("analyze")(compressResult.path)
    } yield analysisResult.path
    
    val analysisResult = pipeline.runWith(Mapping.fromLoamConfig(config))
    
    assert(Files.exists(analysisResult))
  
    import scala.collection.JavaConverters._
    
    val resultLines = Files.readAllLines(analysisResult).asScala.toSeq
    
    assert(resultLines == Seq("3.875"))
  }
}