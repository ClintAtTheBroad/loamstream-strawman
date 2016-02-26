package loamstream

import scala.language.higherKinds

trait HasEmpty[C[_]] {
  def empty[A]: C[A]
}