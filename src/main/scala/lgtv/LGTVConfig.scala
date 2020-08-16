package lgtv

import java.io.File

import cats.effect.IO
import com.typesafe.config.ConfigFactory.{defaultReference, parseFileAnySyntax, parseResourcesAnySyntax}
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus.{stringValueReader, toFicusConfig}
import net.ceedubs.ficus.readers.ArbitraryTypeReader.arbitraryTypeValueReader

final case class LGTVConfig(tvIp: String, wolIp: String, macAddress: String, keycode: String)

object LGTVConfig {
  def load: IO[LGTVConfig] =
    loadDefaultTypesafeConfig.flatMap(conf => IO(conf.as[LGTVConfig]("lgtv")))

  private def loadDefaultTypesafeConfig: IO[Config] =
    IO {
      scala.util.Properties
        .propOrNone("config.file")
        .map(config => parseFileAnySyntax(new File(config)))
        .getOrElse(ConfigFactory.empty())
    }.flatMap(loadTypesafeConfig)

  private def loadTypesafeConfig(baseConfig: Config): IO[Config] =
    IO {
      baseConfig
        .withFallback(parseResourcesAnySyntax("application"))
        .withFallback(defaultReference())
        .resolve()
    }
}
