package lgtv

import java.net.{DatagramPacket, DatagramSocket, InetSocketAddress, Socket}

import cats.effect.IO
import cats.implicits.{catsKernelStdMonoidForList, catsSyntaxSemigroup}

class LGTV(encryption: Encryption)(config: LGTVConfig) {
  private val magicPacket: Array[Byte] = {
    val m = config.macAddress.split(':').toList.map("#" + _).map(Integer.decode).map(_.byteValue()).combineN(16)
    (List.fill(6)(Byte.MaxValue) ++ m).toArray
  }

  def wake(): IO[Unit] =
    for {
      socket <- IO(new DatagramSocket())
      _ <- IO(
        socket.send(
          new DatagramPacket(magicPacket, magicPacket.length, new InetSocketAddress(config.wolIp, 9))
        )
      )
      _ <- IO(socket.close())
    } yield ()

  def sendCommand(command: Command): IO[Unit] =
    for {
      socket <- IO(new Socket(config.tvIp, 9761))
      out    <- IO(socket.getOutputStream)
      _      <- encryption.encrypt(command).map(cEnc => out.write(cEnc.message))
      _      <- IO(out.close())
    } yield ()
}
