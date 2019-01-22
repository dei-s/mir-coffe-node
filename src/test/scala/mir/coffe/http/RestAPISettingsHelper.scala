package mir.coffe.http

import com.typesafe.config.ConfigFactory
import mir.coffe.crypto
import mir.coffe.settings.RestAPISettings
import mir.coffe.utils.Base58

trait RestAPISettingsHelper {
  def apiKey: String = "test_api_key"

  lazy val MaxTransactionsPerRequest = 10000
  lazy val MaxAddressesPerRequest    = 10000

  lazy val restAPISettings = {
    val keyHash = Base58.encode(crypto.secureHash(apiKey.getBytes()))
    RestAPISettings.fromConfig(
      ConfigFactory
        .parseString(
          s"""
             |coffe.rest-api {
             |  api-key-hash = $keyHash
             |  transactions-by-address-limit = $MaxTransactionsPerRequest
             |  distribution-by-address-limit = $MaxAddressesPerRequest
             |}
           """.stripMargin
        )
        .withFallback(ConfigFactory.load()))
  }
}
