package mir.coffe

import mir.coffe.utils.base58Length
import mir.coffe.block.{Block, MicroBlock}

package object transaction {

  type AssetId = mir.coffe.state.ByteStr
  val AssetIdLength: Int       = mir.coffe.crypto.DigestSize
  val AssetIdStringLength: Int = base58Length(AssetIdLength)
  type DiscardedTransactions = Seq[Transaction]
  type DiscardedBlocks       = Seq[Block]
  type DiscardedMicroBlocks  = Seq[MicroBlock]
  type AuthorizedTransaction = Authorized with Transaction
}
