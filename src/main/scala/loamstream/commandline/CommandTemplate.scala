package loamstream.commandline

/**
 * @author clint
 * date: Mar 15, 2016
 */
final case class CommandTemplate(command: String, params: String*) {
  private def paramString = params.mkString(" ")
  
  def toCommandLine = s"$command $paramString}"
}