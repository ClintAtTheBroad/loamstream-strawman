package loamstream.config

import org.scalatest.FunSuite
import scala.reflect.runtime.universe._
import com.typesafe.config.ConfigFactory
import loamstream.commands.CommandType
import java.nio.file.Paths

/**
 * @author clint
 * date: Mar 16, 2016
 */
final class LoamConfigTest extends FunSuite {
  test("Should be able to parse a config file properly") {
    val complexCommand = """perl -e 'foreach my $line (<>) {chomp $line; my $size = length($line); print substr($line,0,1),"x",$size,"\n";}'"""
    val threeQuotes = "\"\"\""
    
    val quotedComplexCommand = s"$threeQuotes$complexCommand$threeQuotes"
    
    val configString = s"""
      loamstream {
        commands {
          foo {
            type = simple
            template = "foo %d --bar %d"
            produces = "/path/to/expected/results"
          }
          bar {
            type = transform
            template = "bar %s --bar %s --baz %d"
            produces = "/path/to/expected/results"
          }
          baz {
            type = transform
            template = $quotedComplexCommand
            produces = "/foo/bar/baz"
          }
        }
      }
      """
    
    val conf = ConfigFactory.parseString(configString)
    
    val loamConf = LoamConfig.fromConfig(conf).get
    
    val foo = loamConf.commands("foo")
    
    val bar = loamConf.commands("bar")
    
    val baz = loamConf.commands("baz")
    
    assert(loamConf.commands.size == 3)
    
    assert(foo.commandString(42, 99) == "foo 42 --bar 99")
    assert(foo.produces == Some(Paths.get("/path/to/expected/results")))
    
    assert(bar.commandString(1.23d, "asdf", 42L) == "bar 1.23 --bar asdf --baz 42")
    assert(bar.produces == Some(Paths.get("/path/to/expected/results")))
    
    assert(baz.commandString() == complexCommand)
    assert(baz.produces == Some(Paths.get("/foo/bar/baz")))
  }
}