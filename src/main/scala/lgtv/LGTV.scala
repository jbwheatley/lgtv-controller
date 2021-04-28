package lgtv

import cats.effect.IO
import com.comcast.ip4s.{IpLiteralSyntax, SocketAddress}
import fs2.Chunk
import fs2.io.net.{Datagram, Network, SocketOption}

class LGTV(
    encryption: Encryption,
    config: LGTVConfig,
    network: Network[IO]
) {
  private val magicPacket: Chunk[Byte] = {
    val m = config.macAddress.split(':').map("0x" + _).map(Integer.decode).map(_.byteValue())
    Chunk.array(Array.fill(6)(Byte.MaxValue) ++ Array.fill(16)(m).flatten)
  }

  private val udpOptions: List[SocketOption] = SocketOption.broadcast(true) :: Nil

  def wake(): IO[Unit] =
    network
      .openDatagramSocket(options = udpOptions)
      .use(_.write(Datagram(SocketAddress(config.wolIp, port"9"), magicPacket)))

  def sendCommand(command: Command): IO[Unit] =
    network
      .client(SocketAddress(config.tvIp, port"9761"))
      .use { socket =>
        encryption.encrypt(command).flatMap { cEnc =>
          socket.write(Chunk.array(cEnc.message))
        }
      }
}
