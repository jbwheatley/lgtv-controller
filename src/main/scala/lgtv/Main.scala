package lgtv

import cats.effect.{ExitCode, IO, IOApp}
import fs2.io.net.Network

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val config      = LGTVConfig()
    val network     = Network.forAsync[IO]
    val tv          = new LGTV(new Encryption(config.keycode), config, network)
    val interpreter = CommandInterpreter(tv)
    val input       = InputReader.stdIn
    new LGTVService(input, interpreter).run(args)
  }
}
