package loamstream.config

import org.scalatest.FunSuite
import scala.reflect.runtime.universe._
import com.typesafe.config.ConfigFactory
import loamstream.commands.CommandType

/**
 * @author clint
 * date: Mar 16, 2016
 */
final class LoamConfigTest extends FunSuite {
  test("Should be able to parse a config file properly") {
    val configString = """
      loamstream {
        commands {
          foo {
            type = "unix"
            params = ["scala.Int", "scala.Int"]
            result = "java.lang.String"
            template = "foo %d --bar %d"
          }
          bar {
            type = "unix"
            params = ["scala.Double", "java.lang.String", "scala.Long"]
            result = "scala.Int"
            template = "bar %s --bar %s --baz %d"
          }
        }
      }
      """
    
    val conf = ConfigFactory.parseString(configString)
    
    val loamConf = LoamConfig.fromConfig(conf).get
    
    val foo = loamConf.commands("foo")
    
    val bar = loamConf.commands("bar")
    
    assert(loamConf.commands.size == 2)
    
    assert(foo.tpe == CommandType.Unix)
    assert(foo.paramTypes == Seq(toType[Int], toType[Int]))
    assert(foo.resultType == toType[java.lang.String])
    assert(foo.commandString(42, 99) == "foo 42 --bar 99")
    
    assert(bar.tpe == CommandType.Unix)
    assert(bar.paramTypes == Seq(toType[Double], toType[java.lang.String], toType[Long]))
    assert(bar.resultType == toType[Int])
    assert(bar.commandString(1.23d, "asdf", 42L) == "bar 1.23 --bar asdf --baz 42")
  }
  
  private def toType[A : TypeTag]: Type = typeTag[A].tpe 
}