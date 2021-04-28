package lgtv

import cats.effect.{IO, Resource}
import com.comcast.ip4s.{IpLiteralSyntax, SocketAddress}
import fs2.Chunk
import fs2.io.net.{Datagram, DatagramSocketGroup, SocketGroup}

class LGTV(
    encryption: Encryption,
    config: LGTVConfig,
    udpSocketGroup: Resource[IO, DatagramSocketGroup[IO]],
    tcpSocketGroup: Resource[IO, SocketGroup[IO]]
) {
  private val magicPacket: Chunk[Byte] = {
    val m = config.macAddress.split(':').map("0x" + _).map(Integer.decode).map(_.byteValue())
    Chunk.array(Array.fill(6)(Byte.MaxValue) ++ Array.fill(16)(m).flatten)
  }

  def wake(): IO[Unit] =
    udpSocketGroup
      .flatMap(_.openDatagramSocket())
      .use(_.write(Datagram(SocketAddress(config.wolIp, port"9"), magicPacket)))

  def sendCommand(command: Command): IO[Unit] =
    tcpSocketGroup.flatMap(_.client(SocketAddress(config.wolIp, port"9761"))).use { socket =>
      encryption.encrypt(command).flatMap { cEnc =>
        socket.write(Chunk.array(cEnc.message))
      }
    }
}
