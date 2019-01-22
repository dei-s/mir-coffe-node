package mir.coffe.transaction.smart

import cats.kernel.Monoid
import mir.coffe.lang.{Global, ScriptVersion}
import mir.coffe.lang.v1.evaluator.ctx.EvaluationContext
import mir.coffe.lang.v1.evaluator.ctx.impl.coffe.CoffeContext
import mir.coffe.lang.v1.evaluator.ctx.impl.{CryptoContext, PureContext}
import mir.coffe.state._
import mir.coffe.transaction._
import mir.coffe.transaction.assets.exchange.Order
import monix.eval.Coeval
import shapeless._

object BlockchainContext {

  type In = Transaction :+: Order :+: CNil
  def build(version: ScriptVersion,
            nByte: Byte,
            in: Coeval[In],
            h: Coeval[Int],
            blockchain: Blockchain,
            isTokenContext: Boolean): EvaluationContext = {
    Monoid
      .combineAll(
        Seq(
          PureContext.build(version),
          CryptoContext.build(Global),
          CoffeContext.build(version, new CoffeEnvironment(nByte, in, h, blockchain), isTokenContext)
        ))
      .evaluationContext
  }
}
