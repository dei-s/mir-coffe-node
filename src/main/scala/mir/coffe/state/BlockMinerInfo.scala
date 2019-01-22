package mir.coffe.state

import mir.coffe.block.Block.BlockId
import mir.coffe.consensus.nxt.NxtLikeConsensusBlockData

case class BlockMinerInfo(consensus: NxtLikeConsensusBlockData, timestamp: Long, blockId: BlockId)
