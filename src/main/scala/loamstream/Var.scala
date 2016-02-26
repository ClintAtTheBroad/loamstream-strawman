package loamstream

final class Var[A] extends VariableLike[A]

object Var extends HasEmpty[Var] {
  def apply[A](): Var[A] = empty
  
  override def empty[A]: Var[A] = new Var
}