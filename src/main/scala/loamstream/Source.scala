package loamstream

trait Source[A, B] {
  def pile: Pile[A, B]
  
  def >>>[C, D](f: (A, B) => (C, D)): Pipeline[Pile[C,D]] = {
    
    
    ???
  }
}

object Source {
  def fromFunction[A, B](f: () => Pile[A, B]): Source[A, B] = new LazySource(f)
  
  def fromPile[A, B](pile: Pile[A, B]): Source[A, B] = ConcreteSource(pile)
  
  final class LazySource[A, B](f: () => Pile[A, B]) extends Source[A, B] {
    override lazy val pile: Pile[A, B] = f()
  }
  
  final case class ConcreteSource[A, B](pile: Pile[A, B]) extends Source[A, B]
}