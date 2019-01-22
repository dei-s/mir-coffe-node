package mir.coffe.state.diffs

import mir.coffe.features.BlockchainFeatures
import mir.coffe.state.{Blockchain, Diff, LeaseBalance, Portfolio}
import mir.coffe.transaction.ValidationError.GenericError
import mir.coffe.transaction.{CreateAliasTransaction, ValidationError}
import mir.coffe.features.FeatureProvider._

import scala.util.Right

object CreateAliasTransactionDiff {
  def apply(blockchain: Blockchain, height: Int)(tx: CreateAliasTransaction): Either[ValidationError, Diff] =
    if (blockchain.isFeatureActivated(BlockchainFeatures.DataTransaction, height) && !blockchain.canCreateAlias(tx.alias))
      Left(GenericError("Alias already claimed"))
    else
      Right(
        Diff(height = height,
             tx = tx,
             portfolios = Map(tx.sender.toAddress -> Portfolio(-tx.fee, LeaseBalance.empty, Map.empty)),
             aliases = Map(tx.alias               -> tx.sender.toAddress)))
}
