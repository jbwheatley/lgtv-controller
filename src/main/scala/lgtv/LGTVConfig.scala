package lgtv

final case class LGTVConfig(tvIp: String, wolIp: String, macAddress: String, keycode: String)

object LGTVConfig {
  def apply(): LGTVConfig = LGTVConfig(Secrets.tvIp, Secrets.wolIp, Secrets.macAddress, Secrets.keycode)
}
