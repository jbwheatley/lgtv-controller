package lgtv

import cats.Traverse.ops.toAllTraverseOps
import cats.effect.{IO, Timer}
import lgtv.Command.SetVolume
import cats.syntax.applicative._
import cats.implicits.catsStdInstancesForList

import scala.concurrent.duration.DurationInt

trait CommandInterpreter {
  def interpret(command: String): IO[Unit]
}

object CommandInterpreter {
  def apply(tv: LGTV)(implicit timer: Timer[IO]): CommandInterpreter =
    (command: String) => {

      def _interpret(command: String): IO[Either[Command, List[Command]]] =
        command match {
          //single commands
          case "off"          => Left(Command.PowerOff).pure[IO]
          case "mute"         => Left(Command.Mute(true)).pure[IO]
          case "unmute"       => Left(Command.Mute(false)).pure[IO]
          case "right"        => Left(Command.RightArrow).pure[IO]
          case "left"         => Left(Command.LeftArrow).pure[IO]
          case "up"           => Left(Command.UpArrow).pure[IO]
          case "down"         => Left(Command.DownArrow).pure[IO]
          case "ok"           => Left(Command.Ok).pure[IO]
          case "back"         => Left(Command.Back).pure[IO]
          case "exit"         => Left(Command.Exit).pure[IO]
          case "hdmi1"        => Left(Command.HDMI1).pure[IO]
          case "hdmi2"        => Left(Command.HDMI2).pure[IO]
          case "hdmi3"        => Left(Command.HDMI3).pure[IO]
          case SetVolume(lvl) => Left(Command.SetVolume(lvl)).pure[IO]
          //complex commands
          case "netflix" => Right(List(Command.MyApp, Command.RightArrow, Command.RightArrow, Command.Ok)).pure[IO]
          case "disney"  => Right(List(Command.MyApp, Command.RightArrow, Command.Ok)).pure[IO]
          case "amazon"  => Right((Command.MyApp :: List.fill(8)(Command.RightArrow)) :+ Command.Ok).pure[IO]
          case x         => IO.raiseError(new Exception(s"Unrecognised command: $x"))
        }

      command.trim.toLowerCase match {
        case "on" => tv.wake()
        case com =>
          _interpret(com)
            .flatMap {
              case Left(c)                    => tv.sendCommand(c)
              case Right(cs) if cs.length > 8 => cs.traverse_(c => timer.sleep(100.millis) *> tv.sendCommand(c))
              case Right(cs)                  => cs.traverse_(tv.sendCommand)
            }
            .handleErrorWith(e => IO(println(e)))
      }
    }
}
