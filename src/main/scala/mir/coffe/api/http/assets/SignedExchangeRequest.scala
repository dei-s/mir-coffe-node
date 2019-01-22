package mir.coffe.api.http.assets

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{Format, Json}
import mir.coffe.account.PublicKeyAccount
import mir.coffe.api.http.BroadcastRequest
import mir.coffe.transaction.TransactionParsers.SignatureStringLength
import mir.coffe.transaction.ValidationError
import mir.coffe.transaction.ValidationError.GenericError
import mir.coffe.transaction.assets.exchange._

object SignedExchangeRequest {
  implicit val orderFormat: Format[Order]                                 = mir.coffe.transaction.assets.exchange.OrderJson.orderFormat
  implicit val signedExchangeRequestFormat: Format[SignedExchangeRequest] = Json.format
}

case class SignedExchangeRequest(@ApiModelProperty(value = "Base58 encoded sender public key", required = true)
                                 senderPublicKey: String,
                                 @ApiModelProperty(value = "Buy Order")
                                 order1: Order,
                                 @ApiModelProperty(value = "Sell Order")
                                 order2: Order,
                                 @ApiModelProperty(required = true, example = "1000000")
                                 amount: Long,
                                 @ApiModelProperty(required = true)
                                 price: Long,
                                 @ApiModelProperty(required = true)
                                 fee: Long,
                                 @ApiModelProperty(required = true)
                                 buyMatcherFee: Long,
                                 @ApiModelProperty(required = true)
                                 sellMatcherFee: Long,
                                 @ApiModelProperty(required = true)
                                 timestamp: Long,
                                 @ApiModelProperty(required = true)
                                 signature: String)
    extends BroadcastRequest {
  def toTx: Either[ValidationError, ExchangeTransaction] =
    for {
      _sender    <- PublicKeyAccount.fromBase58String(senderPublicKey)
      _signature <- parseBase58(signature, "invalid.signature", SignatureStringLength)
      o1         <- castOrder(order1)
      o2         <- castOrder(order2)
      _t         <- ExchangeTransactionV1.create(o1, o2, amount, price, buyMatcherFee, sellMatcherFee, fee, timestamp, _signature)
    } yield _t

  def castOrder(o: Order): Either[ValidationError, OrderV1] = o match {
    case o1 @ OrderV1(_, _, _, _, _, _, _, _, _, _) => Right(o1)
    case _                                          => Left(GenericError("ExchangeTransaction of version 1 can only contain orders of version 1"))
  }
}
