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
