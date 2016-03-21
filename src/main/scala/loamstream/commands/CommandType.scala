package loamstream.commands

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import loamstream.util.Tries
import java.nio.file.Path

/**
 * @author clint
 * date: Mar 16, 2016
 */
sealed trait CommandType

object CommandType {
  
  case object Simple extends CommandType
  case object Transform extends CommandType
  
  def fromString(s: String): Try[CommandType] = Try(String.valueOf(s).toLowerCase) match {
    case Success("simple") => Success(Simple)
    case Success("transform") => Success(Transform)
    case _ => Tries.failure(s"Couldn't determine command type from ${if(s == null) "null" else s"'$s'"}")
  }
}