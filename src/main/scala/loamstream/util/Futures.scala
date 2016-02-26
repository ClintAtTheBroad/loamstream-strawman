package loamstream.util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.concurrent.Promise

object Futures {
  def sequenceToMap[A, B](attempts: TraversableOnce[(A, Future[B])])(implicit executor: ExecutionContext): Future[Map[A, B]] = {
    val z: Future[Map[A, B]] = Future.successful(Map.empty)

    attempts.foldLeft(z) { (futureAcc, pair) =>
      for {
        map <- futureAcc
        (a, futureB) = pair
        b <- futureB
      } yield {
        map + (a -> b)
      }
    }
  }
  
  object Implicits {
    final implicit class FutureOps[A](val future: Future[A]) extends AnyVal {
      def tryMap[B](f: A => Try[B])(implicit executor: ExecutionContext): Future[B] = {
        val promise: Promise[B] = Promise()
        
        for {
          a <- future
        } {
          promise.complete(f(a))
        }
        
        promise.future
      }
    }
  }
}