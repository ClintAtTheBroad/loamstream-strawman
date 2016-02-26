package loamstream

import scala.concurrent.Future
import scala.util.Try

trait Transform[A, B] /*extends (A => Future[B])*/ {
  def id: String
  
  def apply(a: A): Future[B]
}

object Transform {
  def sync[A, B](name: String)(f: A => B): Transform[A, B] = new Transform[A, B] {
    override val id = name
    
    override def apply(a: A): Future[B] = Future.successful(f(a))
  }
  
  def apply[A, B](name: String)(f: A => Future[B]): Transform[A, B] = new Transform[A, B] { 
    override val id = name
    
    override def apply(a: A): Future[B] = f(a)
  }
}