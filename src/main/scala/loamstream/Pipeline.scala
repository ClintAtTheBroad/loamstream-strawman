package loamstream

import scala.reflect.ClassTag
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.concurrent.Await

trait Pipeline[A] { self =>

  def value: A
  
  def run(): A = value
  
  def map[B](f: A ~> B): Pipeline[B] = new Pipeline.Mapped(this, f)
  
  def flatMap[B](f: A ~> Pipeline[B]): Pipeline[B] = new Pipeline.FlatMapped(this, f)
  
  import Pipeline.Source
  
  def partition(p: A => (A, A)): (Pipeline[A], Pipeline[A]) = {
    lazy val (lhs, rhs) = p(value)
    
    (Source(lhs), Source(rhs))
  }
}

object Pipeline {
  def of[A](upstreamValue: => A): Pipeline[A] = apply(upstreamValue)
  
  def apply[A](upstreamValue: => A): Pipeline[A] = Source(upstreamValue)
  
  def from[A](source: Pipeline[A]): Pipeline[A] = Chained(source)
  
  final case class Chained[A](upstream: Pipeline[A]) extends Pipeline[A] {
    override def toString = s"Pipeline.${getClass.getSimpleName}(${upstream})"
  
    override lazy val value: A = upstream.value
  }
  
  final class Source[A](private val v: () => A) extends Pipeline[A] {
    override def toString = s"Pipeline.${getClass.getSimpleName}(${v()})"
    
    override def equals(other: Any): Boolean = other match {
      case that: Source[A] if that != null => this.v == that.v
      case _ => false
    }
    
    override def hashCode: Int = v.hashCode
    
    override lazy val value: A = v()
  }
  
  object Source {
    def apply[A](a: => A): Source[A] = new Source(() => a)
  }
  
  final class Mapped[A, B](upstream: Pipeline[A], transform: A => B) extends Pipeline[B] {
    override def toString = s"Pipeline.${getClass.getSimpleName}($transform, $upstream)"
    
    override lazy val value: B = transform(upstream.value)
  }
  
  final class FlatMapped[A, B](upstream: Pipeline[A], transform: A => Pipeline[B]) extends Pipeline[B] {
    override def toString = s"Pipeline.${getClass.getSimpleName}($transform, $upstream)"
    
    override lazy val value: B = transform(upstream.value).value
  }
}
