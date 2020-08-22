package lgtv

import java.net.InetSocketAddress

import cats.effect.{Concurrent, ContextShift, IO, Resource}
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
  private val magicPacket: Chunk[Byte] = {
    val m = config.macAddress.split(':').map("0x" + _).map(Integer.decode).map(_.byteValue())
    Chunk.bytes(Array.fill(6)(Byte.MaxValue) ++ Array.fill(16)(m).flatten)
  }

  def wake(): IO[Unit] =
    udpSocketGroup
      .flatMap(_.open[IO]())
      .use(_.write(Packet(new InetSocketAddress(config.wolIp, 9), magicPacket)))

  def sendCommand(command: Command): IO[Unit] =
    tcpSocketGroup.flatMap(_.client(new InetSocketAddress(config.tvIp, 9761))).use { socket =>
      encryption.encrypt(command).flatMap { cEnc =>
        socket.write(Chunk.bytes(cEnc.message))
      }
    }
}
