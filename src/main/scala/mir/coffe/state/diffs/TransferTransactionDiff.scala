package mir.coffe.state.diffs

import cats.implicits._
import mir.coffe.settings.FunctionalitySettings
import mir.coffe.state._
import mir.coffe.account.Address
import mir.coffe.transaction.ValidationError
import mir.coffe.transaction.ValidationError.GenericError
import mir.coffe.transaction.transfer._

import scala.util.Right

object TransferTransactionDiff {
  def apply(blockchain: Blockchain, s: FunctionalitySettings, blockTime: Long, height: Int)(
      tx: TransferTransaction): Either[ValidationError, Diff] = {
    val sender = Address.fromPublicKey(tx.sender.publicKey)

    val isInvalidEi = for {
      recipient <- blockchain.resolveAlias(tx.recipient)
      _ <- Either.cond((tx.feeAssetId >>= blockchain.assetDescription >>= (_.script)).isEmpty,
                       (),
                       GenericError("Smart assets can't participate in TransferTransactions as a fee"))
      portfolios = (tx.assetId match {
        case None =>
          Map(sender -> Portfolio(-tx.amount, LeaseBalance.empty, Map.empty)).combine(
            Map(recipient -> Portfolio(tx.amount, LeaseBalance.empty, Map.empty))
          )
        case Some(aid) =>
          Map(sender -> Portfolio(0, LeaseBalance.empty, Map(aid -> -tx.amount))).combine(
            Map(recipient -> Portfolio(0, LeaseBalance.empty, Map(aid -> tx.amount)))
          )
      }).combine(
        tx.feeAssetId match {
          case None => Map(sender -> Portfolio(-tx.fee, LeaseBalance.empty, Map.empty))
          case Some(aid) =>
            val senderPf = Map(sender -> Portfolio(0, LeaseBalance.empty, Map(aid -> -tx.fee)))
            if (height >= Sponsorship.sponsoredFeesSwitchHeight(blockchain, s)) {
              val sponsorPf = blockchain
                .assetDescription(aid)
                .collect {
                  case desc if desc.sponsorship > 0 =>
                    val feeInCoffe = Sponsorship.toCoffe(tx.fee, desc.sponsorship)
                    Map(desc.issuer.toAddress -> Portfolio(-feeInCoffe, LeaseBalance.empty, Map(aid -> tx.fee)))
                }
                .getOrElse(Map.empty)
              senderPf.combine(sponsorPf)
            } else senderPf
        }
      )
      assetIssued    = tx.assetId.forall(blockchain.assetDescription(_).isDefined)
      feeAssetIssued = tx.feeAssetId.forall(blockchain.assetDescription(_).isDefined)
    } yield (portfolios, blockTime > s.allowUnissuedAssetsUntil && !(assetIssued && feeAssetIssued))

    isInvalidEi match {
      case Left(e) => Left(e)
      case Right((portfolios, invalid)) =>
        if (invalid)
          Left(GenericError(s"Unissued assets are not allowed after allowUnissuedAssetsUntil=${s.allowUnissuedAssetsUntil}"))
        else
          Right(Diff(height, tx, portfolios))
    }
  }
}
