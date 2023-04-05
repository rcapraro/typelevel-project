package com.rockthejvm.jobsboard.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.derivation.default.*

final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {
  given hostReader: ConfigReader[Host] = ConfigReader[String].emap { hostString =>
    Host
      .fromString(hostString)
      .toRight(CannotConvert(hostString, Host.getClass.toString, s"Invalid Host string: $hostString"))
  }
  given portReader: ConfigReader[Port] = ConfigReader[Int].emap { portNumber =>
    Port
      .fromInt(portNumber)
      .toRight(CannotConvert(portNumber.toString, Port.getClass.toString, s"Invalid Port number: $portNumber"))
  }
}
