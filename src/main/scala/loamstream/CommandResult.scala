package loamstream

import java.nio.file.Path

/**
 * @author clint
 * date: Mar 21, 2016
 */
final case class CommandResult(produced: Option[Path], exitStatus: Int) {
  //NB: Unsafe, will throw
  def path: Path = produced.get
}
