package mir.coffe.state.diffs

import cats.implicits._
import mir.coffe.account.Address
import mir.coffe.metrics.Instrumented
import mir.coffe.settings.FunctionalitySettings
import mir.coffe.state.{Blockchain, ByteStr, Diff, Portfolio}
import mir.coffe.transaction.ValidationError.AccountBalanceError
import mir.coffe.utils.ScorexLogging

import scala.util.{Left, Right}

object BalanceDiffValidation extends ScorexLogging with Instrumented {

  def apply(b: Blockchain, currentHeight: Int, fs: FunctionalitySettings)(d: Diff): Either[AccountBalanceError, Diff] = {
    val changedAccounts = d.portfolios.keySet

    def check(acc: Address): Option[(Address, String)] = {
      val portfolioDiff = d.portfolios(acc)

      val balance       = portfolioDiff.balance
      lazy val oldCoffe = b.balance(acc, None)
      lazy val oldLease = b.leaseBalance(acc)
      lazy val lease    = cats.Monoid.combine(oldLease, portfolioDiff.lease)
      (if (balance < 0) {
         val newB = oldCoffe + balance

         if (newB < 0) {
           Some(acc -> s"negative coffe balance: $acc, old: ${oldCoffe}, new: ${newB}")
         } else if (newB < lease.out && currentHeight > fs.allowLeasedBalanceTransferUntilHeight) {
           Some(acc -> (if (newB + lease.in - lease.out < 0) {
                          s"negative effective balance: $acc, old: ${(oldCoffe, oldLease)}, new: ${(newB, lease)}"
                        } else if (portfolioDiff.lease.out == 0) {
                          s"$acc trying to spend leased money"
                        } else {
                          s"leased being more than own: $acc, old: ${(oldCoffe, oldLease)}, new: ${(newB, lease)}"
                        }))
         } else {
           None
         }
       } else {
         None
       }) orElse (portfolioDiff.assets find {
        case (a, c) =>
          // Tokens it can produce overflow are exist.
          val oldB = b.balance(acc, Some(a))
          val newB = oldB + c
          newB < 0
      } map { _ =>
        acc -> s"negative asset balance: $acc, new portfolio: ${negativeAssetsInfo(b.portfolio(acc).combine(portfolioDiff))}"
      })
    }

    val positiveBalanceErrors: Map[Address, String] = changedAccounts.flatMap(check).toMap

    if (positiveBalanceErrors.isEmpty) {
      Right(d)
    } else {
      Left(AccountBalanceError(positiveBalanceErrors))
    }
  }

  private def negativeAssetsInfo(p: Portfolio): Map[ByteStr, Long] = p.assets.filter(_._2 < 0)
}
