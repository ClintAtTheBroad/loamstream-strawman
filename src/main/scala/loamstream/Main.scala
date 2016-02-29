package loamstream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object Main extends App {
  /*val source = Source.Literal(1)
  
  val f: Int ~> Int = (_: Int) + 1
  
  val g: Int ~> Int = (_: Int) * 3
  
  val h: Int ~> Double = (_: Int).toDouble
  
  val pipeline = source.map(f).map(g).map(h)*/
  
  val dictFile = Sources.path("/usr/share/dict/words")
  
  val getFirst10Lines = Transforms.firstLines(10)
  
  val getLast10Lines = Transforms.lastLines(10)
  
  val first10Words: Pipeline[Seq[String]] = dictFile.map(getFirst10Lines)
  
  val last10Words: Pipeline[Seq[String]] = dictFile.map(getFirst10Lines)
  
  val pipeline: Pipeline[Seq[String]] = for {
    first <- first10Words
    last <- last10Words
    firstString = first.mkString(":::")
    lastString = last.mkString(":::")
  } yield {
    Seq(firstString, lastString)
  }

  println(pipeline.run())
}