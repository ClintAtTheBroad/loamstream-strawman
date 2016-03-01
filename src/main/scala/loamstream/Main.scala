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
  
  val second5Words = first10Words.map(_.drop(5).take(5))
  val secondToLast5Words = first10Words.map(_.dropRight(5).takeRight(5))
  
  val p0 = first10Words.flatMap { first =>
    last10Words.flatMap { last =>
      val firstString = first.mkString(":::")
      val lastString = last.mkString(":::")
      
      Source.of(Seq(firstString, lastString))
    }
  }
  
  val pipeline: Pipeline[Seq[String]] = for {
    first <- second5Words
    last <- secondToLast5Words
    firstString = first.mkString(":::")
    lastString = last.mkString(":::")
  } yield {
    Seq(firstString, lastString)
  }
  
  println(pipeline)
  println(p0)

  println(pipeline.run())
}