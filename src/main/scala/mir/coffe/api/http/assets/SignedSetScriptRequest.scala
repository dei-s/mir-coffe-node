package mir.coffe.api.http.assets

import cats.implicits._
import mir.coffe.account.PublicKeyAccount
import mir.coffe.api.http.BroadcastRequest
import mir.coffe.transaction.smart.SetScriptTransaction
import mir.coffe.transaction.smart.script.Script
import mir.coffe.transaction.{Proofs, ValidationError}
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.functional.syntax._
import play.api.libs.json._

object SignedSetScriptRequest {
  implicit val signedSetScriptRequestReads: Reads[SignedSetScriptRequest] = (
    (JsPath \ "version").read[Byte] and
      (JsPath \ "senderPublicKey").read[String] and
      (JsPath \ "script").readNullable[String] and
      (JsPath \ "fee").read[Long] and
      (JsPath \ "timestamp").read[Long] and
      (JsPath \ "proofs").read[List[ProofStr]]
  )(SignedSetScriptRequest.apply _)

  implicit val signedSetScriptRequestWrites: OWrites[SignedSetScriptRequest] = Json.writes[SignedSetScriptRequest]
}

@ApiModel(value = "Proven SetScript transaction")
case class SignedSetScriptRequest(@ApiModelProperty(required = true)
                                  version: Byte,
                                  @ApiModelProperty(value = "Base58 encoded sender public key", required = true)
                                  senderPublicKey: String,
                                  @ApiModelProperty(value = "Base64 encoded script(including version and checksum)", required = true)
                                  script: Option[String],
                                  @ApiModelProperty(required = true)
                                  fee: Long,
                                  @ApiModelProperty(required = true)
                                  timestamp: Long,
                                  @ApiModelProperty(required = true)
                                  proofs: List[String])
    extends BroadcastRequest {
  def toTx: Either[ValidationError, SetScriptTransaction] =
    for {
      _sender <- PublicKeyAccount.fromBase58String(senderPublicKey)
      _script <- script match {
        case None | Some("") => Right(None)
        case Some(s)         => Script.fromBase64String(s).map(Some(_))
      }
      _proofBytes <- proofs.traverse(s => parseBase58(s, "invalid proof", Proofs.MaxProofStringSize))
      _proofs     <- Proofs.create(_proofBytes)
      t           <- SetScriptTransaction.create(version, _sender, _script, fee, timestamp, _proofs)
    } yield t
}
