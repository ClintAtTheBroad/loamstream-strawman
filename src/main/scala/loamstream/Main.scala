package loamstream

object Main {
  /*val source: Source[Int, String] = ???
  
  final case class Foo(s: String)
  
  val sink: Sink[Double, Foo] = ???
  
  //source >>> sink
  
  val f: (Int, String) => (Int, String) = (i, s) => (i + 1, s)
  
  val g: (Int, String) => (Int, String) = (i, s) => (i * 2, s)
  
  val h: (Int, String) => (Double, Foo) = (i, s) => (i.toDouble, Foo(s))
  
  val p0 = source >>> f
  
  val p1 = p0 >>> g
  
  //p1 >>> sink
  
  val s = source >>> f >>> g >>> h >>> sink*/
  
  import Scheduler.Implicits.Sync
  
  final case class Foo(s: String)
  
  val f: (Int, String) => (Int, String) = (i, s) => (i + 1, s)
  
  val g: (Int, String) => (Int, String) = (i, s) => (i * 2, s)
  
  val h: (Int, String) => (Double, Foo) = (i, s) => (i.toDouble, Foo(s))
  
  val source: Source[Int, String] = ???
  
  val sink: Sink[Pile[Double, Foo]] = ???
  
  val initial: Pipeline[Pile[Int, String]] = Pipeline.fromSource(source)
  
  val pipeline = source >>> f >>> g >>> h >>> sink
  
  pipeline.run()
}