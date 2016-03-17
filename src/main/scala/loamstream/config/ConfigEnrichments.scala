package loamstream.config

import com.typesafe.config.Config
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import loamstream.util.Tries
import java.{ util => ju }

/**
 * @author clint
 * date: Mar 16, 2016
 */
object ConfigEnrichments {
  import scala.collection.JavaConverters._
  
  final implicit class ConfigOps(val conf: Config) extends AnyVal {
    def tryString(key: String): Try[String] = attempt(_.getString)(key)
    
    def tryInt(key: String): Try[Int] = attempt(_.getInt)(key)
    
    def tryConfig(key: String): Try[Config] = attempt(_.getConfig)(key)
    
    def tryStringList(key: String): Try[Seq[String]] = attempt(_.getStringList)(key).map(_.asScala)
    
    def tryObjectList(key: String): Try[Seq[Config]] = attempt(_.getConfigList)(key).map(_.asScala)
    
    def keysUnder(key: String): Try[Set[String]] = attempt(_.getObject)(key).map(_.asScala.collect { case (k, _) => k }.toSet)
    
    private def attempt[A](get: Config => String => A)(key: String): Try[A] = {
      Try(get(conf)(key)).recoverWith {
        case _ => Tries.failure(s"Couldn't find config key '$key'")
      }
    }
  }
}