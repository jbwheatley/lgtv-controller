package lgtv

import java.net.InetSocketAddress

import cats.effect.{Concurrent, ContextShift, IO, Resource}
import cats.implicits.{catsKernelStdMonoidForList, catsSyntaxSemigroup}
import fs2.Chunk
import fs2.io.udp.Packet
import fs2.io.tcp
import fs2.io.udp

class LGTV(
    encryption: Encryption,
    config: LGTVConfig,
    udpSocketGroup: Resource[IO, udp.SocketGroup],
    tcpSocketGroup: Resource[IO, tcp.SocketGroup]
)(implicit CS: ContextShift[IO], Con: Concurrent[IO]) {
  private val magicPacket: Array[Byte] = {
    val m = config.macAddress.split(':').toList.map("#" + _).map(Integer.decode).map(_.byteValue()).combineN(16)
    (List.fill(6)(Byte.MaxValue) ++ m).toArray
  }

  def wake(): IO[Unit] =
    udpSocketGroup
      .flatMap(_.open[IO]())
      .use(_.write(Packet(new InetSocketAddress(config.wolIp, 9), Chunk.bytes(magicPacket))))

  def sendCommand(command: Command): IO[Unit] =
    tcpSocketGroup.flatMap(_.client(new InetSocketAddress(config.tvIp, 9761))).use { socket =>
      encryption.encrypt(command).flatMap { cEnc =>
        socket.write(Chunk.bytes(cEnc.message))
      }
    }
}
