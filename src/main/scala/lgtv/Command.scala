package lgtv

sealed trait Command {
  def message: String
}

object Command {
  sealed abstract class RawCommand private[Command](val name: String) extends Command {
    def message = name + '\r'
  }

  sealed abstract class SwitchCommand private[Command](val name: String) extends Command {
    def switch: Boolean
    def message: String = s"$name ${if (switch) "on" else "off"}\r"
  }

  sealed abstract class NumericCommand private[Command](val name: String, val min: Int, val max: Int) extends Command {
    def level: Int
    def message: String = {
      val lev = level.min(max).max(min)
      s"$name $lev\r"
    }
  }

  sealed abstract class KeyCommand private[Command](val key: String) extends Command {
    def message: String = s"KEY_ACTION $key\r"
  }

  sealed abstract class InputCommand private[Command](val input: String) extends Command {
    def message: String = s"INPUT_SELECT $input\r"
  }

  //Power
  case object PowerOff extends RawCommand("POWER off")

  //Volume Controls
  final case class SetVolume(level: Int) extends NumericCommand("VOLUME_CONTROL", 0, 100)

  object SetVolume {
    def unapply(arg: String): Option[Int] = {
      if (arg.startsWith("volume")) {
        arg.substring(6).trim.toIntOption
      } else None
    }
  }

  final case class Mute(switch: Boolean) extends SwitchCommand("VOLUME_MUTE")

  //Input controls
  case object HDMI1 extends InputCommand("hdmi1")
  case object HDMI2 extends InputCommand("hdmi2")
  case object HDMI3 extends InputCommand("hdmi3")

  //Key Controls
  case object MyApp extends KeyCommand("myapp")
  case object RightArrow extends KeyCommand("arrowright")
  case object LeftArrow extends KeyCommand("arrowleft")
  case object DownArrow extends KeyCommand("arrowdown")
  case object UpArrow extends KeyCommand("arrowup")
  case object Back extends KeyCommand("returnback")
  case object Ok extends KeyCommand("ok")
  case object Exit extends KeyCommand("exit")
}
