package mir.coffe.state.diffs

import mir.coffe.features.BlockchainFeatures
import mir.coffe.features.FeatureProvider._
import mir.coffe.state.{Blockchain, Diff, LeaseBalance, Portfolio}
import mir.coffe.transaction.ValidationError
import mir.coffe.transaction.smart.SetScriptTransaction

import mir.coffe.transaction.ValidationError
import mir.coffe.transaction.ValidationError.GenericError
import mir.coffe.lang.v1.DenyDuplicateVarNames
import mir.coffe.utils.varNames

import scala.util.Right

object SetScriptTransactionDiff {
  def apply(blockchain: Blockchain, height: Int)(tx: SetScriptTransaction): Either[ValidationError, Diff] = {
    val scriptOpt = tx.script
    for {
      _ <- scriptOpt.fold(Right(()): Either[ValidationError, Unit]) { script =>
        if (blockchain.isFeatureActivated(BlockchainFeatures.SmartAccountTrading, height)) {
          Right(())
        } else {
          val version = script.version
          DenyDuplicateVarNames(version, varNames(version), script.expr).left.map(GenericError.apply)
        }
      }
    } yield {
      Diff(
        height = height,
        tx = tx,
        portfolios = Map(tx.sender.toAddress -> Portfolio(-tx.fee, LeaseBalance.empty, Map.empty)),
        scripts = Map(tx.sender.toAddress    -> scriptOpt)
      )
    }
  }
}
