package mir.coffe.settings

import com.typesafe.config.Config
import mir.coffe.matcher.MatcherSettings
import mir.coffe.metrics.Metrics
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import scala.concurrent.duration._

case class CoffeSettings(directory: String,
                         dataDirectory: String,
                         maxCacheSize: Int,
                         maxRollbackDepth: Int,
                         rememberBlocks: FiniteDuration,
                         ntpServer: String,
                         networkSettings: NetworkSettings,
                         walletSettings: WalletSettings,
                         blockchainSettings: BlockchainSettings,
                         checkpointsSettings: CheckpointsSettings,
                         matcherSettings: MatcherSettings,
                         minerSettings: MinerSettings,
                         restAPISettings: RestAPISettings,
                         synchronizationSettings: SynchronizationSettings,
                         utxSettings: UtxSettings,
                         featuresSettings: FeaturesSettings,
                         metrics: Metrics.Settings)

object CoffeSettings {

  import NetworkSettings.networkSettingsValueReader

  val configPath: String = "coffe"

  def fromConfig(config: Config): CoffeSettings = {
    val directory               = config.as[String](s"$configPath.directory")
    val dataDirectory           = config.as[String](s"$configPath.data-directory")
    val maxCacheSize            = config.as[Int](s"$configPath.max-cache-size")
    val maxRollbackDepth        = config.as[Int](s"$configPath.max-rollback-depth")
    val rememberBlocks          = config.as[FiniteDuration](s"$configPath.remember-blocks-interval-in-cache")
    val ntpServer               = config.as[String](s"$configPath.ntp-server")
    val networkSettings         = config.as[NetworkSettings]("coffe.network")
    val walletSettings          = config.as[WalletSettings]("coffe.wallet")
    val blockchainSettings      = BlockchainSettings.fromConfig(config)
    val checkpointsSettings     = CheckpointsSettings.fromConfig(config)
    val matcherSettings         = MatcherSettings.fromConfig(config)
    val minerSettings           = MinerSettings.fromConfig(config)
    val restAPISettings         = RestAPISettings.fromConfig(config)
    val synchronizationSettings = SynchronizationSettings.fromConfig(config)
    val utxSettings             = config.as[UtxSettings]("coffe.utx")
    val featuresSettings        = config.as[FeaturesSettings]("coffe.features")
    val metrics                 = config.as[Metrics.Settings]("metrics")

    CoffeSettings(
      directory,
      dataDirectory,
      maxCacheSize,
      maxRollbackDepth,
      rememberBlocks,
      ntpServer,
      networkSettings,
      walletSettings,
      blockchainSettings,
      checkpointsSettings,
      matcherSettings,
      minerSettings,
      restAPISettings,
      synchronizationSettings,
      utxSettings,
      featuresSettings,
      metrics
    )
  }
}
