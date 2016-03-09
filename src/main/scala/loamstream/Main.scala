package loamstream

object Main extends App {

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
      
      Pipeline(Seq(firstString, lastString))
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
  
  val p1 = dictFile.map(getFirst10Lines).map(_.head)
  
  println("made pipelines") 
  
  println(pipeline)
  println(p0)
  println(p1)

  println(pipeline.run())
  
  println(p1.run())
}