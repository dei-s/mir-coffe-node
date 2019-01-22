package mir.coffe.state

import mir.coffe.account.Address
import mir.coffe.crypto.SignatureLength
import mir.coffe.db.WithDomain
import mir.coffe.lagonaki.mocks.TestBlock
import mir.coffe.transaction.GenesisTransaction
import mir.coffe.{NoShrink, TestTime, TransactionGen}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FreeSpec, Matchers}

class CommonSpec extends FreeSpec with Matchers with WithDomain with TransactionGen with PropertyChecks with NoShrink {
  private val time          = new TestTime
  private def nextTs        = time.getTimestamp()
  private val AssetIdLength = 32

  private def genesisBlock(genesisTs: Long, address: Address, initialBalance: Long) = TestBlock.create(
    genesisTs,
    ByteStr(Array.fill[Byte](SignatureLength)(0)),
    Seq(GenesisTransaction.create(address, initialBalance, genesisTs).explicitGet())
  )

  "Common Conditions" - {
    "Zero balance of absent asset" in forAll(accountGen, positiveLongGen, byteArrayGen(AssetIdLength)) {
      case (sender, initialBalance, assetId) =>
        withDomain() { d =>
          d.appendBlock(genesisBlock(nextTs, sender, initialBalance))
//          d.portfolio(sender).balanceOf(Some(ByteStr(assetId))) shouldEqual 0L
        }
    }
  }
}
