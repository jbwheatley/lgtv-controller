package lgtv

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    LGTVConfig.load[IO].flatMap { config =>
      val tv = new LGTV[IO](new Encryption[IO](config.keycode))(config)
      val interpreter = CommandInterpreter(tv)
      val input = InputReader.stdIn[IO]
      new LGTVService[IO](input, interpreter).run
    }
  }
}
