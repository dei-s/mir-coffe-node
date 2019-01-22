package mir.coffe.transaction

import mir.coffe.account.Address

case class AssetAcc(account: Address, assetId: Option[AssetId])
