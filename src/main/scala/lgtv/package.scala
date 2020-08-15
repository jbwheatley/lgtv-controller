import cats.effect.Sync

package object lgtv {
  implicit class syncOps[A](value: => A) {
    def delay[F[_]: Sync]: F[A] = Sync[F].delay(value)
  }
}
