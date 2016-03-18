package loamstream

/**
 * @author clint
 * date: Mar 11, 2016
 */
trait Runner {
  def run[A](pipeline: Pipeline[A]): A
}

object Runner {
  def fromMapping(m: Mapping): Runner = new Runner {
    override def run[A](pipeline: Pipeline[A]): A = pipeline.foldMap(m)
  }
}