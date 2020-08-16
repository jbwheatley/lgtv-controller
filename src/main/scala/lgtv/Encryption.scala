package lgtv

import java.security.SecureRandom
import java.util.Base64

import cats.effect.IO
import javax.crypto.spec.{IvParameterSpec, PBEKeySpec, SecretKeySpec}
import javax.crypto.{Cipher, SecretKeyFactory}
import lgtv.Encryption.EncryptedMessage

class Encryption(code: String) {
  private val salt: Array[Byte] = Base64.getDecoder.decode("Y2G4DpvcpmONByDyzFaPuQ==")
  private val factory           = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
  private val spec              = new PBEKeySpec(code.toCharArray, salt, 16384, 128)
  private val keySpec           = new SecretKeySpec(factory.generateSecret(spec).getEncoded, "AES")

  private def ivSpecBuilder: IO[IvParameterSpec] = {
    val iv = new Array[Byte](16)
    IO(new SecureRandom().nextBytes(iv))
      .as(new IvParameterSpec(iv))
  }

  def encrypt(command: Command): IO[EncryptedMessage] =
    for {
      ivSpec <- ivSpecBuilder
      ecbCipher = Cipher.getInstance("AES/ECB/NoPadding")
      _ <- IO(ecbCipher.init(Cipher.ENCRYPT_MODE, keySpec))
      ivEnc     = ecbCipher.doFinal(ivSpec.getIV)
      cbcCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      _ <- IO(cbcCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec))
      messageEnc = cbcCipher.doFinal(command.message.getBytes("UTF-8"))
    } yield EncryptedMessage(ivEnc ++ messageEnc)
}

object Encryption {
  final case class EncryptedMessage(message: Array[Byte])
}
