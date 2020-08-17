package lgtv

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val config      = LGTVConfig()
    val tv          = new LGTV(new Encryption(config.keycode))(config)
    val interpreter = CommandInterpreter(tv)
    val input       = InputReader.stdIn
    new LGTVService(input, interpreter).run(args)
  }
}
