package lgtv

import cats.effect.{ExitCode, IO, Timer}
import cats.implicits.catsStdInstancesForList
import cats.Traverse.ops.toAllTraverseOps

import scala.concurrent.duration.DurationInt

class LGTVService(input: InputReader, interpreter: CommandInterpreter)(implicit timer: Timer[IO]) {
  def run(args: List[String]): IO[ExitCode] =
    args match {
      case Nil     => noArgs
      case c :: cs => someArgs(c, cs)
    }

  def someArgs(com: String, cs: List[String]): IO[ExitCode] =
    interpreter.interpret(com) *>
      cs.traverse_(c => timer.sleep(100.millis) *> interpreter.interpret(c)).as(ExitCode.Success)

  def noArgs: IO[ExitCode] =
    for {
      com <- input.read
      exit <-
        if (com == "quit") IO(ExitCode.Success)
        else interpreter.interpret(com) *> IO.suspend(noArgs)
    } yield exit
}
