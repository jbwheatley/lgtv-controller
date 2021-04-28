package lgtv

import com.comcast.ip4s.IpAddress

final case class LGTVConfig(tvIp: IpAddress, wolIp: IpAddress, macAddress: String, keycode: String)

object LGTVConfig {
  //Just loading from an object because too lazy to deal with config files
  def apply(): LGTVConfig =
    LGTVConfig(
      Secrets.tvIp,
      Secrets.wolIp,
      Secrets.macAddress,
      Secrets.keycode
    )
}
