package loamstream

import scala.reflect.ClassTag
import scala.util.Try

import scala.language.higherKinds

trait PileStream[P] extends Upstream[P] with Sink[P] {

  type Self[A] <: PileStream[A]
  
  protected def asSelf: Self[P]

  def >>>[A, B, C, D](f: (A, B) => (C, D))(implicit scheduler: Scheduler, ev: P <:< Pile[A,B]): Self[Pile[C,D]] = {
    map(_.map(f))
  }
  
  def >>>(sink: Sink[P])(implicit scheduler: Scheduler): Self[P] = ???
  
  def map[Q](f: P => Q)(implicit scheduler: Scheduler): Self[Q]

  def collect[Q](f: PartialFunction[P, Q])(implicit scheduler: Scheduler): Self[Q]

  def collectMulti[Q](f: PartialFunction[P, PileStream[Q]])(implicit scheduler: Scheduler): Self[Q]

  def flatMap[Q](f: P => PileStream[Q])(implicit scheduler: Scheduler): Self[Q]

  def filter(p: P => Boolean)(implicit scheduler: Scheduler): Self[P]

  final def filterNot(p: P => Boolean)(implicit scheduler: Scheduler): Self[P] = filter(e => !p(e))

  def ++(other: PileStream[P])(implicit scheduler: Scheduler): Self[P]

  /*def partition(p: P => Boolean)(implicit scheduler: Scheduler): (Self[P], Self[P])

  def drop(howMany: Int)(implicit scheduler: Scheduler): Self[P]

  def take(howMany: Int)(implicit scheduler: Scheduler): Self[P]*/
}