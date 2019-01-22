package mir.coffe.matcher.smart

import cats.implicits._
import mir.coffe.account.AddressScheme
import mir.coffe.lang.ExprEvaluator.Log
import mir.coffe.lang.v1.compiler.Terms.EVALUATED
import mir.coffe.lang.v1.evaluator.EvaluatorV1
import mir.coffe.transaction.assets.exchange.Order
import mir.coffe.transaction.smart.script.Script
import monix.eval.Coeval

object MatcherScriptRunner {

  def apply[A <: EVALUATED](script: Script, order: Order, isTokenScript: Boolean): (Log, Either[String, A]) = script match {
    case Script.Expr(expr) =>
      val ctx = MatcherContext.build(script.version, AddressScheme.current.chainId, Coeval.evalOnce(order), !isTokenScript)
      EvaluatorV1.applywithLogging[A](ctx, expr)
    case _ => (List.empty, "Unsupported script version".asLeft[A])
  }
}
