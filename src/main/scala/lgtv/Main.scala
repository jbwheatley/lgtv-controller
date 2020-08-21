package lgtv

import java.util.concurrent.Executors

import cats.effect.{Blocker, ExitCode, IO, IOApp}

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val config         = LGTVConfig()
    val blocker        = Blocker.liftExecutionContext(ExecutionContext.fromExecutor(Executors.newCachedThreadPool()))
    val udpSocketGroup = fs2.io.udp.SocketGroup[IO](blocker)
    val tcpSocketGroup = fs2.io.tcp.SocketGroup[IO](blocker)
    val tv             = new LGTV(new Encryption(config.keycode), config, udpSocketGroup, tcpSocketGroup)
    val interpreter    = CommandInterpreter(tv)
    val input          = InputReader.stdIn
    new LGTVService(input, interpreter).run(args)
  }
}
