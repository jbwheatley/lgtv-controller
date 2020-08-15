package lgtv

import cats.effect.Sync

trait InputReader[F[_]] {
  def read: F[String]
}

object InputReader {
  def stdIn[F[_]: Sync]: InputReader[F] = new InputReader[F] {
    def read: F[String] = Sync[F].delay(scala.io.StdIn.readLine("input your command > "))
  }
}
