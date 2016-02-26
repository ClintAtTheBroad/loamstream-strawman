package loamstream

trait Pile[K, V] {
  
  def collect[K1, V1](f: PartialFunction[(K, V), (K1, V1)]): Pile[K1, V1]
  
  def toMap: Map[K, V]
  
  def get(k: K): Option[V]
  
  def >>>[K1, V1](f: (K, V) => (K1, V1)): Pile[K1, V1] = map(f)
  
  def foreach(f: (K, V) => Any): Unit = toMap.foreach(f.tupled)
  
  def keySet: Set[K] = toMap.keySet
  
  def map[K1, V1](f: (K, V) => (K1, V1)): Pile[K1, V1] = collect { case (k, v) => f(k, v) }
  
  def mapKeys[K1](f: K => K1): Pile[K1, V] = collect { case (k, v) => (f(k), v) }
  
  def mapValues[V1](f: V => V1): Pile[K, V1] = collect { case (k, v) => (k, f(v)) }
  
  def filter(p: ((K, V)) => Boolean): Pile[K, V] = collect { case t if p(t) => t } 
  
  def filterKeys(p: K => Boolean): Pile[K, V] = collect { case t @ (k, _) if p(k) => t } 
  
  def filterValues(p: V => Boolean): Pile[K, V] = collect { case t @ (_, v) if p(v) => t }
}

object Pile {
  case class MapPile[K, V](map: () => Map[K, V]) extends Pile[K, V] {
    override def toMap: Map[K, V] = map()
    
    override def get(k: K): Option[V] = toMap.get(k)
    
    override def collect[K1, V1](f: PartialFunction[(K, V), (K1, V1)]): Pile[K1, V1] = {
      MapPile { () =>
        map().collect(f)
      }
    }
  }
}