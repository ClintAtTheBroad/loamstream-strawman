package loamstream

import scala.reflect.ClassTag
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.concurrent.Await

trait Pipeline[A] {

  def value: A
  
  def run(): A = value
  
  def map[B](f: A ~> B): Pipeline[B] = new Pipeline.Mapped(this, f)
  
  def flatMap[B](f: A ~> Pipeline[B]): Pipeline[B] = new Pipeline.FlatMapped(this, f)
}

object Pipeline {
  def of[A](upstreamValue: A): Pipeline[A] = Source(() => upstreamValue)
  
  def apply[A](upstreamValue: () => A): Pipeline[A] = Source(upstreamValue)
  
  def from[A](source: Pipeline[A]): Pipeline[A] = Chained(source)
  
  final case class Chained[A](upstream: Pipeline[A]) extends Pipeline[A] {
    override def toString = s"Pipeline.${getClass.getSimpleName}(${upstream})"
  
    override lazy val value: A = upstream.value
  }
  
  final case class Source[A](v: () => A) extends Pipeline[A] {
    override def toString = s"Pipeline.${getClass.getSimpleName}(${v()})"
    
    override lazy val value: A = v()
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
