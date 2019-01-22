package mir.coffe.matcher.api
import mir.coffe.account.Address
import mir.coffe.transaction.assets.exchange.AssetPair

case class BatchCancel(address: Address, assetPair: Option[AssetPair], timestamp: Long)
