package loamstream

import scala.reflect.ClassTag
import scala.collection.{ Map => ScalaMap, Set => ScalaSet }

/**
 * @author clint
 * date: Mar 11, 2016
 */
trait Pile[K] {
  def contains(k: K): Boolean

  def keySet: ScalaSet[K]

  def mapToMap[V](f: K => (K, V)): Pile.Map[K, V]
}

object Pile {
  trait Map[K, V] extends Pile[K] {
    def toMap: ScalaMap[K, V]

    def map[K1, V1](f: ((K, V)) => (K1, V1)): Pile.Map[K1, V1]

    def flatMap[K1, V1](f: ((K, V)) => Pile.Map[K1, V1]): Pile.Map[K1, V1]

    def collect[K1, V1](pf: PartialFunction[(K, V), (K1, V1)]): Pile.Map[K1, V1]

    def filter(p: ((K, V)) => Boolean): Pile.Map[K, V]

    def foreach(f: ((K, V)) => Any): Unit
  }

  object Map {
    def from[K, V](m: ScalaMap[K, V]): Pile.Map[K, V] = ConcreteMapPile(m)

    private final case class ConcreteMapPile[K, V](m: ScalaMap[K, V]) extends Map[K, V] {
      override def toMap: ScalaMap[K, V] = m

      override def contains(k: K): Boolean = m.contains(k)

      override def keySet: ScalaSet[K] = m.keySet

      override def mapToMap[V1](f: K => (K, V1)): Pile.Map[K, V1] = from(keySet.map(f).toMap)

      override def map[K1, V1](f: ((K, V)) => (K1, V1)): Pile.Map[K1, V1] = from(m.map(f))

      override def flatMap[K1, V1](f: ((K, V)) => Pile.Map[K1, V1]): Pile.Map[K1, V1] = from(m.flatMap(t => f(t).toMap))

      override def collect[K1, V1](pf: PartialFunction[(K, V), (K1, V1)]): Map[K1, V1] = from(m.collect(pf))

      override def filter(p: ((K, V)) => Boolean): Map[K, V] = from(m.filter(p))

      override def foreach(f: ((K, V)) => Any): Unit = m.foreach(f)
    }
  }

  trait Set[K] extends Pile[K] {
    def toSet: ScalaSet[K]

    def map[K1](f: K => K1): Pile.Set[K1]

    def flatMap[K1](f: K => Pile.Set[K1]): Pile.Set[K1]

    def collect[K1](pf: PartialFunction[K, K1]): Pile.Set[K1]

    def filter(p: K => Boolean): Pile.Set[K]

    def foreach(f: K => Any): Unit
  }

  object Set {
    def from[K](s: ScalaSet[K]): Pile.Set[K] = ConcreteSetPile(s)

    private final case class ConcreteSetPile[K](s: ScalaSet[K]) extends Pile.Set[K] {
      override def contains(k: K): Boolean = s.contains(k)

      override def keySet: ScalaSet[K] = s

      override def mapToMap[V](f: K => (K, V)): Pile.Map[K, V] = Pile.Map.from(keySet.map(f).toMap)

      override def toSet: ScalaSet[K] = keySet

      override def map[K1](f: K => K1): Pile.Set[K1] = from(s.map(f))

      override def flatMap[K1](f: K => Pile.Set[K1]): Pile.Set[K1] = from(s.flatMap(k => f(k).toSet))

      override def collect[K1](pf: PartialFunction[K, K1]): Pile.Set[K1] = from(s.collect(pf))

      override def filter(p: K => Boolean): Pile.Set[K] = from(s.filter(p))

      override def foreach(f: K => Any): Unit = s.foreach(f)
    }
  }

}

/*trait Pile[K, V] {
  
  def collect[K1, V1](f: PartialFunction[(K, V), (K1, V1)]): Pile[K1, V1]
  
  def toMap: Map[K, V]
  
  def get(k: K): Option[V] = toMap.get(k)
  
  def >>>[K1, V1](f: (K, V) => (K1, V1)): Pile[K1, V1] = map(f)
  
  def foreach(f: (K, V) => Any): Unit = toMap.foreach(f.tupled)
  
  def keySet: Set[K] = toMap.keySet
  
  def map[K1, V1](f: (K, V) => (K1, V1)): Pile[K1, V1] = collect { case (k, v) => f(k, v) }
  
  def mapKeys[K1](f: K => K1): Pile[K1, V] = collect { case (k, v) => (f(k), v) }
  
  def mapValues[V1](f: V => V1): Pile[K, V1] = collect { case (k, v) => (k, f(v)) }
  
  def filter(p: ((K, V)) => Boolean): Pile[K, V] = collect { case t if p(t) => t } 
  
  def filterKeys(p: K => Boolean): Pile[K, V] = collect { case t @ (k, _) if p(k) => t } 
  
  def filterValues(p: V => Boolean): Pile[K, V] = collect { case t @ (_, v) if p(v) => t }
  
  def ++(other: Pile[K,V]): Pile[K,V]
}

object Pile {
  def fromMap[K,V](m: Map[K,V]): Pile[K,V] = MapPile(() => m)
  
  def fromSet[K](s: Set[K]): SetPile[K] = {
    val toUnitValuedTuple: K => (K,Unit) = (_, ())
    
    fromMap(s.map(toUnitValuedTuple).toMap)
  }
  
  final case class MapPile[K, V](map: () => Map[K, V]) extends Pile[K, V] {
    override def toMap: Map[K, V] = map()
    
    override def ++(other: Pile[K,V]): Pile[K,V] = fromMap(toMap ++ other.toMap)
    
    override def collect[K1, V1](f: PartialFunction[(K, V), (K1, V1)]): Pile[K1, V1] = {
      MapPile { () =>
        map().collect(f)
      }
    }
  }
}*/