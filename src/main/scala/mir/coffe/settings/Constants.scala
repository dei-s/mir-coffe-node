package mir.coffe.settings

import mir.coffe.Version
import mir.coffe.utils.ScorexLogging

/**
  * System constants here.
  */
object Constants extends ScorexLogging {
  val ApplicationName = "coffe"
  val AgentName       = s"Coffe v${Version.VersionString}"

  val UnitsInWave = 100000000L
  val TotalCoffe  = 100000000L
}
