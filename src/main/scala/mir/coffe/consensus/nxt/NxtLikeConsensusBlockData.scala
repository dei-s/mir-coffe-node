package mir.coffe.consensus.nxt

import mir.coffe.state.ByteStr

case class NxtLikeConsensusBlockData(baseTarget: Long, generationSignature: ByteStr)
