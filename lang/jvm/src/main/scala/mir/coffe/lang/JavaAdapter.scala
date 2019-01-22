package mir.coffe.lang

import cats.kernel.Monoid
import mir.coffe.lang.ScriptVersion.Versions.V1
import mir.coffe.lang.v1.compiler.CompilerV1
import mir.coffe.lang.v1.compiler.Terms.EXPR
import mir.coffe.lang.v1.evaluator.ctx.impl.{CryptoContext, PureContext}
import mir.coffe.lang.v1.evaluator.ctx.impl.coffe.CoffeContext

object JavaAdapter {
  private val version = V1

  lazy val compiler =
    new CompilerV1(
      Monoid.combineAll(Seq(
        CryptoContext.compilerContext(mir.coffe.lang.Global),
        CoffeContext.build(version, null, false).compilerContext,
        PureContext.build(version).compilerContext
      )))

  def compile(input: String): EXPR = {
    compiler
      .compile(input, List())
      .fold(
        error => throw new IllegalArgumentException(error),
        expr => expr
      )
  }
}
