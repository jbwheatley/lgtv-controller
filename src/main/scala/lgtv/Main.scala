package lgtv

import java.util.concurrent.Executors

import cats.effect.{Blocker, ExitCode, IO, IOApp}

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val config         = LGTVConfig()
    val blocker        = Blocker[IO]
    val udpSocketGroup = blocker.flatMap(fs2.io.udp.SocketGroup[IO])
    val tcpSocketGroup = blocker.flatMap(b => fs2.io.tcp.SocketGroup[IO](b))
    val tv             = new LGTV(new Encryption(config.keycode), config, udpSocketGroup, tcpSocketGroup)
    val interpreter    = CommandInterpreter(tv)
    val input          = InputReader.stdIn
    new LGTVService(input, interpreter).run(args)
  }
}
