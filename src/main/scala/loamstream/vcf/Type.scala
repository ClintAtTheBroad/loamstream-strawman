package loamstream.vcf

import scala.util.Try
import scala.util.Success
import loamstream.util.Tries

/**
 * @author clint
 * date: Feb 4, 2016
 * 
 * See @link http://www.1000genomes.org/node/101
 */
sealed trait Type {
  type T
  
  def name: String
}

object Type {
  //TODO: TEST
  def fromString(s: String): Try[Type] = {
    //TODO: Improve this boilerplate; make it DRY
    s.toLowerCase match {
      case "integer" => Success(Integer)
      case "float" => Success(Float)
      case "flag" => Success(Flag)
      case "character" => Success(Character)
      case "string" => Success(String)
      case _ => Tries.failure(s"Couldn't understand type name '$s'")
    }
  }
  
  //TODO: Revisit this
  trait Flag
  
  case object Integer extends Type {
    override type T = Int
    
    override val name = "Integer"
  }
  
  case object Float extends Type {
    override type T = Double
    
    override val name = "Float"
  }
  
  case object Flag extends Type {
    override type T = Flag
    
    override val name = "Flag"
  }
  
  case object Character extends Type {
    override type T = Char
    
    override val name = "Character"
  }
  
  case object String extends Type {
    override type T = String
    
    override val name = "String"
  }
}