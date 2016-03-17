package loamstream.commands

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import loamstream.util.Tries

/**
 * @author clint
 * date: Mar 16, 2016
 */
sealed trait CommandType

object CommandType {
  case object Unix extends CommandType
  
  def fromString(s: String): Try[CommandType] = s.toLowerCase match {
    case "unix" => Success(Unix)
    case _ => Tries.failure(s"Couldn't determine command type from ${if(s == null) "null" else s"'$s'"}")
  }
}