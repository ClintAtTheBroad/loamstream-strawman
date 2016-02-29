package loamstream

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext

trait Transform[A, B] extends (A => B) {
  def name: String
  
  def apply(a: A): B
}

object Transform {
  def apply[A, B](n: String)(f: A => B): Transform[A, B] = new Transform[A, B] {
    override val name = n
    
    override def apply(a: A): B = f(a)
  }
  
  import scala.language.implicitConversions
  
  implicit def function1sAreTransforms[A, B](f: A => B)(implicit executor: ExecutionContext): Transform[A, B] = {
    Transform(f.toString)(f)
  }
}