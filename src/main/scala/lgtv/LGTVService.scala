package lgtv

import cats.effect.{ExitCode, IO}
import cats.implicits.catsStdInstancesForList
import cats.Traverse.ops.toAllTraverseOps
import cats.effect.kernel.Temporal

import scala.concurrent.duration.DurationInt

class LGTVService(input: InputReader, interpreter: CommandInterpreter)(implicit timer: Temporal[IO]) {
  def run(args: List[String]): IO[ExitCode] =
    args match {
      case Nil     => noArgs
      case c :: cs => someArgs(c, cs)
    }

  def someArgs(com: String, cs: List[String]): IO[ExitCode] = {
    val sleep = if (com == "on") 5000.millis else 100.millis
    interpreter.interpret(com) *>
      cs.traverse_(c => timer.sleep(sleep) *> interpreter.interpret(c)).as(ExitCode.Success)
  }

  def noArgs: IO[ExitCode] =
    for {
      com <- input.read
      exit <-
        if (com == "quit")
          IO(println("Shutting down...")).as(ExitCode.Success)
        else interpreter.interpret(com) >> IO.defer(noArgs)
    } yield exit
}
