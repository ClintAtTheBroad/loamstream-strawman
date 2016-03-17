package loamstream.commands

import scala.util.Try
import com.typesafe.config.Config
import scala.reflect.runtime.universe._
import loamstream.config.ConfigEnrichments

/**
 * @author clint
 * date: Mar 16, 2016
 */
final case class CommandDescription(tpe: CommandType, paramTypes: Seq[Type], resultType: Type, template: (Any*) => String) {
  def commandString(params: Any*): String = template(params: _*)
}

/*
 * combine {
 *   type = "unix"
 *   params = ["scala.Int"]
 *   result = "scala.Int"
 *   template = "foo %s --bar %s"
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
      params <- config.tryStringList("params")
      result <- config.tryString("result")
      template <- config.tryString("template")
    } yield {
      CommandDescription(tpe, params.map(toType), toType(result), toTemplateFn(template))
    }
  }
  
  private def toType(s: String): Type = {
    import scala.reflect.api

    def stringToTypeTag[A](name: String): TypeTag[A] = {
      val c = Class.forName(name) // obtain java.lang.Class object from a string
      val mirror = runtimeMirror(c.getClassLoader) // obtain runtime mirror
      val sym = mirror.staticClass(name) // obtain class symbol for `c`
      val tpe = sym.selfType // obtain type object for `c`
      // create a type tag which contains above type object
      TypeTag(mirror, new api.TypeCreator {
        def apply[U <: api.Universe with Singleton](m: api.Mirror[U]) =
          if (m eq mirror) tpe.asInstanceOf[U#Type]
          else throw new IllegalArgumentException(s"Type tag defined in $mirror cannot be migrated to other mirrors.")
      })
    }
    
    stringToTypeTag(s).tpe
  }
}