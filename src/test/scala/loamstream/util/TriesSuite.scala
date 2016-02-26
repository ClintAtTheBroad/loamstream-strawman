package loamstream.util

import org.scalatest.FunSuite
import scala.util.Try
import scala.util.Success
import scala.util.Failure

/**
 * @author clint
 * date: Feb 3, 2016
 */
final class TriesSuite extends FunSuite {
  import Tries.failure
  
  test("failure") {
    val f: Failure[String] = failure("foo")
    
    assert(f.isFailure)
    assert(f.exception.getMessage == "foo")
  }
  
  test("sequenceIgnoringFailures") {
    import Tries.sequenceIgnoringFailures
    
    {
      val noAttempts: Seq[Try[Int]] = Nil
      
      assert(sequenceIgnoringFailures(noAttempts) == Success(Nil))
    }
    
    {    
      val attempts: Seq[Try[Int]] = Seq(Success(1), Success(2), Success(3))
      
      assert(sequenceIgnoringFailures(attempts) == Success(Seq(1,2,3)))
    }
    
    {    
      val attempts: Seq[Try[Int]] = Seq(failure("foo"), Success(1), Success(2), Success(3))
      
      assert(sequenceIgnoringFailures(attempts) == Success(Seq(1,2,3)))
    }
    
    {    
      val attempts: Seq[Try[Int]] = Seq(failure("foo"), Success(1), failure("foo"), failure("foo"), Success(2), Success(3), failure("foo"))
      
      assert(sequenceIgnoringFailures(attempts) == Success(Seq(1,2,3)))
    }
    
    {    
      val attempts: Seq[Try[Int]] = Seq(failure("foo"), failure("foo"), failure("foo"), failure("foo"))
      
      assert(sequenceIgnoringFailures(attempts) == Success(Nil))
    }
  }
  
  test("sequence") {
    import Tries.sequence
    
    {
      val noAttempts: Seq[Try[Int]] = Nil
      
      assert(sequence(noAttempts) == Success(Nil))
    }
    
    {    
      val attempts: Seq[Try[Int]] = Seq(Success(1), Success(2), Success(3))
      
      assert(sequence(attempts) == Success(Seq(1,2,3)))
    }
    
    val f = failure("foo")
    
    val g = failure("bar")
    
    val h = failure("baz")
    
    {    
      val attempts: Seq[Try[Int]] = Seq(f, Success(1), Success(2), Success(3))
      
      assert(sequence(attempts) == f)
    }
    
    {    
      val attempts: Seq[Try[Int]] = Seq(f, g, h, Success(2), Success(3), failure("foo"))
      
      assert(sequence(attempts) == f)
    }
    
    {    
      val attempts: Seq[Try[Int]] = Seq(f, g, h)
      
      assert(sequence(attempts) == f)
    }
  }
}