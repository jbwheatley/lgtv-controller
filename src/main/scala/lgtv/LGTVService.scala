package lgtv

import cats.Monad
import cats.implicits._
import cats.effect.ExitCode

class LGTVService[F[_]: Monad](input: InputReader[F], interpreter: CommandInterpreter[F]) {
  def run: F[ExitCode] = for {
    com <- input.read
    exit <-
      if (com == "quit") ExitCode.Success.pure[F]
      else interpreter.interpret(com) *> run
  } yield exit
}
