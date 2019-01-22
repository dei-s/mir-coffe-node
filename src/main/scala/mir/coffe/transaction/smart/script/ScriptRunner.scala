package mir.coffe.transaction.smart.script

import cats.implicits._
import mir.coffe.account.AddressScheme
import mir.coffe.lang.v1.compiler.Terms.EVALUATED
import mir.coffe.lang.v1.evaluator.EvaluatorV1
import mir.coffe.lang.{ExecutionError, ExprEvaluator}
import mir.coffe.state._
import mir.coffe.transaction.Transaction
import mir.coffe.transaction.assets.exchange.Order
import mir.coffe.transaction.smart.BlockchainContext
import monix.eval.Coeval
import shapeless._

object ScriptRunner {

  def apply[A <: EVALUATED](height: Int,
                            in: Transaction :+: Order :+: CNil,
                            blockchain: Blockchain,
                            script: Script,
                            isTokenScript: Boolean): (ExprEvaluator.Log, Either[ExecutionError, A]) = {
    script match {
      case Script.Expr(expr) =>
        val ctx = BlockchainContext.build(
          script.version,
          AddressScheme.current.chainId,
          Coeval.evalOnce(in),
          Coeval.evalOnce(height),
          blockchain,
          isTokenScript
        )
        EvaluatorV1.applywithLogging[A](ctx, expr)

      case _ => (List.empty, "Unsupported script version".asLeft[A])
    }
  }
}
