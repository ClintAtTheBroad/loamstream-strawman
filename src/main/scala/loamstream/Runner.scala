package loamstream

/**
 * @author clint
 * date: Mar 11, 2016
 */
trait Runner {
  def run[A](pipeline: Pipeline[A]): A
}

object Runner {
  object Default extends Runner {
    override def run[A](f: Pipeline[A]): A = f.foldMap(Mapping.Default)
  }
}