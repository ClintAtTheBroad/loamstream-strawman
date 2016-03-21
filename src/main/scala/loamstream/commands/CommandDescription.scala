package loamstream.commands

import scala.util.Try
import com.typesafe.config.Config
import scala.reflect.runtime.universe._
import loamstream.config.ConfigEnrichments
import java.nio.file.Paths
import loamstream.Expectation
import java.nio.file.Path

/**
 * @author clint
 * date: Mar 16, 2016
 */
//TODO: Don't hard-code expectation type if possible
//TODO: Are paramTypes and resultType necessary?
final case class CommandDescription(commandType: CommandType, template: (Any*) => String, produces: Option[Path]) {
  def commandString(params: Any*): String = template((params ++ produces): _*)
}

/*
 * combine {
 *   type = "simple" 
 *   template = "foo %s --bar %s > %s"
 *   produces = "/path/to/expected/results"
 * }
 */
object CommandDescription extends App {

  def fromConfig(config: Config): Try[CommandDescription] = {
    import ConfigEnrichments._

    def toTemplateFn(template: String): (Any*) => String = { params =>
      template.format(params: _*)
    }
    
    for {
      typeName <- config.tryString("type")
      tpe <- CommandType.fromString(typeName)
      template <- config.tryString("template")
      produces <- config.tryString("produces")
      expectation = Option(Paths.get(produces))
    } yield {
      CommandDescription(tpe, toTemplateFn(template), expectation)
    }
  }
}