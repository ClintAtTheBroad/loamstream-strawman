package loamstream.config

import loamstream.commands.CommandDescription
import com.typesafe.config.Config
import scala.util.Try
import loamstream.util.Tries
import com.typesafe.config.ConfigFactory

/**
 * @author clint
 * date: Mar 16, 2016
 */

final case class LoamConfig(commands: Map[String, CommandDescription])

object LoamConfig {
  val Empty: LoamConfig = LoamConfig(Map.empty)
  
  private[config] val prefix = "loamstream"
  
  private[config] def toKey(s: String): String = s"$prefix.$s"
  
  import ConfigEnrichments._

  def load(prefix: String): Try[LoamConfig] = {
    def loadConfigFile = Try(ConfigFactory.load(prefix).withFallback(ConfigFactory.load()))
    
    loadConfigFile.flatMap(fromConfig)
  }
  
  def fromConfig(config: Config): Try[LoamConfig] = {
    val commandsKey = toKey("commands")
    
    for {
      commandSection <- config.tryConfig(commandsKey)
      commandNames <- config.keysUnder(commandsKey)
      commandMap <- parseCommands(commandSection, commandNames)
    } yield {
      LoamConfig(commandMap)
    }
  }

  private def parseCommands(commandSection: Config, commandNames: Set[String]): Try[Map[String, CommandDescription]] = {
    import scala.collection.JavaConverters._

    val commandAttempts = commandNames.map { name =>
      for {
        commandChunk <- commandSection.tryConfig(name)
        desc <- CommandDescription.fromConfig(commandChunk)
      } yield name -> desc
    }

    Tries.sequence(commandAttempts).map(_.toMap)
  }
}