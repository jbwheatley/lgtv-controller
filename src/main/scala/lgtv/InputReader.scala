package lgtv

import cats.effect.IO

trait InputReader {
  def read: IO[String]
}

object InputReader {
  def stdIn: InputReader =
    new InputReader {
      def read: IO[String] = IO(scala.io.StdIn.readLine("input your command > "))
    }
}
