package mir.coffe.matcher.api

import com.google.common.primitives.Longs
import mir.coffe.account.PublicKeyAccount
import mir.coffe.crypto
import mir.coffe.state.ByteStr
import mir.coffe.utils.Base58
import io.swagger.annotations.ApiModelProperty
import monix.eval.Coeval
import play.api.libs.json._

case class CancelOrderRequest(@ApiModelProperty(dataType = "java.lang.String") sender: PublicKeyAccount,
                              @ApiModelProperty(dataType = "java.lang.String") orderId: Option[ByteStr],
                              @ApiModelProperty() timestamp: Option[Long],
                              @ApiModelProperty(dataType = "java.lang.String") signature: Array[Byte]) {

  @ApiModelProperty(hidden = true)
  lazy val toSign: Array[Byte] = (orderId, timestamp) match {
    case (Some(oid), _)   => sender.publicKey ++ oid.arr
    case (None, Some(ts)) => sender.publicKey ++ Longs.toByteArray(ts)
    case (None, None)     => signature // Signature can't sign itself
  }
  @ApiModelProperty(hidden = true)
  val isSignatureValid: Coeval[Boolean] = Coeval.evalOnce(crypto.verify(signature, toSign, sender.publicKey))
}

object CancelOrderRequest {
  implicit val byteArrayFormat: Format[Array[Byte]] = Format(
    {
      case JsString(base58String) => Base58.decode(base58String).fold(_ => JsError("Invalid signature"), b => JsSuccess(b))
      case other                  => JsError(s"Expecting string but got $other")
    },
    b => JsString(Base58.encode(b))
  )

  implicit val pkFormat: Format[PublicKeyAccount] = Format(
    {
      case JsString(value) => PublicKeyAccount.fromBase58String(value).fold(_ => JsError("Invalid public key"), pk => JsSuccess(pk))
      case other           => JsError(s"Expecting string but got $other")
    },
    pk => JsString(Base58.encode(pk.publicKey))
  )

  implicit val format: OFormat[CancelOrderRequest] = Json.format
}
