package lgtv

import java.io.File

import cats.{ApplicativeError, MonadError}
import cats.implicits._
import com.typesafe.config.ConfigFactory.{defaultReference, parseFileAnySyntax, parseResourcesAnySyntax}
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

final case class LGTVConfig(tvIp: String, wolIp: String, macAddress: String, keycode: String)

object LGTVConfig {
  lazy val config: Config = ConfigFactory.load()
  def load[F[_]](implicit F: MonadError[F, Throwable]): F[LGTVConfig] =
    loadDefaultTypesafeConfig[F].flatMap(conf => F.catchNonFatal(conf.as[LGTVConfig]("lgtv")))

  private def loadDefaultTypesafeConfig[F[_]](implicit F: MonadError[F, Throwable]): F[Config] =
    F.catchNonFatal {
      scala.util.Properties
        .propOrNone("config.file")
        .map(config => parseFileAnySyntax(new File(config)))
        .getOrElse(ConfigFactory.empty())
    }
      .flatMap(loadTypesafeConfig[F](_))

  private def loadTypesafeConfig[F[_]](baseConfig: Config)(implicit F: ApplicativeError[F, Throwable]): F[Config] =
    F.catchNonFatal {
      baseConfig
        .withFallback(parseResourcesAnySyntax("application"))
        .withFallback(defaultReference())
        .resolve()
    }
}
