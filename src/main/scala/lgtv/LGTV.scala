package lgtv

import java.net.{DatagramPacket, DatagramSocket, InetSocketAddress, Socket}

import cats.effect.Sync
import cats.implicits._

class LGTV[F[_]: Sync](encryption: Encryption[F])(config: LGTVConfig) {
  def wake(): F[Unit] = {
    val magicPacket: Array[Byte] = {
      val m = config.macAddress.split(':').toList.map("#" + _).map(Integer.decode).map(_.byteValue()).combineN(16)
      (List.fill(6)(Byte.MaxValue) ++ m).toArray
    }
    for {
      socket <- new DatagramSocket().delay[F]
      _ <- socket.send(
        new DatagramPacket(magicPacket, magicPacket.length, new InetSocketAddress(config.wolIp, 9))
      ).delay[F]
      _ <- socket.close().delay[F]
    } yield ()
  }

  def sendCommand(command: Command): F[Unit] = for {
    socket <- new Socket(config.tvIp, 9761).delay[F]
    out <- socket.getOutputStream.delay[F]
    _ <- encryption.encrypt(command).map { cEnc => out.write(cEnc.message) }
    _ <- out.close().delay
  } yield ()
}
