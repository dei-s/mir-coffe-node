package mir.coffe.transaction.smart

import mir.coffe.account.AddressOrAlias
import mir.coffe.lang.v1.traits._
import mir.coffe.lang.v1.traits.domain.Recipient._
import mir.coffe.lang.v1.traits.domain.{Ord, Recipient, Tx}
import mir.coffe.state._
import mir.coffe.transaction.Transaction
import mir.coffe.transaction.assets.exchange.Order
import monix.eval.Coeval
import scodec.bits.ByteVector
import shapeless._

class CoffeEnvironment(nByte: Byte, in: Coeval[Transaction :+: Order :+: CNil], h: Coeval[Int], blockchain: Blockchain) extends Environment {
  override def height: Long = h()

  override def inputEntity: Tx :+: Ord :+: CNil = {
    in.apply()
      .map(InputPoly)
  }

  override def transactionById(id: Array[Byte]): Option[Tx] =
    blockchain
      .transactionInfo(ByteStr(id))
      .map(_._2)
      .map(RealTransactionWrapper(_))

  override def data(recipient: Recipient, key: String, dataType: DataType): Option[Any] = {
    for {
      address <- recipient match {
        case Address(bytes) =>
          mir.coffe.account.Address
            .fromBytes(bytes.toArray)
            .toOption
        case Alias(name) =>
          mir.coffe.account.Alias
            .buildWithCurrentChainId(name)
            .flatMap(blockchain.resolveAlias)
            .toOption
      }
      data <- blockchain
        .accountData(address, key)
        .map((_, dataType))
        .flatMap {
          case (IntegerDataEntry(_, value), DataType.Long)     => Some(value)
          case (BooleanDataEntry(_, value), DataType.Boolean)  => Some(value)
          case (BinaryDataEntry(_, value), DataType.ByteArray) => Some(ByteVector(value.arr))
          case (StringDataEntry(_, value), DataType.String)    => Some(value)
          case _                                               => None
        }
    } yield data
  }
  override def resolveAlias(name: String): Either[String, Recipient.Address] =
    blockchain
      .resolveAlias(mir.coffe.account.Alias.buildWithCurrentChainId(name).explicitGet())
      .left
      .map(_.toString)
      .right
      .map(a => Recipient.Address(ByteVector(a.bytes.arr)))

  override def chainId: Byte = nByte

  override def accountBalanceOf(addressOrAlias: Recipient, maybeAssetId: Option[Array[Byte]]): Either[String, Long] = {
    (for {
      aoa <- addressOrAlias match {
        case Address(bytes) => AddressOrAlias.fromBytes(bytes.toArray, position = 0).map(_._1)
        case Alias(name)    => mir.coffe.account.Alias.buildWithCurrentChainId(name)
      }
      address <- blockchain.resolveAlias(aoa)
      balance = blockchain.balance(address, maybeAssetId.map(ByteStr(_)))
      safeBalance <- Either.cond(balance != 0L || blockchain.balance(address, None) == 0L, balance, "Balance of absent asset")
    } yield safeBalance).left.map(_.toString)
  }
  override def transactionHeightById(id: Array[Byte]): Option[Long] =
    blockchain.transactionHeight(ByteStr(id)).map(_.toLong)
}
