package loamstream

import org.scalatest.FunSuite
import loamstream.config.LoamConfig


final class PipelineTest extends FunSuite {
  test("zip should work") {
    import PipelineStep._
    
    val pa: Pipeline[Int] = literal(42)
    val pb: Pipeline[Int] = literal(1)
    
    val zipped: Pipeline[(Int, Int)] = pa.zip(pb)
    
    val mapping = Mapping.fromLoamConfig(LoamConfig.Empty)
    
    val (a, b) = zipped.runWith(mapping)
    
    assert(a == 42)
    assert(b == 1)
  }
}